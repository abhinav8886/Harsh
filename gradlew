
#!/bin/sh
# Reliable Gradle Wrapper launcher
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if [ -n "${JAVA_HOME:-}" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
  echo "ERROR: Java was not found." >&2
  exit 1
fi

GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "ERROR: Gradle wrapper JAR is missing: $GRADLE_WRAPPER_JAR" >&2
  exit 1
fi

exec "$JAVACMD" -classpath "$GRADLE_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
