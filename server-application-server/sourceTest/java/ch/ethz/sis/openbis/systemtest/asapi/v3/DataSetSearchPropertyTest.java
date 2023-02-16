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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetSearchPropertyTest extends AbstractSearchPropertyTest
{

    @Override
    protected EntityTypePermId createEntityType(final String sessionToken, final String code, final PropertyTypePermId... propertyTypeIds)
    {
        return createADataSetType(sessionToken, false, propertyTypeIds);
    }

    @Override 
    protected void deleteEntityTypes(final String sessionToken, final IEntityTypeId... entityTypeIds)
    {
        final DataSetTypeDeletionOptions deletionOptions = new DataSetTypeDeletionOptions();
        deletionOptions.setReason("Test");
        v3api.deleteDataSetTypes(sessionToken, List.of(entityTypeIds), deletionOptions);
    }

    @Override
    protected ObjectPermId createEntity(final String sessionToken, final String code, final EntityTypePermId entityTypeId,
            final Map<String, String> propertyMap)
    {
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(entityTypeId);
        dataSetCreation.setProperties(new HashMap<>(propertyMap));
        return v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
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
        final DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("Test");
        return v3api.deleteDataSets(sessionToken,
                Arrays.stream(entityIds).map(entityId -> (IDataSetId) entityId).collect(Collectors.toList()),
                deletionOptions);
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria()
    {
        return new DataSetSearchCriteria();
    }

    @Override
    protected List<? extends IPermIdHolder> search(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        return v3api.searchDataSets(sessionToken, (DataSetSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

}
