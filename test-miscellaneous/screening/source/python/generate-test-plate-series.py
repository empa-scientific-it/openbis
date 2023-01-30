#!/usr/bin/env python

"""
Generate a plate.

Usage: generate-test-plate.py <plate code> [<channel 1>:<inset 1> <channel 2>:<inset 2> ...]

It creates a folder named <plate code> (default PLATONIC) with colorful images. It can be dropped
into a dropbox with Python script data-set-handler-plate.py.

If channels are specified black-and-white images are created for each channel. The inset value
specifies the distance from the border of a rectangle to be created. The actual inset is random
number around the specified value. Such data sets can be dropped into a dropbox with Python script
data-set-handler-plate-splitted.py

"""

import imagegen
import os
import shutil
import sys
import getopt

def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)
  
def usage():
    print "generate-test-plate.py [-b|--bit-depth=<integer>] <plate code> [<channel 1>:<inset 1> <channel 2>:<inset 2> ...]"

# Default settings for all options/arguments
config = imagegen.PlateGeneratorConfig()
plateName = "PLATONIC"
  
opts, args = getopt.getopt(sys.argv[1:], "hb:", ["help", "bit-depth="])
for o, a in opts:
    if o in ("-h", "--help"):
        usage()
        sys.exit(2)
    elif o in ("-b", "--bit-depth"):
        config.bit_depth = int(a)
    else:
        print "Option " + o + " unknown. Ignoring."

if len(args) > 0:
  plateName = args[0]
recreateDir(plateName)

config.time_points = [ 5, 10, 15 ]
# Alternative Configurations
#config.depth_points = [ 3, 6, 9 ]
if len(args) > 1:
    config.is_split = True
    color_configs = []
    for color_and_inset in args[1:]:
        splitted = color_and_inset.split(':')
        color_configs = color_configs + [ imagegen.ColorConfig(splitted[0], int(splitted[1])) ]
    config.color_configs = color_configs
    
generator = imagegen.PlateGenerator(config)
generator.generate_raw_images(plateName)
