package com.seismotech.ground.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * JDK21 Hotspot
.arrayList           100  thrpt    2   1545634.948          ops/s
.arrayList          1000  thrpt    2    146189.592          ops/s
.arrayList         10000  thrpt    2     14823.490          ops/s
.arrayListBulk       100  thrpt    2  10822826.077          ops/s
.arrayListBulk      1000  thrpt    2   1227450.912          ops/s
.arrayListBulk     10000  thrpt    2    112663.217          ops/s
.arrayListClone      100  thrpt    2  25007321.307          ops/s
.arrayListClone     1000  thrpt    2   2856107.132          ops/s
.arrayListClone    10000  thrpt    2    263498.721          ops/s
.bTreeList           100  thrpt    2    770305.253          ops/s
.bTreeList          1000  thrpt    2     86993.757          ops/s
.bTreeList         10000  thrpt    2      5805.286          ops/s
.bTreeListBulk       100  thrpt    2   1429956.103          ops/s
.bTreeListBulk      1000  thrpt    2    157965.555          ops/s
.bTreeListBulk     10000  thrpt    2     16176.339          ops/s
.bTreeListClone      100  thrpt    2   5936360.334          ops/s
.bTreeListClone     1000  thrpt    2    763392.874          ops/s
.bTreeListClone    10000  thrpt    2     59796.919          ops/s
.linkedList          100  thrpt    2   1623816.751          ops/s
.linkedList         1000  thrpt    2    136469.870          ops/s
.linkedList        10000  thrpt    2     11851.564          ops/s
.linkedListBulk      100  thrpt    2   2197598.689          ops/s
.linkedListBulk     1000  thrpt    2    223013.270          ops/s
.linkedListBulk    10000  thrpt    2     21879.108          ops/s
.linkedListClone     100  thrpt    2   1824433.081          ops/s
.linkedListClone    1000  thrpt    2    169324.598          ops/s
.linkedListClone   10000  thrpt    2     14858.212          ops/s

 *
 * When `add(E e)` was implemented with `insert(lsize(), e)`,
 * the performance was much worse:
.bTreeList          100  thrpt    2   658318.778          ops/s
.bTreeList         1000  thrpt    2    32725.645          ops/s
.bTreeList        10000  thrpt    2     1346.293          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BTreeListAddBenchmark {

  @Param({"100", "1000", "10000"})
  int size;

  ArrayList<Integer> src;
  LinkedList<Integer> linkedSrc;
  BTreeList<Integer> btreeSrc;

  @Setup
  public void prepareBulk() {
    src = new ArrayList<>();
    linkedSrc = new LinkedList<>();
    btreeSrc = new BTreeList<>(64);
    for (int i = 0; i < size; i++) {
      src.add(i);
      linkedSrc.add(i);
      btreeSrc.add(i);
    }
  }

  @Benchmark
  public ArrayList<Integer> arrayList() {
    final ArrayList<Integer> xs = new ArrayList<>();
    for (int i = 0; i < size; i++) xs.add(i);
    return xs;
  }

  @Benchmark
  public LinkedList<Integer> linkedList() {
    final LinkedList<Integer> xs = new LinkedList<>();
    for (int i = 0; i < size; i++) xs.add(i);
    return xs;
  }

  @Benchmark
  public BTreeList<Integer> bTreeList() {
    final BTreeList<Integer> xs = new BTreeList<>(64);
    for (int i = 0; i < size; i++) xs.add(i);
    return xs;
  }

  @Benchmark
  public ArrayList<Integer> arrayListBulk() {
    final ArrayList<Integer> xs = new ArrayList<>();
    xs.addAll(src);
    return xs;
  }

  @Benchmark
  public LinkedList<Integer> linkedListBulk() {
    final LinkedList<Integer> xs = new LinkedList<>();
    xs.addAll(src);
    return xs;
  }

  @Benchmark
  public BTreeList<Integer> bTreeListBulk() {
    final BTreeList<Integer> xs = new BTreeList<>(64);
    xs.addAll(src);
    return xs;
  }

  @Benchmark
  public Object arrayListClone() {
    return src.clone();
  }

  @Benchmark
  public Object linkedListClone() {
    return linkedSrc.clone();
  }

  @Benchmark
  public BTreeList<Integer> bTreeListClone() {
    return btreeSrc.clone();
  }
}
