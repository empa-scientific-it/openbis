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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * Extractor for processor, data set, file format, and locator type.
 * 
 * @author Franz-Josef Elmer
 */
public interface ITypeExtractor extends IFileFormatTypeExtractor
{
    /** Properties key prefix for the type extractor. */
    public static final String TYPE_EXTRACTOR_KEY = "type-extractor";

    /**
     * Returns <code>true</code> if the specified data set contains measured data.
     */
    public boolean isMeasuredData(File incomingDataSetPath);

    /**
     * Returns the type of the {@link IProcessor} to be used.
     */
    public String getProcessorType(File incomingDataSetPath);

    /**
     * Gets the data set type from the specified path of the incoming data set.
     */
    public DataSetType getDataSetType(File incomingDataSetPath);

    /**
     * Gets the locator type from the specified path of the incoming data set.
     */
    public LocatorType getLocatorType(File incomingDataSetPath);

}
