#!/usr/bin/env python


"""
Generate plate overlays.

Usage: generate-test-plate-overlays.py [<plate> [<overlay name> <analysis procedure>]]

It creates a folder named <plate>.<overlay name> (default PLATONIC.OVERLAY) with overlay images. It can be dropped
into a dropbox with Python script data-set-handler-plate-overlays.py.

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
text = "OVERLAY"
if len(sys.argv) > 2:
  text = sys.argv[2]
plateName = plateName + "." + text
if len(sys.argv) > 3:
  plateName = plateName + "." + sys.argv[3]
  
recreateDir(plateName)
config = imagegen.PlateGeneratorConfig()

# Alternative Configurations
#config.time_points = [ 5, 10, 15 ]
#config.depth_points = [ 3, 6, 9 ]
#config.is_split = True

generator = imagegen.PlateGenerator(config)
generator.generate_overlay_images(plateName, text, 10, 100)