#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

function remove64BitInstructionFromConfigFile() 
{
   confFileName=$1
   sed -i.backup "s/-d64//g" $confFileName
}

function remove64BitInstructionFromConfig() 
{
   remove64BitInstructionFromConfigFile $BASE/../../servers/openBIS-server/jetty/bin/openbis.conf
   remove64BitInstructionFromConfigFile $BASE/../../servers/datastore_server/etc/datastore_server.conf
}

java -d64 -version &> /dev/null || remove64BitInstructionFromConfig