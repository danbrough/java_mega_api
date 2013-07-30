/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.megatest;

import java.io.IOException;
import java.util.HashMap;

import org.danbrough.mega.AccountDetails;
import org.danbrough.mega.Callback;
import org.danbrough.mega.MegaFragmentActivity;
import org.danbrough.mega.Node;
import org.danbrough.mega.Node.NodeType;
import org.danbrough.mega.test.R;
import org.danbrough.mega.ui.FilesFragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends MegaFragmentActivity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  TextView statusText = null;
  FilesFragment filesFragment;
  Thread uiThread = Thread.currentThread();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    statusText = (TextView) findViewById(R.id.txtStatus);
    filesFragment = (FilesFragment) getSupportFragmentManager()
        .findFragmentById(R.id.filesFragment);

  }

  @Override
  public void onAttachedToWindow() {
    log.error("onAttachedToWindow();");
    super.onAttachedToWindow();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private MenuItem _addMenu_v11(Menu menu, int id) {
    MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, id);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    return item;
  }

  private MenuItem addMenu(Menu menu, int id) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      return _addMenu_v11(menu, id);
    } else {
      return menu.add(Menu.NONE, id, Menu.NONE, id);
    }
  }

  @SuppressLint("UseSparseArrays")
  private final HashMap<Integer, MenuItem> actionMenuItems = new HashMap<Integer, MenuItem>();;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    log.info("onCreateOptionsMenu();");

    int actions[] = { R.string.login, R.string.label_updatefiles,
        R.string.label_whoami, R.string.logout };
    for (int action : actions) {
      actionMenuItems.put(action, addMenu(menu, action));
    }
    configureLayout();
    return super.onCreateOptionsMenu(menu);

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      onBackPressed();
      return true;
    case R.string.login:
      application.createLoginDialog().show();
      return true;
    case R.string.label_whoami:
      whoami();
      return true;
    case R.string.label_updatefiles:
      updateFiles();
      return true;
    case R.string.logout:
      application.logout();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    backPressedCount = 0;
  }

  private void configureLayout() {
    log.debug("configureLayout()");
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        boolean loggedIn = application.isLoggedIn();

        actionMenuItems.get(R.string.login).setVisible(!loggedIn);
        actionMenuItems.get(R.string.logout).setVisible(loggedIn);

        StringBuffer status = new StringBuffer();
        if (loggedIn) {
          status.append("Email: " + application.getClient().getEmail() + "\n");
          status.append("SessionID: " + application.getClient().getSessionID()
              + "\n");
        } else {
          status.append("Not logged in.");
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

  @Override
  public void onLogout() {
    log.debug("onLogout();");
    configureLayout();
    filesFragment.refresh();
  }

  @Override
  public void onLogin() {
    log.debug("onLogin();");
    configureLayout();
  }

  @Override
  public void onNodesModified() {
    log.warn("onNodesModified();");
    filesFragment.refresh();
  }

  private boolean firstUpdate = false;

  @Override
  public void onFolderChanged(final Node folder) {
    log.info("onFolderChanged(): {}", folder);

    if (firstUpdate) {
      firstUpdate = false;
      return;
    }

    runOnUiThread(new Runnable() {

      @Override
      public void run() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          _onFolderChanged_v11(folder);
        }
        filesFragment.setFolder(folder);
      }
    });

  }

  @SuppressLint("InlinedApi")
  private static final int ACTION_BAR_OPTIONS = ActionBar.DISPLAY_USE_LOGO
      | ActionBar.DISPLAY_SHOW_HOME;

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void _onFolderChanged_v11(final Node folder) {
    if (folder.getNodeType() == NodeType.ROOTNODE) {
      getActionBar().setDisplayOptions(ACTION_BAR_OPTIONS);
    } else {
      getActionBar().setDisplayOptions(
          ActionBar.DISPLAY_HOME_AS_UP | ACTION_BAR_OPTIONS);
    }
  }

  int backPressedCount = 0;

  @Override
  public void onBackPressed() {
    log.debug("onBackPressed()");

    Node node = application.getClient().getCurrentFolder();

    if (node != null && node.getNodeType() != NodeType.ROOTNODE) {
      Node parent = application.getClient().getNode(node.getParent());
      if (parent != null) {
        application.setFolder(parent);
        return;
      }
    }

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
      dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),
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

  public void whoami() {
    log.info("whoami();");
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

  public void updateFiles() {
    log.info("updateFiles();");
    try {
      application.getClient().fetchNodes(new Callback<Void>() {
        @Override
        public void onResult(Void result) {
          log.info("got files");

        }
      });
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
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
