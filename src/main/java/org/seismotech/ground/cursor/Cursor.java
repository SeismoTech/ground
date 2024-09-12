package org.seismotech.ground.cursor;

/**
 * Minimal <i>cursor</i> interface.
 * <p>It has only one method {@link #advance} moving the cursor to the next
 * element, returning {@code false} iff the end has been reached.
 * Initially, the cursor is pointing before the first element;
 * therefore, to access to the first element,
 * it is necessary to call {@link #adavance} once.
 * <p>The intended usage pattern is
 * <tt><pre>
 * while (cursor.advance()) {
 *   //Use cursor contents
 * }
 * </pre></tt>
 * <p>Cursor implementation could provide contents as a monolithic value
 * ({@link CursorOf}) or as a collection of values.
 */
public interface Cursor {
  boolean advance();
}
