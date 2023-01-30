#!/usr/bin/env python

"""
Generate an image series.

Usage: generate-test-series.py <sample code> <time points> <depth points>

"""

import imagegen
import os
import shutil
import sys

def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

sampleCode = sys.argv[1]
timePoints = int(sys.argv[2])
depthPoints = int(sys.argv[3])
recreateDir(sampleCode)
config = imagegen.GenericImageGeneratorConfig()
config.time_points = range(0, timePoints)
config.depth_points = range(0, depthPoints * 3, 3)

generator = imagegen.GenericImageGenerator(config)
generator.generate_raw_images(sampleCode)
