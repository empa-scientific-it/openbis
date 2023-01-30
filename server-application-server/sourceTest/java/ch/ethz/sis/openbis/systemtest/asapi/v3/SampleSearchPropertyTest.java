/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class SampleSearchPropertyTest extends AbstractSearchPropertyTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, PropertyTypePermId propertyTypeId)
    {
        return createASampleType(sessionToken, false, propertyTypeId);
    }

    @Override
    protected ObjectPermId createEntity(String sessionToken, String code, EntityTypePermId entityTypeId,
            String propertyType, String value)
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TEST-SAMPLE-" + System.currentTimeMillis());
        sampleCreation.setTypeId(entityTypeId);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty(propertyType, value);
        return v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria()
    {
        return new SampleSearchCriteria();
    }

    @Override
    protected List<? extends IPermIdHolder> search(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        return v3api.searchSamples(sessionToken, (SampleSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

}
