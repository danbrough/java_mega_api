/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import com.google.gson.JsonObject;

public class GetUserRequest extends ApiRequest {

  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(GetUserRequest.class.getSimpleName());

  public GetUserRequest(MegaAPI megaAPI) {
    super(megaAPI);
    requestData = new JsonObject();
    requestData.addProperty("a", "ug");

  }
}
