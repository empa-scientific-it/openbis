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

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * The main purpose of this interface is to return a <code>IDataStoreStrategy</code> for a given <code>DataSetInformation</code>.
 * <p>
 * To perform its job it might use some helpers defined in the constructor.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IDataStrategyStore
{

    /**
     * For given <var>dataSetInfo</var> and given <var>incomingDataSetPath</var> returns the corresponding <code>IDataStoreStrategy</code>. As a side
     * effect sets also the sample properties, the experiment, and if not already set, the experiment identifier.
     * 
     * @param dataSetInfo The data set information, gets enriched in the process.
     * @param incomingDataSetPath mainly used for logging purposes.
     */
    public IDataStoreStrategy getDataStoreStrategy(final DataSetInformation dataSetInfo,
            final File incomingDataSetPath);

    /**
     * For given <var>dataStoreStrategyKey</var> return corresponding <code>IDataStoreStrategy</code>
     */
    public IDataStoreStrategy getDataStoreStrategy(DataStoreStrategyKey dataStoreStrategyKey);

    /**
     * Get this data strategy store with the cache for openbis calls.
     */
    public IDataStrategyStore getCachedDataStrategyStore();
}