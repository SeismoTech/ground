package org.seismotech.ground.math;

/**
 * Discrete Math: a set of functions on integers.
 */
public class DMath {

  //----------------------------------------------------------------------
  // About integer division

  public static int cdiv(int n, int d) {
    return (n + d - 1) / d;
  }

  public static long cdiv(long n, long d) {
    return (n + d - 1) / d;
  }

  public static int ceil(int n, int d) {
    return d * cdiv(n,d);
  }

  public static long ceil(long n, long d) {
    return d * cdiv(n,d);
  }

  public static int multiples(int d, int from, int to) {
    return (to-1)/d - cdiv(from,d) + 1;
  }

  public static long multiples(long d, long from, long to) {
    return (to-1)/d - cdiv(from,d) + 1;
  }

  //----------------------------------------------------------------------
  // Log2 like operations

  public static boolean isPow2(int n) {
    return (n & (n-1)) == 0;
  }

  public static boolean isPow2(long n) {
    return (n & (n-1)) == 0;
  }

  /**
   * Number of bits to write down {@code n}
   */
  public static int bitSize(int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  /**
   * Number of bits to write down {@code n}
   */
  public static int bitSize(long n) {
    return 64 - Long.numberOfLeadingZeros(n);
  }

  /**
   * floor(log2(n)), n >= 0
   */
  public static int flog2(int n) {
    return 31 - Integer.numberOfLeadingZeros(n);
  }

  /**
   * floor(log2(n)), n >= 0
   */
  public static int flog2(long n) {
    return 63 - Long.numberOfLeadingZeros(n);
  }

  /**
   * ceil(log2(n)), n >= 0
   */
  public static int clog2(int n) {
    return (32 - Integer.numberOfLeadingZeros(n-1)) & 31;
  }

  /**
   * ceil(log2(n)), n >= 0
   */
  public static int clog2(long n) {
    return (64 - Long.numberOfLeadingZeros(n-1)) & 63;
  }

  //----------------------------------------------------------------------
  // Sign extension

  /** {@code n} is a {@code w} bits 2's complement signed integer.
   * Extend sign to an int (32 bits signed integer) */
  public static int signExtend(int n, int w) {
    int mask = 1 << (w-1);
    return (n ^ mask) - mask;
  }

  /** {@code n} is a {@code w} bits 2's complement signed integer.
   * Extend sign to a long (64 bits signed integer) */
  public static long signExtend(long n, int w) {
    long mask = 1L << (w-1);
    return (n ^ mask) - mask;
  }
}
