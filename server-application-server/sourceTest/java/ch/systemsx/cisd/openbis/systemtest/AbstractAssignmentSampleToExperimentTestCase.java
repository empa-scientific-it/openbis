/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 * @author Franz-Josef Elmer
 */
@Transactional(transactionManager = "transaction-manager")
@Rollback(false)
public abstract class AbstractAssignmentSampleToExperimentTestCase extends BaseTest
{
    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Project sourceProject;

    Project destinationProject;

    Space sourceSpace;

    Space destinationSpace;

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignSampleToExperimentRule;

    AuthorizationRule assignSampleToExperimentThroughExperimentUpdateRule;

    AuthorizationRule assignSharedSampleToExperimentRule;

    private static boolean fixtureRun = false;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        if (fixtureRun)
        {
            return;
        }

        sourceSpace = create(aSpace().withCode("sourceSpace"));
        destinationSpace = create(aSpace().withCode("destinationSpace"));

        sourceProject = create(aProject().inSpace(sourceSpace));
        destinationProject = create(aProject().inSpace(destinationSpace));

        sourceExperiment =
                create(anExperiment().inProject(sourceProject).withCode("sourceExperiment"));
        destinationExperiment =
                create(anExperiment().inProject(destinationProject).withCode(
                        "destinationExperiment"));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());

        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignSampleToExperimentRule =
                or(and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSampleToExperimentThroughExperimentUpdateRule =
                or(rule(source, RoleWithHierarchy.SPACE_POWER_USER),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSharedSampleToExperimentRule =
                and(rule(destination, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER));

        fixtureRun = true;
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToExperimentRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToExperimentRule), source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                assignSampleToExperimentThroughExperimentUpdateRule, source, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                not(assignSampleToExperimentThroughExperimentUpdateRule), source, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToExperimentRule,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToExperimentRule),
                destination, instance);
    }

    /**
     * Reassigns specified samples to specified experiment for the specified user session token. Sub class for testing API V3 should override this
     * method.
     */
    abstract protected void updateExperimentChangeSamples(String experimentIdentifier, List<String> samplePermIds,
            String userSessionToken);

    /**
     * Reassigns specified sample to specified experiment for the specified user session token. If experiment is not specified unassignment is meant.
     * Sub class for testing API V3 should override this method.
     */
    abstract protected void updateSampleChangeExperiment(String samplePermId, String experimentIdentifierOrNull,
            String userSessionToken);

    @Test
    public void unassignSampleWithDataSetsFromExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n");

        updateSampleChangeExperiment(g.s(1), null);

        assertEquals("S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void assignSampleWithoutExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1\n"
                + "S1, data sets: DS1[NET]\n");

        updateSampleChangeExperiment(g.s(1), g.e(1));

        assertEquals("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void assignSampleWithExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS3\n"
                + "E2, samples: S2, data sets: DS2\n"
                + "S1, data sets: DS1\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("E1, data sets: DS3\n"
                + "E2, samples: S1 S2, data sets: DS1 DS2\n"
                + "S1, data sets: DS1\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void assignSampleWithExperimentWithDataSetWithComponentFromAnotherExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "E3\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2");

        updateSampleChangeExperiment(g.s(1), g.e(3));

        assertEquals("E2, data sets: DS2\n"
                + "E3, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(3));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void assignSampleWithExperimentWithDataSetWithContainerFromAnotherSampleButSameExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1 S2, data sets: DS1 DS2\n"
                + "E2\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2");

        updateSampleChangeExperiment(g.s(2), g.e(2));

        assertEquals("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(2));
        assertModified(g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void assignSampleWithExperimentWithDataSetWithComponentFromAnotherSampleButSameExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1 S2, data sets: DS1 DS2\n"
                + "E2\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("E1, samples: S2, data sets: DS2\n"
                + "E2, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void assignScreeningPlateToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("E2, samples: S1, data sets: DS1 DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }

    @Test
    public void assignSamplesWithoutExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS2\n"
                + "S1, data sets: DS1[NET]\n");

        updateExperimentChangeSamples(g.e(1), g.s(1));

        assertEquals("E1, samples: S1, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void removeSamplesWithDataSetsFromExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1 S2, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET]\n");

        updateExperimentChangeSamples(g.e(1));

        assertEquals("E1, data sets: DS2\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void registerExperimentWithSampleWithDataSets()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NET]\n");

        registerExperimentWithSamples(g.e(1), g.s(1));

        assertEquals("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    @Rollback(true)
    public void registerExperimentWithSampleInDifferentSpace()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/S1\n", false);

        try
        {
            registerExperimentWithSamples(g.e(1), g.s(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample '" + entityGraphManager.getSample(g.s(1)).getIdentifier()
                    + "' does not belong to the space 'S0'", getErrorMessage(ex));
        }
    }

    @Test
    @Rollback(true)
    public void removeSamplesWithDataSetsFromExperimentFailsBecauseOneDataSetNeedsAnExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET] DS2\n", false);

        try
        {
            updateExperimentChangeSamples(g.e(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String errorMessage = getErrorMessage(ex);
            AssertionUtil.assertContains("Operation cannot be performed, because the sample "
                    + entityGraphManager.getSample(g.s(1)).getIdentifier(), errorMessage);
            AssertionUtil.assertContains(" has the following datasets which need an experiment: ["
                    + entityGraphManager.getDataSet(g.ds(2)).getCode() + "]", errorMessage);
        }
    }

    private void updateExperimentChangeSamples(ExperimentNode experimentNode, SampleNode... sampleNodes)
    {
        String experimentIdentifier = entityGraphManager.getExperimentIdentifierOrNull(experimentNode);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        updateExperimentChangeSamples(experimentIdentifier, getSamplePermIds(sampleNodes), user);
    }

    @Test
    @Rollback(true)
    public void addSampleToAnExperimentFailingBecauseSampleHasAlreadyAnExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1\n"
                + "E2\n", false);

        try
        {
            updateExperimentChangeSamples(g.e(2), g.s(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample '" + entityGraphManager.getSample(g.s(1)).getIdentifier()
                    + "' is already assigned to the experiment '"
                    + entityGraphManager.getExperimentIdentifierOrNull(g.e(1)) + "'.", getErrorMessage(ex));
        }
    }

    @Test
    public void assignSampleToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1\n"
                + "/S2/P2/E2\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("/S2/P2/E2, samples: /S2/S1\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertUnmodified(g);
    }

    @Test
    public void dataSetsOfSampleAreAssociatedWithNewExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1, data sets: DS1\n"
                + "/S1/S1, data sets: DS1\n"
                + "/S2/P2/E2\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("/S2/P2/E2, samples: /S2/S1, data sets: DS1\n"
                + "/S2/S1, data sets: DS1\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }

    @Test
    public void spaceSampleCanBeAssignedToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S2/P1/E1\n"
                + "/S1/S1\n");

        updateSampleChangeExperiment(g.s(1), g.e(1));

        assertEquals("/S2/P1/E1, samples: /S2/S1\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertUnmodified(g);
    }

    @Test
    @Rollback(true)
    public void spaceSampleCanNotBeAddedToExperimentFromAnotherSpace()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S2/P1/E1\n"
                + "/S1/S1\n", false);

        try
        {
            updateExperimentChangeSamples(g.e(1), g.s(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String errorMessage = getErrorMessage(ex);
            if (errorMessage.startsWith("Samples"))
            {
                AssertionUtil.assertContains("Samples with following codes do not exist in the project '/S2/P1' nor in the space 'S2': '["
                        + entityGraphManager.getSample(g.s(1)).getCode(), errorMessage);
            } else if (errorMessage.startsWith("Sample space"))
            {
                AssertionUtil.assertContains("Sample space must be the same as experiment space. Sample: "
                        + entityGraphManager.getSample(g.s(1)).getIdentifier(), errorMessage);
                String experimentIdentifier = entityGraphManager.getExperimentIdentifierOrNull(g.e(1));
                AssertionUtil.assertContains("Experiment: " + experimentIdentifier, errorMessage);
            } else
            {
                fail("Starts neither with 'Samples' nor with 'Sample space': " + errorMessage);
            }
        }
    }

    @Test
    public void spaceSamplesCanBeAddedToExperimentInSameSpace()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1\n"
                + "/S1/S1\n"
                + "/S1/S2\n");

        updateExperimentChangeSamples(g.e(1), g.s(1), g.s(2));

        assertEquals("/S1/P1/E1, samples: /S1/S1 /S1/S2\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1), g.s(2));
        assertUnmodified(g);
    }

    @Test
    public void sharedSampleCanBeAssignedToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S2/P1/E1\n"
                + "/S1\n");

        updateSampleChangeExperiment(g.s(1), g.e(1));

        assertEquals("/S2/P1/E1, samples: /S2/S1\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertUnmodified(g);
    }

    @Test
    public void childSampleCanBeAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1 /S1/S2\n"
                + "/S2/P2/E2\n"
                + "/S1/S1, children: /S1/S2\n");

        updateSampleChangeExperiment(g.s(2), g.e(2));

        assertEquals("/S1/P1/E1, samples: /S1/S1\n"
                + "/S2/P2/E2, samples: /S2/S2\n"
                + "/S1/S1, children: /S2/S2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(2));
        assertUnmodified(g);
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1 /S1/S2\n"
                + "/S2/P2/E2\n"
                + "/S1/S1, children: /S1/S2\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("/S1/P1/E1, samples: /S1/S2\n"
                + "/S2/P2/E2, samples: /S2/S1\n"
                + "/S2/S1, children: /S1/S2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertUnmodified(g);
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1 /S1/S2\n"
                + "/S2/P2/E2\n"
                + "/S1/S1, components: /S1/S2\n");

        updateSampleChangeExperiment(g.s(2), g.e(2));

        assertEquals("/S1/P1/E1, samples: /S1/S1\n"
                + "/S2/P2/E2, samples: /S2/S2\n"
                + "/S1/S1, components: /S2/S2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(2));
        assertUnmodified(g);
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1 /S1/S2\n"
                + "/S2/P2/E2\n"
                + "/S1/S1, components: /S1/S2\n");

        updateSampleChangeExperiment(g.s(1), g.e(2));

        assertEquals("/S1/P1/E1, samples: /S1/S2\n"
                + "/S2/P2/E2, samples: /S2/S1\n"
                + "/S2/S1, components: /S1/S2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertUnmodified(g);
    }

    @Test
    @Rollback(true)
    public void sampleWithExperimentCanNotBeAssignedToAnotherExperimentThroughExperimentUpdate()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1, samples: /S1/S1\n"
                + "/S1/P2/E2\n", false);

        try
        {
            updateExperimentChangeSamples(g.e(2), g.s(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample '" + entityGraphManager.getSample(g.s(1)).getIdentifier()
                    + "' is already assigned to the experiment '"
                    + entityGraphManager.getExperimentIdentifierOrNull(g.e(1)) + "'.", getErrorMessage(ex));
        }
    }

    @Test
    @Rollback(true)
    public void sharedSampleCanNotBeAssignedToExperimentThroughExperimentUpdate()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/P1/E1\n"
                + "/S1\n", false);

        try
        {
            updateExperimentChangeSamples(g.e(1), g.s(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String errorMessage = getErrorMessage(ex);
            if (errorMessage.startsWith("Samples"))
            {
                AssertionUtil.assertContains("Samples with following codes do not exist in the project '/S1/P1' nor in the space 'S1': '["
                    + entityGraphManager.getSample(g.s(1)).getCode(), errorMessage);
            } else if (errorMessage.startsWith("Shared samples"))
            {
                AssertionUtil.assertContains("Shared samples cannot be attached to experiments. Sample: " 
                        + entityGraphManager.getSample(g.s(1)).getIdentifier(), errorMessage);
                String experimentIdentifier = entityGraphManager.getExperimentIdentifierOrNull(g.e(1));
                AssertionUtil.assertContains("Experiment: " + experimentIdentifier, errorMessage);
            } else
            {
                fail("Starts neither with 'Samples' nor with 'Shared samples': " + errorMessage);
            }
        }
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", groups = "authorization")
    public void assigningSampleToExperimentIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    @Rollback(true)
    public void assigningSampleToExperimentIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToExperiment", groups = "authorization")
    public void assigningSharedSampleToExperimentIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSharedSampleToExperiment(destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    @Rollback(true)
    public void assigningSharedSampleToExperimentIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSharedSampleToExperiment(destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate", groups = "authorization")
    public void assigningSampleToExperimentThroughExperimentUpdateIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSamplesToExperiment(destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    @Rollback(true)
    public void assigningSampleToExperimentThroughExperimentUpdateIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        checkAssigningSamplesToExperiment(destinationSpaceRole, instanceRole);
    }

    private void checkAssigningSampleToExperiment(RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        Sample sample = create(aSample().inExperiment(sourceExperiment));
        updateSampleChangeExperiment(sample.getPermId(), destinationExperiment.getIdentifier(), user);
    }

    private void checkAssigningSharedSampleToExperiment(RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        Sample sample = create(aSample());
        updateSampleChangeExperiment(sample.getPermId(), destinationExperiment.getIdentifier(), user);
    }

    private void checkAssigningSamplesToExperiment(RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        Sample sample = create(aSample().inSpace(destinationSpace));
        updateExperimentChangeSamples(destinationExperiment.getIdentifier(), Arrays.asList(sample.getPermId()), user);
    }

    private void updateSampleChangeExperiment(SampleNode sampleNode, ExperimentNode experimentNodeOrNull)
    {
        String samplePermId = entityGraphManager.getSamplePermIdOrNull(sampleNode);
        String experimentIdentifierOrNull = entityGraphManager.getExperimentIdentifierOrNull(experimentNodeOrNull);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        updateSampleChangeExperiment(samplePermId, experimentIdentifierOrNull, user);
    }

    private void registerExperimentWithSamples(ExperimentNode experimentNode, SampleNode... sampleNodes)
    {
        List<String> samplePermIds = getSamplePermIds(sampleNodes);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        String projectIdentifier = entityGraphManager.getIdentifierOfDefaultProject();
        Sample[] samples = loadSamples(samplePermIds);
        Project project = commonServer.getProjectInfo(systemSessionToken, ProjectIdentifierFactory.parse(projectIdentifier));
        Experiment experiment = create(anExperiment().inProject(project).withSamples(samples).as(user));
        String experimentIdentifier = experiment.getIdentifier();

        addToRepository(experimentNode, etlService.tryGetExperiment(systemSessionToken,
                ExperimentIdentifierFactory.parse(experimentIdentifier)));
    }

    private List<String> getSamplePermIds(SampleNode... sampleNodes)
    {
        List<String> samplePermIds = new ArrayList<String>();
        for (SampleNode sampleNode : sampleNodes)
        {
            samplePermIds.add(entityGraphManager.getSamplePermIdOrNull(sampleNode));
        }
        return samplePermIds;
    }

    protected String getErrorMessage(Exception e)
    {
        return e.getMessage();
    }

}
