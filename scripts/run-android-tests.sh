#!/bin/bash

# What we want to do here is somewhat unusual: we want Android to run all of our tests from
# src/test/java, but run them in the Android emulator (to prove that we're only using Java APIs
# that our minimum Android API version supports). Normally, only tests in src/androidTest/java
# would be run that way. Also, Android needs a different JUnit test runner annotation on all of
# the test classes. So we can't just run the test code as-is.
#
# This script copies all the code from src/test/java into src/androidTest/java, except for the
# base class BaseTest.java, which is already defined in src/androidTest/java to provide the
# necessary test runner annotation. Then it runs the tests in the already-started emulator.

set -x -e -o pipefail

rsync -r ./src/test/java/ ./src/androidTest/java/ --exclude='BaseTest.java'

./gradlew -b build-android.gradle :connectedAndroidTest --console=plain -PdisablePreDex
