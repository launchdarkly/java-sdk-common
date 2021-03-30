package com.launchdarkly.sdk.json;

import com.google.gson.Gson;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.EvaluationReason;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.UserAttribute;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
 * <li> A Gson instance that has been configured with {@link LDGson}.
 * <li> For {@link LDValue}, you may also use the convenience methods {@link LDValue#toJsonString()} and
 * {@link LDValue#parse(String)}.
 * </ol>
 */
public abstract class JsonSerialization {
  private JsonSerialization() {}
  
  static final List<Class<? extends JsonSerializable>> knownDeserializableClasses = new ArrayList<>();
  
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
   * <p>
   * The current implementation is limited in its ability to handle generic types. Currently, the only
   * such type defined by the SDKs is {@link com.launchdarkly.sdk.EvaluationDetail}. You can serialize
   * any {@code EvaluationDetail<T>} instance and it will represent the {@code T} value correctly, but
   * when deserializing, you will always get {@code EvaluationDetail<LDValue>}.
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

  /**
   * Internal method to return all of the classes that we should have a custom deserializer for.
   * <p>
   * The reason for this method is for some JSON frameworks, such as Jackson, it is not possible to
   * register a general deserializer for a base type like JsonSerializable and have it be called by
   * the framework when someone wants to deserialize some concrete type descended from that base type.
   * Instead, we must register a deserializer for each of the latter.
   * <p>
   * Since the SDKs may define their own JsonSerializable types that are not in this common library,
   * there is a reflection-based mechanism for discovering those: the SDK may define a class called
   * com.launchdarkly.sdk.json.SdkSerializationExtensions, with a static method whose signature is
   * the same as this method, and whatever it returns will be added to this return value.
   * <p>
   * In the case of a base class like LDValue where the deserializer is for the base class (because
   * application code does not know about the subclasses) and implements its own polymorphism, we
   * should only list the base class.
   * 
   * @return classes we should have a custom deserializer for
   */
  static Iterable<Class<? extends JsonSerializable>> getDeserializableClasses() {
    // COVERAGE: This method should be excluded from code coverage analysis, because we can't test the
    // reflective SDK extension logic inside this repo. SdkSerializationExtensions is not defined in this
    // repo by necessity, and if we defined it in the test code then we would not be able to test the
    // default case where it *doesn't* exist. This functionality is tested in the Java SDK.
    synchronized (knownDeserializableClasses) {
      if (knownDeserializableClasses.isEmpty()) {
        knownDeserializableClasses.add(EvaluationReason.class);
        knownDeserializableClasses.add(EvaluationDetail.class);
        knownDeserializableClasses.add(LDUser.class);
        knownDeserializableClasses.add(LDValue.class);
        knownDeserializableClasses.add(UserAttribute.class);
        
        // Use reflection to find any additional classes provided by an SDK; if there are none or if
        // this fails for any reason, don't worry about it
        try {
          Class<?> sdkExtensionsClass = Class.forName("com.launchdarkly.sdk.json.SdkSerializationExtensions");
          Method method = sdkExtensionsClass.getMethod("getDeserializableClasses");
          @SuppressWarnings("unchecked")
          Iterable<Class<? extends JsonSerializable>> sdkClasses =
              (Iterable<Class<? extends JsonSerializable>>) method.invoke(null);
          for (Class<? extends JsonSerializable> c: sdkClasses) {
            knownDeserializableClasses.add(c);
          }
        } catch (Exception e) {} 
      }
    }
    
    return knownDeserializableClasses;
  }
}
