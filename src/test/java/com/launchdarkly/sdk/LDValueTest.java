package com.launchdarkly.sdk;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.launchdarkly.sdk.TestHelpers.listFromIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("javadoc")
public class LDValueTest {
  private static final int someInt = 3;
  private static final long someLong = 3;
  private static final float someFloat = 3.25f;
  private static final double someDouble = 3.25d;
  private static final String someString = "hi";
  
  private static final LDValue aTrueBoolValue = LDValue.of(true);
  private static final LDValue anIntValue = LDValue.of(someInt);
  private static final LDValue aLongValue = LDValue.of(someLong);
  private static final LDValue aFloatValue = LDValue.of(someFloat);
  private static final LDValue aDoubleValue = LDValue.of(someDouble);
  private static final LDValue aStringValue = LDValue.of(someString);
  private static final LDValue aNumericLookingStringValue = LDValue.of("3");
  private static final LDValue anArrayValue = LDValue.buildArray().add(LDValue.of(3)).build();
  private static final LDValue anObjectValue = LDValue.buildObject().put("1", LDValue.of("x")).build();
  
  @Test
  public void canGetValueAsBoolean() {
    assertEquals(LDValueType.BOOLEAN, aTrueBoolValue.getType());
    assertTrue(aTrueBoolValue.booleanValue());
  }
  
  @Test
  public void nonBooleanValueAsBooleanIsFalse() {
    LDValue[] values = new LDValue[] {
        LDValue.ofNull(),
        aStringValue,
        anIntValue,
        aLongValue,
        aFloatValue,
        aDoubleValue,
        anArrayValue,
        anObjectValue,
    };
    for (LDValue value: values) {
      assertNotEquals(value.toString(), LDValueType.BOOLEAN, value.getType());
      assertFalse(value.toString(), value.booleanValue());
    }
  }
  
  @Test
  public void canGetValueAsString() {
    assertEquals(LDValueType.STRING, aStringValue.getType());
    assertEquals(someString, aStringValue.stringValue());
  }

  @Test
  public void nonStringValueAsStringIsNull() {
    LDValue[] values = new LDValue[] {
        LDValue.ofNull(),
        aTrueBoolValue,
        anIntValue,
        aLongValue,
        aFloatValue,
        aDoubleValue,
        anArrayValue,
        anObjectValue
    };
    for (LDValue value: values) {
      assertNotEquals(value.toString(), LDValueType.STRING, value.getType());
      assertNull(value.toString(), value.stringValue());
    }
  }
  
  @Test
  public void nullStringConstructorGivesNullInstance() {
    assertEquals(LDValue.ofNull(), LDValue.of((String)null));
  }
  
  @Test
  public void canGetIntegerValueOfAnyNumericType() {
    LDValue[] values = new LDValue[] {
        LDValue.of(3),
        LDValue.of(3L),
        LDValue.of(3.0f),
        LDValue.of(3.25f),
        LDValue.of(3.75f),
        LDValue.of(3.0d),
        LDValue.of(3.25d),
        LDValue.of(3.75d)
    };
    for (LDValue value: values) {
      assertEquals(value.toString(), LDValueType.NUMBER, value.getType());
      assertEquals(value.toString(), 3, value.intValue());
      assertEquals(value.toString(), 3L, value.longValue());
    }
  }
  
  @Test
  public void canGetFloatValueOfAnyNumericType() {
    LDValue[] values = new LDValue[] {
        LDValue.of(3),
        LDValue.of(3L),
        LDValue.of(3.0f),
        LDValue.of(3.0d),
    };
    for (LDValue value: values) {
      assertEquals(value.toString(), LDValueType.NUMBER, value.getType());
      assertEquals(value.toString(), 3.0f, value.floatValue(), 0);
    }
  }
  
  @Test
  public void canGetDoubleValueOfAnyNumericType() {
    LDValue[] values = new LDValue[] {
        LDValue.of(3),
        LDValue.of(3L),
        LDValue.of(3.0f),
        LDValue.of(3.0d),
    };
    for (LDValue value: values) {
      assertEquals(value.toString(), LDValueType.NUMBER, value.getType());
      assertEquals(value.toString(), 3.0d, value.doubleValue(), 0);
    }
  }

  @Test
  public void nonNumericValueAsNumberIsZero() {
    LDValue[] values = new LDValue[] {
        LDValue.ofNull(),
        aTrueBoolValue,
        aStringValue,
        aNumericLookingStringValue,
        anArrayValue,
        anObjectValue
    };
    for (LDValue value: values) {
      assertNotEquals(value.toString(), LDValueType.NUMBER, value.getType());
      assertEquals(value.toString(), 0, value.intValue());
      assertEquals(value.toString(), 0f, value.floatValue(), 0);
      assertEquals(value.toString(), 0d, value.doubleValue(), 0);
    }
  }
  
  @Test
  public void canGetSizeOfArray() {
    assertEquals(1, anArrayValue.size());
  }
  
  @Test
  public void arrayCanGetItemByIndex() {
    assertEquals(LDValueType.ARRAY, anArrayValue.getType());
    assertEquals(LDValue.of(3), anArrayValue.get(0));
    assertEquals(LDValue.ofNull(), anArrayValue.get(-1));
    assertEquals(LDValue.ofNull(), anArrayValue.get(1));
  }
  
  @Test
  public void arrayCanBeEnumerated() {
    LDValue a = LDValue.of("a");
    LDValue b = LDValue.of("b");
    List<LDValue> values = new ArrayList<>();
    for (LDValue v: LDValue.buildArray().add(a).add(b).build().values()) {
      values.add(v);
    }
    List<LDValue> expected = new ArrayList<>();
    addAll(expected, a, b);
    assertEquals(expected, values);
  }
  
  @Test
  public void arrayBuilderCanAddValuesAfterBuilding() {
    ArrayBuilder builder = LDValue.buildArray();
    builder.add("a");
    LDValue firstArray = builder.build();
    assertEquals(1, firstArray.size());
    builder.add("b");
    LDValue secondArray = builder.build();
    assertEquals(2, secondArray.size());
    assertEquals(1, firstArray.size());
  }
  
  @Test
  public void nonArrayValuesBehaveLikeEmptyArray() {
    LDValue[] values = new LDValue[] {
        LDValue.ofNull(),
        aTrueBoolValue,
        anIntValue,
        aLongValue,
        aFloatValue,
        aDoubleValue,
        aStringValue,
        aNumericLookingStringValue,
    };
    for (LDValue value: values) {
      assertEquals(value.toString(), 0, value.size());
      assertEquals(value.toString(), LDValue.of(null), value.get(-1));
      assertEquals(value.toString(), LDValue.of(null), value.get(0));
      for (@SuppressWarnings("unused") LDValue v: value.values()) {
        fail(value.toString());
      }
    }
  }
  
  @Test
  public void canGetSizeOfObject() {
    assertEquals(1, anObjectValue.size());
  }
  
  @Test
  public void objectCanGetValueByName() {
    assertEquals(LDValueType.OBJECT, anObjectValue.getType());
    assertEquals(LDValue.of("x"), anObjectValue.get("1"));
    assertEquals(LDValue.ofNull(), anObjectValue.get(null));
    assertEquals(LDValue.ofNull(), anObjectValue.get("2"));
  }
  
  @Test
  public void objectKeysCanBeEnumerated() {
    List<String> keys = new ArrayList<>();
    for (String key: LDValue.buildObject().put("1", LDValue.of("x")).put("2", LDValue.of("y")).build().keys()) {
      keys.add(key);
    }
    keys.sort(null);
    List<String> expected = new ArrayList<>();
    addAll(expected, "1", "2");
    assertEquals(expected, keys);
  }

  @Test
  public void objectValuesCanBeEnumerated() {
    List<String> values = new ArrayList<>();
    for (LDValue value: LDValue.buildObject().put("1", LDValue.of("x")).put("2", LDValue.of("y")).build().values()) {
      values.add(value.stringValue());
    }
    values.sort(null);
    List<String> expected = new ArrayList<>();
    addAll(expected, "x", "y");
    assertEquals(expected, values);
  }
  
  @Test
  public void objectBuilderCanAddValuesAfterBuilding() {
    ObjectBuilder builder = LDValue.buildObject();
    builder.put("a", 1);
    LDValue firstObject = builder.build();
    assertEquals(1, firstObject.size());
    builder.put("b", 2);
    LDValue secondObject = builder.build();
    assertEquals(2, secondObject.size());
    assertEquals(1, firstObject.size());
  }
  
  @Test
  public void nonObjectValuesBehaveLikeEmptyObject() {
    LDValue[] values = new LDValue[] {
        LDValue.ofNull(),
        aTrueBoolValue,
        anIntValue,
        aLongValue,
        aFloatValue,
        aDoubleValue,
        aStringValue,
        aNumericLookingStringValue,
    };
    for (LDValue value: values) {
      assertEquals(value.toString(), LDValue.of(null), value.get(null));
      assertEquals(value.toString(), LDValue.of(null), value.get("1"));
      for (@SuppressWarnings("unused") String key: value.keys()) {
        fail(value.toString());
      }
    }
  }

  @Test
  public void equalValuesAreEqual()
  {
    List<List<LDValue>> testValues = asList(
        asList(LDValue.ofNull(), LDValue.ofNull()),
        asList(LDValue.of(true), LDValue.of(true)),
        asList(LDValue.of(false), LDValue.of(false)),
        asList(LDValue.of(1), LDValue.of(1)),
        asList(LDValue.of(2), LDValue.of(2)),
        asList(LDValue.of(3), LDValue.of(3.0f)),
        asList(LDValue.of("a"), LDValue.of("a")),
        asList(LDValue.of("b"), LDValue.of("b")),
        
        // arrays use deep equality
        asList(LDValue.buildArray().build(), LDValue.buildArray().build()),
        asList(LDValue.buildArray().add("a").build(), LDValue.buildArray().add("a").build()),
        asList(LDValue.buildArray().add("a").add("b").build(),
            LDValue.buildArray().add("a").add("b").build()),
        asList(LDValue.buildArray().add("a").add("c").build(),
            LDValue.buildArray().add("a").add("c").build()),
        asList(LDValue.buildArray().add("a").add(LDValue.buildArray().add("b").add("c").build()).build(),
            LDValue.buildArray().add("a").add(LDValue.buildArray().add("b").add("c").build()).build()),
        asList(LDValue.buildArray().add("a").add(LDValue.buildArray().add("b").add("d").build()).build(),
            LDValue.buildArray().add("a").add(LDValue.buildArray().add("b").add("d").build()).build()),
        
        // objects use deep equality
        asList(LDValue.buildObject().build(), LDValue.buildObject().build()),
        asList(LDValue.buildObject().put("a", LDValue.of(1)).build(),
            LDValue.buildObject().put("a", LDValue.of(1)).build()),
        asList(LDValue.buildObject().put("a", LDValue.of(2)).build(),
            LDValue.buildObject().put("a", LDValue.of(2)).build()),
        asList(LDValue.buildObject().put("a", LDValue.of(1)).put("b", LDValue.of(2)).build(),
            LDValue.buildObject().put("b", LDValue.of(2)).put("a", LDValue.of(1)).build())
        );
    TestHelpers.doEqualityTests(testValues);
  }
  
  @Test
  public void canUseLongTypeForNumberGreaterThanMaxInt() {
    long n = (long)Integer.MAX_VALUE + 1;
    assertEquals(n, LDValue.of(n).longValue());
    assertEquals(n, LDValue.Convert.Long.toType(LDValue.of(n)).longValue());
    assertEquals(n, LDValue.Convert.Long.fromType(n).longValue());
  }

  @Test
  public void canUseDoubleTypeForNumberGreaterThanMaxFloat() {
    double n = (double)Float.MAX_VALUE + 1;
    assertEquals(n, LDValue.of(n).doubleValue(), 0);
    assertEquals(n, LDValue.Convert.Double.toType(LDValue.of(n)).doubleValue(), 0);
    assertEquals(n, LDValue.Convert.Double.fromType(n).doubleValue(), 0);
  }

  @Test
  public void testTypeConversions() {
    testTypeConversion(LDValue.Convert.Boolean, new Boolean[] { true, false }, LDValue.of(true), LDValue.of(false));
    testTypeConversion(LDValue.Convert.Integer, new Integer[] { 1, 2 }, LDValue.of(1), LDValue.of(2));
    testTypeConversion(LDValue.Convert.Long, new Long[] { 1L, 2L }, LDValue.of(1L), LDValue.of(2L));
    testTypeConversion(LDValue.Convert.Float, new Float[] { 1.5f, 2.5f }, LDValue.of(1.5f), LDValue.of(2.5f));
    testTypeConversion(LDValue.Convert.Double, new Double[] { 1.5d, 2.5d }, LDValue.of(1.5d), LDValue.of(2.5d));
    testTypeConversion(LDValue.Convert.String, new String[] { "a", "b" }, LDValue.of("a"), LDValue.of("b"));
  }
  
  private <T> void testTypeConversion(LDValue.Converter<T> converter, T[] values, LDValue... ldValues) {
    ArrayBuilder ab = LDValue.buildArray();
    for (LDValue v: ldValues) {
      ab.add(v);
    }
    LDValue arrayValue = ab.build();
    assertEquals(arrayValue, converter.arrayOf(values));
    List<T> list = new ArrayList<>();
    for (T v: values) {
      list.add(v);
    }
    assertEquals(arrayValue, converter.arrayFrom(list));
    assertEquals(list, listFromIterable(arrayValue.valuesAs(converter)));
    
    ObjectBuilder ob = LDValue.buildObject();
    int i = 0;
    for (LDValue v: ldValues) {
      ob.put(String.valueOf(++i), v);
    }
    LDValue objectValue = ob.build();
    Map<String, T> map = new HashMap<>();
    i = 0;
    for (T v: values) {
      map.put(String.valueOf(++i), v);
    }
    assertEquals(objectValue, converter.objectFrom(map));
  }
}
