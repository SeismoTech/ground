package org.seismotech.ground.util;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UniqTest {

  @Test
  void randomSelectExpand() {
    final int T = 100, M = 100, R = 10;
    final var rnd = new XRandom();
    for (int t = 0; t < T; t++) {
      final int n = M/2 + rnd.random().nextInt(M);
      final var xs = List.of(rnd.boxedInts(n, n/2));
      final var uxs = new HashSet<>(xs);
      final var uniq = new Uniq<>(xs);
      assertEquals(n, uniq.totalSize());
      assertEquals(uxs.size(), uniq.uniqCount());
      final var us = uniq.select();
      assertEquals(uxs, new HashSet<>(us));

      final var ns1 = inc(us);
      final var ns1x = uniq.expand(ns1);
      assertEquals(inc(xs), ns1x);

      final int r = rnd.random().nextInt(R);
      final var nsr = climb(us, r);
      final var nsrx = uniq.expand(r, nsr);
      assertEquals(climb(xs, r), nsrx);
    }
  }

  private static List<Integer> inc(List<Integer> xs) {
    return xs.stream().map(x -> x+1).toList();
  }

  private static List<Integer> climb(List<Integer> xs, int n) {
    return xs.stream()
      .flatMap(x -> Stream.iterate(x+1, y -> y+1).limit(n))
      .toList();
  }
}
