/*
 * Copyright 2015 ETH Zuerich, SIS
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
import static org.testng.AssertJUnit.fail;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * Abstract super class for all tests assigning a data set to a sample or an experiment. Subclasses for the different versions of the API have only to
 * implement {@link #reassignToExperiment(String, String, String)} and {@link #reassignToSample(String, String, String)}.
 * 
 * @author anttil
 * @author Franz-Josef Elmer
 */
@Transactional(transactionManager = "transaction-manager")
@Rollback(false)
public abstract class AbstractDataSetAssignmentTestCase extends BaseTest
{
    Sample sourceSample;

    Sample destinationSample;

    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Space sourceSpace;

    Space destinationSpace;

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignDataSetToSampleRule;

    AuthorizationRule assignDataSetToExperimentRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    public void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());

        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignDataSetToSampleRule =
                and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER));
        assignDataSetToExperimentRule =
                and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        or(rule(destination, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(destination, RoleWithHierarchy.SPACE_ETL_SERVER)));
    }

    /**
     * Reassign specified data set to specified experiment for the specified user session token. If experiment is not specified unassignment is meant.
     * Sub class for testing API V3 should override this method.
     */
    protected abstract void reassignToExperiment(String dataSetCode, String experimentIdentifierOrNull, String userSessionToken);

    /**
     * Reassign specified data set to specified sample for the specified user session token. If sample is not specified unassignment is meant. Sub
     * class for testing API V3 should override this method.
     */
    protected abstract void reassignToSample(String dataSetCode, String samplePermIdOrNull, String userSessionToken);

    @DataProvider
    public Object[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToExperimentRule, source,
                destination, instance);
    }

    @DataProvider
    public Object[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToExperimentRule), source,
                destination, instance);
    }

    @DataProvider
    public Object[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToSampleRule, source, destination, instance);
    }

    @DataProvider
    public Object[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToSampleRule), source, destination, instance);
    }

    @Test
    public void reassignTheTwoOriginalDataSetsOfPublishedDataSetsToDifferentOriginalSampleAndExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS3 DS4 DS5 DS6\n"
                + "E2, data sets: DS1 DS2\n"
                + "E3, samples: S3\n"
                + "S1, data sets: DS3\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS4\n"
                + "DS3, components: DS5 DS6\n");

        reassignToSample(g.ds(3), g.s(3));
        reassignToExperiment(g.ds(4), g.e(3));

        assertEquals("E1, samples: S1\n"
                + "E2, data sets: DS1 DS2\n"
                + "E3, samples: S3, data sets: DS3 DS4 DS5 DS6\n"
                + "S3, data sets: DS3\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS4\n"
                + "DS3, components: DS5 DS6\n", renderGraph(g));
        assertModified(g.e(1), g.e(3));
        assertModified(g.s(1), g.s(3));
        assertModified(g.ds(3), g.ds(4), g.ds(5), g.ds(6));
        assertUnmodified(g);
    }

    @Test
    public void reassignTwoPublishedDataSetToAnotherPublicExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS3 DS4 DS5 DS6\n"
                + "E2, data sets: DS1 DS2\n"
                + "E3\n"
                + "S1, data sets: DS3\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS4\n"
                + "DS3, components: DS5 DS6\n");

        reassignToExperiment(g.ds(1), g.e(3));
        reassignToExperiment(g.ds(2), g.e(3));

        assertEquals("E1, samples: S1, data sets: DS3 DS4 DS5 DS6\n"
                + "E3, data sets: DS1 DS2\n"
                + "S1, data sets: DS3\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS4\n"
                + "DS3, components: DS5 DS6\n", renderGraph(g));
        assertModified(g.e(2), g.e(3));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromSampleWithoutExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS4\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, data sets: DS4\n"
                + "E2, samples: S2, data sets: DS1[NECT] DS2[NECT]\n"
                + "S2, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4\n", renderGraph(g));
        assertModified(g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromSampleWithoutExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS4\n"
                + "S1, data sets: DS1[NECT] DS2[NECT]\n"
                + "S2\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, data sets: DS4\n"
                + "S2, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4\n", renderGraph(g));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromSampleWithoutExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS4\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, data sets: DS4\n"
                + "E2, samples: S2, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4\n", renderGraph(g));
        assertModified(g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NECT] DS2 DS4\n"
                + "E3, data sets: DS3\n"
                + "S1, data sets: DS2\n"
                + "S2, data sets: DS5[NET]\n"
                + "DS1[NECT], components: DS2 DS3\n"
                + "DS2, components: DS4\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1, data sets: DS2 DS4\n"
                + "E3, data sets: DS3\n"
                + "S1, data sets: DS2\n"
                + "S2, data sets: DS1[NECT] DS5[NET]\n"
                + "DS1[NECT], components: DS2 DS3\n"
                + "DS2, components: DS4\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2 DS4\n"
                + "E2, samples: S2\n"
                + "E3, data sets: DS3\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2 DS4\n"
                + "E3, data sets: DS3\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(1), g.ds(2), g.ds(4));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2 DS4\n"
                + "E2, samples: S2\n"
                + "E3, data sets: DS3\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2 DS4\n"
                + "E3, data sets: DS3\n"
                + "S2, data sets: DS1\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(1), g.ds(2), g.ds(4));
        assertModified(g.s(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithSomeComponentsReassignedFromExperimentToSampleWithExperiment2()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2 DS4\n"
                + "E2, samples: S2\n"
                + "E3, data sets: DS3\n"
                + "S1, data sets: DS2\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1, data sets: DS2 DS4\n"
                + "E2, samples: S2, data sets: DS1\n"
                + "E3, data sets: DS3\n"
                + "S1, data sets: DS2\n"
                + "S2, data sets: DS1\n"
                + "DS1, components: DS2 DS3\n"
                + "DS2, components: DS4\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "DS1, components: DS2\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "DS1, components: DS2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E2, samples: S2, data sets: DS1 DS2\n"
                + "S2, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NECT] DS2[NET]\n"
                + "S2\n"
                + "DS1[NECT], components: DS2[NET]\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "S2, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithExperimentToExperiment2()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, components: DS2");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    // This is screening test case where one container data set is moved to another plate.
    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2\n"
                + "S2, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NECT] DS2[NET]\n"
                + "S1, data sets: DS1[NECT] DS2[NET]\n"
                + "S2\n"
                + "DS1[NECT], components: DS2[NET]\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "S2, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithoutExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1\n"
                + "S1, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n");

        reassignToExperiment(g.ds(1), g.e(1));

        assertEquals("E1, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithoutExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1\n"
                + "S2, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n");

        reassignToSample(g.ds(1), g.s(1));

        assertEquals("E1, samples: S1, data sets: DS1[NECT] DS2[NET]\n"
                + "S1, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void containerWithAllItsComponentsReassignedFromSampleWithoutExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NET]\n"
                + "S2\n"
                + "DS1[NECT], components: DS2[NET]\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("S2, data sets: DS1[NECT] DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    @Rollback(true)
    public void containerWithAComponentOfWrongTypeReassignedFromExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1[NECT] DS2\n"
                + "S2, data sets: DS5[NET]\n"
                + "DS1[NECT], components: DS2\n", false);

        try
        {
            reassignToSample(g.ds(1), g.s(2));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            AbstractExternalData dataSet = entityGraphManager.tryGetDataSet(g.ds(2));
            Sample sample = entityGraphManager.tryGetSample(g.s(2));
            assertDataSetToSampleExceptionMessage(ex, sample, dataSet);
        }
    }

    @Test
    @Rollback(true)
    public void dataSetCannotBeAssignedToSpaceSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\nS2\n", false);

        try
        {
            reassignToSample(g.ds(1), g.s(2));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            AbstractExternalData dataSet = entityGraphManager.tryGetDataSet(g.ds(1));
            Sample sample = entityGraphManager.tryGetSample(g.s(2));
            assertDataSetToSampleExceptionMessage(ex, sample, dataSet);
        }
    }

    @Test
    @Rollback(true)
    public void dataSetCannotBeAssignedToSharedSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NET]\n/S2\n", false);

        try
        {
            reassignToSample(g.ds(1), g.s(2));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            AbstractExternalData dataSet = entityGraphManager.tryGetDataSet(g.ds(1));
            Sample sample = entityGraphManager.tryGetSample(g.s(2));
            assertDataSetToSampleExceptionMessage(ex, sample, dataSet);
        }
    }

    @Test
    public void experimentAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2\n"
                + "DS1, parents: DS2\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, data sets: DS2\n"
                + "E2, data sets: DS1\n"
                + "DS1, parents: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void experimentAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2\n"
                + "DS1, parents: DS2\n");

        reassignToExperiment(g.ds(2), g.e(2));

        assertEquals("E1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "DS1, parents: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void sampleAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, parents: DS2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1, data sets: DS2\n"
                + "E2, samples: S2, data sets: DS1\n"
                + "S1, data sets: DS2\n"
                + "S2, data sets: DS1\n"
                + "DS1, parents: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void sampleAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, parents: DS2\n");

        reassignToSample(g.ds(2), g.s(2));

        assertEquals("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, parents: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void experimentAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2\n"
                + "DS1, components: DS2\n");

        reassignToExperiment(g.ds(2), g.e(2));

        assertEquals("E1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void experimentAssignmentOfComponentDataSetIsChangedWhenContainerDataSetIsAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2\n"
                + "DS1, components: DS2\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E2, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void sampleAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n");

        reassignToSample(g.ds(2), g.s(2));

        assertEquals("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void sampleAssignmentOfComponentDataSetIsChangedWhenContainerDataSetIsAssignedToAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2, samples: S2\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1 DS2\n"
                + "S2, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void dataSetCanBeUnassignedFromSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n");

        reassignToSample(g.ds(1), null);

        assertEquals("E1, samples: S1, data sets: DS1\n", renderGraph(g));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment", groups = "authorization")
    public void assigningDataSetToAnotherExperimentIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssignmentToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions = {
            AuthorizationFailureException.class }, groups = "authorization")
    @Rollback(true)
    public void assigningDataSetToAnotherExperimentIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        checkAssignmentToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssignmentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToSample", expectedExceptions = {
            AuthorizationFailureException.class }, groups = "authorization")
    @Rollback(true)
    public void assigningDataSetToSampleIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
    {
        checkAssignmentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    private void assertDataSetToSampleExceptionMessage(UserFailureException ex, Sample sample, AbstractExternalData dataset)
    {
        String postfix = sample.getSpace() == null ? "shared." : "not connected to any experiment and the data set type ("
                + dataset.getDataSetType().getCode()
                + ") doesn't match one of the following regular expressions:   NO-EXP-.* ,   NE.*  .";
        ex.printStackTrace();
        assertEquals("The dataset '" + dataset.getCode() + "' cannot be connected to the sample '"
                + sample.getIdentifier() + "' because the new sample is " + postfix, getErrorMessage(ex));
    }

    private void checkAssignmentToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
    {
        AbstractExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        reassignToExperiment(dataset.getCode(), destinationExperiment.getIdentifier(), user);
    }

    private void checkAssignmentToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        reassignToSample(dataset.getCode(), destinationSample.getPermId(), user);
    }

    private void reassignToExperiment(DataSetNode dataSetNode, ExperimentNode experimentNodeOrNull)
    {
        String code = entityGraphManager.getDataSetCodeOrNull(dataSetNode);
        String experimentIdentifierOrNull = entityGraphManager.getExperimentIdentifierOrNull(experimentNodeOrNull);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        reassignToExperiment(code, experimentIdentifierOrNull, user);
    }

    private void reassignToSample(DataSetNode dataSetNode, SampleNode sampleNodeOrNull)
    {
        String code = entityGraphManager.getDataSetCodeOrNull(dataSetNode);
        String permIdOrNull = entityGraphManager.getSamplePermIdOrNull(sampleNodeOrNull);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        reassignToSample(code, permIdOrNull, user);
    }

    protected String getErrorMessage(Exception e)
    {
        return e.getMessage();
    }

}
