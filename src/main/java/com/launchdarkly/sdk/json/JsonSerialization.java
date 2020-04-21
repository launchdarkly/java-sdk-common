package com.launchdarkly.sdk.json;

import com.google.gson.Gson;
import com.launchdarkly.sdk.LDValue;

/**
 * Helper methods for JSON serialization of SDK classes.
 * <p>
 * While the LaunchDarkly Java-based SDKs have used <a href="https://github.com/google/gson">Gson</a>
 * internally in the past, they may not always do so-- and even if they do, some SDK distributions may
 * embed their own copy of Gson with modified (shaded) class names so that it does not conflict with
 * any Gson instance elsewhere in the classpath. For both of those reasons, applications should not
 * assume that {@code Gson.toGson()} and {@code Gson.fromGson()}-- or any other JSON framework that is
 * based on reflection-- will work correctly for SDK classes, whose correct JSON representations do
 * not necessarily correspond to their internal field layout. Instead, they should always use one of
 * the following:
 * <ol>
 * <li> The {@link JsonSerialization} methods.
 * <li> A Gson instance that has been configured with {@link GsonTypeAdapters}.
 * <li> For {@link LDValue}, you may also use the convenience methods {@link LDValue#toJsonString()} and
 * {@link LDValue#parse(String)}.
 * </ol>
 */
public abstract class JsonSerialization {
  static final Gson gson = new Gson();
  
  /**
   * Converts an object to its JSON representation.
   * <p>
   * This is only usable for classes that have the {@link JsonSerializable} marker interface,
   * indicating that the SDK knows how to serialize them.
   * 
   * @param <T> class of the object being serialized
   * @param instance the instance to serialize
   * @return the object's JSON encoding as a string
   */
  public static <T extends JsonSerializable> String serialize(T instance) {
    return serializeInternal(instance);
  }
  
  // We use this internally in situations where generic type checking isn't desirable
  static String serializeInternal(Object instance) {
    return gson.toJson(instance);
  }
  
  /**
   * Parses an object from its JSON representation.
   * <p>
   * This is only usable for classes that have the {@link JsonSerializable} marker interface,
   * indicating that the SDK knows how to serialize them.
   * 
   * @param <T> class of the object being deserialized
   * @param json the object's JSON encoding as a string
   * @param objectClass class of the object being deserialized
   * @return the deserialized instance
   * @throws SerializationException if the JSON encoding was invalid
   */
  public static <T extends JsonSerializable> T deserialize(String json, Class<T> objectClass) throws SerializationException {
    return deserializeInternal(json, objectClass);
  }
  
  // We use this internally in situations where generic type checking isn't desirable
  static <T> T deserializeInternal(String json, Class<T> objectClass) throws SerializationException {
    try {
      return gson.fromJson(json, objectClass);
    } catch (Exception e) {
      throw new SerializationException(e);
    }
  }
}
