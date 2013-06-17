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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class StarterActivity extends Activity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(StarterActivity.class.getSimpleName());

  TextView txtStarterActivity;
  TestApplication testApp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    log.info("onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_starter);

    txtStarterActivity = (TextView) findViewById(R.id.txtStartActivity);
    txtStarterActivity.setText("Please Wait..");

    testApp = TestApplication.getInstance();

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        setupMega();
        return null;
      }
    }.execute();

  }

  private void setupMega() {
    log.debug("setupMega()");

    testApp.start(getApplicationContext());

    MegaAPI mega = testApp.getMega();
    final UserContext ctx = mega.getUserContext();
    log.debug("found context: {}", ctx);

    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        startActivity(new Intent(getApplicationContext(),
            ctx == null ? LoginActivity.class : TestActivity.class));
      }
    });

  }
}
