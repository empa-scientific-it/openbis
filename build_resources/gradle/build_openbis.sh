#!/bin/bash

PRODUCT=$1
BRANCH=$2
VERSION=$3

PROJECTS="\
gradle \
authentication \
common \
datamover \
datastore_server \
dbmigration \
deep_sequencing_unit \
image_readers \
installation \
integration-tests \
js-test \
openbis \
openbis_all \
openbis_api \
openbis_mobile \
openbis_standard_technologies \
openbis-common \
plasmid \
rtd_cina \
rtd_phosphonetx \
rtd_yeastx \
sanofi \
screening \
ui-test\
"
LATEST_SPRINT=`svn list svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/sprint/|awk '{print $0, length()}'|sort -n --key=2|awk '{print $1'}|tail -1|sed -e "s/.x\///g"`

rm -rf /tmp/gradle-build
mkdir -p /tmp/gradle-build

for project in $PROJECTS; do

	if [[ -z "$BRANCH" ]]
	then
		BRANCH=trunk
	fi

	if [ "$BRANCH" == "trunk" ]
	then
		SVN_PATH="${project}/trunk"
    elif [ "$BRANCH" == "stage" ]
    then
        SVN_PATH="openbis_all/branches/stage/15.xx.x/${project}"
	elif [ "$BRANCH" == "sprint" ]
	then
		if [[ -z "$VERSION" ]]
		then
			VERSION=$LATEST_SPRINT
		fi
		
		if [[ $VERSION == *.* ]]
		then
			TAG=${VERSION}/
			VERSION=`echo $VERSION|cut -d. -f1`			
		else
			TAG=`svn list svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/sprint/${VERSION}.x/|awk '{print $0, length()}'|sort -n --key=2|awk '{print $1'}|tail -1`
		fi
		
		SVN_PATH="openbis_all/tags/sprint/${VERSION}.x/${TAG}${project}"
		
		svn ls svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH --depth=empty 2>/dev/null >/dev/null 
		if [ $? -ne 0 ]
		then
			echo "Unable to read svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH - aborting"
			exit 1
		fi
	else
		echo "Unknown branch $BRANCH (valid options: trunk / sprint)"
		exit 1
	fi

	svn co svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH /tmp/gradle-build/${project}
done

pushd .
cd /tmp/gradle-build/gradle

if [ "$PRODUCT" == "openbis" ]
then
	./gradlew :installation:build -x test
	popd
	mv /tmp/gradle-build/installation/targets/gradle/distributions/*.tar.gz .	
else
	./gradlew :openbis_standard_technologies:clientsAndApis -x test
	popd
	mv /tmp/gradle-build/openbis_standard_technologies/targets/gradle/distributions/openBIS-clients-and-APIs*.zip .	
fi


