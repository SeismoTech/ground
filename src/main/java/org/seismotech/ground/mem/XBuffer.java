package com.seismotech.nu3.support;

import java.nio.ByteBuffer;

public class XBuffer {
  private XBuffer() {}

  public static int fill(ByteBuffer bb, int n, byte v) {
    final int toFill = Math.min(n, bb.remaining());
    for (int i = 0; i < toFill; i++) bb.put(v);
    return toFill;
  }

  public static ByteBuffer skip(ByteBuffer bb, int n) {
    return bb.position(bb.position() + n);
  }
}
