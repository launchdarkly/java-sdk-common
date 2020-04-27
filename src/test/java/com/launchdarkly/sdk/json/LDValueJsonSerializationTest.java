package com.launchdarkly.sdk.json;

import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import static com.launchdarkly.sdk.json.JsonTestHelpers.parseElement;
import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerialize;
import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerializeAndDeserialize;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("javadoc")
public class LDValueJsonSerializationTest {
  @Test
  public void jsonEncodingForNull() throws Exception {
    verifySerialize(LDValue.ofNull(), "null");
  }
  
  @Test
  public void jsonEncodingForNonNullValues() throws Exception {
    verifyValueSerialization(LDValue.of(true), "true");
    verifyValueSerialization(LDValue.of(false), "false");
    verifyValueSerialization(LDValue.of("x"), "\"x\"");
    verifyValueSerialization(LDValue.of("say \"hello\""), "\"say \\\"hello\\\"\"");
    verifyValueSerialization(LDValue.of(2), "2");
    verifyValueSerialization(LDValue.of(2.5f), "2.5");
    verifyValueSerialization(LDValue.of(2.5d), "2.5");
    verifyValueSerialization(LDValue.buildArray().add(2).add("x").build(), "[2,\"x\"]");
    verifyValueSerialization(LDValue.buildObject().put("x", 2).build(), "{\"x\":2}");
  }
  
  private static void verifyValueSerialization(LDValue value, String expectedJsonString) throws Exception {
    verifySerializeAndDeserialize(value, expectedJsonString);
    assertEquals(parseElement(expectedJsonString), parseElement(value.toJsonString()));
    assertEquals(value, LDValue.parse(expectedJsonString));
  }
}
