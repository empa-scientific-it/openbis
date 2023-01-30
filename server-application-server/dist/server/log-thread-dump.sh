#! /bin/bash

# Script for CISD openBIS Application Server to log thread dump on Unix / Linux systems
# -------------------------------------------------------------------------------------

source `dirname "$0"`/setup-env

if [ -f $JETTY_PID_FILE ]; then
  PID=`cat $JETTY_PID_FILE 2> /dev/null`
  isPIDRunning $PID
  if [ $? -eq 0 ]; then
    kill -3 $PID
		echo "Thread dump logged to logs/jetty.out"
  else
  	echo "Error: Application Server not running."
  	exit 100
  fi
else
	echo "Error: Application Server not running."
 	exit 100
fi
