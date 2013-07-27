package org.danbrough.mega;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class AccountDetails {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(AccountDetails.class.getSimpleName());

  public enum UserType {
    NORMAL(0), PROFESSIONAL(1);
    private int value;

    private UserType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static UserType get(int value) {
      for (UserType type : values())
        if (type.value == value)
          return type;
      return null;
    }
  }

  public static class UserSession {
    int timeStamp;
    int mru;
    String userAgent;
    String ip;
    String country;
    int current;

    public UserSession(JsonArray a) {
      int i = 0;
      timeStamp = a.get(i++).getAsInt();
      mru = a.get(i++).getAsInt();
      userAgent = a.get(i++).getAsString();
      ip = a.get(i++).getAsString();
      country = a.get(i++).getAsString();
      current = a.get(i++).getAsInt();
    }

    public int getTimeStamp() {
      return timeStamp;
    }

    public int getMru() {
      return mru;
    }

    public String getUserAgent() {
      return userAgent;
    }

    public String getIP() {
      return ip;
    }

    public String getCountry() {
      return country;
    }

    public int getCurrent() {
      return current;
    }

    @Override
    public String toString() {
      return "UserSession[timeStamp:" + timeStamp + ",mru:" + mru
          + ",userAgent:" + userAgent + ",ip:" + ip + ",country:" + country
          + ",current:" + current + "]";
    }

  }

  public static class BalanceEntry {
    String amount;
    String currency;

    public BalanceEntry(String amount, String currency) {
      super();
      this.amount = amount;
      this.currency = currency;
    }

    public String getAmount() {
      return amount;
    }

    public String getCurrency() {
      return currency;
    }

  }

  List<UserSession> sessions = new LinkedList<UserSession>();
  private long storageUsed;

  // total storage quota
  private long totalStorage;
  private long transferUsed;
  private long thirdPartyTransfer;
  private long totalTransfer;

  // percentage of transfer allocated for serving

  private float serverRatio;

  private UserType userType;

  private String subscriptionType;

  // // Pro level until
  private int sUntil = 0;

  private LinkedList<BalanceEntry> balance = new LinkedList<BalanceEntry>();

  public List<UserSession> getSessions() {
    return sessions;
  }

  public void setSessions(JsonArray a) {
    sessions.clear();
    for (int i = 0; i < a.size(); i++) {
      JsonArray aa = a.get(i).getAsJsonArray();
      sessions.add(new UserSession(aa));
    }
  }

  public void setStorageUsed(long storageUsed) {
    this.storageUsed = storageUsed;
  }

  public long getStorageUsed() {
    return storageUsed;
  }

  public void setTotalStorage(long totalStorage) {
    this.totalStorage = totalStorage;
  }

  public long getTotalStorage() {
    return totalStorage;
  }

  public void setTransferUsed(long transferUsed) {
    this.transferUsed = transferUsed;
  }

  public long getTransferUsed() {
    return transferUsed;
  }

  public void setThirdPartyTransfer(long thirdPartyTransfer) {
    this.thirdPartyTransfer = thirdPartyTransfer;
  }

  public long getThirdPartyTransfer() {
    return thirdPartyTransfer;
  }

  public void setTotalTransfer(long totalTransfer) {
    this.totalTransfer = totalTransfer;
  }

  public long getTotalTransfer() {
    return totalTransfer;
  }

  public void setServerRatio(float serverRatio) {
    this.serverRatio = serverRatio;
  }

  public float getServerRatio() {
    return serverRatio;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public UserType getUserType() {
    return userType;
  }

  public String getSubscriptionType() {
    return subscriptionType;
  }

  public void setSubscriptionType(String subscriptionType) {
    this.subscriptionType = subscriptionType;
  }

  public int getsUntil() {
    return sUntil;
  }

  public void setsUntil(int sUntil) {
    this.sUntil = sUntil;
  }

  public void setBalance(JsonArray a) {
    balance.clear();
    for (int i = 0; i < a.size(); i++) {
      JsonArray aa = a.get(i).getAsJsonArray();
      String amount = aa.get(0).getAsString();
      String currency = aa.get(1).getAsString();
      balance.add(new BalanceEntry(amount, currency));
    }
  }

  public String toString() {
    try {
      return GSONUtil.getGSON().toJson(this);
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

}
