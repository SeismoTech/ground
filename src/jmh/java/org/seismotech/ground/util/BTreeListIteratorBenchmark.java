package org.seismotech.ground.util;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * OpenJDK21
.arrayList    10000  thrpt    2  35762.486          ops/s
.bTreeList    10000  thrpt    2  17006.642          ops/s
.linkedList   10000  thrpt    2  18743.030          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BTreeListIteratorBenchmark {

  @Param({"10000"})
  int size;

  ArrayList<Integer> array;
  LinkedList<Integer> linked;
  BTreeList<Integer> btree;

  @Setup(Level.Trial)
  public void prepareData() {
    array = new ArrayList<>(size);
    linked = new LinkedList<>();
    btree = new BTreeList<>(64);
    for (int i = 0; i < size; i++) {
      array.add(i);
      linked.add(i);
      btree.add(i);
    }
  }

  @Benchmark
  public int arrayList() {
    int s = 0;
    final Iterator<Integer> it = array.iterator();
    while (it.hasNext()) s += it.next();
    return s;
  }

  @Benchmark
  public int linkedList() {
    int s = 0;
    final Iterator<Integer> it = linked.iterator();
    while (it.hasNext()) s += it.next();
    return s;
  }

  @Benchmark
  public int bTreeList() {
    int s = 0;
    final Iterator<Integer> it = btree.iterator();
    while (it.hasNext()) s += it.next();
    return s;
  }
}
