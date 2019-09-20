#!/usr/bin/env bash

#exec $SNAP/bin/java -jar $SNAP/jar/mqtt-cli-1.0.0.jar "$@"
exec bash -c "${SNAP}/usr/lib/jvm/java-11-openjdk-${SNAP_ARCH}/bin/java -version ; ${SNAP}/usr/lib/jvm/java-11-openjdk-${SNAP_ARCH}/bin/java -Djava.util.prefs.userRoot=\"$SNAP_USER_DATA\" -jar $SNAP/jar/mqtt-cli-1.0.0.jar $*"