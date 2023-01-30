#!/bin/bash
# Installs openbis AS and DSS for the first time. 
# openBIS will be installed in the 'servers' directory on the same level as this script parent directory.

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env
source $BASE/common-functions.sh

ROOT_DIR=$BASE/../servers
BACKUP_DIR=$BASE/../backup

mkdir -p $ROOT_DIR
mkdir -p $BACKUP_DIR

installOpenBisServer $ROOT_DIR
installDataStoreServer $ROOT_DIR

rm -rf "$TMP_EXTRACT"
mv $ROOT_DIR/*.zip $BACKUP_DIR/


