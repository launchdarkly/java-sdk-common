package com.launchdarkly.sdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
