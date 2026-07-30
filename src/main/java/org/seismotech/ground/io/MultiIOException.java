package org.seismotech.ground.io;

import java.io.IOException;

import org.seismotech.ground.lang.ExceptionBundle;

public class MultiIOException extends IOException {

  private final ExceptionBundle<? extends IOException> bundle;

  public MultiIOException(ExceptionBundle<? extends IOException> bundle) {
    super(bundle.simpleMessage(), bundle.firstCause());
    this.bundle = bundle;
  }

  public ExceptionBundle<? extends IOException> bundle() {return bundle;}
}
