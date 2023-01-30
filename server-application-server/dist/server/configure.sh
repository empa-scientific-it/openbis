#!/bin/bash

#
# change to installation directory
#

bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD=`dirname $bin`
cd $WD

SCRIPT=./`basename $0`

command=$1

case "$command" in
	log-service-calls)
	 	mkdir -p ../.control
		if [ "$2" == "off" ]; then
			touch ../.control/log-service-call-start-off
			echo "Switched off logging of service calls."
		else
			touch ../.control/log-service-call-start-on
			echo "Switched on logging of service calls."
		fi
	;;
	log-long-running-invocations)
	 	mkdir -p ../.control
		if [ "$2" == "off" ]; then
			touch ../.control/long-running-thread-logging-off
			echo "Switched off logging of long running invocations."
		else
			touch ../.control/long-running-thread-logging-on
			echo "Switched on logging of long-running-invocations."
		fi
	;;	
	debug-db-connections)
		mkdir -p ../.control
		if [ "$2" == "off" ]; then
		 	touch ../.control/db-connections-debug-off
			echo "Switched off debug logging for database connections."
		else
		 	touch ../.control/db-connections-debug-on
			echo "Switched on debug logging for database connections."
		fi
	;;
	log-db-connections)
		mkdir -p ../.control
		if [ "$2" != "" ]; then
		 	touch ../.control/db-connections-print-active-$2
		else
		 	touch ../.control/db-connections-print-active
		fi
		echo "Active database connections will be logged."
	;;
	record-stacktrace-db-connections)
		mkdir -p ../.control
		if [ "$2" == "off" ]; then
		 	touch ../.control/db-connections-stacktrace-off
			echo "Switched off stacktrace recording for database connections."
		else
		 	touch ../.control/db-connections-stacktrace-on
			echo "Switched on stacktrace recording for database connections."
		fi
	;;
	log-db-connections-separate-log-file)
		mkdir -p ../.control
  	if [ "$2" == "off" ]; then
	  	touch ../.control/db-connections-separate-log-file-off
  		echo "Switched off logging messages to logs/openbis_db_connections.txt"
  	else
	  	touch ../.control/db-connections-separate-log-file-on
  		echo "Switched on logging messages to logs/openbis_db_connections.txt"
  	fi
	;;
	*)
	echo "Usage:"
	echo "$0 log-service-calls on|off - switch on / off logging of start and end of service calls to separate file"
	echo "$0 log-long-running-invocations on|off - switch on / off logging of long running server method invocations"
	echo "$0 debug-db-connections on|off - switch on / off database connection debug logging"
	echo "$0 log-db-connections [min_connection_age_in_millis] - log the currently active database connections"
	echo "$0 record-stacktrace-db-connections on|off - switch on / off database connection stacktrace recording"
	echo "$0 log-db-connections-separate-log-file on|off - log messages related to database connections to logs/openbis_db_connections.txt"
	exit 200
	;;
esac
exit 0
