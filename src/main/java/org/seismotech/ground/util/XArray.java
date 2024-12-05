package org.seismotech.ground.util;

public class XArray {
  private XArray() {}

  public static final int MAX_SIZE = Integer.MAX_VALUE-2;

  /**
   * Size for a new array that will have {@code free} slots free.
   * Current array has {@code used} entries used
   * out of a total size of {@code size}.
   * If possible, tries to at least duplicate current array size,
   * to amortize creation and copy costs.
   * Bounds the size with the max array size ({@link MAX_SIZE}).
   * If getting the {@code free} goal is not possible because of max array size
   * and {@code IllegalStateException} will be thrown.
   * It is assumed that {@code size < used+free}.
   */
  public static int growSize(int size, int used, int free) {
    if (MAX_SIZE - used < free) tooBig(used, free);
    return Math.max(used+free, size + Math.min(size, MAX_SIZE-size));
  }

  private static void tooBig(int used, int free) {
    throw new IllegalStateException(
      "Cannot grow an array with " + used + " slots used to ensure "
      + free + " additional free slots: it will exceed the max int value");
  }

  /**
   * Equivalent to {@code growFree(xs, length(xs), used, free)}.
   */
  public static byte[] growFree(byte[] xs, int used, int free) {
    return growFree(xs, length(xs), used, free);
  }

  /**
   * Grows array {@code xs}
   * having length {@code size}
   * and a prefix of {@code used} used entries to
   * ensure {@code free} free slots.
   * The resulting array will start with {@code xs[0..used)}.
   * The array {@code xs} might be {@code null},
   * but only if {@code used} is 0.
   */
  public static byte[] growFree(byte[] xs, int size, int used, int free) {
    final int newsize = growSize(size, used, free);
    final byte[] ys = new byte[newsize];
    if (xs != null) System.arraycopy(xs,0, ys,0, used);
    return ys;
  }

  //----------------------------------------------------------------------
  public static int length(byte[] xs) {
    return (xs == null) ? 0 : xs.length;
  }

  //----------------------------------------------------------------------
  public static boolean isPrefix(byte[] prefix, byte[] data) {
    return isPrefix(prefix, 0, prefix.length, data, 0, data.length);
  }

  public static boolean isPrefix(byte[] prefix, int poff, int plen,
      byte[] data, int doff, int dlen) {
    return plen <= dlen && isPrefix(prefix,poff, data,doff, plen);
  }

  public static boolean isPrefix(byte[] prefix, int poff,
      byte[] data, int doff, int len) {
    for (int i = 0; i < len; i++) {
      if (prefix[poff+i] != data[doff+i]) return false;
    }
    return true;
  }
}
