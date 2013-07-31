/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.crypto.Cipher;

import com.google.gson.JsonElement;

public class CommandGetFile extends Command<File> {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CommandGetFile.class.getSimpleName());

  public static class Response {
    public long s;
    public String at;
    public String g;
    public String fa;
    public int pfa;
  }

  Response response;
  Node node;
  File file;

  public CommandGetFile(Node node, File file) {
    super("g");
    this.node = node;
    this.file = file;
    addArg("n", node.getHandle());
    addArg("g", 1);

  }

  @Override
  public void processResponse(JsonElement e) throws Exception {

    response = GSONUtil.getGSON().fromJson(e, Response.class);
    MegaCrypto crypto = MegaCrypto.get();
    byte bkey[] = node.getDecodedKey();
    if (bkey == null)
      throw new Exception("Node has no decoded key");

    byte new_key[] = new byte[16];
    for (int j = 0; j < new_key.length; j++) {
      new_key[j] = (byte) (bkey[j] ^ bkey[j + 16]);
    }

    byte iv[] = new byte[] { bkey[16], bkey[17], bkey[18], bkey[19], bkey[20],
        bkey[21], bkey[22], bkey[23], 0, 0, 0, 0, 0, 0, 0, 0 };
    Cipher dl_aes = crypto.createCipherCTR(new_key, Cipher.DECRYPT_MODE, iv);

    URLConnection conn = new URL(response.g).openConnection();
    InputStream input = conn.getInputStream();
    FileOutputStream output = new FileOutputStream(file);
    byte buf[] = new byte[1024 * 8];
    int c = 0;
    byte plaintext[] = null;
    while ((c = input.read(buf)) != -1) {
      plaintext = dl_aes.update(buf, 0, c);
      if (plaintext.length > 0)
        output.write(plaintext, 0, plaintext.length);
    }

    plaintext = dl_aes.doFinal();
    if (plaintext.length > 0)
      output.write(plaintext, 0, plaintext.length);

    output.close();
    log.info("saved to {}", file);

    onResult(file);
  }

  public Response getResponse() {
    return response;
  }

}
