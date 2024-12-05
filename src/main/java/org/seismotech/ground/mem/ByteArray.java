package org.seismotech.ground.mem;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.seismotech.ground.util.Bounds;

/**
 * A low level memory abstraction.
 * An enabler to write code working on byte[] and ByteBuffer.
 * The min amount of memory is a byte, but offers a view of bigger words.
 * Currently, there is only little endian order for multibyte words.
 *
 * <p><b>Unchecked vs unsafe</b>
 * This interface can have
 *   *checked*, *unchecked*, *fast* and *unsafe* implementations.
 * <p>If <i>correctly</i> used, all implementations should be equivalent.
 * But out-of-bounds access will have different behaviour.
 * *Unsafe* will never throw, and JVM memory could became corrupted.
 * *Unchecked* and *fast* will sometimes throw
 * (if the access goes beyond the underlying storage),
 * and others times will access/modify data out of the logical array.
 * *Checked* will throw on any out-of-bound access.
 * <p>*Unchecked* and *fast* differ on the multibyte access behaviour on the
 * logical extremes.
 * *Fast* will check at construction (and assume afterward) that
 * the underlying storage is long enough to perform the multibyte operation.
 * On the contrary, *unchecked* will work with the available space.
 * <p>Nevertheless, currently the only implementation is *unchecked*,
 * therefore all those differences are only theoretical differences.
 */
public interface ByteArray {

  int size();

  ByteArray subarray(int init, int end);

  default ByteArray subarrayClamped(int init, int end) {
    final int effinit = Bounds.clamp(0, init, size());
    final int effend = Bounds.clamp(effinit, end, size());
    return subarray(effinit, effend);
  }

  default BitArray bitArray() {return BitArray.on(this);}

  default BitArray bitArray(int init, int end) {
    return BitArray.on(this, init, end);
  }

  default BitArray bitArrayClamped(int init, int end) {
    final int effinit = Bounds.clamp(0, init, 8*size());
    final int effend = Bounds.clamp(effinit, end, 8*size());
    return bitArray(effinit, effend);
  }

  byte get(int i);
  short getShort(int i);
  int getInt(int i);
  long getLong(int i);

  void set(int i, byte v);
  void setShort(int i, short v);
  void setInt(int i, int v);
  void setLong(int i, long v);

  short getPaddedShort(int i);
  int getPaddedInt(int i);
  long getPaddedLong(int i);

  void setClampedShort(int i, short v);
  void setClampedInt(int i, int v);
  void setClampedLong(int i, long v);

  default int get8u(int i) {return Bits.ubyte(get(i));}
  default short get16(int i) {return getShort(i);}
  default int get32(int i) {return getInt(i);}
  default long get64(int i) {return getLong(i);}
  default short pget16(int i) {return getPaddedShort(i);}
  default int pget32(int i) {return getPaddedInt(i);}
  default long pget64(int i) {return getPaddedLong(i);}

  default void set16(int i, short v) {setShort(i, v);}
  default void set32(int i, int v) {setInt(i, v);}
  default void set64(int i, long v) {setLong(i, v);}
  default void cset16(int i, short v) {setClampedShort(i, v);}
  default void cset32(int i, int v) {setClampedInt(i, v);}
  default void cset64(int i, long v) {setClampedLong(i, v);}

  default void clear() {clear(0, size());}

  void clear(int init, int end);

  void clear(int init, int sinit, int end, int send);

  default int get(int i, byte[] trg) {return get(i, trg, 0, trg.length);}

  int get(int i, byte[] trg, int off, int len);

  default int set(int i, byte[] src) {return set(i, src, 0, src.length);}

  int set(int i, byte[] srt, int off, int len);

  //----------------------------------------------------------------------
  static ByteArray unchecked(byte[] store) {
    return unchecked(store, 0, store.length);
  }

  static ByteArray unchecked(byte[] store, int init, int end) {
    return new UncheckedOnBytes(store, init, end);
  }

  static ByteArray unchecked(ByteBuffer store) {
    return unchecked(store, store.position(), store.limit());
  }

  static ByteArray unchecked(ByteBuffer store, int init, int end) {
    return new UncheckedOnByteBuffer(store, init, end);
  }

  //----------------------------------------------------------------------
  /**
   * An unchecked implementation of ByteArray backed by a byte[].
   */
  public static class UncheckedOnBytes implements ByteArray {
    private final byte[] st;
    private final int off;
    private final int len;

    public UncheckedOnBytes(byte[] st, int init, int end) {
      this.st = st;
      this.off = init;
      this.len = Math.max(0, end-init);
    }

    @Override
    public int size() {return len;}

    @Override
    public ByteArray subarray(int init, int end) {
      return new UncheckedOnBytes(st, off+init, off+end);
    }

    @Override public byte get(int i) {return st[off+i];}
    @Override public short getShort(int i) {return Bits.le16(st, off+i);}
    @Override public int getInt(int i) {return Bits.le32(st, off+i);}
    @Override public long getLong(int i) {return Bits.le64(st, off+i);}

    @Override public void set(int i, byte v) {st[off+i] = v;}
    @Override public void setShort(int i, short v) {Bits.le16(st, off+i, v);}
    @Override public void setInt(int i, int v) {Bits.le32(st, off+i, v);}
    @Override public void setLong(int i, long v) {Bits.le64(st, off+i, v);}

    @Override
    public short getPaddedShort(int i) {
      return (i <= len-2) ? getShort(i) : (short) Bits.ubyte(st[off+i]);
    }

    @Override
    public int getPaddedInt(int i) {
      final int j = off+i, tail = len-i;
      if (j <= st.length-4) {
        final int v = Bits.le32(st, j);
        return (tail >= 4) ? v : v & ((1 << 8*tail) - 1);
      } else {
        return Bits.le32tail(st, j, tail);
      }
    }

    @Override
    public long getPaddedLong(int i) {
      final int j = off+i, tail = len-i;
      if (j <= st.length-8) {
        final long v = Bits.le64(st, j);
        return (tail >= 8) ? v : v & ((1L << 8*tail) - 1);
      } else {
        return Bits.le64tail(st, j, tail);
      }
    }

    @Override
    public void setClampedShort(int i, short v) {
      if (i <= len-2) setShort(i,v);
      else set(i, (byte) v);
    }

    @Override
    public void setClampedInt(int i, int v) {
      final int tail = len-i;
      if (tail >= 4) setInt(i, v);
      else Bits.le32tail(st, off+i, tail, v);
    }

    @Override
    public void setClampedLong(int i, long v) {
      final int tail = len-i;
      if (tail >= 8) setLong(i, v);
      else Bits.le64tail(st, off+i, tail, v);
    }

    @Override
    public void clear(int init, int end) {
      Arrays.fill(st, off+init, off+end, (byte) 0);
    }

    @Override
    public void clear(int init, int sinit, int end, int send) {
      if (init < end) {
        int j = init;
        if (0 < sinit) {st[off+j] &= ~(0xFF << sinit); j++;}
        Arrays.fill(st, off+j, off+end, (byte) 0);
      }
      if (0 < send) {
        int mask = 0xFF << send;
        if (init == end && 0 < sinit) mask |= ~(0xFF << sinit);
        st[off+end] &= mask;
      }
    }

    @Override
    public int get(int i, byte[] trg, int off, int len) {
      final int tocopy = Math.min(this.len-i, len);
      if (tocopy <= 0) return 0;
      System.arraycopy(st, this.off+i, trg, off, tocopy);
      return tocopy;
    }

    @Override
    public int set(int i, byte[] src, int off, int len) {
      final int tocopy = Math.min(this.len-i, len);
      if (tocopy <= 0) return 0;
      System.arraycopy(src, off, st, this.off+i, tocopy);
      return tocopy;
    }
  }

  //----------------------------------------------------------------------
  /**
   * An unchecked implementation of ByteArray backed by a ByteBuffer.
   */
  public static class UncheckedOnByteBuffer implements ByteArray {
    private final ByteBuffer st;
    private final int off;
    private final int len;

    public UncheckedOnByteBuffer(ByteBuffer st, int init, int end) {
      Bits.checkLittleEndian(st);
      this.st = st;
      this.off = init;
      this.len = Math.max(0, end-init);
    }

    @Override
    public int size() {return len;}

    @Override
    public ByteArray subarray(int init, int end) {
      return new UncheckedOnByteBuffer(st, off+init, off+end);
    }

    @Override public byte get(int i) {return st.get(off+i);}
    @Override public short getShort(int i) {return st.getShort(off+i);}
    @Override public int getInt(int i) {return st.getInt(off+i);}
    @Override public long getLong(int i) {return st.getLong(off+i);}

    @Override public void set(int i, byte v) {st.put(off+i, v);}
    @Override public void setShort(int i, short v) {st.putShort(off+i, v);}
    @Override public void setInt(int i, int v) {st.putInt(off+i, v);}
    @Override public void setLong(int i, long v) {st.putLong(off+i, v);}

    @Override public short getPaddedShort(int i) {
      final int j = off+i;
      return (len-i >= 2) ? st.getShort(j) : (short) Bits.ubyte(st.get(j));
    }

    @Override public int getPaddedInt(int i) {
      final int j = off+i, tail = len-i;
      return (tail >= 4) ? st.getInt(j) : Bits.le32tail(st, j, tail);
    }

    @Override public long getPaddedLong(int i) {
      final int j = off+i, tail = len-i;
      return (tail >= 8) ? st.getLong(j) : Bits.le64tail(st, j, tail);
    }

    @Override public void setClampedShort(int i, short v) {
      final int j = off+i;
      if (i <= len-2) st.putShort(j,v);
      else st.put(j, (byte) v);
    }

    @Override public void setClampedInt(int i, int v) {
      final int j = off+i, tail = len-i;
      if (tail >= 4) st.putInt(j, v);
      else Bits.le32tail(st, j, tail, v);
    }

    @Override public void setClampedLong(int i, long v) {
      final int j = off+i, tail = len-i;
      if (tail >= 8) st.putLong(j, v);
      else Bits.le64tail(st, j, tail, v);
    }

    @Override
    public void clear(int init, int end) {
      final int limit = off + end;
      int j = off+init;
      while (j+8 < limit) {st.putLong(j, 0);  j+=8;}
      if (j+4 < limit) {st.putInt(j, 0);  j+=4;}
      while (j < limit) {st.put(j, (byte)0);  j++;}
    }

    @Override
    public void clear(int init, int sinit, int end, int send) {
      if (init < end) {
        int j = init;
        if (0 < sinit) {
          st.put(off+j, (byte)(st.get(off+j) & ~(0xFF << sinit)));
          j++;
        }
        clear(j, end);
      }
      if (0 < send) {
        int mask = 0xFF << send;
        if (init == end && 0 < sinit) mask |= ~(0xFF << sinit);
        st.put(off+end, (byte) (st.get(off+end) & mask));
      }
    }

    @Override
    public int get(int i, byte[] trg, int off, int len) {
      throw new UnsupportedOperationException("FIXME: TODO");
    }

    @Override
    public int set(int i, byte[] src, int off, int len) {
      throw new UnsupportedOperationException("FIXME: TODO");
    }
  }
}
