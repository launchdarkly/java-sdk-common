package com.launchdarkly.sdk;

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
 * 
 * @since 4.3.0
 */
public abstract class EvaluationReason {

  /**
   * Enumerated type defining the possible values of {@link EvaluationReason#getKind()}.
   * @since 4.3.0
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
   * Enumerated type defining the possible values of {@link EvaluationReason.Error#getErrorKind()}.
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
     * in this case, and the exception should be available via {@link EvaluationReason.Error#getException()}.
     */
    EXCEPTION
  }
  
  // static instances to avoid repeatedly allocating reasons for the same errors
  private static final Error ERROR_CLIENT_NOT_READY = new Error(ErrorKind.CLIENT_NOT_READY, null);
  private static final Error ERROR_FLAG_NOT_FOUND = new Error(ErrorKind.FLAG_NOT_FOUND, null);
  private static final Error ERROR_MALFORMED_FLAG = new Error(ErrorKind.MALFORMED_FLAG, null);
  private static final Error ERROR_USER_NOT_SPECIFIED = new Error(ErrorKind.USER_NOT_SPECIFIED, null);
  private static final Error ERROR_WRONG_TYPE = new Error(ErrorKind.WRONG_TYPE, null);
  private static final Error ERROR_EXCEPTION = new Error(ErrorKind.EXCEPTION, null);
  
  private final Kind kind;
  
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
    return -1;
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
    return null;
  }
  
  /**
   * The key of the prerequisite flag that did not return the desired variation, if the
   * {@code kind} is {@link Kind#PREREQUISITE_FAILED}. Otherwise {@code null}.
   * 
   * @return the prerequisite flag key or null 
   */
  public String getPrerequisiteKey() {
    return null;
  }

  /**
   * An enumeration value indicating the general category of error, if the
   * {@code kind} is {@link Kind#PREREQUISITE_FAILED}. Otherwise {@code null}.
   * 
   * @return the error kind or null
   */
  public ErrorKind getErrorKind() {
    return null;
  }

  /**
   * The exception that caused the error condition, if the {@code kind} is
   * {@link EvaluationReason.Kind#ERROR} and the {@code errorKind} is {@link ErrorKind#EXCEPTION}.
   * Otherwise {@code null}.
   * 
   * @return the exception insta
   */
  public Exception getException() {
    return null;
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
    return getKind().name();
  }

  protected EvaluationReason(Kind kind)
  {
    this.kind = kind;
  }
  
  /**
   * Returns an instance of {@link Off}.
   * @return a reason object
   */
  public static EvaluationReason off() {
    return Off.instance;
  }
  
  /**
   * Returns an instance of {@link TargetMatch}.
   * @return a reason object
   */
  public static EvaluationReason targetMatch() {
    return TargetMatch.instance;
  }
  
  /**
   * Returns an instance of {@link RuleMatch}.
   * @param ruleIndex the rule index
   * @param ruleId the rule identifier
   * @return a reason object
   */
  public static EvaluationReason ruleMatch(int ruleIndex, String ruleId) {
    return new RuleMatch(ruleIndex, ruleId);
  }
  
  /**
   * Returns an instance of {@link PrerequisiteFailed}.
   * @param prerequisiteKey the flag key of the prerequisite that failed 
   * @return a reason object
   */
  public static EvaluationReason prerequisiteFailed(String prerequisiteKey) {
    return new PrerequisiteFailed(prerequisiteKey);
  }
  
  /**
   * Returns an instance of {@link Fallthrough}.
   * @return a reason object
   */
  public static EvaluationReason fallthrough() {
    return Fallthrough.instance;
  }
  
  /**
   * Returns an instance of {@link Error}.
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
    default: return new Error(errorKind, null);
    }
  }

  /**
   * Returns an instance of {@link Error} with the kind {@link ErrorKind#EXCEPTION} and an exception instance.
   * @param exception the exception that caused the error
   * @return a reason object
   */
  public static EvaluationReason exception(Exception exception) {
    return new Error(ErrorKind.EXCEPTION, exception);
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the flag was off and therefore returned
   * its configured off value.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class Off extends EvaluationReason {
    private Off() {
      super(Kind.OFF);
    }
    
    private static final Off instance = new Off();
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the user key was specifically targeted
   * for this flag.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class TargetMatch extends EvaluationReason {
    private TargetMatch()
    {
      super(Kind.TARGET_MATCH);
    }
    
    private static final TargetMatch instance = new TargetMatch();
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the user matched one of the flag's rules.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class RuleMatch extends EvaluationReason {
    private final int ruleIndex;
    private final String ruleId;
    
    private RuleMatch(int ruleIndex, String ruleId) {
      super(Kind.RULE_MATCH);
      this.ruleIndex = ruleIndex;
      this.ruleId = ruleId;
    }
    
    /**
     * The index of the rule that was matched (0 for the first rule in the feature flag).
     * @return the rule index
     */
    public int getRuleIndex() {
      return ruleIndex;
    }
    
    /**
     * A unique string identifier for the matched rule, which will not change if other rules are added or deleted.
     * @return the rule identifier
     */
    public String getRuleId() {
      return ruleId;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof RuleMatch) {
        RuleMatch o = (RuleMatch)other;
        return ruleIndex == o.ruleIndex && Objects.equals(ruleId, o.ruleId);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(ruleIndex, ruleId);
    }
    
    @Override
    public String toString() {
      return getKind().name() + "(" + ruleIndex + (ruleId == null ? "" : ("," + ruleId)) + ")";
    }
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the flag was considered off because it
   * had at least one prerequisite flag that either was off or did not return the desired variation.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class PrerequisiteFailed extends EvaluationReason {
    private final String prerequisiteKey;
    
    private PrerequisiteFailed(String prerequisiteKey) {
      super(Kind.PREREQUISITE_FAILED);
      this.prerequisiteKey = prerequisiteKey;
    }
    
    /**
     * The key of the prerequisite flag that did not return the desired variation.
     * @return the prerequisite flag key 
     */
    public String getPrerequisiteKey() {
      return prerequisiteKey;
    }
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof PrerequisiteFailed) &&
          ((PrerequisiteFailed)other).prerequisiteKey.equals(prerequisiteKey);
    }
    
    @Override
    public int hashCode() {
      return prerequisiteKey.hashCode();
    }
    
    @Override
    public String toString() {
      return getKind().name() + "(" + prerequisiteKey + ")";
    }
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the flag was on but the user did not
   * match any targets or rules.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class Fallthrough extends EvaluationReason {
    private Fallthrough()
    {
      super(Kind.FALLTHROUGH);
    }
    
    private static final Fallthrough instance = new Fallthrough();
  }
  
  /**
   * Subclass of {@link EvaluationReason} that indicates that the flag could not be evaluated.
   * <p>
   * This subclass is package-private; application code should only reference {@link EvaluationReason}.
   */
  static final class Error extends EvaluationReason {
    private final ErrorKind errorKind;
    private transient final Exception exception;
    // The exception field is transient because we don't want it to be included in the JSON representation that
    // is used in analytics events; the LD event service wouldn't know what to do with it (and it would include
    // a potentially large amount of stacktrace data).
    
    private Error(ErrorKind errorKind, Exception exception) {
      super(Kind.ERROR);
      this.errorKind = errorKind;
      this.exception = exception;
    }
    
    /**
     * An enumeration value indicating the general category of error.
     * @return the error kind
     */
    public ErrorKind getErrorKind() {
      return errorKind;
    }
    
    /**
     * Returns the exception that caused the error condition, if applicable.
     * <p>
     * This is only set if {@link #getErrorKind()} is {@link ErrorKind#EXCEPTION}.
     * 
     * @return the exception instance
     */
    public Exception getException() {
      return exception;
    }
    
    @Override
    public boolean equals(Object other) {
      return other instanceof Error && errorKind == ((Error) other).errorKind && Objects.equals(exception, ((Error) other).exception);
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(errorKind, exception);
    }
    
    @Override
    public String toString() {
      return getKind().name() + "(" + errorKind.name() + (exception == null ? "" : ("," + exception)) + ")";
    }
  }
}
