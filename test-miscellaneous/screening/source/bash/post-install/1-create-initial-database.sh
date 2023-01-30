#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/../env


function createPostgresUserIfNeeded() {
  postgresUser=$1
  
  psql -U $postgresUser -d template1 -c '\dt' &> /dev/null || psql -U $DB_USER_NAME -c "create role $postgresUser with LOGIN"
}


# screening-specific
echo Creating empty screening database...
USER=`whoami`

createPostgresUserIfNeeded $USER
createPostgresUserIfNeeded "OPENBIS_READONLY"

$PSQL -U $DB_USER_NAME -c "create database $OPENBIS_DB with owner $USER template = template0 encoding = 'UNICODE'"
$PSQL -U $USER -d $OPENBIS_DB -f $BASE/empty-screening-database.sql > /dev/null