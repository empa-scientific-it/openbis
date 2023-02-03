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

import java.util.List;

import loci.formats.IFormatReader;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;

/**
 * {@link IImageReaderLibrary} implementation for BioFormats.
 * 
 * @author Kaloyan Enimanev
 */
public class BioFormatsReaderLibrary implements IImageReaderLibrary
{
    /**
     * If the reader has this suffix then it is assumed that each file contains a single image and
     * the file does not have to be read to determine that.
     */
    public final static String SINGLE_IMAGE_PER_FILE_READER_SUFFIX = "##SINGLE_IMAGE";

    @Override
    public String getName()
    {
        return ImageReaderConstants.BIOFORMATS_LIBRARY;
    }

    @Override
    public List<String> getReaderNames()
    {
        return BioFormatsImageUtils.getReaderNames();
    }

    /**
     * Tries to create a reader of specified name. This is a factory method which returns for each
     * invocation a new instance of the requested reader.
     * 
     * @return <code>null</code> if no corresponding reader is found.
     */
    @Override
    public IImageReader tryGetReader(String readerName)
    {
        String readerNameToUse = readerName;
        boolean singleImagePerFile =
                readerName.toUpperCase().endsWith(SINGLE_IMAGE_PER_FILE_READER_SUFFIX);
        if (singleImagePerFile)
        {
            readerNameToUse =
                    readerName.substring(0, readerName.length()
                            - SINGLE_IMAGE_PER_FILE_READER_SUFFIX.length());
        }
        final IFormatReader formatReaderOrNull =
                BioFormatsImageUtils.tryToCreateReaderByName(readerNameToUse);
        formatReaderOrNull.setGroupFiles(false);
        return tryAdaptFormatReader(formatReaderOrNull, singleImagePerFile);
    }

    /**
     * Tries to create a suitable reader for the file specified with <var>fileName</var>. This is a
     * factory method which returns for each invocation a new instance of a suitable reader.
     * 
     * @return <code>null</code> if no suitable reader is found.
     */
    @Override
    public IImageReader tryGetReaderForFile(String fileName)
    {
        IFormatReader formatReaderOrNull = BioFormatsImageUtils.tryToCreateReaderForFile(fileName);
        return tryAdaptFormatReader(formatReaderOrNull, false);
    }

    /**
     * Delegate the wrapping of non-null readers to an abstract method.
     */
    protected IImageReader tryAdaptFormatReader(final IFormatReader formatReaderOrNull,
            boolean singleImagePerFile)
    {
        return (formatReaderOrNull == null) ? null : adaptFormatReader(formatReaderOrNull,
                singleImagePerFile);
    }

    protected IImageReader adaptFormatReader(final IFormatReader formatReader,
            boolean singleImagePerFile)
    {
        final String libraryName = getName();
        final String readerName = BioFormatsImageUtils.getReaderName(formatReader);
        return new DefaultBioformatsImageReader(libraryName, readerName, formatReader,
                singleImagePerFile);
    }

}
