/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.danbrough.mega.MegaAPI.ProtocolException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CheckQuotaRequest extends ApiRequest {

  long quota = 0;
  long usage = 0;

  public CheckQuotaRequest(MegaAPI megaAPI) {
    super(megaAPI);
    JsonObject requestData = getRequestData();
    requestData.addProperty("a", "uq");
    requestData.addProperty("xfer", 1);
    requestData.addProperty("pro", 1);
    requestData.addProperty("strg", 1);
  }

  @Override
  public void onResponse(JsonElement o) {
    JsonObject job = o.getAsJsonObject();

    if (!job.has("mstrg"))
      throw new ProtocolException(o, "Expecting a mstrg");
    quota = job.get("mstrg").getAsLong();
    if (!job.has("cstrg"))
      throw new ProtocolException(o, "Expecting a cstrg");
    usage = job.get("cstrg").getAsLong();
  }

  public long getQuota() {
    return quota;
  }

  public long getUsage() {
    return usage;
  }
}
