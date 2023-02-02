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
 * An {@link IFileConversionStrategy} for tiff-to-png conversions.
 * 
 * @author Bernd Rinn
 */
public class Tiff2PngConversionStrategy implements IFileConversionStrategy
{
    private final boolean deleteOriginalFile;

    private final IFileConversionMethod method;

    /**
     * @param transparentColorOrNull set the transparent color (either sumbolic or in hexademical RGB notation "#XXXXXX".
     * @param page If the tiff file is a multi-page file, which page to convert to png.
     * @param deleteOriginalFile If <code>true</code>, delte the tiff file after the png file has been created successfully.
     * @throws EnvironmentFailureException If no converter method is available.
     */
    public Tiff2PngConversionStrategy(String transparentColorOrNull, int page,
            boolean deleteOriginalFile) throws EnvironmentFailureException
    {
        this.deleteOriginalFile = deleteOriginalFile;
        this.method = new ImageMagickTiffToPngConverter(transparentColorOrNull, page);
        if (method.isAvailable() == false)
        {
            throw new EnvironmentFailureException("No suitable converter method found");
        }
    }

    public Tiff2PngConversionStrategy(String transparentColorOrNull, int page)
            throws EnvironmentFailureException
    {
        this(transparentColorOrNull, page, false);
    }

    public Tiff2PngConversionStrategy(String transparentColorOrNull)
            throws EnvironmentFailureException
    {
        this(transparentColorOrNull, 0, false);
    }

    public Tiff2PngConversionStrategy() throws EnvironmentFailureException
    {
        this(null, 0, false);
    }

    //
    // IFileConversionStrategy
    //

    @Override
    public File tryCheckConvert(File inFile)
    {
        final String ext = FilenameUtils.getExtension(inFile.getName()).toLowerCase();
        if ("tiff".equals(ext) || "tif".equals(ext))
        {
            return new File(inFile.getParent(), FilenameUtils.getBaseName(inFile.getName())
                    + ".png");
        } else
        {
            return null;
        }
    }

    @Override
    public boolean deleteOriginalFile()
    {
        return deleteOriginalFile;
    }

    @Override
    public IFileConversionMethod getConverter()
    {
        return method;
    }

}
