package com.launchdarkly.sdk;

import org.junit.Test;

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
}
