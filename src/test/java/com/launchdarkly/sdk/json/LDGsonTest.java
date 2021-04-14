package com.launchdarkly.sdk.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("javadoc")
public class LDGsonTest {
  // Note that these unit tests don't fully prove that our Gson integration works as intended
  // in SDK distributions that shade the Gson classes, because the tests for this project are
  // run on the unmodified code with real Gson classes in the classpath. SDKs that use shading
  // must implement their own unit tests, run against SDK code that has had shading applied,
  // to verify that these methods still work in that environment. However, those tests do not
  // need to be as detailed as these in terms of covering all of the various JSON types; if
  // the methods work for anything, they should work for everything, since the issue there is
  // just whether the correct Gson package names are being used.
  
  @Test
  public void valueToJsonElement() {
    verifyValueToJsonElement(LDValue.ofNull());
    verifyValueToJsonElement(LDValue.of(true));
    verifyValueToJsonElement(LDValue.of(false));
    verifyValueToJsonElement(LDValue.of("x"));
    verifyValueToJsonElement(LDValue.of("say \"hello\""));
    verifyValueToJsonElement(LDValue.of(2));
    verifyValueToJsonElement(LDValue.of(2.5f));
    verifyValueToJsonElement(JsonTestHelpers.nestedArrayValue());
    verifyValueToJsonElement(JsonTestHelpers.nestedObjectValue());
    assertEquals(JsonNull.INSTANCE, LDGson.valueToJsonElement(null));
  }
  
  @Test
  public void valueMapToJsonElementMap() {
    Map<String, LDValue> m1 = new HashMap<>();
    m1.put("a", LDValue.of(true));
    m1.put("b", LDValue.of(1));
    String js1 = JsonSerialization.gson.toJson(m1);
    
    Map<String, JsonElement> m2 = LDGson.valueMapToJsonElementMap(m1);
    String js2 = JsonSerialization.gson.toJson(m2);
    JsonTestHelpers.assertJsonEquals(js1, js2);
  }
  
  static void verifyValueSerialization(LDValue value) {
    verifyValueToJsonElement(value);
    
    JsonElement j2 = JsonTestHelpers.configureGson().toJsonTree(value);
    String js2 = JsonSerialization.gson.toJson(j2);
    JsonTestHelpers.assertJsonEquals(value.toJsonString(), js2);
  }
  
  static void verifyValueToJsonElement(LDValue value) {
    JsonElement j1 = LDGson.valueToJsonElement(value);
    String js1 = JsonSerialization.gson.toJson(j1);
    JsonTestHelpers.assertJsonEquals(value.toJsonString(), js1);
  }
}
