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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonElement;

public class TestActivity extends Activity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(TestActivity.class.getSimpleName());

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
}
