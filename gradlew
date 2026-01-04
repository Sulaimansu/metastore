#!/bin/bash

# Use the JAVA_HOME environment variable if it's set
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Check if java command exists
if ! command -v "$JAVA_CMD" &> /dev/null; then
    echo "Java is not installed or JAVA_HOME is not set correctly"
    exit 1
fi

# Set the main class and classpath
MAIN_CLASS=org.gradle.wrapper.GradleWrapperMain
CLASSPATH="gradle/wrapper/gradle-wrapper.jar"

# Execute the command
exec "$JAVA_CMD" -classpath "$CLASSPATH" "$MAIN_CLASS" "$@"
