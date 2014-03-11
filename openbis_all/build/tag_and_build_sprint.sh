#!/bin/sh

usage()
{
 	echo ""
 	echo "Usage: ./tag_and_build_sprint.sh sprint_number [hot_fix_number]"
 	echo ""
 	echo "Example: ./tag_and_build_sprint.sh 175"
 	echo "Example: ./tag_and_build_sprint.sh 175 1"  
 	exit 1
}

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

if [ ${HOT_FIX_NUMBER} -eq 0 ]
then
	./branch.sh sprint/S${1}.x
fi

./tag.sh sprint/S${1}.x S${1}.${HOT_FIX_NUMBER}
./build.sh sprint S${1}.x/S${1}.${HOT_FIX_NUMBER}