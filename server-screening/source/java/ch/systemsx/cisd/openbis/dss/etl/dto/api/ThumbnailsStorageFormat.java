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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Configuration parameters which describe how thumbnails should be generated.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailsStorageFormat extends AbstractHashable implements Serializable
{

    private static final long serialVersionUID = 1L;

    public enum FileFormat
    {
        PNG
        {
            @Override
            public void writeImage(BufferedImage image, OutputStream stream)
            {
                ImageUtil.writeImageToPng(image, stream);
            }

            @Override
            public String getFileExtension()
            {
                return "png";
            }

            @Override
            public String getImageMagickParam()
            {
                return "png";
            }
        },
        JPEG
        {
            @Override
            public void writeImage(BufferedImage image, OutputStream stream)
            {
                ImageUtil.writeImageUsingImageIO(image, stream, "jpg");
            }

            @Override
            public String getFileExtension()
            {
                return "jpg";
            }

            @Override
            public String getImageMagickParam()
            {
                return "jpeg";
            }
        },
        JPEG_2000
        {
            @Override
            public void writeImage(BufferedImage image, OutputStream stream)
            {
                ImageUtil.writeImageUsingImageIO(image, stream, "jpeg 2000");
            }

            @Override
            public String getFileExtension()
            {
                return "jp2";
            }

            @Override
            public String getImageMagickParam()
            {
                return "jp2";
            }
        };

        private static final Map<String, FileFormat> formats;
        static
        {
            formats = new HashMap<String, ThumbnailsStorageFormat.FileFormat>();
            for (FileFormat format : FileFormat.values())
            {
                formats.put(format.name().toUpperCase(), format);
            }
            formats.put("JP2", JPEG_2000);
            formats.put("JPEG 2000", JPEG_2000);
            formats.put("JPG", JPEG);
        }

        public abstract void writeImage(BufferedImage image, OutputStream stream);

        public abstract String getFileExtension();

        public abstract String getImageMagickParam();

        public String getOpenBISFileType()
        {
            return getFileExtension().toUpperCase();
        }

        public static FileFormat getValue(String fileFormat)
        {
            return formats.get(fileFormat.toUpperCase());
        }
    }

    /** Maximum default width and height of a thumbnail */
    public static final int DEFAULT_THUMBNAIL_MAX_SIZE = 200;

    /** Maximum default width and height of a thumbnail */
    public static final boolean DEFAULT_COMPRESS_THUMBNAILS = false;

    // ---

    private int maxWidth = DEFAULT_THUMBNAIL_MAX_SIZE;

    private int maxHeight = DEFAULT_THUMBNAIL_MAX_SIZE;

    private boolean allowEnlarging = true;

    private Double zoomLevel = null;

    private boolean storeCompressed = DEFAULT_COMPRESS_THUMBNAILS;

    private double allowedMachineLoadDuringGeneration = 1;

    private boolean highQuality8Bit = false;

    private boolean generateWithImageMagic = false;

    private List<String> imageMagicParams = Collections.emptyList();

    private String thumbnailsFileName;

    private FileFormat fileFormat = FileFormat.PNG;

    private final Map<String, String> transformations = new HashMap<String, String>();

    /**
     * Creates empty object which instructs that the thumbnails should be generated with default settings. Use setters to change default behaviour
     * (you will probably not have to).
     */
    public ThumbnailsStorageFormat()
    {
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public boolean isStoreCompressed()
    {
        return storeCompressed;
    }

    public double getAllowedMachineLoadDuringGeneration()
    {
        return allowedMachineLoadDuringGeneration;
    }

    public boolean isHighQuality()
    {
        return highQuality8Bit;
    }

    public boolean isGenerateWithImageMagic()
    {
        return generateWithImageMagic;
    }

    public List<String> getImageMagicParams()
    {
        return imageMagicParams;
    }

    public boolean isAllowEnlarging()
    {
        return allowEnlarging;
    }

    // --- setters ---

    /** Sets the maximum width of a thumbnail. */
    public void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    /** Sets the maximum height of a thumbnail. */
    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    /** Sets if each thumbnail should be additionally compressed (lostless) before it is stored. */
    public void setStoreCompressed(boolean storeCompressed)
    {
        this.storeCompressed = storeCompressed;
    }

    /**
     * Specify if this image representation is allowed to be larger than the original image.
     */
    public void setAllowEnlarging(boolean allowEnlarging)
    {
        this.allowEnlarging = allowEnlarging;
    }

    /**
     * The number of threads which will be used during thumbnails generation will be equal to number of processor cores * machineLoad.
     */
    public void setAllowedMachineLoadDuringGeneration(double machineLoad)
    {
        this.allowedMachineLoadDuringGeneration = machineLoad;
    }

    /**
     * Set to true if you want your thumbnails to be of higher quality. In such a case thumbnails generation during dataset registration will take
     * longer. Recommended for overlay images.
     */
    public void setHighQuality(boolean highQuality)
    {
        this.highQuality8Bit = highQuality;
    }

    /**
     * if true ImageMagic 'convert' utility should be installed and will be used to generate thumbnails. <br>
     * Note: if images should be handled with a specific image library, it will be ignored for thumbnails generation if 'convert' is supposed to be
     * used. Make sure that 'convert' can deal with your images in such a case.
     */
    public void setGenerateWithImageMagic(boolean generateWithImageMagic)
    {
        this.generateWithImageMagic = generateWithImageMagic;
    }

    /**
     * Sets additional parameters which should be passed to ImageMagic 'convert' utility when it is used to generate thumbnails. Example: pass
     * "-contrast-stretch 2%" to discard 2% of brightest and darkest pixels in the thumbnails.
     */
    public void setImageMagicParams(List<String> imageMagicParams)
    {
        this.imageMagicParams = imageMagicParams;
    }

    public Double getZoomLevel()
    {
        return zoomLevel;
    }

    public void setZoomLevel(Double zoomLevel)
    {
        this.zoomLevel = zoomLevel;
    }

    public String getThumbnailsFileName()
    {
        return thumbnailsFileName == null ? Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME
                : thumbnailsFileName;
    }

    public void setThumbnailsFileName(String thumbnailsFileName)
    {
        this.thumbnailsFileName = thumbnailsFileName;
    }

    public void setFileFormat(String fileFormat)
    {
        FileFormat value = FileFormat.getValue(fileFormat);
        if (value == null)
        {
            throw new IllegalArgumentException("File format '" + fileFormat + "' is unknown");
        }
        this.fileFormat = value;
    }

    public FileFormat getFileFormat()
    {
        return fileFormat;
    }

    public void setTransformations(Map<String, String> transformations)
    {
        this.transformations.putAll(transformations);
    }

    public String getTransformationCode(String channelCode)
    {
        return transformations.get(channelCode.toUpperCase());
    }

    public Map<String, String> getTransformations()
    {
        return transformations;
    }
}
