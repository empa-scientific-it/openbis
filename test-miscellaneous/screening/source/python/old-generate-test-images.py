#!/usr/bin/env python

#
# OBSELETE
#
# The code here is being replaced by generate-test-plate.py, but this file should be kept around
# until all the features have been moved to the new file.

import canvas
import math
import os
import shutil

size = 512
well = "A1"
tileNum = 6

def drawRect(canvas, r, g, b, start, isUnfilled = 0):
  canvas.draw_inset_rect(r, g, b, start, isUnfilled)
  
def drawText(canvas, x, y, text):
  canvas.draw_text(x, y, text)

def drawMatrix(coordsList, dir, channel, isOverlay):
  nonemptyTiles = set([ calcTile(coords) for coords in coordsList ])
  for tile in range(1, 10):
    imageCanvas = canvas.TileCanvas(size, size)
    if tile in nonemptyTiles:
      if not isOverlay:
        drawRect(imageCanvas, 0, 0, 0, 0)
        drawRect(imageCanvas, 0.5, 0.5, 0.5, 70, isUnfilled = 0)
      elif isOverlay == 1:
        drawRect(imageCanvas, 1, 1, 1, 30, isUnfilled = 1)
      else:
        drawText(imageCanvas, size/2, size/2, "X")
    destFile = dir + "/c"+channel + "_s" + str(tile) +".png"
    imageCanvas.write_png_file(destFile)

def calcTile(coords):
  x,y = coords
  return (y-1)*3+x


def overlayTests(sampleCode):
  rootDir = "targets/generated-images"
  recreateDir(rootDir)
  
  dir = rootDir + "/" + sampleCode+".basic"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  0);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",   0);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  0);
  
  dir = rootDir + "/" + sampleCode+".overlay-surround"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  1);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",     1);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  1);

  dir = rootDir + "/" + sampleCode+".overlay-text"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS-TEXT",   2);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL-TEXT",    2);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION-TEXT",  2);

  
def save(dir, filename, text, r, g, b, merged = 0):
  imageCanvas = canvas.TileCanvas(size, size)
  drawRect(imageCanvas, 0, 0, 0, 0) # fill with black
  
  zero = 0
  if merged:
    zero = 1
  if r:
    drawRect(imageCanvas, 1, zero, zero, 20) # red
  if g:
    drawRect(imageCanvas, zero, 1, zero, 70) # green
  if b:
    drawRect(imageCanvas, zero, zero, 1, 120) # blue

  # text annotation
  drawText(imageCanvas, 5, size / 2, text)
    
  # Write the bitmap to disk in PNG format
  imageCanvas.write_png_file(dir + "/" + filename)

def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

# -------------------------------

def generateMicroscopy3D(dir, channelName, r, g, b, merged = 0):  
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  series = [1,2,3]
  for seriesNum in series:
    for tile in range(1, tileNum+1):
      for time in timePoints:
        for depth in depthPoints:
          desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)+"_n"+str(seriesNum)
          file = desc+"_c"+channelName+".png"
          save(dir, file, desc, r, g, b, merged)

def generateMicroscopySeries(dir, channelName, r, g, b, merged = 0):  
  for seriesNum in range(1,11):
    for tile in range(1, tileNum+1):
      desc = "s"+str(tile)+"_n"+str(seriesNum)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b, merged)

def generateMicroscopySeriesBrokenTiles(dir, channelName, r, g, b, merged = 0): 
  tiles = [1,3,5]
  for seriesNum in range(1,11):
    for tile in tiles:
      desc = "s"+str(tile)+"_n"+str(seriesNum)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b, merged)
          
def generateMicroscopyChannel(dir, channelName, r, g, b, merged = 0):
  #timePoints = [ 1 ] 
  #tileNum = 1
  #depthPoints = [ 1 ]
  
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      for depth in depthPoints:
        desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)
        file = desc+"_c"+channelName+".png"
        save(dir, file, desc, r, g, b, merged)
        
def generateMicroscopyChannelNoDepth(dir, channelName, r, g, b):
  timePoints = [ 5, 10, 15 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      desc = "s"+str(tile)+"_t"+str(time)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b)

def generateMicroscopyChannelNoTimeNoDepth(dir, channelName, r, g, b):
  for tile in range(1, tileNum+1):
    desc = "s"+str(tile)
    file = desc+"_c"+channelName+".png"
    save(dir, file, desc, r, g, b)
      
# -------------------------------

def generatePlateChannel(dir, channelName, r, g, b, merged = 0):
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      for depth in depthPoints:
        desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)
        file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
        save(dir, file, desc, r, g, b, merged)

def generatePlateChannelNoDepth(dir, channelName, r, g, b):
  timePoints = [ 5, 10, 15 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      desc = "s"+str(tile)+"_t"+str(time)
      file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b)

def generatePlateChannelNoTimeNoDepth(dir, channelName, r, g, b):
  for tile in range(1, tileNum+1):
    desc = "s"+str(tile)
    file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
    save(dir, file, desc, r, g, b)


def generatePlateSplitChannels(dir):
  recreateDir(dir)
  generatePlateChannel(dir, "RED", 1, 0, 0,   1)
  generatePlateChannel(dir, "GREEN", 0, 1, 0, 1)
  generatePlateChannel(dir, "BLUE", 0, 0, 1,  1)

def generatePlateMergedChannels(dir):
  recreateDir(dir)
  generatePlateChannel(dir, "RGB", 1, 1, 1)
  
def generatePlateMergedChannelsNoTimeNoDepth(dir):
  recreateDir(dir)
  generatePlateChannelNoTimeNoDepth(dir, "RGB", 1, 1, 1)

# -------------------------------

def generateMicroscopySplitChannels(dir):
  recreateDir(dir)
  generateMicroscopyChannel(dir, "RED", 1, 0, 0,    1)
  generateMicroscopyChannel(dir, "GREEN", 0, 1, 0,  1)
  generateMicroscopyChannel(dir, "BLUE", 0, 0, 1,   1)

def generateMicroscopyMergedChannels(dir):
  recreateDir(dir)
  generateMicroscopyChannel(dir, "RGB", 1, 1, 1)

def generateMicroscopyMergedChannelsNoTimeNoDepth():
  dir = "TEST.microscopy.mergedChannels_tile_noTime_noDepth"
  recreateDir(dir)
  generateMicroscopyChannelNoTimeNoDepth(dir, "RGB", 1, 1, 1)

def generateMicroscopyMergedChannelsNoDepth():
  dir = "TEST.microscopy.mergedChannels_tile_time_noDepth"
  recreateDir(dir)
  generateMicroscopyChannelNoDepth(dir, "RGB", 1, 1, 1)

def generateMicroscopySplitChannelsSeriesBrokenTiles():
  dir = "TEST.microscopy.splitChannels_someTiles_series"
  recreateDir(dir)
  generateMicroscopySeriesBrokenTiles(dir, "AAA", 1, 0, 0,  1)
  generateMicroscopySeriesBrokenTiles(dir, "BBB", 0, 1, 0,  1)
  generateMicroscopySeriesBrokenTiles(dir, "CCC", 0, 0, 1,  1)

def generateMicroscopySplitChannels3D():
  dir = "TEST.microscopy.splitChannels_3D"
  recreateDir(dir)
  generateMicroscopy3D(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopy3D(dir, "DDD", 0, 1, 0,   1)
  generateMicroscopy3D(dir, "EEE", 0, 0, 1,   1)
  
def generateMicroscopySplitChannelsSeries():
  dir = "TEST.microscopy.splitChannels_series"
  recreateDir(dir)
  generateMicroscopySeries(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopySeries(dir, "CCC", 0, 1, 0,   1)
  generateMicroscopySeries(dir, "FFF", 0, 0, 1,   1)

def generateMicroscopySplitChannelsSeries():
  dir = "TEST.microscopy.splitChannels_series"
  recreateDir(dir)
  generateMicroscopySeries(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopySeries(dir, "BBB", 0, 1, 0,   1)
  generateMicroscopySeries(dir, "CCC", 0, 0, 1,   1)
  generateMicroscopySeries(dir, "DDD", 1, 1, 0, 1)
  generateMicroscopySeries(dir, "EEE", 0, 1, 1,   1)
  generateMicroscopySeries(dir, "FFF", 1, 0, 1,   1)

#generatePlateSplitChannels("PLATE.splitChannels_tile_time_depth") 
generatePlateMergedChannelsNoTimeNoDepth("PLATE.mergedChannels_tile_time_depth")  
#generateMicroscopySplitChannels("TEST.microscopy.splitChannels_tile_time_depth")
#generateMicroscopyMergedChannels("TEST.microscopy.mergedChannels_tile_time_depth")
#generateMicroscopyMergedChannels3D("TEST.microscopy.mergedChannels_tile_time_depth_series")
#generateMicroscopyMergedChannelsNoTimeNoDepth()
#generateMicroscopyMergedChannelsNoDepth()
#generateMicroscopySplitChannelsSeriesBrokenTiles()
#generateMicroscopySplitChannelsSeries()
#generateMicroscopySplitChannels3D()
#overlayTests("OVERLAY-TEST")
