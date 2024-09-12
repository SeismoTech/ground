package org.seismotech.ground.math;

import org.seismotech.ground.util.Bits;

/**
 * Reciprocal division for unsigned integers.
 * Can handle the full int range as divident and divisor,
 * except 0 for the divisor.
 * They are interpreted as unsigned numbers.
 * The result {@code new URecDiv(n).div(m)}
 * is equivalent to {@code Integer.divideUnsigned(m,n)}.
 *
 * <p>Algorithm copied from
 * https://oeis.org/A346495 and https://oeis.org/A346496
 * With minor changes.
 * The main change is to store the numerator as an int32
 * and put its bit 33 in a boolean flag.
 * The code to make the division in the case of 33 bits its our own solution,
 * because we didn't understand the proposal in
 * https://oeis.org/A346495
 */
public class URecDiv implements RecDiv {
  private final int n;
  private final int num;
  private final int log2den;
  private final boolean bit33;

  public URecDiv(int n) {
    this.n = n;
    if (DMath.isPow2(n)) {
      this.num = 1;
      this.log2den = DMath.flog2(n);
      this.bit33 = false;
    } else {
      final long un = Bits.uint(n);
      long nc = (1L << 32) - ((1L << 32) % un) - 1;
      int b = 1;
      while (b < 63 && ule(1L << b, nc * (un - 1 - ((1L << b) - 1) % un))) b++;
      final long mask = (1L << b) - 1;
      final long num33 = udiv(mask + un - mask % un, un);
      this.num = (int) num33;
      this.bit33 = DMath.bitSize(num33) > 32;
      this.log2den = this.bit33 ? b - 32 : b;
    }
  }

  static private boolean ule(long a, long b) {
    return Long.compareUnsigned(a, b) <= 0;
  }
  static private long udiv(long a, long b) {
    return Long.divideUnsigned(a, b);
  }
  static private long urem(long a, long b) {
    return Long.remainderUnsigned(a, b);
  }

  @Override public long numerator() {return num;}
  @Override public int log2denominator() {return log2den;}

  @Override public int div(int x) {
    final long ux = Bits.uint(x);
    final long n = ux * Bits.uint(num);
    if (!bit33) {
      return (int) (n >>> log2den);
    } else {
      return (int) (((n >>> 32) + ux) >>> log2den);
    }
  }

  @Override public String toString() {
    long num33 = bit33 ? 1 : 0;
    num33 = (num33 << 32) | Bits.uint(num);
    return "URecDiv[divisor: " + n
      + ", numerator: " + num33 + " (" + DMath.bitSize(num33) + ")"
      + ", log2(denominator): " + log2den
      + "]";
  }
}
