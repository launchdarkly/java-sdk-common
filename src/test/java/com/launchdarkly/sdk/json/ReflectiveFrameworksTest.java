package com.launchdarkly.sdk.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.launchdarkly.sdk.json.JsonTestHelpers.assertJsonEquals;
import static com.launchdarkly.sdk.json.JsonTestHelpers.configureGson;
import static com.launchdarkly.sdk.json.JsonTestHelpers.configureJacksonMapper;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("javadoc")
public class ReflectiveFrameworksTest {
  // Test classes like LDValueJsonSerializationTest already cover using all available JSON
  // frameworks to serialize and deserialize instances of our classes. This one tests the
  // ability of Gson and Jackson, when properly configured, to get the right serialization
  // or deserialization reflectively when we do not specify the desired class up front -
  // that is, when one of our types is used inside another data structure.
  //
  // Since we've already verified the serializations for each of our types separately, we
  // can just use LDValue here and assume that it would work the same for other types.
  
  private static final LDValue TOP_LEVEL_VALUE = LDValue.of("x");
  private static final String EXPECTED_JSON =
      "{\"topLevelValue\":\"x\",\"mapOfValues\":{\"a\":1,\"b\":[2,3]}}";
  
  @Test
  public void gsonSerializesTypeContainingOurType() {
    ObjectContainingValues o = new ObjectContainingValues(TOP_LEVEL_VALUE, makeMapOfValues());
    assertJsonEquals(EXPECTED_JSON, configureGson().toJson(o));
  }
  
  @Test
  public void gsonDeserializesTypeContainingOurTypes() {
    ObjectContainingValues o = configureGson().fromJson(EXPECTED_JSON, ObjectContainingValues.class);
    assertEquals(TOP_LEVEL_VALUE, o.topLevelValue);
    assertEquals(makeMapOfValues(), o.mapOfValues);
  }
  
  @Test
  public void jacksonSerializesTypeContainingOurType() throws Exception {
    ObjectContainingValues o = new ObjectContainingValues(TOP_LEVEL_VALUE, makeMapOfValues());
    assertJsonEquals(EXPECTED_JSON, configureJacksonMapper().writeValueAsString(o));
  }

  @Test
  public void jacksonDeserializesTypeContainingOurTypes() throws Exception {
    ObjectContainingValues o = configureJacksonMapper().readValue(EXPECTED_JSON, ObjectContainingValues.class);
    assertEquals(TOP_LEVEL_VALUE, o.topLevelValue);
    assertEquals(makeMapOfValues(), o.mapOfValues);
  }
  
  private static Map<String, LDValue> makeMapOfValues() {
    Map<String, LDValue> m = new HashMap<>();
    m.put("a", LDValue.of(1));
    m.put("b", LDValue.buildArray().add(2).add(3).build());
    return m;
  }
  
  private static final class ObjectContainingValues {
    public LDValue topLevelValue;
    public Map<String, LDValue> mapOfValues;
    
    @JsonCreator
    public ObjectContainingValues(@JsonProperty("topLevelValue") LDValue topLevelValue,
        @JsonProperty("mapOfValues") Map<String, LDValue> mapOfValues) {
      this.topLevelValue = topLevelValue;
      this.mapOfValues = mapOfValues;
    }
  }
}
