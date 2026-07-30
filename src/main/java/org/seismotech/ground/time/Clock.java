package org.seismotech.ground.time;

/**
 * A clock abstraction working with nanoseconds stored in a long.
 */
public interface Clock {
  long now();

  static final Clock CURRENT_TIME_MILLIS = new CurrentTimeMillis();
  static final Clock NANO_TIME = new NanoTime();
  static final Clock INSTANT = new Instant();

  static class CurrentTimeMillis implements Clock {
    @Override public long now() {return System.currentTimeMillis() * 1_000_000;}
  }

  static class NanoTime implements Clock {
    @Override public long now() {return System.nanoTime();}
  }

  static class Instant implements Clock {
    @Override public long now() {
      final java.time.Instant now = java.time.Instant.now();
      return now.getEpochSecond() * 1_000_000_000 + now.getNano();
    }
  }

  static class Forward implements Clock {
    long now = 0;

    public Forward set(long now) {
      this.now = Math.max(this.now, now);
      return this;
    }

    @Override public long now() {return now;}
  }
}
