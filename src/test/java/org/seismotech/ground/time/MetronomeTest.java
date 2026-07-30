package org.seismotech.ground.time;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetronomeTest {

  @Test
  void testPeriodic() {
    final var clock = new Clock.Forward();
    final Metronome metro = Metronome.fromNow(clock, 10, TimeUnit.NANOSECONDS)
      .mode(Metronome.DelayMode.PERIODIC);
    for (int i = 10; i < 100; i += 10) assertEquals(i, metro.next());
    clock.set(200);
    for (int i = 100; i < 200; i += 10) assertEquals(i, metro.next());
  }

  @Test
  void testNextPeriod() {
    final var clock = new Clock.Forward();
    final Metronome metro = Metronome.fromNow(clock, 10, TimeUnit.NANOSECONDS);
    for (int i = 10; i < 100; i += 10) assertEquals(i, metro.next());
    clock.set(199);
    for (int i = 200; i < 300; i += 10) assertEquals(i, metro.next());
  }

  @Test
  void textPause() {
    final var clock = new Clock.Forward();
    final Metronome metro = Metronome.fromNow(clock, 10, TimeUnit.NANOSECONDS)
      .mode(Metronome.DelayMode.PAUSE);
    for (int i = 10; i < 100; i += 10) assertEquals(10, metro.next());
    for (int i = 10; i < 100; i += 10) {
      assertEquals(i, metro.next());
      clock.set(i);
    }
    for (int i = 100; i < 200; i += 13) {
      assertEquals(i, metro.next());
      clock.set(i+3);
    }
  }

  @Test
  void aligned() {
    final var clock = new Clock.Forward().set(9);
    final Metronome metro = Metronome.aligned(clock, 10, TimeUnit.NANOSECONDS);
    for (int i = 10; i < 100; i += 10) assertEquals(i, metro.next());
    clock.set(199);
    for (int i = 200; i < 300; i += 10) assertEquals(i, metro.next());
  }
}
