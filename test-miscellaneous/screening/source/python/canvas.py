from Quartz import *
from Cocoa import *
from LaunchServices import * # for kUTTypePNG

class TileCanvas:
    """
    A canvas with utility methods for drawing images for tiles of plate wells.
    
    The utility methods support drawing source images and overlays and
    allow the resulting images to be written to files.
    
    The canvas uses an underlying CoreGraphics bitmap context with the
    device RGB or device gray color space.
    """
    
    def __init__(self, width = 512, height = 512, bitdepth = 8, grayscale = False):
        """Constructor that takes the size of the canvas."""
        self.width = width
        self.height = height
        self.bitdepth = bitdepth
        if grayscale:
            self.ctx = self._create_gray_bitmap_context()
        else:
            self.ctx = self._create_rgba_bitmap_context()
        
    def draw_inset_rect(self, r, g, b, inset, fill = True):
        """Draw a rectange inset in the canvas."""
        ctx = self.ctx
        with CGSavedGState(ctx):
            CGContextSetRGBStrokeColor(ctx, r, g, b, 1)
            CGContextSetRGBFillColor(ctx, r, g, b, 1)
            CGContextSetLineWidth(ctx, 40)
            CGContextAddRect(ctx, CGRectMake(inset, inset, self.width - 2 * inset, self.height - 2 * inset))
            if fill:
                draw_mode = kCGPathFillStroke
            else:
                draw_mode = kCGPathStroke
            CGContextDrawPath(ctx, draw_mode)
    
    def draw_text(self, x, y, text):
        """Draw some text"""
        ctx = self.ctx
        with CGSavedGState(ctx):
            CGContextSetRGBStrokeColor(ctx, 0, 0, 0, 1) # black
            CGContextSetRGBFillColor(ctx, 1, 1, 1, 1) # white
            CGContextSetTextMatrix(ctx, CGAffineTransformIdentity)
            CGContextSelectFont(ctx, "Helvetica Neue", 70, kCGEncodingMacRoman)
            CGContextSetTextDrawingMode(ctx, kCGTextFill)
            CGContextShowTextAtPoint(ctx, x, y, text, len(text))
        
    def write_png_file(self, filename):
        """Write the graphics context to a PNG file"""
        ctx = self.ctx
        image = CGBitmapContextCreateImage(ctx)
        fileUrl = NSURL.fileURLWithPath_(filename)
        dest = CGImageDestinationCreateWithURL(fileUrl, kUTTypePNG, 1, None)
        options = NSMutableDictionary.dictionary()
        options[kCGImagePropertyDepth] = self.bitdepth
        CGImageDestinationAddImage(dest, image, options)
        CGImageDestinationFinalize(dest)
    
    
    # Private Methods
    def _create_rgba_bitmap_context(self):
        """Create a new CG Graphics Context."""
        color_space = CGColorSpaceCreateDeviceRGB()
        pixels_wide = self.width
        pixels_high = self.height
        bits_per_component = 8
        number_of_components = 4 # r,g,b,a
        # Convert bits per component to bytes per pixel, rounding up to the next int if necessary
        bytes_per_pixel = (bits_per_component * number_of_components + 7)/8
        bitmap_bytes_per_row = (pixels_wide * bytes_per_pixel);
        # Create an RGB bitmap, let CoreGraphics deal with allocating and managing memory
        ctx = CGBitmapContextCreate(
                    None, pixels_wide, pixels_high, bits_per_component, 
                    bitmap_bytes_per_row, color_space, kCGImageAlphaPremultipliedLast)
        return ctx

    def _create_gray_bitmap_context(self):
        """Create a new CG Graphics Context."""
        color_space = CGColorSpaceCreateDeviceGray()
        pixels_wide = self.width
        pixels_high = self.height
        bits_per_component = self.bitdepth
        number_of_components = 1 # gray
        # Convert bits per component to bytes per pixel, rounding up to the next int if necessary
        bytes_per_pixel = (bits_per_component * number_of_components + 7)/8
        bitmap_bytes_per_row = (pixels_wide * bytes_per_pixel);
        # Create an RGB bitmap, let CoreGraphics deal with allocating and managing memory
        ctx = CGBitmapContextCreate(
                    None, pixels_wide, pixels_high, bits_per_component, 
                    bitmap_bytes_per_row, color_space, kCGImageAlphaNone)
        return ctx