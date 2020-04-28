package com.launchdarkly.sdk;

import java.util.Iterator;

/**
 * Internal helper classes that serve the same purpose as Guava helpers. We do not use Guava in this
 * library because the Android SDK does not have it.
 */
abstract class Helpers {
  // This implementation is much simpler than Guava's Iterables.transform() because it does not attempt
  // to support remove().
  static <T, U> Iterable<U> transform(final Iterable<T> source, final Function<T, U> fn) {
    return new Iterable<U>() {
      @Override
      public Iterator<U> iterator() {
        final Iterator<T> sourceIterator = source.iterator();
        return new Iterator<U>() {
          @Override
          public boolean hasNext() {
            return sourceIterator.hasNext();
          }

          @Override
          public U next() {
            return fn.apply(sourceIterator.next());
          }
        };
      }
    };
  }
}
