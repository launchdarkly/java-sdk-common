package com.launchdarkly.sdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestHelpers {
  public static <T> List<T> listFromIterable(Iterable<T> it) {
    List<T> list = new ArrayList<>();
    it.forEach(list::add);
    return list;
  }

  public static <T> Set<T> setFromIterable(Iterable<T> it) {
    Set<T> set = new HashSet<>();
    it.forEach(set::add);
    return set;
  }
}
