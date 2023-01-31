/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Wrapper class of full identifier and perm id.
 *
 * @author Franz-Josef Elmer
 */
public class Identifier
{
    public static Identifier create(Experiment experiment)
    {
        return new Identifier(experiment.getIdentifier(), experiment.getPermId(), DataSetOwnerType.EXPERIMENT);
    }

    public static Identifier create(Sample sample)
    {
        return new Identifier(sample.getIdentifier(), sample.getPermId(), DataSetOwnerType.SAMPLE);
    }

    public static Identifier create(DataSet dataSet)
    {
        return new Identifier(dataSet.getCode(), dataSet.getCode(), DataSetOwnerType.DATA_SET);
    }

    private final String fullIdentifier;

    private final String permId;

    private final DataSetOwnerType ownerType;

    private Identifier(String fullIdentifier, String permId, DataSetOwnerType entityKind)
    {
        this.fullIdentifier = fullIdentifier;
        this.permId = permId;
        this.ownerType = entityKind;
    }

    public String getFullIdentifier()
    {
        return fullIdentifier;
    }

    public String getPermId()
    {
        return permId;
    }

    public DataSetOwnerType getOwnerType()
    {
        return ownerType;
    }

    @Override
    public String toString()
    {
        return fullIdentifier;
    }

}
