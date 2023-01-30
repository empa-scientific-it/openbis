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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Test
public class UpdateExperimentTypeTest extends UpdateEntityTypeTest<ExperimentTypeCreation, ExperimentTypeUpdate, ExperimentType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected ExperimentTypeCreation newTypeCreation()
    {
        return new ExperimentTypeCreation();
    }

    @Override
    protected ExperimentTypeUpdate newTypeUpdate()
    {
        return new ExperimentTypeUpdate();
    }

    @Override
    protected EntityTypePermId getTypeId()
    {
        return new EntityTypePermId("COMPOUND_HCS", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT);
    }

    @Override
    protected void createEntity(String sessionToken, IEntityTypeId entityType, String propertyType, String propertyValue)
    {
    }

    @Override
    protected List<EntityTypePermId> createTypes(String sessionToken, List<ExperimentTypeCreation> updates)
    {
        return v3api.createExperimentTypes(sessionToken, updates);
    }

    @Override
    protected void updateTypes(String sessionToken, List<ExperimentTypeUpdate> updates)
    {
        v3api.updateExperimentTypes(sessionToken, updates);
    }

    @Override
    protected ExperimentType getType(String sessionToken, EntityTypePermId typeId)
    {
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals(typeId.getPermId());
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withEntityType();
        fetchOptions.withPropertyAssignments().withPropertyType();
        return v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);
    }

    @Override
    protected void updateTypeSpecificFields(ExperimentTypeUpdate update, int variant)
    {
        // No specific fields
    }

    @Override
    protected void assertTypeSpecificFields(ExperimentType type, ExperimentTypeUpdate update, int variant)
    {
        // No specific fields
    }

    @Override
    protected String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId)
    {
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType entityType : commonServer.listExperimentTypes(sessionToken))
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
        ExperimentSearchCriteria sarchCriteria = new ExperimentSearchCriteria();
        sarchCriteria.withType().withId().thatEquals(typeId);
        return sarchCriteria;
    }

    @Override
    protected List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        return v3api.searchExperiments(sessionToken, (ExperimentSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentTypeUpdate update = new ExperimentTypeUpdate();
        update.setTypeId(new EntityTypePermId("SIRNA_HCS"));

        ExperimentTypeUpdate update2 = new ExperimentTypeUpdate();
        update2.setTypeId(new EntityTypePermId("COMPOUND_HCS"));

        v3api.updateExperimentTypes(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-experiment-types  EXPERIMENT_TYPE_UPDATES('[ExperimentTypeUpdate[typeId=SIRNA_HCS (EXPERIMENT)], ExperimentTypeUpdate[typeId=COMPOUND_HCS (EXPERIMENT)]]')");
    }

}
