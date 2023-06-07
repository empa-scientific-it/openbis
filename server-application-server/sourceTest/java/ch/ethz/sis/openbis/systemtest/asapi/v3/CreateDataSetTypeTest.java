/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;

/**
 * @author pkupczyk
 */
@Test
public class CreateDataSetTypeTest extends CreateEntityTypeTest<DataSetTypeCreation, DataSetType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected DataSetTypeCreation newTypeCreation()
    {
        return new DataSetTypeCreation();
    }

    @Override
    protected void fillTypeSpecificFields(DataSetTypeCreation creation)
    {
        creation.setMainDataSetPattern(".*\\.jpg");
        creation.setMainDataSetPath("original/images/");
        creation.setDisallowDeletion(true);
    }

    @Override
    protected void createTypes(String sessionToken, List<DataSetTypeCreation> creations)
    {
        v3api.createDataSetTypes(sessionToken, creations);
    }

    @Override
    protected DataSetType getType(String sessionToken, String code)
    {
        final DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(code);
        return v3api.getDataSetTypes(sessionToken, Collections.singletonList(permId), fo).get(permId);
    }

    @Override
    protected void assertTypeSpecificFields(DataSetTypeCreation creation, DataSetType type)
    {
        assertEquals(type.getMainDataSetPattern(), creation.getMainDataSetPattern());
        assertEquals(type.getMainDataSetPath(), creation.getMainDataSetPath());
        assertEquals(type.isDisallowDeletion(), (Boolean) creation.isDisallowDeletion());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode("LOG_TEST_1");

        DataSetTypeCreation creation2 = new DataSetTypeCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createDataSetTypes(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-data-set-types  NEW_DATA_SET_TYPES('[DataSetTypeCreation[code=LOG_TEST_1], DataSetTypeCreation[code=LOG_TEST_2]]')");
    }

    @Test
    public void testCreateWithMetaData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode("META_DATA_TEST_1");
        creation.setMetaData(Map.of("key", "value"));


        List<EntityTypePermId> ids = v3api.createDataSetTypes(sessionToken, Arrays.asList(creation));

        DataSetType type = getType(sessionToken, "META_DATA_TEST_1");

        assertEquals(type.getMetaData(), Map.of("key", "value"));
    }

}
