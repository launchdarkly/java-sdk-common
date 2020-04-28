package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.List;

import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.CLIENT_NOT_READY;
import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.FLAG_NOT_FOUND;
import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.WRONG_TYPE;
import static com.launchdarkly.sdk.EvaluationReason.Kind.ERROR;
import static com.launchdarkly.sdk.EvaluationReason.Kind.FALLTHROUGH;
import static com.launchdarkly.sdk.EvaluationReason.Kind.OFF;
import static com.launchdarkly.sdk.EvaluationReason.Kind.PREREQUISITE_FAILED;
import static com.launchdarkly.sdk.EvaluationReason.Kind.RULE_MATCH;
import static com.launchdarkly.sdk.EvaluationReason.Kind.TARGET_MATCH;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@SuppressWarnings("javadoc")
public class EvaluationReasonTest {
  @Test
  public void basicProperties() {
    assertEquals(OFF, EvaluationReason.off().getKind());
    assertEquals(FALLTHROUGH, EvaluationReason.fallthrough().getKind());
    assertEquals(TARGET_MATCH, EvaluationReason.targetMatch().getKind());
    assertEquals(RULE_MATCH, EvaluationReason.ruleMatch(1, "id").getKind());
    assertEquals(PREREQUISITE_FAILED, EvaluationReason.prerequisiteFailed("key").getKind());
    assertEquals(ERROR, EvaluationReason.error(FLAG_NOT_FOUND).getKind());
    
    assertEquals(1, EvaluationReason.ruleMatch(1, "id").getRuleIndex());
    assertEquals(-1, EvaluationReason.off().getRuleIndex());
    assertEquals(-1, EvaluationReason.fallthrough().getRuleIndex());
    assertEquals(-1, EvaluationReason.targetMatch().getRuleIndex());
    assertEquals(-1, EvaluationReason.prerequisiteFailed("key").getRuleIndex());
    assertEquals(-1, EvaluationReason.error(FLAG_NOT_FOUND).getRuleIndex());

    assertEquals("id", EvaluationReason.ruleMatch(1, "id").getRuleId());
    assertNull(EvaluationReason.off().getRuleId());
    assertNull(EvaluationReason.fallthrough().getRuleId());
    assertNull(EvaluationReason.targetMatch().getRuleId());
    assertNull(EvaluationReason.prerequisiteFailed("key").getRuleId());
    assertNull(EvaluationReason.error(FLAG_NOT_FOUND).getRuleId());
    
    assertEquals("key", EvaluationReason.prerequisiteFailed("key").getPrerequisiteKey());
    assertNull(EvaluationReason.off().getPrerequisiteKey());
    assertNull(EvaluationReason.fallthrough().getPrerequisiteKey());
    assertNull(EvaluationReason.targetMatch().getPrerequisiteKey());
    assertNull(EvaluationReason.ruleMatch(1, "id").getPrerequisiteKey());
    assertNull(EvaluationReason.error(FLAG_NOT_FOUND).getPrerequisiteKey());
    
    assertEquals(FLAG_NOT_FOUND, EvaluationReason.error(FLAG_NOT_FOUND).getErrorKind());
    assertNull(EvaluationReason.off().getErrorKind());
    assertNull(EvaluationReason.fallthrough().getErrorKind());
    assertNull(EvaluationReason.targetMatch().getErrorKind());
    assertNull(EvaluationReason.ruleMatch(1, "id").getErrorKind());
    assertNull(EvaluationReason.prerequisiteFailed("key").getErrorKind());
    
    Exception e = new Exception("sorry");
    assertEquals(e, EvaluationReason.exception(e).getException());
    assertNull(EvaluationReason.off().getException());
    assertNull(EvaluationReason.fallthrough().getException());
    assertNull(EvaluationReason.targetMatch().getException());
    assertNull(EvaluationReason.ruleMatch(1, "id").getException());
    assertNull(EvaluationReason.prerequisiteFailed("key").getException());
    assertNull(EvaluationReason.error(FLAG_NOT_FOUND).getException());
  }
  
  @Test
  public void simpleStringRepresentations() {
    assertEquals("OFF", EvaluationReason.off().toString());
    assertEquals("FALLTHROUGH", EvaluationReason.fallthrough().toString());
    assertEquals("TARGET_MATCH", EvaluationReason.targetMatch().toString());
    assertEquals("RULE_MATCH(1)", EvaluationReason.ruleMatch(1, null).toString());
    assertEquals("RULE_MATCH(1,id)", EvaluationReason.ruleMatch(1, "id").toString());
    assertEquals("PREREQUISITE_FAILED(key)", EvaluationReason.prerequisiteFailed("key").toString());
    assertEquals("ERROR(FLAG_NOT_FOUND)", EvaluationReason.error(FLAG_NOT_FOUND).toString());
    assertEquals("ERROR(EXCEPTION)", EvaluationReason.exception(null).toString());
    assertEquals("ERROR(EXCEPTION,java.lang.Exception: something happened)",
        EvaluationReason.exception(new Exception("something happened")).toString());
  }
  
  @Test
  public void instancesAreReused() {
    assertSame(EvaluationReason.off(), EvaluationReason.off());
    assertSame(EvaluationReason.fallthrough(), EvaluationReason.fallthrough());
    assertSame(EvaluationReason.targetMatch(), EvaluationReason.targetMatch());
    
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
