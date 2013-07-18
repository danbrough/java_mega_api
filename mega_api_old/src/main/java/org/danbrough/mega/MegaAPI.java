/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MegaAPI {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaAPI.class.getSimpleName());

  public static interface FilesVisitor {
    public boolean visit(MegaFile file);
  }

  /**
   * An exception for unexpected data being returned from the server
   */
  public static class ProtocolException extends RuntimeException {

    private static final long serialVersionUID = -1342234110424726551L;
    JsonElement data;

    public ProtocolException(JsonElement data, String msg) {
      super(msg);
      this.data = data;
    }

    public JsonElement getData() {
      return data;
    }
  }

  protected final Crypto crypto;
  protected final Transport transport;
  private UserContext user;
  private final Random random;

  protected final ThreadPool threadPool;
  MegaListener listener;

  public MegaAPI() {
    super();
    this.crypto = createCrypto();
    this.threadPool = createThreadPool();
    this.transport = createTransport(threadPool);
    random = new Random();
  }

  public void setListener(MegaListener listener) {
    this.listener = listener;
  }

  public MegaListener getListener() {
    return listener;
  }

  public Random getRandom() {
    return random;
  }

  public UserContext createUserContext(String email, String password) {
    byte passwordKey[] = crypto.prepareKey(password);
    return createUserContext(email, passwordKey);
  }

  public UserContext createUserContext(String email, byte passwordKey[]) {
    user = new UserContext();
    user.setEmail(email);
    user.setPasswordKey(passwordKey);
    return user;
  }

  public UserContext createUserContext(JsonObject conf) {
    user = crypto.fromJSON(conf, UserContext.class);
    return user;
  }

  public UserContext getUserContext() {
    return user;
  }

  public ThreadPool getThreadPool() {
    return threadPool;
  }

  protected Crypto createCrypto() {
    return new Crypto();
  }

  protected ThreadPool createThreadPool() {
    return new ExecutorThreadPool();
  }

  protected Transport createTransport(ThreadPool pool) {
    return new Transport(pool);
  }

  public String getTermsHTML() {

    BufferedReader input = new BufferedReader(new InputStreamReader(
        MegaAPI.class.getResourceAsStream("/org/danbrough/mega/terms.html")));
    String s = null;
    StringBuffer content = new StringBuffer();
    try {
      while ((s = input.readLine()) != null) {
        content.append(s).append('\n');
      }
      input.close();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    return content.toString();
  }

  public void start() {
    log.info("start()");
    threadPool.start();
    transport.start();

  }

  public void stop() {
    log.info("stop()");
    transport.stop();
    threadPool.stop();
  }

  protected ApiRequest sendRequest(ApiRequest request) {
    transport.queueRequest(request);
    return request;
  }

  private String makeid(int len) {
    StringBuffer text = new StringBuffer();
    Random r = new Random();
    String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (int i = 0; i < len; i++) {
      text.append(possible.charAt(r.nextInt(possible.length())));
    }
    return text.toString();
  }

  public void load_notifications(JsonObject o) {
    log.info("load_notifications()");

    // NotificationStore.clearData();
    // maxaction = json.fsn;
    boolean nread = false;
    // var nread=false;
    JsonArray c = o.get("c").getAsJsonArray();
    log.debug("c.length = " + c.size());
    for (int i = 0; i < c.size(); i++) {
      if (o.get("la").getAsInt() == i)
        nread = true;
      JsonObject item = c.get(i).getAsJsonObject();

      String id = makeid(10);
      String type = item.get("t").getAsString();
      int timestamp = (int) (System.currentTimeMillis() / 1000)
          - item.get("td").getAsInt();
      String user = item.get("u").getAsString();

      // NotificationStore.loadData([
      // {
      // id: makeid(10),
      // type: json.c[i].t,
      // timestamp: (new Date().getTime()/1000)-json.c[i].td,
      // user: json.c[i].u,
      // folderid: json.c[i].n,
      // nodes: json.c[i].f,
      // read: nread,
      // popup: true,
      // count: nread,
      // rendered: true
      // }],true);
    }

    // donotify();

  }

  public void upload(File file, String destDir, String fileName) {
    log.info("upload() {}", file);

  }

  public void logout() {
    user = null;
    if (listener != null)
      listener.onLoggedOut();
  }

  public boolean isLoggedIn() {
    return user != null;
  }

}
