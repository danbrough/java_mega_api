/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ApiRequest implements Callback {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ApiRequest.class.getSimpleName());

  // An internal error has occurred. Please submit a bug report, detailing the
  // exact circumstances in which this error occurred.
  public static final int EINTERNAL = -1;

  // : You have passed invalid arguments to this command
  public static final int EARGS = -2;

  // (always at the request level): A temporary congestion or server malfunction
  // prevented your request from being processed. No data was altered. Retry.
  // Retries must be spaced with exponential backoff.
  public static final int EAGAIN = -3;

  // You have exceeded your command weight per time quota. Please wait a few
  // seconds, then try again (this should never happen in sane real-life
  // applications).
  public static final int ERATELIMIT = -4;

  // Upload errors:

  // The upload failed. Please restart it from scratch.
  public static final int EFAILED = -5;

  // Too many concurrent IP addresses are accessing this upload target URL.
  public static final int ETOOMANY = -6;

  // The upload file packet is out of range or not starting and ending on a
  // chunk boundary.

  public static final int ERANGE = -7;
  // The upload target URL you are trying to access has expired. Please request
  // a fresh one.
  public static final int EEXPIRED = -8;

  // Filesystem/Account-level errors:
  // Object (typically, node or user) not found
  public static final int ENOENT = -9;

  // Circular linkage attempted
  public static final int ECIRCULAR = -10;

  // Access violation (e.g., trying to write to a read-only share)
  public static final int EACCESS = -11;

  // Trying to create an object that already exists
  public static final int EEXIST = -12;

  // Trying to access an incomplete resource
  public static final int EINCOMPLETE = -13;

  // A decryption operation failed (never returned by the API)
  public static final int EKEY = -14;

  // Invalid or expired user session, please relogin
  public static final int ESID = -15;

  // User blocked
  public static final int EBLOCKED = -16;

  // Request over quota
  public static final int EOVERQUOTA = -17;

  // Resource temporarily not available, please try again later
  public static final int ETEMPUNAVAIL = -18;

  private static int idSequence = (int) Math.ceil((Math.random() * 1000000000));

  protected static Crypto crypto = new Crypto();

  private final int requestId = ++idSequence;
  protected JsonObject requestData;

  protected final MegaAPI megaAPI;
  private int retryTime = 0;

  public ApiRequest(MegaAPI megaAPI) {
    this.megaAPI = megaAPI;
  }

  public int getRequestId() {
    return requestId;
  }

  public JsonObject getRequestData() {
    return requestData;
  }

  public String getRequestParams() {
    return null;
  }

  public String getRequestURL() {
    String url = MegaProperties.getInstance().getApiPath();
    JsonObject payload = getRequestData();
    if (payload != null) {
      url += "cs?id=" + getRequestId();
      String sid = getUserContext().getSid();
      if (sid != null)
        url += "&sid=" + sid;
    } else {
      // sc request
      String params = getRequestParams();
      if (params == null) {
        log.error("Neither request data or request params available");
        return null;
      }
      url += params;
    }
    return url;
  }

  protected final UserContext getUserContext() {
    return megaAPI.getUserContext();
  }

  @Override
  public final void onError(int code) {
    log.error("onError() code: " + code);

    if (code == ApiRequest.EAGAIN) {

      retryTime = retryTime == 0 ? 125 : 2 * retryTime;

      log.warn("EAGAIN: " + retryTime);
      megaAPI.getThreadPool().background(new Runnable() {

        @Override
        public void run() {
          send();
        }
      }, retryTime, TimeUnit.MILLISECONDS);
    } else {
      processOnError(code);
    }
  }

  protected void processOnError(int code) {
  }

  @Override
  public void onError(Exception exception) {
    log.error(exception.getMessage(), exception);
  }

  @Override
  public void onResponse(JsonElement o) {
    log.debug("onResponse() {}", crypto.toPrettyString(o));
  }

  public void send() {
    megaAPI.sendRequest(this);
  }

}