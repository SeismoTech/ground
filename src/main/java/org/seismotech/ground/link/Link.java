package org.seismotech.ground.link;

public interface Link<L extends Link<L>> {
  L next();
  void next(L nxt);

  class Basic<L extends Basic<L>> implements Link<L> {
    private L nxt;

    public Basic() {this(null);}

    public Basic(L nxt) {this.nxt = nxt;}

    @Override public L next() {return nxt;}

    @Override public void next(L nxt) {this.nxt = nxt;}
  }
}
