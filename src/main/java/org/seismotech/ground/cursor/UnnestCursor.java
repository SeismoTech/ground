package org.seismotech.ground.cursor;

public class UnnestCursor<T> implements CursorOf<T> {

  private final CursorOf<CursorOf<T>> inner;

  public UnnestCursor(CursorOf<CursorOf<T>> inner) {
    this.inner = inner;
  }

  @Override public void reset() {inner.reset();}

  @Override public boolean advance() {return inner.advance();}

  @Override public T value() {return inner.value().value();}

  @Override public String toString() {return "UnnestCursor[" + inner + "]";}
}
