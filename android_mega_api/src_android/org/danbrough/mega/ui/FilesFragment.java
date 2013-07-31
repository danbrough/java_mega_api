/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.ui;

import org.danbrough.mega.MegaApplication;
import org.danbrough.mega.Node;
import org.danbrough.mega.Node.NodeType;
import org.danbrough.mega.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class FilesFragment extends android.support.v4.app.Fragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(FilesFragment.class.getSimpleName());
  ListView fileView;
  FilesAdapter filesAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    log.info("onCreateView()");
    fileView = new ListView(getActivity());
    registerForContextMenu(fileView);

    filesAdapter = new FilesAdapter(getActivity());
    fileView.setAdapter(filesAdapter);
    filesAdapter.setNotifyOnChange(false);

    fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        log.info("itemClicked: {}", filesAdapter.getItem(position));
        onNodeClicked(view, filesAdapter.getItem(position));
      }

    });

    fileView
        .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view,
              int position, long id) {
            onNodeLongClicked(view, filesAdapter.getItem(position));
            return true;
          }
        });

    refresh();
    return fileView;
  }

  protected MegaApplication getApplication() {
    return ((MegaApplication) getActivity().getApplication());
  }

  protected void onNodeClicked(View view, Node node) {
    log.debug("onNodeClicked(): {}", node);
    if (node.getNodeType() == NodeType.FOLDERNODE) {
      getApplication().setFolder(node);
    }
  }

  protected AlertDialog.Builder createEditNodeDialog(final Node item) {
    AlertDialog.Builder builder = getApplication().createAlertDialog();
    builder.setTitle(item.getName());
    final String actions[] = { getString(R.string.download),
        getString(R.string.delete) };
    builder.setItems(actions, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        log.debug("which: " + which);
        if (actions[which].equals(getString(R.string.download))) {
          getApplication().download(item);
        }
      }
    });
    builder.setNegativeButton(R.string.cancel,
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
    return builder;
  }

  protected void onNodeLongClicked(View view, Node item) {
    log.debug("onNodeLongClicked(): {}", item);
    createEditNodeDialog(item).show();
  }

  public void setFolder(Node node) {
    log.info("setFolder(); {}", node);
    filesAdapter.setFolder(node);
  }

  public void refresh() {
    filesAdapter.refresh();
  }
}
