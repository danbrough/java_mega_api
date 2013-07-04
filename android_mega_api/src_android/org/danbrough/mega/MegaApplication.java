/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

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

  Activity activity;
  private final Context appContext;

  public MegaApplication(Activity activity) {
    super();

    this.activity = activity;
    this.appContext = activity.getApplicationContext();

    final AlertDialog dialog = createBusyDialog();
    dialog.show();

    megaAPI = new MegaAPI();

    new Thread() {
      @Override
      public void run() {
        initialize();
        dialog.cancel();
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
    UserContext ctx = megaAPI.getUserContext();
    String s = Crypto.getInstance().toJSON(ctx).toString();
    log.debug("saving userContext: {}", s);
    getPrefs().edit().putString(PREF_USER_CONTEXT, s).commit();
  }

  public void login(String username, String password) {
    log.info("login() {}:{}", username, password);
    displayError(new Exception("I like cheese"));
    // try {
    // new LoginRequest(megaAPI) {
    //
    // }.send();
    // } catch (Exception e) {
    // createErrorDialog(e).show();
    // }
  }

  public void logout() {
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

  public AlertDialog createBusyDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setMessage(R.string.please_wait);
    AlertDialog dialog = builder.create();
    return dialog;
  }

  public AlertDialog createLoginDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setTitle(R.string.app_name);
    View view = activity.getLayoutInflater().inflate(R.layout.dialog_login,
        null);

    final TextView username = (TextView) view.findViewById(R.id.username);
    final TextView password = (TextView) view.findViewById(R.id.password);

    builder.setView(view);
    builder.setPositiveButton(R.string.login,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            login(username.getText().toString(), password.getText().toString());
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
    createErrorDialog(e).show();
  }

  public AlertDialog createErrorDialog(Exception e) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity).setIcon(
        R.drawable.ic_launcher).setMessage(e.getMessage());
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    builder.setTitle(R.string.an_error_occurred);
    return builder.create();
  }
}
