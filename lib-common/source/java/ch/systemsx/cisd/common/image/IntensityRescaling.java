/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.EnumSet;

/**
 * Methods for performing an intensity rescaling of images.
 * 
 * @author Bernd Rinn
 */
public class IntensityRescaling
{

    /** The largest number a unsigned short can store. */
    private final static int MAX_USHORT = (1 << 16) - 1;

    public static enum Channel
    {
        RED(0, 16), GREEN(1, 8), BLUE(2, 0);

        private int band;

        private int shift;

        private Channel(int band, int shift)
        {
            this.band = band;
            this.shift = shift;
        }

        public int getShift()
        {
            return shift;
        }

        public int getBand()
        {
            return band;
        }
    }

    /**
     * The levels in a distribution of pixel values for a given symmetric quantile value.
     */
    public static class Levels
    {
        final int minLevel;

        final int maxLevel;

        public Levels(int minLevel, int maxLevel)
        {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        /**
         * The minimal level (black point).
         */
        public int getMinLevel()
        {
            return minLevel;
        }

        /**
         * The maximal level (white point).
         */
        public int getMaxLevel()
        {
            return maxLevel;
        }

        @Override
        public String toString()
        {
            return "MinMax [minLevel=" + minLevel + ", maxLevel=" + maxLevel + "]";
        }

    }

    /**
     * A class to store the histogram of pixel values.
     */
    public static class PixelHistogram
    {
        final int[] histogram = new int[MAX_USHORT + 1];

        int pixelCount = 0;

        /**
         * Returns the total number of pixels in the histogram.
         */
        public int getPixelCount()
        {
            return pixelCount;
        }

        /**
         * Returns the histogram. Changing the returned object changes the pixel histogram.
         */
        public int[] getHistogram()
        {
            return histogram;
        }
    }

    /** @return true if the specified image in not in grayscale */
    public static boolean isNotGrayscale(BufferedImage image)
    {
        return image.getColorModel().getColorSpace().getNumComponents() > 1;
    }

    /** @return Used rgb channels in an image. Returns empty set if image type not RGB */
    public static EnumSet<Channel> getUsedRgbChannels(BufferedImage image)
    {
        EnumSet<Channel> channels = EnumSet.noneOf(Channel.class);

        if (image.getType() != BufferedImage.TYPE_INT_RGB
                && image.getType() != BufferedImage.TYPE_INT_ARGB)
        {
            return channels;
        }

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 16) & 0xff) > 0)
                {
                    channels.add(Channel.RED);
                }
                if (((rgb >> 8) & 0xff) > 0)
                {
                    channels.add(Channel.GREEN);
                }
                if ((rgb & 0xff) > 0)
                {
                    channels.add(Channel.BLUE);
                }
            }
        }
        return channels;
    }

    /**
     * Performs an intensity rescaling on a gray-scale image by shifting all intensities by <var>shiftBits</var> bits.
     * 
     * @param image The original n-bit gray-scale image (n>8).
     * @param shiftBits The number of bits to shift the image by.
     * @return The rescaled 8-bit gray-scale image.
     */
    public static BufferedImage rescaleIntensityBitShiftTo8Bits(Pixels image, int shiftBits)
    {
        final BufferedImage rescaledImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = rescaledImage.getRaster();

        int[] pixelData = image.getPixelData()[0];
        int offset = 0;
        for (int y = 0; y < image.getHeight(); ++y)
        {
            for (int x = 0; x < image.getWidth(); ++x)
            {
                final int intensity = Math.min(255, pixelData[offset++] >>> shiftBits);
                raster.setSample(x, y, 0, intensity);
            }
        }
        return rescaledImage;
    }

    /**
     * Process <var>image</var> and add its pixels to the <var>histogram</var>. Calling this method multiple times with the same <var>histogram</var>
     * accumulates the histogram for all images.
     */
    public static void addToLevelStats(PixelHistogram histogram, Pixels pixels, Channel... channels)
    {
        assert channels.length > 0 : "No channels specified.";
        int[] histogramArray = histogram.getHistogram();
        int[][] pixelData = pixels.getPixelData();
        for (Channel channel : channels)
        {
            int band = channel.getBand();
            if (band < pixelData.length)
            {
                int[] channelPixelData = pixelData[band];
                for (int i = 0; i < channelPixelData.length; i++)
                {
                    histogramArray[channelPixelData[i]]++;
                }
                histogram.pixelCount += channelPixelData.length;
            }
        }
    }

    /**
     * Extracts and stores all pixels of an image. It is usually more efficient to fetch all the pixels from {@link BufferedImage} because then
     * accessing all the pixels one by one is much faster due to compiler optimizations.
     */
    public static class Pixels
    {
        private final int width;

        private final int height;

        private final int[][] pixelData;

        public Pixels(BufferedImage image)
        {
            width = image.getWidth();
            height = image.getHeight();
            ColorModel colorModel = image.getColorModel();
            int numColorComponents = colorModel.getNumColorComponents();
            pixelData = new int[numColorComponents][width * height];
            WritableRaster raster = image.getRaster();
            int numberOfBands = raster.getNumBands();
            int[][] colorIndexMap = null;
            if (numColorComponents == 3 && numberOfBands == 1)
            {
                colorIndexMap = tryCreateColorIndexMap(colorModel);
            }
            if (numberOfBands >= pixelData.length || colorIndexMap != null)
            {
                for (int band = 0, n = Math.min(numberOfBands, pixelData.length); band < n; band++)
                {
                    raster.getSamples(0, 0, width, height, band, pixelData[band]);
                }
                if (colorIndexMap != null)
                {
                    for (int i = 0; i < pixelData[0].length; i++)
                    {
                        int index = pixelData[0][i];
                        for (int c = 0; c < 3; c++)
                        {
                            pixelData[c][i] = colorIndexMap[c][index];
                        }
                    }
                }
            } else
            {
                // In case of the color model isn't a recognized index color model and
                // number of bands is less then number of color components
                // we can not use the fast method
                for (int y = 0; y < height; y++)
                {
                    int offset = y * width;
                    for (int x = 0; x < width; x++)
                    {
                        int rgb = image.getRGB(x, y);
                        for (Channel channel : Channel.values())
                        {
                            pixelData[channel.getBand()][offset + x] = (rgb >> channel.getShift()) & 0xff;
                        }
                    }
                }
            }
        }

        /**
         * Creates the index color map from the specified color model. The result is an array of three integer arrays. The first/second/third array is
         * the red/green/blue index to color map.
         * <p>
         * This method only handles {@link IndexColorModel} for 8bit indicies. To handle other index color models (like
         * loci.formats.gui.Index16ColorModel from the BioFormats library) this method should be overwritten.
         * 
         * @return <code>null</code> if the color model isn't a known index color model.
         */
        protected int[][] tryCreateColorIndexMap(ColorModel colorModel)
        {
            if (colorModel instanceof IndexColorModel == false)
            {
                return null;
            }
            IndexColorModel indexColorModel = (IndexColorModel) colorModel;
            int mapSize = indexColorModel.getMapSize();
            byte[] blues = new byte[mapSize];
            indexColorModel.getBlues(blues);
            byte[] greens = new byte[mapSize];
            indexColorModel.getGreens(greens);
            byte[] reds = new byte[mapSize];
            indexColorModel.getReds(reds);
            int[][] result = new int[3][mapSize];
            copyTo(reds, result[0]);
            copyTo(greens, result[1]);
            copyTo(blues, result[2]);
            return result;
        }

        private void copyTo(byte[] bytes, int[] integers)
        {
            for (int i = 0; i < bytes.length; i++)
            {
                integers[i] = bytes[i] & 0xff;
            }
        }

        /** @return all the pixels of the image */
        public int[][] getPixelData()
        {
            return pixelData;
        }

        /** @return width of the image */
        public int getWidth()
        {
            return width;
        }

        /** @return height of the image */
        public int getHeight()
        {
            return height;
        }

    }

    /**
     * Converts a {@link BufferedImage} instance into a {@link Pixels} instance.
     *
     * @author Franz-Josef Elmer
     */
    public static interface IImageToPixelsConverter
    {
        public Pixels convert(BufferedImage image);
    }

    /**
     * Computes the levels (black point and white point) of the given <var>histogram</var> for the given symmetric <var>threshold</var>. The tail of
     * the histogram below the black point will contain the fraction of <var>treshold</var> of all pixels of the histogram. The same is true for the
     * tail of the <var>histogram</var> above the white point.
     * 
     * @return The levels of the <var>histogram</var> for the <var>treshold</var>.
     */
    public static Levels computeLevels(PixelHistogram histogram, float threshold)
    {
        final int intThreshold = Math.round(threshold * histogram.getPixelCount());
        int[] histogramArray = histogram.getHistogram();

        return computeLevels(intThreshold, histogramArray);
    }

    public static Levels computeLevels(final int intThreshold, int[] histogramArray)
    {
        int min = -1;
        int max = histogramArray.length;

        int minSum = 0;
        int maxSum = 0;

        while (minSum <= intThreshold || maxSum <= intThreshold)
        {
            if (minSum <= intThreshold)
            {
                minSum += histogramArray[++min];
            }
            if (min >= max - 1)
            {
                break;
            }
            if (maxSum <= intThreshold)
            {
                maxSum += histogramArray[--max];
            }
        }

        if (max >= histogramArray.length)
        {
            max = histogramArray.length - 1;
        }
        return new Levels(min, max);
    }

    /**
     * Calculates levels for the specified pixels. It starts with a threshold 0.01 (i.e. cut-off of points to be too light or dark). If the number of
     * levels is less than the specified minimum the threshold is reduced by a factor 10 and the levels are calculated again. This iteration is
     * continue until either the number of levels is large enough or the threshold is below 10<sup>-5</sup>.
     */
    public static Levels computeLevels(Pixels pixels, int minimumNumberOfLevels)
    {
        Levels levels = null;
        for (float threshold = 0.01f; threshold > 1e-5; threshold /= 10)
        {
            levels = computeLevels(pixels, threshold, Channel.values());
            if (levels.getMaxLevel() - levels.getMinLevel() > minimumNumberOfLevels)
            {
                break;
            }
        }
        return levels;
    }

    /**
     * Computes the levels (black point and white point) of the given <var>image</var> for the given symmetric <var>threshold</var>. The tail of the
     * histogram below the black point will contain the fraction of <var>treshold</var> of all pixels of the <var>image</var>. The same is true for
     * the tail of the histogram above the white point.
     * 
     * @param image a gray scale image (will not be checked).
     * @return The levels of the <var>histogram</var> for the <var>treshold</var>.
     */
    public static Levels computeLevels(Pixels image, float threshold, Channel... channels)
    {
        assert channels.length > 0 : "No channels specified.";
        final PixelHistogram stats = new PixelHistogram();
        addToLevelStats(stats, image, channels);
        return computeLevels(stats, threshold);
    }

    /**
     * Computes an intensity rescaled image from the given <var>image</var>, using the black point and white point as defined by <var>levels</var>.
     * Rescaling preserves color hue and saturation.
     */
    public static BufferedImage rescaleIntensityLevelTo8Bits(Pixels pixels, Levels levels, Channel... channels)
    {
        assert channels.length > 0 : "No channels specified.";
        final int width = pixels.getWidth();
        final int height = pixels.getHeight();
        int[][] pixelData = pixels.getPixelData();
        int numberOfColorComponents = pixelData.length;
        int type = numberOfColorComponents == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB;
        final BufferedImage rescaledImage = new BufferedImage(width, height, type);
        WritableRaster rescaledRaster = rescaledImage.getRaster();

        for (int y = 0; y < height; ++y)
        {
            int offset = y * width;
            for (int x = 0; x < width; ++x)
            {
                int intensity = 0;
                for (Channel channel : channels)
                {
                    int band = channel.getBand();
                    if (band < numberOfColorComponents)
                    {
                        intensity = Math.max(intensity, pixelData[band][offset + x]);
                    }
                }
                if (intensity != 0)
                {
                    int rescaledIntensity = rescaleIntensity(intensity, levels);
                    for (Channel channel : channels)
                    {
                        int band = channel.getBand();
                        if (band < numberOfColorComponents)
                        {
                            int bandIntensity = pixelData[band][offset + x];
                            int rescaledBandIntensity = (bandIntensity * rescaledIntensity + intensity / 2) / intensity;
                            rescaledRaster.setSample(x, y, band, rescaledBandIntensity);
                        }
                    }
                }
            }
        }
        return rescaledImage;
    }

    private static int rescaleIntensity(int originalIntensity, Levels levels)
    {
        // cut all intensities above the white point
        int intensity = Math.min(levels.maxLevel, originalIntensity);
        // cut all intensities below the black point and move the origin to 0
        intensity = Math.max(0, intensity - levels.minLevel);
        // normalize to [0, 1] and rescale to 8 bits
        int range = levels.maxLevel - levels.minLevel;
        return (255 * intensity + range / 2) / range;
    }
}