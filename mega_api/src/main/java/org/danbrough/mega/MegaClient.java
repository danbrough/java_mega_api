package org.danbrough.mega;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class MegaClient {
  private transient static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaClient.class.getSimpleName());

  static final String API_URL;
  static final String USER_AGENT;

  static {
    ResourceBundle properties = ResourceBundle.getBundle(MegaClient.class
        .getName());
    API_URL = properties.getString("apiPath");
    USER_AGENT = properties.getString("userAgent");
  }

  transient MegaCrypto crypto = MegaCrypto.get();

  Node currentFolder = null;

  @SuppressWarnings("rawtypes")
  transient LinkedList<Command> cmdQueue = new LinkedList<Command>();

  byte passwordKey[];
  byte masterKey[];
  BigInteger[] privateKey;

  String email;

  User me;

  // no two interrelated client instances should ever have the same sessionid
  @SerializedName("sid")
  String sessionID;

  // request id
  char[] reqid;

  transient String appkey = "MegaJavaTest";

  transient boolean running = false;

  transient ThreadPool threadPool;

  // back off time for server client requests in millis
  transient long backoffsc = 100;

  transient String scnotifyurl = null;

  transient boolean startedSC = false;

  // server-client request sequence number
  String scsn;

  Node rootNode, incomingNode, rubbishNode, mailNode;
  Map<String, Node> nodes;
  HashMap<String, User> users;

  transient HttpURLConnection scConn;

  public MegaClient() {
    super();
  }

  char[] generateID(int len) {
    char id[] = new char[len];
    for (int i = 0; i < 10; i++) {
      id[i] = (char) ('a' + crypto.randInt(26));
    }
    return id;
  }

  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  public String getSessionID() {
    return sessionID;
  }

  public void login(String email, String password, final Callback<Void> callback)
      throws IOException {
    log.info("login() {}", email);

    this.email = email.toLowerCase(Locale.getDefault());

    enqueueCommand(new CommandLogin(this, password, callback) {
      @Override
      public void processResponse(JsonElement e) throws Exception {
        super.processResponse(e);
        fetchNodes(callback);
      }
    });
  }

  public void fetchNodes(final Callback<Void> callback) throws IOException {
    log.info("fetchNodes();");
    enqueueCommand(new CommandFetchNodes(this, callback));
  }

  public void enqueueCommand(@SuppressWarnings("rawtypes") Command cmd) {
    synchronized (cmdQueue) {
      cmdQueue.addLast(cmd);
      cmdQueue.notify();
    }
  }

  public void enqueueCommands(
      @SuppressWarnings("rawtypes") List<Command> commands) {
    synchronized (cmdQueue) {
      cmdQueue.addAll(commands);
      cmdQueue.notify();
    }
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public synchronized void start() {
    if (running)
      return;

    if (threadPool == null)
      throw new RuntimeException("Thread pool not provided");
    running = true;

    if (sessionID != null) {
      log.info("logged in as: {} with session: {}", email, sessionID);
    }

    // initialize random API request sequence ID

    if (reqid == null)
      reqid = generateID(10);

    startSC();

    threadPool.background(new Runnable() {

      @Override
      public void run() {
        try {
          workerLoop();
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        } finally {
          MegaClient.this.stop();
        }
      }
    });
  }

  public synchronized void stop() {
    if (!running)
      return;
    log.info("stop();");
    running = false;
    notifyWorker();
    stopSC();
  }

  @SuppressWarnings("unchecked")
  protected void workerLoop() {

    log.debug("workerLoop();");

    if (scsn != null && sessionID != null) {
      startSC();
    }

    synchronized (cmdQueue) {
      while (running) {
        try {
          cmdQueue.wait();
        } catch (InterruptedException e) {
          log.error(e.getMessage(), e);
          return;
        }
        if (!running)
          return;

        if (!cmdQueue.isEmpty()) {
          // commands to send
          @SuppressWarnings("rawtypes")
          final LinkedList<Command> commands = (LinkedList<Command>) cmdQueue
              .clone();
          cmdQueue.clear();

          if (!running)
            return;

          threadPool.background(new Runnable() {

            @Override
            public void run() {
              log.error("run();");
              try {
                processRequest(commands);
              } catch (IOException e) {
                handleError(e);
              }
            }
          });
        }
      }
    }
  }

  public void getAccountDetails(boolean storage, boolean transfer, boolean pro,
      boolean transactions, boolean purchases, final boolean sessions,
      final Callback<AccountDetails> detailsCallback) {
    final AccountDetails details = new AccountDetails();

    log.info("getAccountDetails() running: {}", this.running);
    @SuppressWarnings("rawtypes")
    LinkedList<Command> commands = new LinkedList<Command>();

    // if (transactions)
    // reqs[r].add(new CommandGetUserTransactions(this, ad));
    // if (purchases)
    // reqs[r].add(new CommandGetUserPurchases(this, ad));

    commands.addLast(new CommandGetUserQuota(this, details, storage, transfer,
        pro) {
      @Override
      public void processResponse(JsonElement e) throws Exception {
        super.processResponse(e);
        if (!sessions)
          detailsCallback.onResult(details);
      }
    });

    log.warn("transactions,purchases not implemented");

    if (sessions) {
      commands.addLast(new Command<AccountDetails>("usl", detailsCallback) {
        @Override
        public void processResponse(JsonElement e) throws Exception {
          details.setSessions(e.getAsJsonArray());
          onResult(details);
        }
      });
    }

    enqueueCommands(commands);

  }

  protected void procsc(JsonObject o) {
    log.trace("procsc() {}", o);

    if (o.has("w")) {
      this.scnotifyurl = o.get("w").getAsString();
    }

    if (o.has("sn")) {
      this.scsn = o.get("sn").getAsString();
      log.trace("scsn: {}", scsn);
    }

    if (o.has("a")) {
      log.trace("a: {}", o.get("a"));
    }
  }

  protected void handleError(IOException e) {
    log.error(e.getMessage(), e);
  }

  protected void processRequest(List<Command> commands) throws IOException {
    log.debug("processRequest() ");

    JsonArray payload = new JsonArray();
    for (Command cmd : commands)
      payload.add(cmd.getPayload());

    StringBuffer url = new StringBuffer(API_URL);

    url.append("cs?id=");
    url.append(reqid);
    if (sessionID != null)
      url.append("&sid=").append(sessionID);
    if (appkey != null)
      url.append("&ak=").append(appkey);

    log.debug("url: {}", url);
    HttpURLConnection conn = (HttpURLConnection) new URL(url.toString())
        .openConnection();

    conn.setDoInput(true);
    conn.setUseCaches(false);
    conn.setRequestProperty("User-Agent", USER_AGENT);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    conn.setAllowUserInteraction(false);
    conn.setRequestMethod("POST");

    if (payload != null) {
      conn.setDoOutput(true);
      String toPost = payload.toString();
      log.debug("posting: {}", toPost);
      byte data[] = toPost.getBytes("UTF-8");
      conn.setRequestProperty("Content-Length", String.valueOf(data.length));
      OutputStream output = conn.getOutputStream();
      output.write(data);
      output.close();
    }

    int responseCode = conn.getResponseCode();
    String encoding = conn.getContentEncoding();
    int length = conn.getContentLength();
    log.debug("responseCode: " + responseCode + " encoding: {} length: "
        + length, encoding);

    InputStream input = conn.getInputStream();
    if ("gzip".equals(encoding))
      input = new GZIPInputStream(input);

    for (int i = reqid.length - 1; i >= 0; i--) {
      reqid[i] += 1;
      if (reqid[i] < 'z')
        break;
      else
        reqid[i] = 'a';
    }

    Reader reader = new BufferedReader(new InputStreamReader(input));
    JsonElement json = GSONUtil.getGSON().fromJson(reader, JsonElement.class);

    log.trace("response: {}", json);

    if (json.isJsonPrimitive()) {
      APIError error = APIError.getError(json.getAsInt());
      for (int i = 0; i < commands.size(); i++) {
        Command cmd = commands.get(i);
        cmd.onError(error);
      }
    } else {

      JsonArray response = json.getAsJsonArray();
      for (int i = 0; i < response.size(); i++) {
        JsonElement e = response.get(i);
        Command cmd = commands.get(i);

        if (e.isJsonPrimitive()) {
          cmd.onError(APIError.getError(e.getAsInt()));
        } else {
          try {
            cmd.processResponse(e);
          } catch (Exception ex) {
            cmd.onError(ex);
          }
        }

      }
    }
  }

  protected void sendSC() {
    if (!running)
      return;

    try {
      String url = null;

      if (scnotifyurl != null) {
        url = scnotifyurl;
      } else {
        url = API_URL;
        url += "sc";
        url += "?sn=" + scsn;
        url += "&sid=" + sessionID;
      }

      log.debug("sendSC() url {}", url);
      scConn = (HttpURLConnection) new URL(url.toString()).openConnection();

      scConn.setDoInput(true);
      scConn.setDoOutput(false);
      scConn.setRequestProperty("User-Agent", USER_AGENT);
      scConn.setRequestProperty("Content-Type", "application/json");
      scConn.setRequestMethod("POST");

      int responseCode = scConn.getResponseCode();
      String encoding = scConn.getContentEncoding();
      int length = scConn.getContentLength();

      log.debug("responseCode: " + responseCode + " encoding: {} length: "
          + length, encoding);

      InputStream input = scConn.getInputStream();
      if ("gzip".equals(encoding))
        input = new GZIPInputStream(input);

      if (scnotifyurl == null) {
        Reader reader = new BufferedReader(new InputStreamReader(input));
        JsonElement response = GSONUtil.getGSON().fromJson(reader,
            JsonElement.class);
        procsc(response.getAsJsonObject());
        backoffsc = 100;
        // scnotifyurl will have been set now
      } else {
        scnotifyurl = null;
      }

    } catch (Exception e) {

      if (!running)
        return;

      if (startedSC)
        log.error(e.getMessage(), e);
      if (backoffsc < 3600000)
        backoffsc <<= 1;
      scnotifyurl = null;
    }

    if (!running || !startedSC)
      return;

    threadPool.background(new Runnable() {

      @Override
      public void run() {
        sendSC();
      }
    }, backoffsc, TimeUnit.MILLISECONDS);
  }

  public void setPasswordKey(byte[] passwordKey) {
    this.passwordKey = passwordKey;
  }

  public byte[] getPasswordKey() {
    return passwordKey;
  }

  public void setMasterKey(byte[] masterKey) {
    this.masterKey = masterKey;
  }

  public byte[] getMasterKey() {
    return masterKey;
  }

  public void setPrivateKey(BigInteger[] rsa_private_key) {
    this.privateKey = rsa_private_key;
  }

  public void setScsn(String scsn) {
    this.scsn = scsn;
    startSC();
  }

  public void startSC() {
    if (!startedSC && running && scsn != null && sessionID != null) {
      synchronized (this) {
        if (!startedSC && running) {
          startedSC = true;
          threadPool.background(new Runnable() {
            @Override
            public void run() {
              sendSC();
            }
          });
        }
      }
    }
  }

  public synchronized void stopSC() {
    if (!startedSC)
      return;
    startedSC = false;
    try {
      if (scConn != null) {
        scConn.disconnect();
        scConn = null;
      }
    } catch (Exception ex) {
    }
  }

  public String getScsn() {
    return scsn;
  }

  protected void notifyWorker() {
    synchronized (cmdQueue) {
      cmdQueue.notifyAll();
    }
  }

  public synchronized void clearNodes() {
    rootNode = incomingNode = mailNode = rubbishNode = null;
    nodes = null;
    users = null;
  }

  public Node getRootNode() {
    return rootNode;
  }

  public void setRootNode(Node rootNode) {
    this.rootNode = rootNode;
    if (currentFolder == null)
      currentFolder = rootNode;
  }

  public Node getIncomingNode() {
    return incomingNode;
  }

  public void setIncomingNode(Node incomingNode) {
    this.incomingNode = incomingNode;
  }

  public Node getRubbishNode() {
    return rubbishNode;
  }

  public User getMe() {
    return me;
  }

  public void setMe(User me) {
    this.me = me;
  }

  public void setRubbishNode(Node rubbishNode) {
    this.rubbishNode = rubbishNode;
  }

  public Node getMailNode() {
    return mailNode;
  }

  public void setMailNode(Node mailNode) {
    this.mailNode = mailNode;
  }

  public void setNodes(HashMap<String, Node> nodes) {
    this.nodes = nodes;
  }

  public List<Node> getChildren() {
    return getChildren(currentFolder);
  }

  public List<Node> getChildren(Node folder) {
    if (nodes == null || folder == null)
      return Collections.emptyList();

    LinkedList<Node> children = new LinkedList<Node>();
    for (Node n : nodes.values()) {
      if (n.getParent() != null && n.getParent().equals(folder.getHandle())) {
        children.add(n);
      }
    }
    return children;
  }

  public Node getCurrentFolder() {
    return currentFolder;
  }

  public void setCurrentFolder(Node folder) {
    this.currentFolder = folder == null ? rootNode : folder;
  }

  public void applyKeys() {
    log.info("applyKeys()");
    for (Node node : nodes.values()) {
      node.applyKey(this);
    }
  }

  public void setUsers(HashMap<String, User> users) {
    this.users = users;
  }

  public void setAppKey(String appKey) {
    this.appkey = appKey;
  }

  public void setThreadPool(ThreadPool threadPool) {
    this.threadPool = threadPool;
  }

  public void onNodesModified() {
    log.info("onNodesModified()");
  }

  public boolean isLoggedIn() {
    return sessionID != null;
  }

  public Node getNode(String handle) {
    for (Node node : nodes.values()) {
      if (node.getHandle().equals(handle))
        return node;
    }
    return null;
  }

}
