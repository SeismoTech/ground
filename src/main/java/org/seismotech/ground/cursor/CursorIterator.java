package org.seismotech.ground.cursor;

import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * An {@code Iterator} build with a {@link CursorOf}.
 */
public class CursorIterator<T> implements Iterator<T> {

  protected final CursorOf<T> cursor;
  protected boolean ready;

  public CursorIterator(final CursorOf<T> cursor) {
    this.cursor = cursor;
    this.ready = false;
  }

  @Override public boolean hasNext() {
    return ready || (ready = cursor.advance());
  }

  @Override public T next() {
    if (hasNext()) {ready = false; return cursor.value();}
    return noSuchElement();
  }

  private static <T> T noSuchElement() {
    throw new NoSuchElementException();
  }
}
