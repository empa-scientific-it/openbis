#! /bin/bash

# Shutdown script for CISD openBIS Application Server on Unix / Linux systems
# -------------------------------------------------------------------------

source `dirname "$0"`/setup-env

"$JVM" -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     -jar ../jetty-dist/start.jar --stop

# Delete PID file
if [ -f "$JETTY_PID_FILE" ]; then
  PID=`cat $JETTY_PID_FILE`
  count=0
  while [ 1 ]; do
    isPIDRunning $PID
    if [ $? -ne 0 ]; then
      break
    fi
    count=$(($count+1))
    if [ $count -eq 10 ]; then
      break
    fi
    sleep 1
  done
        
  isPIDRunning $PID
  if [ $? -ne 0 ]; then
    rm -f "$JETTY_PID_FILE"
  else
    echo "Trying 'kill -KILL' on $PID..."
    kill -KILL $PID
    sleep 3
    isPIDRunning $PID
    if [ $? -ne 0 ]; then
      rm -f "$JETTY_PID_FILE"
    else
      echo "Failed to shutdown process $PID." > /dev/stderr
      exit 1
    fi
  fi
fi
