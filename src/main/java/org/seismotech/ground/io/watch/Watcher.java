package org.seismotech.ground.io.watch;

import org.seismotech.ground.service.Service1;

/**
 * A Watcher is a Service exploring part of a File System
 * and sending notifications to a listener.
 * These watchers must be configured during the creation,
 * or at least before launching them.
 */
public interface Watcher extends Service1 {
}
