/*
 * Copyright 2018 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.AbstractObjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;

/**
 * @author pkupczyk
 */
public class DeleteMaterialTypeTest extends AbstractDeleteEntityTypeTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode)
    {
        MaterialTypeCreation creation = new MaterialTypeCreation();
        creation.setCode(entityTypeCode);

        List<EntityTypePermId> permIds = v3api.createMaterialTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected IObjectId createEntity(String sessionToken, IEntityTypeId entityTypeId)
    {
        MaterialCreation creation = new MaterialCreation();
        creation.setCode("TEST_MATERIAL_" + System.currentTimeMillis());
        creation.setTypeId(entityTypeId);

        List<MaterialPermId> permIds = v3api.createMaterials(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected AbstractObjectDeletionOptions<?> createEntityTypeDeletionOptions()
    {
        return new MaterialTypeDeletionOptions();
    }

    @Override
    protected ICodeHolder getEntityType(String sessionToken, IEntityTypeId entityTypeId)
    {
        return v3api.getMaterialTypes(sessionToken, Collections.singletonList(entityTypeId), new MaterialTypeFetchOptions()).get(entityTypeId);
    }

    @Override
    protected void deleteEntityType(String sessionToken, List<IEntityTypeId> entityTypeIds, AbstractObjectDeletionOptions<?> options)
    {
        v3api.deleteMaterialTypes(sessionToken, entityTypeIds, (MaterialTypeDeletionOptions) options);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialTypeDeletionOptions o = new MaterialTypeDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteMaterialTypes(sessionToken,
                Arrays.asList(new EntityTypePermId("TEST-LOGGING-1"), new EntityTypePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-material-types  MATERIAL_TYPE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('MaterialTypeDeletionOptions[reason=test-reason]')");
    }

}
