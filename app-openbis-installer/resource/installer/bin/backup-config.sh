#!/bin/bash
# makes a backup of all configuration files to the specified directory

CONF=$1
if [ "$CONF" == "" ]; then
  echo Error: directory where configuration should be backed up has not been specified!
  exit 1
fi
mkdir -p $CONF

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
ROOT=$BASE/../servers

# -- AS
if [ -d $ROOT/openBIS-server ]; then
    copyFileIfExists $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/service.properties $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/capabilities $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/dss-datasource-mapping $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/log.xml $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/bin/openbis.conf $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/openbis.conf $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/bin/jetty.properties $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/jetty.properties $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/full-text-search-document-version $CONF/
    copyFileIfExists $ROOT/openBIS-server/jetty/etc/instance-id $CONF/
    copyFileIfExists $ROOT/big_data_link_server/config.json $CONF/
    cp $ROOT/openBIS-server/jetty/webapps/openbis/custom/welcomePageSimpleGeneric.html $CONF/
    # not always present
    copyIfExists $ROOT/openBIS-server/jetty/etc/openBIS.keystore $CONF/.keystore
    copyIfExists $ROOT/openBIS-server/jetty/etc/passwd $CONF/
    copyIfExists $ROOT/openBIS-server/jetty/etc/web-client.properties $CONF/
    copyConfig $ROOT/core-plugins "html/etc$" $CONF/core-plugins
    cp -R $ROOT/openBIS-server/jetty/start.d $CONF/start.d
fi

# -- DSS
cp $ROOT/datastore_server/etc/service.properties $CONF/dss-service.properties
cp $ROOT/datastore_server/etc/log.xml $CONF/dss-log.xml
cp $ROOT/datastore_server/etc/datastore_server.conf $CONF/datastore_server.conf
# not always present
copyIfExists $ROOT/datastore_server/etc/openBIS.keystore $CONF/.keystore
copyIfExists $ROOT/datastore_server/ext-lib $CONF
