#!/bin/sh
# Reliable POSIX launcher for the bundled Gradle Wrapper.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ -n "${JAVA_HOME:-}" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi
if ! command -v "$JAVACMD" >/dev/null 2>&1; then
  echo "Error: Java was not found. Install JDK 17 and/or set JAVA_HOME." >&2
  exit 1
fi
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "Error: Gradle wrapper JAR is missing: $GRADLE_WRAPPER_JAR" >&2
  exit 1
fi
exec "$JAVACMD" -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
