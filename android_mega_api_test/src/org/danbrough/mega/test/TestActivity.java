/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.test;

import org.danbrough.mega.GetFilesRequest;
import org.danbrough.mega.MegaAPI;
import org.danbrough.mega.PollRequest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonElement;

public class TestActivity extends Activity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(TestActivity.class.getSimpleName());

  private static final int MENU_LOGOUT = 10001;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    log.info("onCreate()");
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_test);

    ((Button) findViewById(R.id.btnGetFiles))
        .setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            getUserFiles();
          }
        });
    ((Button) findViewById(R.id.btnLogout))
        .setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            logout();
          }
        });
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private boolean onCreateOptionsMenu_11(Menu menu) {
    menu.add(Menu.NONE, MENU_LOGOUT, Menu.NONE, "Logout").setShowAsAction(
        MenuItem.SHOW_AS_ACTION_IF_ROOM);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      return onCreateOptionsMenu_11(menu);
    }
    menu.add(Menu.NONE, MENU_LOGOUT, Menu.NONE, "Logout");
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_LOGOUT:
      logout();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  void logout() {
    log.info("logout()");
    TestApplication.getInstance().logout(this);
  }

  private void getUserFiles() {
    log.debug("getUserFiles()");
    MegaAPI mega = TestApplication.getInstance().getMega();
    new GetFilesRequest(mega) {
      @Override
      public void onResponse(JsonElement obj) {
        super.onResponse(obj);
      }
    }.send();
  }

  public void startPoll(View view) {
    log.info("startPoll()");
    new PollRequest(TestApplication.getInstance().getMega()).send();
  }
}
