package org.seismotech.ground.mem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.seismotech.ground.util.XArray;

class BitsTest {

  //----------------------------------------------------------------------
  @Test
  void byteArrayByLE16() {
    byteArrayByWord(
      2, ByteOrder.LITTLE_ENDIAN,
      (xs,i) -> Bits.ushort(Bits.le16(xs,i)),
      (xs,i,v) -> Bits.le16(xs,i,(short)v));
  }

  @Test
  void byteArrayByBE16() {
    byteArrayByWord(
      2, ByteOrder.BIG_ENDIAN,
      (xs,i) -> Bits.ushort(Bits.be16(xs,i)),
      (xs,i,v) -> Bits.be16(xs,i,(short)v));
  }

  @Test
  void byteArrayByLE32() {
    byteArrayByWord(
      4, ByteOrder.LITTLE_ENDIAN,
      (xs,i) -> Bits.uint(Bits.le32(xs,i)),
      (xs,i,v) -> Bits.le32(xs,i,(int)v));
  }

  @Test
  void byteArrayByBE32() {
    byteArrayByWord(
      4, ByteOrder.BIG_ENDIAN,
      (xs,i) -> Bits.uint(Bits.be32(xs,i)),
      (xs,i,v) -> Bits.be32(xs,i,(int)v));
  }

  @Test
  void byteArrayByLE64() {
    byteArrayByWord(
      8, ByteOrder.LITTLE_ENDIAN,
      (xs,i) -> Bits.le64(xs,i),
      (xs,i,v) -> Bits.le64(xs,i,v));
  }

  @Test
  void byteArrayByBE64() {
    byteArrayByWord(
      8, ByteOrder.BIG_ENDIAN,
      (xs,i) -> Bits.be64(xs,i),
      (xs,i,v) -> Bits.be64(xs,i,v));
  }

  @Test
  void byteArrayByLE32Tail() {
    byteArrayByTail(
      4, ByteOrder.LITTLE_ENDIAN,
      (xs,i,w) -> Bits.uint(Bits.le32tail(xs,i,w)),
      (xs,i,w,v) -> Bits.le32tail(xs,i,w,(int)v));
  }

  @Test
  void byteArrayByBE32Tail() {
    byteArrayByTail(
      4, ByteOrder.BIG_ENDIAN,
      (xs,i,w) -> Bits.uint(Bits.be32tail(xs,i,w)),
      (xs,i,w,v) -> Bits.be32tail(xs,i,w,(int)v));
  }

  @Test
  void byteArrayByLE64Tail() {
    byteArrayByTail(
      8, ByteOrder.LITTLE_ENDIAN,
      (xs,i,w) -> Bits.le64tail(xs,i,w),
      (xs,i,w,v) -> Bits.le64tail(xs,i,w,v));
  }

  @Test
  void byteArrayByBE64Tail() {
    byteArrayByTail(
      8, ByteOrder.BIG_ENDIAN,
      (xs,i,w) -> Bits.be64tail(xs,i,w),
      (xs,i,w,v) -> Bits.be64tail(xs,i,w,v));
  }

  @FunctionalInterface
  interface Getter {long apply(byte[] xs, int off);}

  @FunctionalInterface
  interface Setter {void apply(byte[] xs, int off, long v);}

  @FunctionalInterface
  interface TailGetter {long apply(byte[] xs, int off, int width);}

  @FunctionalInterface
  interface TailSetter {void apply(byte[] xs, int off, int width, long v);}

  void byteArrayByWord(int n, ByteOrder order, Getter get, Setter set) {
    final byte[] target = new byte[n];
    final byte[] expected = reverse(order, seq(n));
    final long v = iseq(n);
    set.apply(target, 0, v);
    assertArrayEquals(expected, target);
    assertEquals(v, get.apply(target,0));
  }

  void byteArrayByTail(int n, ByteOrder order, TailGetter get, TailSetter set) {
    for (int i = 0; i < n; i++) {
      final int w = i;
      byteArrayByWord(i, order,
          (xs,off) -> get.apply(xs,off,w),
          (xs,off,v) -> set.apply(xs,off,w,v));
    }
  }

  long iseq(int n) {
    long v = 0;
    for (int i = 1; i <= n; i++) v = (v << 8) | i;
    return v;
  }

  byte[] seq(int n) {
    final byte[] xs = new byte[n];
    for (int i = 0; i < n; i++) xs[i] = (byte) (i+1);
    return xs;
  }

  byte[] reverse(ByteOrder order, byte[] xs) {
    if (order == ByteOrder.LITTLE_ENDIAN) {
      for (int i = 0; i < xs.length/2; i++) XArray.swap(xs, i, xs.length-1-i);
    }
    return xs;
  }

  //----------------------------------------------------------------------
  @Test
  void byteArrayByLEF() {
    byteArrayByFloat(
      ByteOrder.LITTLE_ENDIAN,
      (xs,i) -> Bits.lef(xs,i),
      (xs,i,v) -> Bits.lef(xs,i,v));
  }

  @Test
  void byteArrayByBEF() {
    byteArrayByFloat(
      ByteOrder.BIG_ENDIAN,
      (xs,i) -> Bits.bef(xs,i),
      (xs,i,v) -> Bits.bef(xs,i,v));
  }

  @Test
  void byteArrayByLED() {
    byteArrayByDouble(
      ByteOrder.LITTLE_ENDIAN,
      (xs,i) -> Bits.led(xs,i),
      (xs,i,v) -> Bits.led(xs,i,v));
  }

  @Test
  void byteArrayByBED() {
    byteArrayByDouble(
      ByteOrder.BIG_ENDIAN,
      (xs,i) -> Bits.bed(xs,i),
      (xs,i,v) -> Bits.bed(xs,i,v));
  }

  @FunctionalInterface
  interface FGetter<T> {T apply(byte[] xs, int off);}

  @FunctionalInterface
  interface FSetter<T> {void apply(byte[] xs, int off, T v);}

  void byteArrayByFloat(ByteOrder order,
      FGetter<Float> get, FSetter<Float> set) {
    final float X = 3.14f;
    final byte[] expected = new byte[4];
    ByteBuffer.wrap(expected).order(order).putFloat(X);
    final byte[] target = new byte[4];
    set.apply(target, 0, X);
    assertArrayEquals(expected, target);
    assertEquals(X, get.apply(target, 0));
  }

  void byteArrayByDouble(ByteOrder order,
      FGetter<Double> get, FSetter<Double> set) {
    final double X = 3.14;
    final byte[] expected = new byte[8];
    ByteBuffer.wrap(expected).order(order).putDouble(X);
    final byte[] target = new byte[8];
    set.apply(target, 0, X);
    assertArrayEquals(expected, target);
    assertEquals(X, get.apply(target, 0));
  }
}
