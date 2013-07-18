/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

public class FileStore {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(FileStore.class.getSimpleName());
  
  MegaAPI megaAPI;

  public FileStore(MegaAPI megaAPI) {
    super();
    this.megaAPI = megaAPI;
  }
  
  public void start(){
    log.info("start()");
  }
  
  public void stop(){
    log.info("stop()");
  }

}
