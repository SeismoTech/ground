package org.seismotech.ground.mem;

import java.nio.ByteBuffer;

public class Buffer6 {
  private Buffer6() {}

  public static ByteBuffer allocateAs(ByteBuffer bb, int capacity) {
    return bb.isDirect() ? ByteBuffer.allocateDirect(capacity)
      : ByteBuffer.allocate(capacity);
  }

  public static int fill(ByteBuffer bb, int n, byte v) {
    final int toFill = Math.min(n, bb.remaining());
    for (int i = 0; i < toFill; i++) bb.put(v);
    return toFill;
  }

  public static ByteBuffer skip(ByteBuffer bb, int n) {
    return bb.position(bb.position() + n);
  }

  public static void get(ByteBuffer bb, int off, byte[] bs) {
    get(bb, off, bs, 0, bs.length);
  }

  public static void get(ByteBuffer bb, int off, byte[] bs, int init, int len) {
    for (int i = 0; i < len; i++) bs[init+i] = bb.get(off+i);
  }
}
