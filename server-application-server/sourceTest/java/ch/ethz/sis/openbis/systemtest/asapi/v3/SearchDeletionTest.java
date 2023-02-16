/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.DeletedObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class SearchDeletionTest extends AbstractDeletionTest
{

    private Date beforeDate;

    @BeforeTest
    private void setBeforeDate()
    {
        // A deletion date is set by psql now() function which returns a start date of a database transaction. Therefore, all deletions
        // created in the tests below will have an earlier deletion date than even a time when a given test method is entered. To be able make
        // meaningful assertions in the tests, we construct a beforeDate here (i.e. before a database transaction is started).
        beforeDate = new Date();
    }

    @Test
    public void testSearchDeletionsWithIdWithOrOperator()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        fetchOptions.withDeletedObjects();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId1 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_1");
        SamplePermId sampleId2 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_2");
        SamplePermId sampleId3 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_3");

        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("It is just a test");

        IDeletionId deletionId1 = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId1), deletionOptions);
        IDeletionId deletionId2 = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId2), deletionOptions);
        v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId3), deletionOptions);

        assertSampleDoesNotExist(sampleId1);
        assertSampleDoesNotExist(sampleId2);
        assertSampleDoesNotExist(sampleId3);

        DeletionSearchCriteria criteria = new DeletionSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(deletionId1);
        criteria.withId().thatEquals(deletionId2);

        // When
        List<Deletion> objects = v3api.searchDeletions(sessionToken, criteria, fetchOptions).getObjects();

        // Then
        Collections.sort(objects, new SimpleComparator<Deletion, Long>()
            {
                @Override
                public Long evaluate(Deletion item)
                {
                    return ((ObjectTechId) item.getId()).getTechId();
                }
            });
        assertDeletions(objects, deletionId1, deletionId2);
        assertAttributes(objects.get(0).getDeletedObjects(), DeletedObject::getIdentifier, "/CISD/DEFAULT/SAMPLE_TO_DELETE_1");
        assertAttributes(objects.get(0).getDeletedObjects(), DeletedObject::getEntityTypeCode, "CELL_PLATE");
        assertAttributes(objects.get(0).getDeletedObjects(), DeletedObject::getEntityKind, EntityKind.SAMPLE);
        assertAttributes(objects.get(1).getDeletedObjects(), DeletedObject::getIdentifier, "/CISD/DEFAULT/SAMPLE_TO_DELETE_2");
        assertAttributes(objects.get(1).getDeletedObjects(), DeletedObject::getEntityTypeCode, "CELL_PLATE");
        assertAttributes(objects.get(1).getDeletedObjects(), DeletedObject::getEntityKind, EntityKind.SAMPLE);
    }

    private <T> void assertAttributes(List<DeletedObject> deletedObjects, Function<DeletedObject, T> mapper, 
            Object...expectedAttributes)
    {
        List<T> attributes = deletedObjects.stream().map(mapper).collect(Collectors.toList());
        assertEquals(attributes.toString(), Arrays.asList(expectedAttributes).toString());
    }

    @Test
    public void testSearchDeletionsWithIdWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId1 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_1");
        SamplePermId sampleId2 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_2");
        SamplePermId sampleId3 = createCisdSample(experimentId, "SAMPLE_TO_DELETE_3");

        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("It is just a test");

        IDeletionId deletionId1 = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId1), deletionOptions);
        IDeletionId deletionId2 = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId2), deletionOptions);
        v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId3), deletionOptions);

        assertSampleDoesNotExist(sampleId1);
        assertSampleDoesNotExist(sampleId2);
        assertSampleDoesNotExist(sampleId3);

        DeletionSearchCriteria criteria = new DeletionSearchCriteria();
        criteria.withAndOperator();
        criteria.withId().thatEquals(deletionId1);
        criteria.withId().thatEquals(deletionId2);

        SearchResult<Deletion> result = v3api.searchDeletions(sessionToken, criteria, fetchOptions);
        assertDeletions(result.getObjects());
    }

    @Test
    public void testSearchDeletionsWithoutDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Deletion latestDeletion = afterDeletions.get(afterDeletions.size() - 1);
        Assert.assertEquals(deletionId, latestDeletion.getId());
        Assert.assertEquals(deletionOptions.getReason(), latestDeletion.getReason());
        assertDeletionDate(latestDeletion);

        try
        {
            latestDeletion.getDeletedObjects();
            Assert.fail();
        } catch (NotFetchedException e)
        {
            // that's expected
        }
    }

    @Test
    public void testSearchDeletionsWithDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        fetchOptions.withDeletedObjects();
        List<Deletion> beforeDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertExperimentExists(experimentId);
        assertSampleExists(sampleId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Deletion latestDeletion = afterDeletions.get(afterDeletions.size() - 1);
        Assert.assertEquals(deletionId, latestDeletion.getId());
        Assert.assertEquals(deletionOptions.getReason(), latestDeletion.getReason());
        Assert.assertEquals(1, latestDeletion.getDeletedObjects().size());
        Assert.assertEquals(experimentId, latestDeletion.getDeletedObjects().get(0).getId());
        Assert.assertEquals(1, latestDeletion.getTotalExperimentsCount());
        Assert.assertEquals(1, latestDeletion.getTotalSamplesCount());
        Assert.assertEquals(0, latestDeletion.getTotalDataSetsCount());
        assertDeletionDate(latestDeletion);
    }

    @Test
    public void testSearchDeletionsWithUnauthorizedDeletion()
    {
        String spaceSessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.searchDeletions(spaceSessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        v3api.deleteExperiments(adminSessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);

        List<Deletion> afterDeletions = v3api.searchDeletions(spaceSessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionSearchCriteria c = new DeletionSearchCriteria();
        c.withId().thatEquals(new DeletionTechId(1L));

        DeletionFetchOptions fo = new DeletionFetchOptions();
        fo.withDeletedObjects();

        v3api.searchDeletions(sessionToken, c, fo);

        assertAccessLog(
                "search-deletions  SEARCH_CRITERIA:\n'DELETION\n    with id '1'\n'\nFETCH_OPTIONS:\n'Deletion\n    with DeletedObjects\n'");
    }

    private void assertDeletionDate(Deletion deletion)
    {
        Date afterDate = new Date();

        Assert.assertTrue("Before date: " + beforeDate + ", deletion date: " + deletion.getDeletionDate(),
                beforeDate.getTime() <= deletion.getDeletionDate().getTime());
        Assert.assertTrue("After date: " + afterDate + ", deletion date: " + deletion.getDeletionDate(),
                afterDate.getTime() >= deletion.getDeletionDate().getTime());
    }

}
