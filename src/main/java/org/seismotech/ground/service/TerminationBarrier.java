package org.seismotech.ground.service;

import java.util.concurrent.TimeUnit;

/**
 * An auxiliary class to implement Service.awaitTermination().
 */
public class TerminationBarrier {

  private boolean terminated = false;

  public synchronized void terminated() {
    terminated = true;
    notifyAll();
  }

  public synchronized void awaitTermination()
  throws InterruptedException {
    while (!terminated) wait();
  }

  public synchronized boolean awaitTermination(long timeout, TimeUnit unit)
  throws InterruptedException {
    final long limit = System.nanoTime() + unit.toNanos(timeout);
    for (;;) {
      if (terminated) break;
      final long remaining = System.nanoTime() - limit;
      if (remaining <= 0) break;
      TimeUnit.NANOSECONDS.timedWait(this, remaining);
    }
    return terminated;
  }
}
