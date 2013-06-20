/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.math.BigInteger;
import java.util.HashMap;

public class UserContext {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(UserContext.class.getSimpleName());

  private String email;

  private byte[] passwordKey;

  // session id
  private String sid;

  // users display name
  private String name;

  // users private key
  private BigInteger[] rsa_private_key;

  private final HashMap<String, byte[]> sharedKeys = new HashMap<String, byte[]>();

  private byte[] masterKey;

  private String rootId;
  private String trashId;
  private String inboxId;

  private String handle;

  private String sn = "";

  protected UserContext() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPasswordKey(byte[] passwordKey) {
    this.passwordKey = passwordKey;
  }

  public byte[] getPasswordKey() {
    return passwordKey;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public BigInteger[] getPrivateKey() {
    return rsa_private_key;
  }

  public void setRsaPrivateKey(BigInteger[] rsa_private_key) {
    this.rsa_private_key = rsa_private_key;
  }

  public void setMasterKey(byte[] master_key) {
    this.masterKey = master_key;
  }

  public byte[] getMasterKey() {
    return masterKey;
  }

  public HashMap<String, byte[]> getSharedKeys() {
    return sharedKeys;
  }

  public String getRootId() {
    return rootId;
  }

  public void setRootId(String rootId) {
    this.rootId = rootId;
  }

  public String getTrashId() {
    return trashId;
  }

  public void setTrashId(String trashId) {
    this.trashId = trashId;
  }

  public String getInboxId() {
    return inboxId;
  }

  public void setInboxId(String inboxId) {
    this.inboxId = inboxId;
  }

  public void setHandle(String u) {
    this.handle = u;
  }

  public String getHandle() {
    return handle;
  }

  public void setSn(String sn) {
    this.sn = sn;
  }

  public String getSn() {
    return sn;
  }
}
