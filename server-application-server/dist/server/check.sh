#! /bin/bash

# API access check script for CISD openBIS Application Server on Unix / Linux systems
# -----------------------------------------------------------------------------------

source `dirname "$0"`/setup-env

AWK=`awkBin`

printCheckResult()
{
  if [ "$1" == "-q" ]; then
    QUIET=1
  fi
  VAL=`curl --insecure --connect-timeout 10 https://localhost:8443/openbis/openbis/rmi-general-information-v1.json -H "Content-Type: application/json" -H "Accept: application/json" --data '{"id": "1", "jsonrpc": "2.0", "method": "getMinorVersion"}' 2> /dev/null | $AWK '/{"jsonrpc":"2.0","id":"1","result":[0-9]+}/ { print "OK" }'`
  EVAL=$?
  if [ $EVAL -eq 0 -a "$VAL" = "OK" ]; then
    test -z "$QUIET" && echo "Check successful"
    return 0
  else
    test -z "$QUIET" && echo "Check failed"
    return 1
  fi
}

printCheckResult "$1"
