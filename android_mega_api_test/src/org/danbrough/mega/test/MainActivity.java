/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.test;

import org.danbrough.mega.MegaActivity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends MegaActivity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  private static final int MENU_LOGOUT = 10001;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    log.info("onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onStart() {
    log.debug("onStart()");
    super.onStart();
  }

  @Override
  protected void onResume() {
    log.debug("onResume()");
    super.onResume();

    configureLayout();
  }

  private void configureLayout() {
    log.debug("configureLayout()");
    boolean loggedIn = application.getMega().isLoggedIn();
    log.trace("loggedIn: " + loggedIn);
    View btnLogin = findViewById(R.id.btnLogin);
    View btnLogout = findViewById(R.id.btnLogout);
    btnLogin.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    btnLogout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
  }

  @Override
  protected void onRestart() {
    log.debug("onRestart()");
    super.onRestart();
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
      logout(null);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void login(View view) {
    log.info("login()");
    application.createLoginDialog().show();
  }

  public void logout(View view) {
    log.info("logout()");
    application.logout();
  }

  @Override
  public void onLoggedOut() {
    log.debug("onLoggedOut()");
    configureLayout();
  }

}
