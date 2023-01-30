#!/usr/bin/env bash

# Generate the data for the experiment specified in exp-1-library.xls
PYTHON_PATH=`dirname $0 | sed -e 's/bash/python/g'`

$PYTHON_PATH/generate-test-plate.py PLATE-1
$PYTHON_PATH/generate-test-plate-overlays.py PLATE-1 OVERLAY-1
$PYTHON_PATH/generate-test-plate-overlays.py PLATE-1 OVERLAY-2
$PYTHON_PATH/generate-test-plate-analysis.py PLATE-1.analysis1 TPU STATE:STABLE:UNSTABLE
$PYTHON_PATH/generate-test-plate-analysis.py PLATE-1.analysis2 TPU STATE:STABLE:UNSTABLE VALUES_WITH_NAN:-2:-1.5:-1:0:1:1.5:2:NaN

$PYTHON_PATH/generate-test-plate.py -b 16 PLATE-16-BIT CY5:20 GFP:70 DAPI:160

