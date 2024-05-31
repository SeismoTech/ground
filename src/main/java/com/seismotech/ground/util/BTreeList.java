package com.seismotech.ground.util;

import static java.lang.System.arraycopy;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.AbstractList;
import java.util.function.Consumer;

/**
 * An implementation of `List` using a b-tree.
 * The behavious characteristics are somehow between ArrayList
 * and LinkedList.
 * Except for small list, BTreeList will be more efficient,
 * both in cpu and memory, than LinkedList for almost any task.
 * In fact, memory usage is more like a ArrayList;
 * the bigger the order, the nearer to ArrayList.
 * Accessing to a position is O(log n);
 * worse than O(1) of ArrayList but much better than O(n) of LinkedList.
 * Inserting an element in an arbitrary position is O(order + log n);
 * better that ArrayList (except at positions near the end) and LinkedList
 * both needing O(n).
 * Intrinsically scanning operations (`forEach()`, `iterator().next()`)
 * will perform worse with BTreeList than with ArrayList or LinkedList,
 * as walking the b-tree is more complex.
 *
 * B-trees are usually used to implement maps and keys have an important
 * role in internal nodes.
 * In this implementation, keys are indices; they are implicit, not stored.
 * The index of a value is its position in an in-order walk of the tree.
 * Therefore, when a new value is inserted at position `i`,
 * values previously having index/position `j` with `j >= i`
 * will be shifted one position to the right
 * and will have index/position `j+1`.
 * This is, in fact, intended behaviour, to be able to implement
 * `add(int,E)` method.
 *
 * As an usual b-tree, we have 2 kinds of nodes: internal nodes and leaves.
 * Leaves nodes contains an array to values.
 * Internal nodes contains an array to offsping nodes.
 * To simplify typing, both nodes are implemented with the same class `Node`,
 * containing an Object array `data`,
 * that will have `E`s on leaves and `Node`s on internal nodes.
 * Leaves have `.height` = 0.
 */
public class BTreeList<E>
  extends AbstractList<E>
  implements List<E>, Cloneable {

  public static final int DEFAULT_ORDER = 64;

  protected final int order;
  protected Node root;

  public BTreeList() {this(64);}

  public BTreeList(int order) {
    this.order = order;
    this.root = newNode(0);
  }

  public BTreeList(int order, Node root) {
    this.order = order;
    this.root = root;
  }

  //----------------------------------------------------------------------
  @Override
  public boolean isEmpty() {return root.used == 0;}

  public long lsize() {return root.size;}

  @Override
  public int size() {
    long s = lsize();
    if (s < Integer.MAX_VALUE) return (int) s;
    throw new IllegalStateException(
      "Too big to retrieve the size (" + s + ") with this method");
  }

  @Override
  public E get(int i) {return get((long) i);}

  public E get(long i) {
    checkIndex(i);
    final FinderByIndex<E> finder = new FinderByIndex<>(root, i);
    finder.walk2Leaf();
    return asE(finder.node.data[(int) finder.off]);
  }

  @Override
  public E set(int i, E x) {return set((long) i, x);}

  public E set(long i, E x) {
    checkIndex(i);
    final FinderByIndex<E> finder = new FinderByIndex<>(root, i);
    finder.walk2Leaf();
    final int off = (int) finder.off;
    @SuppressWarnings("unchecked")
    final E old = (E) finder.node.data[off];
    finder.node.data[off] = x;
    return old;
  }

  public E first() {
    if (isEmpty()) firstOfEmpty();
    Node node = root;
    while (node.height > 0) node = (Node) node.data[0];
    @SuppressWarnings("unchecked")
    final E fst = (E) node.data[0];
    return fst;
  }

  public E last() {
    if (isEmpty()) lastOfEmpty();
    Node node = root;
    while (node.height > 0) node = (Node) node.data[node.used-1];
    @SuppressWarnings("unchecked")
    final E lst = (E) node.data[node.used-1];
    return lst;
  }

  @Override
  public void clear() {root = newNode(0);}

  //----------------------------------------------------------------------
  @Override
  public boolean add(E e) {
    append(e);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> xs) {
    bulkInsert(xs);
    ensureRightBranchInvariants();
    return true;
  }

  @Override
  public void add(int i, E e) { add((long) i, e); }

  public void add(long i, E e) {
    checkRange(i);
    insert(i, e);
  }

  private void bulkInsert(Iterable<? extends E> xs) {
    Node node = root;
    while (node.height > 0) node = (Node) node.data[node.used-1];
    final Iterator<? extends E> it = xs.iterator();
    while (it.hasNext()) {
      if (node.used == order) node = createLeafAfter(node);
      int i = node.used;
      while (i < order && it.hasNext()) node.data[i++] = it.next();
      final int delta = i - node.used;
      node.size = node.used = i;
      updateAncestorsSize(node, delta);
    }
  }

  private Node createLeafAfter(Node node) {
    final Node leaf = newNode(0);
    Node parent = node.parent;
    Node toLink = leaf;
    while (parent != null && parent.used == order) {
      parent = parent.parent;
      toLink = newParent(toLink);
    }
    if (parent == null) root = newRoot(root, toLink);
    else {
      parent.data[parent.used++] = toLink;
      toLink.parent = parent;
    }
    return leaf;
  }

  private void updateAncestorsSize(Node node, int delta) {
    for (Node parent = node.parent; parent != null; parent = parent.parent)
      parent.size += delta;
  }

  private void ensureRightBranchInvariants() {
    final int min = minOrder();
    Node last = root;
    while (last.height > 0) {
      final Node sndlast = (Node) last.data[last.used-2];
      last = (Node) last.data[last.used-1];
      if (min <= last.used) continue;
      final int toMove = min - last.used;
      arraycopy(last.data,0, last.data,toMove, last.used);
      arraycopy(sndlast.data,sndlast.used-toMove, last.data,0, toMove);
      nullify(sndlast, sndlast.used-toMove, sndlast.used);
      sndlast.used -= toMove;
      last.used = min;
      if (last.height == 0) {
        sndlast.size = sndlast.used;
        last.size = last.used;
      } else {
        long movedSize = 0;
        for (int i = 0; i < toMove; i++) {
          final Node child = (Node) last.data[i];
          movedSize += child.size;
          child.parent = last;
        }
        sndlast.size -= movedSize;
        last.size += movedSize;
      }
    }
  }

  private void append(E e) {
    final Node leaf = findLastLeaf(1);
    insert(leaf, leaf.used, e);
  }

  private void insert(long i, E e) {
    final FinderByIndex<E> finder = new FinderByIndex<>(root, i);
    finder.walk2Leaf(1);
    insert(finder.node, (int) finder.off, e);
  }

  private void insert(Node node, int i, E e) {
    Node oldNode = node;
    Node overNode = insertLeaf(oldNode, i, e);
    while (overNode != null && oldNode.parent != null) {
      final Node parent = oldNode.parent;
      overNode = insertInnerAfter(oldNode, overNode);
      oldNode = parent;
    }
    if (overNode != null) root = newRoot(oldNode, overNode);
  }

  private Node insertLeaf(Node node, int i, E e) {
    return (node.used < order) ? insertLeafVacants(node, i, e)
      : insertLeafFull(node, i, e);
  }

  private Node insertLeafVacants(Node node, int i, E e) {
    insertVacant(node, i, e);
    node.size++;
    return null;
  }

  private Node insertLeafFull(Node node, int i, E e) {
    final Node overNode = insertFull(node, i, e);
    node.size = node.used;
    overNode.size = overNode.used;
    return overNode;
  }

  private Node insertInnerAfter(Node oldNode, Node overNode) {
    final Node parent = oldNode.parent;
    final int i = childIndex(parent, oldNode) + 1;
    return (parent.used < order) ? insertInnerVacant(parent, i, overNode)
      : insertInnerFull(parent, i, overNode);
  }

  private Node insertInnerVacant(Node node, int i, Node child) {
    insertVacant(node, i, child);
    child.parent = node;
    return null;
  }

  private Node insertInnerFull(Node node, int i, Node child) {
    final Node overNode = insertFull(node, i, child);
    fixInnerNode(node);
    fixInnerNode(overNode);
    return overNode;
  }

  private void fixInnerNode(Node node) {
    final int used = node.used;
    final Object[] children = node.data;
    long s = 0;
    for (int i = 0; i < used; i++) {
      final Node child = (Node) children[i];
      child.parent = node;
      s += child.size;
    }
    node.size = s;
  }

  private void insertVacant(Node node, int i, Object value) {
    if (i < node.used) {
      arraycopy(node.data, i, node.data, i+1, node.used-i);
    }
    node.data[i] = value;
    node.used++;
  }

  private Node insertFull(Node node, int i, Object value) {
    final Node overNode = newNode(node.height);
    final int m = order;
    final int n = m / 2 + 1;
    if (i < n) {
      arraycopy(node.data,n-1, overNode.data,0, m-n+1);
      arraycopy(node.data,i, node.data,i+1, n-1-i);
      node.data[i] = value;
    } else {
      arraycopy(node.data,n, overNode.data,0, i-n);
      overNode.data[i-n] = value;
      arraycopy(node.data,i, overNode.data,i-n+1, m-i);
    }
    nullify(node, n, order);
    node.used = n;
    overNode.used = m+1 - n;
    return overNode;
  }

  //----------------------------------------------------------------------
  @Override
  public E remove(int i) {return remove((long) i);}

  public E remove(long i) {
    checkIndex(i);
    final FinderByIndex<E> finder = new FinderByIndex<>(root, i);
    finder.walk2Leaf(-1);
    final int off = (int) finder.off;
    @SuppressWarnings("unchecked")
    final E old = (E) finder.node.data[off];
    remove(finder.node, off);
    return old;
  }

  /**
   * The implementation approach for this method is very different to
   * the approach for {@link #insert}.
   * It is a big loop with all the cases by node neighbors size.
   * Nevertheless, there is no explicit analysis based on the height;
   * that is moved to a few auxiliary methods.
   */
  private void remove(Node leaf, int off) {
    leaf.size--;
    final int min = minOrder();
    Node node = leaf;
    int i = off;
    while (node.used-1 < min && node.parent != null) {
      final Node parent = node.parent;
      final int j = childIndex(parent, node);
      if (0 < j) {
        final Node left = (Node) parent.data[j-1];
        if (min < left.used) {
          arraycopy(node.data,0, node.data,1, i);
          left.used--;
          node.data[0] = left.data[left.used];
          left.data[left.used] = null;
          fixParent(node, 0);
          final long sizeDelta = childSize(node, 0);
          left.size -= sizeDelta;
          node.size += sizeDelta;
          return;
        } else {
          arraycopy(node.data,0, left.data,left.used, i);
          arraycopy(node.data,i+1, left.data,left.used+i, node.used-(i+1));
          final int used0 = left.used;
          left.used += node.used-1;
          fixNewChildren(left, used0, left.used);
        }
      } else /*if (j < parent.used-1)*/ {
        final Node right = (Node) parent.data[j+1];
        if (min < right.used) {
          arraycopy(node.data,i+1, node.data,i, node.used-(i+1));
          node.data[node.used-1] = right.data[0];
          remove(right.data, right.used, 0);
          right.used--;
          right.data[right.used] = null;
          fixParent(node, node.used-1);
          final long sizeDelta = childSize(node, node.used-1);
          right.size -= sizeDelta;
          node.size += sizeDelta;
          return;
        } else {
          arraycopy(right.data,0, right.data,node.used-1, right.used);
          arraycopy(node.data,0, right.data,0, i);
          arraycopy(node.data,i+1, right.data,i, node.used-(i+1));
          right.used += node.used-1;
          fixNewChildren(right, 0, node.used-1);
        }
      }
      node = parent;
      i = j;
    }
    remove(node.data, node.used, i);
    node.used--;
    node.data[node.used] = null;
    if (node.parent == null && node.used == 1 && node.height > 0) {
      root = (Node) node.data[0];
      root.parent = null;
    }
  }

  private void remove(Object[] data, int n, int i) {
    arraycopy(data,i+1, data,i, n-(i+1));
  }

  private long childSize(Node node, int i) {
    return (node.height == 0) ? 1 : ((Node) node.data[i]).size;
  }

  private void fixParent(Node node, int i) {
    if (node.height > 0) ((Node) node.data[i]).parent = node;
  }

  private void fixNewChildren(Node node, int init, int end) {
    if (node.height == 0) node.size = node.used;
    else {
      for (int i = init; i < end; i++) {
        final Node child = (Node) node.data[i];
        child.parent = node;
        node.size += child.size;
      }
    }
  }

  //----------------------------------------------------------------------
  private void nullify(Node node, int init, int end) {
    Arrays.fill(node.data, init, end, null);
  }

  private int childIndex(Node parent, Node child) {
    for (int i = 0; i < parent.used; i++) if (parent.data[i] == child) return i;
    notMyChild(parent, child);  return -1;
  }

  private void notMyChild(Node parent, Node child) {
    throw new IllegalStateException(
      "Node " + child + " is not a child of " + parent);
  }

  private Node findLastLeaf() {
    Node node = root;
    while (node.height > 0) node = (Node) node.data[node.used-1];
    return node;
  }

  private Node findLastLeaf(int sizeDelta) {
    Node node = root;
    while (node.height > 0) {
      node.size += sizeDelta;
      node = (Node) node.data[node.used-1];
    }
    return node;
  }

  private static class FinderByIndex<E> {
    Node node;
    long off;

    FinderByIndex(Node node, long off) {
      this.node = node;
      this.off = off;
    }

    void walk2Leaf() {
      while (node.height > 0) walk1();
    }

    void walk2Leaf(int sizeDelta) {
      while (node.height > 0) {
        node.size += sizeDelta;
        walk1();
      }
    }

    void walk1() {
      final int used = node.used;
      final Object[] children = node.data;
      int i = 0;
      long r = off;
      while (i < used-1) {
        final Node child = (Node) children[i];
        if (r < child.size) break;
        i++;
        r -= child.size;
      }
      node = (Node) children[i];
      off = r;
    }
  }

  //----------------------------------------------------------------------
  @Override
  public BTreeList<E> clone() {
    return new BTreeList<>(order, clone(root));
  }

  private Node clone(Node src) {
    final Node trg = newNode(src.height);
    trg.size = src.size;
    trg.used = src.used;
    if (src.height == 0) arraycopy(src.data,0, trg.data,0, src.used);
    else {
      for (int i = 0; i < src.used; i++) {
        final Node child = clone((Node) src.data[i]);
        child.parent = trg;
        trg.data[i] = child;
      }
    }
    return trg;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    final int height = root.height;
    final int[] cursors = new int[height+1];
    Node node = root;
    int i = height;
    for (;;) {
      if (i == 0) {
        for (int j = 0; j < node.used; j++) {
          @SuppressWarnings("unchecked")
          final E elem = (E) node.data[j];
          action.accept(elem);
        }
        node = node.parent;
        i++;
        if (height == 0) break;
        cursors[1]++;
      } else if (cursors[i] < node.used) {
        node = (Node) node.data[cursors[i]];
        i--;
      } else if (i < height) {
        cursors[i] = 0;
        node = node.parent;
        i++;
        cursors[i]++;
      } else {
        break;
      }
    }
  }

  @Override
  public Iterator<E> iterator() {return new BTreeIterator();}

  protected class BTreeIterator implements Iterator<E> {
    protected final int height;
    protected final int[] cursors;
    protected int cursor0;
    protected int level;
    protected Node node;

    protected BTreeIterator() {
      this.height = root.height;
      this.cursors = new int[height+1];
      this.cursor0 = 0;
      this.level = height;
      this.node = root;
      if (height > 0) searchLeaf();
    }

    public boolean hasNext() {
      return ensureNext();
    }

    public E next() {
      if (ensureNext()) {
        @SuppressWarnings("unchecked")
        final E elem = (E) node.data[cursor0++];
        return elem;
      }
      return noNext();
    }

    protected boolean ensureNext() {
      return cursor0 < node.used || advanceLeaf();
    }

    protected boolean advanceLeaf() {
      if (level == 0) {
        cursor0 = 0;
        node = node.parent;
        level++;
        if (height == 0) return false;
        cursors[1]++;
      }
      return searchLeaf();
    }

    protected boolean searchLeaf() {
      for (;;) {
        if (level == 0) {
          return true;
        } else if (cursors[level] < node.used) {
          node = (Node) node.data[cursors[level]];
          level--;
        } else if (level < height) {
          cursors[level] = 0;
          node = node.parent;
          level++;
          cursors[level]++;
        } else {
          return false;
        }
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("FIXME: TODO");
    }

    protected E noNext() {
      throw new NoSuchElementException();
    }
  }

  //----------------------------------------------------------------------
  @Override
  public Object[] toArray() {
    return toArray(new Object[0]);
  }

  @Override
  public <T> T[] toArray(T[] tmp) {
    if (ArrayUtil.MAX_SIZE < size()) tooBigToConvertToArray(size());
    final int s = (int) size();
    @SuppressWarnings("unchecked")
    final T[] xs = s <= tmp.length ? tmp
      : (T[]) Array.newInstance(tmp.getClass().getComponentType(), s);
    toArray(xs, 0, root);
    return xs;
  }

  private <T> int toArray(T[] xs, int next, Node node) {
    return node.height == 0 ? toArrayLeaf(xs, next, node)
      : toArrayInner(xs, next, node);
  }

  private <T> int toArrayLeaf(T[] xs, int next, Node node) {
    arraycopy(node.data,0, xs,next, node.used);
    return next + node.used;
  }

  private <T> int toArrayInner(T[] xs, int next, Node node) {
    int off = next;
    for (int i = 0; i < node.used; i++) {
      off = toArray(xs, off, (Node) node.data[i]);
    }
    return off;
  }

  //----------------------------------------------------------------------
  private int minOrder() {return (order+1) / 2;}

  private void checkIndex(long i) {
    if (i < 0 || lsize() <= i) indexOutOfBounds(i);
  }

  private void checkRange(long i) {
    if (i < 0 || lsize() < i) rangeOutOfBounds(i);
  }

  private void indexOutOfBounds(long i) {
    throw new IndexOutOfBoundsException(
      "Index " + i + " should be in the range [+0," + lsize() + ")");
  }

  private void rangeOutOfBounds(long i) {
    throw new IndexOutOfBoundsException(
      "Index " + i + " should be in the range [+0," + lsize() + "]");
  }

  private void tooBigToConvertToArray(long n) {
    throw new IllegalStateException(
      "Too big (" + n + ") to be converted to an array");
  }

  private void firstOfEmpty() {
    throw new IllegalStateException(
      "Cannot get first element of an empty collection");
  }

  private void lastOfEmpty() {
    throw new IllegalStateException(
      "Cannot get last element of an empty collection");
  }

  //----------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  private E asE(Object value) {return (E) value;}

  private Node newNode(int height) {return new Node(order, height);}

  private Node newParent(Node child) {
    final Node parent = newNode(child.height+1);
    parent.data[0] = child;
    parent.used++;
    child.parent = parent;
    return parent;
  }

  private Node newRoot(Node node0, Node node1) {
    final Node parent = newNode(node0.height+1);
    parent.data[0] = node0;
    parent.data[1] = node1;
    parent.used = 2;
    parent.size = node0.size + node1.size;
    node0.parent = node1.parent = parent;
    return parent;
  }

  protected static class Node {
    final int height;
    Node parent;
    long size;
    int used;
    final Object[] data;

    Node(int order, int height) {
      this.size = 0;
      this.parent = null;
      this.height = height;
      this.used = 0;
      this.data = new Object[order];
    }
  }

  //----------------------------------------------------------------------
  // private static String toString(Object[] xs, int n) {
  //   final StringBuilder sb = new StringBuilder();
  //   sb.append("[");
  //   for (int i = 0; i < n; i++) sb.append(xs[i]).append(", ");
  //   return sb.append("]").toString();
  // }

  // private long size(Node node) {
  //   if (node.height == 0) return node.size;
  //   long s = 0;
  //   for (int i = 0; i < node.used; i++) s += ((Node) node.data[i]).size;
  //   return s;
  // }

}
