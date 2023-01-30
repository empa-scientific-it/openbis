#! /bin/bash

# Startup script for CISD openBIS Application Server on Unix / Linux systems
# -------------------------------------------------------------------------

source `dirname "$0"`/setup-env



checkNotRoot

bin/status.sh -q
if [ $? -eq 0 ]; then
  echo "openBIS AS already running, shut it down before starting a new one." 2>&1
  exit 1
fi

disableJythonByProperty

`dirname "$0"`/autosymlink.sh $JETTY_BIN_DIR/../webapps/openbis/WEB-INF/lib

"$JVM" -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     $OPENBIS_OPTS \
     -Dpython.path=$JETTY_LIB_PATH \
     -Dnative.libpath=$JETTY_BIN_DIR/../webapps/openbis/WEB-INF/lib/native \
     -jar ../jetty-dist/start.jar --lib=lib/logging/*.jar:webapps/openbis/WEB-INF/lib/*.jar etc/jetty-started.xml >> logs/jetty.out 2>&1 &

# Write PID to PID file
echo $! > "$JETTY_PID_FILE"     
