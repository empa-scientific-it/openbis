#! /bin/bash

# Version printer script for CISD openBIS Application Server on Unix / Linux systems
# ----------------------------------------------------------------------------------

source `dirname "$0"`/setup-env

AWK=`awkBin`

printVersion()
{
  $AWK -F: '{printf "%s (r%s)%s\n", $1, $2, ($3!="clean") ? "*" : ""}' webapps/openbis/WEB-INF/classes/BUILD*.INFO
}

printVersion "$1"
