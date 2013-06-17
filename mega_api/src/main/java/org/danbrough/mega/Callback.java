/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.IOException;

public class Callback {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Callback.class.getSimpleName());

  public void onError(int code) {
    log.error("onError() code: " + code);
  }

  public void onError(IOException exception) {
    log.error("onError()", exception);
  }

  public void onResponse(Object o) {
    log.debug("onResponse() {}", o);
  }
}