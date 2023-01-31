/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * An interface for data set handlers that give other code a chance to implement the registration of the new data object.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IExtensibleDataSetHandler extends IDataSetHandler
{
    public List<DataSetInformation> handleDataSet(final File dataSet,
            DataSetInformation dataSetInformation,
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator);

    /**
     * Return true if this top-level data set handler uses a marker file.
     */
    public boolean isUseIsFinishedMarkerFile();

    /**
     * Return the store root directory for the data set handler.
     */
    public File getStoreRootDir();
}
