package org.danbrough.mega;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Command<T> extends Callback<T> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Command.class.getSimpleName());

  JsonObject args = new JsonObject();
  Callback<T> callback = null;

  public Command(String action) {
    addArg("a", action);
  }

  public Command(String action, Callback<T> callback) {
    this(action);
    this.callback = callback;
  }

  protected void addArg(String name, String value) {
    args.addProperty(name, value);
  }

  protected void addArg(String name, int value) {
    args.addProperty(name, value);
  }

  public JsonObject getPayload() {
    return args;
  }

  public void processResponse(JsonElement e) throws Exception {
    log.debug("processResponse() {}", e);
  }

  @Override
  public void onResult(T result) {
    if (callback != null)
      callback.onResult(result);
    else
      log.info("onResult(): {}", result);
  }

  public void onError(APIError error) {
    if (callback != null)
      callback.onError(error);
    else
      log.error("onError(): {}", error);
  }

  public void onError(Exception e) {
    if (callback != null)
      callback.onError(e);
    else
      log.error(e.getMessage(), e);
  }
}
