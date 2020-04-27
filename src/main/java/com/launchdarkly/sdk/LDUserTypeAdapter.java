package com.launchdarkly.sdk;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

final class LDUserTypeAdapter extends TypeAdapter<LDUser>{
  static final LDUserTypeAdapter INSTANCE = new LDUserTypeAdapter();
  
  @Override
  public LDUser read(JsonReader reader) throws IOException {
    LDUser.Builder builder = new LDUser.Builder((String)null);
    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      String key = reader.nextName();
      switch (key) {
      case "key":
        builder.key(reader.nextString());
        break;
      case "secondary":
        builder.secondary(reader.nextString());
        break;
      case "ip":
        builder.ip(reader.nextString());
        break;
      case "email":
        builder.email(reader.nextString());
        break;
      case "name":
        builder.name(reader.nextString());
        break;
      case "avatar":
        builder.avatar(reader.nextString());
        break;
      case "firstName":
        builder.firstName(reader.nextString());
        break;
      case "lastName":
        builder.lastName(reader.nextString());
        break;
      case "country":
        builder.country(reader.nextString());
        break;
      case "anonymous":
        builder.anonymous(reader.nextBoolean());
        break;
      case "custom":
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
        } else {
          reader.beginObject();
          while (reader.peek() != JsonToken.END_OBJECT) {
            String customKey = reader.nextName();
            LDValue customValue = LDValueTypeAdapter.INSTANCE.read(reader);
            builder.custom(customKey, customValue);
          }
          reader.endObject();
        }
        break;
      case "privateAttributeNames":
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
        } else {
          reader.beginArray();
          while (reader.peek() != JsonToken.END_ARRAY) {
            String name = reader.nextString();
            builder.addPrivate(UserAttribute.forName(name));
          }
          reader.endArray();
        }
        break;
      default:
        LDValueTypeAdapter.INSTANCE.read(reader);
      }
    }
    reader.endObject();
    return builder.build();
  }

  @Override
  public void write(JsonWriter writer, LDUser user) throws IOException {
    // Currently, the field layout of LDUser does match the JSON representation, so Gson's default
    // reflection mechanism would work, but we've implemented serialization manually here to avoid
    // relying on that implementation detail and also to reduce the overhead of reflection.
    //
    // Note that this is not the serialization we use in analytics events; the SDK has a different
    // custom serializer for that, in order to implement the private attribute redaction logic.
    // The logic here is for serializing LDUser in the format that is used when you pass a user to
    // the SDK as an *input*, i.e. if you are passing it to front-end JS code.

    writer.beginObject();
    for (UserAttribute attr: UserAttribute.BUILTINS.values()) {
      LDValue value = user.getAttribute(attr);
      if (value != null && !value.isNull()) {
        writer.name(attr.getName());
        LDValueTypeAdapter.INSTANCE.write(writer, value);
      }
    }
    boolean hasCustom = false;
    for (UserAttribute attr: user.getCustomAttributes()) {
      if (!hasCustom) {
        hasCustom = true;
        writer.name("custom");
        writer.beginObject();
      }
      writer.name(attr.getName());
      LDValueTypeAdapter.INSTANCE.write(writer, user.getAttribute(attr));
    }
    if (hasCustom) {
      writer.endObject();
    }
    boolean hasPrivate = false;
    for (UserAttribute attr: user.getPrivateAttributes()) {
      if (!hasPrivate) {
        hasPrivate = true;
        writer.name("privateAttributeNames");
        writer.beginArray();
      }
      writer.value(attr.getName());
    }
    if (hasPrivate) {
      writer.endArray();
    }
    writer.endObject();
  }
}
