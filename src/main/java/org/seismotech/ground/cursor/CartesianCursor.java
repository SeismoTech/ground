package org.seismotech.ground.cursor;

import java.util.function.BiFunction;

import org.seismotech.ground.util.Tuple2;

public class CartesianCursor<A extends Cursor, B extends Cursor, T>
  implements CursorOf<T> {

  private final A cursor1;
  private final B cursor2;
  private final BiFunction<A,B,T> pair;
  private boolean started;

  public CartesianCursor(A cursor1, B cursor2, BiFunction<A,B,T> pair) {
    this.cursor1 = cursor1;
    this.cursor2 = cursor2;
    this.pair = pair;
    this.started = false;
  }

  public static <C1 extends Cursor, C2 extends Cursor>
    CursorOf<Tuple2<C1,C2>> cursorTuples(
      C1 cursor1, C2 cursor2) {
    return new CartesianCursor<>(cursor1, cursor2,
        (c1,c2) -> Tuple2.of(c1, c2));
  }

  public static <T1, T2, C1 extends CursorOf<T1>, C2 extends CursorOf<T2>>
    CursorOf<Tuple2<T1,T2>> tuples(
      C1 cursor1, C2 cursor2) {
    return new CartesianCursor<>(cursor1, cursor2,
        (c1,c2) -> Tuple2.of(c1.value(), c2.value()));
  }

  @Override public Status status() {return cursor1.status();}

  @Override public void reset() {
    cursor1.reset();
    cursor2.reset();
    started = false;
  }

  @Override public boolean advance() {
    for (;;) {
      if (started && cursor2.advance()) return true;
      if (!cursor1.advance()) return false;
      started = true;
      cursor2.reset();
    }
  }

  public A value1() {return cursor1;}
  public B value2() {return cursor2;}

  @Override public T value() {return pair.apply(cursor1, cursor2);}
}
