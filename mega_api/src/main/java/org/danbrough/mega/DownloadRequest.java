/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import com.google.gson.JsonObject;

public class DownloadRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(DownloadRequest.class.getSimpleName());

  public DownloadRequest(MegaAPI megaAPI) {
    super(megaAPI);
  }

  @Override
  public JsonObject getRequestData() {
    requestData = new JsonObject();

    return requestData;
  }
}
