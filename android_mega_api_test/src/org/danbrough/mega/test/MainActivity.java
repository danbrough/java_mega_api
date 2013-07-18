/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.test;

import org.danbrough.mega.MegaActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends MegaActivity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    configureLayout();
  }

  private void configureLayout() {
    log.debug("configureLayout()");
    boolean loggedIn = false;
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

  public void login(View view) {
    log.info("login()");
    application.createLoginDialog().show();

  }

  public void logout(View view) {
    log.info("logout()");
  }

}
