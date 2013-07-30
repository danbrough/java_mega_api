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

    filesAdapter = new FilesAdapter(getActivity());
    fileView.setAdapter(filesAdapter);
    filesAdapter.setNotifyOnChange(false);

    fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        log.info("itemClicked: {}", filesAdapter.getItem(position));
        onNodeClicked(filesAdapter.getItem(position));
      }

    });

    fileView
        .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view,
              int position, long id) {
            onNodeLongClicked(filesAdapter.getItem(position));
            return true;
          }
        });

    refresh();
    return fileView;
  }

  protected MegaApplication getApplication() {
    return ((MegaApplication) getActivity().getApplication());
  }

  protected void onNodeClicked(Node node) {
    log.debug("onNodeClicked(): {}", node);
    if (node.getNodeType() == NodeType.FOLDERNODE) {
      getApplication().setFolder(node);
    }
  }

  public void setFolder(Node node) {
    log.info("setFolder(); {}", node);
    filesAdapter.setFolder(node);
  }

  protected void onNodeLongClicked(Node item) {
    log.debug("onNodeLongClicked(): {}", item);
  }

  public void refresh() {
    filesAdapter.refresh();
  }
}
