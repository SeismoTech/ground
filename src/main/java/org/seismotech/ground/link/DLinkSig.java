package org.seismotech.ground.link;

public interface DLinkSig<L> extends LinkSig<L> {
  L prev(L trg);
  void prev(L trg, L prv);

  class ForDLink<L extends DLink<L>>
    extends LinkSig.ForLink<L>
    implements DLinkSig<L> {

    @Override public L prev(L trg) {return trg.prev();}
    @Override public void prev(L trg, L prv) {trg.prev(prv);}
  }
}
