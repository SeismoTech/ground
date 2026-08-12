package org.seismotech.ground.io.watch;

import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.seismotech.ground.io.function.IOConsumer;
import org.seismotech.ground.time.Metronome;
import org.seismotech.ground.service.TerminationBarrier;

public class TraversalWatcher implements Watcher {

  private static final Logger logger
    = Logger.getLogger(TraversalWatcher.class.getName());

  private final TraversalWatcherMachine machine;
  private final long periodNanos;

  private Thread thread;
  private volatile RunningStatus status;
  private final TerminationBarrier termination;

  public TraversalWatcher(List<Path> roots, long periodNanos,
      Predicate<Path> accept, IOConsumer<WatchEvent> listener) {
    this.machine = new TraversalWatcherMachine(roots, accept, listener);
    this.periodNanos = periodNanos;
    this.thread = null;
    this.status = RunningStatus.CREATED;
    this.termination = new TerminationBarrier();
  }

  public synchronized RunningStatus runningStatus() {
    return status;
  }

  public synchronized TraversalWatcher start(ThreadFactory threads) {
    switch (status) {
    case CREATED -> _start(threads);
    case RUNNING -> {}
    default -> _cannotStart();
    }
    return this;
  }

  @Override
  public synchronized void shutdown() {
    if (thread == null) _done();
    else _shutdown();
  }

  @Override
  public void awaitTermination()
  throws InterruptedException {
    termination.awaitTermination();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
  throws InterruptedException {
    return termination.awaitTermination(timeout, unit);
  }

  private void _cannotStart() {
    throw new IllegalStateException("Cannot start at state " + status);
  }

  private void _start(ThreadFactory threads) {
    thread = threads.newThread(this::run);
    status = RunningStatus.RUNNING;
    thread.start();
  }

  private void _done() {
    thread = null;
    status = RunningStatus.TERMINATED;
    termination.terminated();
  }

  private void _shutdown() {
    thread.interrupt();
    status = RunningStatus.SHUTTINGDOWN;
  }

  private void run() {
    final Metronome metro = Metronome
      .aligned(periodNanos, TimeUnit.NANOSECONDS)
      .mode(Metronome.DelayMode.NEXT_PERIOD);
    loop:
    do {
      try {
        machine.traverse();
      } catch (InterruptedIOException e) {
        break loop;
      } catch (Exception e) {
        logger.log(Level.WARNING, "Traversal failure: " + e.getMessage(), e);
      }
      try {
        metro.sleep();
      } catch (InterruptedException e) {
        break loop;
      }
    } while (status == RunningStatus.RUNNING);
    synchronized (this) {_done();}
  }
}
