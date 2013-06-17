/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.test;

import org.danbrough.mega.MegaAPI;
import org.danbrough.mega.UserContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.JsonParser;

public class TestApplication {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(TestApplication.class.getSimpleName());

  private Context appContext;
  MegaAPI mega;
  private static TestApplication INSTANCE;

  public TestApplication() {
    super();
    INSTANCE = this;
  }

  public void start(Context context) {
    this.appContext = context;
    if (mega != null)
      return;
    mega = new MegaAPI();
    mega.start();

    if (getPrefs().contains("userContext")) {
      mega.createUserContext(new JsonParser().parse(
          getPrefs().getString("userContext", null)).getAsJsonObject());
    }

  }

  public SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(appContext);
  }

  public void saveUserContext() {
    UserContext ctx = mega.getUserContext();

    String s = ctx.toJSON().toString();
    log.debug("saving userContext: {}", s);
    getPrefs().edit().putString("userContext", s).commit();
  }

  public void stop() {
    if (mega == null)
      return;
    mega.stop();
  }

  public MegaAPI getMega() {
    return mega;
  }

  public static TestApplication getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TestApplication();
    }

    return INSTANCE;
  }
}
