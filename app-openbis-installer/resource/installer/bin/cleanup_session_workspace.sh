#!/bin/bash
#
# Removes stale sessions in session workspace
# 
# Usage: cleanup_session_workspace <path to workspace> <openBIS AS base URL>
#
session_workspace="$1"
url=$2

pushd "$session_workspace" >/dev/null
for f in `ls .`; do
  if [[ -d $f ]]; then
    response=`curl -sk -H "Accept: application/json-rpc" \
                 -d "{\"id\":\"-\", \"method\":\"isSessionActive\", \"params\":[\"$f\"]}" \
                 $url/openbis/rmi-general-information-v1.json`
    if [[ $response == '' ]]; then
      echo "Couldn't access openBIS AS at $url."
    else
      # strip of outermost '{' and '}'
      response=${response#\{}
      response=${response%\}}
      # skip the initial part: "jsonrpc":"2.0","id":"-",
      result=`echo $response | awk '{i=index($0,"\"-\"")+4; print substr($0, i, 1000)}'`
      type=${result%%:*}
      value=${result#*:}
      
      if [[ $type == '"error"' ]]; then
        echo "Session: $f, ERROR: $value"
      elif [[ $value == 'false' ]]; then
        echo -n "Removing workspace of stale session $f ..."
        rm -rf $f
        echo " removed"
      fi
    fi
  fi
done
popd >/dev/null
