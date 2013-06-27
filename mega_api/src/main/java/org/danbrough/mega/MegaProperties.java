/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.util.ResourceBundle;

public class MegaProperties {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(MegaProperties.class.getSimpleName());

  private static final MegaProperties INSTANCE = new MegaProperties();

  String userAgent;
  String defaultApiPath;
  String defaultFilePath;

  private MegaProperties() {
    ResourceBundle props = ResourceBundle.getBundle(MegaProperties.class
        .getCanonicalName());
    userAgent = props.getString("userAgent");
    defaultApiPath = props.getString("defaultApiPath");
    defaultFilePath = props.getString("defaultFilePath");
  }

  public static MegaProperties getInstance() {
    return INSTANCE;
  }

  public String getApiPath() {
    return defaultApiPath;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getDefaultFilePath() {
    return defaultFilePath;
  }

}
