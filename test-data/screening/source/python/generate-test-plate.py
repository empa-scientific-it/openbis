#!/usr/bin/env python

"""
Generate a plate.

Usage: generate-test-plate.py <plate code> [<color 1>:<inset 1> <color 2>:<inset 2> ...]

It creates a folder named <plate code> (default PLATONIC) with colorful images. It can be dropped
into a dropbox with Python script data-set-handler-plate.py.

"""

import imagegen
import os
import shutil
import sys

def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

plateName = "PLATONIC"
if len(sys.argv) > 1:
  plateName = sys.argv[1]
recreateDir(plateName)
config = imagegen.PlateGeneratorConfig()

# Alternative Configurations
#config.time_points = [ 5, 10, 15 ]
#config.depth_points = [ 3, 6, 9 ]
if len(sys.argv) > 2:
    config.is_split = True
    color_configs = []
    for color_and_inset in sys.argv[2:]:
        splitted = color_and_inset.split(':')
        color_configs = color_configs + [ imagegen.ColorConfig(splitted[0], int(splitted[1])) ]
    config.color_configs = color_configs

generator = imagegen.PlateGenerator(config)
generator.generate_raw_images(plateName	)
