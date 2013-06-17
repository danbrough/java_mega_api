/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.json.JSONObject;

/**
 * attributes:
 * 
 * "a": "", "h": "I91ESJKZ", "k": "", "p": "", "t": 2, "ts": 1370612740, "u":
 * "XF6DfmTorLE"
 */

public class MegaFile {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaFile.class.getSimpleName());

  public static final int TYPE_FILE = 0;
  public static final int TYPE_DIR = 1;
  public static final int TYPE_ROOT = 2;
  public static final int TYPE_INBOX = 3;
  public static final int TYPE_TRASH = 4;

  private final JSONObject job = new JSONObject();

  public MegaFile() {
    super();
  }

  public int getType() {
    return -1;
  }

}
