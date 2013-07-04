/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.danbrough.mega.ApiRequest;
import org.danbrough.mega.MegaAPI;
import org.danbrough.mega.MegaAPI.ProtocolException;

import com.google.gson.JsonObject;

public class UploadRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(UploadRequest.class.getSimpleName());

  private final File file;
  private final String destDir;

  public UploadRequest(MegaAPI megaAPI, File file, String destDir) {
    super(megaAPI);
    this.file = file;
    this.destDir = destDir;
  }

  protected String getDestDir() {
    return destDir;
  }

  protected long getSize() {
    return file.length();
  }

  protected String getDestName() {
    return file.getName();
  }

  protected InputStream getInput() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public void send() {
    log.info("send() {}", file);

    // req.push({ a : 'u', ssl : use_ssl, ms : ul_maxSpeed, s :
    // ul_queue[i].size, r : ul_queue[i].retries, e : ul_lastreason });

    // retrieve the upload url
    new ApiRequest(megaAPI) {

      @Override
      public com.google.gson.JsonObject getRequestData() {
        JsonObject payload = new JsonObject();
        payload.addProperty("a", "u");
        payload.addProperty("s", getSize());
        return payload;
      };

      @Override
      public void onResponse(com.google.gson.JsonElement o) {
        if (!o.isJsonObject()) {
          throw new MegaAPI.ProtocolException(o, "Expecting object");
        }
        if (!o.getAsJsonObject().has("p")) {
          throw new MegaAPI.ProtocolException(o, "Expecting property 'p'");
        }
        send2(o.getAsJsonObject().get("p").getAsString());
      }

    }.send();
  }

  private void send2(String p) {
    log.trace("send2() p:{}", p);
    // p = the upload url

    // ul_key = Array(6);
    //
    // // generate ul_key and nonce
    // for (i = 6; i--; ) ul_key[i] = rand(0x100000000);
    //
    // ul_keyNonce = JSON.stringify(ul_key);
    byte ul_key[] = new byte[24];
    megaAPI.getRandom().nextBytes(ul_key);

  }

}
