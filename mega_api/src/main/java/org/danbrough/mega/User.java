package org.danbrough.mega;

import com.google.gson.annotations.SerializedName;

public class User {

  public enum Visibility {

    VISIBILITY_UNKNOWN(-1), HIDDEN(0), VISIBLE(1), ME(2);

    int value;

    private Visibility(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static Visibility get(int value) {
      for (Visibility v : values())
        if (v.value == value)
          return v;
      return null;
    }
  }

  @SerializedName("u")
  String handle;

  @SerializedName("c")
  Visibility visibility;

  @SerializedName("ts")
  int timeStamp;

  @SerializedName("m")
  String email;

  public User() {
    super();
  }

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public int getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(int timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return GSONUtil.getGSON().toJson(this);
  }

}
