#!/usr/bin/python

from CoreGraphics import *
import math
import os
import shutil

size = 512
well = "A1"
tileNum = 6

def drawRect(context, r, g, b, start, isUnfilled = 0):
	context.saveGState ()
	context.setRGBStrokeColor (r,g,b,1)
	if isUnfilled:
		context.setRGBFillColor (0,0,0,0) # transparent
	else:
		context.setRGBFillColor (r,g,b,1)
	context.setLineWidth (40)
	context.addRect (CGRectMake (start, start, size - 2*start, size - 2*start))
	context.drawPath (kCGPathFillStroke);
	context.restoreGState ()

def createContext():
	cs = CGColorSpaceCreateDeviceRGB ()
	return CGBitmapContextCreateWithColor (size, size, cs, (0,0,0,0))

# -------------------------
"""
def drawMatrixRect(context, isOverlay, x,y):
	if isOverlay < 2:
		context.saveGState ()
		if isOverlay == 0:
			context.setRGBFillColor (0.5,0.5,0.5,1)
			context.setLineWidth (40)
			ident = 0
		else:
			context.setRGBFillColor (0,0,0,0)
			context.setLineWidth (5)
			context.setRGBStrokeColor (1,1,1,1)
			ident = 10
		context.addRect (CGRectMake (ident+(x-1)*(size/3), ident+(y-1)*(size/3), 
									 size/3 - ident*2, size/3 - ident*2))
		context.drawPath (kCGPathFillStroke);
		context.restoreGState ()
	else:
		drawText(context, (x-0.5)*(size/3), (y-0.5)*(size/3), "X")
"""

def calcTile(coords):
	x,y = coords
	return (y-1)*3+x
			
def drawMatrix(coordsList, dir, channel, isOverlay):
	nonemptyTiles = set([ calcTile(coords) for coords in coordsList ])
	for tile in range(1, 10):
		c = createContext()
		if tile in nonemptyTiles:
			if not isOverlay:
				drawRect(c, 0, 0, 0, 0)
				drawRect(c, 0.5, 0.5, 0.5, 70, isUnfilled = 0)
			elif isOverlay == 1:
				drawRect(c, 1, 1, 1, 30, isUnfilled = 1)
			else:
				drawText(c, size/2, size/2, "X")
		destFile = dir + "/c"+channel + "_s" + str(tile) +".png"
		c.writeToFile (destFile, kCGImageFormatPNG)

def overlayTests(sampleCode):
	rootDir = "targets/generated-images"
	recreateDir(rootDir)
	
	dir = rootDir + "/" + sampleCode+".basic"
	recreateDir(dir)
	drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",	0);
	drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",		0);
	drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",	0);
	
	dir = rootDir + "/" + sampleCode+".overlay-surround"
	recreateDir(dir)
	drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",	1);
	drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL", 		1);
	drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  1);

	dir = rootDir + "/" + sampleCode+".overlay-text"
	recreateDir(dir)
	drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS-TEXT",   2);
	drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL-TEXT",		2);
	drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION-TEXT",  2);

# ---------------------------
def drawText(c, x, y, text):
	c.saveGState ()
	c.setRGBStrokeColor (0,0,0,1)
	c.setRGBFillColor (1,1,1,1)
	c.selectFont ("Helvetica", 70, kCGEncodingMacRoman)
	c.setTextPosition (x, y)
	c.setTextDrawingMode (kCGTextFillStroke)
	c.showText (text, len(text))
	c.restoreGState ()
	
def save(dir, filename, text, r, g, b, merged = 0):
	c = createContext()
	drawRect(c, 0, 0, 0, 0) # fill with black
	
	zero = 0
	if merged:
		zero = 1
	if r:
		drawRect(c, 1, zero, zero, 20) # red
	if g:
		drawRect(c, zero, 1, zero, 70) # green
	if b:
		drawRect(c, zero, zero, 1, 120) # blue

	# text annotation
	drawText(c, 5, size / 2, text)
		
	# Write the bitmap to disk in PNG format
	c.writeToFile (dir + "/" + filename, kCGImageFormatPNG)

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
	generatePlateChannel(dir, "RED", 1, 0, 0, 	1)
	generatePlateChannel(dir, "GREEN", 0, 1, 0, 1)
	generatePlateChannel(dir, "BLUE", 0, 0, 1, 	1)

def generatePlateMergedChannels(dir):
	recreateDir(dir)
	generatePlateChannel(dir, "RGB", 1, 1, 1)

# -------------------------------

def generateMicroscopySplitChannels(dir):
	recreateDir(dir)
	generateMicroscopyChannel(dir, "RED", 1, 0, 0, 		1)
	generateMicroscopyChannel(dir, "GREEN", 0, 1, 0, 	1)
	generateMicroscopyChannel(dir, "BLUE", 0, 0, 1, 	1)

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
	generateMicroscopySeriesBrokenTiles(dir, "AAA", 1, 0, 0,	1)
	generateMicroscopySeriesBrokenTiles(dir, "BBB", 0, 1, 0, 	1)
	generateMicroscopySeriesBrokenTiles(dir, "CCC", 0, 0, 1, 	1)

def generateMicroscopySplitChannels3D():
	dir = "TEST.microscopy.splitChannels_3D"
	recreateDir(dir)
	generateMicroscopy3D(dir, "AAA", 1, 0, 0,	1)
	generateMicroscopy3D(dir, "DDD", 0, 1, 0, 	1)
	generateMicroscopy3D(dir, "EEE", 0, 0, 1, 	1)
	
def generateMicroscopySplitChannelsSeries():
	dir = "TEST.microscopy.splitChannels_series"
	recreateDir(dir)
	generateMicroscopySeries(dir, "AAA", 1, 0, 0,	1)
	generateMicroscopySeries(dir, "CCC", 0, 1, 0, 	1)
	generateMicroscopySeries(dir, "FFF", 0, 0, 1, 	1)

def generateMicroscopySplitChannelsSeries():
	dir = "TEST.microscopy.splitChannels_series"
	recreateDir(dir)
	generateMicroscopySeries(dir, "AAA", 1, 0, 0,	1)
	generateMicroscopySeries(dir, "BBB", 0, 1, 0, 	1)
	generateMicroscopySeries(dir, "CCC", 0, 0, 1, 	1)
	generateMicroscopySeries(dir, "DDD", 1, 1, 0,	1)
	generateMicroscopySeries(dir, "EEE", 0, 1, 1, 	1)
	generateMicroscopySeries(dir, "FFF", 1, 0, 1, 	1)

#generatePlateSplitChannels("PLATE.splitChannels_tile_time_depth") 
#generatePlateMergedChannels("PLATE.mergedChannels_tile_time_depth") 
#generateMicroscopySplitChannels("TEST.microscopy.splitChannels_tile_time_depth")
#generateMicroscopyMergedChannels("TEST.microscopy.mergedChannels_tile_time_depth")
#generateMicroscopyMergedChannels3D("TEST.microscopy.mergedChannels_tile_time_depth_series")
#generateMicroscopyMergedChannelsNoTimeNoDepth()
#generateMicroscopyMergedChannelsNoDepth()
#generateMicroscopySplitChannelsSeriesBrokenTiles()
#generateMicroscopySplitChannelsSeries()
#generateMicroscopySplitChannels3D()
overlayTests("OVERLAY-TEST")