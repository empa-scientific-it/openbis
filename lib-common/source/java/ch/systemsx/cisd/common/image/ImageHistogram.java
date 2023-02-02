/*
 * Copyright 2014 ETH Zuerich, SIS
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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;

/**
 * Class which calculates color histograms (in red, green, and blue) of {@link BufferedImage} instances.
 *
 * @author Franz-Josef Elmer
 */
public class ImageHistogram
{
    private static final class ASCIICanvas
    {
        private int width;

        private int height;

        private char[] canvas;

        ASCIICanvas(int width, int height)
        {
            this.height = height;
            this.width = width + 1;
            canvas = new char[this.width * height];
            Arrays.fill(canvas, ' ');
        }

        void setPixel(int x, int y, char symbol)
        {
            canvas[y * width + x] = symbol;
        }

        void drawHorizontalLine(int x, int y, int length, char symbol)
        {
            for (int i = x, n = Math.min(x + length, width); i < n; i++)
            {
                canvas[y * width + i] = symbol;
            }
        }

        void drawVerticalLine(int x, int y, int length, char symbol)
        {
            for (int i = y, n = Math.min(y + length, height); i < n; i++)
            {
                canvas[i * width + x] = symbol;
            }
        }

        void drawCenteredText(int x, int y, String text)
        {
            for (int i = 0; i < text.length(); i++)
            {
                canvas[y * width + x + i - text.length() / 2] = text.charAt(i);
            }
        }

        @Override
        public String toString()
        {
            drawVerticalLine(width - 1, 0, height, '\n');
            return new String(canvas);
        }
    }

    private static final int EIGHT_BIT_MASK = 0xff;

    private static final int FIVE_BIT_MASK = 0x1f;

    private static final int SIX_BIT_MASK = 0x3f;

    /**
     * Calculates the color histogram of the specified image.
     */
    public static final ImageHistogram calculateHistogram(BufferedImage image)
    {
        int type = image.getType();
        DataBuffer dataBuffer = image.getData().getDataBuffer();
        if (dataBuffer instanceof DataBufferInt)
        {
            DataBufferInt db = (DataBufferInt) dataBuffer;
            switch (type)
            {
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_INT_RGB:
                    return calculateHistogram(db, 16, 8, 0);
                case BufferedImage.TYPE_INT_BGR:
                    return calculateHistogram(db, 0, 8, 16);
                default:
                    return calculateHistogramSlow(image);
            }
        }
        if (dataBuffer instanceof DataBufferByte)
        {
            DataBufferByte db = (DataBufferByte) dataBuffer;
            switch (type)
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                    return calculateHistogram(db, 3, 2, 1, 0);
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                    return calculateHistogram(db, 4, 3, 2, 1);
                case BufferedImage.TYPE_BYTE_GRAY:
                    return calculateHistogram(db, 1, 0, 0, 0);
                default:
                    return calculateHistogramSlow(image);
            }
        }
        if (dataBuffer instanceof DataBufferUShort)
        {
            DataBufferUShort db = (DataBufferUShort) dataBuffer;
            switch (type)
            {
                case BufferedImage.TYPE_USHORT_555_RGB:
                    return calculateHistogram(db, 10,
                            FIVE_BIT_MASK, 5, FIVE_BIT_MASK, 0, FIVE_BIT_MASK);
                case BufferedImage.TYPE_USHORT_565_RGB:
                    return calculateHistogram(db, 11,
                            FIVE_BIT_MASK, 5, SIX_BIT_MASK, 0, FIVE_BIT_MASK);
                case BufferedImage.TYPE_USHORT_GRAY:
                    return calculateHistogram(db, 8,
                            EIGHT_BIT_MASK, 8, EIGHT_BIT_MASK, 8, EIGHT_BIT_MASK);
                default:
                    return calculateHistogramSlow(image);
            }
        }
        return calculateHistogramSlow(image);
    }

    private static final ImageHistogram calculateHistogramSlow(BufferedImage image)
    {
        int[] redCounters = new int[256];
        int[] greenCounters = new int[256];
        int[] blueCounters = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);
        for (int pixel : rgbArray)
        {
            redCounters[(pixel >> 16) & EIGHT_BIT_MASK]++;
            greenCounters[(pixel >> 8) & EIGHT_BIT_MASK]++;
            blueCounters[pixel & EIGHT_BIT_MASK]++;
        }
        return new ImageHistogram(redCounters, greenCounters, blueCounters);
    }

    private static final ImageHistogram calculateHistogram(DataBufferInt dataBuffer,
            int redShift, int greenShift, int blueShift)
    {
        int[] data = dataBuffer.getData();
        int[] redCounters = new int[256];
        int[] greenCounters = new int[256];
        int[] blueCounters = new int[256];
        for (int pixel : data)
        {
            redCounters[(pixel >> redShift) & EIGHT_BIT_MASK]++;
            greenCounters[(pixel >> greenShift) & EIGHT_BIT_MASK]++;
            blueCounters[(pixel >> blueShift) & EIGHT_BIT_MASK]++;
        }
        return new ImageHistogram(redCounters, greenCounters, blueCounters);
    }

    private static final ImageHistogram calculateHistogram(DataBufferUShort dataBuffer,
            int redShift, int redMask, int greenShift, int greenMask, int blueShift, int blueMask)
    {
        short[] data = dataBuffer.getData();
        int[] redCounters = new int[256];
        int[] greenCounters = new int[256];
        int[] blueCounters = new int[256];
        for (short pixel : data)
        {
            redCounters[(pixel >> redShift) & redMask]++;
            greenCounters[(pixel >> greenShift) & greenMask]++;
            blueCounters[(pixel >> blueShift) & blueMask]++;
        }
        return new ImageHistogram(redCounters, greenCounters, blueCounters);
    }

    private static final ImageHistogram calculateHistogram(DataBufferByte dataBuffer, int pixelSize,
            int redIndex, int greenIndex, int blueIndex)
    {
        byte[] data = dataBuffer.getData();
        int[] redCounters = new int[256];
        int[] greenCounters = new int[256];
        int[] blueCounters = new int[256];
        for (int i = 0, n = data.length; i < n; i += pixelSize)
        {
            redCounters[data[i + redIndex] & EIGHT_BIT_MASK]++;
            greenCounters[data[i + greenIndex] & EIGHT_BIT_MASK]++;
            blueCounters[data[i + blueIndex] & EIGHT_BIT_MASK]++;
        }
        return new ImageHistogram(redCounters, greenCounters, blueCounters);
    }

    private final int[] redCounters;

    private final int[] greenCounters;

    private final int[] blueCounters;

    private ImageHistogram(int[] redCounters, int[] greenCounters, int[] blueCounters)
    {
        this.redCounters = redCounters;
        this.greenCounters = greenCounters;
        this.blueCounters = blueCounters;
    }

    /**
     * Returns the histogram of the 8-bit red color component.
     */
    public int[] getRedHistogram()
    {
        return redCounters;
    }

    /**
     * Returns the histogram of the 8-bit green color component.
     */
    public int[] getGreenHistogram()
    {
        return greenCounters;
    }

    /**
     * Returns the histogram of the 8-bit blue color component.
     */
    public int[] getBlueHistogram()
    {
        return blueCounters;
    }

    @Override
    public String toString()
    {
        return renderAsASCIIChart(40, 10);
    }

    /**
     * Creates a string with the chart of the histogram.
     * 
     * @param width Number of characters in width.
     * @param height Number of lines.
     */
    public String renderAsASCIIChart(int width, int height)
    {
        if (height < 5)
        {
            throw new IllegalArgumentException("Less than minimum height of 5: " + height);
        }
        ASCIICanvas canvas = new ASCIICanvas(width, height);
        if (isGray())
        {
            if (width < 5)
            {
                throw new IllegalArgumentException("Less than minimum width of 5: " + width);
            }
            drawHistogram(canvas, 0, 0, width, height, redCounters, "gray");
        } else
        {
            if (width < 10)
            {
                throw new IllegalArgumentException("Less than minimum width of 10: " + width);
            }
            int w3 = (width + 2) / 3;
            drawHistogram(canvas, 0, 0, w3, height, redCounters, "red");
            drawHistogram(canvas, w3 - 1, 0, w3, height, greenCounters, "green");
            drawHistogram(canvas, 2 * w3 - 2, 0, w3, height, blueCounters, "blue");
        }
        return canvas.toString();
    }

    private void drawHistogram(ASCIICanvas canvas, int x, int y, int width, int height, int[] data, String title)
    {
        canvas.drawHorizontalLine(x, y, width, '-');
        canvas.drawHorizontalLine(x, y + height - 2, width, '_');
        canvas.drawVerticalLine(x, y, height - 1, '|');
        canvas.drawVerticalLine(x + width - 1, y, height - 1, '|');
        canvas.setPixel(x, y, '+');
        canvas.setPixel(x + width - 1, y, '+');
        canvas.setPixel(x + width - 1, y + height - 2, '|');
        canvas.setPixel(x, y + height - 2, '|');
        canvas.drawCenteredText(x + width / 2, y + height - 1, title);
        int[] bins = new int[width - 2];
        int max = 0;
        for (int i = 0; i < data.length; i++)
        {
            int binIndex = (i * bins.length) / data.length;
            bins[binIndex] += data[i];
            max = Math.max(max, bins[binIndex]);
        }
        for (int i = 0; i < bins.length; i++)
        {
            int barLength = (bins[i] * (height - 2)) / max;
            canvas.drawVerticalLine(x + 1 + i, y + height - 1 - barLength, barLength, '*');
        }
    }

    public boolean isGray()
    {
        for (int i = 0; i < redCounters.length; i++)
        {
            int red = redCounters[i];
            int green = greenCounters[i];
            int blue = blueCounters[i];
            if (red != green || green != blue || blue != red)
            {
                return false;
            }
        }
        return true;
    }
}
