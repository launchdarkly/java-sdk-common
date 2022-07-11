package com.launchdarkly.sdk;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

final class AttributeRefTypeAdapter extends TypeAdapter<AttributeRef> {
  @Override
  public AttributeRef read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      return null;
    }
    if (reader.peek() != JsonToken.STRING) {
      // We have to do this because Gson's nextString() does not do strict type checking
      throw new JsonParseException("expected string, got " + reader.peek());
    }
    return AttributeRef.fromPath(reader.nextString());
  }

  @Override
  public void write(JsonWriter writer, AttributeRef a) throws IOException {
    writer.value(a.toString());
  }
}
