#!/bin/bash
# Shuts down openBIS and DSS

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

$BASE/dssdown.sh
$BASE/bisdown.sh
