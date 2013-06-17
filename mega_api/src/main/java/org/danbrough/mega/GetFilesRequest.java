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

public class GetFilesRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(GetFilesRequest.class.getSimpleName());

  public GetFilesRequest(MegaAPI megaAPI) {
    super(megaAPI);
    try {
      getRequestData().put("a", "f").put("c", 1).put("r", 1);
    } catch (JSONException e) {
    }
  }

  @Override
  public void onResponse(Object obj) throws JSONException {
    JSONObject o = (JSONObject) obj;

    log.debug("onResponse() {}", o.toString(1));
    if (o.has("u")) {
      megaAPI.process_u(o.getJSONArray("u"));
    }
    if (o.has("ok")) {
      megaAPI.process_ok(o.getJSONArray("ok"));
    }
    //
    // "s": [{
    // "h": "p08S2LyJ",
    // "r": 2,
    // "ts": 1371328442,
    // "u": "vnO5t0dt9iU"
    // }],
    //
    if (o.has("s")) {
      megaAPI.process_s(o.getJSONArray("s"));

    }

    // final String n_sn = "&sn=" + o.getString("sn");
    // megaAPI.sendRequest(new ApiRequest(megaAPI) {
    // @Override
    // public String getURL() {
    // return MegaProperties.getInstance().getApiPath() + "sc?c=100" + n_sn;
    // }
    //
    // @Override
    // public void onResponse(Object o) {
    // try {
    // log.debug("read: {}", ((JSONObject) o).toString(1));
    // megaAPI.load_notifications((JSONObject) o);
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // }
    // });
    //

  }
}
