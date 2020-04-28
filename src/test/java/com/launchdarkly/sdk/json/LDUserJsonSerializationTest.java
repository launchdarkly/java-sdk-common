package com.launchdarkly.sdk.json;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;

import org.junit.Test;

import static com.launchdarkly.sdk.json.JsonTestHelpers.verifyDeserializeInvalidJson;
import static com.launchdarkly.sdk.json.JsonTestHelpers.verifySerializeAndDeserialize;

@SuppressWarnings("javadoc")
public class LDUserJsonSerializationTest {
  @Test
  public void minimalJsonEncoding() throws Exception {
    LDUser user = new LDUser("userkey");
    verifySerializeAndDeserialize(user, "{\"key\":\"userkey\"}");
    
    verifyDeserializeInvalidJson(LDUser.class, "3");
    verifyDeserializeInvalidJson(LDUser.class, "{\"key\":\"userkey\",\"name\":3");
  }

  @Test
  public void defaultJsonEncodingWithoutPrivateAttributes() throws Exception {
    LDUser user = new LDUser.Builder("userkey")
        .secondary("s")
        .ip("i")
        .email("e")
        .name("n")
        .avatar("a")
        .firstName("f")
        .lastName("l")
        .country("c")
        .anonymous(true)
        .custom("c1", "v1")
        .build();
    LDValue expectedJson = LDValue.buildObject()
        .put("key", "userkey")
        .put("secondary", "s")
        .put("ip", "i")
        .put("email", "e")
        .put("name", "n")
        .put("avatar", "a")
        .put("firstName", "f")
        .put("lastName", "l")
        .put("country", "c")
        .put("anonymous", true)
        .put("custom", LDValue.buildObject().put("c1", "v1").build())
        .build();
    verifySerializeAndDeserialize(user, expectedJson.toJsonString());
  }

  @Test
  public void defaultJsonEncodingWithPrivateAttributes() throws Exception {
    LDUser user = new LDUser.Builder("userkey")
        .email("e")
        .privateName("n")
        .build();
    LDValue expectedJson = LDValue.buildObject()
        .put("key", "userkey")
        .put("email", "e")
        .put("name", "n")
        .put("privateAttributeNames", LDValue.buildArray().add("name").build())
        .build();
    verifySerializeAndDeserialize(user, expectedJson.toJsonString());
  }
}
