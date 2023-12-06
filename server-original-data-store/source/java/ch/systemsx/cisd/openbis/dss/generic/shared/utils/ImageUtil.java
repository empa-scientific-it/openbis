/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.PngFilterType;
import ar.com.hjg.pngj.PngWriter;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.image.ImageHistogram;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.common.image.IntensityRescaling.IImageToPixelsConverter;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Utility function on images.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    public static final String TIFF_FILE = "tif";

    public static final String PNG_FILE = "png";

    public static final String JPEG_FILE = "jpg";

    public static final String GIF_FILE = "gif";

    /**
     * When a grayscale image with color depth > 8 bits has to be displayed and user has not decided how it should be converted, then this threshold
     * will be used.
     */
    public static final float DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR = 0.01f;

    /** Maximal size of the initial image for scaling based on the maximum heap size setting. It is set to prevent out of memory errors. */
    private static final long MAX_IMAGE_SIZE = Runtime.getRuntime().maxMemory() * 3 / 10;

    private static final String WARNING_IMAGE_PATH = "image-too-large-warning.png";

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImageUtil.class);

    private static final Set<String> FILE_TYPES = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("gif", "jpg", "jpeg", "png", "tif", "tiff")));

    private static BufferedImage warningImage;

    static
    {
        try (final InputStream is = ImageUtil.class.getResourceAsStream(WARNING_IMAGE_PATH))
        {
            if (is == null)
            {
                throw new IOException(String.format("Template image %s not found.", WARNING_IMAGE_PATH));
            }

            warningImage = ImageIO.read(is);
        } catch (final IOException e)
        {
            operationLog.error(String.format("Template image %s cannot be loaded.", WARNING_IMAGE_PATH));
        }
    }

    private static interface ImageLoader
    {
        public BufferedImage load(IRandomAccessFile raf, ImageID imageID);

        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID);

        public int readColorDepth(IRandomAccessFile handle, ImageID imageID);
    }

    private static interface IReadingOperation<T>
    {
        T read(IImageReader imageReader, IRandomAccessFile handle, ImageID imageID, IReadParams params);

        T read(ImageLoader imageLoader, IRandomAccessFile handle, ImageID imageID);
    }

    private static final class MagicNumber
    {
        private final String fileType;

        private final String[] magicHexNumbers;

        private final int maxLength;

        MagicNumber(String fileType, String... magicHexNumbers)
        {
            this.fileType = fileType;
            this.magicHexNumbers = magicHexNumbers;
            int length = 0;
            for (String magicNumber : magicHexNumbers)
            {
                length = Math.max(length, magicNumber.length());
            }
            maxLength = length / 2;
        }

        public String getFileType()
        {
            return fileType;
        }

        int getMaxLength()
        {
            return maxLength;
        }

        public boolean matches(byte[] bytes)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < maxLength; i++)
            {
                byte b = bytes[i];
                builder.append(Integer.toHexString((b >> 4) & 0xf));
                builder.append(Integer.toHexString(b & 0xf));
            }
            String initialBytes = builder.toString().toLowerCase();
            for (String magicNumber : magicHexNumbers)
            {
                if (initialBytes.startsWith(magicNumber))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class MagicNumbersManager
    {
        private final MagicNumber[] magicNumbers;

        MagicNumbersManager(MagicNumber... magicNumbers)
        {
            this.magicNumbers = magicNumbers;
        }

        int getMaxLength()
        {
            int max = 0;
            for (MagicNumber magicNumber : magicNumbers)
            {
                max = Math.max(max, magicNumber.getMaxLength());
            }
            return max;
        }

        String tryToFigureOutFileTypeOf(byte[] initialBytes)
        {
            for (MagicNumber magicNumber : magicNumbers)
            {
                if (magicNumber.matches(initialBytes))
                {
                    return magicNumber.getFileType();
                }
            }
            return null;
        }
    }

    private static final MagicNumbersManager MAGIC_NUMBERS_MANAGER = new MagicNumbersManager(
            new MagicNumber(GIF_FILE, "474946383961", "474946383761"), new MagicNumber(JPEG_FILE,
                    "ffd8ff"), new MagicNumber(PNG_FILE, "89504e470d0a1a0a"), new MagicNumber(
                    TIFF_FILE, "49492a00", "4d4d002a"));

    private static final class TiffImageLoader implements ImageLoader
    {
        private final static int MAX_READ_AHEAD = 30000000;

        @Override
        public BufferedImage load(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadWithBioFormats(handle, imageID);
            } catch (RuntimeException ex1)
            {
                try
                {
                    return loadJavaAdvancedImagingTiff(handle, imageID);
                } catch (RuntimeException ex2)
                {
                    if (imageID.equals(ImageID.NULL))
                    {
                        handle.reset();
                        // There are some TIFF files which cannot be opened by JAI, try ImageJ
                        // instead...
                        return loadWithImageJ(handle);
                    } else
                    {
                        throw ex2;
                    }
                }
            }
        }

        @Override
        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadDimensionWithBioFormats(handle, imageID);
            } catch (RuntimeException ex1)
            {
                try
                {
                    return loadDimensionJavaAdvancedImagingTiff(handle, imageID);
                } catch (RuntimeException ex2)
                {
                    if (imageID.equals(ImageID.NULL))
                    {
                        handle.reset();
                        // There are some TIFF files which cannot be opened by JAI, try ImageJ
                        // instead...
                        return loadDimensionWithImageJ(handle);
                    } else
                    {
                        throw ex2;
                    }
                }
            }
        }

        @Override
        public int readColorDepth(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadColorDepthWithBioFormats(handle, imageID);
            } catch (RuntimeException ex1)
            {
                try
                {
                    return loadColorDepthJavaAdvancedImagingTiff(handle, imageID);
                } catch (RuntimeException ex2)
                {
                    if (imageID.equals(ImageID.NULL))
                    {
                        handle.reset();
                        // There are some TIFF files which cannot be opened by JAI, try ImageJ
                        // instead...
                        return loadColorDepthWithImageJ(handle);
                    } else
                    {
                        throw ex2;
                    }
                }
            }
        }
    }

    private static BufferedImage loadWithImageJ(IRandomAccessFile handle)
    {
        return loadWithLibrary(handle, ImageID.NULL, ImageReaderConstants.IMAGEJ_LIBRARY, "tiff");
    }

    private static Dimension loadDimensionWithImageJ(IRandomAccessFile handle)
    {
        return loadDimensionWithLibrary(handle, ImageID.NULL, ImageReaderConstants.IMAGEJ_LIBRARY,
                "tiff");
    }

    private static int loadColorDepthWithImageJ(IRandomAccessFile handle)
    {
        return loadColorDepthWithLibrary(handle, ImageID.NULL, ImageReaderConstants.IMAGEJ_LIBRARY,
                "tiff");
    }

    private static BufferedImage loadWithBioFormats(IRandomAccessFile handle, ImageID imageID)
    {
        return loadWithLibrary(handle, imageID, ImageReaderConstants.BIOFORMATS_LIBRARY,
                "TiffDelegateReader");
    }

    private static Dimension loadDimensionWithBioFormats(IRandomAccessFile handle, ImageID imageID)
    {
        return loadDimensionWithLibrary(handle, imageID, ImageReaderConstants.BIOFORMATS_LIBRARY,
                "TiffDelegateReader");
    }

    private static int loadColorDepthWithBioFormats(IRandomAccessFile handle, ImageID imageID)
    {
        return loadColorDepthWithLibrary(handle, imageID, ImageReaderConstants.BIOFORMATS_LIBRARY,
                "TiffDelegateReader");
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     */
    public static BufferedImage loadJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        return loadWithLibrary(handle, imageID, ImageReaderConstants.JAI_LIBRARY, "tiff");
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     */
    public static Dimension loadDimensionJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        return loadDimensionWithLibrary(handle, imageID, ImageReaderConstants.JAI_LIBRARY, "tiff");
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     */
    public static int loadColorDepthJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        return loadColorDepthWithLibrary(handle, imageID, ImageReaderConstants.JAI_LIBRARY, "tiff");
    }

    private static BufferedImage loadWithLibrary(IRandomAccessFile handle, ImageID imageIDOrNull,
            String libraryName, String readerName)
    {
        operationLog.debug("Load tiff image using " + libraryName);
        IImageReader imageReader = ImageReaderFactory.tryGetReader(libraryName, readerName);
        if (imageReader == null)
        {
            throw new IllegalStateException(String.format(
                    "There is no reader '%s' in image library '%s'.", readerName, libraryName));
        }
        try
        {
            return imageReader.readImage(handle, imageIDOrNull, null);
        } catch (Exception ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
        }
    }

    private static Dimension loadDimensionWithLibrary(IRandomAccessFile handle,
            ImageID imageIDOrNull, String libraryName, String readerName)
    {
        operationLog.debug("Load tiff image using " + libraryName);
        IImageReader imageReader = ImageReaderFactory.tryGetReader(libraryName, readerName);
        if (imageReader == null)
        {
            throw new IllegalStateException(String.format(
                    "There is no reader '%s' in image library '%s'.", readerName, libraryName));
        }
        try
        {
            return imageReader.readDimensions(handle, imageIDOrNull);
        } catch (Exception ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
        }
    }

    private static int loadColorDepthWithLibrary(IRandomAccessFile handle, ImageID imageIDOrNull,
            String libraryName, String readerName)
    {
        operationLog.debug("Load tiff image using " + libraryName);
        IImageReader imageReader = ImageReaderFactory.tryGetReader(libraryName, readerName);
        if (imageReader == null)
        {
            throw new IllegalStateException(String.format(
                    "There is no reader '%s' in image library '%s'.", readerName, libraryName));
        }
        try
        {
            return imageReader.readColorDepth(handle, imageIDOrNull);
        } catch (Exception ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
        }
    }

    private static final class JavaImageLoader implements ImageLoader
    {
        private final String fileType;

        JavaImageLoader(String fileType)
        {
            this.fileType = fileType;
        }

        @Override
        public BufferedImage load(IRandomAccessFile handle, ImageID imageID)
        {
            if (imageID.equals(ImageID.NULL))
            {
                IImageReader imageReader =
                        ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEIO_LIBRARY,
                                fileType);
                if (imageReader == null)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Cannot find ImageIO reader for file type '%s'", fileType);
                }
                return imageReader.readImage(handle, ImageID.NULL, null);
            } else
            {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID)
        {
            if (imageID.equals(ImageID.NULL))
            {
                IImageReader imageReader =
                        ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEIO_LIBRARY,
                                fileType);
                if (imageReader == null)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Cannot find ImageIO reader for file type '%s'", fileType);
                }
                return imageReader.readDimensions(handle, imageID);
            } else
            {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public int readColorDepth(IRandomAccessFile handle, ImageID imageID)
        {
            if (imageID.equals(ImageID.NULL))
            {
                IImageReader imageReader =
                        ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEIO_LIBRARY,
                                fileType);
                if (imageReader == null)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Cannot find ImageIO reader for file type '%s'", fileType);
                }
                return imageReader.readColorDepth(handle, imageID);
            } else
            {
                throw new UnsupportedOperationException();
            }
        }

    }

    private static class ReaderAndFileHandler
    {
        private IImageReader imageReader;

        private IRandomAccessFile handle;

        private String filePath;

        private String imageLibraryName;

        private String imageLibraryReaderName;

        public ReaderAndFileHandler(String imageLibraryName, String imageLibraryReaderName)
        {
            this.imageLibraryName = imageLibraryName;
            this.imageLibraryReaderName = imageLibraryReaderName;
        }

        void setFileHandler(IHierarchicalContentNode contentNode)
        {
            String newFilePath = contentNode.getRelativePath();
            if (newFilePath.equals(filePath))
            {
                return;
            }
            filePath = newFilePath;
            if (handle != null)
            {
                closeQuietly(handle);
            }
            handle = contentNode.getFileContent();
        }

        void close()
        {
            closeQuietly(handle);
            imageReader.close();
        }

        void setImageReader(IImageReader imageReader)
        {
            this.imageReader = imageReader;
        }
    }

    private static final Map<String, ImageLoader> imageLoaders = new HashMap<String, ImageLoader>();

    static
    {
        imageLoaders.put(GIF_FILE, new JavaImageLoader(GIF_FILE));
        imageLoaders.put(JPEG_FILE, new JavaImageLoader(JPEG_FILE));
        imageLoaders.put(PNG_FILE, new JavaImageLoader(PNG_FILE));
        imageLoaders.put(TIFF_FILE, new TiffImageLoader());
    }

    /**
     * Returns <code>true</code> if the specified file is a supported image file. Supported formats are GIF, JPG, PNG, TIFF. Only file type is taken
     * into account for figuring out the image format. Following file types are recognized: <code>.gif, .jpg, .jpeg, .png, .tif, .tiff</code>
     */
    public static boolean isImageFile(File file)
    {
        String fileName = file.getName();
        String fileType = FilenameUtils.getExtension(fileName);
        return fileType != null && FILE_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * Loads the image specified by <var>imageIdOrNull</var> from the given </var>inputStream</var>. Supported images formats are GIF, JPG, PNG, and
     * TIFF. The input stream will be closed after loading.
     * <p>
     * Note that the original color depth will be kept, so e.g. 12 or 16 bit grayscale images will not be converted to RGB.
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number identifying supported image format.
     */
    public static BufferedImage loadUnchangedImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, String imageLibraryNameOrNull,
            String imageLibraryReaderNameOrNull, IReadParams params)
    {
        IReadingOperation<BufferedImage> operation = new IReadingOperation<BufferedImage>()
            {

                @Override
                public BufferedImage read(IImageReader imageReader, IRandomAccessFile handle, ImageID imageID, IReadParams p)
                {
                    return imageReader.readImage(handle, imageID, p);
                }

                @Override
                public BufferedImage read(ImageLoader imageLoader, IRandomAccessFile handle, ImageID imageID)
                {
                    return imageLoader.load(handle, imageID);
                }

            };
        return loadUnchangedData(contentNode, imageIdOrNull, imageLibraryNameOrNull, imageLibraryReaderNameOrNull, params, operation);
    }

    private static ThreadLocal<String> sessionIdThreadLocal = new ThreadLocal<>();

    /**
     * Set unique session id. This thread will reuse image readers between invocation with the same session id.
     */
    public static void setThreadLocalSessionId(String sessionId)
    {
        sessionIdThreadLocal.set(sessionId);
    }

    /**
     * Gets the unique session id.
     */
    public static String getThreadLocalSessionId()
    {
        return sessionIdThreadLocal.get();
    }

    /**
     * Closes all readers created in scope of a single session.
     */
    public static void closeSession(String sessionId)
    {
        globalReaderPool.releaseSession(sessionId);
    }

    private static ImageUtilReaderPool<ReaderAndFileHandler> globalReaderPool = new ImageUtilReaderPool<ReaderAndFileHandler>(
            new ImageUtilReaderPool.ReaderUtil<ReaderAndFileHandler>()
                {

                    @Override
                    public ReaderAndFileHandler create(String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull)
                    {
                        IImageReader imageReader = ImageReaderFactory.tryGetReader(imageLibraryNameOrNull,
                                imageLibraryReaderNameOrNull);
                        if (imageReader == null)
                        {
                            return null;
                        }
                        ReaderAndFileHandler reader = new ReaderAndFileHandler(imageLibraryNameOrNull,
                                imageLibraryReaderNameOrNull);
                        reader.setImageReader(imageReader);
                        return reader;
                    }

                    @Override
                    public boolean isSameLibraryAndReader(ReaderAndFileHandler reader, String imageLibraryNameOrNull,
                            String imageLibraryReaderNameOrNull)
                    {
                        return reader.imageLibraryName.equals(imageLibraryNameOrNull)
                                && reader.imageLibraryReaderName.equals(imageLibraryReaderNameOrNull);
                    }

                    @Override
                    public void close(ReaderAndFileHandler reader)
                    {
                        reader.close();
                    }
                });

    private static <T> T loadUnchangedData(IHierarchicalContentNode contentNode, String imageIdOrNull, String imageLibraryNameOrNull,
            String imageLibraryReaderNameOrNull, IReadParams params, IReadingOperation<T> operation)
    {
        assert (imageLibraryReaderNameOrNull == null || imageLibraryNameOrNull != null) : "if image reader "
                + "is specified then library name should be specified as well";
        ImageID imageID = parseImageID(imageIdOrNull, contentNode);

        String sessionId = sessionIdThreadLocal.get();

        ReaderAndFileHandler reader = globalReaderPool.get(sessionId, imageLibraryNameOrNull, imageLibraryReaderNameOrNull);

        if (reader == null)
        {
            return loadUnchangedDataGuessingLibrary(contentNode, operation, imageID);
        }

        try
        {
            // The actual operation execution
            reader.setFileHandler(contentNode);
            T result = operation.read(reader.imageReader, reader.handle, imageID, params);
            return result;
        } finally
        {
            // persist the user in a thread local variable if there is specified process ID
            if (sessionId != null)
            {
                globalReaderPool.put(sessionId, reader);
            }
            else
            {
                reader.close();
            }
        }
    }

    /**
     * Loads the data specified by <var>imageID</var> from the image from the given </var>handle</var>. Supported images formats are GIF, JPG, PNG,
     * and TIFF. The input stream will be closed after loading.
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number identifying supported image format.
     */
    private static <T> T loadUnchangedDataGuessingLibrary(IHierarchicalContentNode contentNode, IReadingOperation<T> operation, ImageID imageID)
    {
        IRandomAccessFile handle = contentNode.getFileContent();
        String fileType = tryToFigureOutFileTypeOf(handle);

        try
        {
            if (fileType == null)
            {
                throw new IllegalArgumentException(
                        "File type of an image input stream couldn't be determined.");
            }
            ImageLoader imageLoader = imageLoaders.get(fileType);
            if (imageLoader == null)
            {
                throw new IllegalArgumentException("Unable to load image of file type '" + fileType
                        + "'.");
            }
            return operation.read(imageLoader, handle, imageID);
        } finally
        {
            closeQuietly(handle);
        }
    }

    /**
     * Loads the size of image specified by <var>imageIdOrNull</var> from the given </var>inputStream</var>. Supported images formats are GIF, JPG,
     * PNG, and TIFF. The input stream will be closed after loading.
     * <p>
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number identifying supported image format.
     */
    public static Dimension loadUnchangedImageDimension(IHierarchicalContentNode contentNode,
            String imageIdOrNull, String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull)
    {
        IReadingOperation<Dimension> operation = new IReadingOperation<Dimension>()
            {
                @Override
                public Dimension read(IImageReader imageReader, IRandomAccessFile handle, ImageID imageID, IReadParams params)
                {
                    return imageReader.readDimensions(handle, imageID);
                }

                @Override
                public Dimension read(ImageLoader imageLoader, IRandomAccessFile handle, ImageID imageID)
                {
                    return imageLoader.readDimension(handle, imageID);
                }

            };
        return loadUnchangedData(contentNode, imageIdOrNull, imageLibraryNameOrNull, imageLibraryReaderNameOrNull, null, operation);
    }

    /**
     * Loads the color depth of image specified by <var>imageIdOrNull</var> from the given </var>inputStream</var>. Supported images formats are GIF,
     * JPG, PNG, and TIFF. The input stream will be closed after loading.
     * <p>
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number identifying supported image format.
     */
    public static int loadUnchangedImageColorDepth(IHierarchicalContentNode contentNode,
            String imageIdOrNull, String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull)
    {
        IReadingOperation<Integer> operation = new IReadingOperation<Integer>()
            {
                @Override
                public Integer read(IImageReader imageReader, IRandomAccessFile handle, ImageID imageID, IReadParams params)
                {
                    return imageReader.readColorDepth(handle, imageID);
                }

                @Override
                public Integer read(ImageLoader imageLoader, IRandomAccessFile handle, ImageID imageID)
                {
                    return imageLoader.readColorDepth(handle, imageID);
                }

            };
        return loadUnchangedData(contentNode, imageIdOrNull, imageLibraryNameOrNull, imageLibraryReaderNameOrNull, null, operation);
    }

    /**
     * Converts the given <var>image</var> to a PNG image. Uses fast parameters for the filter and deflate level (no filter and no deflation).
     * <p>
     * <b>This method is about 7 times faster than {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should be
     * preferred whenever speed is important.</b>
     * 
     * @param image The image to convert to the PNG <code>byte[]</code>.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPngFast(BufferedImage image)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPngFast(image, out);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream. Uses fast parameters for the filter and deflate
     * level (no filter and no deflation).
     * <p>
     * <b>This method is about 7 times faster than {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should be
     * preferred whenever speed is important.</b>
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to.
     */
    public static void writeImageToPngFast(BufferedImage image, OutputStream out)
    {
        writeImageToPng(image, out, PngFilterType.FILTER_DEFAULT, 0);
    }

    /**
     * Converts the given <var>image</var> to a PNG image. Uses default parameters for the filter and deflate level.
     * <p>
     * <b>This method is about 7 times faster than {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should be
     * preferred whenever speed is important.</b>
     * 
     * @param image The image to convert to the PNG <code>byte[]</code>.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPng(BufferedImage image)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPng(image, out);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream. Uses default parameters for the filter and deflate
     * level.
     * <p>
     * <b>This method is about 3 times faster than {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should be
     * preferred whenever speed is important.</b>
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to.
     */
    public static void writeImageToPng(BufferedImage image, OutputStream out)
    {
        writeImageToPng(image, out, PngFilterType.FILTER_DEFAULT, 6);
    }

    /**
     * Converts the given <var>image</var> to a PNG image.
     * <p>
     * 
     * @param image The image to write to the output stream.
     * @param filterType The type of the filter (see <a href="http://www.w3.org/TR/PNG-Filters.html">PNG filters</a>) to apply when converting to PNG,
     *            <code>null</code> means {@link PngFilterType#FILTER_DEFAULT}.
     * @param compressionLevel the compression level for the deflation filter of the PNG conversion, from -1 to 9. 0 means no compression, 9 means
     *            maximal compression, -1 means 6 which is the default deflation level.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPng(BufferedImage image, PngFilterType filterType,
            int compressionLevel)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPng(image, out, filterType, compressionLevel);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream.
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to
     * @param filterType The type of the filter (see <a href="http://www.w3.org/TR/PNG-Filters.html">PNG filters</a>) to apply when converting to PNG,
     *            <code>null</code> means {@link PngFilterType#FILTER_DEFAULT}.
     * @param compressionLevel the compression level for the deflation filter of the PNG conversion, from -1 to 9. 0 means no compression, 9 means
     *            maximal compression, -1 means 6 which is the default deflation level.
     */
    public static void writeImageToPng(BufferedImage image, OutputStream out,
            PngFilterType filterType, int compressionLevel)
    {
        PngWritingHelper helper = PngWritingHelper.createHelper(image);
        ImageInfo imgInfo = helper.getImageInfo();
        int rows = imgInfo.rows;
        PngWriter png = new PngWriter(out, imgInfo);
        png.setFilterType(filterType == null ? PngFilterType.FILTER_DEFAULT : filterType);
        png.setCompLevel(compressionLevel == -1 ? 6 : compressionLevel);
        ImageLine imageLine = new ImageLine(imgInfo);
        for (int row = 0; row < rows; ++row)
        {
            helper.fillLine(imageLine, row);
            imageLine.setRown(row);
            png.writeRow(imageLine);
        }
        png.end();
    }

    public static void writeImageUsingImageIO(BufferedImage image, OutputStream out, String format)
            throws IOExceptionUnchecked
    {
        try
        {
            ImageIO.write(image, format, out);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    /**
     * Parses specified string representation of an {@link ImageID}. If the argument is <code>null</code> {@link ImageID#NULL} will be returned.
     */
    public static ImageID parseImageID(String imageIdOrNull, IHierarchicalContentNode contentNode)
    {
        ImageID id = imageIdOrNull == null ? ImageID.NULL : ImageID.parse(imageIdOrNull);
        try
        {
            final File fileOrNull = contentNode.tryGetFile();
            if (fileOrNull != null)
            {
                id.setFileName(fileOrNull.getCanonicalPath());
            }
        } catch (Exception ex)
        {
            operationLog.warn("Unable to set file name on image id. ", ex);
        }
        return id;
    }

    /**
     * Only for tests
     */
    @Private
    static BufferedImage loadImage(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        return loadImage(new FileBasedContentNode(file));
    }

    /**
     * Only for tests.
     */
    @Private
    static BufferedImage loadImage(IHierarchicalContentNode contentNode)
    {
        return loadUnchangedImage(contentNode, null, null, null, null);
    }

    /**
     * Loads an image from specified file node. Supported file formats are GIF, JPG, PNG, and TIFF.
     * 
     * @throws IllegalArgumentException if the file isn't a valid image file.
     */
    public static BufferedImage loadImageForDisplay(IHierarchicalContentNode contentNode,
            IImageToPixelsConverter converterOrNull)
    {
        if (contentNode.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: "
                    + contentNode.getRelativePath());
        }
        BufferedImage result = loadImage(contentNode);
        result = convertForDisplayIfNecessary(result, null, converterOrNull);
        return result;
    }

    /**
     * Re-scales the image to be the biggest one which fits into a (0,0,maxWidth, maxHeight) rectangle. Preserves the aspect ratio. If the rectangle
     * is bigger than the image does nothing.
     * <p>
     * If the specified image uses grayscale with color depth larger then 8 bits, conversion to 8 bits grayscale is done.
     * </p>
     * 
     * @param maxWidth Maximum width of the result image.
     * @param maxHeight Maximum height of the result image.
     */
    public static BufferedImage createThumbnailForDisplay(BufferedImage image, int maxWidth,
            int maxHeight, IImageToPixelsConverter converterOrNull)
    {
        BufferedImage result = rescale(image, maxWidth, maxHeight, true, false, converterOrNull);
        result = convertForDisplayIfNecessary(result, null, converterOrNull);
        return result;
    }

    /**
     * If the specified image uses grayscale with color depth larger then 8 bits, conversion to 8 bits grayscale is done. Otherwise the original image
     * is returned.
     * <p>
     * Conversion is done by intensity rescaling (with outlier cutoff specified by <code>threshold</code>) because the actual resolution is often not
     * know because getMaxNumberOfBitsPerComponent() might return 16 even tough the resolution is only 12.
     */
    private static BufferedImage convertForDisplayIfNecessary(BufferedImage image, Float threshold,
            IImageToPixelsConverter converterOrNull)
    {
        return convertForDisplayIfNecessary(image, threshold, null, converterOrNull);
    }

    private static BufferedImage convertForDisplayIfNecessary(BufferedImage image, Float threshold,
            IntensityRescaling.Channel representativeChannelOrNull, IImageToPixelsConverter converterOrNull)
    {
        Channel channel = getRepresentativeChannelIfEffectiveGrayAndMoreThan8Bit(image, representativeChannelOrNull);
        if (channel != null)
        {
            Pixels pixels = converterOrNull == null ? new Pixels(image) : converterOrNull.convert(image);
            Levels intensityRange = IntensityRescaling.computeLevels(pixels,
                    threshold == null ? DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR : threshold, channel);
            convertToGray(pixels);
            BufferedImage result =
                    IntensityRescaling.rescaleIntensityLevelTo8Bits(pixels, intensityRange, Channel.values());
            return result;
        }
        return image;
    }

    private static void convertToGray(Pixels pixels)
    {
        int[][] pixelData = pixels.getPixelData();
        if (pixelData.length > 1)
        {
            for (int i = 0, n = pixelData[0].length; i < n; i++)
            {
                int max = 0;
                for (int c = 0; c < pixelData.length; c++)
                {
                    max = Math.max(max, pixelData[c][i]);
                }
                for (int c = 0; c < pixelData.length; c++)
                {
                    pixelData[c][i] = max;
                }
            }
        }
    }

    private static Channel getRepresentativeChannelIfEffectiveGrayAndMoreThan8Bit(BufferedImage image,
            IntensityRescaling.Channel representativeChannelOrNull)
    {
        if (getMaxNumberOfBitsPerComponent(image) <= 8)
        {
            return null;
        }
        ColorModel colorModel = image.getColorModel();
        if (image.getType() != BufferedImage.TYPE_CUSTOM)
        {
            return colorModel.getNumColorComponents() == 1 ? Channel.RED : null;
        }
        return representativeChannelOrNull != null ? representativeChannelOrNull : getRepresentativeChannelIfEffectiveGray(image);
    }

    /**
     * Returns the maximum bit resolution of the specified image. It returns the maximum of the array returned by
     * {@link ColorModel#getComponentSize()}. If not defined (which should be only the case for Index16ColorModel of the BioFormats library)
     * {@link ColorModel#getPixelSize()} is returned.
     */
    public static int getMaxNumberOfBitsPerComponent(BufferedImage image)
    {
        ColorModel colorModel = image.getColorModel();
        int[] componentSize = colorModel.getComponentSize();
        if (componentSize == null)
        {
            return colorModel.getPixelSize();
        }
        int max = 0;
        for (int size : componentSize)
        {
            max = Math.max(max, size);
        }
        return max;
    }

    /**
     * Checks whether the specified image is actually a (colored) gray image.
     * 
     * @return the representative color channel which can be used to extract the gray image. <code>null</code> if the image isn't a gray image.
     */
    public static Channel getRepresentativeChannelIfEffectiveGray(BufferedImage image)
    {
        ImageHistogram imageHistogram = ImageHistogram.calculateHistogram(image);
        Map<Channel, int[]> histogramsByChannels = new LinkedHashMap<Channel, int[]>();
        int numberOfPixels = image.getWidth() * image.getHeight();
        checkIfChannelIsUsed(histogramsByChannels, numberOfPixels, Channel.RED, imageHistogram.getRedHistogram());
        checkIfChannelIsUsed(histogramsByChannels, numberOfPixels, Channel.GREEN, imageHistogram.getGreenHistogram());
        checkIfChannelIsUsed(histogramsByChannels, numberOfPixels, Channel.BLUE, imageHistogram.getBlueHistogram());
        if (histogramsByChannels.isEmpty())
        {
            return Channel.RED; // Black image is a gray image, doesn't matter which channel to return.
        }
        List<Entry<Channel, int[]>> usedChannels = new ArrayList<Map.Entry<Channel, int[]>>(histogramsByChannels.entrySet());
        int[] representativeHistogram = usedChannels.get(0).getValue();
        for (int i = 1; i < usedChannels.size(); i++)
        {
            int[] histogram = usedChannels.get(i).getValue();
            for (int j = 0; j < histogram.length; j++)
            {
                if (histogram[j] != representativeHistogram[j])
                {
                    return null;
                }
            }
        }
        return usedChannels.get(0).getKey();
    }

    private static void checkIfChannelIsUsed(Map<Channel, int[]> usedChannels, int numberOfPixels, Channel channel, int[] histogram)
    {
        if (histogram[0] < numberOfPixels)
        {
            usedChannels.put(channel, histogram);
        }
    }

    /**
     * Re-scales the image to be the biggest one which fits into a (0,0,maxWidth, maxHeight) rectangle. Preserves the aspect ratio. If the rectangle
     * is bigger than the image and 'enlargeIfNecessary' is false then nothing is done.
     * 
     * @param maxWidth Maximum width of the result image.
     * @param maxHeight Maximum height of the result image.
     * @param enlargeIfNecessary if false and the image has smaller width and height than the specified limit, then the image is not changed.
     * @param highQuality8Bit if true thumbnails will be of higher quality, but rescaling will take longer and the image will be converted to 8 bit.
     * @param converterOrNull
     */
    public static BufferedImage rescale(BufferedImage image, int maxWidth, int maxHeight,
            boolean enlargeIfNecessary, boolean highQuality8Bit, IImageToPixelsConverter converterOrNull)
    {
        return rescale(image, maxWidth, maxHeight, enlargeIfNecessary, highQuality8Bit, null, converterOrNull);
    }

    public static BufferedImage rescale(BufferedImage image, int maxWidth, int maxHeight,
            boolean enlargeIfNecessary, boolean highQuality8Bit,
            IntensityRescaling.Channel representativeChannelOrNull, IImageToPixelsConverter converterOrNull)
    {
        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final int bytesPerPixel = image.getColorModel().getPixelSize() >> 3;
        final long imageSize = (long) imageWidth * imageHeight * bytesPerPixel;
        if (imageSize <= MAX_IMAGE_SIZE)
        {
            int width = image.getWidth();
            int height = image.getHeight();
            // the image has already the required size
            if ((width == maxWidth && maxHeight >= height)
                    || (height == maxHeight && maxWidth >= width))
            {
                return image;
            }
            double widthScale = maxWidth / (double) width;
            double heightScale = maxHeight / (double) height;
            double scale = Math.min(widthScale, heightScale);
            // image is smaller than required
            if (enlargeIfNecessary == false && scale > 1)
            {
                return image;
            }
            int thumbnailWidth = (int) (scale * width + 0.5);
            int thumbnailHeight = (int) (scale * height + 0.5);

            BufferedImage thumbnail =
                    createNewEmptyImage(image, highQuality8Bit, thumbnailWidth, thumbnailHeight);

            Graphics2D graphics2D = thumbnail.createGraphics();
            BufferedImage imageToRescale = image;
            if (highQuality8Bit)
            {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                // WORKAROUND: non-default interpolations do not work well with 16 bit grayscale images.
                // We have to rescale colors to 8 bit here, otherwise the result will contain only few
                // colors.
                imageToRescale = convertForDisplayIfNecessary(imageToRescale, 0f, representativeChannelOrNull, converterOrNull);
            }
            graphics2D.drawImage(imageToRescale, 0, 0, thumbnailWidth, thumbnailHeight, null);
            graphics2D.dispose();
            return thumbnail;
        } else
        {
            operationLog.warn(String.format("Original image is too large: %d bytes.", imageSize));
            return warningImage;
        }
    }

    public static BufferedImage convertToRGB(BufferedImage image)
    {
        if (image.getType() == BufferedImage.TYPE_INT_RGB
                || image.getType() == BufferedImage.TYPE_INT_ARGB)
        {
            return image;
        }

        BufferedImage thumbnail =
                createNewEmptyImage(image, false, image.getWidth(), image.getHeight());
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        graphics2D.dispose();
        return thumbnail;
    }

    private static BufferedImage createNewEmptyImage(BufferedImage image, boolean highQuality8Bit,
            int thumbnailWidth, int thumbnailHeight)
    {
        boolean isTransparent = image.getColorModel().hasAlpha();
        int imageType = image.getType();
        if (highQuality8Bit)
        {
            imageType =
                    imageType == BufferedImage.TYPE_USHORT_GRAY ? BufferedImage.TYPE_BYTE_GRAY
                            : BufferedImage.TYPE_INT_RGB;
        } else if (imageType == BufferedImage.TYPE_BYTE_INDEXED)
        {
            imageType = BufferedImage.TYPE_INT_RGB;
        } else
        {
            imageType = isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, imageType);
        return thumbnail;
    }

    private static void closeQuietly(IRandomAccessFile handle)
    {
        try
        {
            handle.close();
        } catch (Exception ex)
        {
            // keep quiet
        }
    }

    /**
     * Tries to figure out the file type of the specified binary content. It uses the first few bytes as a finger print (so-called 'magic numbers') as
     * a heuristic to get the type of content. Currently only the following types are recognized: <code>gif, jpg, png, tif</code>.
     * 
     * @param handle {@link IRandomAccessFile} which supports marking.
     * @return <code>null</code> if file type couldn't be figured out.
     */
    public static String tryToFigureOutFileTypeOf(IRandomAccessFile handle)
    {
        if (handle.markSupported() == false)
        {
            throw new IllegalArgumentException("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.");
        }
        int maxLength = MAGIC_NUMBERS_MANAGER.getMaxLength();
        handle.mark(maxLength);
        byte[] initialBytes = new byte[maxLength];
        handle.read(initialBytes);
        handle.reset();
        return MAGIC_NUMBERS_MANAGER.tryToFigureOutFileTypeOf(initialBytes);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a tiff file.
     */
    public static boolean isTiff(String fileTypeOrNull)
    {
        return TIFF_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a jpeg file.
     */
    public static boolean isJpeg(String fileTypeOrNull)
    {
        return JPEG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a png file.
     */
    public static boolean isPng(String fileTypeOrNull)
    {
        return PNG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a gif file.
     */
    public static boolean isGif(String fileTypeOrNull)
    {
        return GIF_FILE.equals(fileTypeOrNull);
    }

}
