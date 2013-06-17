/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import com.google.gson.JsonObject;

public class CheckQuotaRequest extends ApiRequest {

  long quota = 0;

  public CheckQuotaRequest(MegaAPI megaAPI) {
    super(megaAPI);
    JsonObject requestData = getRequestData();
    requestData.addProperty("a", "uq");
    requestData.addProperty("xfer", 1);
  }

  @Override
  public void onResponse(Object o) {
    JsonObject job = (JsonObject) o;
    if (!job.has("mstrg"))
      throw new RuntimeException("Expecting a mstrg in the response");
    quota = job.get("mstrg").getAsLong();
  }

  public long getQuota() {
    return quota;
  }
}
