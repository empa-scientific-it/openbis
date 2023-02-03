#!/bin/bash
# lists all queries which are currently executed by the database engine


echo Remove 'exit' after configuring existing database username ($DB_USERNAME) and openbis database ($OPENBIS_DB)
exit 1

DB_USER_NAME=$(whoami)
OPENBIS_DB=openbis_prod

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/common-functions.sh

exe_psql -U $DB_USER_NAME -d $OPENBIS_DB -c "select datname,procpid,usename,current_query,waiting,xact_start FROM pg_catalog.pg_stat_activity"