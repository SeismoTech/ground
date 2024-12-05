package org.seismotech.ground.mem;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.seismotech.ground.math.DMath;

class BitArrayTest {

  static final Random rnd = new Random();

  @Test
  void randomBits() {
    final int TIMES = 1000;
    for (final int sparsity: new int[] {2, 32, 128}) {
      for (int t = 0; t < TIMES; t++) {
        final int size = 1 + 3*t;
        final BitArray bits = ByteArray
          .unchecked(new byte[DMath.cdiv(3 + size, 8)])
          .bitArrayClamped(3, 3+size);
        assertEquals(size, bits.size());

        final int markCount = DMath.cdiv(size, sparsity);
        final int[] marks = fill(bits, markCount);
        Arrays.sort(marks);
        //System.err.println(Arrays.toString(marks));

        checkEnabled(bits, marks, marks.length);
        checkPopcnt(bits, marks, marks.length);
        checkNext1(bits, marks, marks.length);
        checkNext1n(bits, marks, marks.length);

        int markSize = removeEven(bits, marks, marks.length);
        checkEnabled(bits, marks, markSize);
        checkPopcnt(bits, marks, markSize);
        checkNext1(bits, marks, markSize);
        checkNext1n(bits, marks, markSize);

        bits.clear();
        assertEquals(0, bits.popcnt());
      }
    }
  }

  int[] fill(BitArray bits, int n) {
    final int[] marks = new int[n];
    for (int i = 0; i < n; ) {
      final int p = rnd.nextInt(bits.size());
      if (!bits.has(p)) {
        bits.set(p);
        marks[i++] = p;
      }
    }
    return marks;
  }

  int removeEven(BitArray bits, int[] marks, int markSize) {
    int remain = 0;
    for (int i = 0; i < markSize; i+=2) {
      bits.clear(marks[i]);
      if (i+1 < markSize) marks[remain++] = marks[i+1];
    }
    return remain;
  }

  void checkEnabled(BitArray bits, int[] marks, int markSize) {
    for (int i = 0, j = 0; i < bits.size(); i++) {
      if (j < markSize && marks[j] == i) {
        assertTrue(bits.has(i));
        j++;
      } else {
        assertFalse(bits.has(i));
      }
    }
  }

  void checkPopcnt(BitArray bits, int[] marks, int markSize) {
    final int TIMES = Math.min(markSize*markSize, 1000);
    for (int i = 0; i < TIMES; i++) {
      final int j = rnd.nextInt(markSize);
      final int k = j + rnd.nextInt(markSize-j);
      final int n = bits.popcnt(marks[j], marks[k]);
      assertEquals(k-j, n);
    }
  }

  void checkNext1(BitArray bits, int[] marks, int markSize) {
    final int[] MARGINS = {-1,0,1,2,4,8,16,32,64,128};
    final int size = bits.size();
    for (int i = 0, j = 0; i < size; i++) {
      if (j < markSize) {
        assertEquals(marks[j], bits.next1(i, size));
        for (final int k: MARGINS) {
          assertEquals(
            Math.min(marks[j], marks[j]+k),
            bits.next1(i, Math.min(marks[j]+k, size)));
        }
        if (marks[j] == i) j++;
      } else {
        assertEquals(size, bits.next1(i, size));
      }
    }
  }

  void checkNext1n(BitArray bits, int[] marks, int markSize) {
    final int TIMES = Math.min(markSize*markSize, 1_000);
    final int size = bits.size();
    for (int c = 0; c < TIMES; c++) {
      final int n = rnd.nextInt(markSize);
      final int i = rnd.nextInt(markSize - n);
      final int j = i + n + rnd.nextInt(markSize - (i+n));
      final int expected = marks[i+Math.max(0,n-1)];
      final int[] inits, expects;
      if (0 < i && marks[i-1]+1 < marks[i]) {
        inits = new int[] {marks[i], marks[i-1]+1, marks[i]-1};
        expects = (n == 0)
          ? new int[] {expected, marks[i-1]+1, marks[i]-1}
          : new int[] {expected, expected, expected};
      } else {
        inits = new int[] {marks[i]};
        expects = new int[] {expected};
      }
      final int[] ends = 0 < j && marks[j-1]+1 < marks[j]
        ? new int[] {marks[j], marks[j-1]+1, marks[j]-1}
        : new int[] {marks[j]};
      for (int k = 0; k < inits.length; k++) {
        for (int end: ends) {
          assertEquals(expects[k], bits.next1n(inits[k], end, n));
        }
      }
    }
  }
}
