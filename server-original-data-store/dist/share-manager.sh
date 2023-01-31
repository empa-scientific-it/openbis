#!/bin/bash
#
# Script for managing data store shares
# -------------------------------------------------------------------------
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
cd "$BASE"
CONFFILE=etc/datastore_server.conf
LIB_FOLDER=lib

test -f $CONFFILE && source $CONFFILE
if [ "$JAVA_HOME" != "" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
else
    JAVA_BIN="java"
fi

CP=`echo $LIB_FOLDER/datastore_server.jar $LIB_FOLDER/*.jar | sed 's/ /:/g'`
"$JAVA_BIN" $JAVA_OPTS -classpath $CP ch.systemsx.cisd.openbis.dss.client.admin.ShareManagerApplication "$@"