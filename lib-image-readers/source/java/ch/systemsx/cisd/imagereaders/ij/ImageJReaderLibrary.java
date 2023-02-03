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

package ch.systemsx.cisd.imagereaders.ij;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;

/**
 * Implementation of {@link IImageReader} using ImageJ under the hood.
 * <p>
 * Currently, only the only supported image format for this library is single-page TIFF. In the
 * future we can add support for other image types.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageJReaderLibrary implements IImageReaderLibrary
{

    private static final String TIFF_READER = "tiff";

    private static final List<String> TIFF_FILE_EXTS = Arrays.asList("tiff", "tif");

    private final IImageReader TIFF_IMAGE_READER = new AbstractImageReader(getName(), TIFF_READER)
        {
            @Override
            public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID,
                    IReadParams params) throws IOExceptionUnchecked
            {
                AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(handle);
                ImagePlus imagePlus = new Opener().openTiff(is, "");
                if (imagePlus == null)
                {
                    throw new IllegalStateException("Cannot open the image file with ImageJ.");
                }
                return createBufferedImageOfSameType(imagePlus);
            }

            @Override
            public Dimension readDimensions(IRandomAccessFile handle, ImageID imageID)
            {
                AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(handle);
                ImagePlus imagePlus = new Opener().openTiff(is, "");
                if (imagePlus == null)
                {
                    throw new IllegalStateException("Cannot open the image file with ImageJ.");
                }
                return new Dimension(imagePlus.getWidth(), imagePlus.getHeight());
            }

            @Override
            public Integer readColorDepth(IRandomAccessFile handle, ImageID imageID)
            {
                AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(handle);
                ImagePlus imagePlus = new Opener().openTiff(is, "");
                if (imagePlus == null)
                {
                    throw new IllegalStateException("Cannot open the image file with ImageJ.");
                }
                return imagePlus.getBytesPerPixel();
            }
        };

    private BufferedImage createBufferedImageOfSameType(ImagePlus imagePlus)
    {
        ImageProcessor processor = imagePlus.getProcessor();
        if (processor instanceof ShortProcessor)
        {
            BufferedImage bufferedImage = ((ShortProcessor) processor).get16BitBufferedImage();
            return bufferedImage;
        }
        int bufferedImageType = findBufferedImageType(imagePlus);
        BufferedImage bufferedImage =
                new BufferedImage(imagePlus.getWidth(), imagePlus.getHeight(), bufferedImageType);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.drawImage(imagePlus.getImage(), 0, 0, null);
        return bufferedImage;
    }

    private static int findBufferedImageType(ImagePlus imagePlus)
    {
        switch (imagePlus.getType())
        {
            case ImagePlus.GRAY8:
                return BufferedImage.TYPE_BYTE_GRAY;
            case ImagePlus.GRAY16:
            case ImagePlus.GRAY32:
                return BufferedImage.TYPE_USHORT_GRAY;
            case ImagePlus.COLOR_256:
            case ImagePlus.COLOR_RGB:
                return BufferedImage.TYPE_INT_RGB;
            default:
                return BufferedImage.TYPE_INT_RGB;
        }
    }

    @Override
    public String getName()
    {
        return ImageReaderConstants.IMAGEJ_LIBRARY;
    }

    @Override
    public List<String> getReaderNames()
    {
        return Collections.singletonList(TIFF_READER);
    }

    @Override
    public IImageReader tryGetReader(String readerName)
    {
        return tryGetReaderForExtension(readerName);
    }

    @Override
    public IImageReader tryGetReaderForFile(String fileName)
    {
        String fileExt = FilenameUtils.getExtension(fileName);
        return tryGetReaderForExtension(fileExt);
    }

    private IImageReader tryGetReaderForExtension(String fileExtension)
    {
        if (fileExtension != null)
        {
            String lowerCaseExt = fileExtension.toLowerCase();
            if (TIFF_FILE_EXTS.contains(lowerCaseExt))
            {
                return TIFF_IMAGE_READER;
            }
        }
        return null;
    }

}
