package com.launchdarkly.sdk;

import static com.launchdarkly.sdk.Helpers.hashFrom;
import static com.launchdarkly.sdk.Helpers.objectsEqual;

/**
 * An object returned by the SDK's "variation detail" methods such as {@code boolVariationDetail},
 * combining the result of a flag evaluation with an explanation of how it was calculated.
 * 
 * @param <T> the type of the wrapped value
 */
public class EvaluationDetail<T> {

  private final EvaluationReason reason;
  private final Integer variationIndex;
  private final T value;

  /**
   * Constructs an instance with all properties specified.
   * 
   * @param reason an {@link EvaluationReason} (should not be null)
   * @param variationIndex an optional variation index
   * @param value a value of the desired type
   */
  public EvaluationDetail(EvaluationReason reason, Integer variationIndex, T value) {
    this.value = value;
    this.variationIndex = variationIndex;
    this.reason = reason;
  }

  /**
   * Factory method for an arbitrary value.
   * 
   * @param <T> the type of the value
   * @param value a value of the desired type
   * @param variationIndex an optional variation index
   * @param reason an {@link EvaluationReason} (should not be null)
   * @return an {@link EvaluationDetail}
   */
  public static <T> EvaluationDetail<T> fromValue(T value, Integer variationIndex, EvaluationReason reason) {
    return new EvaluationDetail<T>(reason, variationIndex, value);
  }
  
  /**
   * Shortcut for creating an instance with an error result.
   * 
   * @param errorKind the type of error
   * @param defaultValue the application default value
   * @return an {@link EvaluationDetail}
   */
  public static EvaluationDetail<LDValue> error(EvaluationReason.ErrorKind errorKind, LDValue defaultValue) {
    return new EvaluationDetail<LDValue>(EvaluationReason.error(errorKind), null, LDValue.normalize(defaultValue));
  }
  
  /**
   * An object describing the main factor that influenced the flag evaluation value.
   * @return an {@link EvaluationReason}
   */
  public EvaluationReason getReason() {
    return reason;
  }

  /**
   * The index of the returned value within the flag's list of variations, e.g. 0 for the first variation -
   * or {@code null} if the default value was returned.
   * @return the variation index or null
   */
  public Integer getVariationIndex() {
    return variationIndex;
  }

  /**
   * The result of the flag evaluation. This will be either one of the flag's variations or the default
   * value that was passed to the {@code variation} method.
   * @return the flag value
   */
  public T getValue() {
    return value;
  }
  
  /**
   * Returns true if the flag evaluation returned the default value, rather than one of the flag's
   * variations.
   * @return true if this is the default value
   */
  public boolean isDefaultValue() {
    return variationIndex == null;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other instanceof EvaluationDetail) {
      @SuppressWarnings("unchecked")
      EvaluationDetail<T> o = (EvaluationDetail<T>)other;
      return objectsEqual(reason, o.reason) && objectsEqual(variationIndex, o.variationIndex) && objectsEqual(value, o.value);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return hashFrom(reason, variationIndex, value);
  }
  
  @Override
  public String toString() {
    return "{" + reason + "," + variationIndex + "," + value + "}";
  }
}
