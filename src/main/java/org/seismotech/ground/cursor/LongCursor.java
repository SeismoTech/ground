package org.seismotech.ground.cursor;

public interface LongCursor extends CursorOf<Long> {
  long longValue();

  @Override default Long value() {return longValue();}
}
