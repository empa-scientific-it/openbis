#!/bin/bash

pushd $(dirname $0) > /dev/null

CURRENT_DIR=$(pwd)
TEMP_DIR=${CURRENT_DIR}/temp
V3_DIR=${CURRENT_DIR}/../src/v3
NODE_DIR=$(echo ${CURRENT_DIR}/../node/nodejs/node-*/bin)

export PATH=$PATH:${NODE_DIR}

# create an empty temporary folder
rm -rvf $TEMP_DIR
mkdir -p $TEMP_DIR

# create a list of all *.js files from chosen V3 folders
cd $V3_DIR
find ./as ./dss ./imaging ./util -name "*.js" | sed 's/^.\//"/g' | sed 's/.js$/",/g' > $TEMP_DIR/files.js

# create config files from their templates by replacing '__FILES__' with a content of 'files.js'
cd $CURRENT_DIR
cat r.config.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/r.config.js
cat config.bundle.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/config.bundle.js
cat config.bundle.min.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/config.bundle.min.js
cat webpack.config.v3api.generate.entry.template.js | sed -e '\|__FILES__|{' -e "r $TEMP_DIR/files.js" -e 'd' -e '}' > $TEMP_DIR/webpack.config.v3api.generate.entry.js

# install npm dependencies needed for bundles creation
npm install

# create AMD (RequireJS) bundle
node r.js -o $TEMP_DIR/r.config.js baseUrl=$V3_DIR optimize=none out=$TEMP_DIR/openbis.bundle.js
npm run uglifyjs -- $TEMP_DIR/openbis.bundle.js -o $TEMP_DIR/openbis.bundle.min.js

# create VAR and ESM bundles
node $TEMP_DIR/webpack.config.v3api.generate.entry.js > $TEMP_DIR/webpack.config.v3api.entry.js
npm run v3api.esm
npm run v3api.var

# copy relevant files to the V3 public folder
cp $TEMP_DIR/config.bundle.js $TEMP_DIR/config.bundle.min.js $TEMP_DIR/openbis.bundle.js $TEMP_DIR/openbis.bundle.min.js $V3_DIR
cp $TEMP_DIR/openbis.esm.* $TEMP_DIR/openbis.var.* $V3_DIR

popd > /dev/null