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
  private final int[] sel;
  private final int uniq;

  public Uniq(List<T> ks) {
    this.ks = ks;
    this.sel = new int[ks.size()];
    final Map<T,Integer> k2i = new HashMap<>();
    int j = 0;
    for (final T k: ks) {
      final Integer i = k2i.get(k);
      if (i != null) sel[j] = i;
      else {final int s = sel[j] = k2i.size();  k2i.put(k, s);}
      j++;
    }
    this.uniq = k2i.size();
  }

  public static <T> Uniq<T> of(List<T> ks) {return new Uniq<>(ks);}

  public boolean hasRepetitions() {return uniqCount() < totalSize();}

  public int totalSize() {return sel.length;}

  public int uniqCount() {return uniq;}

  public List<T> original() {return ks;}

  public List<T> select() {
    if (!hasRepetitions()) return ks;
    final List<T> uks = new ArrayList<>(uniq);
    int j = 0;
    for (final T k: ks) {
      if (sel[j] == uks.size()) uks.add(k);
      j++;
    }
    return uks;
  }

  public <V> List<V> expand(List<V> vs) {
    if (vs.size() != uniq) uniqMismatch("expand", vs);
    if (!hasRepetitions()) return vs;
    final List<V> xvs = new ArrayList<>(sel.length);
    for (int i = 0; i < sel.length; i++) xvs.add(vs.get(sel[i]));
    return xvs;
  }

  public <V> List<V> expand(int times, List<V> vs) {
    if (vs.size() != uniq * times) uniqMismatch("expand", times, vs);
    if (!hasRepetitions()) return vs;
    final List<V> xvs = new ArrayList<>(sel.length * times);
    for (int i = 0; i < sel.length; i++) {
      for (int j = 0; j < times; j++) {
        xvs.add(vs.get(sel[i] * times + j));
      }
    }
    return xvs;
  }

  private void uniqMismatch(String what, List<?> vs) {
    uniqMismatch(what, 1, vs);
  }

  private void uniqMismatch(String what, int times, List<?> vs) {
    String msg = "Cannot " + what + " a list of length " + vs.size()
      + "; its length must be equals to the amount (" + uniq
      + ") of unique entries";
    if (times != 1) msg += " times " + times;
    throw new IllegalArgumentException(msg);
  }
}
