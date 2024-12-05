package org.seismotech.ground.mem;

import org.seismotech.ground.util.XArray;

/**
 * A minimal interface to emit binary data produced in an incremental way.
 * For instance, when encoding some type.
 */
public interface ByteDrain {

  void drain(byte x);
  void drain(short x);
  void drain(int x);
  void drain(long x);

  //----------------------------------------------------------------------
  public static class OnArray implements ByteDrain {

    private byte[] bs;
    private int used;

    public OnArray(int initialSize) {
      this.bs = new byte[initialSize];
      this.used = 0;
    }

    @Override
    public void drain(byte x) {
      ensure(1);
      bs[used++] = x;
    }

    @Override
    public void drain(short x) {
      ensure(2);
      Bits.le16(bs, used, x);
      used += 2;
    }

    @Override
    public void drain(int x) {
      ensure(4);
      Bits.le32(bs, used, x);
      used += 4;
    }

    @Override
    public void drain(long x) {
      ensure(8);
      Bits.le64(bs, used, x);
      used += 8;
    }

    private void ensure(int n) {
      if (bs.length < used + n) bs = XArray.growFree(bs, used, n);
    }
  }
}
