package org.seismotech.ground.link;

public class DLinker<L> implements DLinkSig<L> {

  //----------------------------------------------------------------------
  // Static behaviour

  /**
   * Unlinks {@code trg}.
   * Assumes that {@code trg} belongs to a double-linked list.
   * Both prev and next pointers are set to {@code null}.
   */
  public static <L> void unlink(DLinkSig<L> sig, L trg) {
    final L prv = sig.prev(trg), nxt = sig.next(trg);
    if (prv != null) {sig.next(prv, nxt); sig.prev(trg, null);}
    if (nxt != null) {sig.prev(nxt, prv); sig.next(trg, null);}
  }

  /**
   * Self-links {@code trg}.
   * Assumes {@code trg} is unlinked.
   */
  public static <L> void selflink(DLinkSig<L> sig, L trg) {
    sig.next(trg, trg);
    sig.prev(trg, trg);
  }

  /**
   * Links {@code trg} after {@code prv}.
   * Allows {@code prv} to be {@code null}.
   * Assumes that {@code trg} is unlinked (its pointers content can be ignored)
   * and that {@code prv}, if not null, belongs to a double-linked list.
   * Returns first node: {@code prv} if not null; {@code trg} otherwise.
   */
  public static <L> L after(DLinkSig<L> sig, L trg, L prv) {
    final L nxt = prv == null ? null : sig.next(prv);
    if (prv != null) sig.next(prv, trg);
    sig.prev(trg, prv);
    sig.next(trg, nxt);
    if (nxt != null) sig.prev(nxt, trg);
    return prv != null ? prv : trg;
  }

  /**
   * Links {@code trg} before {@code nxt}.
   * Allows {@code nxt} to be {@code null}.
   * Assumes that {@code trg} is unlinked (its pointers content can be ignored)
   * and that {@code next}, if not null, belongs to a double-linked list.
   * Returns first node: always {@code trg}.
   */
  public static <L> L before(DLinkSig<L> sig, L trg, L nxt) {
    final L prv = nxt == null ? null : sig.prev(nxt);
    if (prv != null) sig.next(prv, trg);
    sig.prev(trg, prv);
    sig.next(trg, nxt);
    if (nxt != null) sig.prev(nxt, trg);
    return trg;
  }

  /**
   * First element of the double-linked list containing {@code trg}.
   * If the list is circular, the element following {@code trg}.
   */
  public static <L> L first(DLinkSig<L> sig, L trg) {
    if (trg == null) return null;
    L tmp = trg;
    for (;;) {
      final L prv = sig.prev(tmp);
      if (prv == null || prv == trg) break;
      tmp = prv;
    }
    return tmp;
  }

  /**
   * Last element of the double-linked list containing {@code trg}.
   * If the list is circular, the element preceding {@code trg}.
   */
  public static <L> L last(DLinkSig<L> sig, L trg) {
    if (trg == null) return null;
    L tmp = trg;
    for (;;) {
      final L nxt = sig.next(tmp);
      if (nxt == null || nxt == trg) break;
      tmp = nxt;
    }
    return tmp;
  }

  /**
   * Checks that the double-linked list containing {@code lnk} is
   * correctly linked.
   * Allows open and circular linked lists.
   * Returns {@code null} if correctly lined;
   * otherwise, a reachable link not correctly linked.
   */
  public static <L> L firstBad(DLinkSig<L> sig, L lnk) {
    if (lnk == null) return null;
    L tmp = lnk;
    for (;;) {
      final L nxt = sig.next(tmp);
      if (nxt == null) break;
      if (sig.prev(nxt) != tmp) return tmp;
      if (nxt == lnk) return null;
    }
    tmp = lnk;
    for (;;) {
      final L prv = sig.prev(tmp);
      if (prv == null) return null;
      if (sig.next(prv) != tmp) return tmp;
    }
  }

  //----------------------------------------------------------------------
  // Dynamic behaviour

  private final DLinkSig<L> sig;

  public DLinker(DLinkSig<L> sig) {this.sig = sig;}

  @Override public L next(L trg) {return sig.next(trg);}
  @Override public L prev(L trg) {return sig.prev(trg);}
  @Override public void next(L trg, L nxt) {sig.next(trg, nxt);}
  @Override public void prev(L trg, L prv) {sig.prev(trg, prv);}

  public void unlink(L trg) {unlink(sig, trg);}

  public void selflink(L trg) {selflink(sig, trg);}

  public L after(L trg, L prv) {return after(sig, trg, prv);}

  public L before(L trg, L nxt) {return before(sig, trg, nxt);}

  public L first(L trg) {return first(sig, trg);}

  public L last(L trg) {return last(sig, trg);}

  public L firstBad(L trg) {return firstBad(sig, trg);}
}
