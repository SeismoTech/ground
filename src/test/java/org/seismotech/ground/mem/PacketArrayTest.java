package org.seismotech.ground.mem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.seismotech.ground.math.DMath;

class PacketArrayTest {

  static final Random rnd = new Random();

  static interface Model {
    boolean isInstance(PacketArray array);
    PacketArray newArray(int width, int off, int size);
  }

  static Stream models() {
    return Stream.of(
      new Model() {
        @Override public boolean isInstance(final PacketArray array) {
          return array.getClass().getName().contains("Unchecked");
        }
        @Override public PacketArray newArray(int width, int off, int entries) {
          final int byteSize = DMath.cdiv(off + entries*width, 8);
          return PacketArray.unchecked(
            ByteArray.unchecked(new byte[byteSize]),
            width, off, entries);
        }
      },
      new Model() {
        @Override public boolean isInstance(final PacketArray array) {
          return array.getClass().getName().contains("Fast");
        }
        @Override public PacketArray newArray(int width, int off, int entries) {
          final int byteSize = ((off + (entries-1)*width) / 8)
            + (width <= PacketArray.MAX_WIDTH_32 ? 4 : 8);
          return PacketArray.fast(
            ByteArray.unchecked(new byte[byteSize]),
            width, off, entries);
        }
      },

      new Model() {
        @Override public boolean isInstance(final PacketArray array) {
          return array.getClass().getName().contains("Unchecked");
        }
        @Override public PacketArray newArray(int width, int off, int entries) {
          final int byteSize = DMath.cdiv(off + entries*width, 8);
          return PacketArray.unchecked(
            ByteArray.unchecked(
              ByteBuffer.allocateDirect(byteSize)
              .order(ByteOrder.LITTLE_ENDIAN)),
            width, off, entries);
        }
      },
      new Model() {
        @Override public boolean isInstance(final PacketArray array) {
          return array.getClass().getName().contains("Fast");
        }
        @Override public PacketArray newArray(int width, int off, int entries) {
          final int byteSize = ((off + (entries-1)*width) / 8)
            + (width <= PacketArray.MAX_WIDTH_32 ? 4 : 8);
          return PacketArray.fast(
            ByteArray.unchecked(
              ByteBuffer.allocateDirect(byteSize)
              .order(ByteOrder.LITTLE_ENDIAN)),
            width, off, entries);
        }
      }
    );
  }

  @ParameterizedTest
  @MethodSource("models")
  void scanWidthsWithRandomValues(Model model) {
    final int TIMES = 1000, AVGSIZE = 100;
    for (int width = 1; width <= 7*8+1; width++) {
      final long mask = ~(-1L << width);
      for (int off = 0; off <= 8; off++) {
        for (int t = 0; t < TIMES; t++) {
          final int size = 1 + rnd.nextInt(2*AVGSIZE);
          final PacketArray store = model.newArray(width, off, size);
          // System.err.println("Parameters: width=" + width + ", offset=" + off
          //     + ", size=" + size + ": " + store);
          assertTrue(model.isInstance(store));
          final long[] ref = new long[size];
          for (int i = 0; i < size; i++) {
            final long v = rnd.nextLong();
            ref[i] = v;
            store.set(i, v);
          }
          for (int i = 0; i < size; i++) {
            assertEquals(ref[i] & mask, store.get(i));
          }
          store.clear();
          for (int i = 0; i < size; i++) {
            assertEquals(0, store.get(i));
          }
          for (int i = 0; i < size; i++) {
            store.orblend(i, ref[i]);
          }
          for (int i = 0; i < size; i++) {
            assertEquals(ref[i] & mask, store.get(i));
          }
          for (int i = 0; i < size; i++) {
            int j = rnd.nextInt(size);
            store.set(j, ref[j]);
          }
          for (int i = 0; i < size; i++) {
            assertEquals(ref[i] & mask, store.get(i));
          }
        }
      }
    }
  }
}
