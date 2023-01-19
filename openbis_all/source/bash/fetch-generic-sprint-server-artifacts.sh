#! /bin/bash
# 
# Fetches from CI server all artifacts needed for a generic openBIS installation.
#
# Dependencies:
# - fetch-ci-artifacts.sh
#
set -o nounset
set -o errexit

BIN_DIR=`dirname "$0"`
SCRIPT="$BIN_DIR/fetch-ci-artifacts.sh"

"$SCRIPT" openbis
"$SCRIPT" -p server datastore_server
