package org.seismotech.ground.cursor;

public interface IntCursor extends CursorOf<Integer> {
  int intValue();
  default Integer value() {return intValue();}
}
