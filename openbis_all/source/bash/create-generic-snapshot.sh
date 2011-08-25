#! /bin/bash
#
# Creates a snapshot of the sprint server assuming standard paths to servers, snapshot repository
# and configuration file.
#
# Usage: create-generic-snapshot.sh
#
# Important Notes: see Important Notes of create-snapshot.sh 
#
# Dependencies:
# - create-snapshot.sh
#

BIN_DIR=`dirname "$0"`
REPOSITORY=~/snapshots

$BIN_DIR/create-snapshot.sh ~/sprint $REPOSITORY $REPOSITORY/snapshot.config