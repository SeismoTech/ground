package com.seismotech.ground.math;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * A benchmark for reciprocal division.
 *
 * <p>Some conclusions:
 * <ul>
 * <li>Our dynamic reciprocal division URecDiv has a performance on par with
 * the compiler optimization.
 * <li>The compiler does trace the value of a divisor to discover that it is
 * effectively invariant and to emit a reciprocal division optimization
 * (guarded with a check to bailout). Therefore, there is a need for URecDiv.
 * <li>Although Hotspot have an optimization for signed division by invariant
 * integer, it doesn't have int for unsigned division.
 * Graal and Zing have both.
 * </ul>
 *
 * <p>Hotspot 21
<tt><pre>
.directConstant            7  thrpt    2  611266975.623          ops/s
.directConstant          100  thrpt    2  610812843.221          ops/s
.directUnsignedConstant    7  thrpt    2  252851071.032          ops/s
.directUnsignedConstant  100  thrpt    2  254444017.469          ops/s
.indirectConstant          7  thrpt    2  254728758.460          ops/s
.indirectConstant        100  thrpt    2  254461175.225          ops/s
.reciprocal                7  thrpt    2  509037575.379          ops/s
.reciprocal              100  thrpt    2  572737719.698          ops/s
</pre></tt>
 *
 * <p>Graal EE 21
<tt><pre>
.directConstant            7  thrpt    2  634901969.500          ops/s
.directConstant          100  thrpt    2  632131746.959          ops/s
.directUnsignedConstant    7  thrpt    2  607584426.506          ops/s
.directUnsignedConstant  100  thrpt    2  600648071.105          ops/s
.indirectConstant          7  thrpt    2  286504171.937          ops/s
.indirectConstant        100  thrpt    2  286512200.368          ops/s
.reciprocal                7  thrpt    2  458129581.858          ops/s
.reciprocal              100  thrpt    2  481437949.247          ops/s
</pre></tt>
 *
 * <p>Zing 21
<tt><pre>
.directConstant            7  thrpt    2  611188370.473          ops/s
.directConstant          100  thrpt    2  607213135.319          ops/s
.directUnsignedConstant    7  thrpt    2  704330685.020          ops/s
.directUnsignedConstant  100  thrpt    2  703654572.693          ops/s
.indirectConstant          7  thrpt    2  221488492.761          ops/s
.indirectConstant        100  thrpt    2  217109395.375          ops/s
.reciprocal                7  thrpt    2  555063187.482          ops/s
.reciprocal              100  thrpt    2  571114470.947          ops/s
</pre></tt>
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class RecDivBenchmark {

  int x = 123456789;

  @Param({"7", "100"})
  int y;
  URecDiv byY;

  @Setup
  public void initReciprocal() {
    byY = new URecDiv(y);
  }

  @Benchmark
  public int directConstant() {
    return x / 7;
  }

  @Benchmark
  public int directUnsignedConstant() {
    return Integer.divideUnsigned(x, 7);
  }

  @Benchmark
  public int indirectConstant() {
    return x / y;
  }

  @Benchmark
  public int reciprocal() {
    return byY.div(x);
  }
}
