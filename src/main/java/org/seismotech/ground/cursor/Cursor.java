package org.seismotech.ground.cursor;

/**
 * Minimal <i>cursor</i> interface.
 *
 * <p>It has only one method {@link #advance} moving the cursor to the next
 * element, returning {@code false} iff the end has been reached.
 * Initially, the cursor is pointing before the first element;
 * therefore, to access to the first element,
 * it is necessary to call {@link #advance} once.
 *
 * <p>The intended usage pattern is
 * <tt><pre>
 * while (cursor.advance()) {
 *   //Use cursor contents
 * }
 * </pre></tt>
 * {@link #advance} must be monotonically decreasing;
 * it is legal, but useless, to keep calling it once {@code false} is returned.
 *
 * <p>Cursor implementation could provide contents as a monolithic value
 * ({@link CursorOf}) or as a collection of values.
 *
 * <p><b>Possible enhancements</b>
 *
 * <p>Method {@code ended()}.
 * Having reached the end of a Cursor ({@code advance()} returned false)
 * could be checked with {@code ended()}.
 * This method helps implementing continuation of a prematurely exited
 * processing.
 * For instance:
```
while (cursor.advance() && cursor.get() < N);
if (!cursor.ended()) {
  do {
    show(cursor.value());
  } while (cursor.advance());
}
```
 *
 * <p>Method {@code ready()} or {@code loaded()}.
 * This method returns true iff there is a value available;
 * that is, {@code advance()} was called and the last call returned true.
 * This method is very similar to {@code !ended()},
 * the only different being that this method returns false
 * if the cursor has never been advanced.
 */
public interface Cursor {
  boolean advance();

  default void reset() {throw new UnsupportedOperationException();}
}
