#!/bin/bash
#
# Checks that a specified database exists. Prints TRUE or FALSE onto the console.
#

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/common-functions.sh

DATABASE="$1"
OWNER="$2"
HOST=$3
PORT=$4

databaseExist $HOST $PORT "$DATABASE" "$OWNER"