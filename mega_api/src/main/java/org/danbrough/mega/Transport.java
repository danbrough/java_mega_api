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
import com.google.gson.JsonParser;

public class Transport {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Transport.class.getSimpleName());

  private String userAgent;

  private boolean running = false;

  private final LinkedList<ApiRequest> requestQueue = new LinkedList<ApiRequest>();
  private int requestCount = 0;
  private int maxRequests = 4;
  private final ThreadPool threadPool;

  public Transport(ThreadPool threadPool) {
    super();
    MegaProperties props = MegaProperties.getInstance();
    userAgent = props.getUserAgent();
    this.threadPool = threadPool;
  }

  public void setMaxRequests(int maxRequests) {
    this.maxRequests = maxRequests;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
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

  private boolean shouldWait() {
    return requestQueue.isEmpty() || requestCount > maxRequests;
  }

  protected void workLoop() {

    while (running) {

      if (shouldWait()) {
        synchronized (requestQueue) {
          if (shouldWait()) {
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

      if (!shouldWait()) {

        ApiRequest request = null;

        synchronized (requestQueue) {
          request = requestQueue.getFirst();
          requestQueue.remove(request);
          requestCount++;
          log.error("requestCount:< " + requestCount);
        }

        final ApiRequest req = request;

        threadPool.background(new Runnable() {
          @Override
          public void run() {
            try {
              postRequest(req);
            } finally {
              synchronized (requestQueue) {
                requestCount--;
                log.error("requestCount:> " + requestCount);
                requestQueue.notify();
              }
            }
          }
        });
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

  public void postRequest(String url, JsonElement payload,
      final Callback callback) {

    log.trace("postRequest() url: {} payload: {}", url, payload);
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(url)
          .openConnection();

      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setRequestProperty("User-Agent", userAgent);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
      conn.setAllowUserInteraction(false);
      conn.setRequestMethod("POST");

      if (payload != null) {
        conn.setDoOutput(true);
        String toPost = '[' + payload.toString() + ']';
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

      JsonElement o = new JsonParser().parse(new BufferedReader(
          new InputStreamReader(is)));

      if (o.isJsonArray() && o.getAsJsonArray().size() == 1) {
        JsonElement element = o.getAsJsonArray().get(0);
        if (element.isJsonPrimitive()) {
          String s = element.getAsString();
          try {
            int i = Integer.parseInt(s);
            callback.onError(i);
          } catch (NumberFormatException ex) {
            callback.onResponse(element);
          }

        } else {
          callback.onResponse(element);
        }
      } else {
        callback.onResponse(o);
      }

    } catch (IOException e) {
      log.error(e.getMessage(), e);
      callback.onError(e);
    }
  }

  protected void postRequest(final ApiRequest request) {
    postRequest(request.getRequestURL(), request.getRequestData(), request);
  }

  public boolean getRequestsPending() {
    return requestCount > 0 || !requestQueue.isEmpty();
  }

}
