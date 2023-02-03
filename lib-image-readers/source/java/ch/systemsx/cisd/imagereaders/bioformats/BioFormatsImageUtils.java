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

package ch.systemsx.cisd.imagereaders.bioformats;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import loci.common.IRandomAccess;
import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatHandler;
import loci.formats.IFormatHandler;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.codec.ZlibCodec;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.CellomicsReader;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.imagereaders.ImageID;

/**
 * A utility class to use bio-formats to read images.
 * 
 * @author Bernd Rinn
 */
final class BioFormatsImageUtils
{

    private static final IFormatReader[] READERS = new ImageReader().getReaders();

    static
    {
        MetadataTools.setDefaultDateEnabled(false);
    }

    /**
     * Tries to create a suitable reader for the file specified with <var>fileName</var>. This is a
     * factory method which returns for each invocation a new instance of a suitable reader. May
     * return <code>null</code> if no suitable reader is found.
     */
    public static IFormatReader tryToCreateReaderForFile(String fileName)
    {
        synchronized (READERS)
        {
            for (IFormatReader reader : READERS)
            {
                try
                {
                    if (reader.isThisType(fileName))
                    {
                        return createReader(reader.getClass());
                    }

                } finally
                {
                    // "r.isThisType(fileName)" line can open a file handle,
                    // so we need to close it
                    closeOpenedFiles(reader);
                }
            }
        }
        return null;
    }

    private static void closeOpenedFiles(IFormatReader r)
    {
        try
        {
            r.close(true);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }

    }

    /**
     * Tries to create an {@link IFormatReader} for a specified name. This is a factory method which
     * returns for each invocation a new instance of the requested reader. May return
     * <code>null</code> if no corresponding reader is found.
     */
    public static IFormatReader tryToCreateReaderByName(String readerName)
            throws IllegalArgumentException
    {
        for (Class<? extends IFormatReader> clazz : ImageReader.getDefaultReaderClasses()
                .getClasses())
        {
            if (clazz.getSimpleName().equals(readerName))
            {
                return createReader(clazz);
            }
        }
        return null;
    }

    private static IFormatReader createReader(Class<? extends IFormatReader> clazz) throws Error
    {
        try
        {
            return clazz.newInstance();
        } catch (InstantiationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        } catch (IllegalAccessException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Return a list with the names of all known readers.
     */
    public static List<String> getReaderNames()
    {
        List<String> readerNames = new ArrayList<String>(READERS.length);
        for (IFormatReader reader : READERS)
        {
            String readerName = getReaderName(reader);
            readerNames.add(readerName);
        }
        return readerNames;
    }

    /**
     * Return the name of a {@link IFormatReader}.
     */
    public static String getReaderName(IFormatReader reader)
    {
        return reader.getClass().getSimpleName();
    }

    static List<ImageID> listImageIDs(IFormatReader reader, IRandomAccess handle)
    {
        String handleId = generateHandleId(reader, null);
        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
            List<ImageID> ids = new ArrayList<ImageID>();
            int seriesCount = reader.getSeriesCount();
            for (int s = 0; s < seriesCount; s++)
            {
                reader.setSeries(s);
                int effectiveSizeC = reader.getEffectiveSizeC();
                int sizeT = reader.getSizeT();
                int sizeZ = reader.getSizeZ();
                for (int t = 0; t < sizeT; t++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        for (int c = 0; c < effectiveSizeC; c++)
                        {
                            ids.add(new ImageID(s, t, z, c));
                        }
                    }
                }
            }
            close(reader);
            return ids;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    /**
     * Returns the image <var>page</var> of the image file represented by <var>handle</var> as
     * {@link BufferedImage}.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     */
    static BufferedImage readImage(IFormatReader reader, IRandomAccess handle, ImageID imageID)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        String handleId = generateHandleId(reader, imageID);

        // Add to static map.
        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
            reader.setSeries(imageID.getSeriesIndex());
            final BufferedImageReader biReader =
                    BufferedImageReader.makeBufferedImageReader(reader);
            int index = calculateImageIndex(reader, imageID);
            final BufferedImage image = biReader.openImage(index);
            close(reader);
            return image;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    private static void close(IFormatReader reader) throws IOException
    {
        reader.close();
    }

    private static int calculateImageIndex(IFormatReader reader, ImageID imageID)
    {
        return reader.getIndex(imageID.getFocalPlaneIndex(), imageID.getColorChannelIndex(),
                imageID.getTimeSeriesIndex());
    }

    /**
     * Returns the metadata of the image file represented by <var>handle</var>.
     * 
     * @param imageID
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     */
    public static Map<String, Object> readMetadata(IFormatReader reader, IRandomAccess handle,
            ImageID imageID)
    {
        // Add to static map.
        String handleId = generateHandleId(reader, imageID);
        Location.mapFile(handleId, handle);

        try
        {
            HashMap<String, Object> result = new HashMap<String, Object>();
            MetadataOptions metaOptions = new DefaultMetadataOptions(MetadataLevel.ALL);
            reader.setMetadataOptions(metaOptions);

            reader.setId(handleId);
            reader.setSeries(imageID.getSeriesIndex());
            nullSafeAddAll(result, reader.getGlobalMetadata());
            nullSafeAddAll(result, reader.getSeriesMetadata());
            close(reader);

            return result;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    /**
     * An utility method that uses bio-formats reader to read an image and ImageJ to do a basic
     * intensity rescaling.
     * <p>
     * Returns the image <var>filename</var> represented by <var>handle</var> as
     * {@link BufferedImage}.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    static BufferedImage readImageWithIntensityRescaling(IFormatReader reader,
            IRandomAccess handle, ImageID imageID, int channel) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        // Add to static map.
        String handleId = generateHandleId(reader, imageID);

        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
            reader.setSeries(imageID.getSeriesIndex());
            int width = reader.getSizeX();
            int height = reader.getSizeY();
            final ImageStack stack = new ImageStack(width, height);
            int imageIndex = calculateImageIndex(reader, imageID);
            final ImageProcessor ip =
                    BioFormatsImageProcessor.openProcessor(reader, imageIndex, channel);
            stack.addSlice("", ip);
            final ImagePlus imp = new ImagePlus(handleId, stack);

            final BufferedImage image = imp.getBufferedImage();
            close(reader);
            return image;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    static Dimension readImageDimensions(IFormatReader reader, IRandomAccess handle, ImageID imageID)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        // Add to static map.
        String handleId = generateHandleId(reader, imageID);
        Location.mapFile(handleId, handle);
        try
        {
            if (reader instanceof CellomicsReader)
            {
                return CellomicsReaderUtil.readSize(handleId);
            }

            // This does the actual parsing.
            reader.setId(handleId);
            reader.setSeries(imageID.getSeriesIndex());
            int width = reader.getSizeX();
            int height = reader.getSizeY();
            close(reader);
            return new Dimension(width, height);
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    public static class CellomicsReaderUtil
    {
        public static Dimension readSize(String id) throws FormatException, IOException
        {
            RandomAccessInputStream in = getDecompressedStream(id);

            in.order(true);
            in.skipBytes(4);

            int x = in.readInt();
            int y = in.readInt();

            in.close();

            return new Dimension(x, y);

        }

        private static RandomAccessInputStream getDecompressedStream(String filename)
                throws FormatException, IOException
        {
            RandomAccessInputStream s = new RandomAccessInputStream(filename);
            if (FormatHandler.checkSuffix(filename, "c01"))
            {

                s.seek(4);
                ZlibCodec codec = new ZlibCodec();
                byte[] file = codec.decompress(s, null);
                s.close();

                return new RandomAccessInputStream(file);
            }
            return s;
        }
    }

    static Integer readImageColorDepth(IFormatReader reader, IRandomAccess handle, ImageID imageID)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        // Add to static map.
        String handleId = generateHandleId(reader, imageID);
        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
            reader.setSeries(imageID.getSeriesIndex());
            int depth = reader.getBitsPerPixel();
            close(reader);
            return depth;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }

    public static String generateHandleId(IFormatHandler formatHandler, ImageID imageId)
    {
        String id = UUID.randomUUID().toString() + "." + formatHandler.getSuffixes()[0];
        if (imageId != null && imageId.getFileName() != null)
        {
            id = imageId.getFileName();
        }
        return id;

    }

    private static void nullSafeAddAll(HashMap<String, Object> accumulator,
            Hashtable<String, Object> toAdd)
    {
        if (toAdd != null)
        {
            accumulator.putAll(toAdd);
        }

    }

    private BioFormatsImageUtils()
    {
        // Not to be instantiated.
    }
}
