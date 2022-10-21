package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.launchdarkly.sdk.TestHelpers.setFromIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("javadoc")
public class LDUserTest extends BaseTest {
  private static enum OptionalStringAttributes {
    ip(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.ip(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateIp(s); } }),

    firstName(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.firstName(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateFirstName(s); } }),

    lastName(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.lastName(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateLastName(s); } }),

    email(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.email(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateEmail(s); } }),

    name(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.name(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateName(s); } }),

    avatar(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.avatar(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateAvatar(s); } }),

    country(
        new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.country(s); } },
          new BiFunction<LDUser.Builder, String, LDUser.Builder>()
          { public LDUser.Builder apply(LDUser.Builder b, String s) { return b.privateCountry(s); } });
    
    final String attribute;
    final BiFunction<LDUser.Builder, String, LDUser.Builder> setter;
    final BiFunction<LDUser.Builder, String, LDUser.Builder> privateSetter;
    
    private OptionalStringAttributes(
        BiFunction<LDUser.Builder, String, LDUser.Builder> setter,
        BiFunction<LDUser.Builder, String, LDUser.Builder> privateSetter
      ) {
      final String name = this.name();
      this.attribute = name;
      this.setter = setter;
      this.privateSetter = privateSetter;
    }
  };
  
  @Test
  public void builderSetsOptionalStringAttribute() {
    for (OptionalStringAttributes a: OptionalStringAttributes.values()) {
      String value = "value-of-" + a.name();
      LDUser.Builder builder = new LDUser.Builder("key");
      a.setter.apply(builder, value);
      LDContext user = builder.build();
      for (OptionalStringAttributes a1: OptionalStringAttributes.values()) {
        if (a1 == a) {
          assertEquals(a.toString(), LDValue.of(value), user.getValue(a1.attribute));
        } else {
          assertEquals(a.toString(), LDValue.ofNull(), user.getValue(a1.attribute)); 
        }
      }
      assertThat(user.isAnonymous(), is(false));
      assertThat(user.getPrivateAttributeCount(), equalTo(0));
    }
  }

  @Test
  public void builderSetsPrivateOptionalStringAttribute() {
    for (OptionalStringAttributes a: OptionalStringAttributes.values()) {
      String value = "value-of-" + a.name();
      LDUser.Builder builder = new LDUser.Builder("key");
      a.privateSetter.apply(builder, value);
      LDContext user = builder.build();
      for (OptionalStringAttributes a1: OptionalStringAttributes.values()) {
        if (a1 == a) {
          assertEquals(a.toString(), LDValue.of(value), user.getValue(a1.attribute));
        } else {
          assertEquals(a.toString(), LDValue.ofNull(), user.getValue(a1.attribute)); 
        }
      }
      assertThat(user.isAnonymous(), is(false));
      assertThat(user.getPrivateAttributeCount(), equalTo(1));
      assertThat(user.getPrivateAttribute(0).toString(), equalTo(a.attribute));
    }
  }
  
  @Test
  public void builderSetsCustomAttributes() {
    LDValue boolValue = LDValue.of(true),
        intValue = LDValue.of(2),
        floatValue = LDValue.of(2.5),
        stringValue = LDValue.of("x"),
        jsonValue = LDValue.buildArray().build();
    LDContext user = new LDUser.Builder("key")
        .custom("custom-bool", boolValue.booleanValue())
        .custom("custom-int", intValue.intValue())
        .custom("custom-float", floatValue.floatValue())
        .custom("custom-double", floatValue.doubleValue())
        .custom("custom-string", stringValue.stringValue())
        .custom("custom-json", jsonValue)
        .build();
    List<String> names = Arrays.asList("custom-bool", "custom-int", "custom-float", "custom-double", "custom-string", "custom-json");
    assertThat(user.getValue("custom-bool"), equalTo(boolValue));
    assertThat(user.getValue("custom-int"), equalTo(intValue));
    assertThat(user.getValue("custom-float"), equalTo(floatValue));
    assertThat(user.getValue("custom-double"), equalTo(floatValue));
    assertThat(user.getValue("custom-string"), equalTo(stringValue));
    assertThat(user.getValue("custom-json"), equalTo(jsonValue));
    assertThat(setFromIterable(user.getCustomAttributeNames()),
        equalTo(setFromIterable(names)));
    assertThat(user.getPrivateAttributeCount(), equalTo(0));
  }

  @Test
  public void builderSetsPrivateCustomAttributes() {
    LDValue boolValue = LDValue.of(true),
        intValue = LDValue.of(2),
        floatValue = LDValue.of(2.5),
        stringValue = LDValue.of("x"),
        jsonValue = LDValue.buildArray().build();
    LDContext user = new LDUser.Builder("key")
        .privateCustom("custom-bool", boolValue.booleanValue())
        .privateCustom("custom-int", intValue.intValue())
        .privateCustom("custom-float", floatValue.floatValue())
        .privateCustom("custom-double", floatValue.doubleValue())
        .privateCustom("custom-string", stringValue.stringValue())
        .privateCustom("custom-json", jsonValue)
        .build();
    List<String> names = Arrays.asList("custom-bool", "custom-int", "custom-float", "custom-double", "custom-string", "custom-json");
    assertThat(user.getValue("custom-bool"), equalTo(boolValue));
    assertThat(user.getValue("custom-int"), equalTo(intValue));
    assertThat(user.getValue("custom-float"), equalTo(floatValue));
    assertThat(user.getValue("custom-double"), equalTo(floatValue));
    assertThat(user.getValue("custom-string"), equalTo(stringValue));
    assertThat(user.getValue("custom-json"), equalTo(jsonValue));
    assertThat(setFromIterable(user.getCustomAttributeNames()),
        equalTo(setFromIterable(names)));
    assertThat(user.getPrivateAttributeCount(), equalTo(names.size()));
    for (int i = 0; i < names.size(); i++) {
      assertThat(user.getPrivateAttribute(i).toString(), equalTo(names.get(i)));
    }
  }

  @Test
  public void builderSetsKey() {
    assertThat(new LDUser.Builder("a").key("b").build().getKey(), equalTo("b"));
  }
  
  @Test
  public void canCopyContextWithBuilder() {
    LDContext user = new LDUser.Builder("key")
        .ip("127.0.0.1")
        .firstName("Bob")
        .lastName("Loblaw")
        .email("bob@example.com")
        .name("Bob Loblaw")
        .avatar("image")
        .anonymous(false)
        .country("US")
        .build();
    assertEquals(user, new LDUser.Builder(user).build());
    
    LDContext userWithPrivateAttrs = new LDUser.Builder("key").privateName("x").build();
    assertEquals(userWithPrivateAttrs, new LDUser.Builder(userWithPrivateAttrs).build());
    
    LDContext userWithCustomAttrs = new LDUser.Builder("key").custom("org", "LaunchDarkly").build();
    assertEquals(userWithCustomAttrs, new LDUser.Builder(userWithCustomAttrs).build());
  }

  @Test
  public void canSetAnonymous() {
    LDContext user1 = new LDUser.Builder("key").anonymous(true).build();
    assertThat(user1.isAnonymous(), is(true));
    
    LDContext user2 = new LDUser.Builder("key").anonymous(false).build();
    assertThat(user2.isAnonymous(), is(false));
  }
}
