package org.seismotech.ground.mem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.seismotech.ground.math.DMath;

class ByteArrayTest {

  @Test
  void byteBufferAbsoluteOpsIndexFromTheBeginning() {
    final ByteBuffer bb = ByteBuffer.allocate(10);
    bb.position(2).limit(8);
    bb.put((byte) 1);
    assertEquals(0, bb.get(0));
    assertEquals(1, bb.get(2));
    bb.put(2, (byte) 2);
    assertEquals(2, bb.get(2));
    bb.put(3, (byte) 3);
    assertEquals(3, bb.get());
  }

  @Test
  void byteBufferAbsoluteOpsCannotAccessBeyondLimit() {
    final ByteBuffer bb = ByteBuffer.allocate(10);
    bb.position(2).limit(8);
    assertThrows(IndexOutOfBoundsException.class, () -> bb.put(8, (byte) 80));
    assertThrows(IndexOutOfBoundsException.class, () -> bb.get(8));
  }

  //----------------------------------------------------------------------
  static Stream byteArrays() {
    return Stream.of(
      (IntFunction<ByteArray>) n -> ByteArray.unchecked(new byte[n]),
      (IntFunction<ByteArray>) n -> ByteArray.unchecked(
        ByteBuffer.allocateDirect(n).order(ByteOrder.LITTLE_ENDIAN))
    );
  }

  static final Word[] WORDS = {
    Word.Width16.THE, Word.Width32.THE, Word.Width64.THE,
  };

  @ParameterizedTest
  @MethodSource("byteArrays")
  void correctAccess(IntFunction<ByteArray> arrayBuilder) {
    final int margin = 17;
    final int size = 2*margin + 8*256;
    final ByteArray main = arrayBuilder.apply(size);
    for (int init = 0; init <= margin; init++) {
      for (int end = 0; end <= margin; end++) {
        final ByteArray arr = main.subarray(init, size-end);
        assertEquals(size - init - end, arr.size());
        for (int off = 0; off <= margin; off++) {
          for (final Word word: WORDS) {
            correctAccess(main, arr, init, end, off, word);
          }
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("byteArrays")
  void correctAccessSmall(IntFunction<ByteArray> arrayBuilder) {
    for (int size = 1; size < 8; size++) {
      final byte[] bs = new byte[size];
      final ByteArray arr = ByteArray.unchecked(bs);
      for (final Word word: WORDS) {
        correctAccess(arr, arr, 0, 0, 0, word);
      }
    }
  }

  private void correctAccess(ByteArray main,
      ByteArray arr, int init, int end, int off, Word word) {
    //System.err.println(init + "-" + end + ":" + off + "[" + word + "]");
    final Random rnd = new Random();
    final byte sentinel = (byte) rnd.nextInt();
    if (end > 0) main.set(main.size()-end, sentinel);
    final int blen = arr.size()-off;
    final int width = word.width()/8;
    final int wlen = DMath.cdiv(blen, width);
    final int wtail = blen % width;
    final long[] ref = new long[wlen];
    for (int i = 0; i < wlen; i++) {
      ref[i] = word.random(rnd);
      word.set(arr, off+i*width, ref[i]);
    }
    if (end > 0) assertEquals(sentinel, main.get(main.size()-end));
    for (int i = 0; i < wlen - (wtail == 0 ? 0 : 1); i++) {
      assertEquals(ref[i], word.get(arr, off+i*width));
    }
    if (wtail > 0) {
      assertEquals(
        ref[wlen-1] & ((1L << 8*wtail) - 1),
        word.get(arr, off+(wlen-1)*width));
    }
  }
}
