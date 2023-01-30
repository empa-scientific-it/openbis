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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;

/**
 * @author pkupczyk
 */
public class GetExperimentTypeTest extends AbstractGetEntityTypeTest
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode, List<PropertyAssignmentCreation> properties,
            IPluginId validationPluginId)
    {
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode(entityTypeCode);
        creation.setPropertyAssignments(properties);
        creation.setValidationPluginId(validationPluginId);

        List<EntityTypePermId> permIds = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected FetchOptions<?> createFetchOptions(boolean withProperties, boolean withValidationPlugin)
    {
        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
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
        return v3api.getExperimentTypes(sessionToken, entityTypeIds, (ExperimentTypeFetchOptions) fetchOptions);
    }

    @Test
    public void testGetByIdsWithBasicFields()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("TEST_EXPERIMENT_TYPE");
        creation.setDescription("test description");

        List<EntityTypePermId> permIds = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation));
        Map<IEntityTypeId, ExperimentType> types = v3api.getExperimentTypes(sessionToken, permIds, new ExperimentTypeFetchOptions());

        ExperimentType type = types.values().iterator().next();
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

        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.getExperimentTypes(sessionToken, Arrays.asList(new EntityTypePermId("SIRNA_HCS"), new EntityTypePermId("COMPOUND_HCS")), fo);

        assertAccessLog(
                "get-experiment-types  EXPERIMENT_TYPE_IDS('[SIRNA_HCS, COMPOUND_HCS]') FETCH_OPTIONS('ExperimentType\n    with PropertyAssignments\n')");
    }

}
