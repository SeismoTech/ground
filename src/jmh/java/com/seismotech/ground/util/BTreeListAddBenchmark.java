package com.seismotech.ground.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * JDK21 Hotspot
.arrayList          100  thrpt    2  1284322.115          ops/s
.arrayList         1000  thrpt    2   123184.332          ops/s
.arrayList        10000  thrpt    2    12412.408          ops/s
.arrayListBulk      100  thrpt    2  9221121.590          ops/s
.arrayListBulk     1000  thrpt    2  1077141.289          ops/s
.arrayListBulk    10000  thrpt    2    95897.264          ops/s
.bTreeList          100  thrpt    2   910278.485          ops/s
.bTreeList         1000  thrpt    2    48077.738          ops/s
.bTreeList        10000  thrpt    2     4091.983          ops/s
.bTreeListBulk      100  thrpt    2  1263486.697          ops/s
.bTreeListBulk     1000  thrpt    2   131385.667          ops/s
.bTreeListBulk    10000  thrpt    2    13151.630          ops/s
.linkedList         100  thrpt    2  1334187.773          ops/s
.linkedList        1000  thrpt    2   113400.460          ops/s
.linkedList       10000  thrpt    2     9302.391          ops/s
.linkedListBulk     100  thrpt    2  1819036.262          ops/s
.linkedListBulk    1000  thrpt    2   185546.607          ops/s
.linkedListBulk   10000  thrpt    2    18342.747          ops/s
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

  ArrayList<Integer> all;

  @Setup
  public void prepareBulk() {
    all = new ArrayList<>();
    for (int i = 0; i < size; i++) all.add(i);
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
    xs.addAll(all);
    return xs;
  }

  @Benchmark
  public LinkedList<Integer> linkedListBulk() {
    final LinkedList<Integer> xs = new LinkedList<>();
    xs.addAll(all);
    return xs;
  }

  @Benchmark
  public BTreeList<Integer> bTreeListBulk() {
    final BTreeList<Integer> xs = new BTreeList<>(64);
    xs.addAll(all);
    return xs;
  }
}
