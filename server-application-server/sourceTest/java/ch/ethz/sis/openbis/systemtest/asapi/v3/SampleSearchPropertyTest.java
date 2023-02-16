/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class SampleSearchPropertyTest extends AbstractSearchPropertyTest
{

    @Override
    protected EntityTypePermId createEntityType(final String sessionToken, final String code,
            final PropertyTypePermId... propertyTypeIds)
    {
        return createASampleType(sessionToken, false, propertyTypeIds);
    }

    @Override
    protected void deleteEntityTypes(final String sessionToken, final IEntityTypeId... entityTypeIds)
    {
        final SampleTypeDeletionOptions deletionOptions = new SampleTypeDeletionOptions();
        deletionOptions.setReason("Test");
        v3api.deleteSampleTypes(sessionToken, List.of(entityTypeIds), deletionOptions);
    }

    @Override
    protected ObjectPermId createEntity(final String sessionToken, final String code,
            final EntityTypePermId entityTypeId, final Map<String, String> propertyMap)
    {
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TEST-SAMPLE-" + System.currentTimeMillis());
        sampleCreation.setTypeId(entityTypeId);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperties(new HashMap<>(propertyMap));
        return v3api.createSamples(sessionToken, List.of(sampleCreation)).get(0);
    }

    @Override
    protected ObjectPermId createEntity(String sessionToken, String code, EntityTypePermId entityTypeId,
            String propertyType, String value)
    {
        return createEntity(sessionToken, code, entityTypeId, Map.of(propertyType, value));
    }

    @Override
    protected IDeletionId deleteEntities(final String sessionToken, final IObjectId... entityIds)
    {
        final SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("Test");
        return v3api.deleteSamples(sessionToken,
                Arrays.stream(entityIds).map(entityId -> (ISampleId) entityId).collect(Collectors.toList()),
                deletionOptions);
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
