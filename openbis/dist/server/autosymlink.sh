#!/bin/bash
pushd . > /dev/null
cd `dirname $0`/..
libfolder=$1
java -cp "$1/*" ch.ethz.sis.openbis.generic.shared.utils.AutoSymlink 2>&1 |grep -v log4j
popd > /dev/null

