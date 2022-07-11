package com.launchdarkly.sdk;

abstract class Errors {
  static final String ATTR_EMPTY = "attribute reference cannot be empty";
  static final String ATTR_EXTRA_SLASH = "attribute reference contained a double slash or a trailing slash";
  static final String ATTR_INVALID_ESCAPE =
      "attribute reference contained an escape character (~) that was not followed by 0 or 1";
}
