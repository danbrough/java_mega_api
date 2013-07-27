package org.danbrough.mega;

import java.util.HashMap;
import java.util.Map;

import org.danbrough.mega.Node.ShareReadMode;
import org.danbrough.mega.User.Visibility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CommandFetchNodes extends Command<Void> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CommandFetchNodes.class.getSimpleName());

  MegaClient client;
  Node rootNode, incomingNode, mailNode, rubbishNode;
  Gson gson = GSONUtil.getGSON();
  HashMap<String, Node> nodes;
  HashMap<String, User> users;
  String scsn;
  User me;

  public CommandFetchNodes(MegaClient client, Callback<Void> callback) {
    super("f", callback);
    this.client = client;
    addArg("c", 1);
    addArg("r", 1);
  }

  public Map<String, Node> readnodes(JsonArray nodes) throws Exception {
    Map<String, Node> nodeMap = new HashMap<String, Node>();
    for (int i = 0; i < nodes.size(); i++) {
      readNode(nodes.get(i).getAsJsonObject(), nodeMap);
    }
    return nodeMap;
  }

  public void readNode(JsonObject o, Map<String, Node> nodeMap)
      throws Exception {
    log.debug("readNode() {}", o);

    Node node = gson.fromJson(o, Node.class);

    switch (node.getNodeType()) {
    case TYPE_UNKNOWN:
      log.error("Unknown node type for {}", node);
      break;
    case FILENODE:
      if (node.getSize() == 0) {
        log.error("File node {} has size 0", node);
      }
    case FOLDERNODE:
      if (node.getParent() == null) {
        log.error("Missing parent for {}", node);
      }
      if (node.getAttributes() == null) {
        log.error("Missing attributes for {}", node);
      }
      if (node.getKey() == null) {
        log.error("Missing key for {}", node);
      }
      break;
    case INCOMINGNODE:
      incomingNode = node;
      break;
    case MAILNODE:
      mailNode = node;
      break;
    case ROOTNODE:
      rootNode = node;
      break;
    case RUBBISHNODE:
      rubbishNode = node;
      break;
    }

    if (node.getHandle() == null) {
      log.error("node {} has no handle", node);
      return;
    }

    nodes.put(node.getHandle(), node);

  }

  protected void readUsers(JsonArray a) throws Exception {
    log.info("readUsers();");
    for (int i = 0; i < a.size(); i++) {
      JsonObject o = a.get(i).getAsJsonObject();
      User user = gson.fromJson(o, User.class);
      users.put(user.getHandle(), user);
      log.debug("readUser() {} as {}", o, gson.toJson(user));
      if (user.getVisibility() == Visibility.ME)
        me = user;
    }
  }

  protected void readShares(JsonArray a, ShareReadMode readMode)
      throws Exception {
    log.info("readShares() mode: {}", readMode);
    for (int i = 0; i < a.size(); i++) {
      JsonObject o = a.get(i).getAsJsonObject();
      log.debug("readShares() {} ", o);
    }
  }

  @Override
  public void processResponse(JsonElement e) throws Exception {
    log.trace("processResponse() {}", e);
    // clear everything in case this is a reload
    // client->purgenodes();
    // client->purgeusers();

    nodes = new HashMap<String, Node>();
    users = new HashMap<String, User>();

    JsonObject o = e.getAsJsonObject();
    if (o.has("f")) {
      readnodes(o.get("f").getAsJsonArray());
    } else {
      throw new Exception("expecing \"f\"");
    }
    if (o.has("ok")) {
      readShares(o.get("ok").getAsJsonArray(), ShareReadMode.SHAREOWNERKEY);
    }

    if (o.has("u")) {
      readUsers(o.get("u").getAsJsonArray());
    } else {
      throw new Exception("expecing \"u\"");
    }

    if (o.has("cr")) {
      log.error("NOT IMPLEMENTED: CR: {}", o.get("cr"));
    }

    if (o.has("s")) {
      readShares(o.get("s").getAsJsonArray(), ShareReadMode.OUTSHARE);
    }

    if (o.has("sr")) {
      log.error("NOT IMPLEMENTED SR: {}", o.get("sr"));
    }

    if (o.has("sn")) {
      scsn = o.get("sn").getAsString();
    }

    updateClient();

    onResult(null);

  }

  protected void updateClient() {
    log.debug("updateClient();");

    synchronized (client) {
      client.clearNodes();
      client.setScsn(scsn);
      client.setRootNode(rootNode);
      client.setCurrentFolder(rootNode);
      client.setMailNode(mailNode);
      client.setIncomingNode(incomingNode);
      client.setRubbishNode(rubbishNode);
      client.setNodes(nodes);
      client.setUsers(users);
      client.setMe(me);

      client.setEmail(me.getEmail());
      client.applyKeys();
    }

    nodes = null;
    users = null;
  }
}
