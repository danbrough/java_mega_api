/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MegaAPI {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaAPI.class.getSimpleName());

  protected final Crypto crypto;
  protected final Transport transport;

  public MegaAPI() {
    super();
    this.crypto = createCrypto();
    this.transport = createTransport();
  }

  protected Crypto createCrypto() {
    return new Crypto();
  }

  protected Transport createTransport() {
    return new Transport();
  }

  public String getTermsHTML() {

    BufferedReader input = new BufferedReader(new InputStreamReader(
        MegaAPI.class.getResourceAsStream("/org/danbrough/mega/terms.html")));
    String s = null;
    StringBuffer content = new StringBuffer();
    try {
      while ((s = input.readLine()) != null) {
        content.append(s).append('\n');
      }
      input.close();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    return content.toString();
  }

  public void start() {
    log.info("start()");
    transport.start();
  }

  public void stop() {
    log.info("stop()");
    transport.stop();
  }

  public void sendRequest(ApiRequest request) {
    transport.queueRequest(request);
  }

}
