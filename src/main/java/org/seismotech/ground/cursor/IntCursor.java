package org.seismotech.ground.cursor;

public interface IntCursor extends CursorOf<Integer> {
  int intValue();

  @Override default Integer value() {return intValue();}
}
