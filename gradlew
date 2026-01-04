#!/bin/sh

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039
        MAX_FD=$(ulimit -H -n) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in
      '' | soft) : ;;
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
        ;;
    esac
fi

# For Darwin, add options to specify how the application appears in the dock
if "$darwin"; then
    GRADLE_OPTS="$GRADLE_OPTS -Xdock:name=Gradle -Xdock:icon=$APP_HOME/media/gradle.icns"
fi

# For Cygwin, switch paths to Windows format before running java
if "$cygwin" ; then
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in
              -*) false ;;
              *)  true ;;
            esac
        then
            # No leading / character
            case $arg in
              [\\/]*) arg=$(cygpath --path --mixed "$arg") ;;
            esac
            # No colon character
            case $arg in
              *:*) arg=$(cygpath --path --mixed "$arg") ;;
            esac
        fi
        # Roll the args list around exactly as many times as there were args
        # shellcheck disable=SC2034
        _=$1
        shift
        set -- "$@" "$arg"
    done
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --add-opens (only if needed)
#   * DEFAULT_JVM_OPTS, DEFAULT_Gradle_OPTS and JAVA_OPTS
#   * -server
#   * GRADLE_OPTS (DEPRECATED!)
#   * -Dorg.gradle.appname
#   * -b
#   * APP_HOME
# The main class name is placed between -classpath and -D...appname so that the
# process can be easily identified by name.

# For Cygwin, switch the path to Windows format before running java
if "$cygwin" ; then
    # This is normally unused
    # shellcheck disable=SC2086
    exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
        -classpath "$CLASSPATH" \
        -Dorg.gradle.appname="$APP_BASE_NAME" \
        -b "$APP_HOME/gradle/buildfile.gradle" \
        "$APP_HOME"
else
    # This is normally unused
    # shellcheck disable=SC2086
    exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
        -classpath "$CLASSPATH" \
        -Dorg.gradle.appname="$APP_BASE_NAME" \
        -b "$APP_HOME/gradle/buildfile.gradle" \
        "$APP_HOME"
fi
