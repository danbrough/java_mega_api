/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.danbrough.mega.MegaAPI.FilesVisitor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetFilesRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(GetFilesRequest.class.getSimpleName());

  public GetFilesRequest(MegaAPI megaAPI) {
    super(megaAPI);

    JsonObject requestData = getRequestData();
    requestData.addProperty("a", "f");
    requestData.addProperty("c", "1");
    requestData.addProperty("r", 1);

  }

  @Override
  public void onResponse(JsonElement obj) {
    JsonObject o = obj.getAsJsonObject();

    log.debug("onResponse() {}", crypto.toPrettyString(o));
    if (o.has("u")) {

      megaAPI.process_u(o.get("u").getAsJsonArray());
    }
    if (o.has("ok")) {
      megaAPI.process_ok(o.get("ok").getAsJsonArray());
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
      megaAPI.process_s(o.get("s").getAsJsonArray());

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
    // log.debug("read: {}", ((JsonObject) o).toString(1));
    // megaAPI.load_notifications((JsonObject) o);
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // }
    // });
    //

    if (o.has("f")) {
      megaAPI.processFiles(o.get("f").getAsJsonArray(), new FilesVisitor() {

        @Override
        public boolean visit(MegaFile file) {
          log.debug("visited {}", file);
          return true;
        }
      });
    }
  }
}
