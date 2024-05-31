package com.seismotech.ground.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/**
 * OpenJDK21
.arrayList      100  thrpt    2  537317.978          ops/s
.arrayList     1000  thrpt    2   21912.271          ops/s
.arrayList     4000  thrpt    2    2074.343          ops/s
.bTreeList      100  thrpt    2  390986.746          ops/s
.bTreeList     1000  thrpt    2   33062.166          ops/s
.bTreeList     4000  thrpt    2    6973.886          ops/s
.linkedList     100  thrpt    2  458504.993          ops/s
.linkedList    1000  thrpt    2    3211.581          ops/s
.linkedList    4000  thrpt    2     192.048          ops/s
 */
@Fork(value = 1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BTreeListRemoveBenchmark {

  @Param({"100", "1000", "4000"})
  int size;

  ArrayList<Integer> all;

  @Setup(Level.Trial)
  public void prepareData() {
    all = new ArrayList<>(size);
    for (int i = 0; i < size; i++) all.add(i);
  }

  ArrayList<Integer> array;
  LinkedList<Integer> linked;
  BTreeList<Integer> btree;

  @Setup(Level.Invocation)
  public void fillCollections() {
    array = new ArrayList<>(size);
    array.addAll(all);
    linked = new LinkedList<>();
    linked.addAll(all);
    btree = new BTreeList<>(64);
    btree.addAll(all);
  }

  @Benchmark
  public ArrayList<Integer> arrayList() {
    for (int i = 0; i < size/2; i++) array.remove(size/4);
    return array;
  }

  @Benchmark
  public LinkedList<Integer> linkedList() {
    for (int i = 0; i < size/2; i++) linked.remove(size/4);
    return linked;
  }

  @Benchmark
  public BTreeList<Integer> bTreeList() {
    for (int i = 0; i < size/2; i++) btree.remove(size/4);
    return btree;
  }
}
