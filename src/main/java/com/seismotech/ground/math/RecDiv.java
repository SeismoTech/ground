package com.seismotech.ground.math;

/**
 * Reciprocal division
 * is an invariant division solved with a multiplication followed by
 * division by a power of 2 (right shift).
 *
 * See [Division By Invariant Integers Using Multiplication](
 *   doc/bib/div-inv-int/DivisionByInvariantIntegersUsingMultiplication.pdf),
 * [Improved Division By Invariant Integers](
 *   doc/bib/div-int-int/ImprovedDivisionByInvariantIntegers.pdf)
 * and https://oeis.org/A346495 and https://oeis.org/A346496
 */
public interface RecDiv extends InvDiv {
  long numerator();
  int log2denominator();

  default int div(int x) {return div(x, numerator(), log2denominator());}

  static int div(int x, long num, int log2den) {
    return (int) ((x * num) >>> log2den);
  }
}
