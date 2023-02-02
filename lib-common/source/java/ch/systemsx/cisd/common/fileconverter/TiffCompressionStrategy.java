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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * The {@link IFileConversionStrategy} for tiff compressions.
 * 
 * @author Bernd Rinn
 */
public class TiffCompressionStrategy implements IFileConversionStrategy
{
    final private IFileConversionMethod method;

    public enum TiffCompressionMethod
    {
        /**
         * Uses ImageMagick's <code>convert</code> tool.
         */
        IMAGEMAGICK_CONVERT,

        /**
         * Uses LibTiff's <code>tiffcp</code> tool.
         */
        LIBTIFF_TIFFCP
    }

    public TiffCompressionStrategy(TiffCompressionMethod methodId, String compressionType)
    {
        switch (methodId)
        {
            case IMAGEMAGICK_CONVERT:
                method = new ImageMagickTiffCompressionConverter(compressionType);
                break;
            case LIBTIFF_TIFFCP:
                method = new TiffCpTiffCompressionConverter(compressionType);
                break;
            default:
                throw new IllegalArgumentException("methodId: " + methodId.name());
        }
    }

    public TiffCompressionStrategy(TiffCompressionMethod methodId)
    {
        this(methodId, null);
    }

    /**
     * Chooses the best method.
     *
     * @throws EnvironmentFailureException If not method is available.
     */
    public TiffCompressionStrategy() throws EnvironmentFailureException
    {
        // tiffcp is considerably faster than convert
        final TiffCpTiffCompressionConverter tiffCpConverter = new TiffCpTiffCompressionConverter();
        if (tiffCpConverter.isAvailable())
        {
            method = tiffCpConverter;
        } else
        {
            final ImageMagickTiffCompressionConverter convertConverter =
                    new ImageMagickTiffCompressionConverter();
            if (convertConverter.isAvailable())
            {
                method = convertConverter;
            } else
            {
                throw new EnvironmentFailureException("No suitable compression method found");
            }
        }
    }

    @Override
    public File tryCheckConvert(File inFile)
    {
        final String ext = FilenameUtils.getExtension(inFile.getName()).toLowerCase();
        if ("tiff".equals(ext) || "tif".equals(ext))
        {
            return inFile;
        } else
        {
            return null;
        }
    }

    @Override
    public boolean deleteOriginalFile()
    {
        return false;
    }

    @Override
    public IFileConversionMethod getConverter()
    {
        return method;
    }

}
