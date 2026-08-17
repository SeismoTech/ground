package org.seismotech.ground.link;

public interface DLink<L extends DLink<L>> extends Link<L>{
  L prev();
  void prev(L prv);

  class Basic<L extends Basic<L>> extends Link.Basic<L> implements DLink<L> {
    private L prv;

    public Basic() {this(null, null);}

    public Basic(L prv, L nxt) {super(nxt); this.prv = prv;}

    @Override public L prev() {return prv;}

    @Override public void prev(L prv) {this.prv = prv;}
  }
}
