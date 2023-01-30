#!/usr/bin/env python

"""
A module with classes and functions for generating test images, plates, and overlays for the screening version of openBIS.
"""

import canvas
import string
import random

def generate_tile_description(tile, time = None, depth = None):
    """
    Generate a description for the combination of well, tile, channel
    and, optionaly, depth and/or time
    """
    
    desc = "s"+ str(tile)
    if depth is not None:
        desc = desc + "_z" + str(depth)
    if time is not None:
        desc = desc + "_t" + str(time)
    return desc

def generate_file_name(well, channel, desc):
    """
    Generate a name for a file using the description and channel
    """
    
    return "bPLATE_w" + well + "_" + desc + "_c" + channel + ".png"

def drawRect(canvas, r, g, b, start, fill = False):
    canvas.draw_inset_rect(r, g, b, start, fill)

def drawText(canvas, x, y, text):
    canvas.draw_text(x, y, text)
    
def calcTile(coords):
    x,y = coords
    return (y-1)*3+x
    
class ColorConfig:
    """
    Color name and color channels for an image to be created
    """
    def __init__(self, color_name, inset):
        self.color_name = color_name
        self.inset = inset
    
class ImageGeneratorConfig:
    """
    Represents the configuration options for generating images
    """
    def __init__(self):
        self.number_of_tiles = 9
        self.image_size = 512
        self.bit_depth = 8
        self.is_split = False
        self.color_configs = [ None ]
        # Use an array with None for not time points / depth points
        self.time_points = [ None ]
        self.depth_points = [ None ]
        self.tile_description_generator = generate_tile_description

class PlateGeneratorConfig(ImageGeneratorConfig):
    """
    Represents the configuration options for generating plates
    """
    def __init__(self):
        ImageGeneratorConfig.__init__(self)
        self.rows = 8
        self.cols = 12

class WellGenerator:
    """
    A class that generates raw images or overlays for a well. Used by the PlateGenerator and ImageGenerator.
    """
    
    def __init__(self, config, well, directory):
        """Constructor that takes the config and the well we are generating images for."""
        self.config = config
        self.well = well
        self.directory = directory
        
    def generate_raw_images(self):
        for tile in range(1, self.config.number_of_tiles + 1):
            for time in self.config.time_points:
                for depth in self.config.depth_points:
                    desc = self.config.tile_description_generator(tile, time, depth)
                    if self.config.is_split:
                        for color_config in self.config.color_configs:
                             file_name = self._generate_raw_file_name(self.well, color_config.color_name, desc)
                             self._generate_tile(file_name, desc, color_config.inset)
                    else:
                        file_name = self._generate_raw_file_name(self.well, "RGB", desc)
                        self._generate_tile(file_name, desc, 1)
                        
    def generate_overlay_images(self, overlay_name, x, y):
        """
        Generate overlay images containing text x, y
        
        The arguments x and y may be negative, in which case the position will be offset from the right/top edge of the image.
        """
        for tile in range(1, self.config.number_of_tiles + 1):
            for time in self.config.time_points:
                for depth in self.config.depth_points:
                    desc = self.config.tile_description_generator(tile, time, depth)
                    file_name = self._generate_overlay_file_name(self.well, overlay_name, desc)
                    self._generate_overlay(file_name, overlay_name + '-' + self.well + '-' + desc, x, y)        
        
    def _generate_tile(self, filename, tile_desc, inset):        
        if self.config.is_split:
            # if split, we want to draw white instead of the specified color, since
            # the channel assignment will happen in openBIS, not here
            tile_canvas = canvas.TileCanvas(self.config.image_size, self.config.image_size, self.config.bit_depth, True)
            drawRect(tile_canvas, 0, 0, 0, 0, False) # fill with black
            drawRect(tile_canvas, 1, 1, 1, inset * random.gauss(1.0, 0.2), False)
        else:
            tile_canvas = canvas.TileCanvas(self.config.image_size, self.config.image_size)
            drawRect(tile_canvas, 0, 0, 0, 0) # fill with black
            drawRect(tile_canvas, 1, 0, 0, 20) # red
            drawRect(tile_canvas, 0, 1, 0, 70) # green
            drawRect(tile_canvas, 0, 0, 1, random.randint(120, 200)) # blue
    
        # text annotation
        drawText(tile_canvas, 5, self.config.image_size - 70, self.directory)
        drawText(tile_canvas, 5, self.config.image_size / 2, tile_desc)
        drawText(tile_canvas, 5, 5, self.well)

        # Write the bitmap to disk in PNG format
        tile_canvas.write_png_file(self.directory + "/" + filename)
        
    def _generate_overlay(self, filename, overlay_desc, x, y):
        tile_canvas = canvas.TileCanvas(self.config.image_size, self.config.image_size)
        
        textx = x
        if textx < 0:
            textx = self.config.image_size - x
        texty = y
        if texty < 0:
            texty = self.config.image_size - y
        # text annotation
        drawText(tile_canvas, textx, texty, overlay_desc)

        # Write the bitmap to disk in PNG format
        tile_canvas.write_png_file(self.directory + "/" + filename)
        
    def _generate_raw_file_name(self, well, channel, desc):
        """
        Generate a name for a file using the description and channel
        """
            
        return "bPLATE_w" + well + "_" + desc + "_c" + channel + ".png"
        
    def _generate_overlay_file_name(self, well, channel, desc):
        """
        Generate a name for a file using the description and channel
        """
            
        return "c" + channel + "_w" + well + "_" + desc + ".png"

        
class PlateGenerator:
    """
    A class that generates raw images or overlays for a plate. Similar 
    to GenericImageGenerator, but with plates added.
    
    To use, instantiate it with a PlateGeneratorConfig that describes its properties, then
    call generate_raw_images or generate_overlays
    """
    
    def __init__(self, config):
        """Constructor that takes the configuration for the plate"""
        self.config = config
        
    def generate_raw_images(self, directory):
        for row in range(0, self.config.rows):
            for col in range(1, self.config.cols + 1):
                well = string.letters[26 + row] + str(col)
                well_generator = WellGenerator(self.config, well, directory)
                well_generator.generate_raw_images()
                
    def generate_overlay_images(self, directory, overlay_name, x, y):
        for row in range(0, self.config.rows):
            for col in range(1, self.config.cols + 1):
                well = string.letters[26 + row] + str(col)
                well_generator = WellGenerator(self.config, well, directory)
                well_generator.generate_overlay_images(overlay_name, x, y)

class GenericImageGeneratorConfig(ImageGeneratorConfig):
    """
    Represents the configuration options for generating plates
    """
    def __init__(self):
        ImageGeneratorConfig.__init__(self)
        self.number_of_tiles = 1
        
class GenericImageGenerator:
    """
    A class that generates raw images or overlays simulating images from a microscope. Similar to PlateGenerator, except without the plates.
    
    To use, instantiate it with a ImageGeneratorConfig that describes its properties, then
    call generate_raw_images or generate_overlays
    """
    
    def __init__(self, config):
        """Constructor that takes the configuration for the plate"""
        self.config = config
        
    def generate_raw_images(self, directory):
        well_generator = WellGenerator(self.config, "", directory)
        well_generator.generate_raw_images()
