#! /bin/bash
#
# (C) 2008, ETH Zurich, CISD
#
# Script for maintaining the password file.
# 
# Note that the password file and consequenctly this script 
# are only relevant if you set in the service.properties file
#
# authentication-service = file-authentication-service
#    
# -----------------------------------------------------------

source `dirname "$0"`/setup-env

APPLICATION_NAME=openbis

name=`basename "$0"`
if [[ "$name" == *cache* ]]; then
   pwd_prop="-DPASSWORD_CACHE_FILE=etc/passwd_cache"
else
   pwd_prop="-DPASSWORD_FILE=etc/passwd"
fi


#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD="`dirname $bin`"

if [ ! -x "$JVM" ]; then
  echo "No java runtime environment executable found. You need to set the environment variable JAVA_HOME appropriately."
  exit 1
fi


LIB=webapps/$APPLICATION_NAME/WEB-INF/lib

# Build classpath from $LIB content.
CP=`echo $LIB/*.jar | sed 's/ /:/g'`

"$JVM" $pwd_prop -cp $CP ch.systemsx.cisd.authentication.file.PasswordEditorCommand "$@"
