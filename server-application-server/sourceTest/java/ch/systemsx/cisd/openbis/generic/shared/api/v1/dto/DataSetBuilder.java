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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;

/**
 * Builder of {@link DataSet} instances.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetBuilder
{
    private DataSetInitializer initializer;

    public DataSetBuilder()
    {
        initializer = new DataSetInitializer();
        EntityRegistrationDetails registrationDetails =
                new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
        initializer.setRegistrationDetails(registrationDetails);
    }

    public DataSet getDataSet()
    {
        return new DataSet(initializer);
    }

    public DataSetBuilder code(String code)
    {
        initializer.setCode(code);
        return this;
    }

    public DataSetBuilder type(String type)
    {
        initializer.setDataSetTypeCode(type);
        return this;
    }

    public DataSetBuilder experiment(String experimentIdentifier)
    {
        initializer.setExperimentIdentifier(experimentIdentifier);
        return this;
    }

    public DataSetBuilder sample(String sampleIdentifier)
    {
        initializer.setSampleIdentifierOrNull(sampleIdentifier);
        return this;
    }

    public DataSetBuilder property(String key, String value)
    {
        initializer.getProperties().put(key, value);
        return this;
    }

}
