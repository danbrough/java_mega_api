/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckQuotaRequest extends ApiRequest {

  long quota = 0;

  public CheckQuotaRequest(MegaAPI megaAPI) throws JSONException {
    super(megaAPI);
    getRequestData().put("a", "uq").put("xfer", 1);
  }

  @Override
  public void onResponse(Object o) throws JSONException {
    JSONObject job = (JSONObject) o;
    if (!job.has("mstrg"))
      throw new JSONException("Expecting a mstrg in the response");
    quota = job.getLong("mstrg");
  }

  public long getQuota() {
    return quota;
  }
}
