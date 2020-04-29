#!/bin/bash

set -x -e -o pipefail

unset ANDROID_NDK_HOME

$ANDROID_HOME/emulator/emulator -avd ci-android-avd \
  -netdelay none -netspeed full -no-audio -no-window -no-snapshot -no-boot-anim
