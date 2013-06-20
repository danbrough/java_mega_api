/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import javax.xml.ws.ProtocolException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PollRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(PollRequest.class.getSimpleName());

  public PollRequest(MegaAPI megaApi) {
    super(megaApi);
  }

  @Override
  public String getRequestParams() {
    return "sc?ssl=1&sn=" + megaAPI.getUserContext().getSn();
  }

  @Override
  public void onResponse(JsonElement o) {
    super.onResponse(o);

    JsonObject obj = o.getAsJsonObject();

    if (obj.has("w")) {
      String waitUrl = obj.get("w").getAsString();
      log.warn("found wait url {}", waitUrl);
      // if (waitbackoff > 1000) setTimeout(waitsc,waitbackoff);
      // else waitsc();
      return;
    }

    if (obj.has("sn")) {
      getUserContext().setSn(obj.get("sn").getAsString());
      log.trace("found sn: {}", getUserContext().getSn());
    } else {
      throw new ProtocolException("Expecting a sn field");
    }

    if (obj.has("a")) {
      if (obj.get("a").isJsonArray()) {
        processA(obj.get("a").getAsJsonArray());
      } else {
        log.error("a = {}", obj.get("a"));
        // if (a == -15)
        // {
        // ul_uploading=false;
        // downloading=false;
        // logout();
        // return false;
        // }
      }
    } else {
      throw new ProtocolException("Expecting a \"a\" field");
    }
  }

  void processA(JsonArray w) {
    for (JsonElement e : w) {
      processPacket(e.getAsJsonObject());
    }
    packetComplete();
  }

  void packetComplete() {
    log.error("packetComplete() not implemented");
  }

  void processPacket(JsonObject o) {
    String a = o.get("a").getAsString();

  }
}
