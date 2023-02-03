#!/bin/bash
# Shows daily usage statistics of openBIS users, excluding some users.

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

# change it if you e.g. do not want to see when admin users access openBIS
IGNORED_USERS="tpylak|brinn|kohleman|izabelaa|buczekp|felmer|hclaus|cramakri|baucha|ryanj|etlserver"

cat $BASE/../servers/openBIS-server/jetty/logs/*usage_log.txt* | egrep -v "$IGNORED_USERS" | cut -d" " -f1,10,14 | sort | uniq -c