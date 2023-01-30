#! /bin/bash

# Watchdog script for CISD openBIS Application Server on Unix / Linux systems
# ---------------------------------------------------------------------------
# (call this in a cron job every minute or so)

# bin directory of the openBIS Application Server
OBIS_AS_BIN_DIR=`dirname "$0"`

# Diectory of the openBIS Application Server
OBIS_AS_DIR=${OBIS_AS_BIN_DIR}/..

# The watchdog log file
WATCHDOG_FILE=${OBIS_AS_DIR}/watchdog.log

# Email of admin for notification of restart
ADMIN_EMAIL="root@localhost"

# Number of retries when remote API check fails
RETRIES=5

# Number of seconds to sleep before retrying when remote API check fails
SLEEP_SECS_ON_FAIL=60

# Set this to 1 to not restart the Application Server but only send an email
DRY_RUN_MODE=0


# Restart the openBIS AS
restart()
{
  if [ ${DRY_RUN_MODE} -ne 0 ]; then
    echo "openBIS Application Server needs restart (dry-run mode)"
    echo "Watchdog: openBIS AS dead" | mail -s "Watchdog: openBIS AS should be restarted" ${ADMIN_EMAIL}
    return
  fi
  ${OBIS_AS_BIN_DIR}/shutdown.sh
  ${OBIS_AS_BIN_DIR}/startup.sh
  date +"%F %H:%M:%S" >> ${WATCHDOG_FILE}
  echo "Watchdog: openBIS AS restarted" >>  ${WATCHDOG_FILE}
  echo "Watchdog: openBIS AS restarted" | mail -s "Watchdog: openBIS AS restarted" ${ADMIN_EMAIL}
}

# Request a thread dump of the openBIS AS
threadDump()
{
   PID=`cat ${OBIS_AS_DIR}/openbis.pid 2> /dev/null`
   test -n "$PID" && kill -3 "$PID"
}

${OBIS_AS_BIN_DIR}/status.sh -q
ok=$?

if [ $ok -ne 0 ]; then
  restart
else
  n=0
  ${OBIS_AS_BIN_DIR}/check.sh -q
  ok=$?
  while [ $ok -ne 0 -a $n -lt ${RETRIES} ]; do
    sleep ${SLEEP_SECS_ON_FAIL}
    ${OBIS_AS_BIN_DIR}/check.sh -q
    ok=$?
    n=$(($n+1))
  done
  if [ $ok -ne 0 ]; then
    threadDump
    restart
  fi
fi
