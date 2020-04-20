package com.launchdarkly.sdk.json;

import com.launchdarkly.sdk.EvaluationReason;

import org.junit.Test;

import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerialize;
import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerializeAndDeserialize;

public class EvaluationReasonJsonSerializationTest {
  @Test
  public void reasonJsonSerializations() throws Exception {
    verifySerializeAndDeserialize(EvaluationReason.off(), "{\"kind\":\"OFF\"}");
    verifySerializeAndDeserialize(EvaluationReason.fallthrough(), "{\"kind\":\"FALLTHROUGH\"}");
    verifySerializeAndDeserialize(EvaluationReason.targetMatch(), "{\"kind\":\"TARGET_MATCH\"}");
    verifySerializeAndDeserialize(EvaluationReason.ruleMatch(1, "id"),
        "{\"kind\":\"RULE_MATCH\",\"ruleIndex\":1,\"ruleId\":\"id\"}");
    verifySerializeAndDeserialize(EvaluationReason.prerequisiteFailed("key"),
        "{\"kind\":\"PREREQUISITE_FAILED\",\"prerequisiteKey\":\"key\"}");
    verifySerializeAndDeserialize(EvaluationReason.error(EvaluationReason.ErrorKind.FLAG_NOT_FOUND),
        "{\"kind\":\"ERROR\",\"errorKind\":\"FLAG_NOT_FOUND\"}");
  }

  @Test
  public void errorSerializationWithException() throws Exception {
    // We do *not* want the JSON representation to include the exception, because that is used in events, and
    // the LD event service won't know what to do with that field (which will also contain a big stacktrace).
    EvaluationReason reason = EvaluationReason.exception(new Exception("something happened"));
    String expectedJsonString = "{\"kind\":\"ERROR\",\"errorKind\":\"EXCEPTION\"}";
    verifySerialize(reason, expectedJsonString);
  }
}
