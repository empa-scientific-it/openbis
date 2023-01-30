#!/bin/bash
# lists all queries which are currently executed by the database engine

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

psql -U $DB_USER_NAME -d $OPENBIS_DB -c "select datname,pid,usename,query,state FROM pg_catalog.pg_stat_activity"