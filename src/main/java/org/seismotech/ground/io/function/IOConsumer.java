package org.seismotech.ground.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<T> {
  void accept(T t) throws IOException;

  default IOConsumer<T> andThen(IOConsumer<? super T> after) {
    return t -> {this.accept(t); after.accept(t);};
  }
}
