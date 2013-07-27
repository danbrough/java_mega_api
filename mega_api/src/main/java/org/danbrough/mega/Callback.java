package org.danbrough.mega;

public class Callback<T> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(Callback.class.getSimpleName());

  public void onResult(T result) {
    log.info("onResult(): {}", result);
  }

  public void onError(APIError error) {
    log.error(error.getMessage());
  }

  public void onError(Exception e) {
    log.error(e.getMessage(), e);
  }
}
