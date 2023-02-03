#!/bin/bash

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

java -Djava.util.logging.config.file=$BASE/jul.config -cp $BASE/openBIS-installer.jar ch.systemsx.cisd.openbis.installer.izpack.Extractor "$@"