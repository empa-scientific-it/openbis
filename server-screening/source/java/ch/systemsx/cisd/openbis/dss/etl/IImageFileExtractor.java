/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * This role is supposed to be implemented by classes that can extract HCS or Microscopy image files from an incoming data set directory.
 * Implementations of this interface need to have a constructor that takes {@link Properties} to initialize itself.
 * 
 * @author Tomasz Pylak
 */
public interface IImageFileExtractor
{

    /**
     * Extracts plate images in the given <var>incomingDataSetDirectory</var> and for the given <var>dataSetInfo</var> and returns the image files
     * that it finds.
     * 
     * @return the extraction result.
     */
    public ImageFileExtractionResult extract(final File incomingDataSetDirectory,
            DataSetInformation dataSetInformation);
}