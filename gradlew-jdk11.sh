#!/usr/bin/env bash
set -e

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 11)}"

TASK="${1:-assembleDist}"

./gradlew "$TASK"
