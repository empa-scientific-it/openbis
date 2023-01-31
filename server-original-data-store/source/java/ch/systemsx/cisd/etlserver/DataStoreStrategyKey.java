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

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Key associated with each {@link IDataStoreStrategy}.
 * 
 * @author Christian Ribeaud
 */
public enum DataStoreStrategyKey
{
    /**
     * This <code>IDataStoreStrategy</code> implementation if for data set that has been identified as <i>unidentified</i>, meaning that, for
     * instance, no experiment could be mapped to the one found in given {@link DataSetInformation} (if we try to find out the sample to which this
     * data set should be registered through the experiment).
     */
    UNIDENTIFIED,
    /**
     * This <code>IDataStoreStrategy</code> implementation if for data set that has been <i>identified</i>, meaning that kind of connection to this
     * data set could be found in the database (through the derived <i>Master Plate</i> or through the experiment specified).
     */
    IDENTIFIED,
    /**
     * This <code>IDataStoreStrategy</code> implementation if for data set that has been identified as <i>invalid</i>, meaning that the data set
     * itself or its <code>Master Plate</code> code is not registered in the database. So there is no possibility to link the data set to an already
     * existing sample.
     */
    INVALID,
    /**
     * States that the transformation part could not be processed correctly.
     */
    ERROR;
}