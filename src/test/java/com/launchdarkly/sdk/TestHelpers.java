package com.launchdarkly.sdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings("javadoc")
public class TestHelpers {
  public static <T> List<T> listFromIterable(Iterable<T> it) {
    List<T> list = new ArrayList<>();
    for (T t: it) {
      list.add(t);
    }
    return list;
  }

  public static <T> Set<T> setFromIterable(Iterable<T> it) {
    Set<T> set = new HashSet<>();
    for (T t: it) {
      set.add(t);
    }
    return set;
  }
  
  public static <T> void doEqualityTests(List<List<T>> testValues) {
    // Each element of testValues should be a list of two values that should be equal to each other,
    // but not equal to any of the other values in testValues. It would have been nicer to use a
    // single function that *creates* a value and call it twice, but since we can't use lambdas in
    // Java 7 that would be very verbose.
    for (int i = 0; i < testValues.size(); i++) {
      List<T> equalValues = testValues.get(i);
      assertEquals(equalValues.get(0), equalValues.get(0));
      assertEquals(equalValues.get(0), equalValues.get(1));
      assertEquals(equalValues.get(1), equalValues.get(0));
      assertEquals(equalValues.get(0).hashCode(), equalValues.get(1).hashCode());
      for (int j = 0; j < testValues.size(); j++) {
        if (j != i) {
          assertNotEquals(testValues.get(j).get(0), equalValues.get(0));
          assertNotEquals(equalValues.get(0), testValues.get(j).get(0));
        }
      }
    }
  }
}
