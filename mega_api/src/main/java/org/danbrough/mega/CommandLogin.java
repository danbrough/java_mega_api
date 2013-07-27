package org.danbrough.mega;

import java.math.BigInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CommandLogin extends Command<Void> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CommandLogin.class.getSimpleName());

  MegaClient client;
  String password;

  public CommandLogin(MegaClient client, String password,
      Callback<Void> callback) {
    super("us", callback);
    this.client = client;
    this.password = password;
    addArg("user", client.getEmail());
  }

  @Override
  public JsonObject getPayload() {
    MegaCrypto crypto = MegaCrypto.get();
    byte passwordKey[] = crypto.prepareKey(password);
    client.setPasswordKey(passwordKey);
    String uh = crypto.stringhash(client.getEmail(), passwordKey);
    addArg("uh", uh);
    return super.getPayload();
  }

  @Override
  public void processResponse(JsonElement e) throws Exception {
    log.debug("processResponse() {}", e);

    MegaCrypto crypto = MegaCrypto.get();

    JsonObject o = e.getAsJsonObject();
    byte[] sid = crypto.base64urldecode(o.get("csid").getAsString());
    byte[] k = crypto.base64urldecode(o.get("k").getAsString());
    byte[] privk = crypto.base64urldecode(o.get("privk").getAsString());

    log.trace("len_k " + k.length + " csid.length " + sid.length
        + " privk.length " + privk.length);

    if (k.length != MegaCrypto.SYMM_CIPHER_KEY_LENGTH)
      throw new Exception("Invalid length for k " + k.length + " != "
          + MegaCrypto.SYMM_CIPHER_KEY_LENGTH);
    if (sid.length < 32)
      throw new Exception("Length csid < 32");
    if (privk.length < 256)
      throw new Exception("Length privk < 256");

    byte masterKey[] = crypto.decrypt_key(k, client.getPasswordKey());
    log.trace("masterKey: {}", crypto.base64urlencode(masterKey));

    client.setMasterKey(masterKey);

    privk = crypto.decrypt_key(privk, masterKey);

    BigInteger rsa_private_key[] = { BigInteger.ZERO, BigInteger.ZERO,
        BigInteger.ZERO, BigInteger.ZERO };

    for (int i = 0; i < 4; i++) {
      // l = ((ord(private_key[0]) * 256 + ord(private_key[1]) + 7) >> 3) + 2
      int l = ((0x0000ff00 & (privk[0] << 8) + (privk[1] & 0x00ff) + 7) >> 3) + 2;
      log.debug("l: " + l);
      byte b[] = new byte[l];
      System.arraycopy(privk, 0, b, 0, b.length);

      rsa_private_key[i] = crypto.mpi2big(b);
      // log.debug("rsa_private_key: " + i + " = " + crypto.toHex(b));
      // crypto.toHex(rsa_private_key[i].toByteArray()));
      // Assert.assertEquals(example.rsa_private_keys[i],
      // crypto.bigToString(rsa_private_key[i]));
      // log.debug("first ok at: " + i);

      b = new byte[privk.length - l];
      System.arraycopy(privk, l, b, 0, b.length);
      privk = b;

    }

    // ctx.setRsaPrivateKey(rsa_private_key);
    client.setPrivateKey(rsa_private_key);

    BigInteger encrypted_sid = crypto.mpi2big(sid);

    BigInteger bResult = crypto.rsaDecrypt(encrypted_sid, rsa_private_key[2],
        rsa_private_key[0], rsa_private_key[1], rsa_private_key[3]);
    String sResult = crypto.bigToString(bResult);

    if (sResult.charAt(0) == 0)
      sResult = sResult.substring(1);

    String sSid = crypto.base64urlencode(crypto.fromHex(sResult
        .substring(0, 86)));
    log.warn("sid: {}", sSid);
    // ctx.setSid(sid);

    client.setSessionID(sSid);

    onResult(null);

  }
}
