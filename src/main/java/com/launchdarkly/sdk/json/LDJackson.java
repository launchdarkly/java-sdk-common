package com.launchdarkly.sdk.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;

import java.io.IOException;

/**
 * A helper class for interoperability with application code that uses
 * <a href="https://github.com/FasterXML/jackson">Jackson</a>.
 * <p>
 * An application that wishes to use Jackson to serialize or deserialize classes from the SDK should
 * configure its {@code ObjectMapper} instance as follows:
 * <pre><code>
 *     import com.launchdarkly.sdk.json.LDJackson;
 *     
 *     ObjectMapper mapper = new ObjectMapper();
 *     mapper.registerModule(LDJackson.module());
 * </code></pre>
 * <p>
 * This causes Jackson to use the correct JSON representation logic (the same that would be used by
 * {@link JsonSerialization}) for any types that have the SDK's {@link JsonSerializable} marker
 * interface, such as {@link LDUser} and {@link LDValue}, regardless of whether they are the
 * top-level object being serialized or are contained in something else such as a collection. It
 * does not affect Jackson's behavior for any other classes.
 */
public class LDJackson {
  /**
   * Returns a Jackson {@code Module} that defines the correct serialization and deserialization
   * behavior for all LaunchDarkly SDK objects that implement {@link JsonSerializable}.
   * <pre><code>
   *     import com.launchdarkly.sdk.json.LDJackson;
   *     
   *     ObjectMapper mapper = new ObjectMapper();
   *     mapper.registerModule(LDJackson.module());
   * </code></pre>
   * @return a {@code Module}
   */
  public static Module module() {
    SimpleModule module = new SimpleModule(LDJackson.class.getName());    
    module.addSerializer(JsonSerializable.class, LDJacksonSerializer.INSTANCE);
    for (Class<?> c: JsonSerialization.getDeserializableClasses()) {
      @SuppressWarnings("unchecked")
      Class<JsonSerializable> cjs = (Class<JsonSerializable>)c;
      module.addDeserializer(cjs, new LDJacksonDeserializer<>(cjs));
    }
    return module;
  }
  
  private static class LDJacksonSerializer extends JsonSerializer<JsonSerializable> {
    static final LDJacksonSerializer INSTANCE = new LDJacksonSerializer();
    
    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        String json = JsonSerialization.serializeInternal(value);
        gen.writeRawValue(json);
      }
    }
  }
  
  private static class LDJacksonDeserializer<T extends JsonSerializable> extends JsonDeserializer<T> {
    private final Class<T> objectClass;
    
    LDJacksonDeserializer(Class<T> objectClass) {
      this.objectClass = objectClass;
    }
    
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      // This implementation is inefficient because our internal Gson instance can't use Jackson's
      // streaming parser directly; instead we have to read the next JSON value, convert it to a
      // string, and then ask our JsonSerialization to parse it back from a string.
      JsonLocation loc = p.getCurrentLocation();
      TreeNode jsonTree = p.readValueAsTree();
      String jsonString = jsonTree.toString();
      try {
        return JsonSerialization.deserialize(jsonString, objectClass);
      } catch (SerializationException e) {
        throw new JsonParseException(p, "invalid JSON encoding for " + objectClass.getSimpleName(), loc, e);
      }
    }
  }
}
