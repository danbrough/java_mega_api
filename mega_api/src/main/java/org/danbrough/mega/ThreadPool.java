/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

  ScheduledExecutorService pool;
  boolean running = false;

  public synchronized void start() {
    if (running)
      return;
    running = true;
    pool = Executors.newScheduledThreadPool(2);
  }

  public synchronized void stop() {
    if (!running)
      return;
    running = false;
    pool.shutdownNow();
    pool = null;
  }

  public void background(Runnable job) {
    if (!running)
      return;
    pool.execute(job);
  }

  public void background(Runnable callable, long delay, TimeUnit unit) {
    if (!running)
      return;
    pool.schedule(callable, delay, unit);
  }
}
