package org.seismotech.ground.cursor;

public class FlatCursor<C extends Cursor> implements CursorOf<C> {

  private final CursorOf<C> cursors;
  private C active;

  public FlatCursor(CursorOf<C> cursors) {
    this.cursors = cursors;
    this.active = null;
  }

  public static <T> CursorOf<T> unnested(CursorOf<CursorOf<T>> cursors) {
    return new UnnestCursor<>(new FlatCursor<>(cursors));
  }

  @Override public Status status() {return cursors.status();}

  @Override public void reset() {
    cursors.reset();
    active = null;
  }

  @Override public boolean advance() {
    if (active != null && active.advance()) return true;
    if (!cursors.advance()) return false;
    active = cursors.value();
    return true;
  }

  @Override public C value() {return active;}

  @Override public String toString() {return "FlatCursor[" + cursors + "]";}
}
