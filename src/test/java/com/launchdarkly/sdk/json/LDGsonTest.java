package com.launchdarkly.sdk.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
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
    verifyValueSerialization(LDValue.ofNull());
    verifyValueSerialization(LDValue.of(true));
    verifyValueSerialization(LDValue.of(false));
    verifyValueSerialization(LDValue.of("x"));
    verifyValueSerialization(LDValue.of("say \"hello\""));
    verifyValueSerialization(LDValue.of(2));
    verifyValueSerialization(LDValue.of(2.5f));
    verifyValueSerialization(JsonTestHelpers.nestedArrayValue());
    verifyValueSerialization(JsonTestHelpers.nestedObjectValue());
    assertEquals(JsonNull.INSTANCE, LDGson.valueToJsonElement(null));
  }
  
  @Test
  public void valueMapToJsonElementMap() {
    Map<String, LDValue> m1 = new HashMap<>();
    m1.put("a", LDValue.of(true));
    m1.put("b", LDValue.of(1));
    String js1 = JsonTestHelpers.gson.toJson(m1);
    
    Map<String, JsonElement> m2 = LDGson.valueMapToJsonElementMap(m1);
    String js2 = JsonTestHelpers.gson.toJson(m2);
    JsonTestHelpers.assertJsonEquals(js1, js2);
  }
  
  @Test
  public void complexObjectToJsonTree() {
    LDUser user = new LDUser.Builder("userkey").name("name")
        .custom("attr1", LDValue.ofNull())
        .custom("atrt2", LDValue.of(true))
        .custom("attr3", LDValue.of(false))
        .custom("attr4", LDValue.of(0))
        .custom("attr5", LDValue.of(1))
        .custom("attr6", LDValue.of(""))
        .custom("attr7", LDValue.of("x"))
        .custom("attr8", JsonTestHelpers.nestedArrayValue())
        .custom("attr9", JsonTestHelpers.nestedObjectValue())
        .build();
    JsonElement j = JsonTestHelpers.configureGson().toJsonTree(user);
    String js = JsonTestHelpers.gson.toJson(j);
    assertEquals(LDValue.parse(JsonSerialization.serialize(user)), LDValue.parse(js));
  }
  
  @Test
  public void testInternalReaderAdapter() throws Exception {
    // This and testInternalWriterAdapter verify that all of our reader/writer delegation
    // methods work as expected, regardless of whether or not they are exercised indirectly
    // by our other unit tests.
    String json = "[null,false,true,1,2,3,\"x\",{\"a\":false}]";
    try (StringReader sr = new StringReader(json)) {
      try (JsonReader jr0 = new JsonReader(sr)) {
        try (JsonReader jr = new LDGson.DelegatingJsonReaderAdapter(jr0)) {
          jr.beginArray();
          assertEquals(true, jr.hasNext());
          jr.nextNull();
          assertEquals(JsonToken.BOOLEAN, jr.peek());
          jr.skipValue();
          assertEquals(true, jr.nextBoolean());
          assertEquals(1d, jr.nextDouble(), 0);
          assertEquals(2, jr.nextInt());
          assertEquals(3, jr.nextLong());
          assertEquals("x", jr.nextString());
          jr.beginObject();
          assertEquals("a", jr.nextName());
          assertEquals(false, jr.nextBoolean());
          jr.endObject();
          jr.endArray();
        }
      }
    }
  }
  
  @Test
  public void testInternalWriterAdapter() throws Exception {
    try (StringWriter sw = new StringWriter()) {
      try (JsonWriter jw0 = new JsonWriter(sw)) {
        try (JsonWriter jw = new LDGson.DelegatingJsonWriterAdapter(jw0)) {
          jw.beginArray();
          jw.nullValue();
          jw.value(true);
          jw.value(Boolean.valueOf(true));
          jw.value((Boolean)null);
          jw.value((double)1);
          jw.value((long)2);
          jw.value(Float.valueOf(3));
          jw.value("x");
          jw.beginObject();
          jw.name("a");
          jw.value(false);
          jw.endObject();
          jw.jsonValue("123");
          jw.endArray();
          jw.flush();
        }
      }
      String expected = "[null,true,true,null,1,2,3,\"x\",{\"a\":false},123]";
      JsonTestHelpers.assertJsonEquals(expected, sw.toString());
    }
  }
  
  static void verifyValueSerialization(LDValue value) {
    JsonElement j1 = LDGson.valueToJsonElement(value);
    String js1 = JsonTestHelpers.gson.toJson(j1);
    JsonTestHelpers.assertJsonEquals(value.toJsonString(), js1);
    
    JsonElement j2 = JsonTestHelpers.configureGson().toJsonTree(value);
    String js2 = JsonTestHelpers.gson.toJson(j2);
    JsonTestHelpers.assertJsonEquals(value.toJsonString(), js2);
  }
}
