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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author pkupczyk
 */
public class DeleteSampleTypeTest extends AbstractDeleteEntityTypeTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode)
    {
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode(entityTypeCode);

        List<EntityTypePermId> permIds = v3api.createSampleTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected IObjectId createEntity(String sessionToken, IEntityTypeId entityTypeId)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE_" + System.currentTimeMillis());
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setTypeId(entityTypeId);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected AbstractObjectDeletionOptions<?> createEntityTypeDeletionOptions()
    {
        return new SampleTypeDeletionOptions();
    }

    @Override
    protected ICodeHolder getEntityType(String sessionToken, IEntityTypeId entityTypeId)
    {
        return v3api.getSampleTypes(sessionToken, Collections.singletonList(entityTypeId), new SampleTypeFetchOptions()).get(entityTypeId);
    }

    @Override
    protected void deleteEntityType(String sessionToken, List<IEntityTypeId> entityTypeIds, AbstractObjectDeletionOptions<?> options)
    {
        v3api.deleteSampleTypes(sessionToken, entityTypeIds, (SampleTypeDeletionOptions) options);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeDeletionOptions o = new SampleTypeDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteSampleTypes(sessionToken,
                Arrays.asList(new EntityTypePermId("TEST-LOGGING-1"), new EntityTypePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-sample-types  SAMPLE_TYPE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('SampleTypeDeletionOptions[reason=test-reason]')");
    }

}
