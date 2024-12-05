package org.seismotech.ground.mem;

import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.lang.Integer.lowestOneBit;
import static java.lang.Long.bitCount;
import static java.lang.Long.numberOfTrailingZeros;
import static java.lang.Long.lowestOneBit;

import org.seismotech.ground.util.Bounds;

public interface BitArray {

  /** Size of this BitArray, in bits, of course */
  int size();

  /** Bit at position i */
  int get(int i);

  /** Similar to {@link #get(int)}, but returning a boolean */
  default boolean has(int i) {return get(i) != 0;}

  /** Sets bit a position i to v (lsb of v) */
  void set(int i, int v);

  /** Similar to {@link #set(int,int)} but providing a boolean */
  default void set(int i, boolean v) {set(i, v ? 1 : 0);}

  /** Equivalent to {@code set(i,true)} */
  void set(int i);

  /** Equivalent to {@code set(i,false)} */
  void clear(int i);

  /** Counts 1s in the array */
  default int popcnt() {return popcnt(0, size());}

  /** Counts 1s in the range [init,end) */
  int popcnt(int init, int end);

  /** Equivalent to {@code next1(init, size())} */
  default int next1(int init) {return next1(init, size());}

  /** Position of the next bit to 1 starting at {@code init},
   * or {@code end} if there is no next bit to 1 in that range. */
  int next1(int init, int end);

  /** Equivalent to {@code next1n(init, size(), n)} */
  default int next1n(int init, int n) {return next1n(init, size(), n);}

  /** Position of the {@code n}-th bit to 1 starting at {@code init},
   * or {@code end} if there is no {@code n} bits to 1 in that range.
   * {@code n} starts counting at 0 */
  int next1n(int init, int end, int n);

  /** Gets w-bits integer starting at position i */
  int get(int i, int w);

  /** Sets w-bits integer starting at position i */
  void set(int i, int w, int v);

  /** Clears the array: set all bits to 0 */
  default void clear() {clear(0, size());}

  /** Clears (set to 0) bits from {@code init} (inclusive)
   * to {@code end} (exclusive) */
  void clear(int init, int end);

  BitArray subarray(int init, int end);

  default BitArray subarrayClamped(int init, int end) {
    final int effinit = Bounds.clamp(0, init, size());
    final int effend = Bounds.clamp(effinit, end, size());
    return subarray(init, end);
  }

  //----------------------------------------------------------------------
  public static BitArray on(ByteArray store) {
    return new OnByteArray(store, 0, 8*store.size());
  }

  public static BitArray on(ByteArray store, int bitinit, int bitend) {
    return new OnByteArray(store, bitinit, Math.max(0, bitend - bitinit));
  }

  public static class OnByteArray implements BitArray {
    private final ByteArray st;
    private final int off;
    private final int len;

    public OnByteArray(final ByteArray store, int bitoff, int bitlen) {
      this.st = store;
      this.off = bitoff;
      this.len = bitlen;
    }

    @Override public int size() {return len;}

    @Override public int get(int i) {
      return (st.get(block8(i)) >>> shift8(i)) & 1;
    }

    @Override public void set(int i, int v) {
      final int b = block8(i), s = shift8(i);
      st.set(b, (byte) (st.get(b) & ~(1 << s) | ((v & 1) << s)));
    }

    @Override public void set(int i) {
      final int b = block8(i), s = shift8(i);
      st.set(b, (byte) (st.get(b) | 1 << s));
    }

    @Override public void clear(int i) {
      final int b = block8(i), s = shift8(i);
      st.set(b, (byte) (st.get(b) & ~(1 << s)));
    }

    @Override public int popcnt(int init, int end) {
      if (end <= init) return 0;
      int n = 0;
      final int binit = block8(init), bend = block8(end);
      if (binit < bend) {
        int i = binit;
        { final int sinit = shift8(init);
          if (sinit > 0) n += bitCount(st.get8u(i++) >>> sinit);
        }
        for (; i+8 <= bend; i+=8) n += bitCount(st.get64(i));
        if (i+4 <= bend) {n += bitCount(st.get32(i));  i += 4;}
        for (; i < bend; i++) n += bitCount(st.get8u(i));
      }
      { final int send = shift8(end);
        if (send > 0) {
          int b = st.get8u(bend) & ~(-1 << send);
          if (binit == bend) b >>>= shift8(init);
          n += bitCount(b);
        }
      }
      return n;
    }

    @Override public int next1(int init, int end) {
      if (end <= init) return end;
      final int binit = block8(init), bend = block8(end);
      int p = init;
      if (binit < bend) {
        int i = binit;
        { final int sinit = shift8(init);
          if (sinit > 0) {
            final int b = st.get8u(i) >>> sinit;
            if (b != 0) return p + numberOfTrailingZeros(b);
            p += 8 - sinit;
            i++;
          }
        }
        for (; i+8 <= bend; i+=8) {
          final long b = st.get64(i);
          if (b != 0) return p + numberOfTrailingZeros(b);
          p += 64;
        }
        for (; i+4 <= bend; i+=4) {
          final int b = st.get32(i);
          if (b != 0) return p + numberOfTrailingZeros(b);
          p += 32;
        }
        for (; i < bend; i++) {
          final int b = st.get8u(i);
          if (b != 0) return p + numberOfTrailingZeros(b);
          p += 8;
        }
      }
      { final int send = shift8(end);
        if (send > 0) {
          int b = st.get8u(bend) & ~(-1 << send);
          if (binit == bend) b >>>= shift8(init);
          if (b != 0) return p + numberOfTrailingZeros(b);
        }
      }
      return end;
    }

    @Override public int next1n(int init, int end, int n) {
      if (n <= 0) return init;
      if (end <= init) return end;
      final int binit = block8(init), bend = block8(end);
      int p = init, r = n;
      if (binit < bend) {
        int i = binit;
        { final int sinit = shift8(init);
          if (sinit > 0) {
            final int b = st.get8u(i) >>> sinit;
            final int c = bitCount(b);
            if (r <= c) return next1n32(p, b, r);
            p += 8 - sinit;  r -= c;  i++;
          }
        }
        for (; i+8 <= bend; i+=8) {
          final long b = st.get64(i);
          final int c = bitCount(b);
          if (r <= c) return next1n64(p, b, r);
          p += 64;  r -= c;
        }
        for (; i+4 <= bend; i+=4) {
          final int b = st.get32(i);
          final int c = bitCount(b);
          if (r <= c) return next1n32(p, b, r);
          p += 32;  r -= c;
        }
        for (; i < bend; i++) {
          final int b = st.get8u(i);
          final int c = bitCount(b);
          if (r <= c) return next1n32(p, b, r);
          p += 8;  r -= c;
        }
      }
      { final int send = shift8(end);
        if (send > 0) {
          int b = st.get8u(bend) & ~(-1 << send);
          if (binit == bend) b >>>= shift8(init);
          final int c = bitCount(b);
          if (r <= c) return next1n32(p, b, r);
        }
      }
      return end;
    }

    private static int next1n32(int p, int b, int n) {
      int mask;
      for (;;) {
        mask = lowestOneBit(b);
        n--;
        if (n == 0) break;
        b ^= mask;
      }
      return p + numberOfTrailingZeros(mask);
    }

    private static int next1n64(int p, long b, int n) {
      long mask;
      for (;;) {
        mask = lowestOneBit(b);
        n--;
        if (n == 0) break;
        b ^= mask;
      }
      return p + numberOfTrailingZeros(mask);
    }

    @Override public int get(int i, int w) {
      throw new UnsupportedOperationException("FIXME: TODO");
    }

    @Override public void set(int i, int w, int v) {
      throw new UnsupportedOperationException("FIXME: TODO");
    }

    @Override public void clear(int init, int end) {
      st.clear(block8(init), shift8(init), block8(end), shift8(end));
    }

    @Override public BitArray subarray(int init, int end) {
      return new OnByteArray(st, off+init, off+end);
    }

    private int block8(int i) {return (off + i) >>> 3;}
    private int shift8(int i) {return (off + i) & 7;}
  }
}
