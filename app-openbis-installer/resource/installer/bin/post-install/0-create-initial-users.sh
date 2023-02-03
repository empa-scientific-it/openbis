#!/bin/bash
#
# Creates an openBIS user by executing the 'passwd.sh' command-line tool.
# These users will only be available when 'file-authentication-service' is configured in openBIS.
 
# $1 - username
# $2 - password. 
createUser()
{
  username=$1
  password=$2
  
  pushd . > /dev/null
  cd $BASE/../../servers/openBIS-server/jetty
  
  echo "Creating user $username ..."
  ./bin/passwd.sh add -p "$password" $username
  
  popd  > /dev/null
}

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

if [ ! -d "$BASE/../../servers/openBIS-server" ]; then
    exit
fi

#
# create 'admin' user
#
if [ -z "$ADMIN_PASSWORD" ]; then
    read -s -p "Enter password for 'admin' : " ADMIN_PASSWORD
fi
createUser "admin" "$ADMIN_PASSWORD"

#
# create 'etlserver' user
#
if [ -z "$ETLSERVER_PASSWORD" ]; then
    read -s -p "Enter password for 'etlserver' : " $ETLSERVER_PASSWORD
fi
createUser "etlserver" "$ETLSERVER_PASSWORD"

# remove existing password configuration

DSS_SERVICE_PROPERTIES=$BASE/../../servers/datastore_server/etc/service.properties
cat $DSS_SERVICE_PROPERTIES | awk '{ sub("^password[ tab]*=.*$", "password=" ENVIRON["ETLSERVER_PASSWORD"]); print }' > $DSS_SERVICE_PROPERTIES.TMP
mv $DSS_SERVICE_PROPERTIES.TMP $DSS_SERVICE_PROPERTIES
