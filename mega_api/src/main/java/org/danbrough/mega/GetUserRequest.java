/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.json.JSONException;

public class GetUserRequest extends ApiRequest {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(GetUserRequest.class.getSimpleName());

  public GetUserRequest(UserContext ctx) {
    super(ctx);

    try {
      getRequestData().put("a", "ug");
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
    }

  }
}
