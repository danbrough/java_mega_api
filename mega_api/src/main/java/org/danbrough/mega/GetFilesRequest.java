/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import org.danbrough.mega.MegaAPI.FilesVisitor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetFilesRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(GetFilesRequest.class.getSimpleName());

  protected UserContext user;

  public GetFilesRequest(MegaAPI megaAPI) {
    super(megaAPI);

    requestData = new JsonObject();
    requestData.addProperty("a", "f");
    requestData.addProperty("c", 1);
    requestData.addProperty("r", 1);

    user = megaAPI.getUserContext();

  }

  @Override
  public void onResponse(JsonElement obj) {
    JsonObject o = obj.getAsJsonObject();

    log.debug("onResponse() {}", crypto.toPrettyString(o));

    if (o.has("sn")) {
      megaAPI.getUserContext().setSn(o.get("sn").getAsString());
    }

    if (o.has("u")) {
      megaAPI.process_u(o.get("u").getAsJsonArray());
    }

    if (o.has("ok")) {
      megaAPI.process_ok(o.get("ok").getAsJsonArray());
    }
    //
    // "s": [{
    // "h": "p08S2LyJ",
    // "r": 2,
    // "ts": 1371328442,
    // "u": "vnO5t0dt9iU"
    // }],
    //

    if (o.has("s")) {
      megaAPI.process_s(o.get("s").getAsJsonArray());
    }

    // final String n_sn = "&sn=" + o.getString("sn");
    // megaAPI.sendRequest(new ApiRequest(megaAPI) {
    // @Override
    // public String getURL() {
    // return MegaProperties.getInstance().getApiPath() + "sc?c=100" + n_sn;
    // }
    //
    // @Override
    // public void onResponse(Object o) {
    // try {
    // log.debug("read: {}", ((JsonObject) o).toString(1));
    // megaAPI.load_notifications((JsonObject) o);
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // }
    // });
    //

    if (o.has("f")) {
      processFiles(o.get("f").getAsJsonArray(), new FilesVisitor() {
        @Override
        public boolean visit(MegaFile file) {
          log.debug("visited {}", file);
          return true;
        }
      });
    }

  }

  public void processFiles(JsonArray files, FilesVisitor visitor) {
    for (JsonElement file : files) {
      if (!processFile(file.getAsJsonObject(), visitor))
        return;
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

    if ((t != 2) && (t != 3) && (t != 4)) {
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

  private void processKey(MegaFile file, String attrs) {
    log.debug("processKey() {}", file.getHandle());

    String k = file.getKey();

    String id = getUserContext().getHandle();
    log.debug("me: {}", id);
    if (id == null) {
      log.error("NO USER ID");
      return;
    }

    // if (k == null || k.equals("")) {
    // log.error("file has no key");
    // return;
    // }

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

    decodeAttributes(file, key, attrs);

  }

  private void decodeAttributes(MegaFile file, String key, String attrs) {
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
      } else {
        log.error("failed to decode attribute");
      }

    } else {
      // long keys rsa
      log.error("long key rsa NOT IMPLEMENTED!");
    }
  }
}
