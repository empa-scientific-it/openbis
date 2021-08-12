#!/bin/bash

pushd $(dirname $0) > /dev/null

# create temporary output folder
rm -rvf output
mkdir output

# create a list of all *.js files from 'as', 'dss' and 'util' folders
find ../as ../dss ../util -name "*.js" | sed 's/^..\//"/g' | sed 's/.js$/",/g' > output/files.js

# create config files from their templates by replacing '__FILES__' with a content of 'files.js'
cat r.config.template.js | sed -e '\|__FILES__|{' -e 'r output/files.js' -e 'd' -e '}' > output/r.config.js
cat config.bundle.template.js | sed -e '\|__FILES__|{' -e 'r output/files.js' -e 'd' -e '}' > output/config.bundle.js

# create a JS bundle with all *.js files
node r.js -o output/r.config.js

# copy relevant output files to resource folder
cp output/config.bundle.js output/openbis.bundle.js ../

# remove temporary output folder
rm -rvf output

popd > /dev/null