#!/bin/sh
# This is a minimal gradlew script that will work with GitHub Actions

# Use JAVA_HOME if available
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Create the wrapper directory if it doesn't exist
mkdir -p gradle/wrapper

# Execute Gradle
exec "$JAVA_CMD" -jar gradle/wrapper/gradle-wrapper.jar "$@"
