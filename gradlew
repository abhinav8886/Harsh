#!/bin/sh
# Gradle startup script for POSIX systems
exec "${JAVA_HOME:-/usr}/bin/java" -version >/dev/null 2>&1 || {
  echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  exit 1
}
DIRNAME=`dirname "$0"`
GRADLE_WRAPPER_JAR="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle Wrapper..."
    curl -sL https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar -o "$GRADLE_WRAPPER_JAR" || true
fi
exec "$JAVA_HOME/bin/java" -jar "$GRADLE_WRAPPER_JAR" "$@"
