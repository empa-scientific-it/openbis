/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class ExperimentBrowsingTest extends SystemTestCase
{
    @Test
    public void testListAllExperiments()
    {
        logIntoCommonClientService();
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(EntityType.ALL_TYPES_CODE);
        criteria.setExperimentType(experimentType);
        Project project = new Project();
        Space space = new Space();
        space.setCode("CISD");
        project.setSpace(space);
        project.setCode("NEMO");
        criteria.setProject(project);
        TypedTableResultSet<Experiment> resultSet = commonClientService.listExperiments(criteria);
        Experiment e1 = getOriginalObjectByCode(resultSet, "EXP-TEST-1");
        assertProperty(e1, "COMMENT", "cmnt");
        Experiment e2 = getOriginalObjectByCode(resultSet, "EXP-TEST-2");
        assertProperty(e2, "GENDER", "FEMALE");
        assertEquals(false, resultSet.getResultSet().isPartial());
        assertEquals(5, resultSet.getResultSet().getTotalLength());
        assertEquals(5, resultSet.getResultSet().getList().size());
    }

    @Test
    public void testListExperiments()
    {
        logIntoCommonClientService();
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");
        criteria.setExperimentType(experimentType);
        Project project = new Project();
        Space space = new Space();
        space.setCode("CISD");
        project.setSpace(space);
        project.setCode("DEFAULT");
        criteria.setProject(project);
        TypedTableResultSet<Experiment> resultSet = commonClientService.listExperiments(criteria);
        Experiment e1 = getOriginalObjectByCode(resultSet, "EXP-REUSE");
        assertEquals(null, e1.getDeletion());
        assertObjectWithCodeDoesNotExists(resultSet, "EXP-X"); // deleted
        assertEquals(false, resultSet.getResultSet().isPartial());
        assertEquals(3, resultSet.getResultSet().getTotalLength());
        assertEquals(3, resultSet.getResultSet().getList().size());
    }
}
