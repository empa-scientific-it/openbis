/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.validation;

import java.io.File;
import java.io.Reader;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Interface for data set validator
 *
 * @author Franz-Josef Elmer
 */
public interface IDataSetValidator
{

    /**
     * Validates the specified data set file/folder assuming the specified data set type. Implementation can ignore data set type.
     * 
     * @throws UserFailureException if validation failed.
     */
    public abstract void assertValidDataSet(DataSetType dataSetType,
            File incomingDataSetFileOrFolder);

    /**
     * Validates data with specified name from the specified reader.
     * 
     * @throws UserFailureException if validation failed.
     */
    public abstract void assertValidDataSet(DataSetType dataSetType, Reader reader, String dataSourceName);

}