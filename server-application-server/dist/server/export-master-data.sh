#! /bin/bash
#
# (C) 2011, ETH Zurich, CISD
#
# Exports the master data from a running openBIS system in the form of a script.
#
# Example : 
#
# ./export-master-data.sh -s https://localhost:8443/openbis/openbis
#
# -----------------------------------------------------------

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

"$BASE/register-master-data.sh" -f "$BASE/export-master-data.py" "$@"
