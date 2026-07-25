package org.seismotech.ground.conc;

import java.util.concurrent.Executor;

public class XExecutors {
  private XExecutors() {}

  public static Executor newDirectExecutor() {return new DirectExecutor();}
}
