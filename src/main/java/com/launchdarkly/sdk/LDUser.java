package com.launchdarkly.sdk;

/**
 * Contains legacy methods for constructing simple evaluation contexts, using the older LaunchDarkly
 * SDK model for user properties.
 * <p>
 * The SDK now uses the type {@link LDContext} to represent an evaluation context that might
 * represent a user, or some other kind of entity, or multiple kinds. In older SDK versions,
 * this was limited to one kind and was represented by the type {@code LDUser}. This differed from
 * LDContext in several ways:
 * <ul>
 * <li> There was always a single implicit context kind of "user". </li>
 * <li> Unlike LDContext where only a few attributes such as {@link ContextBuilder#key(String)}
 * and {@link ContextBuilder#name(String)} have special behavior, the user model defined many
 * other built-in attributes such as {@code email} which, like {@code name}, were constrained to
 * only allow string values. These had specific setter methods in {@link LDUser.Builder}. </li>
 * </ul>
 * <p>
 * The LDUser class now exists only as a container for {@link LDUser.Builder}, which has been
 * modified to be a wrapper for {@link ContextBuilder}. This allows code that used the older
 * older model to still work with minor adjustments.
 * <p>
 * For any code that still uses this builder, the significant differences from older SDK
 * versions are:
 * <ul>
 * <li> The concrete type being constructed is {@link LDContext}, so you will need to update
 * any part of your code that referred to LDUser as a concrete type. </li>
 * <li> The SDK no longer supports setting the key to an empty string. If you do this,
 * the returned LDContext will be invalid (as indicated by {@link LDContext#isValid()}) and
 * the SDK will refuse to use it for evaluations or events. </li>
 * <li> Previously, the {@link LDUser.Builder#anonymous(boolean)} property had three states:
 * true, false, or undefined/null. Undefined/null and false were functionally the same in terms
 * of the LaunchDarkly dashboard/indexing behavior, but they were represented differently in
 * JSON and could behave differently if referenced in a flag rule (an undefined/null value
 * would not match "anonymous is false"). Now, the property is a simple boolean defaulting to
 * false, and the undefined state is the same as false. </li>
 * <li> The {@code secondary} attribute no longer exists. </li>
 * </ul>
 */
public abstract class LDUser {
  private LDUser() {}

  /**
   * A <a href="http://en.wikipedia.org/wiki/Builder_pattern">builder</a> that helps construct {@link LDUser} objects. Builder
   * calls can be chained, enabling the following pattern:
   * <pre>
   * LDUser user = new LDUser.Builder("key")
   *      .country("US")
   *      .ip("192.168.0.1")
   *      .build()
   * </pre>
   */
  public static class Builder {
    private final ContextBuilder builder;

    /**
     * Creates a builder with the specified key.
     *
     * @param key the unique key for this user
     */
    public Builder(String key) {
      this.builder = LDContext.builder(key);
    }

    /**
    * Creates a builder based on an existing context.
    *
    * @param context an existing {@code LDContext}
    */
    public Builder(LDContext context) {
      this.builder = LDContext.builderFromContext(context);
    }
    
    /**
     * Changes the user's key.
     * 
     * @param s the user key
     * @return the builder
     */
    public Builder key(String s) {
      builder.key(s);
      return this;
    }
    
    /**
     * Sets the IP for a user.
     *
     * @param s the IP address for the user
     * @return the builder
     */
    public Builder ip(String s) {
      builder.set("ip", s);
      return this;
    }

    /**
     * Sets the IP for a user, and ensures that the IP attribute is not sent back to LaunchDarkly.
     *
     * @param s the IP address for the user
     * @return the builder
     */
    public Builder privateIp(String s) {
      builder.privateAttributes("ip");
      return ip(s);
    }

    /**
     * Set the country for a user. Before version 5.0.0, this field was validated and normalized by the SDK
     * as an ISO-3166-1 country code before assignment. This behavior has been removed so that the SDK can
     * treat this field as a normal string, leaving the meaning of this field up to the application.
     *
     * @param s the country for the user
     * @return the builder
     */
    public Builder country(String s) {
      builder.set("country", s);
      return this;
    }

    /**
     * Set the country for a user, and ensures that the country attribute will not be sent back to LaunchDarkly.
     * Before version 5.0.0, this field was validated and normalized by the SDK as an ISO-3166-1 country code
     * before assignment. This behavior has been removed so that the SDK can treat this field as a normal string,
     * leaving the meaning of this field up to the application.
     *
     * @param s the country for the user
     * @return the builder
     */
    public Builder privateCountry(String s) {
      builder.privateAttributes("country");
      return country(s);
    }

    /**
     * Sets the user's first name
     *
     * @param firstName the user's first name
     * @return the builder
     */
    public Builder firstName(String firstName) {
      builder.set("firstName", firstName);
      return this;
    }


    /**
     * Sets the user's first name, and ensures that the first name attribute will not be sent back to LaunchDarkly.
     *
     * @param firstName the user's first name
     * @return the builder
     */
    public Builder privateFirstName(String firstName) {
      builder.privateAttributes("firstName");
      return firstName(firstName);
    }


    /**
     * Sets whether this user is anonymous.
     *
     * @param anonymous whether the user is anonymous
     * @return the builder
     */
    public Builder anonymous(boolean anonymous) {
      builder.anonymous(anonymous);
      return this;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the user's last name
     * @return the builder
     */
    public Builder lastName(String lastName) {
      builder.set("lastName", lastName);
      return this;
    }

    /**
     * Sets the user's last name, and ensures that the last name attribute will not be sent back to LaunchDarkly.
     *
     * @param lastName the user's last name
     * @return the builder
     */
    public Builder privateLastName(String lastName) {
      builder.privateAttributes("lastName");
      return lastName(lastName);
    }


    /**
     * Sets the user's full name.
     *
     * @param name the user's full name
     * @return the builder
     */
    public Builder name(String name) {
      builder.name(name);
      return this;
    }

    /**
     * Sets the user's full name, and ensures that the name attribute will not be sent back to LaunchDarkly.
     *
     * @param name the user's full name
     * @return the builder
     */
    public Builder privateName(String name) {
      builder.privateAttributes("name");
      return name(name);
    }

    /**
     * Sets the user's avatar.
     *
     * @param avatar the user's avatar
     * @return the builder
     */
    public Builder avatar(String avatar) {
      builder.set("avatar", avatar);
      return this;
    }

    /**
     * Sets the user's avatar, and ensures that the avatar attribute will not be sent back to LaunchDarkly.
     *
     * @param avatar the user's avatar
     * @return the builder
     */
    public Builder privateAvatar(String avatar) {
      builder.privateAttributes("avatar");
      return avatar(avatar);
    }


    /**
     * Sets the user's e-mail address.
     *
     * @param email the e-mail address
     * @return the builder
     */
    public Builder email(String email) {
      builder.set("email", email);
      return this;
    }

    /**
     * Sets the user's e-mail address, and ensures that the e-mail address attribute will not be sent back to LaunchDarkly.
     *
     * @param email the e-mail address
     * @return the builder
     */
    public Builder privateEmail(String email) {
      builder.privateAttributes("email");
      return email(email);
    }

    /**
     * Adds a {@link java.lang.String}-valued custom attribute. When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param v the value for the custom attribute
     * @return the builder
     */
    public Builder custom(String k, String v) {
      return custom(k, LDValue.of(v));
    }

    /**
     * Adds an integer-valued custom attribute. When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param n the value for the custom attribute
     * @return the builder
     */
    public Builder custom(String k, int n) {
      return custom(k, LDValue.of(n));
    }

    /**
     * Adds a double-precision numeric custom attribute. When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param n the value for the custom attribute
     * @return the builder
     */
    public Builder custom(String k, double n) {
      return custom(k, LDValue.of(n));
    }

    /**
     * Add a boolean-valued custom attribute. When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param b the value for the custom attribute
     * @return the builder
     */
    public Builder custom(String k, boolean b) {
      return custom(k, LDValue.of(b));
    }

    /**
     * Add a custom attribute whose value can be any JSON type, using {@link LDValue}. When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param v the value for the custom attribute
     * @return the builder
     */
    public Builder custom(String k, LDValue v) {
      builder.set(k, v);
      return this;
    }
    
    /**
     * Add a {@link java.lang.String}-valued custom attribute that will not be sent back to LaunchDarkly.
     * When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param v the value for the custom attribute
     * @return the builder
     */
    public Builder privateCustom(String k, String v) {
      builder.privateAttributes(k);
      return custom(k, v);
    }

    /**
     * Add an int-valued custom attribute that will not be sent back to LaunchDarkly.
     * When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param n the value for the custom attribute
     * @return the builder
     */
    public Builder privateCustom(String k, int n) {
      builder.privateAttributes(k);
      return custom(k, n);
    }

    /**
     * Add a double-precision numeric custom attribute that will not be sent back to LaunchDarkly.
     * When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param n the value for the custom attribute
     * @return the builder
     */
    public Builder privateCustom(String k, double n) {
      builder.privateAttributes(k);
      return custom(k, n);
    }

    /**
     * Add a boolean-valued custom attribute that will not be sent back to LaunchDarkly.
     * When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param b the value for the custom attribute
     * @return the builder
     */
    public Builder privateCustom(String k, boolean b) {
      builder.privateAttributes(k);
      return custom(k, b);
    }

    /**
     * Add a custom attribute of any JSON type, that will not be sent back to LaunchDarkly.
     * When set to one of the
     * <a href="https://docs.launchdarkly.com/home/flags/targeting-users#targeting-rules-based-on-user-attributes">built-in
     * user attribute keys</a>, this custom attribute will be ignored.
     *
     * @param k the key for the custom attribute
     * @param v the value for the custom attribute
     * @return the builder
     */
    public Builder privateCustom(String k, LDValue v) {
      builder.privateAttributes(k);
      return custom(k, v);
    }

    /**
     * Builds the configured {@link LDContext} object.
     *
     * @return the {@link LDContext} configured by this builder
     */
    public LDContext build() {
      return builder.build();
    }
  }
}
