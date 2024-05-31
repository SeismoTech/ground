package com.seismotech.ground.util;

import java.util.Random;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static com.seismotech.ground.util.BTreeList.Node;

class BTreeListTest {

  @Test
  void appendTest() {
    final int ORDER = 64;
    final int SIZE = 2*ORDER*ORDER;
    final BTreeList<Integer> xs = new BTreeList<>(ORDER);
    assertTrue(xs.isEmpty());
    assertEquals(0, xs.size());
    for (int i = 0; i < SIZE; i++) {
      xs.add(i);
      assertEquals(i+1, xs.size());
    }
    checkInvariants(xs.root, ORDER);
    assertTrue(isId(xs.toArray(new Integer[0])));
    for (int i = SIZE; i < 2*SIZE; i++) {
      xs.add(i);
      assertEquals(i+1, xs.size());
    }
    checkInvariants(xs.root, ORDER);
    assertTrue(isId(xs.toArray(new Integer[0])));
    for (int i = 0; i < 2*SIZE; i++) {
      assertEquals(i, xs.get(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> xs.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> xs.get(xs.size()));
  }

  @Test
  void insertTest() {
    final Random rnd = new Random();
    final int TIMES = 100;
    final int ORDER = 64;
    final int SIZE = 2*ORDER*ORDER;
    int onExtreme = 0;
    for (int t = 0; t < TIMES; t++) {
      final BTreeList<Long> xs = new BTreeList<>(ORDER);
      final Long[] ref = new Long[SIZE];
      final long forth = Long.MAX_VALUE / 4;
      ref[xs.size()] = forth;  xs.add(forth);
      ref[xs.size()] = 2*forth;  xs.add(2*forth);
      while (xs.size() < SIZE) {
        final int i = rnd.nextInt(xs.size() + 1);
        long prev = (i == 0) ? -1 : xs.get(i-1);
        long next = (i == xs.size()) ? -1 : xs.get(i);
        if (prev == -1) {prev = next - (xs.get(1) - next)/2;  onExtreme++;}
        else if (next == -1) {next = prev + (prev-xs.get(i-2)/2);  onExtreme++;}
        if (prev+1 >= next) continue;
        final long v = (prev+next) >>> 1;
        ref[xs.size()] = v;
        xs.add(i, v);
      }
      checkInvariants(xs.root, ORDER);
      Arrays.sort(ref);
      final Long[] ys = xs.toArray(new Long[0]);
      assertTrue(Arrays.equals(ref, ys));
    }
    //System.err.println("Extremes: " + onExtreme);
  }

  @Test
  void bulkAddTest() {
    final Random rnd = new Random();
    final int TIMES = 100;
    final int ORDER = 64;
    for (int s = 1; s < 2*ORDER*ORDER; s++) checkBulkAdd(ORDER, s);
    for (int t = 0; t < TIMES; t++) {
      final int size = 1+rnd.nextInt(2*ORDER*ORDER*ORDER);
      checkBulkAdd(ORDER, size);
    }
  }

  void checkBulkAdd(int order, int size) {
    final ArrayList<Integer> lst = new ArrayList<>(size);
    for (int i = 0; i < size; i++) lst.add(i);
    final BTreeList<Integer> xs = new BTreeList<>(order);
    xs.addAll(lst);
    assertEquals(size, xs.size());
    for (int i = 0; i < size; i++) assertEquals(i, xs.get(i));
    checkInvariants(xs.root, order);
  }

  @Test
  void setTest() {
    final int ORDER = 64;
    final int MAX_SIZE = 2*ORDER*ORDER;
    for (int size = 0; size <= MAX_SIZE; size++) {
      final BTreeList<Integer> xs = new BTreeList<>(ORDER);
      for (int i = 0; i < size; i++) xs.add(i+1);
      for (int i = 0; i < size; i++) xs.set(i, xs.get(i)-1);
      for (int i = 0; i < size; i++) assertEquals(i, xs.get(i));
    }
  }

  @Test
  void removeTest() {
    final int NONE = 0, ADD = 1, REM = 2;
    final float REM_CUT = 1 / (1 + 1+0.1f);  //10% more ADDs than REMs
    final float CHECK = 0.05f;
    final int OUTROUNDS = 25;
    final int ROUNDS = 100_000;
    final int ORDER = 64;
    final int MIN_SIZE = 0;
    final int MAX_SIZE = 1_000;
    final Random rnd = new Random();
    final ArrayList<Long> ref = new ArrayList<>();
    final BTreeList<Long> xs = new BTreeList<>(ORDER);
    for (int rr = 0; rr < OUTROUNDS; rr++) {
      final int roundCut = (rr == OUTROUNDS-1) ? 0 : ORDER / 4;
      for (int r = 0; r < ROUNDS || roundCut < xs.size(); r++) {
        final boolean mayAdd = xs.size() < MAX_SIZE && r < ROUNDS;
        final boolean mayRem = MIN_SIZE < xs.size() || ROUNDS <= r;
        final int action
          = (mayAdd && mayRem) ? rnd.nextDouble() < REM_CUT ? REM : ADD
          : mayAdd ? ADD
          : mayRem ? REM
          : NONE;
        switch (action) {
        case REM: {
          final int i = rnd.nextInt(ref.size());
          ref.remove(i);
          xs.remove(i);
          break;
        }
        case ADD: {
          final int i = rnd.nextInt(ref.size()+1);
          final long x = rnd.nextLong();
          ref.add(i, x);
          xs.add(i, x);
          break;
        }
        default: throw new IllegalStateException("Unexpected action " + action);
        }
        assertEquals(ref.size(), xs.size());
        if (ref.size() < 2*ORDER || rnd.nextFloat() < CHECK) {
          assertEquals(ref, xs);
          assertEquals(ref.size(), xs.size());
          checkInvariants(xs.root, ORDER);
        }
      }
    }
  }

  //----------------------------------------------------------------------
  @Test
  void forEachTest() {
    final int ORDER = 64;
    final int MAX_SIZE = 2*ORDER*ORDER;
    for (int size = 0; size <= MAX_SIZE; size++) {
      final BTreeList<Integer> xs = new BTreeList<>(ORDER);
      for (int i = 0; i < size; i++) xs.add(i);
      final ArrayList<Integer> ys = new ArrayList<>(size);
      xs.forEach(x -> ys.add(x));
      assertEquals(size, ys.size());
      assertTrue(isId(ys.toArray(new Integer[0])));
    }
  }

  @Test
  void iteratorTest() {
    final int ORDER = 64;
    final int MAX_SIZE = 2*ORDER*ORDER;
    for (int size = 0; size <= MAX_SIZE; size++) {
      final BTreeList<Integer> xs = new BTreeList<>(ORDER);
      for (int i = 0; i < size; i++) xs.add(i);
      final Iterator it = xs.iterator();
      for (int i = 0; i < size; i++) {
        assertTrue(it.hasNext());
        assertEquals(i, it.next());
      }
    }
  }

  //----------------------------------------------------------------------
  private static void checkInvariants(Node root, int order) {
    checkInvariants(root, order, 0);
  }

  private static long checkInvariants(Node node, int order, int depth) {
    assertEquals(order, node.data.length);
    assertThat(node.used, greaterThanOrEqualTo(
          depth > 0 ? (order+1)/2 : node.height == 0 ? 0 : 2));
    assertThat(node.used, lessThanOrEqualTo(order));
    for (int i = node.used; i < order; i++) assertNull(node.data[i]);
    if (node.height > 0) {
      long s = 0;
      for (int i = 0; i < node.used; i++) {
        final Node child = (Node) node.data[i];
        assertEquals(node, child.parent);
        s += checkInvariants(child, order, depth+1);
      }
      assertEquals(s, node.size);
    }
    return node.size;
  }

  private static boolean isId(Integer[] xs) {
    for (int i = 0; i < xs.length; i++) if (i != xs[i]) return false;
    return true;
  }
}
