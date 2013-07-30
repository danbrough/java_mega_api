/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.danbrough.mega.MegaApplication;
import org.danbrough.mega.MegaClient;
import org.danbrough.mega.Node;
import org.danbrough.mega.Node.NodeType;
import org.danbrough.mega.R;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<Node> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(FilesAdapter.class.getSimpleName());

  Activity activity;
  Node folder;

  public FilesAdapter(Activity activity) {
    super(activity, R.layout.node_list_item);
    this.activity = activity;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;

    if (view == null) {
      view = activity.getLayoutInflater().inflate(R.layout.node_list_item,
          parent, false);
    }

    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
    ImageView icon = (ImageView) view.findViewById(android.R.id.icon);

    Node node = getItem(position);
    text1.setText(node.getName() != null ? node.getName() : node.getHandle());
    icon.setImageResource(node.getNodeType() == NodeType.FOLDERNODE ? R.drawable.folder
        : R.drawable.binary);

    return view;
  }

  protected MegaApplication getApplication() {
    return ((MegaApplication) getContext().getApplicationContext());
  }

  protected void refresh() {
    log.debug("refresh();");
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        clear();

        MegaClient client = getApplication().getClient();

        if (!client.isLoggedIn()) {
          log.trace("client not logged in");
          notifyDataSetChanged();
          return;
        }

        if (client.getRootNode() == null) {
          log.trace("root node is null");
          notifyDataSetChanged();
          return;
        }

        if (folder == null)
          folder = client.getRootNode();

        List<Node> children = client.getChildren(folder);
        Collections.sort(children, new Comparator<Node>() {
          @Override
          public int compare(Node lhs, Node rhs) {
            String lname = lhs.getName();
            if (lname == null)
              return -1;
            return lname.compareToIgnoreCase(rhs.getName());
          }
        });
        for (Node node : children)
          add(node);
        notifyDataSetChanged();
      }
    });
  }

  public void setFolder(Node folder) {
    this.folder = folder;
    refresh();
  }

}
