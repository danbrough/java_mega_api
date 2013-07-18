package org.danbrough.mega;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Command {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Command.class.getSimpleName());

  JsonObject args = new JsonObject();

  public Command(String action) {
    addArg("a", action);
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

  public void onError(APIError error) {
    log.error("onError() {}", error);
  }

  public void onError(Exception e) {
    log.error("onError()", e);
  }
}
