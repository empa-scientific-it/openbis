#!/bin/bash
#
# Control script for CISD openBIS Data Store Server on Unix / Linux systems
# -------------------------------------------------------------------------

awkBin()
{
  # We need a awk that accepts variable assignments with '-v'
  case `uname -s` in
    "SunOS")
      echo "nawk"
      return
      ;;
  esac
  # default
  echo "awk"
}

isPIDRunning()
{
  if [ "$1" = "" ]; then
    return 1
  fi
  if [ "$1" = "fake" ]; then # for unit tests
    return 0
  fi
  # This will have a return value of 0 on BSDish systems
  isBSD="`ps aux > /dev/null 2>&1; echo $?`"
  AWK=`awkBin`
  if [ "$isBSD" = "0" ]; then
    if [ "`ps aux | $AWK -v PID=$1 '{if ($2==PID) {print "FOUND"}}'`" = "FOUND" ]; then
      return 0
    else
      return 1
    fi
  else
    if [ "`ps -ef | $AWK -v PID=$1 '{if ($2==PID) {print "FOUND"}}'`" = "FOUND" ]; then
      return 0
    else
      return 1
    fi
  fi
}

checkNotRoot()
{
  if [ $UID -eq 0 ]; then
    echo "openBIS Data Store Server cannot run as user 'root'." > /dev/stderr
    exit 1
  fi
}

rotateLogFiles()
{
  logfile=$1
  max=$2
  if [ -z "$logfile" ]; then
    echo "Error: rotateLogFiles: logfile argument missing"
    return 1
  fi
  if [ -z "$max" ]; then
    echo "Error: rotateLogFiles: max argument missing"
    return 1
  fi
  test -f $logfile.$max && rm $logfile.$max
  n=$max
  while [ $n -gt 1 ]; do
    nnew=$(($n-1))
    test -f $logfile.$nnew && mv $logfile.$nnew $logfile.$n
    n=$nnew
  done
  test -f $logfile && mv $logfile $logfile.1
}

getStatus()
{
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      return 0
    else
      return 1
    fi
  else
    return 2
  fi
}
      
printStatus()
{
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      echo "Data Store Server is running (pid $PID)"
      return 0
    else
      echo "Data Store Server is dead (stale pid $PID)"
      return 1
    fi
  else
    echo "Data Store Server is not running."
    return 2
  fi
}

#
# definitions
#

PIDFILE=${DATASTORE_SERVER_PID:-datastore_server.pid}
CONFFILE=etc/datastore_server.conf
LOGFILE=log/datastore_server_log.txt
STARTUPLOG=log/startup_log.txt
SUCCESS_MSG="Data Store Server ready and waiting for data"
LIB_FOLDER=lib
# contains custom libraries e.g. JDBC drivers for external databases
EXT_LIB_FOLDER=ext-lib
MAX_LOOPS=20

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

#
# source configuration script, if any
#
test -f $CONFFILE && source $CONFFILE
if [ "$JAVA_HOME" != "" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="java"
fi

command=$1

if [ "$command" == "start" ]; then
  # Create lib symlinks before building the classpath
  ./autosymlink.sh
fi

# Unpack native libraries
rm -rf $LIB_FOLDER/native
unzip -q lib/sis-base-*.jar -d $LIB_FOLDER native/*
unzip -q lib/sis-jhdf5-*.jar -d $LIB_FOLDER native/*

# Build classpath from $LIB_FOLDER and $EXT_LIB_FOLDER content. 
# datastore_server.jar and common.jar have to appear before cifex.jar
CP=`echo $LIB_FOLDER/slf4j-log4j12-1.6.2.jar $LIB_FOLDER/datastore_server.jar $LIB_FOLDER/common.jar \
    $LIB_FOLDER/dbmigration*.jar $LIB_FOLDER/*.jar $EXT_LIB_FOLDER/*.jar \
    | sed 's/\(.*\) [^ ]*jython27[^ ]* \(.*\)/\1 \2/g' \
    | sed 's/ /:/g'`

CMD="${JAVA_BIN}"
version=$("${CMD}" -version 2>&1 | awk -F '"' '/version/ {print $2}'| cut -c1-3)
JAVA_VERSION_OPTS=""
if [[ "$version" != "1.8" ]]; then
    JAVA_VERSION_OPTS="--add-exports java.xml/jdk.xml.internal=ALL-UNNAMED"
fi
COMMON_OPTIONS="${JAVA_OPTS} ${JAVA_MEM_OPTS} $JAVA_VERSION_OPTS -Dnative.libpath=$LIB_FOLDER/native -classpath $CP ch.systemsx.cisd.openbis.dss.generic.DataStoreServer"


# ensure that we ignore a possible prefix "--" for any command 
command="${command#--*}"
case "$command" in
  start)
    checkNotRoot
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -eq 0 ]; then
      echo "Cannot start Data Store Server: already running."
      exit 100
    fi

    echo -n "Starting Data Store Server "
    rotateLogFiles $LOGFILE $MAXLOGS
    shift 1
    "${CMD}" $COMMON_OPTIONS "$@" > $STARTUPLOG 2>&1 & echo $! > $PIDFILE
    if [ $? -eq 0 ]; then
      # wait for initial self-test to finish
      n=0
      while [ $n -lt $MAX_LOOPS ]; do
        sleep 1
        if [ ! -f $PIDFILE ]; then
          break
        fi
        if [ -s $STARTUPLOG ]; then
          PID=`cat $PIDFILE 2> /dev/null`
          isPIDRunning $PID
          if [ $? -ne 0 ]; then
            break
          fi
        fi
        grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
        if [ $? -eq 0 ]; then
          break
        fi
        n=$(($n+1))
      done 
      PID=`cat $PIDFILE 2> /dev/null`
      isPIDRunning $PID
      if [ $? -eq 0 ]; then
        grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
        if [ $? -ne 0 ]; then
          echo "(pid $PID - WARNING: SelfTest not yet finished)"
        else
          echo "(pid $PID)"
        fi
      else
        echo "FAILED"
        if [ -s $STARTUPLOG ]; then
          echo "startup log says:"
          cat $STARTUPLOG
        else
          echo "log file says:"
          tail $LOGFILE
        fi
      fi
    else
      echo "FAILED"
    fi
    ;;
  stop)
    echo -n "Stopping Data Store Server "
    if [ -f $PIDFILE ]; then
      PID=`cat $PIDFILE 2> /dev/null`
      isPIDRunning $PID
      if [ $? -eq 0 ]; then
        kill $PID
        n=0
        while [ $n -lt $MAX_LOOPS ]; do
          isPIDRunning $PID
          if [ $? -ne 0 ]; then
            break
          fi
          sleep 1
          n=$(($n+1))
        done
        isPIDRunning $PID
        if [ $? -ne 0 ]; then
          echo "(pid $PID)"
          test -f $PIDFILE && rm $PIDFILE 2> /dev/null
        else
          echo "FAILED"
        fi
      else
        if [ -f $PIDFILE ]; then
          rm $PIDFILE 2> /dev/null
          echo "(was dead - cleaned up pid file)"
        fi
      fi
    else
      echo "(not running - nothing to do)"
    fi
    ;;
  status)
    printStatus
    EXIT_STATUS=$?
    exit $EXIT_STATUS
    ;;
  restart)
    $SCRIPT stop
    $SCRIPT start
    ;;
  help)
    echo "Usage: $0 {start|stop|restart|status|help|version}"
    echo "Advanced:"
    echo "  $0 show-shredder  -  show the list of files / directories that wait to be shreddered"
    echo "  $0 show-updater-queue  -  show the queue of datasets that await updating their archiving status in openBIS AS"
    echo "  $0 show-command-queue  -  show the queue of commands from openBIS AS waiting to be executed"
    echo "  $0 log-db-connections-separate-log-file on / off  -  switch on / off logging messages related to database connections to log/datastore_server_db_connections.txt"
    echo "  $0 log-db-connections  -  log the currently active database connections"
    echo "  $0 log-thread-dump  -  log the current thread dump to log/startup_log.txt"
    echo "  $0 debug-db-connections on / off -  switch on / off database connection debug logging"
    echo "  $0 record-stacktrace-db-connections on / off -  switch on / off database connection stacktrace recording"
    echo "  $0 log-service-calls on / off -  switch on / off logging of start and end of service calls to separate file"
    echo "  $0 log-long-running-invocations on / off -  switch on / off logging of long running invocations"
    echo "  $0 verify-archives  -  verify integrity of dataset archives created by ZipArchiver"
    
    ;;
  version)
    "${CMD}" $COMMON_OPTIONS --version
    ;;
  show-shredder)
    "${CMD}" $COMMON_OPTIONS --show-shredder
    ;;
  show-updater-queue)
    "${CMD}" $COMMON_OPTIONS --show-updater-queue
    ;;
  show-command-queue)
    "${CMD}" $COMMON_OPTIONS --show-command-queue
    ;;
  verify-archives)
    shift
    java -cp "lib/*" ch.systemsx.cisd.openbis.dss.archiveverifier.cli.Main etc/service.properties $*
    exit $?
    ;;    
  log-thread-dump)
    if [ -f $PIDFILE ]; then
      PID=`cat $PIDFILE 2> /dev/null`
      isPIDRunning $PID
      if [ $? -eq 0 ]; then
        kill -3 $PID
        echo "Thread dump logged to log/startup_log.txt"
      else
        echo "Error: Data Store Server not running."
        exit 100
      fi
    else
      echo "Error: Data Store Server not running."
      exit 100
    fi
    ;;
  log-db-connections-separate-log-file)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" == "off" ]; then
      touch .control/db-connections-separate-log-file-off
      echo "Switched off logging messages to log/datastore_server_db_connections.txt"
    else
      touch .control/db-connections-separate-log-file-on
      echo "Switched on logging messages to log/datastore_server_db_connections.txt"
    fi
    ;;    
  log-db-connections)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" != "" ]; then
      touch .control/db-connections-print-active-$2
    else
      touch .control/db-connections-print-active
    fi
    echo "Active database connections will be logged"
    ;;
  debug-db-connections)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" == "off" ]; then
      touch .control/db-connections-debug-off
      echo "Switched off debug logging for database connections."
    else
      touch .control/db-connections-debug-on
      echo "Switched on debug logging for database connections."
    fi
    ;;
  record-stacktrace-db-connections)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" == "off" ]; then
      touch .control/db-connections-stacktrace-off
      echo "Switched off stacktrace recording for database connections."
    else
      touch .control/db-connections-stacktrace-on
      echo "Switched on stacktrace recording for database connections."
    fi
    ;;
  log-service-calls)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" == "off" ]; then
      touch .control/log-service-call-start-off
      echo "Switched off logging of service calls."
    else
      touch .control/log-service-call-start-on
      echo "Switched on logging of service calls."
    fi
    ;;    
  log-long-running-invocations)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -ne 0 ]; then
      echo "Error: Data Store Server not running."
      exit 100
    fi
    mkdir -p .control
    if [ "$2" == "off" ]; then
      touch .control/long-running-thread-logging-off
      echo "Switched off logging of long running invocations."
    else
      touch .control/long-running-thread-logging-on
      echo "Switched on logging of long running invocations."
    fi
    ;; 
  *)
    echo "Usage: $0 {start|stop|restart|status|help|version}"
    exit 200
    ;;
esac
exit 0
