/*
 * Copyright 2017 ETH Zuerich, SIS
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Test
public class UpdateMaterialTypeTest extends UpdateEntityTypeTest<MaterialTypeCreation, MaterialTypeUpdate, MaterialType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected MaterialTypeCreation newTypeCreation()
    {
        return new MaterialTypeCreation();
    }

    @Override
    protected MaterialTypeUpdate newTypeUpdate()
    {
        return new MaterialTypeUpdate();
    }

    @Override
    protected EntityTypePermId getTypeId()
    {
        return new EntityTypePermId("gene", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.MATERIAL);
    }

    @Override
    protected void createEntity(String sessionToken, IEntityTypeId entityType, String propertyType, String propertyValue)
    {
    }

    @Override
    protected List<EntityTypePermId> createTypes(String sessionToken, List<MaterialTypeCreation> updates)
    {
        return v3api.createMaterialTypes(sessionToken, updates);
    }

    @Override
    protected void updateTypes(String sessionToken, List<MaterialTypeUpdate> updates)
    {
        v3api.updateMaterialTypes(sessionToken, updates);
    }

    @Override
    protected MaterialType getType(String sessionToken, EntityTypePermId typeId)
    {
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals(typeId.getPermId());
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withEntityType();
        fetchOptions.withPropertyAssignments().withPropertyType();
        return v3api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);
    }

    @Override
    protected void updateTypeSpecificFields(MaterialTypeUpdate update, int variant)
    {
        // No specific fields
    }

    @Override
    protected void assertTypeSpecificFields(MaterialType type, MaterialTypeUpdate update, int variant)
    {
        // No specific fields
    }

    @Override
    protected String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId)
    {
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType entityType : commonServer.listMaterialTypes(sessionToken))
        {
            if (entityType.getCode().equals(typeId.getPermId()))
            {
                Script validationScript = entityType.getValidationScript();
                return validationScript == null ? null : validationScript.getName();
            }
        }
        return null;
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId)
    {
        MaterialSearchCriteria sarchCriteria = new MaterialSearchCriteria();
        sarchCriteria.withType().withId().thatEquals(typeId);
        return sarchCriteria;
    }

    @Override
    protected List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();
        return v3api.searchMaterials(sessionToken, (MaterialSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialTypeUpdate update = new MaterialTypeUpdate();
        update.setTypeId(new EntityTypePermId("VIRUS"));

        MaterialTypeUpdate update2 = new MaterialTypeUpdate();
        update2.setTypeId(new EntityTypePermId("BACTERIUM"));

        v3api.updateMaterialTypes(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-material-types  MATERIAL_TYPE_UPDATES('[MaterialTypeUpdate[typeId=VIRUS (MATERIAL)], MaterialTypeUpdate[typeId=BACTERIUM (MATERIAL)]]')");
    }

}
