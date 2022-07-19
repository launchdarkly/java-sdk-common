package com.launchdarkly.sdk;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;
import com.launchdarkly.sdk.json.JsonSerializable;

/**
 * An attribute name or path expression identifying a value within an {@code LDContext}.
 * <p>
 * Applications are unlikely to need to use the AttributeRef type directly, but see below
 * for details of the string attribute reference syntax.
 * <p>
 * The reason to use this type directly is to avoid repetitive string parsing in code where
 * efficiency is a priority; AttributeRef parses its contents once when it is created, and
 * is immutable afterward. If an AttributeRef instance was created from an invalid string,
 * it is considered invalid and its {@link #getError()} method will return a non-null error.
 * <p>
 * The string representation of an attribute reference in LaunchDarkly JSON data uses the
 * following syntax:
 * <ul>
 * <li> If the first character is not a slash, the string is interpreted literally as an
 * attribute name. An attribute name can contain any characters, but must not be empty. </li>
 * <li> If the first character is a slash, the string is interpreted as a slash-delimited
 * path where the first path component is an attribute name, and each subsequent path
 * component is either the name of a property in a JSON object, or a decimal numeric string
 * that is the index of an element in a JSON array. Any instances of the characters "/" or
 * "~" in a path component are escaped as "~1" or "~0" respectively. This syntax
 * deliberately resembles JSON Pointer, but no JSON Pointer behaviors other than those
 * mentioned here are supported. </li>
 * </ul>
 */
@JsonAdapter(AttributeRefTypeAdapter.class)
public final class AttributeRef implements JsonSerializable {
  private static final Map<String, AttributeRef> COMMON_LITERALS = makeLiteralsMap(
      "kind", "key", "name", "anonymous", // built-ins
      "email", "firstName", "lastName", "country", "ip", "avatar" // frequently used custom attributes
      );
  
  private final String error;
  private final String rawPath;
  private final String singlePathComponent;
  private final Component[] components;
  
  private static final class Component {
    final String name;
    final Integer asInteger;
    
    Component(String name, Integer asInteger) {
      this.name = name;
      this.asInteger = asInteger;
    }
  }
  
  private AttributeRef(String rawPath, String singlePathComponent, Component[] components) {
    this.error = null;
    this.rawPath = rawPath == null ? "" : rawPath;
    this.singlePathComponent = singlePathComponent;
    this.components = components;
  }
  
  private AttributeRef(String error, String rawPath) {
    this.error = error;
    this.rawPath = rawPath == null ? "" : rawPath;
    this.singlePathComponent = null;
    this.components = null;
  }
  
  /**
   * Creates an AttributeRef from a string. For the supported syntax and examples, see
   * comments on the {@link AttributeRef} type.
   * <p>
   * This method always returns an AttributeRef that preserves the original string, even if
   * validation fails, so that calling {@link #toString()} (or serializing the AttributeRef
   * to JSON) will produce the original string. If validation fails, {@link #getError()} will
   * return a non-null error and any SDK method that takes this AttributeRef as a parameter
   * will consider it invalid.
   * 
   * @param refPath an attribute name or path
   * @return an AttributeRef
   * @see #fromLiteral(String)
   */
  public static AttributeRef fromPath(String refPath) {
    if (refPath == null || refPath.isEmpty() || refPath.equals("/")) {
      return new AttributeRef(Errors.ATTR_EMPTY, refPath);
    }
    if (refPath.charAt(0) != '/') {
      // When there is no leading slash, this is a simple attribute reference with no character escaping.
      return new AttributeRef(refPath, refPath, null);
    }
    if (refPath.indexOf('/', 1) < 0) {
      // There's only one segment, so this is still a simple attribute reference. However, we still may
      // need to unescape special characters.
      String unescaped = unescapePath(refPath.substring(1));
      if (unescaped == null) {
        return new AttributeRef(Errors.ATTR_INVALID_ESCAPE, refPath);
      }
      return new AttributeRef(refPath, unescaped, null);
    }
    if (refPath.endsWith("/")) {
      // String.split won't behave properly in this case
      return new AttributeRef(Errors.ATTR_EXTRA_SLASH, refPath);
    }
    String[] parsed = refPath.split("/");
    Component[] components = new Component[parsed.length - 1];
    for (int i = 0; i < components.length; i++) {
      String p = parsed[i + 1];
      if (p.isEmpty()) {
        return new AttributeRef(Errors.ATTR_EXTRA_SLASH, refPath);
      }
      String unescaped = unescapePath(p);
      if (unescaped == null) {
        return new AttributeRef(Errors.ATTR_INVALID_ESCAPE, refPath);
      }
      Integer asInteger = null;
      if (Character.isDigit(p.charAt(0))) {
        try {
          asInteger = Integer.parseInt(p);
        } catch (NumberFormatException e) {}
      }
      components[i] = new Component(unescaped, asInteger);
    }
    return new AttributeRef(refPath, null, components);
  }
  
  /**
   * Similar to {@link #fromPath(String)}, except that it always interprets the string as a literal
   * attribute name, never as a slash-delimited path expression.
   * <p>
   * There is no escaping or unescaping, even if the name contains literal '/' or '~' characters.
   * Since an attribute name can contain any characters, this method always returns a valid
   * AttributeRef unless the name is empty.
   * <p>
   * For example: {@code AttributeRef.fromLiteral("name")} is exactly equivalent to
   * {@code AttributeRef.fromPath("name")}. {@code AttributeRef.fromLiteral("a/b")} is exactly
   * equivalent to {@code AttributeRef.fromPath("a/b")} (since the syntax used by
   * {@link #fromPath(String)} treats the whole string as a literal as long as it does not start
   * with a slash), or to {@code AttributeRef.fromPath("/a~1b")}.
   * 
   * @param attributeName an attribute name
   * @return an AttributeRef
   * @see #fromPath(String)
   */
  public static AttributeRef fromLiteral(String attributeName) {
    if (attributeName == null || attributeName.isEmpty()) {
      return new AttributeRef(Errors.ATTR_EMPTY, "");
    }
    if (attributeName.charAt(0) != '/') {
      // When there is no leading slash, this is a simple attribute reference with no character escaping.
      AttributeRef internedInstance = COMMON_LITERALS.get(attributeName);
      return internedInstance == null ? new AttributeRef(attributeName, attributeName, null) : internedInstance;
    }
    // If there is a leading slash, then the attribute name actually starts with a slash. To represent it
    // as an AttributeRef, it'll need to be escaped.
    String escapedPath = "/" + attributeName.replace("~", "~0").replace("/", "~1");
    return new AttributeRef(escapedPath, attributeName, null);
  }
  
  /**
   * True for a valid AttributeRef, false for an invalid AttributeRef.
   * <p>
   * An AttributeRef can only be invalid for the following reasons:
   * <ul>
   * <li> The input string was empty, or consisted only of "/". </li>
   * <li> A slash-delimited string had a double slash causing one component to be empty, such as "/a//b". </li>
   * <li> A slash-delimited string contained a "~" character that was not followed by "0" or "1". </li>
   * </ul>
   * <p>
   * Otherwise, the AttributeRef is valid, but that does not guarantee that such an attribute exists
   * in any given {code LDContext}. For instance, {@code fromLiteral("name")} is a valid AttributeRef,
   * but a specific {@code LDContext} might or might not have a name.
   * <p>
   * See comments on the {@link AttributeRef} type for more details of the attribute reference synax.
   *
   * @return true if the instance is valid
   * @see #getError()
   */
  public boolean isValid() {
    return error == null;
  }
  
  /**
   * Null for a valid AttributeRef, or a non-null error message for an invalid AttributeRef.
   * <p>
   * If this is null, then {@link #isValid()} is true. If it is non-null, then {@link #isValid()} is false.
   * 
   * @return an error string or null
   * @see #isValid()
   */
  public String getError() {
    return error;
  }
  
  /**
   * The number of path components in the AttributeRef.
   * <p>
   * For a simple attribute reference such as "name" with no leading slash, this returns 1.
   * <p>
   * For an attribute reference with a leading slash, it is the number of slash-delimited path
   * components after the initial slash. For instance, {@code AttributeRef.fromPath("/a/b").getDepth()}
   * returns 2.
   * <p>
   * For an invalid attribute reference, it returns zero
   * 
   * @return the number of path components
   */
  public int getDepth() {
    if (error != null) {
      return 0;
    }
    return components == null ? 1 : components.length;
  }
  
  /**
   * Retrieves a single path component from the attribute reference.
   * <p>
   * For a simple attribute reference such as "name" with no leading slash, getComponent returns the
   * attribute name if index is zero, and null otherwise.
   * <p>
   * For an attribute reference with a leading slash, if index is non-negative and less than
   * {@link #getDepth()}, getComponent returns the path component string at that position.
   * 
   * @param index the zero-based index of the desired path component
   * @return the path component, or null if not available
   */
  public String getComponent(int index) {
    if (components == null) {
      return index == 0 ? singlePathComponent : null;
    }
    return index < 0 || index >= components.length ? null : components[index].name;
  }
  
  /**
   * Retrieves a single path component from the attribute reference in the form of an integer.
   * <p>
   * This is equivalent to converting the string returned by {@link #getComponent(int)} to an
   * integer, or null if it is not a numeric string.
   * 
   * @param index the zero-based index of the desired path component
   * @return the path component parsed as an integer, or null
   */
  public Integer getComponentAsInteger(int index) {
    if (components == null) {
      return null;
    }
    return index < 0 || index >= components.length ? null : components[index].asInteger;
  }
  
  /**
   * Returns the attribute reference as a string, in the same format used by
   * {@link #fromPath(String)}.
   * <p>
   * If the AttributeRef was created with {@link #fromPath(String)}, this value is identical to
   * to the original string. If it was created with {@link #fromLiteral(String)}, the value may
   * be different due to unescaping (for instance, an attribute whose name is "/a" would be
   * represented as "~1a").
   * 
   * @return the attribute reference string (guaranteed non-null)
   */
  @Override
  public String toString() {
    return rawPath;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other instanceof AttributeRef) {
      AttributeRef o = (AttributeRef)other;
      return rawPath.equals(o.rawPath);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return rawPath.hashCode();
  }
  
  private static String unescapePath(String path) {
    // If there are no tildes then there's definitely nothing to do
    if (path.indexOf('~') < 0) {
        return path;
    }
    StringBuilder ret = new StringBuilder(100); // arbitrary initial capacity
    for (int i = 0; i < path.length(); i++) {
        char ch = path.charAt(i);
        if (ch != '~')
        {
            ret.append(ch);
            continue;
        }
        i++;
        if (i >= path.length())
        {
            return null;
        }
        switch (path.charAt(i)) {
            case '0':
                ret.append('~');
                break;
            case '1':
                ret.append('/');
                break;
            default:
                return null;
        }
    }
    return ret.toString();
  }
  
  private static Map<String, AttributeRef> makeLiteralsMap(String... names) {
    Map<String, AttributeRef> ret = new HashMap<>();
    for (String name: names) {
      ret.put(name, new AttributeRef(name, name, null));
    }
    return ret;
  }
}
