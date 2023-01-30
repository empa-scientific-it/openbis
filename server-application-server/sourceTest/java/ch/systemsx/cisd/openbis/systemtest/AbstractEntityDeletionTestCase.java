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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SessionBuilder;

/**
 * Abstract super class of all entity deletion system tests.
 *
 * @author Franz-Josef Elmer
 */
@Transactional(transactionManager = "transaction-manager")
@Rollback(false)
public abstract class AbstractEntityDeletionTestCase extends BaseTest
{
    @Test
    public final void testTrashExperimentsWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph(
                "E1, samples: S50, data sets: DS60 DS61\n"
                        + "E2, samples: S51\n"
                        + "E3\n");

        deleteExperiments(g.e(1), g.e(2), g.e(3));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2), g.e(3));
        assertDeleted(g.s(50), g.s(51));
        assertDeleted(g.ds(60), g.ds(61));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentsWithDeepDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S50, data sets: DS60 DS61 DS70 DS71\n"
                + "E2, samples: S51\n"
                + "E3\n"
                + "S50, components: S52, data sets: DS70 DS71\n"
                + "S51, components: S53\n");

        deleteExperiments(g.e(1), g.e(2), g.e(3));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2), g.e(3));
        assertDeleted(g.s(50), g.s(51), g.s(52), g.s(53));
        assertDeleted(g.ds(60), g.ds(61), g.ds(70), g.ds(71));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentWithSampleWhichIsAComponentOfASpaceSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2\n"
                + "S1, components: S2\n");

        deleteExperiments(g.e(1));

        assertEquals("", renderGraph(g));
        assertModified(g.s(1));
        assertDeleted(g.e(1));
        assertDeleted(g.s(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public void testTrashExperimentWithSampleWhichHasAChild()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1\n"
                + "S1, children: S3\n");

        deleteExperiments(g.e(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.s(1));
        assertModified(g.s(3));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(false)
    public final void testTrashExperimentWithSampleAndDataSet()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n", false);

        deleteExperiments(g.e(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentsWithSampleAndDataSetsAndNoExternalLinks()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");

        deleteExperiments(g.e(1), g.e(2));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentWithARelatedDataSetComponentWhichBelongsToAnExternalExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");

        deleteExperiments(g.e(1));

        assertEquals("E2, data sets: DS2\n", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public final void testTrashExperimentWithARelatedDataSetInAnExternalContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", false);

        failTrashExperiment(g.e(2), createExpectedErrorMessage(g.ds(2), g.ds(1), g.s(1)));

        assertEquals("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public void testTrashPublishedExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS3 DS4 DS5 DS6\n"
                + "E2, data sets: DS1 DS2\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS6\n"
                + "DS3, components: DS4\n"
                + "DS5, parents: DS6\n");

        deleteExperiments(g.e(2));

        assertEquals("E1, data sets: DS3 DS4 DS5 DS6\n"
                + "DS3, components: DS4\n"
                + "DS5, parents: DS6\n", renderGraph(g));
        assertDeleted(g.e(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashPublishedExperimentWithOrginalExperimentWithSamples()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n");

        deleteExperiments(g.e(2));

        assertEquals("E1, samples: S1, data sets: DS2 DS3\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS2, components: DS3\n", renderGraph(g));
        assertDeleted(g.e(2));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public final void testTrashOrginalExperimentWithSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n", false);

        failTrashExperiment(g.e(1), createExpectedErrorMessage(g.ds(2), g.ds(1), g.e(2)));

        assertEquals("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentsWithContainerDataSetWithPhysicalDataSetFromAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "DS1, components: DS2\n");

        deleteExperiments(g.e(1));

        assertEquals("E2, data sets: DS2\n", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSamplesAlreadyTrashed()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1\nS2\n");

        assertEquals("", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSamplesWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S20\n"
                + "S3, data sets: DS60[NET]\n"
                + "S20, data sets: DS61[NET]\n");

        deleteSamples(g.s(1), g.s(3));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(3), g.s(20));
        assertDeleted(g.ds(60), g.ds(61));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSampleWithTwoLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2 S3, data sets: DS1[NET]\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS3[NET]\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2), g.s(3));
        assertDeleted(g.ds(1), g.ds(2), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashComponentSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2\n"
                + "S1, components: S2\n");

        deleteSamples(g.s(2));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(2));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSampleWithAComponentWithAContainerDataSetWithAComponentDataSetOfFirstSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2, data sets: DS2[NET]\n"
                + "S2, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSampleWithAComponentWithAComponentDataSetOfAContainerDataSetOfFirstSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2, data sets: DS1[NECT]\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSampleWithAnExperimentSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2\n"
                + "S1, components: S2\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertModified(g.e(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSampleWithAnExperimentSampleWithADataSet()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS1\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertDeleted(g.ds(1));
        assertModified(g.e(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentSampleWithADataSet()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n");

        deleteSamples(g.s(1));

        assertEquals("", renderGraph(g));
        assertModified(g.e(1));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashExperimentSampleWhichIsAComponentOfASpaceSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2\n"
                + "S1, components: S2\n");

        deleteSamples(g.s(2));

        assertEquals("", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertDeleted(g.s(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n", false);

        failTrashSample(g.s(1), createExpectedErrorMessage(g.ds(2), g.ds(1), g.s(3)));

        assertEquals("S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1, components: DS2[NET]\n", false);

        failTrashSample(g.s(1), createExpectedErrorMessage(g.ds(2), g.ds(1), g.e(1)));

        assertEquals("E1, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1, components: DS2[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public void testTrashSampleWithDataSetWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "S2, data sets: DS2[NECT]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n", false);

        failTrashSample(g.s(1), createExpectedErrorMessage(g.ds(4), g.ds(2), g.s(2)));

        assertEquals("S1, data sets: DS1[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "S2, data sets: DS2[NECT]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    @Rollback(true)
    public void testTrashSampleWithComponentsFromAnInvisibleSpace()
    {
        EntityGraphGenerator g = parseAndCreateGraph("/S1/S1, components: /S2/S2\n", false);
        SessionBuilder sessionBuilder = aSession().withSpaceRole(RoleWithHierarchy.SPACE_ADMIN,
                entityGraphManager.getSample(g.s(1)).getSpace());
        String userSessionToken = create(sessionBuilder);

        String userID = sessionBuilder.getUserID();
        String sample = entityGraphManager.getSample(g.s(2)).getIdentifier();
        failTrashSample(userSessionToken, g.s(1), createExpectedErrorMessage(userID, sample));

        assertEquals("/S1/S1, components: /S2/S2\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashDataSets()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET] DS4[NET] DS5[NET] DS6[NET]\n"
                + "DS1[NECT], components: DS5[NET]\n"
                + "DS2[NECT], components: DS6[NET]\n");

        deleteDataSets(g.ds(1), g.ds(2), g.ds(3));

        assertEquals("S1, data sets: DS4[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertDeleted(g.ds(1), g.ds(2), g.ds(3), g.ds(5), g.ds(6));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashDataSetAndComponentsBelongingToSameExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2, data sets: DS3\n"
                + "DS1, components: DS2 DS3\n");

        deleteDataSets(g.ds(1));

        assertEquals("E2, data sets: DS3\n", renderGraph(g));
        assertModified(g.e(1));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashDataSetOfAContainerWhichBelongingToSameExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n");

        deleteDataSets(g.ds(2));

        assertEquals("E1, data sets: DS1\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.ds(1));
        assertDeleted(g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashDataSetOfAContainerWhichBelongingToSameExperimentButDifferentSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "S1, data sets: DS2\n"
                + "DS1, components: DS2\n");

        deleteDataSets(g.ds(2));

        assertEquals("E1, samples: S1, data sets: DS1\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertDeleted(g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public void testTrashDataSetsWithDataSetInAContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT]\n"
                + "DS2[NECT], components: DS3[NET]\n");

        deleteDataSets(g.ds(2));

        assertEquals("S1, data sets: DS1[NECT]\n", renderGraph(g));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertDeleted(g.ds(2), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public void testTrashDataSetsWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n");

        deleteDataSets(g.ds(1));

        assertEquals("S1, data sets: DS2[NECT] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertDeleted(g.ds(1), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }

    private void failTrashExperiment(ExperimentNode experimentNode, String expectedErrorMessage)
    {
        try
        {
            deleteExperiments(experimentNode);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(expectedErrorMessage,
                    ex.getMessage());
        }
    }

    private void failTrashSample(SampleNode sampleNode, String expectedErrorMessage)
    {
        String userSessionToken = createAdminUser();
        failTrashSample(userSessionToken, sampleNode, expectedErrorMessage);
    }

    private void failTrashSample(String userSessionToken, SampleNode sampleNode, String expectedErrorMessage)
    {
        try
        {
            deleteSamples(userSessionToken, sampleNode);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(expectedErrorMessage, ex.getMessage());
        }
    }

    protected String createExpectedErrorMessage(SampleNode relatedSample, EntityNode outsiderNode)
    {
        String relation = outsiderNode instanceof ExperimentNode ? "belongs to" : "is a component of";
        return "The sample " + entityGraphManager.getSample(relatedSample).getIdentifier()
                + " " + relation + " the " + render(outsiderNode) + " which is outside the deletion set.";
    }

    protected String createExpectedErrorMessage(EntityNode originalNode, EntityNode relatedEntity, EntityNode outsiderNode)
    {
        return "The " + render(originalNode)
                + " is a component of the " + render(relatedEntity)
                + " which belongs to the " + render(outsiderNode) + " which is outside the deletion set.";
    }

    protected String createExpectedErrorMessage(String userID, String... samples)
    {
        return "Authorization failure: Can not delete samples " + Arrays.asList(samples)
                + " because user " + userID + " does not have enough privileges.";
    }

    private String render(EntityNode entityNode)
    {
        String type;
        String identifier;
        if (entityNode instanceof ExperimentNode)
        {
            type = "experiment";
            identifier = entityGraphManager.getExperimentIdentifierOrNull((ExperimentNode) entityNode);
        } else if (entityNode instanceof SampleNode)
        {
            type = "sample";
            identifier = entityGraphManager.getSample((SampleNode) entityNode).getIdentifier();
        } else
        {
            type = "data set";
            identifier = entityGraphManager.getDataSetCodeOrNull((DataSetNode) entityNode);
        }
        return type + " " + identifier;
    }

    private void deleteExperiments(ExperimentNode... experimentNodes)
    {
        List<String> experimentIdentifiers = new ArrayList<String>();
        for (ExperimentNode experimentNode : experimentNodes)
        {
            experimentIdentifiers.add(entityGraphManager.getExperimentIdentifierOrNull(experimentNode));
        }
        deleteExperiments(experimentIdentifiers, createAdminUser());
        flushAndClearHibernateSession();
    }

    private void deleteSamples(SampleNode... sampleNodes)
    {
        deleteSamples(createAdminUser(), sampleNodes);
    }

    private void deleteSamples(String userSessionToken, SampleNode... sampleNodes)
    {
        List<String> samplePermIds = new ArrayList<String>();
        for (SampleNode sampleNode : sampleNodes)
        {
            samplePermIds.add(entityGraphManager.getSamplePermIdOrNull(sampleNode));
        }
        deleteSamples(samplePermIds, userSessionToken);
        flushAndClearHibernateSession();
    }

    private void deleteDataSets(DataSetNode... dataSetNodes)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (DataSetNode dataSetNode : dataSetNodes)
        {
            dataSetCodes.add(entityGraphManager.getDataSetCodeOrNull(dataSetNode));
        }
        deleteDataSets(dataSetCodes, createAdminUser());
        flushAndClearHibernateSession();
    }

    private String createAdminUser()
    {
        return create(aSession().withInstanceRole(RoleCode.ADMIN));
    }

    /**
     * Trashes specified experiments for specified user session token.
     */
    protected abstract void deleteExperiments(List<String> experimentIdentifiers, String userSessionToken);

    /**
     * Trashes specified samples for specified user session token.
     */
    protected abstract void deleteSamples(List<String> samplePermIds, String userSessionToken);

    /**
     * Trashes specified data sets for specified user session token.
     */
    protected abstract void deleteDataSets(List<String> dataSetCodes, String userSessionToken);

}
