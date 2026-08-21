package org.seismotech.ground.util;

import java.util.Random;

public class XRandom {

  private final Random rnd;

  public XRandom() {this(new Random());}

  public XRandom(Random rnd) {this.rnd = rnd;}

  public Random random() {return rnd;}

  public int[] ints(int n) {
    final int[] xs = new int[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextInt();
    return xs;
  }

  public Integer[] boxedInts(int n) {
    final Integer[] xs = new Integer[n];
    for (int i = 0; i < n; i++) xs[i] = rnd.nextInt();
    return xs;
  }
}
