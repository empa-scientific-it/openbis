#!/bin/bash
# makes a backup of all configuration files to the specified directory

CONF=$1
if [ "$CONF" == "" ]; then
	echo Error: directory where configuration should be backed up has not been specified! 
	exit 1
fi
mkdir -p $CONF

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/common-functions.sh

ROOT=$BASE/../servers

# -- AS
copyFileIfExists $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/etc/service.properties $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/etc/log.xml $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/bin/openbis.conf $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/etc/openbis.conf $CONF/
cp $ROOT/openBIS-server/jetty/etc/jetty.xml $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/bin/jetty.properties $CONF/
copyFileIfExists $ROOT/openBIS-server/jetty/etc/jetty.properties $CONF/
cp $ROOT/openBIS-server/jetty/webapps/openbis/welcomePageSimple.html $CONF/
# not always present
copyIfExists $ROOT/openBIS-server/jetty/etc/openBIS.keystore $CONF/.keystore 
copyIfExists $ROOT/openBIS-server/jetty/etc/passwd $CONF/
copyIfExists $ROOT/openBIS-server/jetty/etc/web-client.properties $CONF/

# -- DSS
cp $ROOT/datastore_server/etc/service.properties $CONF/dss-service.properties
cp $ROOT/datastore_server/etc/log.xml $CONF/dss-log.xml
cp $ROOT/datastore_server/etc/datastore_server.conf $CONF/datastore_server.conf
# not always present
copyIfExists $ROOT/datastore_server/etc/openBIS.keystore $CONF/.keystore 
