/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

public class AndroidClient extends MegaClient {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(AndroidClient.class.getSimpleName());

  private transient MegaApplication application;

  public void setApplication(MegaApplication application) {
    this.application = application;
  }

  @Override
  public void onNodesModified() {
    application.onNodesModified();
  }

  @Override
  public void setCurrentFolder(Node folder) {
    super.setCurrentFolder(folder);
    application.onFolderChanged(getCurrentFolder());
  }

}
