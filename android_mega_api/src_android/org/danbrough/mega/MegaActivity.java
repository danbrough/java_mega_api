/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import android.app.Activity;
import android.os.Bundle;

import com.google.gson.JsonElement;

public class MegaActivity extends Activity implements MegaListener {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaActivity.class.getSimpleName());

  protected MegaApplication application;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (application == null) {
      application = new MegaApplication(this);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    application.setActivity(this);
  }

  public void onFilesModified(JsonElement o) {
    log.info("onFilesModified() :{}", o);
  }

  @Override
  public void onLoggedOut() {
  }
}
