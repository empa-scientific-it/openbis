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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class DeleteExperimentTypeTest extends AbstractDeleteEntityTypeTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode)
    {
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode(entityTypeCode);

        List<EntityTypePermId> permIds = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected IObjectId createEntity(String sessionToken, IEntityTypeId entityTypeId)
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT_" + System.currentTimeMillis());
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setTypeId(entityTypeId);

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected AbstractObjectDeletionOptions<?> createEntityTypeDeletionOptions()
    {
        return new ExperimentTypeDeletionOptions();
    }

    @Override
    protected ICodeHolder getEntityType(String sessionToken, IEntityTypeId entityTypeId)
    {
        return v3api.getExperimentTypes(sessionToken, Collections.singletonList(entityTypeId), new ExperimentTypeFetchOptions()).get(entityTypeId);
    }

    @Override
    protected void deleteEntityType(String sessionToken, List<IEntityTypeId> entityTypeIds, AbstractObjectDeletionOptions<?> options)
    {
        v3api.deleteExperimentTypes(sessionToken, entityTypeIds, (ExperimentTypeDeletionOptions) options);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentTypeDeletionOptions o = new ExperimentTypeDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteExperimentTypes(sessionToken,
                Arrays.asList(new EntityTypePermId("TEST-LOGGING-1"), new EntityTypePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-experiment-types  EXPERIMENT_TYPE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('ExperimentTypeDeletionOptions[reason=test-reason]')");
    }

}
