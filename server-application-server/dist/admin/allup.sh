#!/bin/bash
# Starts up openBIS and DSS

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

echo Starting openBIS.
$BASE/bisup.sh
echo Waiting 20 sec for openBIS to start up....
sleep 20
echo Starting Datastore Server.
$BASE/dssup.sh