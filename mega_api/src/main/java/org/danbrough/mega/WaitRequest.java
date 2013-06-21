/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

public class WaitRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(WaitRequest.class.getSimpleName());

  public WaitRequest(MegaAPI megaApi) {
    super(megaApi);
  }

}
