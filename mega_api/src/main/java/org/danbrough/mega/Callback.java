package org.danbrough.mega;

public class Callback<T> {
  public void onResult(T result) {
  }

  public void onError(Throwable e) {
    e.printStackTrace();
  }
}
