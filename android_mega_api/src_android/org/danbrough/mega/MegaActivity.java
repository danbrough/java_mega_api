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

public class MegaActivity extends Activity implements MegaListener {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaActivity.class.getSimpleName());

  protected MegaApplication application;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    log.trace("onCreate() {}", savedInstanceState);
    super.onCreate(savedInstanceState);
    application = (MegaApplication) getApplication();
    application.onActivityCreated(this, savedInstanceState);
  }

  @Override
  protected void onStart() {
    log.trace("onStart()");
    super.onStart();
    application.onActivityStarted(this);
  }

  @Override
  protected void onResume() {
    log.trace("onResume()");
    super.onResume();
    application.onActivityResumed(this);
  }

  @Override
  protected void onPause() {
    log.trace("onPause()");
    super.onPause();
    application.onActivityPaused(this);
  }

  @Override
  protected void onStop() {
    log.trace("onStop()");
    super.onStop();
    application.onActivityStopped(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    log.trace("onSaveInstanceState() {}", outState);
    super.onSaveInstanceState(outState);
    application.onActivitySaveInstanceState(this, outState);
  }

  @Override
  protected void onDestroy() {
    log.trace("onDestroy()");
    super.onDestroy();
    application.onActivityDestroyed(this);
  }

  @Override
  public void onLogout() {
    log.info("onLogout();");
  }

  @Override
  public void onLogin() {
    log.info("onLogin();");
  }

  @Override
  public void onNodesModified() {
    log.info("onFilesModified();");
  }

  @Override
  public void onFolderChanged(Node folder) {
    log.info("onFolderChanged();");
  }

}
