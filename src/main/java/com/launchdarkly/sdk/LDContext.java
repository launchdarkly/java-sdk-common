package com.launchdarkly.sdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A collection of attributes that can be referenced in flag evaluations and analytics events.
 * <p>
 * To create an LDContext of a single kind, such as a user, you may use {@link #create(String)}
 * or {@link #create(ContextKind, String)} when only the key matters; or, to specify other
 * attributes, use {@link #builder(String)}.
 * <p>
 * To create an LDContext with multiple kinds, use {@link #createMulti(LDContext...)} or
 * {@link #multiBuilder()}.
 * <p>
 * An LDContext can be in an error state if it was built with invalid attributes. See
 * {@link #isValid()} and {@link #getError()}.
 */
public final class LDContext {
  final String error;
  final ContextKind kind;
  final LDContext[] multiContexts;
  final String key;
  final String fullyQualifiedKey;
  final String name;
  final Map<String, LDValue> attributes;
  final String secondary;
  final boolean anonymous;
  final List<AttributeRef> privateAttributes;
  
  private LDContext(
      ContextKind kind,
      LDContext[] multiContexts,
      String key,
      String fullyQualifiedKey,
      String name,
      Map<String, LDValue> attributes,
      String secondary,
      boolean anonymous,
      List<AttributeRef> privateAttributes
      ) {
    this.error = null;
    this.kind = kind == null ? ContextKind.DEFAULT : kind;
    this.multiContexts = multiContexts;
    this.key = key;
    this.fullyQualifiedKey = fullyQualifiedKey;
    this.name = name;
    this.attributes = attributes;
    this.secondary = secondary;
    this.anonymous = anonymous;
    this.privateAttributes = privateAttributes;
  }

  private LDContext(String error) {
    this.error = error;
    this.kind = null;
    this.multiContexts = null;
    this.key = "";
    this.fullyQualifiedKey = "";
    this.name = null;
    this.attributes = null;
    this.secondary = null;
    this.anonymous = false;
    this.privateAttributes = null;
  }
  
  // Internal factory method for single-kind contexts.
  static LDContext createSingle(
      ContextKind kind,
      String key,
      String name,
      Map<String, LDValue> attributes,
      String secondary,
      boolean anonymous,
      List<AttributeRef> privateAttributes
      ) {
    if (kind != null) {
      String error = kind.validateAsSingleKind();
      if (error != null) {
        return failed(error);
      }
    }
    if (key == null || key.isEmpty()) {
      return failed(Errors.CONTEXT_NO_KEY);
    }
    String fullyQualifiedKey = kind.isDefault() ? key :
      (kind.toString() + ":" + urlEncodeKey(key));
    return new LDContext(kind, null, key, fullyQualifiedKey, name, attributes, secondary, anonymous, privateAttributes);
  }
  
  // Internal factory method for multi-kind contexts - implements all of the validation logic
  // except for validating that there is more than one context. We take ownership of the list
  // that is passed in, so it is effectively immutable afterward; ContextMultiBuilder has
  // copy-on-write logic to manage that.
  static LDContext createMultiInternal(LDContext[] multiContexts) {
    List<String> errors = null;
    boolean nestedMulti = false, duplicates = false;
    for (int i = 0; i < multiContexts.length; i++) {
      LDContext c = multiContexts[i];
      if (!c.isValid()) {
        if (errors == null) {
          errors = new ArrayList<String>();
        }
        errors.add(c.getError());
      } else if (c.isMultiple()) {
        nestedMulti = true;
      } else {
        for (int j = 0; j < i; j++) {
          if (multiContexts[j].getKind().equals(c.getKind())) {
            duplicates = true;
            break;
          }
        }
      }
    }
    if (nestedMulti) {
      if (errors == null) {
        errors = new ArrayList<String>();
      }
      errors.add(Errors.CONTEXT_KIND_MULTI_WITHIN_MULTI);
    }
    if (duplicates) {
      if (errors == null) {
        errors = new ArrayList<String>();
      }
      errors.add(Errors.CONTEXT_KIND_MULTI_DUPLICATES);
    }
    
    if (errors != null) {
      StringBuilder s = new StringBuilder();
      for (String e: errors) {
        if (s.length() != 0) {
          s.append(", ");
        }
        s.append(e);
      }
      return failed(s.toString());
    }
    
    Arrays.sort(multiContexts, ByKindComparator.INSTNACE);
    StringBuilder fullKey = new StringBuilder();
    for (LDContext c: multiContexts) {
      if (fullKey.length() != 0) {
        fullKey.append(':');
      }
      fullKey.append(c.getKind().toString()).append(':').append(urlEncodeKey(c.getKey()));
    }
    return new LDContext(ContextKind.MULTI, multiContexts, "", fullKey.toString(),
        null, null, null, false, null);
  }
  
  // Internal factory method for a context in an invalid state.
  static LDContext failed(String error) {
    return new LDContext(error);
  }
  
  /**
   * Creates a single-kind LDContext with a kind of {@link ContextKind#DEFAULT}} and the specified key.
   * <p>
   * To specify additional properties, use {@link #builder(String)}. To create a multi-kind
   * LDContext, use {@link #createMulti(LDContext...)} or {@link #multiBuilder()}. To create a
   * single-kind LDContext of a different kind than "user", use {@link #create(ContextKind, String)}.
   * 
   * @param key the context key
   * @return an LDContext
   * @see #create(ContextKind, String)
   * @see #builder(String)
   */
  public static LDContext create(String key) {
    return create(ContextKind.DEFAULT, key);
  }
  
  /**
   * Creates a single-kind LDContext with only the kind and keys specified.
   * <p>
   * To specify additional properties, use {@link #builder(ContextKind, String)}. To create a multi-kind
   * LDContext, use {@link #createMulti(LDContext...)} or {@link #multiBuilder()}.
   * 
   * @param kind the context kind; if null, {@link ContextKind#DEFAULT} will be used
   * @param key the context key
   * @return an LDContext
   * @see #create(String)
   * @see #builder(ContextKind, String)
   */
  public static LDContext create(ContextKind kind, String key) {
    return createSingle(kind, key, null, null, null, false, null);
  }
  
  /**
   * Creates a multi-kind LDContext out of the specified single-kind LDContexts.
   * <p>
   * To create a single-kind Context, use {@link #create(String)}, {@link #create(ContextKind, String)},
   * or {@link #builder(String)}.
   * <p>
   * For the returned LDContext to be valid, the contexts list must not be empty, and all of its
   * elements must be single-kind LDContexts. Otherwise, the returned LDContext will be invalid as
   * reported by {@link #getError()}.
   * <p>
   * If only one context parameter is given, the method returns a single-kind context (that is,
   * just that same context) rather than a multi-kind context.
   * 
   * @param contexts a list of contexts
   * @return an LDContext
   * @see #multiBuilder()
   */
  public static LDContext createMulti(LDContext... contexts) {
    if (contexts == null || contexts.length == 0) {
      return failed(Errors.CONTEXT_KIND_MULTI_WITH_NO_KINDS);
    }
    if (contexts.length == 1) {
      return contexts[0]; // just return a single-kind context
    }
    // copy the array because the caller could've passed in an array that they will later mutate 
    LDContext[] copied = Arrays.copyOf(contexts, contexts.length);
    return createMultiInternal(copied);
  }
  
  /**
   * Creates a {@link ContextBuilder} for building an LDContext, initializing its {@code key} and setting
   * {@code kind} to {@link ContextKind#DEFAULT}.
   * <p>
   * You may use {@link ContextBuilder} methods to set additional attributes and/or change the
   * {@link ContextBuilder#kind(ContextKind)} before calling {@link ContextBuilder#build()}.
   * If you do not change any values, the defaults for the LDContext are that its {@code kind} is
   * {@link ContextKind#DEFAULT} ("user"), its {@code key} is set to the key parameter passed here,
   * {@code anonymous} is {@code false}, and it has no values for any other attributes.
   * <p>
   * This method is for building an LDContext that has only a single Kind. To define a multi-kind
   * LDContext, use {@link #multiBuilder()}.
   * <p>
   * if {@code key} is an empty string, there is no default. An LDContext must have a non-empty
   * key, so if you call {@link ContextBuilder#build()} in this state without using
   * {@link ContextBuilder#key(String)} to set the key, you will get an invalid LDContext.
   * 
   * @param key the context key
   * @return a builder
   * @see #builder(ContextKind, String)
   * @see #multiBuilder()
   * @see #create(String)
   */
  public static ContextBuilder builder(String key) {
    return builder(ContextKind.DEFAULT, key);
  }
  
  /**
   * Creates a {@link ContextBuilder} for building an LDContext, initializing its {@code key} and
   * {@code kind}.
   * <p>
   * You may use {@link ContextBuilder} methods to set additional attributes and/or change the
   * {@link ContextBuilder#kind(ContextKind)} before calling {@link ContextBuilder#build()}.
   * If you do not change any values, the defaults for the LDContext are that its {@code kind} and
   * {@code key} is set to the parameters passed here, {@code anonymous} is {@code false}, and it has
   * no values for any other attributes.
   * <p>
   * This method is for building an LDContext that has only a single Kind. To define a multi-kind
   * LDContext, use {@link #multiBuilder()}.
   * <p>
   * if {@code key} is an empty string, there is no default. An LDContext must have a non-empty
   * key, so if you call {@link ContextBuilder#build()} in this state without using
   * {@link ContextBuilder#key(String)} to set the key, you will get an invalid LDContext.
   * 
   * @param kind the context kind; if null, {@link ContextKind#DEFAULT} is used
   * @param key the context key
   * @return a builder
   * @see #builder(String)
   * @see #multiBuilder()
   * @see #create(ContextKind, String)
   */
  public static ContextBuilder builder(ContextKind kind, String key) {
    return new ContextBuilder(kind, key);
  }
  
  /**
   * Creates a builder whose properties are the same as an existing single-kind LDContext.
   * <p>
   * You may then change the builder's state in any way and call {@link ContextBuilder#build()}
   * to create a new independent LDContext.
   * 
   * @param context the context to copy from
   * @return a builder
   * @see #builder(String)
   */
  public static ContextBuilder builderFromContext(LDContext context) {
    return new ContextBuilder().copyFrom(context);
  }
  
  /**
   * Creates a {@link ContextMultiBuilder} for building a multi-kind context.
   * <p>
   * This method is for building a Context that has multiple {@link ContextKind} values,
   * each with its own nested LDContext. To define a single-kind context, use
   * {@link #builder(String)} instead.
   * 
   * @return a builder
   * @see #createMulti(LDContext...)
   */
  public static ContextMultiBuilder multiBuilder() {
    return new ContextMultiBuilder();
  }
  
  /**
   * Returns {@code true} for a valid LDContext, {@code false} for an invalid one.
   * <p>
   * A valid context is one that can be used in SDK operations. An invalid context is one that
   * is missing necessary attributes or has invalid attributes, indicating an incorrect usage
   * of the SDK API. The only ways for a context to be invalid are:
   * <ul>
   * <li> It has a disallowed value for the {@code kind} property. See {@link ContextKind}. </li>
   * <li> It is a single-kind context whose {@code key} is empty. </li>
   * <li> It is a multi-kind context that does not have any kinds. See {@link #createMulti(LDContext...)}. </li>
   * <li> It is a multi-kind context where the same kind appears more than once. </li>
   * <li> It is a multi-kind context where at least one of the nested LDContexts has an error. </li>
   * </ul>
   * <p>
   * In any of these cases, {@link #isValid()} will return false, and {@link #getError()}
   * will return a description of the error.
   * <p>
   * Since in normal usage it is easy for applications to be sure they are using context kinds
   * correctly, and because throwing an exception is undesirable in application code that uses
   * LaunchDarkly, the SDK stores the error state in the LDContext itself and checks for such
   * errors at the time the Context is used, such as in a flag evaluation. At that point, if
   * the context is invalid, the operation will fail in some well-defined way as described in
   * the documentation for that method, and the SDK will generally log a warning as well. But
   * in any situation where you are not sure if you have a valid LDContext, you can check
   * {@link #isValid()} or {@link #getError()}.
   * 
   * @return true if the context is valid
   * @see #getError()
   */
  public boolean isValid() {
    return error == null;
  }
  
  /**
   * Returns null for a valid LDContext, or an error message for an invalid one.
   * <p>
   * If this is null, then {@link #isValid()} is true. If it is non-null, then {@link #isValid()}
   * is false.
   * 
   * @return an error description or null
   * @see #isValid()
   */
  public String getError() {
    return error;
  }
  
  /**
   * Returns the context's {@code kind} attribute.
   * <p>
   * Every valid context has a non-empty {@link ContextKind}. For multi-kind contexts, this value
   * is {@link ContextKind#MULTI} and the kinds within the context can be inspected with
   * {@link #getIndividualContext(int)} or {@link #getIndividualContext(String)}.
   * 
   * @return the context kind
   * @see ContextBuilder#kind(ContextKind)
   */
  public ContextKind getKind() {
    return kind;
  }
  
  /**
   * Returns true if this is a multi-kind context.
   * <p>
   * If this value is true, then {@link #getKind()} is guaranteed to be
   * {@link ContextKind#MULTI}, and you can inspect the individual contexts for each kind
   * with {@link #getIndividualContext(int)} or {@link #getIndividualContext(ContextKind)}.
   * <p>
   * If this value is false, then {@link #getKind()} is guaranteed to return a value that
   * is not {@link ContextKind#MULTI}.
   * 
   * @return true for a multi-kind context, false for a single-kind context
   */
  public boolean isMultiple() {
    return multiContexts != null;
  }

  /**
   * Returns the context's {@code key} attribute.
   * <p>
   * For a single-kind context, this value is set by one of the LDContext factory methods
   * or builders ({@link #create(String)}, {@link #create(ContextKind, String)},
   * {@link #builder(String)}, {@link #builder(ContextKind, String)}).
   * <p>
   * For a multi-kind context, there is no single value and {@link #getKey()} returns an
   * empty string. Use {@link #getIndividualContext(int)} or {@link #getIndividualContext(String)}
   * to inspect the LDContext for a particular kind, then call {@link #getKey()} on it.
   * <p>
   * This value is never null.
   * 
   * @return the context key
   * @see ContextBuilder#key(String)
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Returns the context's {@code name} attribute.
   * <p>
   * For a single-kind context, this value is set by {@link ContextBuilder#name(String)}.
   * It is null if no value was set.
   * <p>
   * For a multi-kind context, there is no single value and {@link #getName()} returns null.
   * Use {@link #getIndividualContext(int)} or {@link #getIndividualContext(String)} to
   * inspect the LDContext for a particular kind, then call {@link #getName()} on it.
   * 
   * @return the context name or null
   * @see ContextBuilder#name(String)
   */
  public String getName() {
    return name;
  }

  /**
   * Returns true if this context is only intended for flag evaluations and will not be
   * indexed by LaunchDarkly.
   * <p>
   * The default value is false. False means that this LDContext represents an entity
   * such as a user that you want to be able to see on the LaunchDarkly dashboard.
   * <p>
   * Setting {@code anonymous} to true excludes this context from the database that is
   * used by the dashboard. It does not exclude it from analytics event data, so it is
   * not the same as making attributes private; all non-private attributes will still be
   * included in events and data export. There is no limitation on what other attributes
   * may be included (so, for instance, {@code anonymous} does not mean there is no
   * {@code name}), and the context will still have whatever {@code key} you have given it.
   * <p>
   * This value is also addressable in evaluations as the attribute name "anonymous". It
   * is always treated as a boolean true or false in evaluations.
   * 
   * @return true if the context should be excluded from the LaunchDarkly database
   * @see ContextBuilder#anonymous(boolean)
   */
  public boolean isAnonymous() {
    return anonymous;
  }

  /**
   * Returns the context's optional secondary key attribute.
   * <p>
   * For a single-kind context, this value is set by {@link ContextBuilder#secondary(String)}.
   * It is null if no value was set.
   * <p>
   * For a multi-kind context, there is no single value and {@link #getSecondary()} returns null.
   * Use {@link #getIndividualContext(int)} or {@link #getIndividualContext(String)} to
   * inspect the LDContext for a particular kind, then call {@link #getSecondary()} on it.
   * 
   * @return the secondary key or null
   * @see ContextBuilder#secondary(String)
   */
  public String getSecondary() {
    return secondary;
  }
  
  /**
   * Looks up the value of any attribute of the context by name.
   * <p>
   * This includes only attributes that are addressable in evaluations-- not metadata such
   * as {@link #getSecondary()}.
   * <p>
   * For a single-kind context, the attribute name can be any custom attribute that was set
   * by methods like {@link ContextBuilder#set(String, boolean)}. It can also be one of the
   * built-in ones like "kind", "key", or "name"; in such cases, it is equivalent to
   * {@link #getKind()}, {@link #getKey()}, or {@link #getName()}, except that the value is
   * returned using the general-purpose {@link LDValue} type.
   * <p>
   * For a multi-kind context, the only supported attribute name is "kind". Use
   * {@link #getIndividualContext(int)} or {@link #getIndividualContext(ContextKind)} to
   * inspect the LDContext for a particular kind and then get its attributes.
   * <p>
   * This method does not support complex expressions for getting individual values out of
   * JSON objects or arrays, such as "/address/street". Use {@link #getValue(AttributeRef)}
   * with an {@link AttributeRef} for that purpose.
   * <p>
   * If the value is found, the return value is the attribute value, using the type
   * {@link LDValue} to represent a value of any JSON type.
   * <p>
   * If there is no such attribute, the return value is {@link LDValue#ofNull()} (the method
   * never returns a Java {@code null}). An attribute that actually exists cannot have a null
   * value.
   * 
   * @param attributeName the desired attribute name
   * @return the value or {@link LDValue#ofNull()}
   * @see #getValue(AttributeRef)
   * @see ContextBuilder#set(String, String)
   */
  public LDValue getValue(String attributeName) {
    return getTopLevelAttribute(attributeName);
  }
  
  /**
   * Looks up the value of any attribute of the context, or a value contained within an
   * attribute, based on an {@link AttributeRef}.
   * <p>
   * This includes only attributes that are addressable in evaluations-- not metadata such
   * as {@link #getSecondary()}.
   * <p>
   * This implements the same behavior that the SDK uses to resolve attribute references
   * during a flag evaluation. In a single-kind context, the {@link AttributeRef} can
   * represent a simple attribute name-- either a built-in one like "name" or "key", or a
   * custom attribute that was set by methods like {@link ContextBuilder#set(String, String)}--
   * or, it can be a slash-delimited path using a JSON-Pointer-like syntax. See
   * {@link AttributeRef} for more details.
   * <p>
   * For a multi-kind context, the only supported attribute name is "kind". Use
   * {@link #getIndividualContext(int)} or {@link #getIndividualContext(ContextKind)} to
   * inspect the LDContext for a particular kind and then get its attributes.
   * <p>
   * This method does not support complex expressions for getting individual values out of
   * JSON objects or arrays, such as "/address/street". Use {@link #getValue(AttributeRef)}
   * with an {@link AttributeRef} for that purpose.
   * <p>
   * If the value is found, the return value is the attribute value, using the type
   * {@link LDValue} to represent a value of any JSON type.
   * <p>
   * If there is no such attribute, the return value is {@link LDValue#ofNull()} (the method
   * never returns a Java {@code null}). An attribute that actually exists cannot have a null
   * value.
   * @param attributeRef an attribute reference
   * @return the attribute value
   */
  public LDValue getValue(AttributeRef attributeRef) {
    if (attributeRef == null || !attributeRef.isValid()) {
      return LDValue.ofNull();
    }
    
    String name = attributeRef.getComponent(0);
    
    if (isMultiple()) {
      if (attributeRef.getDepth() == 1 && name.equals("kind")) {
        return LDValue.of(kind.toString());
      }
      return LDValue.ofNull(); // multi-kind context has no other addressable attributes
    }
    
    // Look up attribute in single-kind context
    LDValue value = getTopLevelAttribute(name);
    if (value.isNull()) {
      return value;
    }
    for (int i = 1; i < attributeRef.getDepth(); i++) {
      String component = attributeRef.getComponent(i);
      Integer asInt = attributeRef.getComponentAsInteger(i);
      if (asInt != null && value.getType() == LDValueType.ARRAY) {
        value = value.get(asInt.intValue());
      } else {
        value = value.get(component);
      }
      if (value.isNull()) {
        break;
      }
    }
    return value;
  }
  
  /**
   * Returns the names of all non-built-in attributes that have been set in this context.
   * <p>
   * For a single-kind context, this includes all the names that were passed to
   * any of the overloads of {@link ContextBuilder#set(String, LDValue)} as long as the
   * values were not null (since a null value in LaunchDarkly is equivalent to the attribute
   * not being set).
   * <p>
   * For a multi-kind context, there are no such names.
   *    
   * @return an iterable of strings (may be empty, but will never be null)
   */
  public Iterable<String> getCustomAttributeNames() {
    return attributes == null ? Collections.<String>emptyList() : attributes.keySet();
  }
  
  /**
   * Returns the number of context kinds in this context.
   * <p>
   * For a valid single-kind context, this returns 1. For a multi-kind context, it returns
   * the number of kinds that were added with {@link #createMulti(LDContext...)} or
   * {@link #multiBuilder()}. For an invalid context, it returns zero.
   *
   * @return the number of context kinds
   */
  public int getIndividualContextCount() {
    if (error != null) {
      return 0;
    }
    return multiContexts == null ? 1 : multiContexts.length;
  }
  
  /**
   * Returns the single-kind LDContext corresponding to one of the kinds in this context.
   * <p>
   * If this method is called on a single-kind LDContext, then the only allowable value
   * for {@code index} is zero, and the return value on success is the same LDContext. If
   * the method is called on a multi-kind context, then index must be non-negative and
   * less than the number of kinds (that is, less than the return value of
   * {@link #getIndividualContextCount()}), and the return value on success is one of the
   * individual LDContexts within.
   * 
   * @param index the zero-based index of the context to get
   * @return an {@link LDContext}, or null if the index was out of range
   */
  public LDContext getIndividualContext(int index) {
    if (multiContexts == null) {
      return index == 0 ? this : null;
    }
    return index < 0 || index >= multiContexts.length ? null : multiContexts[index];
  }

  /**
   * Returns the single-kind LDContext corresponding to one of the kinds in this context.
   * <p>
   * If this method is called on a single-kind LDContext, then the only allowable value
   * for {@code kind} is the same as {@link #getKind()}, and the return value on success
   * is the same LDContext. If the method is called on a multi-kind context, then
   * {@code kind} should be match the kind of one of the contexts that was added with
   * {@link #createMulti(LDContext...)} or {@link #multiBuilder()}, and the return value on
   * success is the corresponding individual LDContext within.
   * 
   * @param kind the context kind to get; if null, defaults to {@link ContextKind#DEFAULT}
   * @return an {@link LDContext}, or null if that kind was not found
   */
  public LDContext getIndividualContext(ContextKind kind) {
    if (kind == null) {
      kind = ContextKind.DEFAULT;
    }
    if (multiContexts == null) {
      return this.kind.equals(kind) ? this : null;
    }
    for (LDContext c: multiContexts) {
      if (c.kind.equals(kind)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Same as {@link #getIndividualContext(ContextKind)}, but specifies the kind as a
   * plain string.
   * 
   * @param kind the context kind to get
   * @return an {@link LDContext}, or null if that kind was not found
   */
  public LDContext getIndividualContext(String kind) {
    if (kind == null || kind.isEmpty()) {
      return getIndividualContext(ContextKind.DEFAULT);
    }
    if (multiContexts == null) {
      return this.kind.toString().equals(kind) ? this : null;
    }
    for (LDContext c: multiContexts) {
      if (c.kind.toString().equals(kind)) {
        return c;
      }
    }
    return null;
  }
  
  /**
   * Returns the number of private attribute references that were specified for this context.
   * <p>
   * This is equal to the total number of values passed to {@link ContextBuilder#privateAttributes(String...)}
   * and/or its overload {@link ContextBuilder#privateAttributes(AttributeRef...)}.
   * 
   * @return the number of private attribute references
   */
  public int getPrivateAttributeCount() {
    return privateAttributes == null ? 0 : privateAttributes.size();
  }
  
  /**
   * Retrieves one of the private attribute references that were specified for this context.
   * 
   * @param index a non-negative index that must be less than {@link #getPrivateAttributeCount()}
   * @return an {@link AttributeRef}, or null if the index was out of range
   */
  public AttributeRef getPrivateAttribute(int index) {
    if (privateAttributes == null) {
      return null;
    }
    return index < 0 || index >= privateAttributes.size() ? null : privateAttributes.get(index);
  }
  
  /**
   * Returns a string that describes the LDContext uniquely based on {@code kind} and
   * {@code key} values.
   * <p>
   * This value is used whenever LaunchDarkly needs a string identifier based on all of the
   * {@code kind} and {@code key} values in the context; the SDK may use this for caching
   * previously seen contexts, for instance.
   * 
   * @return the fully-qualified key
   */
  public String getFullyQualifiedKey() {
    return fullyQualifiedKey;
  }
  
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof LDContext)) {
      return false;
    }
    LDContext o = (LDContext)other;
    if (!Objects.equals(error, o.error)) {
      return false;
    }
    if (error != null) {
      return true; // there aren't any other attributes
    }
    if (!kind.equals(o.kind)) {
      return false;
    }
    if (isMultiple()) {
      if (multiContexts.length != o.multiContexts.length) {
        return false;
      }
      for (int i = 0; i < multiContexts.length; i++) {
        if (!multiContexts[i].equals(o.multiContexts[i])) {
          return false;
        }
      }
      return true;
    }
    if (!key.equals(o.key) || !Objects.equals(name, o.name) || anonymous != o.anonymous ||
        !Objects.equals(secondary, o.secondary)) {
      return false;
    }
    if ((attributes == null ? 0 : attributes.size()) !=
        (o.attributes == null ? 0 : o.attributes.size())) {
      return false;
    }
    if (attributes != null) {
      for (Map.Entry<String, LDValue> kv: attributes.entrySet()) {
        if (!Objects.equals(o.attributes.get(kv.getKey()), kv.getValue())) {
          return false;
        }
      }
    }
    if (getPrivateAttributeCount() != o.getPrivateAttributeCount()) {
      return false;
    }
    if (privateAttributes != null) {
      for (AttributeRef a: privateAttributes) {
        boolean found = false;
        for (AttributeRef a1: o.privateAttributes) {
          if (a1.equals(a)) {
           found = true;
           break;
          }
        }
        if (!found) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    // This implementation of hashCode() is inefficient due to the need to create a predictable ordering
    // of attribute names. That's necessary just for the sake of aligning with the behavior of equals(),
    // which is insensitive to ordering. However, using an LDContext as a map key is not an anticipated
    // or recommended use case.
    int h = Objects.hash(error, kind, key, name, anonymous, secondary);
    if (multiContexts != null) {
      for (LDContext c: multiContexts) {
        h = h * 17 + c.hashCode();
      }
    }
    if (attributes != null) {
      String[] names = attributes.keySet().toArray(new String[attributes.size()]);
      Arrays.sort(names);
      for (String name: names) {
        h = (h * 17 + name.hashCode()) * 17 + attributes.get(name).hashCode(); 
      }
    }
    if (privateAttributes != null) {
      AttributeRef[] refs = privateAttributes.toArray(new AttributeRef[privateAttributes.size()]);
      Arrays.sort(refs);;
      for (AttributeRef a: refs) {
        h = h * 17 + a.hashCode();
      }
    }
    return h;
  }
  
  private LDValue getTopLevelAttribute(String attributeName) {
    switch (attributeName) {
    case "kind":
      return LDValue.of(kind.toString());
    case "key":
      return multiContexts == null ? LDValue.of(key) : LDValue.ofNull();
    case "name":
      return LDValue.of(name);
    case "anonymous":
      return LDValue.of(anonymous);
    default:
      if (attributes == null) {
        return LDValue.ofNull();
      }
      LDValue v = attributes.get(attributeName);
      return v == null ? LDValue.ofNull() : v;
    }
  }
  
  private static String urlEncodeKey(String key) {
    try {
      return URLEncoder.encode(key, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return ""; // COVERAGE: not a reachable condition
    }
  }
  
  private static class ByKindComparator implements Comparator<LDContext> {
    static final ByKindComparator INSTNACE = new ByKindComparator();
    
    public int compare(LDContext c1, LDContext c2) {
      return c1.getKind().toString().compareTo(c2.getKind().toString());
    }
  }
}
