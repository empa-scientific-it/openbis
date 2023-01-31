/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.ImageLineHelper;

/**
 * An object that encapsulates some knowledge about how PNG files should be created.
 * 
 * @author cramakri
 */
public class PngWritingHelper
{
    public static PngWritingHelper createHelper(BufferedImage image)
    {
        ColorModel colorModel = image.getColorModel();
        int[] componentSize = colorModel.getComponentSize();
        if (componentSize != null)
        {
            return new NormalPngWritingHelper(image, componentSize[0]);
        }
        if ("loci.formats.gui.Index16ColorModel".equals(colorModel.getClass().getName()))
        {
            return new Index16PngWritingHelper(image);
        } else
        {
            throw new IllegalArgumentException(
                    "The image color model does not specify a bit depth for the color channels and is not one of the known special cases.");
        }
    }

    /**
     * The png writing helper that handles most cases.
     * 
     * @author cramakri
     */
    private static class NormalPngWritingHelper extends PngWritingHelper
    {
        private NormalPngWritingHelper(BufferedImage image, int bitDepth)
        {
            super(image, bitDepth);
        }
    }

    /**
     * The png writing helper that handles images that use the loci.formats.gui.Index16ColorModel color model from BioFormats and have no component
     * information. Images that use this color model and have component information are treated normally.
     * 
     * @author cramakri
     */
    private static class Index16PngWritingHelper extends PngWritingHelper
    {
        private Index16PngWritingHelper(BufferedImage image)
        {
            super(image, 8);
        }

        @Override
        protected void fillRGBLine(ImageLine imageLine, int row)
        {
            for (int col = 0; col < cols; ++col)
            {
                WritableRaster raster = image.getRaster();
                short[] value = (short[]) raster.getDataElements(col, row, null);
                // TODO The values converted by the color model seem to be byte swapped. We
                // currently just put the value in the green channel until we figure out how to
                // handle these images.
                // Alpha is ignored by setPixelRGB8
                int pixel = (0 << 24) | (0 << 16) | ((value[0]) << 8) | (0 << 0);
                ImageLineHelper.setPixelRGB8(imageLine, col, pixel);

                // Potentially useful code if we improve the implementation of this method.
                // ColorModel colorModel = image.getColorModel();
                // int a = colorModel.getAlpha(value);
                // int g = colorModel.getGreen(value);
            }
        }

    }

    private static boolean isGrayscale(BufferedImage image)
    {
        return image.getColorModel().getColorSpace().getNumComponents() == 1;
    }

    protected final BufferedImage image;

    protected final int cols;

    protected final int rows;

    protected final boolean hasAlpha;

    protected final boolean isGrayscale;

    protected final int bitDepth;

    private PngWritingHelper(BufferedImage image, int bitDepth)
    {
        this.image = image;
        cols = image.getWidth();
        rows = image.getHeight();
        this.bitDepth = bitDepth;
        hasAlpha = false; // NOTE: it would be nice to support alpha channel
        isGrayscale = isGrayscale(image);
    }

    public int getRows()
    {
        return rows;
    }

    public int getCols()
    {
        return cols;
    }

    public ImageInfo getImageInfo()
    {
        ImageInfo imgInfo = new ImageInfo(cols, rows, bitDepth, hasAlpha, isGrayscale, false);
        return imgInfo;
    }

    public void fillLine(ImageLine imageLine, int row)
    {
        if (isGrayscale)
        {
            fillGrayscaleLine(imageLine, row);
        } else
        {
            fillRGBLine(imageLine, row);
        }
    }

    protected void fillGrayscaleLine(ImageLine imageLine, int row)
    {
        for (int col = 0; col < cols; ++col)
        {
            imageLine.scanline[col] = image.getRaster().getSample(col, row, 0);
        }
    }

    protected void fillRGBLine(ImageLine imageLine, int row)
    {
        for (int col = 0; col < cols; ++col)
        {
            int pixel = image.getRGB(col, row);
            ImageLineHelper.setPixelRGB8(imageLine, col, pixel);
        }
    }
}
