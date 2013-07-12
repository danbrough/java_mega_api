/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.danbrough.mega.protocol.LoginRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonParser;

public class MegaApplication {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaApplication.class.getSimpleName());

  private static final String PREF_USER_CONTEXT = "userContext";

  private MegaAPI megaAPI;

  AlertDialog busyDialog;
  Activity activity;
  private final Context appContext;

  public MegaApplication(Activity activity) {
    super();

    this.activity = activity;
    this.appContext = activity.getApplicationContext();

    showBusyDialog();

    megaAPI = new MegaAPI();

    new Thread() {
      @Override
      public void run() {
        try {
          initialize();
        } finally {
          hideBusyDialog();
        }
      }
    }.start();
  }

  private void initialize() {
    megaAPI.start();
    if (getPrefs().contains(PREF_USER_CONTEXT)) {
      megaAPI.createUserContext(new JsonParser().parse(
          getPrefs().getString(PREF_USER_CONTEXT, null)).getAsJsonObject());
    }
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
    if (activity instanceof MegaListener)
      megaAPI.setListener((MegaListener) activity);
  }

  public SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(appContext);
  }

  public void saveUserContext() {
    log.debug("saveUserContext()");
    UserContext ctx = megaAPI.getUserContext();
    String s = Crypto.getInstance().toJSON(ctx).toString();
    getPrefs().edit().putString(PREF_USER_CONTEXT, s).commit();
  }

  public void login(final String username, final String password) {
    log.info("login() {}", username);

    showBusyDialog();

    megaAPI.getThreadPool().background(new Runnable() {

      @Override
      public void run() {
        try {

          new LoginRequest(megaAPI, username, password) {
            public void onError(Exception exception) {
              super.onError(exception);
              hideBusyDialog();
            }

            public void onResponse(com.google.gson.JsonElement response) {
              super.onResponse(response);
              hideBusyDialog();
            }

          }.send();
        } catch (Exception e) {
          displayError(e);
        }
      }
    });

  }

  public void logout() {
    log.info("logout()");
    getPrefs().edit().remove(PREF_USER_CONTEXT).commit();
    megaAPI.logout();
  }

  public void stop() {
    if (megaAPI == null)
      return;
    megaAPI.stop();
  }

  public MegaAPI getMega() {
    return megaAPI;
  }

  public void showBusyDialog() {
    if (busyDialog != null)
      return;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setMessage(R.string.please_wait);
    builder.setCancelable(false);
    busyDialog = builder.create();

    busyDialog.show();
  }

  public void hideBusyDialog() {
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (busyDialog == null)
          return;
        busyDialog.dismiss();
        busyDialog = null;
      }
    });
  }

  public AlertDialog createLoginDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setTitle(R.string.app_name);
    builder.setCancelable(false);

    View view = activity.getLayoutInflater().inflate(R.layout.dialog_login,
        null);

    final TextView txtUsername = (TextView) view.findViewById(R.id.username);
    final TextView txtPassword = (TextView) view.findViewById(R.id.password);

    builder.setView(view);
    builder.setPositiveButton(R.string.login,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();

            if (username.equals("")
                || !Crypto.getInstance().isValidEmailAddress(username)) {
              displayError(R.string.msg_invalid_username);
              return;
            }

            if (password.equals("")) {
              displayError(R.string.msg_invalid_password);
              return;
            }

            login(username, password);
          }
        });
    builder.setNegativeButton(R.string.cancel,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    return builder.create();
  }

  public void displayError(Exception e) {
    log.error(e.getMessage(), e);
    displayError(e.getMessage());
  }

  public void displayError(int msgId) {
    displayError(appContext.getString(msgId));
  }

  public void displayError(final String msg) {
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        hideBusyDialog();
        createErrorDialog(msg).show();
      }
    });
  }

  public AlertDialog createErrorDialog(String msg) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity).setIcon(
        R.drawable.ic_launcher).setMessage(msg);
    builder.setPositiveButton(R.string.ok,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    builder.setTitle(R.string.msg_an_error_occurred);

    return builder.create();
  }
}
