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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;

/**
 * @author pkupczyk
 */
@Test
public class CreateExperimentTypeTest extends CreateEntityTypeTest<ExperimentTypeCreation, ExperimentType>
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
    protected void fillTypeSpecificFields(ExperimentTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void createTypes(String sessionToken, List<ExperimentTypeCreation> creations)
    {
        v3api.createExperimentTypes(sessionToken, creations);
    }

    @Override
    protected ExperimentType getType(String sessionToken, String code)
    {
        final ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(code);
        return v3api.getExperimentTypes(sessionToken, Collections.singletonList(permId), fo).get(permId);
    }

    @Override
    protected void assertTypeSpecificFields(ExperimentTypeCreation creation, ExperimentType type)
    {
        // nothing to do
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("LOG_TEST_1");

        ExperimentTypeCreation creation2 = new ExperimentTypeCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createExperimentTypes(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-experiment-types  NEW_EXPERIMENT_TYPES('[ExperimentTypeCreation[code=LOG_TEST_1], ExperimentTypeCreation[code=LOG_TEST_2]]')");
    }

}
