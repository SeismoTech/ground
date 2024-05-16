package com.seismotech.ground.math;

import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecDivTest {

  //@Test
  void showSomeUnsignedReciprocal() {
    final RecDiv by7 = new URecDiv(7);
    System.err.println(by7);
    final RecDiv by_10 = new URecDiv(10);
    System.err.println(by_10);
    final RecDiv by_10_2 = new URecDiv(100);
    System.err.println(by_10_2);
    final RecDiv by_10_4 = new URecDiv(10000);
    System.err.println(by_10_4);
    final RecDiv by_10_8 = new URecDiv(1_0000_0000);
    System.err.println(by_10_8);
  }

  @Test
  void unsignedReciprocalTest() {
    final Random rnd = new Random();
    final int[] CONTEST = {
      1, 2, Integer.MAX_VALUE-1, Integer.MAX_VALUE,
      -1, -2, Integer.MIN_VALUE+1, Integer.MIN_VALUE+2,
    };
    for (final int n: CONTEST) {
      checkUnsignedReciprocal(rnd, 1_000, n);
    }
    for (int p = 0, n = 1; p < 32; p++, n = (n << 1) | 1) {
      if (n-1 > 0) checkUnsignedReciprocal(rnd, 1_000, n-1);
      checkUnsignedReciprocal(rnd, 1_000, n);
      if (n+1 > 0) checkUnsignedReciprocal(rnd, 1_000, n+1);
    }
    for (int i = 0; i < 100_000; i++) {
      final int n = rnd.nextInt();
      checkUnsignedReciprocal(rnd, 1_000, n);
    }
  }

  void checkUnsignedReciprocal(Random rnd, int times, int n) {
    final RecDiv byn = new URecDiv(n);
    checkUnsignedReciprocalSpecial(n, byn);
    checkUnsignedReciprocalRandom(rnd, times, n, byn);
  }

  void checkUnsignedReciprocalSpecial(int n, RecDiv byn) {
    final int[] CONTEST = {
      0, 10, 100, Integer.MAX_VALUE,
      -1, -10, -100, Integer.MIN_VALUE};
    for (final int m: CONTEST) {
      assertEquals(Integer.divideUnsigned(m,n), byn.div(m));
    }
  }

  void checkUnsignedReciprocalRandom(Random rnd, int times, int n, RecDiv byn) {
    for (int j = 0; j < times; j++) {
      final int m = rnd.nextInt();
      assertEquals(Integer.divideUnsigned(m,n), byn.div(m));
    }
  }
}
