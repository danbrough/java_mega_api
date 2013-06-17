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
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

  public JSONObject toJSON() {
    JSONObject o = new JSONObject();
    Crypto crypto = Crypto.getInstance();
    try {
      if (name != null)
        o.put("name", name);
      if (email != null)
        o.put("email", email);
      if (sid != null)
        o.put("sid", sid);
      if (masterKey != null)
        o.put("masterKey", crypto.base64urlencode(masterKey));
      if (passwordKey != null)
        o.put("passwordKey", crypto.base64urlencode(passwordKey));
      if (rsa_private_key != null) {
        JSONArray a = new JSONArray();
        for (int i = 0; i < rsa_private_key.length; i++) {
          a.put(i, rsa_private_key[i].toString(16));
        }
        o.put("rsa_private_key", a);
      }
      JSONObject keys = new JSONObject();
      for (String key : sharedKeys.keySet()) {
        keys.put(key, crypto.base64urlencode(sharedKeys.get(key)));
      }
      o.put("sharedKeys", keys);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return o;
  }

  public void fromJSON(JSONObject o) {
    Crypto crypto = Crypto.getInstance();

    try {
      if (o.has("name"))
        name = o.getString("name");
      if (o.has("sid"))
        sid = o.getString("sid");
      if (o.has("email"))
        email = o.getString("email");
      if (o.has("masterKey"))
        masterKey = crypto.base64urldecode(o.getString("masterKey"));
      if (o.has("passwordKey"))
        passwordKey = crypto.base64urldecode(o.getString("passwordKey"));
      if (o.has("rsa_private_key")) {
        JSONArray a = o.getJSONArray("rsa_private_key");
        rsa_private_key = new BigInteger[a.length()];
        for (int i = 0; i < rsa_private_key.length; i++) {
          rsa_private_key[i] = new BigInteger(a.getString(i), 16);
        }
      }
      if (o.has("sharedKeys")) {
        JSONObject keys = o.getJSONObject("sharedKeys");
        for (Iterator i = keys.keys(); i.hasNext();) {
          String key = (String) i.next();
          sharedKeys.put(key, crypto.base64urldecode(keys.getString(key)));
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
