/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;

/**
 * @author pkupczyk
 */
@Test
public class CreateMaterialTypeTest extends CreateEntityTypeTest<MaterialTypeCreation, MaterialType>
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
    protected void fillTypeSpecificFields(MaterialTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void createTypes(String sessionToken, List<MaterialTypeCreation> creations)
    {
        v3api.createMaterialTypes(sessionToken, creations);
    }

    @Override
    protected MaterialType getType(String sessionToken, String code)
    {
        final MaterialTypeFetchOptions fo = new MaterialTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(code);
        return v3api.getMaterialTypes(sessionToken, Collections.singletonList(permId), fo).get(permId);
    }

    @Override
    protected void assertTypeSpecificFields(MaterialTypeCreation creation, MaterialType type)
    {
        // nothing to do
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialTypeCreation creation = new MaterialTypeCreation();
        creation.setCode("LOG_TEST_1");

        MaterialTypeCreation creation2 = new MaterialTypeCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createMaterialTypes(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-material-types  NEW_MATERIAL_TYPES('[MaterialTypeCreation[code=LOG_TEST_1], MaterialTypeCreation[code=LOG_TEST_2]]')");
    }

}
