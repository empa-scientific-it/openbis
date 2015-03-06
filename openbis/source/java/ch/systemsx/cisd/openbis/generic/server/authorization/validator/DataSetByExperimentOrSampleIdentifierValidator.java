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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * Validator based on the experiment or sample identifier of a {@link DataSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetByExperimentOrSampleIdentifierValidator extends AbstractDataSetByExperimentOrSampleIdentifierValidator<DataSet>
{

    @Override
    protected boolean isStorageConfirmed(DataSet dataSet)
    {
        return dataSet.isStorageConfirmed();
    }

    @Override
    protected String getExperimentIdentifier(DataSet dataSet)
    {
        return dataSet.getExperimentIdentifier();
    }

    @Override
    protected String getSampleIdentifier(DataSet dataSet)
    {
        return dataSet.getSampleIdentifierOrNull();
    }

}
