package com.launchdarkly.sdk;

import com.google.gson.annotations.JsonAdapter;
import com.launchdarkly.sdk.json.JsonSerializable;

import java.util.Objects;

/**
 * Describes the reason that a flag evaluation produced a particular value.
 * <p>
 * This is returned within {@link EvaluationDetail} by the SDK's "variation detail" methods such as
 * {@code boolVariationDetail}.
 * <p>
 * Note that while {@link EvaluationReason} has subclasses as an implementation detail, the subclasses
 * are not public and may be removed in the future. Always use methods of the base class such as
 * {@link #getKind()} or {@link #getRuleIndex()} to inspect the reason.
 * <p>
 * LaunchDarkly defines a standard JSON encoding for evaluation reasons, used in analytics events.
 * {@link EvaluationReason} can be converted to and from JSON in any of these ways:
 * <ol>
 * <li> With {@link com.launchdarkly.sdk.json.JsonSerialization}.
 * <li> With Gson, if and only if you configure your {@code Gson} instance with
 * {@link com.launchdarkly.sdk.json.LDGson}.
 * <li> With Jackson, if and only if you configure your {@code ObjectMapper} instance with
 * {@link com.launchdarkly.sdk.json.LDJackson}.
 * </ol>
 */
@JsonAdapter(EvaluationReasonTypeAdapter.class)
public final class EvaluationReason implements JsonSerializable {
  /**
   * Enumerated type defining the possible values of {@link EvaluationReason#getKind()}.
   */
  public static enum Kind {
    /**
     * Indicates that the flag was off and therefore returned its configured off value.
     */
    OFF,
    /**
     * Indicates that the flag was on but the user did not match any targets or rules. 
     */
    FALLTHROUGH,
    /**
     * Indicates that the user key was specifically targeted for this flag.
     */
    TARGET_MATCH,
    /**
     * Indicates that the user matched one of the flag's rules.
     */
    RULE_MATCH,
    /**
     * Indicates that the flag was considered off because it had at least one prerequisite flag
     * that either was off or did not return the desired variation.
     */
    PREREQUISITE_FAILED,
    /**
     * Indicates that the flag could not be evaluated, e.g. because it does not exist or due to an unexpected
     * error. In this case the result value will be the default value that the caller passed to the client.
     * Check the errorKind property for more details on the problem.
     */
    ERROR;
  }
  
  /**
   * Enumerated type defining the possible values of {@link #getErrorKind()}.
   */
  public static enum ErrorKind {
    /**
     * Indicates that the caller tried to evaluate a flag before the client had successfully initialized.
     */
    CLIENT_NOT_READY,
    /**
     * Indicates that the caller provided a flag key that did not match any known flag.
     */
    FLAG_NOT_FOUND,
    /**
     * Indicates that there was an internal inconsistency in the flag data, e.g. a rule specified a nonexistent
     * variation. An error message will always be logged in this case.
     */
    MALFORMED_FLAG,
    /**
     * Indicates that the caller passed {@code null} for the user parameter, or the user lacked a key.
     */
    USER_NOT_SPECIFIED,
    /**
     * Indicates that the result value was not of the requested type, e.g. you called {@code boolVariationDetail}
     * but the value was an integer.
     */
    WRONG_TYPE,
    /**
     * Indicates that an unexpected exception stopped flag evaluation. An error message will always be logged
     * in this case, and the exception should be available via {@link #getException()}.
     */
    EXCEPTION
  }
  
  // static instances to avoid repeatedly allocating reasons for the same parameters
  private static final EvaluationReason OFF_INSTANCE = new EvaluationReason(Kind.OFF);
  private static final EvaluationReason FALLTHROUGH_INSTANCE = new EvaluationReason(Kind.FALLTHROUGH);
  private static final EvaluationReason TARGET_MATCH_INSTANCE = new EvaluationReason(Kind.TARGET_MATCH);
  private static final EvaluationReason ERROR_CLIENT_NOT_READY = new EvaluationReason(ErrorKind.CLIENT_NOT_READY, null);
  private static final EvaluationReason ERROR_FLAG_NOT_FOUND = new EvaluationReason(ErrorKind.FLAG_NOT_FOUND, null);
  private static final EvaluationReason ERROR_MALFORMED_FLAG = new EvaluationReason(ErrorKind.MALFORMED_FLAG, null);
  private static final EvaluationReason ERROR_USER_NOT_SPECIFIED = new EvaluationReason(ErrorKind.USER_NOT_SPECIFIED, null);
  private static final EvaluationReason ERROR_WRONG_TYPE = new EvaluationReason(ErrorKind.WRONG_TYPE, null);
  private static final EvaluationReason ERROR_EXCEPTION = new EvaluationReason(ErrorKind.EXCEPTION, null);
  
  private final Kind kind;
  private final int ruleIndex;
  private final String ruleId;
  private final String prerequisiteKey;
  private final ErrorKind errorKind;
  private final Exception exception;
  
  private EvaluationReason(Kind kind, int ruleIndex, String ruleId, String prerequisiteKey,
      ErrorKind errorKind, Exception exception) {
    this.kind = kind;
    this.ruleIndex = ruleIndex;
    this.ruleId = ruleId;
    this.prerequisiteKey = prerequisiteKey;
    this.errorKind = errorKind;
    this.exception = exception;
  }
  
  private EvaluationReason(Kind kind) {
    this(kind, -1, null, null, null, null);
  }
  
  private EvaluationReason(ErrorKind errorKind, Exception exception) {
    this(Kind.ERROR, -1, null, null, errorKind, exception);
  }
  
  /**
   * Returns an enum indicating the general category of the reason.
   * 
   * @return a {@link Kind} value
   */
  public Kind getKind()
  {
    return kind;
  }

  /**
   * The index of the rule that was matched (0 for the first rule in the feature flag),
   * if the {@code kind} is {@link Kind#RULE_MATCH}. Otherwise this returns -1.
   * 
   * @return the rule index or -1
   */
  public int getRuleIndex() {
    return ruleIndex;
  }
  
  /**
   * The unique identifier of the rule that was matched, if the {@code kind} is
   * {@link Kind#RULE_MATCH}. Otherwise {@code null}.
   * <p>
   * Unlike the rule index, this identifier will not change if other rules are added or deleted.
   * 
   * @return the rule identifier or null
   */
  public String getRuleId() {
    return ruleId;
  }
  
  /**
   * The key of the prerequisite flag that did not return the desired variation, if the
   * {@code kind} is {@link Kind#PREREQUISITE_FAILED}. Otherwise {@code null}.
   * 
   * @return the prerequisite flag key or null 
   */
  public String getPrerequisiteKey() {
    return prerequisiteKey;
  }

  /**
   * An enumeration value indicating the general category of error, if the
   * {@code kind} is {@link Kind#PREREQUISITE_FAILED}. Otherwise {@code null}.
   * 
   * @return the error kind or null
   */
  public ErrorKind getErrorKind() {
    return errorKind;
  }

  /**
   * The exception that caused the error condition, if the {@code kind} is
   * {@link EvaluationReason.Kind#ERROR} and the {@code errorKind} is {@link ErrorKind#EXCEPTION}.
   * Otherwise {@code null}. 
   * <p>
   * Note that the exception will not be included in the JSON serialization of the reason when it
   * appears in analytics events; it is only provided informationally for use by application code.
   * 
   * @return the exception instance
   */
  public Exception getException() {
    return exception;
  }
  
  /**
   * Returns a simple string representation of the reason.
   * <p>
   * This is a convenience method for debugging and any other use cases where a human-readable string is
   * helpful. The exact format of the string is subject to change; if you need to make programmatic
   * decisions based on the reason properties, use other methods like {@link #getKind()}.
   */
  @Override
  public String toString() {
    switch (kind) {
    case RULE_MATCH:
      return kind + "(" + ruleIndex + (ruleId == null ? "" : ("," + ruleId)) + ")";
    case PREREQUISITE_FAILED:
      return kind + "(" + prerequisiteKey + ")";
    case ERROR:
      return kind + "(" + errorKind + (exception == null ? "" : ("," + exception)) + ")";
    default:
      return getKind().name();
    }
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof EvaluationReason) {
      EvaluationReason o = (EvaluationReason)other;
      return kind == o.kind && ruleIndex == o.ruleIndex && Objects.equals(ruleId, o.ruleId)&&
          Objects.equals(prerequisiteKey, o.prerequisiteKey) && Objects.equals(errorKind, o.errorKind) &&
          Objects.equals(exception, o.exception);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(kind, ruleIndex, ruleId, prerequisiteKey, errorKind, exception);
  }
  
  /**
   * Returns an instance whose {@code kind} is {@link Kind#OFF}.
   * 
   * @return a reason object
   */
  public static EvaluationReason off() {
    return OFF_INSTANCE;
  }

  /**
   * Returns an instance whose {@code kind} is {@link Kind#FALLTHROUGH}.
   * 
   * @return a reason object
   */
  public static EvaluationReason fallthrough() {
    return FALLTHROUGH_INSTANCE;
  }
  
  /**
   * Returns an instance whose {@code kind} is {@link Kind#TARGET_MATCH}.
   * 
   * @return a reason object
   */
  public static EvaluationReason targetMatch() {
    return TARGET_MATCH_INSTANCE;
  }
  
  /**
   * Returns an instance whose {@code kind} is {@link Kind#RULE_MATCH}.
   * 
   * @param ruleIndex the rule index
   * @param ruleId the rule identifier
   * @return a reason object
   */
  public static EvaluationReason ruleMatch(int ruleIndex, String ruleId) {
    return new EvaluationReason(Kind.RULE_MATCH, ruleIndex, ruleId, null, null, null);
  }
  
  /**
   * Returns an instance whose {@code kind} is {@link Kind#PREREQUISITE_FAILED}.
   * 
   * @param prerequisiteKey the flag key of the prerequisite that failed 
   * @return a reason object
   */
  public static EvaluationReason prerequisiteFailed(String prerequisiteKey) {
    return new EvaluationReason(Kind.PREREQUISITE_FAILED, -1, null, prerequisiteKey, null, null);
  }
  
  /**
   * Returns an instance whose {@code kind} is {@link Kind#ERROR}.
   * 
   * @param errorKind describes the type of error
   * @return a reason object
   */
  public static EvaluationReason error(ErrorKind errorKind) {
    switch (errorKind) {
    case CLIENT_NOT_READY: return ERROR_CLIENT_NOT_READY;
    case EXCEPTION: return ERROR_EXCEPTION;
    case FLAG_NOT_FOUND: return ERROR_FLAG_NOT_FOUND;
    case MALFORMED_FLAG: return ERROR_MALFORMED_FLAG;
    case USER_NOT_SPECIFIED: return ERROR_USER_NOT_SPECIFIED;
    case WRONG_TYPE: return ERROR_WRONG_TYPE;
    default: return new EvaluationReason(errorKind, null); // COVERAGE: compiler requires default but there are no other ErrorKind values
    }
  }

  /**
   * Returns an instance whose {@code kind} is {@link Kind#ERROR}, with an exception instance.
   * <p>
   * Note that the exception will not be included in the JSON serialization of the reason when it
   * appears in analytics events; it is only provided informationally for use by application code.
   * 
   * @param exception the exception that caused the error
   * @return a reason object
   */
  public static EvaluationReason exception(Exception exception) {
    return new EvaluationReason(ErrorKind.EXCEPTION, exception);
  }
}
