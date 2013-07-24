package org.danbrough.mega;

import java.lang.reflect.Type;

import org.danbrough.mega.Node.AccessLevel;
import org.danbrough.mega.Node.NodeType;
import org.danbrough.mega.User.Visibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GSONUtil {

  private static class NodeTypeSerializer implements JsonSerializer<NodeType>,
      JsonDeserializer<NodeType> {
    @Override
    public JsonElement serialize(NodeType src, Type typeOfSrc,
        JsonSerializationContext context) {
      return new JsonPrimitive(src.getValue());
    }

    @Override
    public NodeType deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return NodeType.get(json.getAsInt());
    }
  }

  private static class AccessLevelSerializer implements
      JsonSerializer<AccessLevel>, JsonDeserializer<AccessLevel> {

    @Override
    public AccessLevel deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return AccessLevel.get(json.getAsInt());
    }

    @Override
    public JsonElement serialize(AccessLevel src, Type typeOfSrc,
        JsonSerializationContext context) {
      return new JsonPrimitive(src.getValue());
    }

  }

  private static class VisibilitySerializer implements
      JsonSerializer<Visibility>, JsonDeserializer<Visibility> {

    @Override
    public Visibility deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return Visibility.get(json.getAsInt());
    }

    @Override
    public JsonElement serialize(Visibility src, Type typeOfSrc,
        JsonSerializationContext context) {
      return new JsonPrimitive(src.getValue());
    }

  }

  private static GsonBuilder builder = null;

  public static GsonBuilder getGSONBuilder() {
    if (builder != null)
      return builder;
    builder = new GsonBuilder().serializeNulls()
        .registerTypeAdapter(NodeType.class, new NodeTypeSerializer())
        .registerTypeAdapter(AccessLevel.class, new AccessLevelSerializer())
        .registerTypeAdapter(Visibility.class, new VisibilitySerializer());
    return builder;
  }

  public static Gson getGSON() {
    return getGSONBuilder().create();
  }
}
