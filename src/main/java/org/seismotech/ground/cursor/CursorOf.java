package org.seismotech.ground.cursor;

/**
 * A cursor giving access to <i>monolithic</i> value of type {@code T}.
 */
public interface CursorOf<T> extends Cursor {
  T value();
}
