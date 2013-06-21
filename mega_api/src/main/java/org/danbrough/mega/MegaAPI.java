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
import com.google.gson.JsonParser;

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

  public MegaAPI() {
    super();
    this.crypto = createCrypto();
    this.threadPool = createThreadPool();
    this.transport = createTransport(threadPool);
    random = new Random();
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
    return new ThreadPool();
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

  protected void process_u(JsonArray a) {
    log.info("process_u() {}", a);

    for (int i = 0; i < a.size(); i++) {
      JsonObject o = a.get(i).getAsJsonObject();

      int c = o.get("c").getAsInt();
      String email = o.get("m").getAsString();
      String u = o.get("u").getAsString();
      log.debug("c " + c + " email: " + email + " u: " + u);

      if ((c == 1) && !u.equals(user.getHandle())) {
        // add this as a contact
      } else if (c == 0) {
        // delete this contact
      } else if (c == 2) {
        // this is the current user
        user.setHandle(u);
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
    log.info("process_ok()");
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
      String s = crypto.crypto_handleauth(h, user);

      // if (ok[i].ha == crypto_handleauth(ok[i].h)) u_sharekeys[ok[i].h] =
      // decrypt_key(u_k_aes,base64_to_a32(ok[i].k));

      if (ha.equals(s)) {
        byte decKey[] = crypto.decrypt_key(user.getMasterKey(),
            crypto.base64urldecode(k));
        user.getSharedKeys().put(h, decKey);
        log.debug("added sharedkey: {}", h);
      }
    }
  }

  public void process_s(JsonArray s) {
    log.info("process_s() {}", s);
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
        log.debug("id: " + (h + h) + " userid: " + item.get("u").getAsString()
            + " folderid: " + item.get("h").getAsString() + " rights: "
            + item.get("r").getAsInt());
        // sharednodes[json.s[i].h]=true;
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

  public void processFiles(JsonArray files, FilesVisitor visitor) {
    for (JsonElement file : files) {
      if (!processFile(file.getAsJsonObject(), visitor))
        return;
    }
  }

  private void processKey(MegaFile file, String attrs) {
    log.debug("processKey() {}", file.getHandle());

    String k = file.getKey();
    if (k == null) {
      log.error("file has no key");
      return;
    }

    String id = user.getHandle();
    log.debug("me: {}", id);
    int p = k.indexOf(id + ':');

    if (p == -1) {
      log.trace("file is not owned by me");
      // check to see if I have a suitable shared key
      // if not .. record as missing
      return;
    }

    // i own the file

    // delete keycache[file.h];

    int pp = k.indexOf('/', p);

    if (pp < 0)
      pp = k.length();
    p += id.length() + 1;
    String key = k.substring(p, pp);

    log.trace("key {}", key);
    if (key.length() < 46) {
      log.trace("short key aes");
      // k = decrypt_key(id == me ? master_aes : new
      // sjcl.cipher.aes(u_sharekeys[id]),k);

      byte dec_key[] = crypto.base64urldecode(key);

      log.debug("master_key length:{} dec_key.length:{}",
          user.getMasterKey().length, dec_key.length);

      dec_key = crypto.decrypt_key(dec_key, user.getMasterKey());

      if (file.getType() == MegaFile.TYPE_FILE) {
        // int a32Key[] = crypto.bytes_to_a32(dec_key);
        // int newkey[] = { a32Key[0] ^ a32Key[4], a32Key[1] ^ a32Key[5],
        // a32Key[2] ^ a32Key[6], a32Key[3] ^ a32Key[7] };
        // dec_key = crypto.a32_to_bytes(newkey);
        byte new_key[] = new byte[16];
        for (int j = 0; j < new_key.length; j++) {
          new_key[j] = (byte) (dec_key[j] ^ dec_key[j + 16]);
        }
        dec_key = new_key;
      }

      attrs = crypto.decrypt_attrs(attrs, dec_key);
      if (attrs.startsWith("MEGA{")) {
        attrs = attrs.substring(4);
        file.setAttributes(new JsonParser().parse(attrs).getAsJsonObject());
        log.trace("attrs {}", crypto.toPrettyString(file.getAttributes()));
        if (file.getAttributes().has("n")) {
          file.setName(file.getAttributes().get("n").getAsString());
        }
      } else
        log.error("failed to decode attribute");

    } else {
      // long keys rsa
      log.trace("long key rsa");
    }

  }

  public boolean processFile(JsonObject o, FilesVisitor visitor) {
    log.debug("processFile() {}", o.toString());

    int t = o.get("t").getAsInt();
    String h = o.get("h").getAsString();

    MegaFile file = new MegaFile(h);
    file.setType(t);
    file.setTimestamp(o.get("ts").getAsInt());

    // if (f.sk) u_sharekeys[f.h] = crypto_process_sharekey(f.h,f.sk);
    if (o.has("sk")) {
      log.warn("found sk");
    }

    // if ((f.t !== 2) && (f.t !== 3) && (f.t !== 4) && (f.k))
    // {
    // crypto_processkey(u_handle,u_k_aes,f);
    // u_nodekeys[f.h] = f.key;
    //
    // if ((typeof f.name !== 'undefined') && (f.p == InboxID)) InboxCount++;
    // }

    if ((t != 2) && (t != 3) && (t != 4) && o.has("k")) {
      file.setKey(o.get("k").getAsString());
      processKey(file, o.get("a").getAsString());
    }
    // else
    // {
    // if (f.a)
    // {
    // if (!missingkeys[f.h])
    // {
    // missingkeys[f.h] =true;
    // newmissingkeys = true;
    // }
    // }
    // f.k = '';
    // f.name = '';
    // }
    else {
      if (o.has("a")) {
        log.debug("o.a exists");
      }
    }

    // if (f.t == 2) RootID = f.h;
    // else if (f.t == 3) InboxID = f.h;
    // else if (f.t == 4) TrashbinID = f.h;

    if (t == MegaFile.TYPE_ROOT) {
      user.setRootId(h);
    } else if (t == MegaFile.TYPE_INBOX) {
      user.setInboxId(h);
    } else if (t == MegaFile.TYPE_TRASH) {
      user.setTrashId(h);
    }
    // else if ((f.t < 2) || (f.t == 5))
    else if ((t < 2) || (t == 5)) {
      // if (f.t == 5)
      // {
      // f.p = f.u;
      // f.t = 1;
      // }
      if (t == 5) {
        log.debug("t == 5");
      }
    }
    return visitor.visit(file);
  }

  public void upload(File file, String destDir, String fileName) {
    log.info("upload() {}", file);

  }

}
