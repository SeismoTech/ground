package org.seismotech.ground.service;

import java.util.concurrent.TimeUnit;

/**
 * A *One-Shot Service*:
 * it is an active entity that can be started and could be *shut down*.
 * This interface gives no information about the entity,
 * neither about how to start it.
 * It only provides a way to know its running state and to shut down it.
 * This service cannot be restart, due to its *one shot* characteristic.
 */
public interface Service1 {
  enum RunningStatus {CREATED, RUNNING, SHUTTINGDOWN, TERMINATED}

  RunningStatus runningStatus();

  void shutdown();

  void awaitTermination()
  throws InterruptedException;

  boolean awaitTermination(long timeout, TimeUnit unit)
  throws InterruptedException;
}
