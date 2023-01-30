#!/bin/bash
# Shows openBIS server log

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
less $BASE/../servers/openBIS-server/jetty/logs/openbis_log.txt
