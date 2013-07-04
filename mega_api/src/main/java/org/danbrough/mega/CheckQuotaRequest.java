/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.util.Locale;

import org.danbrough.mega.MegaAPI.ProtocolException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CheckQuotaRequest extends ApiRequest {

  public static enum Units {
    KB(1024f), MB(1048576f), GB(1073741824f);

    private float size;

    private Units(float size) {
      this.size = size;
    }
  }

  long quota = 0;
  long usage = 0;
  int balance = 0;

  public CheckQuotaRequest(MegaAPI megaAPI) {
    super(megaAPI);
    requestData = new JsonObject();
    requestData.addProperty("a", "uq");
    requestData.addProperty("xfer", 1);
    requestData.addProperty("pro", 1);
    requestData.addProperty("strg", 1);
  }

  @Override
  public void onResponse(JsonElement o) {
    super.onResponse(o);
    JsonObject job = o.getAsJsonObject();

    if (!job.has("mstrg"))
      throw new ProtocolException(o, "Expecting a mstrg");
    quota = job.get("mstrg").getAsLong();

    if (!job.has("cstrg"))
      throw new ProtocolException(o, "Expecting a cstrg");
    usage = job.get("cstrg").getAsLong();

    // if (job.has("balance"))
    // balance = job.get("balance").getAsInt();
  }

  public String getQuota(Units units) {
    return String.format(Locale.US, "%.02f", quota / units.size);
  }

  public String getUsage(Units units) {
    return String.format(Locale.US, "%.02f", usage / units.size);
  }

  public long getQuota() {
    return quota;
  }

  public long getUsage() {
    return usage;
  }

}
