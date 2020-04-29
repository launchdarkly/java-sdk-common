#!/bin/bash

set -x -e -o pipefail

adb emu kill || true
