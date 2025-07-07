package org.seismotech.ground.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DMathTest {

  @Test
  void testLog2() {
    assertEquals(-1, DMath.flog2(0));
    assertEquals(-1, DMath.flog2(0L));
    assertEquals(0, DMath.clog2(0));
    assertEquals(0, DMath.clog2(0L));
    final double log2 = Math.log(2);
    for (int i = 1; i < 0x10000; i++) {
      final double log = Math.log(i) / log2;
      assertEquals((int) Math.floor(log), DMath.flog2(i));
      assertEquals((int) Math.ceil(log), DMath.clog2(i));
      assertEquals((int) Math.floor(log), DMath.flog2((long) i));
      assertEquals((int) Math.ceil(log), DMath.clog2((long) i));
    }
  }

  @Test
  void testSignExtend() {
    final int W = 10;
    final int M = (1 << W) - 1;
    for (int n = -(M >> 1) - 1; n < (M >> 1); n++) {
      assertEquals(n, DMath.signExtend(n & M, W));
      assertEquals(n, DMath.signExtend((long)(n & M), W));
    }
  }
}
