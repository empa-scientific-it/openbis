/**
6   LOCI Plugins for ImageJ: a collection of ImageJ plugins including the
7   Bio-Formats Importer, Bio-Formats Exporter, Bio-Formats Macro Extensions,
8   Data Browser and Stack Slicer. Copyright (C) 2005-@year@ Melissa Linkert,
9   Curtis Rueden and Christopher Peterson.
10  
11  This program is free software; you can redistribute it and/or modify
12  it under the terms of the GNU General Public License as published by
13  the Free Software Foundation; either version 2 of the License, or
14  (at your option) any later version.
15  
16  This program is distributed in the hope that it will be useful,
17  but WITHOUT ANY WARRANTY; without even the implied warranty of
18  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
19  GNU General Public License for more details.
20  
21  You should have received a copy of the GNU General Public License
22  along with this program; if not, write to the Free Software
23  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
24  */

package ch.systemsx.cisd.imagereaders.bioformats;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.ShortProcessor;

import java.io.IOException;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageTools;

/**
 * Utility methods copied from <code>loci.plugins.util.ImageProcessorReader</code>.
 *
 * @author Bernd Rinn
 */
final class BioFormatsImageProcessor
{

    static LUT createColorModel(IFormatReader reader) throws FormatException, IOException
    {
        // NB: If a color table is present, we might as well use it,
        // regardless of the value of isIndexed.
        // if (!isIndexed()) return null;

        byte[][] byteTable = reader.get8BitLookupTable();
        if (byteTable == null)
            byteTable = convertTo8Bit(reader.get16BitLookupTable());
        if (byteTable == null || byteTable.length == 0)
            return null;

        // extract red, green and blue elements
        final int colors = byteTable.length;
        final int samples = byteTable[0].length;
        final byte[] r = colors >= 1 ? byteTable[0] : new byte[samples];
        final byte[] g = colors >= 2 ? byteTable[1] : new byte[samples];
        final byte[] b = colors >= 3 ? byteTable[2] : new byte[samples];
        return new LUT(8, samples, r, g, b);
    }

    static byte[][] convertTo8Bit(short[][] shortTable)
    {
        if (shortTable == null)
            return null;
        byte[][] byteTable = new byte[shortTable.length][256];
        for (int c = 0; c < byteTable.length; c++)
        {
            int len = Math.min(byteTable[c].length, shortTable[c].length);

            for (int i = 0; i < len; i++)
            {
                // NB: you could generate the 8-bit LUT by casting the first 256 samples
                // in the 16-bit LUT to bytes. However, this will not produce optimal
                // results; in many cases, you will end up with a completely black
                // 8-bit LUT even if the original 16-bit LUT contained non-zero samples.
                //
                // Another option would be to scale every 256th value in the 16-bit LUT;
                // this may be a bit faster, but will be less accurate than the
                // averaging approach taken below.

                // TODO: For non-continuous LUTs, this approach does not work well.
                //
                // For an example, try:
                // 'i16&pixelType=uint16&indexed=true&falseColor=true.fake'
                //
                // To fully resolve this issue, we would need to redither the image.
                //
                // At minimum, we should issue a warning to the ImageJ log whenever
                // this convertTo8Bit routine is invoked, so the user is informed.

                int valuesPerBin = shortTable[c].length / byteTable[c].length;
                double average = 0;
                for (int p = 0; p < valuesPerBin; p++)
                {
                    average += shortTable[c][i * valuesPerBin + p];
                }
                average /= valuesPerBin;
                byteTable[c][i] = (byte) (255 * (average / 65535.0));
            }
        }
        return byteTable;
    }

    /**
     * Returns an array of ImageProcessors that represent the given slice. There is one
     * ImageProcessor per RGB channel; i.e., length of returned array == getRGBChannelCount().
     * 
     * @param no Position of image plane.
     */
    static ImageProcessor openProcessor(IFormatReader reader, int no, int channel, int x,
            int y, int w, int h) throws FormatException, IOException
    {
        // read byte array
        byte[] b = reader.openBytes(no, x, y, w, h);

        int channelCount = reader.getRGBChannelCount();
        int type = reader.getPixelType();
        int bpp = FormatTools.getBytesPerPixel(type);
        boolean interleave = reader.isInterleaved();

        if (b.length != w * h * channelCount * bpp && b.length != w * h * bpp)
        {
            throw new FormatException("Invalid byte array length: " + b.length + " (expected w="
                    + w + ", h=" + h + ", c=" + channelCount + ", bpp=" + bpp + ")");
        }

        // create a color model for this plane (null means default)
        final LUT cm = createColorModel(reader);

        // convert byte array to appropriate primitive array type
        boolean isFloat = FormatTools.isFloatingPoint(type);
        boolean isLittle = reader.isLittleEndian();
        boolean isSigned = FormatTools.isSigned(type);

        // construct image processors
        ImageProcessor ip = null;
        byte[] channelData =
                ImageTools.splitChannels(b, channel, channelCount, bpp, false, interleave);
        Object pixels = DataTools.makeDataArray(channelData, bpp, isFloat, isLittle);
        if (pixels instanceof byte[])
        {
            byte[] q = (byte[]) pixels;
            if (q.length != w * h)
            {
                byte[] tmp = q;
                q = new byte[w * h];
                System.arraycopy(tmp, 0, q, 0, Math.min(q.length, tmp.length));
            }
            if (isSigned)
            {
                q = DataTools.makeSigned(q);
            }

            ip = new ByteProcessor(w, h, q, null);
            if (cm != null)
            {
                ip.setColorModel(cm);
            }
        } else if (pixels instanceof short[])
        {
            short[] q = (short[]) pixels;
            if (q.length != w * h)
            {
                short[] tmp = q;
                q = new short[w * h];
                System.arraycopy(tmp, 0, q, 0, Math.min(q.length, tmp.length));
            }
            if (isSigned)
            {
                q = DataTools.makeSigned(q);
            }

            ip = new ShortProcessor(w, h, q, cm);
        } else if (pixels instanceof int[])
        {
            int[] q = (int[]) pixels;
            if (q.length != w * h)
            {
                int[] tmp = q;
                q = new int[w * h];
                System.arraycopy(tmp, 0, q, 0, Math.min(q.length, tmp.length));
            }

            ip = new FloatProcessor(w, h, q);
        } else if (pixels instanceof float[])
        {
            float[] q = (float[]) pixels;
            if (q.length != w * h)
            {
                float[] tmp = q;
                q = new float[w * h];
                System.arraycopy(tmp, 0, q, 0, Math.min(q.length, tmp.length));
            }
            ip = new FloatProcessor(w, h, q, null);
        } else if (pixels instanceof double[])
        {
            double[] q = (double[]) pixels;
            if (q.length != w * h)
            {
                double[] tmp = q;
                q = new double[w * h];
                System.arraycopy(tmp, 0, q, 0, Math.min(q.length, tmp.length));
            }
            ip = new FloatProcessor(w, h, q);
        }

        return ip;
    }

    /**
     * Creates an ImageJ image processor object for the image plane at the given position.
     * 
     * @param page Position of image plane.
     */
    static ImageProcessor openProcessor(IFormatReader reader, int page, int channel)
            throws FormatException, IOException
    {
        return openProcessor(reader, page, channel, 0, 0, reader.getSizeX(), reader.getSizeY());
    }

    private BioFormatsImageProcessor()
    {
        // Not to be instantiated.
    }

}
