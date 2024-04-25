package com.seismotech.ground.util;

import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Bits {

  //----------------------------------------------------------------------
  // Unsigned conversion

  public static int ubyte(byte n) {return n & 0xFF;}
  public static int ushort(short n) {return n & 0xFFFF;}
  public static long uint(int n) {return n & 0xFFFF_FFFFL;}

  //----------------------------------------------------------------------
  // Combination

  public static int low(long n) {return (int) n;}
  public static int high(long n) {return (int) (n >>> 32);}
  public static long concat(int high, int low) {
    return (((long) high) << 32) | uint(low);
  }

  //----------------------------------------------------------------------
  // Accessing integer collections with bigger word sizes

  private static int illegalTailWidth(int max, int width) {
    throw new IllegalArgumentException(
      "A tail scanner called with tail width " + width
      + ", but a value in the range [0," + max + "] expected");
  }

  //----------------------------------------------------------------------
  // Accessing byte[] with bigger word sizes

  public static final VarHandle LE16_ON_BYTES
    = MethodHandles.byteArrayViewVarHandle(
      short[].class, ByteOrder.LITTLE_ENDIAN);

  public static short le16(byte[] xs, int off) {
    return (short) LE16_ON_BYTES.get(xs, off);
  }

  public static void le16(byte[] xs, int off, short v) {
    LE16_ON_BYTES.set(xs, off, v);
  }

  public static final VarHandle LE32_ON_BYTES
    = MethodHandles.byteArrayViewVarHandle(
      int[].class, ByteOrder.LITTLE_ENDIAN);

  public static int le32(byte[] xs, int off) {
    return (int) LE32_ON_BYTES.get(xs, off);
  }

  public static void le32(byte[] xs, int off, int v) {
    LE32_ON_BYTES.set(xs, off, v);
  }

  public static int le32tail(byte[] xs, int off, int width) {
    int tail = 0;
    switch (width) {
    case 3: tail |= ubyte(xs[off+2]) << 16;
    case 2: tail |= ubyte(xs[off+1]) << 8;
    case 1: tail |= ubyte(xs[off]);
    case 0: return tail;
    }
    return illegalTailWidth(3, width);
  }

  public static void le32tail(byte[] xs, int off, int width, int v) {
    switch (width) {
    case 3: xs[off+2] = (byte) (v >>> 16);
    case 2: xs[off+1] = (byte) (v >>> 8);
    case 1: xs[off] = (byte) v;
    case 0: return;
    }
    illegalTailWidth(3, width);
  }

  public static final VarHandle LE64_ON_BYTES
    = MethodHandles.byteArrayViewVarHandle(
      long[].class, ByteOrder.LITTLE_ENDIAN);

  public static long le64(byte[] xs, int off) {
    return (long) LE64_ON_BYTES.get(xs, off);
  }

  public static void le64(byte[] xs, int off, long v) {
    LE64_ON_BYTES.set(xs, off, v);
  }

  public static long le64tail(byte[] xs, int off, int width) {
    return (width < 4) ? le32tail(xs, off, width)
      : uint(le32(xs, off)) | (uint(le32tail(xs, off+4, width-4)) << 32);
  }

  public static void le64tail(byte[] xs, int off, int width, long v) {
    if (width >= 4) {
      le32(xs, off, (int) v);
      off += 4;  width -= 4;  v >>>= 32;
    }
    le32tail(xs, off, width, (int) v);
  }

  //----------------------------------------------------------------------
  // Accessing ByteBuffer with bigger word sizes.
  // Methods for tail access, because ByteBuffer already supports full access

  public static void checkLittleEndian(ByteBuffer bb) {
    check(bb, ByteOrder.LITTLE_ENDIAN);
  }

  public static void check(ByteBuffer bb, ByteOrder expected) {
    if (bb.order() != expected) unexpectedByteOrder(bb, expected);
  }

  private static void unexpectedByteOrder(ByteBuffer bb, ByteOrder expected) {
    throw new IllegalArgumentException(
      "Expecting byte order " + expected + " but provided a buffer with order "
      + bb.order());
  }

  public static int le32tail(ByteBuffer xs, int off, int width) {
    int tail = 0;
    switch (width) {
    case 3: tail |= ubyte(xs.get(off+2)) << 16;
    case 2: tail |= ubyte(xs.get(off+1)) << 8;
    case 1: tail |= ubyte(xs.get(off));
    case 0: return tail;
    }
    return illegalTailWidth(3, width);
  }

  public static void le32tail(ByteBuffer xs, int off, int width, int v) {
    switch (width) {
    case 3: xs.put(off+2, (byte) (v >>> 16));
    case 2: xs.put(off+1, (byte) (v >>> 8));
    case 1: xs.put(off, (byte) v);
    case 0: return;
    }
    illegalTailWidth(3, width);
  }

  public static long le64tail(ByteBuffer xs, int off, int width) {
    return (width < 4) ? le32tail(xs, off, width)
      : uint(xs.getInt(off)) | (uint(le32tail(xs, off+4, width-4)) << 32);
  }

  public static void le64tail(ByteBuffer xs, int off, int width, long v) {
    if (width >= 4) {
      xs.putInt(off, (int) v);
      off += 4;  width -= 4;  v >>>= 32;
    }
    le32tail(xs, off, width, (int) v);
  }

  //----------------------------------------------------------------------
  // Accessing char[] with bigger word sizes

  public static int le32(char[] xs, int off) {
    return xs[off] | (xs[off+1] << 16);
  }

  public static int le32tail(char[] xs, int off, int len) {
    int tail = 0;
    switch (len) {
    case 1: tail = xs[off];
    case 0: return tail;
    }
    return illegalTailWidth(1, len);
  }

  public static long le64(char[] xs, int off) {
    return uint(le32(xs,off)) | (uint(le32(xs,off+2)) << 32);
  }

  public static long le64tail(char[] xs, int off, int len) {
    long tail = 0;
    switch (len) {
    case 3: tail = ((long) xs[off+2]) << 32;
    case 2: tail |= ((long) xs[off+1]) << 16;
    case 1: tail |= xs[off];
    case 0: return tail;
    }
    return illegalTailWidth(3, len);
  }

  //----------------------------------------------------------------------
  // Accessing String with bigger word sizes

  public static int le32(String xs, int off) {
    return xs.charAt(off) | (xs.charAt(off+1) << 16);
  }

  public static int le32tail(String xs, int off, int len) {
    int tail = 0;
    switch (len) {
    case 1: tail = xs.charAt(off);
    case 0: return tail;
    }
    return illegalTailWidth(1, len);
  }

  public static long le64(String xs, int off) {
    return uint(le32(xs,off)) | (uint(le32(xs,off+2)) << 32);
  }

  public static long le64tail(String xs, int off, int len) {
    long tail = 0;
    switch (len) {
    case 3: tail = ((long) xs.charAt(off+2)) << 32;
    case 2: tail |= ((long) xs.charAt(off+1)) << 16;
    case 1: tail |= xs.charAt(off);
    case 0: return tail;
    }
    return illegalTailWidth(3, len);
  }
}
