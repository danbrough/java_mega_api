/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.megatest;

import org.danbrough.mega.AccountDetails;
import org.danbrough.mega.Callback;
import org.danbrough.mega.MegaActivity;
import org.danbrough.mega.test.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
  protected void onResume() {
    super.onResume();
    backPressedCount = 0;
    configureLayout();
  }

  private void configureLayout() {
    log.debug("configureLayout()");
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        boolean loggedIn = application.isLoggedIn();
        View btnLogin = findViewById(R.id.btnLogin);
        View btnLogout = findViewById(R.id.btnLogout);
        btnLogin.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        btnLogout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
      }
    });
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
    application.logout();
  }

  @Override
  protected void onLogout() {
    log.debug("onLogout();");
    configureLayout();
  }

  @Override
  protected void onLogin() {
    log.debug("onLogin();");
    configureLayout();
  }

  int backPressedCount = 0;

  @Override
  public void onBackPressed() {
    log.debug("onBackPressed()");
    backPressedCount++;
    if (backPressedCount == 1) {
      AlertDialog dialog = new AlertDialog(this, false, null) {
        @Override
        public void onBackPressed() {
          super.onBackPressed();
          MainActivity.this.finish();
        }
      };

      dialog.setMessage("Press back again if you want to quit.");
      dialog.setCancelable(true);

      dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
          new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              backPressedCount = 0;
              dialog.dismiss();
            }
          });
      dialog.show();
    }
  }

  public void cmdWhoAmi(View view) {
    log.info("cmdWhoAmi();");
    application.getClient().getAccountDetails(true, true, true, true, true,
        false, new Callback<AccountDetails>() {
          @Override
          public void onResult(final AccountDetails result) {
            log.warn("got result: {}", result.toString());
            runOnUiThread(new Runnable() {
              public void run() {
                application.createAlertDialog().setMessage(result.toString())
                    .show();
              }
            });
          }
        });

  }
}
