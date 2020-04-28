package com.launchdarkly.sdk.json;

import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.EvaluationReason;
import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import static com.launchdarkly.sdk.EvaluationDetail.NO_VARIATION;
import static com.launchdarkly.sdk.EvaluationReason.ErrorKind.CLIENT_NOT_READY;
import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerializeAndDeserialize;

@SuppressWarnings("javadoc")
public class EvaluationDetailJsonSerializationTest {
  @Test
  public void detailJsonSerializations() throws Exception {
    verifySerializeAndDeserialize(EvaluationDetail.fromValue(LDValue.of("x"), 1, EvaluationReason.off()),
        "{\"value\":\"x\",\"variationIndex\":1,\"reason\":{\"kind\":\"OFF\"}}");
    
    // variationIndex of NO_VARIATION is omitted, rather than serialized as -1
    verifySerializeAndDeserialize(
        EvaluationDetail.fromValue(LDValue.of("x"), NO_VARIATION, EvaluationReason.error(CLIENT_NOT_READY)),
        "{\"value\":\"x\",\"reason\":{\"kind\":\"ERROR\",\"errorKind\":\"CLIENT_NOT_READY\"}}");

    // Due to how generic types work in Gson, simply calling Gson.fromJson<EvaluationDetail<T>> will *not*
    // use any custom deserialization for type T; it will behave as if T were LDValue. However, it should
    // correctly pick up the type signature if you deserialize an object that contains such a value. That
    // scenario is covered in ReflectiveFrameworksTest.
  }
}
