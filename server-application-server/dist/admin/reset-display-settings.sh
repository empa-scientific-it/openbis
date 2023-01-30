#!/bin/bash
# Use with care!
# Overrides all user display settings with the settings of the template user.

echo Remove 'exit' after configuring existing user name, currently it is $TEMPLATE_USER
exit 1
TEMPLATE_USER=openbis-user-name

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

psql -U $DB_USER_NAME -d $OPENBIS_DB -c "update persons set display_settings = (select display_settings from persons where user_id = '$TEMPLATE_USER') where user_id != '$TEMPLATE_USER'"
