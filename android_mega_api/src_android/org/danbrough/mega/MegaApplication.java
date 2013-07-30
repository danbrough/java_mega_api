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
import android.app.ProgressDialog;
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

  private static final String PREF_SESSION = "session";

  protected AndroidClient client;
  Handler handler;
  protected Thread uiThread;
  protected Activity activity;
  protected MegaListener listener;
  protected ThreadPool threadPool;

  public MegaApplication() {
    super();
  }

  public SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(this);
  }

  AlertDialog busyDialog;

  public void showBusyDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (busyDialog != null)
          return;

        ProgressDialog dialog = new ProgressDialog(activity) {
          int backPressCount = 0;

          @Override
          public void onBackPressed() {
            backPressCount++;
            if (backPressCount > 1) {
              dismiss();
              activity.finish();
            }
          }
        };
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        // dialog.setOnCancelListener(cancelListener);
        dialog.show();
        busyDialog = dialog;
        busyDialog.show();
      }
    });
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

  public AlertDialog.Builder createAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setIcon(R.drawable.ic_launcher);
    builder.setTitle(R.string.app_name);
    return builder;
  }

  public AlertDialog.Builder createLoginDialog() {
    AlertDialog.Builder builder = createAlertDialog();
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

            showBusyDialog();
            try {
              client.login(username, password, new Callback<Void>() {
                @Override
                public void onError(APIError error) {
                  displayError(error);
                }

                @Override
                public void onError(Exception e) {
                  displayError(e);
                }

                @Override
                public void onResult(Void result) {
                  hideBusyDialog();
                  onLogin();
                }
              });
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
    return builder;
  }

  public void logout() {
    log.info("logout();");
    if (client != null) {
      client.stop();
      client = null;
    }
    getPrefs().edit().remove(PREF_SESSION).commit();
    startClient();
    onLogout();
  }

  public void displayError(Throwable e) {
    log.error(e.getMessage(), e);
    displayError(e.getMessage());
  }

  public void displayError(int msgId) {
    displayError(getString(msgId));
  }

  public void displayError(APIError error) {
    displayError(error.getMessage());
  }

  public void displayError(final String msg) {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        hideBusyDialog();
        createErrorDialog(msg).show();
      }
    });
  }

  public AlertDialog.Builder createErrorDialog(String msg) {
    AlertDialog.Builder builder = createAlertDialog().setMessage(msg);
    builder.setPositiveButton(R.string.ok,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    builder.setTitle(R.string.msg_an_error_occurred);
    return builder;
  }

  @Override
  public void onCreate() {
    log.info("onCreate();");
    super.onCreate();

    handler = new Handler();
    uiThread = Thread.currentThread();
    threadPool = createThreadPool();

    try {
      String json = getPrefs().getString(PREF_SESSION, null);
      if (json != null) {
        client = GSONUtil.getGSON().fromJson(
            getPrefs().getString(PREF_SESSION, null), AndroidClient.class);
      }

    } catch (Throwable ex) {
      log.error("failed to restore session", ex);
    }

    startClient();
  }

  protected void startClient() {
    if (client == null)
      client = new AndroidClient();
    client.setApplication(this);
    client.setAppKey(getString(R.string.app_key));
    client.setThreadPool(threadPool);
    client.start();
  }

  protected ThreadPool createThreadPool() {
    threadPool = new ExecutorThreadPool();
    threadPool.start();
    return threadPool;
  }

  protected void destroyThreadPool() {
    if (threadPool == null)
      return;
    threadPool.stop();
    threadPool = null;
  }

  @Override
  public void onTerminate() {
    log.info("onTerminate()");
    super.onTerminate();

    if (client != null) {
      client.stop();
      client = null;
    }

    destroyThreadPool();
  }

  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    this.activity = activity;
    if (activity instanceof MegaListener)
      listener = (MegaListener) activity;
  }

  public void onActivityStarted(Activity activity) {

  }

  public void onActivityResumed(Activity activity) {
    log.info("onActivityResumed()");
    client.startSC();
  }

  public void onActivityPaused(Activity activity) {
    log.info("onActivityPaused()");
    client.stopSC();
  }

  public void onActivityStopped(Activity activity) {

  }

  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    log.info("onActivitySaveInstanceState() {}", activity);
    saveSession(null);
  }

  public void onActivityDestroyed(Activity activity) {
  }

  public boolean isLoggedIn() {
    return client != null && client.getSessionID() != null;
  }

  protected void onLogin() {
    if (listener != null)
      listener.onLogin();
    saveSession(null);
  }

  protected void onLogout() {
    if (listener != null)
      listener.onLogout();
  }

  public void onFolderChanged(Node folder) {
    if (listener != null)
      listener.onFolderChanged(folder);

  }

  protected void onNodesModified() {
    saveSession(new Callback<Void>() {
      @Override
      public void onResult(Void result) {
        if (listener != null)
          listener.onNodesModified();
      }
    });

  }

  public void runInBackground(Runnable runnable) {
    threadPool.background(runnable);
  }

  public void saveSession(final Callback<Void> callback) {
    runInBackground(new Runnable() {

      @Override
      public void run() {
        try {
          String json = null;
          synchronized (client) {
            json = GSONUtil.getGSON().toJson(client);
          }
          getPrefs().edit().putString(PREF_SESSION, json).commit();
        } catch (Exception ex) {
          displayError(ex);
        } finally {
          if (callback != null)
            callback.onResult(null);
        }
      }
    });
  }

  public AndroidClient getClient() {
    return client;
  }

  public void setFolder(Node node) {
    log.info("setFolder(): {}", node);
    client.setCurrentFolder(node);
  }

}
