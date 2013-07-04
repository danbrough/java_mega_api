/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.protocol;

import org.danbrough.mega.ApiRequest;
import org.danbrough.mega.MegaAPI;
import org.danbrough.mega.MegaFile;
import org.danbrough.mega.MegaProperties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetLinkRequest extends ApiRequest {

  protected String link;
  private final MegaFile file;

  public GetLinkRequest(MegaAPI megaAPI, MegaFile file) {
    super(megaAPI);
    this.file = file;
  }

  @Override
  public JsonObject getRequestData() {
    requestData = new JsonObject();
    requestData.addProperty("a", "l");
    requestData.addProperty("n", file.getHandle());
    return super.getRequestData();
  }

  @Override
  public void onResponse(JsonElement o) {
    link = o.getAsString();
  }

  public String getUrl() {
    return MegaProperties.getInstance().getDefaultFilePath() + "#!" + link
        + '!' + file.getDecodedKey();
  }
}
