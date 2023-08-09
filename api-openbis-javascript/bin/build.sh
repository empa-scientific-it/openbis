#!/bin/bash

pushd $(dirname $0) > /dev/null

NODE_PATH=../node/nodejs/node-*/bin/node
NPM_PATH=../node/nodejs/node-*/bin/npm

CURRENT_DIR=$(pwd)
TEMP_DIR=${CURRENT_DIR}/temp

V3_DIR=../src/v3

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
cat webpack.config.v3api.generate.entry.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/webpack.config.v3api.generate.entry.js

# create AMD (RequireJS) bundle
$NODE_PATH r.js -o $TEMP_DIR/r.config.js baseUrl=$V3_DIR optimize=none out=$TEMP_DIR/openbis.bundle.js
$NODE_PATH r.js -o $TEMP_DIR/r.config.js baseUrl=$V3_DIR optimize=uglify out=$TEMP_DIR/openbis.bundle.min.js

# create UMD and ESM bundles
$NODE_PATH $TEMP_DIR/webpack.config.v3api.generate.entry.js > $TEMP_DIR/webpack.config.v3api.entry.js
$NPM_PATH install
$NPM_PATH run v3api.esm
$NPM_PATH run v3api.umd

# copy relevant files to the V3 public folder
cp $TEMP_DIR/config.bundle.js $TEMP_DIR/config.bundle.min.js $TEMP_DIR/openbis.bundle.js $TEMP_DIR/openbis.bundle.min.js $V3_DIR
cp $TEMP_DIR/openbis.esm.* $TEMP_DIR/openbis.umd.* $V3_DIR

popd > /dev/null