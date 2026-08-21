package org.seismotech.ground.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * An utility class to avoid processing duplicated entries in a collection.
 * The 2 main methods are {@link #select} and {@link #expand}.
 * {@link #select} returns uniq elements in the collection,
 * preserving relative order and returning the first occurrence.
 * The returned list has size {@link #uniqCount}.
 * {@link #expand} expect a list VS of values of size {@link #uniqCount};
 * it assumes that each value is related to the uniq value returned by
 * {@link #select};
 * it produces a result of size {@link #totalSize}
 * repeting the values of VS in the repeated positions of the {@link #original}
 * data.
 */
public class Uniq<T> {

  private final List<T> ks;
  private final int[] first;
  private final int uniq;

  public Uniq(List<T> ks) {
    this.ks = ks;
    this.first = new int[ks.size()];
    final Map<T,Integer> k2i = new HashMap<>();
    int u = 0, j = 0;
    for (final T k: ks) {
      final Integer i = k2i.get(k);
      if (i != null) first[j] = i;
      else {u++;  first[j] = j;  k2i.put(k, j);}
      j++;
    }
    this.uniq = u;
  }

  public boolean hasRepetitions() {return uniqCount() < totalSize();}

  public int totalSize() {return first.length;}

  public int uniqCount() {return uniq;}

  public List<T> original() {return ks;}

  public List<T> select() {
    if (!hasRepetitions()) return ks;
    final List<T> uks = new ArrayList<>(uniq);
    int j = 0;
    for (final T k: ks) {
      if (first[j] == j) uks.add(k);
      j++;
    }
    return uks;
  }

  public List<T> expand(List<T> vs) {
    if (vs.size() != uniq) uniqMismatch("expand", vs);
    if (!hasRepetitions()) return vs;
    final List<T> xvs = new ArrayList<>(first.length);
    for (int i = 0; i < first.length; i++) xvs.add(vs.get(first[i]));
    return xvs;
  }

  private void uniqMismatch(String what, List<T> vs) {
    throw new IllegalArgumentException(
      "Cannot " + what + " a list of length " + vs.size()
      + "; its length must be the amount of unique entries " + uniq);
  }
}
