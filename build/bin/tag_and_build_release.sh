#!/bin/bash
# Tags, builds the software components, uploads them to sprint server 
# and places a copy of them on the sprint server ready for installation. 
#
# This script assumes that you have a SSH access on 'sprint-openbis.ethz.ch' 
# and 'cisd-vesuvio.ethz.ch'. This is typically configured in the SSH config file.

if [ $# -lt 1 ]; then
    echo "Usage: $0 <year.month> [version]"
    exit 1
fi

BIN_DIR=`dirname "$0"`
TODAY=`date "+%Y-%m-%d"`
VER=$1
SUBVER=0
if [ $2 ]; then
	SUBVER=$2
fi
FULL_VER=$VER.$SUBVER
SPRINT_SERVER=sprint-openbis.ethz.ch
CISD_SERVER=cisd-vesuvio.ethz.ch
SPRINT_INSTALL_SCRIPT=sprint_install.sh
# this folder is publicly accessible at http://svnsis.ethz.ch/doc/javadoc/
JAVADOC_FOLDER=~/fileserver/doc/openbis

# Unset this to do a dry-run (like rsync -n) and set it to actually execute the commands
# unset EXECUTE_COMMANDS
EXECUTE_COMMANDS=1

function state_start {
	echo "----------------------------------------------------------------------"
	echo -n "| "
	echo $1
	echo ""
}

function state_end {
	echo "\ -----------------------------------"
	echo ""	
}

function setup {
  state_start Setup
  
  if [ $SUBVER -eq 0 ]; then
    echo "$BIN_DIR/branch.sh $VER.x"
    if [ $EXECUTE_COMMANDS ]; then
      ./$BIN_DIR/branch.sh $VER.x
    fi
  fi
  
  state_end
}

function tag {
  state_start "Tagging $VER.x to $FULL_VER..."
  
  echo "$BIN_DIR/tag.sh $VER.x $FULL_VER"
  if [ $EXECUTE_COMMANDS ]; then
    "$BIN_DIR/tag.sh" $VER.x $FULL_VER
  fi
  
  state_end
}

function build {
  state_start "Building openBIS $FULL_VER"
  
  echo "$BIN_DIR/build.sh $VER.x $FULL_VER"
  if [ $EXECUTE_COMMANDS ]; then
    "$BIN_DIR/build.sh" $VER.x $FULL_VER
  fi
  
  state_end
}


if [ $EXECUTE_COMMANDS ]; then
	echo -n
else
	state_start "RUNNING DRY RUN"
fi

setup
tag
build

state_start Done!
