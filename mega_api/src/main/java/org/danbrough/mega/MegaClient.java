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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.http.message.BasicLineFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MegaClient {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaClient.class.getSimpleName());

  public static final String API_URL = "https://g.api.mega.co.nz/";
  public static final String USER_AGENT = "MegaJavaClient-1.0";

  MegaCrypto crypto = MegaCrypto.get();

  LinkedList<Command> cmdQueue = new LinkedList<Command>();
  BasicLineFormatter d;

  byte passwordKey[];
  byte masterKey[];
  BigInteger[] privateKey;
  String email;

  // no two interrelated client instances should ever have the same sessionid
  String sessionID;
  char[] reqid;
  String appkey;
  String auth;
  boolean running = false;

  ThreadPool threadPool;

  // back off time for server client requests
  long backoffsc = 500;

  String scnotifyurl = null;

  boolean startedSC = false;

  // server-client request sequence number
  String scsn;

  public MegaClient(String appkey, ThreadPool threadPool) {
    this.threadPool = threadPool;
    this.appkey = "&ak=" + appkey;
    this.auth = "";

    // initialize random client application instance ID

    this.sessionID = new String(generateID(10));

    // initialize random API request sequence ID

    this.reqid = generateID(10);

    log.debug("sessionID: {} reqid: {}", sessionID, new String(reqid));
  }

  char[] generateID(int len) {
    char id[] = new char[len];
    for (int i = 0; i < 10; i++) {
      id[i] = (char) ('a' + crypto.randInt(26));
    }
    return id;
  }

  void incrementRequestID() {
    // increment unique request ID
    for (int i = reqid.length - 1; i >= 0; i--) {
      reqid[i] += 1;
      if (reqid[i] < 'z')
        break;
      else
        reqid[i] = 'a';
    }
  }

  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
    auth = "&sid=" + sessionID;
  }

  public String getSessionID() {
    return sessionID;
  }

  public void login(String email, String password) throws IOException {
    log.info("login() {}", email);

    this.email = email.toLowerCase(Locale.getDefault());

    enqueueCommand(new CommandLogin(this, password) {

      @Override
      public void processResponse(JsonElement e) throws Exception {
        super.processResponse(e);
        fetchNodes();
      }
    });
  }

  public void fetchNodes() throws IOException {
    log.info("fetchNodes();");

    enqueueCommand(new CommandFetchNodes(this) {
      @Override
      public void processResponse(JsonElement e) throws Exception {
        super.processResponse(e);
      }
    });
  }

  public void enqueueCommand(Command cmd) {
    synchronized (cmdQueue) {
      cmdQueue.addLast(cmd);
      cmdQueue.notify();
    }
  }

  public String getEmail() {
    return email;
  }

  public synchronized void start() {
    if (running)
      return;
    running = true;

    new Thread() {
      public void run() {
        try {
          workerLoop();
        } finally {
          MegaClient.this.stop();
        }
      }
    }.start();
  }

  public synchronized void stop() {
    if (!running)
      return;
    running = false;

    notifyWorker();
  }

  @SuppressWarnings("unchecked")
  protected void workerLoop() {

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
          final LinkedList<Command> commands = (LinkedList<Command>) cmdQueue
              .clone();
          cmdQueue.clear();

          threadPool.background(new Runnable() {

            @Override
            public void run() {
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

  protected void sendSC() {
    if (!running)
      return;
    log.trace("sendSC()");

    try {
      String url = null;

      if (scnotifyurl != null)
        url = scnotifyurl;
      else {
        url = API_URL;
        url += "sc?ssl=1&sn=";
        url += scsn;
        url += auth;
      }

      log.trace("url {}", url);
      final HttpURLConnection conn = (HttpURLConnection) new URL(url.toString())
          .openConnection();
      scnotifyurl = null;
      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setRequestProperty("User-Agent", USER_AGENT);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
      conn.setAllowUserInteraction(false);
      conn.setRequestMethod("POST");

      int responseCode = conn.getResponseCode();
      String encoding = conn.getContentEncoding();
      int length = conn.getContentLength();

      log.debug("responseCode: " + responseCode + " encoding: {} length: "
          + length, encoding);

      InputStream input = conn.getInputStream();
      if ("gzip".equals(encoding))
        input = new GZIPInputStream(input);

      backoffsc = 500;

      if (length > 0) {
        Reader reader = new BufferedReader(new InputStreamReader(input));
        JsonElement response = GSONUtil.getGSON().fromJson(reader,
            JsonElement.class);

        procsc(response.getAsJsonObject());

      } else {
        log.error("No response to sc");

        if (backoffsc < 3600000)
          backoffsc <<= 1;
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);

      if (backoffsc < 3600000)
        backoffsc <<= 1;
    }

    threadPool.background(new Runnable() {

      @Override
      public void run() {
        sendSC();
      }
    }, backoffsc, TimeUnit.MILLISECONDS);

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
      log.error("A: {}", o.get("a"));
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
    url.append(auth);
    url.append(appkey);

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

    Reader reader = new BufferedReader(new InputStreamReader(input));
    JsonArray response = GSONUtil.getGSON().fromJson(reader, JsonArray.class);

    log.trace("response: {}", response);

    for (int i = 0; i < response.size(); i++) {
      JsonElement e = response.get(i);
      Command cmd = commands.get(i);

      try {
        cmd.onError(APIError.getError(e.getAsInt()));
      } catch (Exception ex) {
        try {
          cmd.processResponse(e);
        } catch (Exception ex2) {
          cmd.onError(ex2);
        }
      }
    }
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

  public void setPrivateKey(BigInteger[] rsa_private_key) {
    this.privateKey = rsa_private_key;
  }

  public void setScsn(String scsn) {
    this.scsn = scsn;

    if (!startedSC) {
      synchronized (this) {
        if (!startedSC) {
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

  protected void notifyWorker() {
    synchronized (cmdQueue) {
      cmdQueue.notifyAll();
    }
  }

  public String getScsn() {
    return scsn;
  }

  // protected void processRequestAsync(List<Command> commands) throws
  // IOException {
  // log.debug("processRequestAsync() ");
  //
  // JsonArray payload = new JsonArray();
  // for (Command cmd : commands)
  // payload.add(cmd.getPayload());
  //
  // StringBuffer url = new StringBuffer(API_URL);
  //
  // url.append("cs?id=");
  // url.append(reqid);
  // url.append(auth);
  // url.append(appkey);
  //
  // HttpAsyncRequestProducer post = HttpAsyncMethods.createPost(url.toString(),
  // payload.toString(), ContentType.APPLICATION_JSON);
  //
  // // request.setHeader("User-Agent", USER_AGENT);
  // // request.setHeader("Content-Type", "application/json");
  // // request.setHeader("Accept-Encoding", "gzip, deflate");
  //
  // try {
  // JsonArray response = httpClient.execute(post, new JsonConsumer(), null)
  // .get();
  //
  // for (int i = 0; i < response.size(); i++) {
  // JsonElement e = response.get(i);
  // Command cmd = commands.get(i);
  //
  // try {
  // cmd.onError(APIError.getError(e.getAsInt()));
  // } catch (Exception ex) {
  // try {
  // cmd.processResponse(e);
  // } catch (Exception ex2) {
  // cmd.onError(ex2);
  // }
  // }
  //
  // }
  // } catch (Exception e) {
  // log.error(e.getMessage(), e);
  // }
  //
  // }
}
