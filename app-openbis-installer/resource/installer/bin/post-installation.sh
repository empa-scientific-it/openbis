#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/common-functions.sh

executeScriptHooks "Executing post installation script " "$BASE/post-install/*.sh"

