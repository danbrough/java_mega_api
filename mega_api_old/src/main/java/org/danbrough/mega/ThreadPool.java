/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;


import java.util.concurrent.TimeUnit;

public interface ThreadPool {

  void start();

  void stop();

  void background(Runnable job);

  void background(Runnable callable, long delay, TimeUnit unit);
}
