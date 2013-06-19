/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.IOException;
import java.math.BigInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LoginRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(LoginRequest.class.getSimpleName());

  UserContext ctx;

  public LoginRequest(MegaAPI megaAPI) throws Exception {
    super(megaAPI);

    ctx = getUserContext();

    String uh = crypto.stringhash(ctx.getEmail(), ctx.getPasswordKey());

    JsonObject requestData = getRequestData();
    requestData.addProperty("a", "us");
    requestData.addProperty("user", ctx.getEmail());
    requestData.addProperty("uh", uh);
  }

  @Override
  public void onResponse(JsonElement response) {
    super.onResponse(response);

    try {
      computeSid(response.getAsJsonObject());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

  }

  protected void computeSid(JsonObject response) throws IOException {
    log.info("computeSid()");

    byte encrypted_master_key[] = crypto.base64urldecode(response.get("k")
        .getAsString());

    byte master_key[] = crypto.decrypt_key(encrypted_master_key, megaAPI
        .getUserContext().getPasswordKey());

    megaAPI.getUserContext().setMasterKey(master_key);

    byte encrypted_rsa_private_key[] = crypto.base64urldecode(response.get(
        "privk").getAsString());

    byte private_key[] = crypto.decrypt_key(encrypted_rsa_private_key,
        master_key);

    BigInteger rsa_private_key[] = { BigInteger.ZERO, BigInteger.ZERO,
        BigInteger.ZERO, BigInteger.ZERO };

    for (int i = 0; i < 4; i++) {
      // l = ((ord(private_key[0]) * 256 + ord(private_key[1]) + 7) >> 3) + 2
      int l = ((0x0000ff00 & (private_key[0] << 8) + (private_key[1] & 0x00ff)
          + 7) >> 3) + 2;
      log.debug("l: " + l);
      byte b[] = new byte[l];
      System.arraycopy(private_key, 0, b, 0, b.length);

      rsa_private_key[i] = crypto.mpi2big(b);
      // log.debug("rsa_private_key: " + i + " = " + crypto.toHex(b));
      // crypto.toHex(rsa_private_key[i].toByteArray()));
      // Assert.assertEquals(example.rsa_private_keys[i],
      // crypto.bigToString(rsa_private_key[i]));
      // log.debug("first ok at: " + i);

      b = new byte[private_key.length - l];
      System.arraycopy(private_key, l, b, 0, b.length);
      private_key = b;

    }

    ctx.setRsaPrivateKey(rsa_private_key);

    BigInteger encrypted_sid = crypto.mpi2big(crypto.base64urldecode(response
        .get("csid").getAsString()));

    BigInteger bResult = crypto.RSAdecrypt(encrypted_sid, rsa_private_key[2],
        rsa_private_key[0], rsa_private_key[1], rsa_private_key[3]);
    String sResult = crypto.bigToString(bResult);

    if (sResult.charAt(0) == 0)
      sResult = sResult.substring(1);

    String sid = crypto
        .base64urlencode(crypto.fromHex(sResult.substring(0, 86)));
    // log.debug("sid: {}", sid);
    ctx.setSid(sid);

  }
}
