/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MegaAPI {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaAPI.class.getSimpleName());

  protected final Crypto crypto;
  protected final Transport transport;
  private UserContext ctx;
  private int sequence = (int) Math.ceil((Math.random() * 1000000000));

  private String currentuser;

  public MegaAPI() {
    super();
    this.crypto = createCrypto();
    this.transport = createTransport();
  }

  public UserContext createUserContext(String email, String password) {
    byte passwordKey[] = crypto.prepareKey(password);
    return createUserContext(email, passwordKey);
  }

  public UserContext createUserContext(String email, byte passwordKey[]) {
    ctx = new UserContext();
    ctx.setEmail(email);
    ctx.setPasswordKey(passwordKey);
    return ctx;
  }

  public UserContext createUserContext(JsonObject conf) {
    ctx = UserContext.fromJSON(conf);
    return ctx;
  }

  public UserContext getUserContext() {
    return ctx;
  }

  protected Crypto createCrypto() {
    return new Crypto();
  }

  protected Transport createTransport() {
    return new Transport();
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
    transport.start();
  }

  public void stop() {
    log.info("stop()");
    transport.stop();
  }

  protected ApiRequest sendRequest(ApiRequest request) {
    transport.queueRequest(request);
    return request;
  }

  protected final int getNextRequestID() {
    return ++sequence;
  }

  protected void process_u(JsonArray a) {
    log.debug("process_u() {}", a);

    for (int i = 0; i < a.size(); i++) {
      JsonObject o = a.get(i).getAsJsonObject();

      int c = o.get("c").getAsInt();
      String email = o.get("m").getAsString();
      String u = o.get("u").getAsString();
      log.debug("c " + c + " email: " + email + " u: " + u);

      if ((c == 1) && u.equals(currentuser)) {
        // add this as a contact
      } else if (c == 0) {
        // delete this contact
      } else if (c == 2) {
        // this is the current user
      }
    }

  }

  /**
   * For each item in "ok", if ((item.h + item.h) decrypted with the master key)
   * equals item.ha then put shared_keys[item.h] = (k decrypted with master key)
   * 
   * if (ok[i].ha == crypto_handleauth(ok[i].h)) u_sharekeys[ok[i].h] =
   * decrypt_key(u_k_aes,base64_to_a32(ok[i].k));
   */
  protected void process_ok(JsonArray ok) {
    log.debug("process_ok()");
    log.debug("ok: {}", ok.toString());

    // "ok": [{
    // "h": "p08S2LyJ",
    // "ha": "_jQmHRNgfI_4CaQmuUE3ig",
    // "k": "rLH91zslsr2Y2MfRIREFKw"
    // }],

    for (int i = 0; i < ok.size(); i++) {
      JsonObject o = ok.get(i).getAsJsonObject();
      String ha = o.get("ha").getAsString();
      String h = o.get("h").getAsString();
      String k = o.get("k").getAsString();

      log.debug("ha: " + ha + " h: " + h + " k: " + k);
      // crypto_handleauth('p08S2LyJ') should be "_jQmHRNgfI_4CaQmuUE3ig"
      String s = crypto.crypto_handleauth(h, ctx);

      // if (ok[i].ha == crypto_handleauth(ok[i].h)) u_sharekeys[ok[i].h] =
      // decrypt_key(u_k_aes,base64_to_a32(ok[i].k));

      if (ha.equals(s)) {
        byte decKey[] = crypto.decrypt_key(ctx.getMasterKey(),
            crypto.base64urldecode(k));
        ctx.getSharedKeys().put(h, decKey);
        log.debug("added sharedkey: {}", h);
      }
    }
  }

  public void process_s(JsonArray s) {
    // for(i in json.s)
    for (int i = 0; i < s.size(); i++) {
      JsonObject item = s.get(i).getAsJsonObject();

      // {
      // if (u_sharekeys[json.s[i].h])
      // {
      // sharingData.push(
      // {
      // id: json.s[i].h + '_' + json.s[i].u,
      // userid: json.s[i].u,
      // folderid: json.s[i].h,
      // rights: json.s[i].r,
      // date: json.s[i].ts
      // });
      // sharednodes[json.s[i].h]=true;
      // }
      // }
      //

      String h = item.get("h").getAsString();
      if (getUserContext().getSharedKeys().containsKey(h)) {
        log.debug("u_sharekeys[json.s[i].h] is true i: " + i + " h: {}", h);

      }

    }
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

}
