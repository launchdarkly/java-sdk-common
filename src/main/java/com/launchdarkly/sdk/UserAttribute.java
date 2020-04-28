package com.launchdarkly.sdk;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.launchdarkly.sdk.json.JsonSerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a built-in or custom attribute name supported by {@link LDUser}.
 * <p>
 * This abstraction helps to distinguish attribute names from other {@link String} values, and also
 * improves efficiency in feature flag data structures and evaluations because built-in attributes
 * always reuse the same instances. 
 * <p>
 * For a fuller description of user attributes and how they can be referenced in feature flag rules, see the reference
 * guides on <a href="https://docs.launchdarkly.com/home/managing-users/user-attributes">Setting user attributes</a>
 * and <a href="https://docs.launchdarkly.com/home/managing-flags/targeting-users">Targeting users</a>.
 */
@JsonAdapter(UserAttribute.UserAttributeTypeAdapter.class)
public final class UserAttribute implements JsonSerializable {
  /**
   * Represents the user key attribute.
   */
  public static final UserAttribute KEY = new UserAttribute("key", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.key;
    }
  });

  /**
   * Represents the secondary key attribute.
   */
  public static final UserAttribute SECONDARY_KEY = new UserAttribute("secondary", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.secondary;
    }
  });
  
  /**
   * Represents the IP address attribute.
   */
  public static final UserAttribute IP = new UserAttribute("ip", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.ip;
    }
  });

  /**
   * Represents the user key attribute.
   */
  public static final UserAttribute EMAIL = new UserAttribute("email", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.email;
    }
  });

  /**
   * Represents the full name attribute.
   */
  public static final UserAttribute NAME = new UserAttribute("name", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.name;
    }
  });

  /**
   * Represents the avatar URL attribute.
   */
  public static final UserAttribute AVATAR = new UserAttribute("avatar", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.avatar;
    }
  });

  /**
   * Represents the first name attribute.
   */
  public static final UserAttribute FIRST_NAME = new UserAttribute("firstName", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.firstName;
    }
  });

  /**
   * Represents the last name attribute.
   */
  public static final UserAttribute LAST_NAME = new UserAttribute("lastName", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.lastName;
    }
  });

  /**
   * Represents the country attribute.
   */
  public static final UserAttribute COUNTRY = new UserAttribute("country", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.country;
    }
  });

  /**
   * Represents the anonymous attribute.
   */
  public static final UserAttribute ANONYMOUS = new UserAttribute("anonymous", new Function<LDUser, LDValue>() {
    public LDValue apply(LDUser u) {
      return u.anonymous;
    }
  });

  
  static final Map<String, UserAttribute> BUILTINS;
  static {
    BUILTINS = new HashMap<>();
    for (UserAttribute a: new UserAttribute[] { KEY, SECONDARY_KEY, IP, EMAIL, NAME, AVATAR, FIRST_NAME, LAST_NAME, COUNTRY, ANONYMOUS }) {
      BUILTINS.put(a.getName(), a);
    }
  }
  
  private final String name;
  final Function<LDUser, LDValue> builtInGetter;
  
  private UserAttribute(String name, Function<LDUser, LDValue> builtInGetter) {
    this.name = name;
    this.builtInGetter = builtInGetter;
  }
  
  /**
   * Returns a UserAttribute instance for the specified attribute name.
   * <p>
   * For built-in attributes, the same instances are always reused and {@link #isBuiltIn()} will
   * return true. For custom attributes, a new instance is created and {@link #isBuiltIn()} will
   * return false.
   * 
   * @param name the attribute name
   * @return a {@link UserAttribute}
   */
  public static UserAttribute forName(String name) {
    UserAttribute a = BUILTINS.get(name);
    return a != null ? a : new UserAttribute(name, null);
  }
  
  /**
   * Returns the case-sensitive attribute name.
   * 
   * @return the attribute name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Returns true for a built-in attribute or false for a custom attribute.
   * 
   * @return true if it is a built-in attribute
   */
  public boolean isBuiltIn() {
    return builtInGetter != null;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserAttribute) {
      UserAttribute o = (UserAttribute)other;
      if (isBuiltIn() || o.isBuiltIn()) {
        return this == o; // faster comparison since built-in instances are interned
      }
      return name.equals(o.name);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return isBuiltIn() ? super.hashCode() : name.hashCode();
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  static final class UserAttributeTypeAdapter extends TypeAdapter<UserAttribute>{    
    @Override
    public UserAttribute read(JsonReader reader) throws IOException {
      // Unfortunately, JsonReader.nextString() does not actually enforce that the value is a string
      switch (reader.peek()) {
      case STRING:
        return UserAttribute.forName(reader.nextString());
      default:
        throw new IllegalStateException("expected string for UserAttribute");
        // IllegalStateException seems to be what Gson parsing methods normally use for wrong types
      }
    }
  
    @Override
    public void write(JsonWriter writer, UserAttribute value) throws IOException {
      writer.value(value.getName());
    }
  }
}
