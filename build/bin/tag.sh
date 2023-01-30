#!/bin/bash


if [ $# -ne 2 ]
then
  echo "Usage: ./tag.sh [branch] [tag]"
  echo ""
  echo "Example: ./tag.sh 13.04.x 13.04.1"
  exit 1
fi
branch=$1
tag=$2

# cd to repository root directory
cd "$(dirname "$0")/../../../.."

# switch to branch - exit if it does not exist
git checkout $branch
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi
git pull

# exit if tag already exists
git checkout $tag
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi


# set version for ELN
echo $tag > openbis_standard_technologies/dist/core-plugins/eln-lims/1/as/webapps/eln-lims/html/version.txt
git add openbis_standard_technologies/dist/core-plugins/eln-lims/1/as/webapps/eln-lims/html/version.txt
git commit -m "Added ELN build info to tag $tag"
git push

# create tag
git tag $tag
git push origin $tag
