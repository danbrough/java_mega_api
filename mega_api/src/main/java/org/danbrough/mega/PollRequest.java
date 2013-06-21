/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PollRequest extends ApiRequest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(PollRequest.class.getSimpleName());

  int waitbackoff = 125;
  String waitUrl;

  public PollRequest(MegaAPI megaApi) {
    super(megaApi);
  }

  @Override
  public String getRequestParams() {
    UserContext user = megaAPI.getUserContext();
    String url = "sc?ssl=1";
    String s = user.getSn();
    if (s != null)
      url += "&sn=" + s;
    s = user.getSid();
    if (s != null)
      url += "&sid=" + s;
    return url;
  }

  void waitsc() {
    final long waitBegin = System.currentTimeMillis();

    new ApiRequest(megaAPI) {
      @Override
      public String getRequestURL() {
        return waitUrl;
      }

      @Override
      public void onError(Exception exception) {
        log.debug(exception.getMessage(), exception);
        getsc();
      }

      @Override
      public void onResponse(JsonElement o) {
        long timeTaken = System.currentTimeMillis() - waitBegin;

        if (timeTaken < 1000)
          waitbackoff += waitbackoff;
        else
          waitbackoff = 125;

        getsc();
      }
    }.send();
  }

  public void getsc() {
    send();
  }

  @Override
  public void onResponse(JsonElement o) {
    super.onResponse(o);

    // if (res.w)
    // {
    // waiturl = res.w;
    //
    // if (waitbackoff > 1000) setTimeout(waitsc,waitbackoff);
    // else waitsc();
    // }
    // else
    // {
    // if (res.sn) maxaction = res.sn;
    // execsc(res.a);
    // }

    JsonObject obj = o.getAsJsonObject();

    if (obj.has("w")) {
      waitUrl = obj.get("w").getAsString();
      log.warn("found wait url {}", waitUrl);
      if (waitbackoff > 1000) {
        megaAPI.getThreadPool().background(new Runnable() {
          @Override
          public void run() {
            waitsc();
          }
        }, waitbackoff, TimeUnit.MILLISECONDS);
      } else {
        waitsc();
      }

      return;
    }

    if (obj.has("sn")) {
      getUserContext().setSn(obj.get("sn").getAsString());
      log.trace("found sn: {}", getUserContext().getSn());
    } else {
      throw new MegaAPI.ProtocolException(obj, "Expecting a sn field");
    }

    if (obj.has("a")) {
      execsc(obj.get("a"));
    } else {
      throw new MegaAPI.ProtocolException(obj, "Expecting a \"a\" field");
    }
  }

  void execsc(JsonElement a) {
    if (a.isJsonArray()) {
      processA(a.getAsJsonArray());
    } else {
      log.error("a = {}", a);
      // if (a == -15)
      // {
      // ul_uploading=false;
      // downloading=false;
      // logout();
      // return false;
      // }
    }
  }

  void processA(JsonArray w) {
    for (JsonElement e : w) {
      processPacket(e.getAsJsonObject());
    }
    packetComplete();
  }

  void packetComplete() {
    log.error("packetComplete() not implemented");
    // not sure if anything to do here .. perhaps just to reschedule the poll
    // and update gui

    // pc_lastt = new Date().getTime();
    // pc_proct = new Date().getTime();
    // crypto_sendrsa2aes();
    // if (refreshtree)
    // {
    // refreshtree=false;
    // refreshtreepanel();
    // }
    // else if (refreshdirsort)
    // {
    // refreshdirsort=false;
    // setTimeout("dirsort(dirroot);",500);
    // }
    // if (refreshtopmenu)
    // {
    // refreshtopmenu=false;
    // updateTopFolderMenu(currentdirid);
    // }
    // if (refreshgr)
    // {
    // refreshgr=false;
    // refreshgrid();
    // }
    // pc_lastt = new Date().getTime();
    // pc_proct = new Date().getTime()-pc_proct;
    //
    // fm_thumbnails();
    //
    // if (mobileversion) mobileui(1);
    //
    //
    // document.getElementById('overlay').style.cursor='default';
    // setTimeout("document.getElementById('overlay').style.display='none';",10);
    // setTimeout("getsc()",100);
    //

    megaAPI.getThreadPool().background(new Runnable() {

      @Override
      public void run() {
        getsc();
      }
    }, 100, TimeUnit.MILLISECONDS);

    // if (anotifications > 0)
    // {
    // donotify();
    // anotifications=0;
    // }
  }

  void processPacket(JsonObject o) {
    log.debug("processPacket() {}", o);

    if (!o.has("a")) {
      log.error("NNNNNNNNNNNNNNOOOOOOOOOOO AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
      return;
    }
    String a = o.get("a").getAsString();

    // if (packet.i == requesti) {
    // if (d)
    // console.log('OWN ACTION PACKET; IGNORE');

  }
}
