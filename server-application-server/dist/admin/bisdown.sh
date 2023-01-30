#!/bin/bash
# Shuts down openBIS server

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

echo Shutting openBIS down...
$BASE/../servers/openBIS-server/jetty/bin/shutdown.sh
