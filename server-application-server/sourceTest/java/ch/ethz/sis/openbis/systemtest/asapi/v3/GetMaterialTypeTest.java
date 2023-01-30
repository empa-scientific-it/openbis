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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;

/**
 * @author pkupczyk
 */
public class GetMaterialTypeTest extends AbstractGetEntityTypeTest
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode, List<PropertyAssignmentCreation> properties,
            IPluginId validationPluginId)
    {
        MaterialTypeCreation creation = new MaterialTypeCreation();
        creation.setCode(entityTypeCode);
        creation.setPropertyAssignments(properties);
        creation.setValidationPluginId(validationPluginId);

        List<EntityTypePermId> permIds = v3api.createMaterialTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected FetchOptions<?> createFetchOptions(boolean withProperties, boolean withValidationPlugin)
    {
        MaterialTypeFetchOptions fo = new MaterialTypeFetchOptions();
        if (withProperties)
        {
            fo.withPropertyAssignments().withEntityType();
            fo.withPropertyAssignments().withPropertyType();
            fo.withPropertyAssignments().withPlugin();
        }
        if (withValidationPlugin)
        {
            fo.withValidationPlugin();
        }
        return fo;
    }

    @Override
    protected Map<IEntityTypeId, ? extends IEntityType> getEntityTypes(String sessionToken, List<? extends IEntityTypeId> entityTypeIds,
            FetchOptions<?> fetchOptions)
    {
        return v3api.getMaterialTypes(sessionToken, entityTypeIds, (MaterialTypeFetchOptions) fetchOptions);
    }

    @Test
    public void testGetByIdsWithBasicFields()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialTypeCreation creation = new MaterialTypeCreation();
        creation.setCode("TEST_MATERIAL_TYPE");
        creation.setDescription("test description");

        List<EntityTypePermId> permIds = v3api.createMaterialTypes(sessionToken, Arrays.asList(creation));
        Map<IEntityTypeId, MaterialType> types = v3api.getMaterialTypes(sessionToken, permIds, new MaterialTypeFetchOptions());

        MaterialType type = types.values().iterator().next();
        assertEquals(type.getPermId().getPermId(), creation.getCode());
        assertEquals(type.getCode(), creation.getCode());
        assertEquals(type.getDescription(), creation.getDescription());

        assertPropertyAssignmentsNotFetched(type);
        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialTypeFetchOptions fo = new MaterialTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.getMaterialTypes(sessionToken, Arrays.asList(new EntityTypePermId("VIRUS"), new EntityTypePermId("GENE")), fo);

        assertAccessLog(
                "get-material-types  MATERIAL_TYPE_IDS('[VIRUS, GENE]') FETCH_OPTIONS('MaterialType\n    with PropertyAssignments\n')");
    }

}
