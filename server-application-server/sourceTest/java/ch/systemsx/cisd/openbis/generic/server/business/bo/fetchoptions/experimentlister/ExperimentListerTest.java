/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Test cases for {@link IExperimentListingQuery}.
 * 
 * @author Piotr Kupczyk
 */
@Test(groups =
{ "db", "dataset" })
public class ExperimentListerTest extends AbstractDAOTest
{

    @Autowired
    private IServiceForDataStoreServer service;

    private IExperimentLister lister;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryAuthenticate("test", "password").getSessionToken();
        lister =
                new ExperimentLister(daoFactory, service.getBaseIndexURL(sessionToken),
                        EntityListingTestUtils.createQuery(daoFactory,
                                IExperimentListingQuery.class));
    }

    @Test
    public void testListExperimentsForEmptyExperimentIdentifiersListShouldReturnEmptyList()
    {
        List<Experiment> result =
                lister.listExperiments(new ArrayList<ExperimentIdentifier>(),
                        new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(new ArrayList<ExperimentIdentifier>(), result);
    }

    @Test
    public void testListExperimentsForExistingExperimentIdentifierShouldReturnExperiment()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "NEMO", "EXP1"));

        List<Experiment> result = lister.listExperiments(identifiers, new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(identifiers, result);
    }

    @Test
    public void testListExperimentsForExistingExperimentIdentifiersShouldReturnExperiments()
    {
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        identifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-1"));
        identifiers.add(new ExperimentIdentifier("CISD", "NOE", "EXP-TEST-2"));

        List<Experiment> result = lister.listExperiments(identifiers, new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(identifiers, result);
    }

    @Test
    public void testListExperimentsForExistingAndNotExistingExperimentIdentifiersShouldReturnExistingExperiments()
    {
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        identifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-1"));
        identifiers.add(new ExperimentIdentifier("CISD", "NOE", "EXP-TEST-10"));

        List<Experiment> result = lister.listExperiments(identifiers, new ExperimentFetchOptions());

        identifiers.remove(1);
        assertEqualToExperimentsWithIdentifiers(identifiers, result);
    }

    @Test
    public void testListExperimentsForProjectsForEmptyProjectIdentifiersListShouldReturnEmptyList()
    {
        List<Experiment> result =
                lister.listExperimentsForProjects(new ArrayList<ProjectIdentifier>(),
                        new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(new ArrayList<ExperimentIdentifier>(), result);
    }

    @Test
    public void testListExperimentsForProjectsForExistingProjectShouldReturnProjectExperiments()
    {
        List<ProjectIdentifier> projectIdentifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "NEMO"));

        List<ExperimentIdentifier> experimentIdentifiers = new ArrayList<ExperimentIdentifier>();
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP1"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP10"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP11"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-1"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-2"));

        List<Experiment> result =
                lister.listExperimentsForProjects(projectIdentifiers, new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(experimentIdentifiers, result);
    }

    @Test
    public void testListExperimentsForProjectsForExistingProjectsShouldReturnProjectsExperiments()
    {
        List<ProjectIdentifier> projectIdentifiers = new ArrayList<ProjectIdentifier>();
        projectIdentifiers.add(new ProjectIdentifier("CISD", "NEMO"));
        projectIdentifiers.add(new ProjectIdentifier("CISD", "NOE"));

        List<ExperimentIdentifier> experimentIdentifiers = new ArrayList<ExperimentIdentifier>();
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP1"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP10"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP11"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-1"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NEMO", "EXP-TEST-2"));
        experimentIdentifiers.add(new ExperimentIdentifier("CISD", "NOE", "EXP-TEST-2"));

        List<Experiment> result =
                lister.listExperimentsForProjects(projectIdentifiers, new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(experimentIdentifiers, result);
    }

    @Test
    public void testListExperimentsForProjectsForExistingAndNotExistingProjectIdentifiersShouldReturnExistingProjectsExperiments()
    {
        List<ProjectIdentifier> projectIdentifiers = new ArrayList<ProjectIdentifier>();
        projectIdentifiers.add(new ProjectIdentifier("CISD", "NOE"));
        projectIdentifiers.add(new ProjectIdentifier("CISD", "UNKNOWN-PROJECT"));

        List<ExperimentIdentifier> experimentIdentifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "NOE",
                        "EXP-TEST-2"));

        List<Experiment> result =
                lister.listExperimentsForProjects(projectIdentifiers, new ExperimentFetchOptions());

        assertEqualToExperimentsWithIdentifiers(experimentIdentifiers, result);
    }

    private void assertEqualToExperimentsWithIdentifiers(
            List<ExperimentIdentifier> expectedExperimentsIdentifiers,
            List<Experiment> actualExperiments)
    {
        assertEquals(expectedExperimentsIdentifiers.size(), actualExperiments.size());

        Map<Long, Experiment> expectedExperimentsMap = new HashedMap<Long, Experiment>();
        for (ExperimentIdentifier expectedExperimentIdentifier : expectedExperimentsIdentifiers)
        {
            Experiment expectedExperiment =
                    service.tryGetExperiment(sessionToken, expectedExperimentIdentifier);
            expectedExperimentsMap.put(expectedExperiment.getId(), expectedExperiment);
        }

        for (Experiment actualExperiment : actualExperiments)
        {
            Experiment expectedExperiment = expectedExperimentsMap.get(actualExperiment.getId());
            assertNotNull(expectedExperiment);
            assertEqualsToExperiment(expectedExperiment, actualExperiment);
        }

    }

    private void assertEqualsToExperiment(Experiment expected, Experiment actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getPermId(), actual.getPermId());
        assertEquals(expected.getPermlink(), actual.getPermlink());
        assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        assertEquals(expected.getModificationDate(), actual.getModificationDate());
        assertEqualsToExperimentType(expected.getExperimentType(), actual.getExperimentType());
        assertEqualsToProject(expected.getProject(), actual.getProject());
        assertEqualsToPerson(expected.getRegistrator(), actual.getRegistrator());
        assertTrue(actual.getFetchOptions().isSetOf(ExperimentFetchOption.BASIC));
    }

    private void assertEqualsToExperimentType(ExperimentType expected, ExperimentType actual)
    {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getDescription(), actual.getDescription());
    }

    private void assertEqualsToProject(Project expected, Project actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getModificationDate(), actual.getModificationDate());
        assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        assertEqualsToSpace(expected.getSpace(), actual.getSpace());
    }

    private void assertEqualsToPerson(Person expected, Person actual)
    {
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        assertEquals(expected.getUserId(), actual.getUserId());
    }

    private void assertEqualsToSpace(Space expected, Space actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
    }
}