#!/bin/bash

pushd $(dirname $0) > /dev/null

OPENBIS_STANDARD_TECHNOLOGIES_DIR=../../..
OPENBIS_DIR=../../../../openbis

CURRENT_DIR=$(pwd)
TEMP_DIR=${CURRENT_DIR}/temp

V3_DIR=$OPENBIS_DIR/source/java/ch/systemsx/cisd/openbis/public/resources/api/v3

# create an empty temporary folder
rm -rvf $TEMP_DIR
mkdir -p $TEMP_DIR

# create a list of all *.js files from chosen V3 folders
cd $V3_DIR
find ./as ./dss ./util -name "*.js" | sed 's/^.\//"/g' | sed 's/.js$/",/g' > $TEMP_DIR/files.js

# create config files from their templates by replacing '__FILES__' with a content of 'files.js'
cd $CURRENT_DIR
cat r.config.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/r.config.js
cat config.bundle.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/config.bundle.js
cat config.bundle.min.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/config.bundle.min.js

# create a JS bundle using NodeJS binary installed by NodeJS gradle plugin
$OPENBIS_STANDARD_TECHNOLOGIES_DIR/node/nodejs/node-*/bin/node r.js -o $TEMP_DIR/r.config.js baseUrl=$V3_DIR optimize=none out=$TEMP_DIR/openbis.bundle.js
$OPENBIS_STANDARD_TECHNOLOGIES_DIR/node/nodejs/node-*/bin/node r.js -o $TEMP_DIR/r.config.js baseUrl=$V3_DIR optimize=uglify out=$TEMP_DIR/openbis.bundle.min.js

# copy relevant files to the V3 public folder
cp $TEMP_DIR/config.bundle.js $TEMP_DIR/config.bundle.min.js $TEMP_DIR/openbis.bundle.js $TEMP_DIR/openbis.bundle.min.js $V3_DIR

popd > /dev/null