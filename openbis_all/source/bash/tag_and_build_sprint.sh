#!/bin/sh
BIN_DIR=`dirname "$0"`

usage()
{
 	echo ""
 	echo "Usage: ./tag_and_build_sprint.sh sprint_number [hot_fix_number]"
 	echo ""
 	echo "Example: ./tag_and_build_sprint.sh 175"
 	echo "Example: ./tag_and_build_sprint.sh 175 1"  
 	exit 1
}

# parameter checks

if [ $# -eq 0 ]
then
	usage
fi

if [[ ! -z $(echo ${1} | sed 's/[0-9]//g') ]]
then
	echo "sprint_number has to be numeric" 
	usage
fi

if [ $# -eq 1 ]
then
	HOT_FIX_NUMBER=0
else
	HOT_FIX_NUMBER=$2
fi

if [[ ! -z $(echo ${HOT_FIX_NUMBER} | sed 's/[0-9]//g') ]]
then
	echo "hot_fix_number has to be numeric" 
	usage
fi

# create sprint branch
if [ ${HOT_FIX_NUMBER} -eq 0 ]
then
	"$BIN_DIR/build/branch.sh" S${1}.x
	if [ $? -ne 0 ];then exit 1; fi	
fi

"$BIN_DIR/build/tag.sh" S${1}.x S${1}.${HOT_FIX_NUMBER}
if [ $? -ne 0 ];then exit 1; fi	

"$BIN_DIR/build/build.sh" S${1}.x S${1}.${HOT_FIX_NUMBER}
