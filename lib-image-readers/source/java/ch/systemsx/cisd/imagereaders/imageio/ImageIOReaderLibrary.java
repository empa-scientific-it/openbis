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

package ch.systemsx.cisd.imagereaders.imageio;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;

/**
 * @author Kaloyan Enimanev
 */
public class ImageIOReaderLibrary implements IImageReaderLibrary
{

    @Override
    public String getName()
    {
        return ImageReaderConstants.IMAGEIO_LIBRARY;
    }

    @Override
    public List<String> getReaderNames()
    {
        return removeDuplicates(ImageIO.getReaderFormatNames());
    }

    @Override
    public IImageReader tryGetReader(String readerName)
    {
        assert readerName != null : "Reader name cannot be null";

        Iterator<ImageReader> iterator =
                ImageIO.getImageReadersByFormatName(readerName.toUpperCase());
        return tryGetReader(iterator);
    }

    @Override
    public IImageReader tryGetReaderForFile(String fileName)
    {
        String fileSuffix = FilenameUtils.getExtension(fileName);
        Iterator<ImageReader> iterator = null;
        if (StringUtils.isBlank(fileSuffix) == false)
        {
            iterator = ImageIO.getImageReadersBySuffix(fileSuffix);
        }
        return tryGetReader(iterator);
    }

    private IImageReader tryGetReader(Iterator<ImageReader> iterator)
    {
        if (iterator != null && iterator.hasNext())
        {
            ImageReader ioReader = iterator.next();
            return adaptImageIOReader(ioReader);
        } else
        {
            return null;
        }
    }

    private IImageReader adaptImageIOReader(final ImageReader ioReader)
    {
        final String libraryName = getName();
        final String readerName = getReaderName(ioReader);
        return new AbstractImageReader(libraryName, readerName)
            {
                @Override
                public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID,
                        IReadParams ignored) throws IOExceptionUnchecked
                {
                    try
                    {
                        ImageInputStream imageInput = adaptHandle(handle);
                        ioReader.setInput(imageInput);
                        return ioReader.read(0);
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }

                @Override
                public Dimension readDimensions(IRandomAccessFile handle, ImageID imageID)
                {
                    try
                    {
                        ImageInputStream imageInput = adaptHandle(handle);
                        ioReader.setInput(imageInput);
                        return new Dimension(ioReader.getWidth(0), ioReader.getHeight(0));
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }

                @Override
                public Integer readColorDepth(IRandomAccessFile handle, ImageID imageID)
                {
                    try
                    {
                        ImageInputStream imageInput = adaptHandle(handle);
                        ioReader.setInput(imageInput);
                        return ioReader.read(0).getColorModel().getPixelSize();
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }

                private ImageInputStream adaptHandle(IRandomAccessFile handle)
                {

                    InputStream input = new AdapterIInputStreamToInputStream(handle);
                    return new MemoryCacheImageInputStream(input);
                }
            };
    }

    private String getReaderName(ImageReader ioReader)
    {
        try
        {
            return ioReader.getFormatName();
        } catch (IOException ex)
        {
            return null;
        }
    }

    /**
     * Remove duplicates from readerFormatNames (e.g. "png" and "PNG" can be contained in the input
     * array).
     */
    private List<String> removeDuplicates(String[] readerFormatNames)
    {
        Set<String> names = new HashSet<String>();
        for (String name : readerFormatNames)
        {
            names.add(name.toLowerCase());
        }
        return new ArrayList<String>(names);
    }
}
