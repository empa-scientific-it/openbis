/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;

/**
 * @author pkupczyk
 */
public interface IDataSetLister
{

    /**
     * Returns the data set meta data for the given <var>dataSetCodes</var> with the given <var>dataSetFetchOptions</var>.
     * 
     * @return One data set meta data object for each entry in <var>dataSetCodes</var>.
     */
    public List<DataSet> getDataSetMetaData(List<String> dataSetCodes,
            DataSetFetchOptions dataSetFetchOptions);

    /**
     * Returns the download URLs for a set of <var>dataSetCodes</var>.
     * 
     * @return The list of data store download URLs, each with the list of data set codes it has stored.
     */
    public List<DataStoreURLForDataSets> getDataStoreDownloadURLs(List<String> dataSetCodes);

    /**
     * Returns the remote URLs for a set of <var>dataSetCodes</var>.
     * 
     * @return The list of data store remote URLs, each with the list of data set codes it has stored.
     */
    public List<DataStoreURLForDataSets> getDataStoreRemoteURLs(List<String> dataSetCodes);
}
