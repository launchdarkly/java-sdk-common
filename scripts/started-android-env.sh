#!/bin/bash

set -x -e

$(dirname $0)/circleci/circle-android wait-for-boot

while ! adb shell getprop ro.build.version.sdk; do
  sleep 1
done
