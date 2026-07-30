package org.seismotech.ground.time;

import java.util.concurrent.TimeUnit;

import org.seismotech.ground.math.DMath;

/**
 * An utility to simplify periodic execution.
 */
public class Metronome {

  public enum DelayMode {PAUSE, PERIODIC, NEXT_PERIOD;}

  private final Clock clock;
  private final long duration;
  private DelayMode mode;
  private long prev;

  protected Metronome(Clock clock, long duration, long prev) {
    this.clock = clock;
    this.duration = duration;
    this.mode = DelayMode.NEXT_PERIOD;
    this.prev = prev;
  }

  public static Metronome fromNow(long duration, TimeUnit unit) {
    return fromNow(Clock.NANO_TIME, duration, unit);
  }

  public static Metronome fromNow(Clock clock, long duration, TimeUnit unit) {
    return new Metronome(clock, duration, clock.now());
  }

  public static Metronome aligned(long duration, TimeUnit unit) {
    return aligned(Clock.NANO_TIME, duration, unit);
  }

  public static Metronome aligned(Clock clock, long duration, TimeUnit unit) {
    final long prev = DMath.floor(clock.now(), duration);
    return new Metronome(clock, duration, prev);
  }

  public Metronome mode(DelayMode mode) {this.mode = mode;  return this;}

  public long next() {return next(clock.now());}

  private long next(long now) {
    final long next = switch (mode) {
    case PAUSE -> now + duration;
    case PERIODIC -> prev + duration;
    case NEXT_PERIOD ->
      prev + duration * (1 + Math.max(0, now - prev) / duration);
    };
    prev = next;
    return next;
  }

  public void sleep() throws InterruptedException {
    final long now = clock.now(), next = next(now);
    TimeUnit.NANOSECONDS.sleep(next - now);
  }
}
