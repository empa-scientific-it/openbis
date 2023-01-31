#!/bin/bash
pushd . > /dev/null
cd `dirname $0`
java -cp "lib/datastore_server.jar:lib/*" ch.systemsx.cisd.openbis.dss.generic.shared.utils.AutoSymlink 2>&1 |grep -v log4j
popd > /dev/null

