#!/usr/bin/env bash
set -e

export JAVA_HOME=/Users/rphilippe/Library/Java/JavaVirtualMachines/corretto-11.0.30/Contents/Home

TASK="${1:-assembleDist}"

./gradlew "$TASK"
