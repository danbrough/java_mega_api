package org.danbrough.mega;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class AccountDetails {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(AccountDetails.class.getSimpleName());

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

  // while (client->json.enterarray()) {
  // int t = details->sessions.size();
  // details->sessions.resize(t + 1);
  //
  // details->sessions[t].timestamp = client->json.getint();
  // details->sessions[t].mru = client->json.getint();
  // client->json.storestring(&details->sessions[t].useragent);
  //
  // client->json.storestring(&details->sessions[t].ip);
  //
  // const char* country = client->json.getvalue();
  // memcpy(details->sessions[t].country, country ? country : "\0\0", 2);
  //
  // details->sessions[t].current = client->json.getint();
  //
  // client->json.leavearray();
  // }
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
  private int storageUsed;

  // total storage quota
  private int totalStorage;
  private int transferUsed;
  private int thirdPartyTransfer;
  private int totalTransfer;

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

  public void setStorageUsed(int storageUsed) {
    this.storageUsed = storageUsed;
  }

  public int getStorageUsed() {
    return storageUsed;
  }

  public void setTotalStorage(int totalStorage) {
    this.totalStorage = totalStorage;
  }

  public int getTotalStorage() {
    return totalStorage;
  }

  public void setTransferUsed(int transferUsed) {
    this.transferUsed = transferUsed;
  }

  public int getTransferUsed() {
    return transferUsed;
  }

  public void setThirdPartyTransfer(int thirdPartyTransfer) {
    this.thirdPartyTransfer = thirdPartyTransfer;
  }

  public int getThirdPartyTransfer() {
    return thirdPartyTransfer;
  }

  public void setTotalTransfer(int totalTransfer) {
    this.totalTransfer = totalTransfer;
  }

  public int getTotalTransfer() {
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

}
