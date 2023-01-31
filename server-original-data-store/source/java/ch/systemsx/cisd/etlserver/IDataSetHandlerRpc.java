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

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * The interface for data set handlers that are used in RPC situtations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetHandlerRpc extends IDataSetHandler
{
    /**
     * Handles the data set using the template to get default information for the data sets to be registered.
     * 
     * @param dataSet The File containing the data set to register
     * @param template A template containing information for the data sets to register
     * @see IDataSetHandler#handleDataSet
     */
    public List<DataSetInformation> handleDataSet(final File dataSet,
            final DataSetInformation template);

    /**
     * Create a (hard) link to an file or folder within an existing data set and handle that.
     * 
     * @param dataSetComponent A file within a data set
     * @param template A template containing information for the data sets to register
     */
    public List<DataSetInformation> linkAndHandleDataSet(File dataSetComponent,
            DataSetInformation template);

    /**
     * Returns the session context for the current user.
     */
    public SessionContextDTO getSessionContext();

    /**
     * Get the file for a data set from the data store.
     * 
     * @param shareId
     */
    public File getFileForDataSet(PhysicalDataSet data, String shareId);

    /**
     * Get the owner information provided by the caller.
     */
    public DataSetOwner getDataSetOwner();

    /**
     * Get the data set information provided by the caller
     */
    public DataSetInformation getCallerDataSetInformation();
}
