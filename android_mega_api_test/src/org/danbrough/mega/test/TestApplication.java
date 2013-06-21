/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.test;

import org.danbrough.mega.Crypto;
import org.danbrough.mega.MegaAPI;
import org.danbrough.mega.UserContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.JsonParser;

public class TestApplication {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(TestApplication.class.getSimpleName());

  private Context appContext;
  MegaAPI mega;
  private static TestApplication INSTANCE;
  private static final String PREF_USER_CONTEXT = "userContext";

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

    if (getPrefs().contains(PREF_USER_CONTEXT)) {
      mega.createUserContext(new JsonParser().parse(
          getPrefs().getString(PREF_USER_CONTEXT, null)).getAsJsonObject());
    }

  }

  public SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(appContext);
  }

  public void saveUserContext() {
    UserContext ctx = mega.getUserContext();
    String s = Crypto.getInstance().toJSON(ctx).toString();
    log.debug("saving userContext: {}", s);
    getPrefs().edit().putString(PREF_USER_CONTEXT, s).commit();
  }

  public void logout(TestActivity activity) {
    getPrefs().edit().remove(PREF_USER_CONTEXT).commit();
    activity.startActivity(new Intent(appContext, LoginActivity.class));
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
