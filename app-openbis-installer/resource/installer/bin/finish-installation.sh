#!/bin/bash
#
# 1) Mark all scripts as executable 
# 2) Clean up all temporary folders after installation 
#
set -o errexit

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

INSTALL_TMPEXTRACT=$1
DATA_TMPEXTRACT=$2
DSS_ROOT_DIR=$3

echo $4 > $BASE/postgres_bin_path.txt

echo Finish installation...
source $BASE/chmodx-all-scripts.sh


if [ -d "$INSTALL_TMPEXTRACT" ]; then
  rm -rf $INSTALL_TMPEXTRACT
fi

if [ -d "$DATA_TMPEXTRACT" ]; then
   mkdir -p "$DSS_ROOT_DIR"
   for f in `ls "$DATA_TMPEXTRACT/data/"`; do
      cp -Rf "$DATA_TMPEXTRACT/data/$f" "$DSS_ROOT_DIR"
   done
   rm -rf "$DATA_TMPEXTRACT"
fi
