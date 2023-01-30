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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;

/**
 * @author pkupczyk
 */
public class GetDataSetTypeTest extends AbstractGetEntityTypeTest
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode, List<PropertyAssignmentCreation> properties,
            IPluginId validationPluginId)
    {
        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode(entityTypeCode);
        creation.setPropertyAssignments(properties);
        creation.setValidationPluginId(validationPluginId);

        List<EntityTypePermId> permIds = v3api.createDataSetTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected FetchOptions<?> createFetchOptions(boolean withProperties, boolean withValidationPlugin)
    {
        DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
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
        return v3api.getDataSetTypes(sessionToken, entityTypeIds, (DataSetTypeFetchOptions) fetchOptions);
    }

    @Test
    public void testGetByIdsWithBasicFields()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode("TEST_DATA_SET_TYPE");
        creation.setDescription("test description");
        creation.setDisallowDeletion(true);
        creation.setMainDataSetPath("main data set path");
        creation.setMainDataSetPattern("main data set pattern");

        List<EntityTypePermId> permIds = v3api.createDataSetTypes(sessionToken, Arrays.asList(creation));
        Map<IEntityTypeId, DataSetType> types = v3api.getDataSetTypes(sessionToken, permIds, new DataSetTypeFetchOptions());

        DataSetType type = types.values().iterator().next();
        assertEquals(type.getPermId().getPermId(), creation.getCode());
        assertEquals(type.getCode(), creation.getCode());
        assertEquals(type.getDescription(), creation.getDescription());
        assertEquals(type.isDisallowDeletion(), (Boolean) creation.isDisallowDeletion());
        assertEquals(type.getMainDataSetPath(), creation.getMainDataSetPath());
        assertEquals(type.getMainDataSetPattern(), creation.getMainDataSetPattern());

        assertPropertyAssignmentsNotFetched(type);
        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.getDataSetTypes(sessionToken, Arrays.asList(new EntityTypePermId("HCS_IMAGE"), new EntityTypePermId("CONTAINER_TYPE")), fo);

        assertAccessLog(
                "get-data-set-types  DATA_SET_TYPE_IDS('[HCS_IMAGE, CONTAINER_TYPE]') FETCH_OPTIONS('DataSetType\n    with PropertyAssignments\n')");
    }

}
