package com.launchdarkly.sdk.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import static org.junit.Assert.assertEquals;

public abstract class JsonTestHelpers {
  // Note that when we verify the behavior of Gson with GsonTypeAdapters in this project's unit tests,
  // that is not an adequate test for whether the adapters will work in the Java SDK where there is
  // the additional issue of Gson types being shaded. The Java SDK project must do its own basic tests
  // of Gson interoperability using the shaded SDK jar. But the tests in this project still prove that
  // the adapters work correctly if Gson actually uses them.
  
  public static <T extends JsonSerializable> void verifySerializeAndDeserialize(T instance, String expectedJsonString) throws Exception {
    verifySerialize(instance, expectedJsonString);
    verifyDeserialize(instance, expectedJsonString);
  }
  
  public static <T extends JsonSerializable> void verifySerialize(T instance, String expectedJsonString) throws Exception {
    // All subclasses of Gson's JsonElement implement deep equality for equals(). So does our own LDValue,
    // but since some of our tests are testing LDValue itself, we can't assume that its behavior is correct. 
    assertEquals(parseElement(expectedJsonString), parseElement(JsonSerialization.serialize(instance)));
    
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonTypeAdapters()).create();
    assertEquals(parseElement(expectedJsonString), parseElement(gson.toJson(instance)));
  }

  @SuppressWarnings("unchecked")
  public static <T extends JsonSerializable> void verifyDeserialize(T instance, String expectedJsonString) throws Exception {
    T instance1 = JsonSerialization.deserialize(expectedJsonString, (Class<T>)instance.getClass());
    assertEquals(instance, instance1);
    
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonTypeAdapters()).create();
    T instance2 = gson.fromJson(expectedJsonString, (Class<T>)instance.getClass());
    assertEquals(instance, instance2);
  }

  static JsonElement parseElement(String jsonString) {
    return JsonSerialization.gson.fromJson(jsonString, JsonElement.class);
  }
}
