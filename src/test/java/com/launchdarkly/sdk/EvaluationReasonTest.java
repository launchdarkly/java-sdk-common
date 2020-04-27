package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.List;

import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.CLIENT_NOT_READY;
import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.WRONG_TYPE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SuppressWarnings("javadoc")
public class EvaluationReasonTest {
  @Test
  public void simpleStringRepresentations() {
    assertEquals("OFF", EvaluationReason.off().toString());
    assertEquals("FALLTHROUGH", EvaluationReason.fallthrough().toString());
    assertEquals("TARGET_MATCH", EvaluationReason.targetMatch().toString());
    assertEquals("RULE_MATCH(1,id)", EvaluationReason.ruleMatch(1, "id").toString());
    assertEquals("PREREQUISITE_FAILED(key)", EvaluationReason.prerequisiteFailed("key").toString());
    assertEquals("ERROR(FLAG_NOT_FOUND)", EvaluationReason.error(EvaluationReason.ErrorKind.FLAG_NOT_FOUND).toString());
    assertEquals("ERROR(EXCEPTION,java.lang.Exception: something happened)",
        EvaluationReason.exception(new Exception("something happened")).toString());
  }
  
  @Test
  public void errorInstancesAreReused() {
    for (EvaluationReason.ErrorKind errorKind: EvaluationReason.ErrorKind.values()) {
      EvaluationReason r0 = EvaluationReason.error(errorKind);
      assertEquals(errorKind, r0.getErrorKind());
      EvaluationReason r1 = EvaluationReason.error(errorKind);
      assertSame(r0, r1);
    }
  }
  
  @Test
  public void equalInstancesAreEqual() {
    List<List<EvaluationReason>> testValues = asList(
        asList(EvaluationReason.off(), EvaluationReason.off()),
        asList(EvaluationReason.fallthrough(), EvaluationReason.fallthrough()),
        asList(EvaluationReason.targetMatch(), EvaluationReason.targetMatch()),
        asList(EvaluationReason.ruleMatch(1, "id1"), EvaluationReason.ruleMatch(1, "id1")),
        asList(EvaluationReason.ruleMatch(1, "id2"), EvaluationReason.ruleMatch(1, "id2")),
        asList(EvaluationReason.ruleMatch(2, "id1"), EvaluationReason.ruleMatch(2, "id1")),
        asList(EvaluationReason.prerequisiteFailed("a"), EvaluationReason.prerequisiteFailed("a")),
        asList(EvaluationReason.error(CLIENT_NOT_READY), EvaluationReason.error(CLIENT_NOT_READY)),
        asList(EvaluationReason.error(WRONG_TYPE), EvaluationReason.error(WRONG_TYPE))
    );
    TestHelpers.doEqualityTests(testValues);
  }
}
