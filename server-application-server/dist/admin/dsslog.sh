#!/bin/bash
# Shows DSS log

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
less $BASE/../servers/datastore_server/log/datastore_server_log.txt
