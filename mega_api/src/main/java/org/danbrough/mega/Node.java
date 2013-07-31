package org.danbrough.mega;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Node {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Node.class.getSimpleName());

  public enum ShareReadMode {
    ShareReadMode(0), SHAREOWNERKEY(1), OUTSHARE(2);

    private int value;

    private ShareReadMode(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static ShareReadMode get(int value) {
      for (ShareReadMode type : values())
        if (type.value == value)
          return type;
      return null;
    }
  }

  public enum NodeType {
    TYPE_UNKNOWN(-1), FILENODE(0), FOLDERNODE(1), ROOTNODE(2), INCOMINGNODE(3), RUBBISHNODE(
        4), MAILNODE(5);

    private int value;

    private NodeType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static NodeType get(int value) {
      for (NodeType type : values())
        if (type.value == value)
          return type;
      return null;
    }
  }

  public enum AccessLevel {
    TYPE_UNKNOWN(-1), FILENODE(0), FOLDERNODE(1), ROOTNODE(2), INCOMINGNODE(3), RUBBISHNODE(
        4), MAILNODE(5);

    private int value;

    private AccessLevel(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static AccessLevel get(int value) {
      for (AccessLevel level : values())
        if (level.value == value)
          return level;
      return null;
    }
  }

  @SerializedName("h")
  private String handle;

  @SerializedName("p")
  private String parent;

  @SerializedName("t")
  private NodeType nodeType;

  @SerializedName("u")
  private String user;

  @SerializedName("a")
  private String attributes;

  @SerializedName("k")
  private String key;

  @SerializedName("fa")
  private String fileAttributes;

  @SerializedName("s")
  private int size;

  @SerializedName("tm")
  private int lastModified;

  @SerializedName("ts")
  private int timeStamp;

  @SerializedName("r")
  AccessLevel accessLevel;

  @SerializedName("sk")
  private String sharingKey;

  @SerializedName("su")
  private String sharingUser;

  @SerializedName("name")
  private String name;

  private byte[] decodedKey = null;

  public Node() {
    super();
  }

  public String getName() {
    return name;
  }

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getParent() {
    return parent;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public void setNodeType(int value) {
    this.nodeType = NodeType.get(value);
  }

  public String getAttributes() {
    return attributes;
  }

  public void setAttributes(String attributes) {
    this.attributes = attributes;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public byte[] getDecodedKey() {
    return decodedKey;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setLastModified(int lastModified) {
    this.lastModified = lastModified;
  }

  public int getLastModified() {
    return lastModified;
  }

  public int getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(int timeStamp) {
    this.timeStamp = timeStamp;
  }

  public void setFileAttributes(String fileAttributes) {
    this.fileAttributes = fileAttributes;
  }

  public String getFileAttributes() {
    return fileAttributes;
  }

  public void setAccessLevel(int accessLevel) {
    this.accessLevel = AccessLevel.get(accessLevel);
  }

  public void setAccessLevel(AccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }

  public AccessLevel getAccessLevel() {
    return accessLevel;
  }

  public void setSharingKey(String sharingKey) {
    this.sharingKey = sharingKey;
  }

  public String getSharingKey() {
    return sharingKey;
  }

  public void setSharingUser(String sharingUser) {
    this.sharingUser = sharingUser;
  }

  public String getSharingUser() {
    return sharingUser;
  }

  @Override
  public String toString() {
    return GSONUtil.getGSON().toJson(this);
  }

  public void applyKey(MegaClient client) {
    log.info("applyKey() {} key: {}", handle, key);

    MegaCrypto crypto = MegaCrypto.get();
    User me = client.getMe();

    if (key == null) {
      log.error("key is null");
      return;
    }

    if (me == null) {
      log.error("ME IS NULL");
      return;
    }

    if (key.equals("")) {
      log.error("key is empty");
      return;
    }

    String k = null;
    String keys[] = key.split("/");
    for (String keyString : keys) {
      int i = keyString.indexOf(':');
      if (i == -1) {
        log.error("expecting a \":\" in the key [{}]", keyString);
        continue;
      }

      String handle = keyString.substring(0, i);
      k = keyString.substring(i + 1);

      byte bHandle[] = crypto.base64urldecode(handle);
      log.trace("handle: {} data: {}", handle, k);

      if (bHandle.length == 8) {
        log.debug("found user handle <{}>", handle);

        if (handle.equals(me.handle)) {
          // found key
          break;
        }
      }

      k = null;
    }

    if (k != null) {
      log.debug("decrypting attrs with key {}", k);
      byte master_key[] = client.getMasterKey();
      if (master_key == null) {
        log.error("No master key");
        return;
      }
      byte bkey[] = crypto.base64urldecode(k);
      bkey = crypto.decrypt_key(bkey, master_key);
      this.decodedKey = bkey;

      log.debug("bkey.length " + bkey.length);
      if (nodeType == NodeType.FILENODE) {
        byte new_key[] = new byte[16];
        for (int j = 0; j < new_key.length; j++) {
          new_key[j] = (byte) (bkey[j] ^ bkey[j + 16]);
        }
        bkey = new_key;
      }

      String json = crypto.decrypt_attrs(attributes, bkey);
      log.info("json: {}", json);
      if (!json.startsWith("MEGA")) {
        log.error("expecting MEGA prefix");
        return;
      }
      json = json.substring(4);
      JsonObject attrs = GSONUtil.getGSON().fromJson(json, JsonObject.class);

      if (attrs.has("n")) {
        this.name = attrs.get("n").getAsString();
      }

    }

  }
}
