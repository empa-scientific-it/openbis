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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import loci.common.IRandomAccess;
import loci.formats.IFormatReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractMetaDataAwareImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ReadParams;

/**
 * Default BioFormats {@link IImageReader}.
 * 
 * @author Kaloyan Enimanev
 */
class DefaultBioformatsImageReader extends AbstractMetaDataAwareImageReader
{
    private final IFormatReader formatReader;

    private final boolean singleImagePerFile;

    DefaultBioformatsImageReader(String libraryName, String readerName, IFormatReader formatReader,
            boolean singleImagePerFile)
    {
        super(libraryName, readerName);
        this.formatReader = formatReader;
        this.singleImagePerFile = singleImagePerFile;
    }

    @Override
    public List<ImageID> getImageIDs(IRandomAccessFile handle) throws IOExceptionUnchecked
    {
        if (singleImagePerFile)
        {
            return super.getImageIDs(handle);
        } else
        {
            IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
            return BioFormatsImageUtils.listImageIDs(formatReader, input);
        }
    }

    @Override
    public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        Integer intensityRescalingChannel = null;
        ReadParams readParams = (ReadParams) params;
        if (readParams != null)
        {
            intensityRescalingChannel = readParams.getIntensityRescalingChannel();
        }

        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        if (intensityRescalingChannel != null)
        {
            return BioFormatsImageUtils.readImageWithIntensityRescaling(formatReader, input,
                    imageID, intensityRescalingChannel);
        } else
        {
            return BioFormatsImageUtils.readImage(formatReader, input, imageID);
        }
    }

    @Override
    public Map<String, Object> readMetaData(IRandomAccessFile handle, ImageID imageID,
            IReadParams params) throws IOExceptionUnchecked
    {
        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        return BioFormatsImageUtils.readMetadata(formatReader, input, imageID);
    }

    @Override
    public Dimension readDimensions(IRandomAccessFile handle, ImageID imageID)
    {
        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        return BioFormatsImageUtils.readImageDimensions(formatReader, input, imageID);
    }

    @Override
    public Integer readColorDepth(IRandomAccessFile handle, ImageID imageID)
    {
        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        return BioFormatsImageUtils.readImageColorDepth(formatReader, input, imageID);
    }
    
    @Override
    public void close()
    {
        try
        {
            formatReader.close();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
    }
}