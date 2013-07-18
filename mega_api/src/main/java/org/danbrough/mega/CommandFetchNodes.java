package org.danbrough.mega;

import java.util.HashMap;
import java.util.Map;

import org.danbrough.mega.Node.ShareReadMode;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CommandFetchNodes extends Command {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CommandFetchNodes.class.getSimpleName());

  MegaClient client;
  Node rootNode;
  Gson gson = GSONUtil.getGSON();

  public CommandFetchNodes(MegaClient client) {
    super("f");
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

    if (rootNode == null) {
      rootNode = node;
      log.debug("rootNode() {}", o);
    }
  }

  protected void readUsers(JsonArray a) throws Exception {

    for (int i = 0; i < a.size(); i++) {
      JsonObject o = a.get(i).getAsJsonObject();

      User user = gson.fromJson(o, User.class);
      log.debug("readUser() {} as {}", o, gson.toJson(user));
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

    JsonObject o = e.getAsJsonObject();
    if (o.has("f")) {
      readnodes(o.get("f").getAsJsonArray());
    } else {
      throw new Exception("expecing \"f\"");
    }

    if (o.has("u")) {
      readUsers(o.get("u").getAsJsonArray());
    } else {
      throw new Exception("expecing \"u\"");
    }

    if (o.has("s")) {
      readShares(o.get("s").getAsJsonArray(), ShareReadMode.OUTSHARE);
    }

    if (o.has("ok")) {
      readShares(o.get("ok").getAsJsonArray(), ShareReadMode.SHAREOWNERKEY);
    }

    if (o.has("cr")) {
      log.error("FOUND A CR: {}", o.get("cr"));
    }

    if (o.has("sr")) {
      log.error("FOUND A SR: {}", o.get("sr"));
    }

    if (o.has("sn")) {
      log.error("FOUND A SN: {}", o.get("sn"));
      client.setScsn(o.get("sn").getAsString());
    }

  }
}
