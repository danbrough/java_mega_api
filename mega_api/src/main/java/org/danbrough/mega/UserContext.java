/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.math.BigInteger;

public class UserContext {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(UserContext.class.getSimpleName());
  int sequence = (int) Math.ceil((Math.random() * 1000000000));
  private String email;

  private byte[] passwordKey;

  // session id
  private String sid;

  // users display name
  private String name;

  // users private key
  private BigInteger[] rsa_private_key;

  public UserContext(String email, byte passwordKey[]) {
    super();
    setEmail(email);
    setPasswordKey(passwordKey);
  }

  public UserContext(String email, String password) {
    super();
    setEmail(email);
    setPasswordKey(new Crypto().prepareKey(password));

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

  public int getNextRequestID() {
    return ++sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

}
