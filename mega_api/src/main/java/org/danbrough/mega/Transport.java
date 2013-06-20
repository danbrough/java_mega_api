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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Transport {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Transport.class.getSimpleName());

  private String userAgent;
  private String apiPath;

  private boolean running = false;

  private final LinkedList<ApiRequest> requestQueue = new LinkedList<ApiRequest>();
  private int requestCount = 0;
  private final ThreadPool threadPool;

  public Transport(ThreadPool threadPool) {
    super();
    MegaProperties props = MegaProperties.getInstance();
    userAgent = props.getUserAgent();
    apiPath = props.getApiPath();
    this.threadPool = threadPool;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public void setApiPath(String apiPath) {
    this.apiPath = apiPath;
  }

  public synchronized void start() {
    log.info("start()");
    if (running)
      return;
    running = true;

    Thread workerThread = new Thread() {
      @Override
      public void run() {
        workLoop();
        log.debug("workLoop() finished");
        Transport.this.running = false;
      }
    };
    workerThread.start();
  }

  public synchronized void stop() {
    log.info("stop()");
    if (!running)
      return;
    running = false;
    synchronized (requestQueue) {
      requestQueue.notify();
    }
  }

  protected void workLoop() {

    while (true) {
      if (requestQueue.isEmpty() && running) {
        synchronized (requestQueue) {
          if (requestQueue.isEmpty() && running) {
            try {
              requestQueue.wait();
            } catch (InterruptedException e) {
              continue;
            }
          }
        }
      }

      if (!running)
        return;

      if (!requestQueue.isEmpty()) {
        ApiRequest request = null;
        synchronized (requestQueue) {
          request = requestQueue.getFirst();
        }
        if (request != null && running) {
          synchronized (requestQueue) {
            requestQueue.remove(request);
          }
          final ApiRequest req = request;
          threadPool.background(new Runnable() {

            @Override
            public void run() {
              try {
                requestCount++;
                postRequest(req);
              } catch (IOException e) {
                log.error(e.getMessage(), e);
                req.onError(e);
              } finally {
                requestCount--;
              }
            }
          });

        }
      }
    }
  }

  public void queueRequest(ApiRequest request) {
    log.debug("queueRequest() request: {} running: {}", request, running);
    synchronized (requestQueue) {
      requestQueue.addLast(request);
      requestQueue.notify();
    }
  }

  protected void postRequest(ApiRequest request) throws IOException {
    log.debug("postRequest() {}", request);

    String url = apiPath;
    JsonObject requestData = request.getRequestData();

    if (requestData != null) {
      url += "cs?id=" + request.getRequestId();
      String sid = request.getUserContext().getSid();
      if (sid != null)
        url += "&sid=" + sid;
    } else {
      // sc request
      String params = request.getRequestParams();
      if (params == null) {
        log.error("Neither request data or request params available");
        return;
      }
      url += params;
    }

    log.trace("url: {}", url);

    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

    conn.setDoInput(true);
    conn.setUseCaches(false);
    conn.setRequestProperty("User-Agent", userAgent);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    conn.setAllowUserInteraction(false);
    conn.setRequestMethod("POST");

    if (requestData != null) {
      conn.setDoOutput(true);
      String toPost = '[' + requestData.toString() + ']';
      log.trace("posting <{}>", toPost);
      byte data[] = toPost.getBytes("UTF-8");
      conn.setRequestProperty("Content-Length", String.valueOf(data.length));
      OutputStream output = conn.getOutputStream();
      output.write(data);
      output.close();
    }

    log.trace("content length: " + conn.getContentLength());

    InputStream is = conn.getInputStream();
    if ("gzip".equals(conn.getContentEncoding()))
      is = new GZIPInputStream(is);

    try {
      JsonElement o = new JsonParser().parse(new BufferedReader(
          new InputStreamReader(is)));

      if (o.isJsonPrimitive() && o.getAsInt() == ApiRequest.EAGAIN) {
        int retryTime = request.getRetryTime();
        retryTime = retryTime == 0 ? 125 : 2 * retryTime;
        request.setRetryTime(retryTime);
        log.warn("EAGAIN: " + retryTime);
        try {
          Thread.sleep(retryTime);
        } catch (InterruptedException e) {
          log.error(e.getMessage(), e);
        }
        queueRequest(request);
        return;
      }

      request.setResponse(o);
    } catch (JsonParseException e) {
      log.error(e.getMessage(), e);
    }

  }

  public boolean getRequestsPending() {
    return requestCount > 0 || !requestQueue.isEmpty();
  }

}
