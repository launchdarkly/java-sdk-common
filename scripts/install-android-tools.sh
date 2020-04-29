#!/bin/bash

set -e -x
set +o pipefail  # necessary because of how we're using "yes |"

echo 'export PATH="$PATH:/usr/local/share/android-sdk/tools/bin"' >> $BASH_ENV
echo 'export PATH="$PATH:/usr/local/share/android-sdk/platform-tools"' >> $BASH_ENV

HOMEBREW_NO_AUTO_UPDATE=1 brew tap homebrew/cask
HOMEBREW_NO_AUTO_UPDATE=1 brew cask install android-sdk

yes | sdkmanager "platform-tools" \
  "platforms;android-19" \
  "extras;intel;Hardware_Accelerated_Execution_Manager" \
  "build-tools;26.0.2" \
  "system-images;android-19;default;x86" \
  "emulator" | grep -v = || true

yes | sdkmanager --licenses

echo no | avdmanager create avd -n ci-android-avd -f -k "system-images;android-19;default;x86"

./gradlew -b build-android.gradle androidDependencies
