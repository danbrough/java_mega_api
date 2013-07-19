package org.danbrough.mega;

import org.danbrough.mega.AccountDetails.UserType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CommandGetUserQuota extends Command {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(CommandGetUserQuota.class.getSimpleName());

  private AccountDetails details;

  public CommandGetUserQuota(MegaClient megaClient, AccountDetails details,
      boolean storage, boolean transfer, boolean pro) {
    super("uq");
    this.details = details;

    if (storage)
      addArg("strg", 1);
    if (transfer)
      addArg("xfer", 1);
    if (pro)
      addArg("pro", 1);
  }

  @Override
  public void processResponse(JsonElement e) throws Exception {
    JsonObject o = e.getAsJsonObject();
    if (o.has("cstrg")) {
      details.setStorageUsed(o.get("cstrg").getAsInt());
    }

    if (o.has("mstrg")) {
      details.setTotalStorage(o.get("mstrg").getAsInt());
    }

    // case MAKENAMEID6('c','a','x','f','e','r') : // own transfer quota used
    if (o.has("caxfer")) {
      details.setTransferUsed(o.get("caxfer").getAsInt());
    }

    // * case MAKENAMEID6('c','s','x','f','e','r') : // third-party transfer
    // quota
    if (o.has("csxfer")) {
      details.setThirdPartyTransfer(o.get("csxfer").getAsInt());
    }

    // * case MAKENAMEID5('m','x','f','e','r') : // total transfer quota

    if (o.has("mxfer")) {
      details.setTotalTransfer(o.get("mxfer").getAsInt());
    }

    // * case MAKENAMEID8('s','r','v','r','a','t','i','o') : // percentage of
    // * transfer allocated for serving details->srv_ratio =
    // * client->json.getfloat(); break;
    // *

    if (o.has("srvratio")) {
      details.setServerRatio(o.get("srvratio").getAsFloat());
    }

    // * case MAKENAMEID5('u','t','y','p','e') : // Pro level (0 == none)
    // * details->pro_level = client->json.getint(); got_pro = 1; break;
    if (o.has("utype")) {
      details.setUserType(UserType.get(o.get("utype").getAsInt()));
    }

    // * case MAKENAMEID5('s','t','y','p','e') : // subscription type const
    // char*
    // * ptr; if ((ptr = client->json.getvalue())) details->subscription_type =
    // * *ptr; break;
    if (o.has("stype")) {
      details.setSubscriptionType(o.get("stype").getAsString());
    }

    // * case MAKENAMEID6('s','u','n','t','i','l') : // Pro level until
    // * details->pro_until = client->json.getint(); break;
    // *

    if (o.has("suntil")) {
      details.setsUntil(o.get("suntil").getAsInt());
    }

    /*
     * case MAKENAMEID7('b','a','l','a','n','c','e') : // account balances if
     * (client->json.enterarray()) { const char* cur; const char* amount;
     * 
     * while (client->json.enterarray()) { if ((amount =
     * client->json.getvalue()) && (cur = client->json.getvalue())) { int t =
     * details->balances.size(); details->balances.resize(t + 1);
     * details->balances[t].amount = atof(amount);
     * memcpy(details->balances[t].currency, cur, 3); }
     * 
     * client->json.leavearray(); }
     * 
     * client->json.leavearray(); } break;
     */

    if (o.has("balance")) {
      details.setBalance(o.get("balance").getAsJsonArray());
    }
  }
}
