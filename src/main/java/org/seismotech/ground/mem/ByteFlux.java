package org.seismotech.ground.mem;

/**
 * A minimal interface to fetch binary data in an incremental way.
 */
public interface ByteFlux {

  int remaining();

  byte nextByte();
  short nextShort();
  int nextInt();
  long nextLong();

  //----------------------------------------------------------------------
  public static class OnByteArray implements ByteFlux {

    private final ByteArray data;
    private int next;

    public OnByteArray(ByteArray data) {
      this.data = data;
      this.next = 0;
    }

    @Override public int remaining() {return data.size() - next;}

    @Override public byte nextByte() {return data.get(next++);}

    @Override public short nextShort() {
      final short v = data.get16(next);
      next += 2;
      return v;
    }

    @Override public int nextInt() {
      final int v = data.get32(next);
      next += 4;
      return v;
    }

    @Override public long nextLong() {
      final long v = data.get64(next);
      next += 8;
      return v;
    }
  }
}
