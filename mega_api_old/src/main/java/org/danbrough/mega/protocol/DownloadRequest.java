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

import com.google.gson.JsonObject;

public class DownloadRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(DownloadRequest.class.getSimpleName());

  private final MegaFile file;

  public DownloadRequest(MegaAPI megaAPI, MegaFile file) {
    super(megaAPI);
    this.file = file;
  }

  @Override
  public JsonObject getRequestData() {
    requestData = new JsonObject();

    // dl_keyNonce =
    // JSON.stringify([dl_key[0]^dl_key[4],dl_key[1]^dl_key[5],dl_key[2]^dl_key[6],dl_key[3]^dl_key[7],dl_key[4],dl_key[5]]);

    return requestData;
  }
}
