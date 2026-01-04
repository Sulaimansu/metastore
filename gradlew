#!/bin/sh
exec /bin/sh -c '"$0" "$@"' -- \
'exec java -jar $(dirname "$0")/gradle/wrapper/gradle-wrapper.jar "$@"'
