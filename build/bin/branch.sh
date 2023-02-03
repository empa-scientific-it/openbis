#!/bin/bash

# exit if no parameter given
if [ $# -ne 1 ]
then
  echo "Usage: ./branch.sh [branch]"
  echo ""
  echo "Example: ./branch.sh 13.04.x"
  exit 1
fi

# exit if branch already exists
branch_heads=`git ls-remote --heads git@sissource.ethz.ch:sispub/openbis.git $1`
if [ -n "$branch_heads" ]; then
  echo "Branch already exists!"
  exit 1
fi

# cd to repository root directory
cd "$(dirname "$0")/../../../.."

# create branch in git from master
git checkout -b $1
git push -u origin $1

# fix dependency versions
GRADLE_PROJECTS="\
lib-authentication \
lib-commonbase \
lib-common \
server-original-data-store \
dbmigration \
installation \
test-api-openbis-javascript \
server-application-server \
lib-openbis-common \
openbis_standard_technologies \
openbis_api \
server-screening \
test-ui-core\
"

for project in $GRADLE_PROJECTS; do
	cd $project;
	./gradlew dependencyReport;
	cat targets/gradle/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands;
	
	for file in build.gradle javaproject.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle; do
		if [ -s $file ]; then
			sed -f sed_commands $file > $file.tmp;
			mv $file.tmp $file;
			# add to git
			git add $file
		fi;
	done
	
	rm sed_commands
	cd ..;
done

# commit dependency versions
git commit -m "fixed dependencies of $1";
git push
