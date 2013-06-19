/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.Serializable;

import com.google.gson.JsonObject;

/**
 * attributes:
 * 
 * "a": "", "h": "I91ESJKZ", "k": "", "p": "", "t": 2, "ts": 1370612740, "u":
 * "XF6DfmTorLE"
 */

public class MegaFile implements Serializable {

  private static final long serialVersionUID = 7765528900820913999L;

  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(MegaFile.class.getSimpleName());

  public static final int TYPE_FILE = 0;
  public static final int TYPE_DIR = 1;
  public static final int TYPE_ROOT = 2;
  public static final int TYPE_INBOX = 3;
  public static final int TYPE_TRASH = 4;

  private int type;
  private int timestamp;
  private String parent;
  private final String handle;
  private String key;
  private JsonObject attrs;
  private String name;

  public MegaFile(String handle) {
    super();
    this.handle = handle;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setTimestamp(int timestamp) {
    this.timestamp = timestamp;
  }

  public int getTimestamp() {
    return timestamp;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getHandle() {
    return handle;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setAttributes(JsonObject attrs) {
    this.attrs = attrs;
  }

  public JsonObject getAttributes() {
    return attrs;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
