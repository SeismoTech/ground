package org.seismotech.ground.link;

public interface LinkSig<L> {
  L next(L trg);
  void next(L trg, L nxt);

  class ForLink<L extends Link<L>> implements LinkSig<L> {
    @Override public L next(L trg) {return trg.next();}
    @Override public void next(L trg, L nxt) {trg.next(nxt);}
  }
}
