#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

if [ "$ELN_MASTER_DATA" == "false" ]; then
    echo "Disabling ELN master data script";
    mv $BASE/../servers/core-plugins/eln-lims/1/as/initializemasterdataminimum.py $BASE/../servers/core-plugins/eln-lims/1/as/initialize-master-data.py;
    mv $BASE/../servers/core-plugins/eln-lims/2/as/initializemasterdataminimum.py $BASE/../servers/core-plugins/eln-lims/2/as/initialize-master-data.py;
    mv $BASE/../servers/core-plugins/eln-lims/3/as/initializemasterdataminimum.py $BASE/../servers/core-plugins/eln-lims/3/as/initialize-master-data.py;
fi
touch $BASE/../servers/core-plugins/eln-lims/1/as/.eln-master-data-installed
touch $BASE/../servers/core-plugins/eln-lims/2/as/.eln-master-data-installed
touch $BASE/../servers/core-plugins/eln-lims/3/as/.eln-master-data-installed