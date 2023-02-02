#!/bin/bash
# Overrides all configuration files with those which can be found in the specified config backup directory.
# Assumes that openbis is installed in the parent directory of the directory where this script is located.

CONF=$1
if [ "$CONF" == "" ]; then
  echo Error: directory from which configuration should be restored has not been specified!
  exit 1
fi

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

echo "Restoring configuration backup from $CONF to $ROOT ..."

# -- AS
if [ -d $ROOT/openBIS-server ]; then
    version=`cat $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/BUILD-openbis.INFO`
    restore $CONF/.keystore $ROOT/openBIS-server/jetty/etc openBIS.keystore
    restore $CONF/service.properties $ROOT/openBIS-server/jetty/etc service.properties
    restore $CONF/log.xml $ROOT/openBIS-server/jetty/etc log.xml
    restore $CONF/openbis.conf $ROOT/openBIS-server/jetty/etc openbis.conf
    restore $CONF/jetty.properties $ROOT/openBIS-server/jetty/etc jetty.properties
    restore $CONF/passwd $ROOT/openBIS-server/jetty/etc passwd
    restore $CONF/web-client.properties $ROOT/openBIS-server/jetty/etc web-client.properties
    restore $CONF/dss-datasource-mapping $ROOT/openBIS-server/jetty/etc dss-datasource-mapping
    restore $CONF/capabilities $ROOT/openBIS-server/jetty/etc capabilities
    restore $CONF/../openBIS-server/jetty/webapps/openbis/custom/welcomePageSimpleGeneric.html $ROOT/openBIS-server/jetty/webapps/openbis/custom welcomePageSimpleGeneric.html
    restore $CONF/config.json $ROOT/big_data_link_server config.json
    restore $CONF/full-text-search-document-version $ROOT/openBIS-server/jetty/etc full-text-search-document-version
    restore $CONF/instance-id $ROOT/openBIS-server/jetty/etc instance-id

    copyConfig $CONF/core-plugins "html/etc$" $ROOT/core-plugins
    copyFolderIfExists $CONF/start.d $ROOT/openBIS-server/jetty/start.d
    copyFolderIfExists $CONF/../openBIS-server/jetty/indices $ROOT/openBIS-server/jetty/indices
fi

# -- DSS
version=`unzip -c $ROOT/datastore_server/lib/server-original-data-store-*.jar  BUILD-datastore_server.INFO|tail -n 1`
restore $CONF/dss-service.properties $ROOT/datastore_server/etc service.properties
restore $CONF/dss-log.xml $ROOT/datastore_server/etc log.xml
restore $CONF/datastore_server.conf $ROOT/datastore_server/etc datastore_server.conf
restore $CONF/.keystore $ROOT/datastore_server/etc openBIS.keystore
copyIfExists $CONF/ext-lib $ROOT/datastore_server
