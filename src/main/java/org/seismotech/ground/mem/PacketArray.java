package org.seismotech.ground.mem;

import org.seismotech.ground.util.Bounds;

/**
 * A packet integer array of fixed size (width).
 * Any size from 1 to 64 is allowed.
 */
public interface PacketArray {

  /** Elements width in bits. */
  int entryWidth();

  /** Array length. */
  int size();

  /** Return the element at position {@code i}, counting from 0. */
  long get(int i);

  /** Change element at position {@code i} to {@code v}.
   * If {@code v} cannot be represented with width bits, it will be clampled. */
  void set(int i, long v);

  /** Or-combines the contents at position {@code i} with {@code v}. */
  void orblend(int i, long v);

  /** Set all values to 0. */
  default void clear() {clear(0,size());}

  /** Set all values in the range [{@code init}, {@code end}) to 0. */
  void clear(int init, int end);

  /** Returns a subarray view of [{@code init}, {@code end}).
   * No bound checking is performed. */
  PacketArray subarray(int init, int end);

  /** Clamps {@code init} and {@code end} to the size of the array,
   * and returns a subarray with the resulting bounds. */
  default PacketArray subarrayClamped(int init, int end) {
    final int effinit = Bounds.clamp(0, init, size());
    final int effend = Bounds.clamp(effinit, end, size());
    return subarray(init, end);
  }

  //----------------------------------------------------------------------
  public static final int MAX_WIDTH_32 = 3*8+1;
  public static final int MAX_WIDTH_64 = 7*8+1;

  static PacketArray unchecked(ByteArray store, int width) {
    return unchecked(store, width, 0, (8*store.size()) / width);
  }

  static PacketArray unchecked(ByteArray store, int width,
      int bitoff, int entries) {
    if (width <= MAX_WIDTH_32) {
      return new UncheckedOnByteArray32(store, width, bitoff, entries);
    } else if (width <= MAX_WIDTH_64) {
      return new UncheckedOnByteArray64(store, width, bitoff, entries);
    } else {
      return illegalWidth(width);
    }
  }

  static PacketArray fast(ByteArray store, int width,
      int bitoff, int entries) {
    if (width <= 0 || MAX_WIDTH_64 < width) illegalWidth(width);
    final int minsize = (bitoff + width*(entries-1)) / 8
      + (width <= MAX_WIDTH_32 ? 4 : 8);
    if (store.size() < minsize) throw new IllegalArgumentException(
      "Store of size " + store.size() + " is not big enough to support a fast"
      + " PacketArray of " + entries + " entries of width " + width
      + " from " + bitoff + "; at least " + minsize + " bytes are needed");
    return (width <= MAX_WIDTH_32)
      ? new FastOnByteArray32(store, width, bitoff, entries)
      : new FastOnByteArray64(store, width, bitoff, entries);
  }

  private static PacketArray illegalWidth(int width) {
      throw new IllegalArgumentException(
        "Illegal/unsupported width " + width
        + ": should be in the range [1," + MAX_WIDTH_64 + "]");
  }

  //----------------------------------------------------------------------
  public static abstract class OnByteArray implements PacketArray {
    protected final ByteArray st;
    protected final int width;
    protected final int off;
    protected final int len;

    protected OnByteArray(ByteArray st, int width, int off, int len) {
      this.st = st;
      this.width = width;
      this.off = off;
      this.len = len;
    }

    @Override public int entryWidth() {return width;}

    @Override public int size() {return len;}

    @Override public void clear(int init, int end) {
      st.clear(block8(init), shift8(init), block8(end), shift8(end));
    }

    @Override
    public PacketArray subarray(int init, int end) {
      return instance(st, width, off + width*init, end-init);
    }

    protected abstract PacketArray instance(
      ByteArray st, int width, int off, int len);

    protected int block8(int i) {return (off + i*width) >>> 3;}
    protected int shift8(int i) {return (off + i*width) & 7;}
  }

  public static abstract class OnByteArray32 extends OnByteArray {
    protected final int mask;

    public OnByteArray32(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
      this.mask = ~(-1 << width);
    }
  }

  public static abstract class OnByteArray64 extends OnByteArray {
    protected final long mask;

    public OnByteArray64(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
      this.mask = ~(-1L << width);
    }
  }

  //----------------------------------------------------------------------
  public static class UncheckedOnByteArray32 extends OnByteArray32 {
    public UncheckedOnByteArray32(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
    }

    @Override
    protected PacketArray instance(ByteArray st, int width, int off, int len) {
      return new UncheckedOnByteArray32(st, width, off, len);
    }

    @Override
    public long get(int i) {
      return (st.pget32(block8(i)) >>> shift8(i)) & mask;
    }

    @Override
    public void set(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.cset32(b, st.pget32(b) & ~(mask << s) | ((((int) v) & mask) << s));
    }

    @Override
    public void orblend(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.cset32(b, st.pget32(b) | ((((int) v) & mask) << s));
    }
  }

  public static class UncheckedOnByteArray64 extends OnByteArray64 {
    public UncheckedOnByteArray64(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
    }

    @Override
    protected PacketArray instance(ByteArray st, int width, int off, int len) {
      return new UncheckedOnByteArray64(st, width, off, len);
    }

    @Override
    public long get(int i) {
      return (st.pget64(block8(i)) >>> shift8(i)) & mask;
    }

    @Override
    public void set(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.cset64(b, st.pget64(b) & ~(mask << s) | ((v & mask) << s));
    }

    @Override
    public void orblend(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.cset64(b, st.pget64(b) | ((v & mask) << s));
    }
  }

  //----------------------------------------------------------------------
  public static class FastOnByteArray32 extends OnByteArray32 {
    public FastOnByteArray32(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
    }

    @Override
    protected PacketArray instance(ByteArray st, int width, int off, int len) {
      return new UncheckedOnByteArray32(st, width, off, len);
    }

    @Override
    public long get(int i) {
      return (st.get32(block8(i)) >>> shift8(i)) & mask;
    }

    @Override
    public void set(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.set32(b, st.get32(b) & ~(mask << s) | ((((int) v) & mask) << s));
    }

    @Override
    public void orblend(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.set32(b, st.get32(b) | ((((int) v) & mask) << s));
    }
  }

  public static class FastOnByteArray64 extends OnByteArray64 {
    public FastOnByteArray64(ByteArray st, int width, int off, int len) {
      super(st, width, off, len);
    }

    @Override
    protected PacketArray instance(ByteArray st, int width, int off, int len) {
      return new UncheckedOnByteArray64(st, width, off, len);
    }

    @Override
    public long get(int i) {
      return (st.get64(block8(i)) >>> shift8(i)) & mask;
    }

    @Override
    public void set(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.set64(b, st.get64(b) & ~(mask << s) | ((v & mask) << s));
    }

    @Override
    public void orblend(int i, long v) {
      final int b = block8(i), s = shift8(i);
      st.set64(b, st.get64(b) | ((v & mask) << s));
    }
  }
}
