#! /bin/bash

ME="$0"
MYDIR=${ME%/*}
cd $MYDIR
ant -lib ../../build_resources/lib/ecj.jar "$@"
