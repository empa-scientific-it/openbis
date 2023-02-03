#!/bin/bash
#
# Reports failed login to a given email address, helps in identifying dictionary
# attacks on the openBIS server
#
# Author: Manuel Kohler

if [ -f /etc/sysconfig/openbis ]; then
   . /etc/sysconfig/openbis
fi

EMAIL=manuel.kohler@bsse.ethz.ch
LOG_FOLDER=/servers/openBIS-server/jetty/logs/
LOG_FILE=openbis_auth_log.txt
FULL_PATH=${BIS_HOME}${LOG_FOLDER}${LOG_FILE}
LIMIT=5
HOSTNAME=`hostname`
FAILED=`grep FAILED ${FULL_PATH} | wc -l`

if [ $FAILED -gt "$LIMIT" ]; then
    /bin/mailx -s "Failed Logins on ${HOSTNAME} exceed limit of ${LIMIT}" \
                  ${EMAIL} < ${FULL_PATH}
fi
