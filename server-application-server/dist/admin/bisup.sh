#!/bin/bash
# Starts up openBIS server

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
$BASE/../servers/openBIS-server/jetty/bin/startup.sh
