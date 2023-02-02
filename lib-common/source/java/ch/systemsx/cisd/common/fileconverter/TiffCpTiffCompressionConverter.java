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
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A class that performs tiff file compression using TibTiff's <code>tiffcp</code> utility.
 * 
 * @author Bernd Rinn
 */
class TiffCpTiffCompressionConverter extends AbstractTiffCpImageFileConverter
{

    private final static String DEFAULT_COMPRESSION_TYPE = "lzw:2";

    private final String compressionType;

    public TiffCpTiffCompressionConverter()
    {
        this(DEFAULT_COMPRESSION_TYPE);
    }

    public TiffCpTiffCompressionConverter(String compressionTypeOrNull)
    {
        super(LogFactory.getLogger(LogCategory.MACHINE, TiffCpTiffCompressionConverter.class),
                LogFactory.getLogger(LogCategory.OPERATION, TiffCpTiffCompressionConverter.class));
        this.compressionType =
                (compressionTypeOrNull == null) ? DEFAULT_COMPRESSION_TYPE : compressionTypeOrNull;
    }

    @Override
    protected List<String> getCommandLine(File inFile, File outFile)
    {
        return Arrays.asList("-c", compressionType, inFile.getAbsolutePath(),
                outFile.getAbsolutePath());
    }

}