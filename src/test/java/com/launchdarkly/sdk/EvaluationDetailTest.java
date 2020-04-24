package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("javadoc")
public class EvaluationDetailTest {
  @Test
  public void equalInstancesAreEqual() {
    List<List<EvaluationDetail<String>>> testValues = new ArrayList<>();
    for (EvaluationReason reason: new EvaluationReason[] { EvaluationReason.off(), EvaluationReason.fallthrough() }) {
      for (int variation = 0; variation < 2; variation++) {
        for (String value: new String[] { "a", "b" }) {
          List<EvaluationDetail<String>> equalValues = new ArrayList<>();
          for (int i = 0; i < 2; i++) {
            equalValues.add(new EvaluationDetail<>(reason, variation, value));
          }
          testValues.add(equalValues);
        }
      }
    }
    TestHelpers.doEqualityTests(testValues);
  }
}
