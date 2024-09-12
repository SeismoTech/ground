package org.seismotech.ground.util;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * OpenJDK21
.arrayList     100  thrpt    2  256750.751          ops/s
.arrayList    1000  thrpt    2   10436.737          ops/s
.arrayList    4000  thrpt    2     928.375          ops/s
.bTreeList     100  thrpt    2  217582.259          ops/s
.bTreeList    1000  thrpt    2   19544.326          ops/s
.bTreeList    4000  thrpt    2    4642.843          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BTreeListInsertBenchmark {

  @Param({"100", "1000", "4000"})
  int size;

  @Benchmark
  public ArrayList<Integer> arrayList() {
    final ArrayList<Integer> xs = new ArrayList<>();
    for (int i = 0; i < size; i++) xs.add(0, i);
    return xs;
  }

  @Benchmark
  public BTreeList<Integer> bTreeList() {
    final BTreeList<Integer> xs = new BTreeList<>(64);
    for (int i = 0; i < size; i++) xs.add(0, i);
    return xs;
  }
}
