package org.seismotech.ground.util;

public record Tuple2<A,B>(A _1, B _2) {
  public static <A,B> Tuple2<A,B> of(A a, B b) {
    return new Tuple2<>(a,b);
  }
}
