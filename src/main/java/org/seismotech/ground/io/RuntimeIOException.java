package org.seismotech.ground.io;

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {
  public RuntimeIOException(String msg) {super(msg);}
  public RuntimeIOException(IOException cause) {super(cause);}
  public RuntimeIOException(String msg, IOException cause) {super(msg, cause);}
}
