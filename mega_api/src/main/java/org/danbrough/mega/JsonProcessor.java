/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonProcessor {

  public void parse(JsonReader reader) throws IOException {
    onStartDocument();
    while (true) {
      JsonToken token = reader.peek();
      switch (token) {
      case BEGIN_ARRAY:
        reader.beginArray();
        onBeginArray();
        break;
      case END_ARRAY:
        reader.endArray();
        onEndArray();
        break;
      case BEGIN_OBJECT:
        reader.beginObject();
        onBeginObject();
        break;
      case END_OBJECT:
        reader.endObject();
        onEndObject();
        break;
      case NAME:
        String name = reader.nextName();
        onName(name);
        break;
      case STRING:
        String s = reader.nextString();
        onString(s);
        break;
      case NUMBER:
        String n = reader.nextString();
        onNumber(new BigDecimal(n));
        break;
      case BOOLEAN:
        boolean b = reader.nextBoolean();
        onBoolean(b);
        break;
      case NULL:
        reader.nextNull();
        onNull();
        break;
      case END_DOCUMENT:
        onEndDocument();
        return;
      }
    }
  }

  public void onStartDocument() {
  }

  public void onBeginObject() {
  }

  public void onEndObject() {
  }

  public void onBeginArray() {
  }

  public void onEndArray() {
  }

  public void onName(String name) {
  }

  public void onNumber(BigDecimal n) {
  }

  public void onString(String s) {
  }

  public void onNull() {
  }

  public void onEndDocument() {
  }

  public void onBoolean(boolean b) {
  }

}
