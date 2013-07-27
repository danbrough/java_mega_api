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
import android.widget.TextView;

public class MainActivity extends MegaActivity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  TextView statusText = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    statusText = (TextView) findViewById(R.id.txtStatus);
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

        findViewById(R.id.btnLogin).setVisibility(
            loggedIn ? View.GONE : View.VISIBLE);
        findViewById(R.id.btnLogout).setVisibility(
            loggedIn ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnWhoAmI).setVisibility(
            loggedIn ? View.VISIBLE : View.GONE);

        StringBuffer status = new StringBuffer();
        if (loggedIn) {
          status.append("email: " + application.getClient().getEmail() + "\n");
          status.append("sessionID: " + application.getClient().getSessionID()
              + "\n");
        } else {
          status.append("not logged in");
        }
        setStatus(status);
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
      dialog.setCanceledOnTouchOutside(true);
      dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
          backPressedCount = 0;
        }
      });
      dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
          new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.cancel();
              backPressedCount = 0;
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
          public void onResult(final AccountDetails details) {
            log.info("onResult(): details:{}", details.toString());
            runOnUiThread(new Runnable() {
              public void run() {
                application
                    .createAlertDialog()
                    .setMessage(details.toString())
                    .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                          }
                        }).show();
              }
            });
          }
        });
  }

  public void setStatus(final CharSequence msg) {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        statusText.setText(msg);
      }
    });
  }
}
