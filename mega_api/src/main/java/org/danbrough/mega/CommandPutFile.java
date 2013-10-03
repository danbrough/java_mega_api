/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.File;

import com.google.gson.JsonElement;

public class CommandPutFile extends Command<Void> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CommandPutFile.class.getSimpleName());
  private File file;

  public CommandPutFile(File file, Callback<Void> callback) {
    super("u", callback);
    addArg("s", file.length());
    addArg("ms", 0);
    this.file = file;
  }

  @Override
  public void processResponse(JsonElement e) throws Exception {
    log.info("processResponse() {}", e);
    String p = e.getAsJsonObject().get("p").getAsString();
    log.debug("p {}", p);
    onResult(null);
  }

}
