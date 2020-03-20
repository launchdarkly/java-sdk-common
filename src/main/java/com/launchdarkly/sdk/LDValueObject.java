package com.launchdarkly.sdk;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

@JsonAdapter(LDValueTypeAdapter.class)
final class LDValueObject extends LDValue {
  private static final LDValueObject EMPTY = new LDValueObject(emptyMap());
  private final Map<String, LDValue> map;
  
  static LDValueObject fromMap(Map<String, LDValue> map) {
    return map.isEmpty() ? EMPTY : new LDValueObject(map);
  }
  
  private LDValueObject(Map<String, LDValue> map) {
    this.map = map;
  }
  
  public LDValueType getType() {
    return LDValueType.OBJECT;
  }
  
  @Override
  public int size() {
    return map.size();
  }
  
  @Override
  public Iterable<String> keys() {
    return map.keySet();
  }
  
  @Override
  public Iterable<LDValue> values() {
    return map.values();
  }
  
  @Override
  public LDValue get(String name) {
    LDValue v = map.get(name);
    return v == null ? ofNull() : v;
  }

  @Override
  void write(JsonWriter writer) throws IOException {
    writer.beginObject();
    for (Map.Entry<String, LDValue> e: map.entrySet()) {
      writer.name(e.getKey());
      e.getValue().write(writer);
    }
    writer.endObject();
  }
}
