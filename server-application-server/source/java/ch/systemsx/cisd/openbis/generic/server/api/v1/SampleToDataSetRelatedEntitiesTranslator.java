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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * A class that converts {@link Sample} objects to {@link DataSetRelatedEntities} objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleToDataSetRelatedEntitiesTranslator
{
    // A map from sample type Id to sample type.
    private final HashMap<Long, SampleType> sampleTypesMap;

    private final List<Sample> samples;

    private final ArrayList<BasicEntityInformationHolder> entityInformationHolders;

    /**
     * Creates a translator from public {@Sample} objects to the internal {@link DataSetRelatedEntities} objects.
     * <p>
     * A list of sample types known to the DB must be provided because Sample knows only the code of the SampleType.
     * 
     * @param sampleTypes A list of SampleTypes known to the DB.
     * @param samples The samples to convert.
     */
    public SampleToDataSetRelatedEntitiesTranslator(List<SampleType> sampleTypes,
            List<Sample> samples)
    {
        this.sampleTypesMap = convertSampleTypesListToMap(sampleTypes);
        this.samples = samples;
        entityInformationHolders = new ArrayList<BasicEntityInformationHolder>(samples.size());
    }

    private static HashMap<Long, SampleType> convertSampleTypesListToMap(
            List<SampleType> sampleTypes)
    {
        HashMap<Long, SampleType> map = new HashMap<Long, SampleType>(sampleTypes.size());

        for (SampleType sampleType : sampleTypes)
        {
            map.put(sampleType.getId(), sampleType);
        }

        return map;
    }

    public DataSetRelatedEntities convertToDataSetRelatedEntities()
    {
        for (Sample sample : samples)
        {
            BasicEntityInformationHolder holderOrNull =
                    tryConvertSampleToEntityInformationHolder(sample);
            if (null != holderOrNull)
            {
                entityInformationHolders.add(holderOrNull);
            }
        }
        return new DataSetRelatedEntities(entityInformationHolders);
    }

    private BasicEntityInformationHolder tryConvertSampleToEntityInformationHolder(Sample sample)
    {
        EntityType entityType = sampleTypesMap.get(sample.getSampleTypeId());
        if (null == entityType)
        {
            return null;
        }
        BasicEntityInformationHolder holder =
                new BasicEntityInformationHolder(EntityKind.SAMPLE, entityType, sample.getCode(),
                        sample.getId(), sample.getPermId());
        return holder;
    }
}
