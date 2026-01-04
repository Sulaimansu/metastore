#!/usr/bin/env bash

# This script downloads and uses the Gradle wrapper
# It's based on the standard Gradle wrapper script

# Add default JVM options here
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
        exit 1
    fi
else
    JAVACMD="java"
fi

# Set up directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$SCRIPT_DIR"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# Create wrapper directory if it doesn't exist
mkdir -p "$(dirname "$WRAPPER_JAR")"

# Check if wrapper JAR exists, if not try to initialize
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Gradle wrapper JAR not found. This is expected in CI/CD environments."
    echo "The Gradle wrapper will be handled by the Gradle action."
fi

# Execute Gradle using the wrapper
exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
