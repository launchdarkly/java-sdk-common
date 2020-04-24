package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("javadoc")
public class EvaluationDetailTest {
  @Test
  public void equalInstancesAreEqual() {
    List<List<EvaluationDetail<String>>> testValues = Arrays.asList(
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.off(), 0, "a"),
            new EvaluationDetail<>(EvaluationReason.off(), 0, "a")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 0, "a"),
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 0, "a")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.off(), 1, "a"),
            new EvaluationDetail<>(EvaluationReason.off(), 1, "a")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 1, "a"),
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 1, "a")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.off(), 0, "b"),
            new EvaluationDetail<>(EvaluationReason.off(), 0, "b")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 0, "b"),
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 0, "b")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.off(), 1, "b"),
            new EvaluationDetail<>(EvaluationReason.off(), 1, "b")
        ),
        Arrays.asList(
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 1, "b"),
            new EvaluationDetail<>(EvaluationReason.fallthrough(), 1, "b")
        )
    );
    TestHelpers.doEqualityTests(testValues);
  }
}
