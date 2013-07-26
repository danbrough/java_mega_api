/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class MegaApplication extends Application {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaApplication.class.getSimpleName());

  ThreadPool threadPool;
  MegaClient client;
  Handler handler;
  Thread uiThread;
  Activity activity;

  public MegaApplication() {
    super();
  }

  public SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(this);
  }

  AlertDialog busyDialog;

  public void showBusyDialog() {
    if (busyDialog != null)
      return;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setMessage(R.string.please_wait);
    builder.setCancelable(false);
    busyDialog = builder.create();

    busyDialog.show();
  }

  public void hideBusyDialog() {

    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (busyDialog == null)
          return;
        busyDialog.dismiss();
        busyDialog = null;
      }
    });
  }

  public void runOnUiThread(Runnable runnable) {
    if (Thread.currentThread() == uiThread)
      runnable.run();
    else
      handler.post(runnable);
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

            try {
              client.login(username, password, null);
            } catch (IOException e) {
              log.error(e.getMessage(), e);
            }

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
    displayError(getString(msgId));
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

  @Override
  public void onCreate() {
    log.info("onCreate();");
    super.onCreate();

    handler = new Handler();
    uiThread = Thread.currentThread();
    threadPool = new ExecutorThreadPool();
    threadPool.start();

    client = new MegaClient();
    client.setAppKey(getString(R.string.app_key));
    client.setThreadPool(threadPool);
    client.start();
  }

  @Override
  public void onTerminate() {
    log.info("onTerminate()");
    super.onTerminate();
    client.stop();
    client = null;
    threadPool.stop();
    threadPool = null;
  }

  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    this.activity = activity;
  }

  public void onActivityStarted(MegaActivity megaActivity) {

  }

  public void onActivityResumed(MegaActivity megaActivity) {

  }

  public void onActivityPaused(MegaActivity megaActivity) {

  }

  public void onActivityStopped(MegaActivity megaActivity) {

  }

  public void onActivitySaveInstanceState(MegaActivity megaActivity,
      Bundle outState) {

  }

  public void onActivityDestroyed(MegaActivity megaActivity) {

  }
}
