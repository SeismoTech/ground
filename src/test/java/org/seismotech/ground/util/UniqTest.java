package org.seismotech.ground.util;

import java.util.List;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UniqTest {

  @Test
  void randomSelectExpand() {
    final int T = 100, M = 100;
    final var rnd = new XRandom();
    for (int t = 0; t < T; t++) {
      final int n = M/2 + rnd.random().nextInt(M);
      final var xs = List.of(rnd.boxedInts(n));
      final var uxs = new HashSet<>(xs);
      final var uniq = new Uniq<>(xs);
      assertEquals(n, uniq.totalSize());
      assertEquals(uxs.size(), uniq.uniqCount());
      final var us = uniq.select();
      assertEquals(uxs, new HashSet<>(us));
      final var ns = inc(us);
      final var nsx = uniq.expand(ns);
      assertEquals(inc(xs), nsx);
    }
  }

  private static List<Integer> inc(List<Integer> xs) {
    return xs.stream().map(x -> x+1).toList();
  }
}
