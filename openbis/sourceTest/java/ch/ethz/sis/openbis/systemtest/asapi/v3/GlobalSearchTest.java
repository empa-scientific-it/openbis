/*
 * Copyright 2016 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * @author pkupczyk
 */
public class GlobalSearchTest extends AbstractTest
{

    private static final String TERM = "RAT";

    @Test
    public void testSearchWithNoCriteria()
    {
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, new GlobalSearchCriteria(),
                new GlobalSearchObjectFetchOptions());
        assertEquals(result.getTotalCount(), 0);
        assertTrue(result.getObjects().isEmpty());
    }

    @Test
    public void testSearchWithMatchesEmptyCriteria()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("  ");

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result.getTotalCount(), 0);
        assertTrue(result.getObjects().isEmpty());
    }

    @Test
    public void testSearchWithAuthorized()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("200902091219327-1025");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 1);

        GlobalSearchObject object = result.getObjects().get(0);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);
    }

    @Test
    public void testSearchSamplesUnauthorized()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("200902091219327-1025");

        SearchResult<GlobalSearchObject> result = search(TEST_SPACE_USER, criteria,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchExperimentsUnauthorized()
    {
        final GlobalSearchCriteria criteria1 = new GlobalSearchCriteria();
        criteria1.withText().thatMatches("200811050951882-1028");
        final SearchResult<GlobalSearchObject> result1 = search(TEST_USER, criteria1,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result1.getObjects().size(), 1);

        final GlobalSearchCriteria criteria2 = new GlobalSearchCriteria();
        criteria2.withText().thatMatches("200811050951882-1028");
        final SearchResult<GlobalSearchObject> result2 = search(TEST_SPACE_USER, criteria2,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result2.getObjects().size(), 0);
    }

    @Test
    public void testSearchDataSetsUnauthorized()
    {
        final GlobalSearchCriteria criteria1 = new GlobalSearchCriteria();
        criteria1.withText().thatMatches("20110509092359990-10");
        final SearchResult<GlobalSearchObject> result1 = search(TEST_USER, criteria1,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result1.getObjects().size(), 1);

        final GlobalSearchCriteria criteria2 = new GlobalSearchCriteria();
        criteria2.withText().thatMatches("20110509092359990-10");
        final SearchResult<GlobalSearchObject> result2 = search(TEST_SPACE_USER, criteria2,
                new GlobalSearchObjectFetchOptions());
        assertEquals(result2.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithOneMatchesOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertStuff(result);
    }

    @Test
    public void testSearchWithOneContainsOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);

        final Set<String> permIds = objects.stream().map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());

        assertTrue(permIds.contains("200902091219327-1025"));
        assertTrue(permIds.contains("200902091250077-1026"));
        assertTrue(permIds.contains("200902091225616-1027"));
    }

    @Test
    public void testSearchWithOneMatchesMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithOneContainsMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuffForContains(result);
    }

    @Test
    public void testSearchWithMultipleMatchesOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatMatches("simple   ");
        criteria.withText().thatMatches(" stuff");
        criteria.withText().thatMatches("  ");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatContains("simple");
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuffForContains(result);
    }

    @Test
    public void testSearchWithMultipleMatchesMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches(" simple  stuff");
        criteria.withText().thatMatches("stuff simple ");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithMultipleContainsMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");
        criteria.withText().thatMatches("stuff  simple");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertStuffForContains(result);
    }

    @Test
    public void testSearchWithOneContainsExactlyMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleStuffForContains(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyOneWord()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatContainsExactly("simple");
        criteria.withText().thatContainsExactly("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuffForContains(result);
    }

    @Test
    public void testSearchWithMultipleContainsExactlyMultipleWords()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuff");
        criteria.withText().thatContainsExactly("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 1);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "", false);
    }

    @Test
    public void testSearchWithMatchWithAndOperatorMatchingSameProperty()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.AND);
        criteria.withText().thatMatches("simple");
        criteria.withText().thatMatches("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleStuff(result);
    }

    @Test
    public void testSearchWithContainsWithAndOperatorMatchingDifferentProperties()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.AND);
        criteria.withText().thatContains("human");
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(result.getTotalCount(), 1);
        assertEquals(objects.size(), 1);

        final GlobalSearchObject object = objects.iterator().next();
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(object.getObjectPermId(), new SamplePermId("200902091219327-1025"));
        assertEquals(object.getObjectIdentifier(), new SampleIdentifier("/CISD/CP-TEST-1"));
    }

    @Test
    public void testSearchWithMatchWithOrOperator()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatMatches("simple");
        criteria.withText().thatMatches("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        assertSimpleOrStuff(result);
    }

    @Test
    public void testSearchWithContainsWithOrOperator()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);

        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 8);

        final GlobalSearchObject[] searchObjects = new GlobalSearchObject[] {
                findObjectByPermId(objects, "200902091219327-1025"),
                findObjectByPermId(objects, "200902091250077-1026"),
                findObjectByPermId(objects, "200902091225616-1027"),
                findObjectByPermId(objects, "200811050951882-1028"),
                findObjectByPermId(objects, "200811050952663-1029"),
                findObjectByPermId(objects, "200811050952663-1030"),
                findObjectByPermId(objects, "201108050937246-1031"),
                findObjectByPermId(objects, "HSV1 (VIRUS)"),
        };

        assertEquals(searchObjects[0].getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(searchObjects[0].getObjectPermId(), new SamplePermId("200902091219327-1025"));
        assertEquals(searchObjects[0].getObjectIdentifier(), new SampleIdentifier("/CISD/CP-TEST-1"));

        assertEquals(searchObjects[1].getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(searchObjects[1].getObjectPermId(), new SamplePermId("200902091250077-1026"));
        assertEquals(searchObjects[1].getObjectIdentifier(), new SampleIdentifier("/CISD/CP-TEST-2"));

        assertEquals(searchObjects[2].getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(searchObjects[2].getObjectPermId(), new SamplePermId("200902091225616-1027"));
        assertEquals(searchObjects[2].getObjectIdentifier(), new SampleIdentifier("/CISD/CP-TEST-3"));

        assertEquals(searchObjects[3].getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(searchObjects[3].getObjectPermId(), new ExperimentPermId("200811050951882-1028"));
        assertEquals(searchObjects[3].getObjectIdentifier(), new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertEquals(searchObjects[4].getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(searchObjects[4].getObjectPermId(), new ExperimentPermId("200811050952663-1029"));
        assertEquals(searchObjects[4].getObjectIdentifier(), new ExperimentIdentifier("/CISD/NEMO/EXP10"));

        assertEquals(searchObjects[5].getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(searchObjects[5].getObjectPermId(), new ExperimentPermId("200811050952663-1030"));
        assertEquals(searchObjects[5].getObjectIdentifier(), new ExperimentIdentifier("/CISD/NEMO/EXP11"));

        assertEquals(searchObjects[6].getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(searchObjects[6].getObjectPermId(), new ExperimentPermId("201108050937246-1031"));
        assertEquals(searchObjects[6].getObjectIdentifier(), new ExperimentIdentifier("/CISD/DEFAULT/EXP-Y"));

        assertEquals(searchObjects[7].getObjectKind(), GlobalSearchObjectKind.MATERIAL);
        assertEquals(searchObjects[7].getObjectPermId(), new MaterialPermId("HSV1", "VIRUS"));
        assertEquals(searchObjects[7].getObjectIdentifier(), new MaterialPermId("HSV1", "VIRUS"));
    }

    @Test
    public void testSearchWithObjectKindsSpecified()
    {
        GlobalSearchObject object;

        // experiment
        object = searchAndAssertOneOrNone("200811050951882-1028", true, GlobalSearchObjectKind.EXPERIMENT);
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028", true);

        object = searchAndAssertOneOrNone("200811050951882-1028", true, GlobalSearchObjectKind.SAMPLE);
        assertNull(object);

        // sample
        object = searchAndAssertOneOrNone("200902091219327-1025", true, GlobalSearchObjectKind.SAMPLE);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);

        object = searchAndAssertOneOrNone("200902091219327-1025", true, GlobalSearchObjectKind.DATA_SET);
        assertNull(object);

        // data set
        object = searchAndAssertOneOrNone("20081105092159111-1", true, GlobalSearchObjectKind.DATA_SET);
        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL, true);

        object = searchAndAssertOneOrNone("20081105092159111-1", true, GlobalSearchObjectKind.MATERIAL);
        assertNull(object);

        // material
        object = searchAndAssertOneOrNone("HSV1", true, GlobalSearchObjectKind.MATERIAL);
        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)", true);

        object = searchAndAssertOneOrNone("HSV1", true, GlobalSearchObjectKind.EXPERIMENT);
        assertNull(object);
    }

    @Test
    public void testSearchWithContainsWithObjectKindsSpecified()
    {
        GlobalSearchObject object;

        // experiment
        object = searchAndAssertOneOrNone("200811050951882-1028", false, GlobalSearchObjectKind.EXPERIMENT);
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", null, false);

        object = searchAndAssertOneOrNone("200811050951882-1028", false, GlobalSearchObjectKind.SAMPLE);
        assertNull(object);

        // sample
        object = searchAndAssertOneOrNone("200902091219327-1025", false, GlobalSearchObjectKind.SAMPLE);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", null, false);

        object = searchAndAssertOneOrNone("200902091219327-1025", false, GlobalSearchObjectKind.DATA_SET);
        assertNull(object);

        // data set
        object = searchAndAssertOneOrNone("20081105092159111-1", false, GlobalSearchObjectKind.DATA_SET);
        assertDataSet(object, "20081105092159111-1", null, DataSetKind.PHYSICAL, false);

        object = searchAndAssertOneOrNone("20081105092159111-1", false, GlobalSearchObjectKind.MATERIAL);
        assertNull(object);

        // material
        object = searchAndAssertOneOrNone("HSV1", false, GlobalSearchObjectKind.MATERIAL);
        assertMaterial(object, "HSV1", "VIRUS", null, false);

        object = searchAndAssertOneOrNone("HSV1", false, GlobalSearchObjectKind.EXPERIMENT);
        assertNull(object);
    }

    @Test
    public void testSearchWithObjectKindsNotSpecified()
    {
        GlobalSearchObject object;

        // experiment
        object = searchAndAssertOneOrNone("200811050951882-1028", true);
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028", true);

        // sample
        object = searchAndAssertOneOrNone("200902091219327-1025", true);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);

        // data set
        object = searchAndAssertOneOrNone("20081105092159111-1", true);
        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL, true);

        // material
        object = searchAndAssertOneOrNone("HSV1", true);
        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)", true);
    }

    @Test
    public void testSearchWithContainsWithObjectKindsNotSpecified()
    {
        GlobalSearchObject object;

        // experiment
        object = searchAndAssertOneOrNone("200811050951882-1028", false);
        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", null, false);

        // sample
        object = searchAndAssertOneOrNone("200902091219327-1025", false);
        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", null, false);

        // data set
        object = searchAndAssertOneOrNone("20081105092159111-1", false);
        assertDataSet(object, "20081105092159111-1", null, DataSetKind.PHYSICAL, false);

        // material
        object = searchAndAssertOneOrNone("HSV1", false);
        assertMaterial(object, "HSV1", "VIRUS", null, false);
    }

    @Test
    public void testSearchWithContainsAndWildCardsEnabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuf*");
        criteria.withWildCards();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);

        final Set<String> objectPermIds = objects.stream().map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());
        assertTrue(objectPermIds.contains("200902091219327-1025"));
        assertTrue(objectPermIds.contains("200902091250077-1026"));
        assertTrue(objectPermIds.contains("200902091225616-1027"));
    }

    @Test
    public void testSearchWithContainsAndWildCardsDisabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("stuf*");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithMatchesAndWildCards()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("stuf*");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithContainsExactlyAndWildCardsEnabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuf*");
        criteria.withWildCards();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 1);

        assertEquals(objects.get(0).getObjectPermId().toString(), "200902091250077-1026");
        assertEquals(objects.get(0).getObjectIdentifier().toString(), "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithContainsExactlyAndWildCardsDisabled()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContainsExactly("simple stuf*");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, new GlobalSearchObjectFetchOptions());
        assertEquals(result.getObjects().size(), 0);
    }

    @Test
    public void testSearchWithSortingByScoreAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().score();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getScore, true);
    }

    @Test
    public void testSearchWithSortingByScoreDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().score().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getScore, false);
    }

    @Test
    public void testSearchWithSortingByObjectKindAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, true);
    }

    @Test
    public void testSearchWithSortingByObjectKindDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, false);
    }

    @Test
    public void testSearchContainsWithSortingByObjectKindAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, true);
    }

    @Test
    public void testSearchContainsWithSortingByObjectKindDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectKind().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, GlobalSearchObject::getObjectKind, false);
    }

    @Test
    public void testSearchWithSortingByObjectPermIdAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), true);
    }

    @Test
    public void testSearchWithSortingByObjectPermIdDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), false);
    }

    // Sorting is not implemented yet.
    @Test(enabled = false)
    public void testSearchContainsWithSortingByObjectPermIdAsc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), true);
    }

    // Sorting is not implemented yet.
    @Test(enabled = false)
    public void testSearchContainsWithSortingByObjectPermIdDesc()
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId().desc();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectPermId().toString(), false);
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierAsc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), true);
    }

    @Test
    public void testSearchWithSortingByObjectIdentifierDesc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().desc();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), false);
    }

    // Sorting is not implemented yet.
    @Test(enabled = false)
    public void testSearchContainsWithSortingByObjectIdentifierAsc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), true);
    }

    // Sorting is not implemented yet.
    @Test(enabled = false)
    public void testSearchContainsWithSortingByObjectIdentifierDesc()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatContains("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().desc();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertSorted(objects, globalSearchObject -> globalSearchObject.getObjectIdentifier().toString(), false);
    }

    @Test
    public void testSearchWithSortingByMultipleFields()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        final GlobalSearchObjectSortOptions sortOptions = fo.sortBy();
        sortOptions.score().asc();
        sortOptions.objectKind().desc();
        sortOptions.objectPermId().asc();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        for (int index = 1, size = objects.size(); index < size; index++)
        {
            final GlobalSearchObject o1 = objects.get(index - 1);
            final GlobalSearchObject o2 = objects.get(index);
            final double value1 = o1.getScore();
            final double value2 = o2.getScore();
            final boolean scoresEqual = Math.abs(value1 - value2) < 0.0000001;
            if (scoresEqual)
            {
                final GlobalSearchObjectKind objectKind1 = o1.getObjectKind();
                final GlobalSearchObjectKind objectKind2 = o2.getObjectKind();

                if (objectKind1.equals(objectKind2))
                {
                    final IObjectId permId1 = o1.getObjectPermId();
                    final IObjectId permId2 = o2.getObjectPermId();

                    assertFalse(permId1.toString().compareTo(permId2.toString()) > 0,
                            String.format("Subsubordering is incorrect. [index=%d, permId1=%s, permId2=%s]",
                                    index, permId1, permId2));
                } else
                {
                    assertFalse(objectKind1.compareTo(objectKind2) < 0,
                            String.format("Subordering is incorrect. [index=%d, objectKind1=%s, objectKind2=%s]",
                                    index, objectKind1, objectKind2));
                }
            } else
            {
                assertFalse(value1 > value2, String.format("Ordering is incorrect. [index=%d, value1=%s, value2=%s]",
                        index, value1, value2));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertSorted(final List<GlobalSearchObject> globalSearchObjects,
            final Function<GlobalSearchObject, Comparable<?>> valueRetriever, final boolean ascending)
    {
        for (int index = 1, size = globalSearchObjects.size(); index < size; index++)
        {
            final Comparable<Object> value1 = (Comparable<Object>) valueRetriever.apply(
                    globalSearchObjects.get(index - 1));
            final Object value2 = valueRetriever.apply(globalSearchObjects.get(index));
            final int comparison = value1.compareTo(value2);
            assertFalse(ascending && comparison > 0 || !ascending && comparison < 0,
                    String.format("Ordering is incorrect. [index=%d, value1=%s, value2=%s]", index, value1, value2));
        }
    }

    @Test
    public void testSearchWithPagingSameProperty()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().asc();
        fo.from(3).count(2);
        fo.withMatch();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(result.getTotalCount(), 7);
        assertEquals(objects.size(), 2);

        assertExperiment(objects.get(0), "201108050937246-1031", "/CISD/DEFAULT/EXP-Y", "Property 'Description': A simple experiment", true);
        assertExperiment(objects.get(1), "200811050951882-1028", "/CISD/NEMO/EXP1", "Property 'Description': A simple experiment", true);
    }

    @Test
    public void testSearchThatContainsExactlyWithPagingSameProperty()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatContainsExactly("simple");
        criteria.withText().thatContainsExactly("stuff");

        final SearchResult<GlobalSearchObject> fullResult = search(TEST_USER, criteria,
                new GlobalSearchObjectFetchOptions());

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectIdentifier().asc();
        fo.from(3).count(2);
        fo.withMatch();

        final SearchResult<GlobalSearchObject> pagedResult = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> pagedObjects = pagedResult.getObjects();

        assertEquals(pagedResult.getTotalCount(), fullResult.getObjects().size());
        assertEquals(pagedObjects.size(), 2);

        for (int i = 0; i <= 1; i++)
        {
            assertEquals(pagedObjects.get(i).getObjectPermId().toString(),
                    fullResult.getObjects().get(3 + i).getObjectPermId().toString());
            assertEquals(pagedObjects.get(i).getObjectIdentifier().toString(),
                    fullResult.getObjects().get(3 + i).getObjectIdentifier().toString());
        }
    }

    @Test
    public void testSearchWithPagingDifferentProperties()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple male");

        final GlobalSearchObjectFetchOptions fo1 = new GlobalSearchObjectFetchOptions();
        fo1.from(1).count(2);
        fo1.withExperiment();
        fo1.withMatch();

        final SearchResult<GlobalSearchObject> searchResult = search(TEST_USER, criteria, fo1);
        final List<GlobalSearchObject> results = searchResult.getObjects();
        assertEquals(searchResult.getTotalCount(), 5);
        assertEquals(results.size(), 2);

        for (int i = 0; i < 2; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertEquals(globalSearchObject.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment"));
            assertTrue(globalSearchObject.getMatch().contains("Property 'Gender': MALE"));
            assertTrue(globalSearchObject.getScore() > 0);
        }
    }

    @Test
    public void testSearchWithPagingOffsetTooLarge()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("simple male");

        final GlobalSearchObjectFetchOptions fo1 = new GlobalSearchObjectFetchOptions();
        fo1.from(10).count(1);

        final SearchResult<GlobalSearchObject> searchResult = search(TEST_USER, criteria, fo1);
        final List<GlobalSearchObject> results = searchResult.getObjects();
        assertEquals(searchResult.getTotalCount(), 5);
        assertEquals(results.size(), 0);
    }

    @Test
    public void testSearchWithExperimentPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028", true);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithExperimentPermIdAndExperimentFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028", true);
        assertEquals(object.getExperiment().getCode(), "EXP1");
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithExperimentPermIdAndNonExperimentFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withSample();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200811050951882-1028", fo);

        assertExperiment(object, "200811050951882-1028", "/CISD/NEMO/EXP1", "Perm ID: 200811050951882-1028", true);
        assertExperimentNotFetched(object);
        assertNull(object.getSample());
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSampleCodeThatMatches()
    {
        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("B1B3:B01");

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 3);

        result.getObjects().forEach(object -> assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE));

        final Set<String> objectPermIds = result.getObjects().stream()
                .map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());
        assertEquals(objectPermIds, new HashSet<>(Arrays.asList("200811050924274-995", "200811050924274-996",
                "200811050924274-994")));

        final Set<String> objectIdentifiers = result.getObjects().stream()
                .map(object -> object.getObjectIdentifier().toString())
                .collect(Collectors.toSet());
        assertEquals(objectIdentifiers, new HashSet<>(Arrays.asList("/CISD/B1B3:B01", "/CISD/B1B3:B03", "/CISD/B1B3")));
    }

    @Test(enabled = false)
    public void testSearchWithSampleCodeThatContains()
    {
        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("B1B3:B01");

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 3);

        final GlobalSearchObject object = result.getObjects().get(0);

        assertSample(object, "200811050924274-995", "/CISD/B1B3:B01", "Code: B1B3:B01", true);
        assertNotNull(result.getObjects().get(1).getMatch());
        assertNotNull(result.getObjects().get(2).getMatch());
    }

    @Test
    public void testSearchWithSampleCodeOfContainer()
    {
        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("B1B3");

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertTrue(result.getTotalCount() >= 2);

        final List<GlobalSearchObject> resultObjects = result.getObjects();
        assertTrue(resultObjects.size() >= 2);

        final Set<String> permIds = resultObjects.stream().map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());
        assertTrue(permIds.containsAll(Arrays.asList("200811050924274-994", "200811050924274-995",
                "200811050924274-996")));

        final Set<String> identifiers = resultObjects.stream().map(object -> object.getObjectIdentifier().toString())
                .collect(Collectors.toSet());
        assertTrue(identifiers.containsAll(Arrays.asList("/CISD/B1B3", "/CISD/B1B3:B01", "/CISD/B1B3:B03")));

        final Set<String> matches = resultObjects.stream().map(GlobalSearchObject::getMatch)
                .collect(Collectors.toSet());
        assertEquals(matches, Collections.singleton("Code: B1B3"));
    }

    @Test
    public void testSearchWithSampleCodeOfComponent()
    {
        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        fo.sortBy().objectPermId().asc();
        fo.withMatch();

        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("A01");

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getObjects().size(), 4);

        assertSample(result.getObjects().get(0), "200811050919915-9", "/CISD/CL1:A01", "Code: A01", true);
        assertSample(result.getObjects().get(1), "200811050927630-1004", "/CISD/MP1-MIXED:A01", "Code: A01", true);
        assertSample(result.getObjects().get(2), "200811050928301-1009", "/CISD/MP2-NO-CL:A01", "Code: A01", true);
        assertSample(result.getObjects().get(3), "200811050944030-974", "/CISD/CL-3V:A01", "Code: A01", true);
    }

    @Test
    public void testSearchWithSamplePermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndSampleFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withSample();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);
        assertEquals(object.getSample().getCode(), "CP-TEST-1");
        assertExperimentNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndNonSampleFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "Perm ID: 200902091219327-1025", true);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithSamplePermIdAndNonSampleFetchedWithNoMatches()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();

        GlobalSearchObject object = searchAndAssertOne("200902091219327-1025", fo);

        assertSample(object, "200902091219327-1025", "/CISD/CP-TEST-1", "", true);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithDataSetPermId()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("20110509092359990-11");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertEquals(result.getTotalCount(), 1);

        final GlobalSearchObject object1 = result.getObjects().get(0);
        assertDataSet(object1, "20110509092359990-11", object1.getMatch(), DataSetKind.PHYSICAL, true);
        AssertionUtil.assertContains("Perm ID: 20110509092359990-11", object1.getMatch());
        assertEquals(object1.getDataSet().getCode(), "20110509092359990-11");
        assertExperimentNotFetched(object1);
        assertSampleNotFetched(object1);
        assertMaterialNotFetched(object1);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", null, true);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithDataSetPermIdAndDataSetFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", DataSetKind.PHYSICAL, true);
        assertEquals(object.getDataSet().getCode(), "20081105092159111-1");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchDataSetWithKindLink()
    {
        // given
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        // when
        GlobalSearchObject object = searchAndAssertOne("20120628092259000-23", fo);

        // then
        assertDataSet(object, "20120628092259000-23", "Perm ID: 20120628092259000-23", DataSetKind.LINK, true);
    }

    @Test
    public void testSearchWithDataSetPermIdAndNonDataSetFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("20081105092159111-1", fo);

        assertDataSet(object, "20081105092159111-1", "Perm ID: 20081105092159111-1", null, true);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("HSV1 (VIRUS)");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertFalse(result.getObjects().isEmpty());

        GlobalSearchObject object = result.getObjects().get(0);

        assertMaterial(object, "HSV1", "VIRUS",
                "Identifier: HSV1 (VIRUS)\nProperty 'Description': Herpes Simplex Virus 1", true);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMaterial();
        fo.withMatch();

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("HSV1 (VIRUS)");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertFalse(result.getObjects().isEmpty());

        GlobalSearchObject object = result.getObjects().get(0);

        assertMaterial(object, "HSV1", "VIRUS",
                "Identifier: HSV1 (VIRUS)\nProperty 'Description': Herpes Simplex Virus 1", true);
        assertEquals(object.getMaterial().getPermId().toString(), "HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialPermIdAndNonMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("HSV1 (VIRUS)");

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertFalse(result.getObjects().isEmpty());

        GlobalSearchObject object = result.getObjects().get(0);

        assertMaterial(object, "HSV1", "VIRUS",
                "Identifier: HSV1 (VIRUS)\nProperty 'Description': Herpes Simplex Virus 1", true);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndNothingFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)", true);
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMaterial();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)", true);
        assertEquals(object.getMaterial().getPermId().toString(), "HSV1 (VIRUS)");
        assertExperimentNotFetched(object);
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
    }

    @Test
    public void testSearchWithMaterialCodeAndNonMaterialFetched()
    {
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        GlobalSearchObject object = searchAndAssertOne("HSV1", fo);

        assertMaterial(object, "HSV1", "VIRUS", "Identifier: HSV1 (VIRUS)", true);
        assertNull(object.getExperiment());
        assertSampleNotFetched(object);
        assertDataSetNotFetched(object);
        assertMaterialNotFetched(object);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withOperator(SearchOperator.OR);
        criteria.withText().thatMatches("/CISD/DEFAULT/EXP-REUSE");
        criteria.withText().thatMatches("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        GlobalSearchObjectFetchOptions fetchOptions = new GlobalSearchObjectFetchOptions();

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        search(user.getUserId(), criteria, fetchOptions);
                    }
                });
        } else
        {
            SearchResult<GlobalSearchObject> result = search(user.getUserId(), criteria, fetchOptions);

            if (user.isInstanceUser())
            {
                assertEquals(result.getObjects().size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(result.getObjects().size(), 1);
                assertEquals(result.getObjects().get(0).getObjectIdentifier().toString(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(result.getObjects().size(), 0);
            }
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatMatches("200902091219327-1025");

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withSample();

        v3api.searchGlobally(sessionToken, c, fo);

        assertAccessLog(
                "search-globally  SEARCH_CRITERIA:\n'GLOBAL_SEARCH\n    any field matches '200902091219327-1025'\n'\nFETCH_OPTIONS:\n'GlobalSearchObject\n    with Sample\n    with DataSet\n'");
    }

    @Test
    public void testTextThatMatchesPermIdsAndCode()
    {
        final GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatMatches("200902091239077-1033 20110509092359990-11 200811050919915-8 VIRUS1");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withExperiment();
        fo.withSample();
        fo.withMaterial();
        fo.withMatch();

        final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
        assertEquals(results.size(), 4);

        results.forEach(result ->
        {
            switch (result.getObjectKind())
            {
                case DATA_SET:
                {
                    assertEquals(result.getObjectPermId().toString(), "20110509092359990-11");
                    assertEquals(result.getObjectIdentifier().toString(), "20110509092359990-11");
                    assertTrue(result.getMatch().contains("Perm ID: 20110509092359990-11"));
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertNull(result.getSample());
                    assertEquals(result.getDataSet().getCode(), "20110509092359990-11");
                    assertNull(result.getMaterial());
                    break;
                }
                case EXPERIMENT:
                {
                    assertEquals(result.getObjectPermId().toString(), "200902091239077-1033");
                    assertEquals(result.getObjectIdentifier().toString(), "/CISD/NEMO/EXP-TEST-1");
                    assertEquals(result.getMatch(), "Perm ID: 200902091239077-1033");
                    assertTrue(result.getScore() > 0);
                    assertEquals(result.getExperiment().getCode(), "EXP-TEST-1");
                    assertNull(result.getSample());
                    assertNull(result.getDataSet());
                    assertNull(result.getMaterial());
                    break;
                }
                case SAMPLE:
                {
                    assertEquals(result.getObjectPermId().toString(), "200811050919915-8");
                    assertEquals(result.getObjectIdentifier().toString(), "/CISD/CL1");
                    assertEquals(result.getMatch(), "Perm ID: 200811050919915-8");
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertEquals(result.getSample().getCode(), "CL1");
                    assertNull(result.getDataSet());
                    assertNull(result.getMaterial());
                    break;
                }
                case MATERIAL:
                {
                    final MaterialPermId materialPermId = (MaterialPermId) result.getObjectPermId();
                    final MaterialPermId materialObjectIdentifier = (MaterialPermId) result.getObjectIdentifier();
                    assertEquals(materialPermId.toString(), "VIRUS1 (VIRUS)");
                    assertEquals(materialPermId.getCode(), "VIRUS1");
                    assertEquals(materialPermId.getTypeCode(), "VIRUS");
                    assertEquals(materialObjectIdentifier.toString(), "VIRUS1 (VIRUS)");
                    assertEquals(materialObjectIdentifier.getCode(), "VIRUS1");
                    assertEquals(materialObjectIdentifier.getTypeCode(), "VIRUS");
                    assertEquals(result.getMatch(), "Identifier: VIRUS1 (VIRUS)");
                    assertTrue(result.getScore() > 0);
                    assertNull(result.getExperiment());
                    assertNull(result.getSample());
                    assertNull(result.getDataSet());
                    assertEquals(result.getMaterial().getCode(), "VIRUS1");
                    break;
                }
            }
        });
    }

    @Test
    public void testRanking()
    {
        final GlobalSearchCriteria c = new GlobalSearchCriteria();
        c.withText().thatMatches("simple male");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().score().desc();
        fo.withExperiment();
        fo.withMatch();

        final List<GlobalSearchObject> results = search(TEST_USER, c, fo).getObjects();
        assertEquals(results.size(), 5);

        assertTrue(results.get(0).getScore() > results.get(4).getScore());

        for (int i = 0; i < 3; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertEquals(globalSearchObject.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment"));
            assertTrue(globalSearchObject.getMatch().contains("Property 'Gender': MALE"));
            assertTrue(globalSearchObject.getScore() > 0);
        }

        for (int i = 3; i < 5; i++)
        {
            final GlobalSearchObject globalSearchObject = results.get(i);
            assertTrue(globalSearchObject.getObjectKind() == GlobalSearchObjectKind.EXPERIMENT ||
                    globalSearchObject.getObjectKind() == GlobalSearchObjectKind.SAMPLE);
            assertTrue(globalSearchObject.getMatch().contains("Property 'Description': A simple experiment") ||
                    globalSearchObject.getMatch().contains("Property 'Comment': extremely simple stuff"));
            assertTrue(globalSearchObject.getScore() > 0);
        }
    }

    @Test
    public void testCharacterCases()
    {
        final GlobalSearchCriteria c1 = new GlobalSearchCriteria();
        c1.withText().thatMatches("simple male");

        final GlobalSearchCriteria c2 = new GlobalSearchCriteria();
        c2.withText().thatMatches("SIMPLE MALE");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withExperiment();
        fo.withMatch();

        final List<GlobalSearchObject> results1 = search(TEST_USER, c1, fo).getObjects();
        final List<GlobalSearchObject> results2 = search(TEST_USER, c2, fo).getObjects();
        assertEquals(results1.size(), results2.size());

        for (int i = 0; i < results1.size(); i++)
        {
            final GlobalSearchObject result1 = results1.get(i);
            final GlobalSearchObject result2 = results2.get(i);

            assertEquals(result1.getObjectKind(), result2.getObjectKind());
            assertEquals(result1.getObjectPermId(), result2.getObjectPermId());
            assertEquals(result1.getObjectIdentifier(), result2.getObjectIdentifier());
            assertEquals(result1.getMatch(), result2.getMatch());
            assertEquals(result1.getScore(), result2.getScore());

            assertEquals((result1.getExperiment() != null) ? result1.getExperiment().getPermId() : null,
                    (result2.getExperiment() != null) ? result2.getExperiment().getPermId() : null);
        }
    }

    @Test
    public void testComplexScoreSortingForSamples() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        List<SampleCreation> creations = getSampleCreationsForTest(TERM);
        List<SamplePermId> identifiers = v3api.createSamples(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatMatches(TERM);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withSample();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = filterSearchResults(search(TEST_USER, c, fo).getObjects(),
                    false, true, false);
            assertEquals(results.size(), 4);
            assertEquals(results.get(0).getSample().getCode(), TERM);
            assertEquals(results.get(1).getSample().getCode(), TERM + "2");
            assertEquals(results.get(2).getSample().getCode(), TERM + "3");
        } finally
        {
            /* Cleanup */
            cleanupSamplesForTest(v3api, sessionToken, identifiers);
        }
    }

    @Test
    public void testComplexScoreSortingForExperiments() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        List<ExperimentCreation> creations = getExperimentCreationsForTest(TERM);
        List<ExperimentPermId> identifiers = v3api.createExperiments(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatMatches(TERM);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withExperiment();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = filterSearchResults(search(TEST_USER, c, fo).getObjects(),
                    false, false, true);
            assertEquals(results.size(), 4);
            assertEquals(results.get(0).getExperiment().getCode(), TERM);
            assertEquals(results.get(1).getExperiment().getCode(), TERM + "2");
            assertEquals(results.get(2).getExperiment().getCode(), TERM + "3");
        } finally
        {
            /* Cleanup */
            cleanupExperinmentsForTest(v3api, sessionToken, identifiers);
        }
    }

    @Test
    public void testComplexScoreSortingForDataSets() {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        List<DataSetCreation> creations = getDataSetsCreationsForTest(TERM);
        List<DataSetPermId> identifiers = v3api.createDataSets(sessionToken, creations);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatMatches(TERM);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withDataSet();
            fo.sortBy().score().desc();

            final List<GlobalSearchObject> results = filterSearchResults(search(TEST_USER, c, fo).getObjects(),
                    true, false, false);
            assertEquals(results.size(), 4);
            assertEquals(results.get(0).getDataSet().getCode(), TERM);
            assertEquals(results.get(1).getDataSet().getCode(), TERM + "2");
            assertEquals(results.get(2).getDataSet().getCode(), TERM + "3");
        } finally
        {
            /* Cleanup */
            cleanupDataSetsForTest(v3api, sessionToken, identifiers);
        }
    }

    /**
     * Often failure in this test indicates that full text search main SQL script has not been executed.
     */
    @Test
    public void testComplexScoreSortingForAll()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        /* Setup */
        List<SampleCreation> creationsS = getSampleCreationsForTest(TERM);
        List<SamplePermId> identifiersS = v3api.createSamples(sessionToken, creationsS);
        List<ExperimentCreation> creationsE = getExperimentCreationsForTest(TERM);
        List<ExperimentPermId> identifiersE = v3api.createExperiments(sessionToken, creationsE);
        List<DataSetCreation> creationsD = getDataSetsCreationsForTest(TERM);
        List<DataSetPermId> identifiersD = v3api.createDataSets(sessionToken, creationsD);

        try
        {
            /* Test */
            final GlobalSearchCriteria c = new GlobalSearchCriteria();
            c.withText().thatMatches(TERM);

            final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
            fo.withSample();
            fo.withExperiment();
            fo.withDataSet();
            fo.sortBy().score().desc();
            fo.sortBy().objectKind().desc();

            final List<GlobalSearchObject> results = filterSearchResults(search(TEST_USER, c, fo).getObjects(),
                    true, true, true);
            assertEquals(results.size(), 12);

            /*
             * When the same Score is given, DataSets come before Samples that come before Experiments
             */

            assertEquals(results.get(0).getDataSet().getCode(), TERM);
            assertEquals(results.get(1).getSample().getCode(), TERM);
            assertEquals(results.get(2).getExperiment().getCode(), TERM);

            assertEquals(results.get(3).getDataSet().getCode(), TERM + "2");
            assertEquals(results.get(4).getSample().getCode(), TERM + "2");
            assertEquals(results.get(5).getExperiment().getCode(), TERM + "2");

            assertEquals(results.get(6).getDataSet().getCode(), TERM + "3");
            assertEquals(results.get(7).getSample().getCode(), TERM + "3");
            assertEquals(results.get(8).getExperiment().getCode(), TERM + "3");
        } finally
        {
            /* Cleanup */
            cleanupSamplesForTest(v3api, sessionToken, identifiersS);
            cleanupExperinmentsForTest(v3api, sessionToken, identifiersE);
            cleanupDataSetsForTest(v3api, sessionToken, identifiersD);
        }
    }

    @Test
    public void testSearchMatchingMaterialProperty()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("BACTERIUM-Y");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId().asc();
        fo.withMatch();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(result.getTotalCount(), 4);
        assertEquals(objects.size(), 4);

        assertSample(objects.get(0), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'bacterium': BACTERIUM-Y", true);
        assertSample(objects.get(1), "200902091250077-1051", "/CISD/PLATE_WELLSEARCH:WELL-A01",
                "Property 'bacterium': BACTERIUM-Y", true);
        assertExperiment(objects.get(2), "201108050937246-1031", "/CISD/DEFAULT/EXP-Y",
                "Property 'any_material': BACTERIUM-Y", true);
        assertMaterial(objects.get(3), "BACTERIUM-Y", "BACTERIUM", "Identifier: BACTERIUM-Y (BACTERIUM)", true);
    }

    @Test
    public void testSearchMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final String propertyTypeValue = "/CISD/CL1";
        final String sampleCode = "TEST-SAMPLE-FULL-TEXT-SEARCH";
        final PropertyTypePermId propertyTypeId = createASamplePropertyType(sessionToken, null);
        createSample(sessionToken, propertyTypeId, propertyTypeValue, sampleCode);

        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches(propertyTypeValue);

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.sortBy().objectPermId().asc();
        fo.withMatch();

        final SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        final List<GlobalSearchObject> objects = result.getObjects();

        assertEquals(result.getTotalCount(), 2);
        assertEquals(objects.size(), 2);

        assertSample(objects.get(0), "200811050919915-8", propertyTypeValue, "Identifier: /CISD/CL1", true);
        assertSample(objects.get(1), null, "/CISD/" + sampleCode, "Property 'label': " + propertyTypeValue, true);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Cannot combine matches and contains criteria in global search.*")
    public void testCriteriaMixing()
    {
        final GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches("stuff");
        criteria.withText().thatContains("stuff");

        final GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withMatch();
        search(TEST_USER, criteria, fo);
    }

    private ObjectPermId createSample(final String sessionToken, final PropertyTypePermId propertyTypeId,
            final String value, final String code)
    {
        final EntityTypePermId entityTypeId = createASampleType(sessionToken, false, propertyTypeId);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(false);
        sampleCreation.setCode(code);
        sampleCreation.setTypeId(entityTypeId);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty(propertyTypeId.getPermId(), value);
        return v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation)).get(0);
    }

    private List<GlobalSearchObject> filterSearchResults(final List<GlobalSearchObject> results,
            final boolean withDataset, final boolean withSample, final boolean withExperiment)
    {
        return results.stream().filter(globalSearchObject ->
                        withDataset && globalSearchObject.getDataSet() != null &&
                                globalSearchObject.getDataSet().getCode().startsWith(TERM) ||
                        withSample && globalSearchObject.getSample() != null &&
                                globalSearchObject.getSample().getCode().startsWith(TERM) ||
                        withExperiment && globalSearchObject.getExperiment() != null &&
                                globalSearchObject.getExperiment().getCode().startsWith(TERM)
                ).collect(Collectors.toList());
    }

    private static DataSetCreation getDataSetCreationForTest(String code, String description, String organism) {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER"));
        creation.setComponentIds(Arrays.asList(new DataSetPermId("DATASET-TO-DELETE")));
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        if (description != null) {
            creation.setProperty("DESCRIPTION", description);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static List<DataSetCreation> getDataSetsCreationsForTest(String term) {
        List<DataSetCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getDataSetCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getDataSetCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getDataSetCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getDataSetCreationForTest(term + "4", term, null));

        return creations;
    }

    private static void cleanupDataSetsForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends IDataSetId> identifiers) {
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private static ExperimentCreation getExperimentCreationForTest(String code, String description, String organism) {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        if (description != null) {
            creation.setProperty("DESCRIPTION", description);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static List<ExperimentCreation> getExperimentCreationsForTest(String term) {
        List<ExperimentCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getExperimentCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getExperimentCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getExperimentCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getExperimentCreationForTest(term + "4", term, null));

        return creations;
    }

    private static void cleanupExperinmentsForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends IExperimentId> identifiers) {
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private static List<SampleCreation> getSampleCreationsForTest(String term) {
        List<SampleCreation> creations = new ArrayList<>();

        // Score 1000 + 100 + 1 = 1101
        creations.add(getSampleCreationForTest(term, term, term));
        // Score 100 + 1 = 101
        creations.add(getSampleCreationForTest(term + "2", term, term));
        // Score 100 = 100
        creations.add(getSampleCreationForTest(term + "3", null, term));
        // Score 1 = 1
        creations.add(getSampleCreationForTest(term + "4", term, null));

        return creations;
    }

    private static SampleCreation getSampleCreationForTest(String code, String comment, String organism) {
        SampleCreation creation = new SampleCreation();
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        if (comment != null) {
            creation.setProperty("COMMENT", comment);
        }
        if (organism != null) {
            creation.setProperty("ORGANISM", organism);
        }
        return creation;
    }

    private static void cleanupSamplesForTest(IApplicationServerInternalApi v3api, String sessionToken, List<? extends ISampleId> identifiers) {
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("Test Cleanup");
        IDeletionId deletionId = v3api.deleteSamples(sessionToken, identifiers, deletionOptions);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
    }

    private SearchResult<GlobalSearchObject> search(String user, GlobalSearchCriteria criteria, GlobalSearchObjectFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<GlobalSearchObject> result = v3api.searchGlobally(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return result;
    }

    private GlobalSearchObject searchAndAssertOne(String permId, GlobalSearchObjectFetchOptions fetchOptions)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        criteria.withText().thatMatches(permId);

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fetchOptions);
        assertEquals(result.getObjects().size(), 1);

        return result.getObjects().get(0);
    }

    private GlobalSearchObject searchAndAssertOneOrNone(final String permId, final boolean useThatMatches,
            final GlobalSearchObjectKind... objectKinds)
    {
        GlobalSearchCriteria criteria = new GlobalSearchCriteria();
        if (useThatMatches)
        {
            criteria.withText().thatMatches(permId);
        } else
        {
            criteria.withText().thatContainsExactly(permId);
        }
        criteria.withObjectKind().thatIn(objectKinds);

        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();
        fo.withDataSet();
        fo.withMatch();

        SearchResult<GlobalSearchObject> result = search(TEST_USER, criteria, fo);
        assertTrue(result.getObjects().size() <= 1);

        return result.getObjects().isEmpty() ? null : result.getObjects().get(0);
    }

    private void assertStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);
        final GlobalSearchObject obj1 = findObjectByPermId(objects, "200902091219327-1025");
        final GlobalSearchObject obj2 = findObjectByPermId(objects, "200902091250077-1026");
        final GlobalSearchObject obj3 = findObjectByPermId(objects, "200902091225616-1027");

        assertSample(obj1, "200902091219327-1025", "/CISD/CP-TEST-1", "Property 'Comment': very advanced stuff", true);
        assertSample(obj2, "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff", true);
        assertSample(obj3, "200902091225616-1027", "/CISD/CP-TEST-3", "Property 'Comment': stuff like others", true);
    }

    private void assertStuffForContains(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 3);

        objects.forEach(object -> assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE));

        final Set<String> permIds = objects.stream().map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());
        assertEquals(permIds, new HashSet<>(Arrays.asList("200902091219327-1025", "200902091250077-1026",
                "200902091225616-1027")));
        final Set<String> identifiers = objects.stream().map(object -> object.getObjectIdentifier().toString())
                .collect(Collectors.toSet());
        assertEquals(identifiers, new HashSet<>(Arrays.asList("/CISD/CP-TEST-1", "/CISD/CP-TEST-2",
                "/CISD/CP-TEST-3")));
    }

    private void assertSimpleStuff(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(result.getTotalCount(), 1);
        assertEquals(objects.size(), 1);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        assertSample(iter.next(), "200902091250077-1026", "/CISD/CP-TEST-2", "Property 'Comment': extremely simple stuff", true);
    }

    private void assertSimpleStuffForContains(SearchResult<GlobalSearchObject> result)
    {
        List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 1);
        Iterator<GlobalSearchObject> iter = objects.iterator();

        GlobalSearchObject object = iter.next();
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(object.getObjectPermId(), new SamplePermId("200902091250077-1026"));
        assertEquals(object.getObjectIdentifier(), new SampleIdentifier("/CISD/CP-TEST-2"));
    }

    private void assertSimpleOrStuff(final SearchResult<GlobalSearchObject> result)
    {
        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 7);

        // Even though we have 8 results, one of them has two matches. Therefore, we need just 7 search objects.
        final GlobalSearchObject[] searchObjects = new GlobalSearchObject[] {
                findObjectByPermId(objects, "200902091219327-1025"),
                findObjectByPermId(objects, "200902091250077-1026"),
                findObjectByPermId(objects, "200902091225616-1027"),
                findObjectByPermId(objects, "201108050937246-1031"),
                findObjectByPermId(objects, "200811050951882-1028"),
                findObjectByPermId(objects, "200811050952663-1029"),
                findObjectByPermId(objects, "200811050952663-1030"),
        };

        assertSample(searchObjects[0], "200902091219327-1025", "/CISD/CP-TEST-1",
                "Property 'Comment': very advanced stuff", true);
        assertSample(searchObjects[1], "200902091250077-1026", "/CISD/CP-TEST-2",
                "Property 'Comment': extremely simple stuff", true);
        assertSample(searchObjects[2], "200902091225616-1027", "/CISD/CP-TEST-3",
                "Property 'Comment': stuff like others", true);
        assertExperiment(searchObjects[3], "201108050937246-1031", "/CISD/DEFAULT/EXP-Y",
                "Property 'Description': A simple experiment", true);
        assertExperiment(searchObjects[4], "200811050951882-1028", "/CISD/NEMO/EXP1",
                "Property 'Description': A simple experiment", true);
        assertExperiment(searchObjects[5], "200811050952663-1029", "/CISD/NEMO/EXP10",
                "Property 'Description': A simple experiment", true);
        assertExperiment(searchObjects[6], "200811050952663-1030", "/CISD/NEMO/EXP11",
                "Property 'Description': A simple experiment", true);
    }

    private void assertSimpleOrStuffForContains(final SearchResult<GlobalSearchObject> result)
    {
        final List<GlobalSearchObject> objects = result.getObjects();
        assertEquals(objects.size(), 8);

        final Set<String> objectPermIds = objects.stream().map(object -> object.getObjectPermId().toString())
                .collect(Collectors.toSet());
        assertEquals(objectPermIds, new HashSet<>(Arrays.asList("200902091219327-1025", "200902091250077-1026",
                "200902091225616-1027", "201108050937246-1031", "200811050951882-1028", "200811050952663-1029",
                "200811050952663-1030", "HSV1 (VIRUS)")));

        final Set<String> objectIdentifiers = objects.stream().map(object -> object.getObjectIdentifier().toString())
                .collect(Collectors.toSet());
        assertEquals(objectIdentifiers, new HashSet<>(Arrays.asList("/CISD/CP-TEST-1", "/CISD/CP-TEST-2",
                "/CISD/CP-TEST-3", "/CISD/DEFAULT/EXP-Y", "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10",
                "/CISD/NEMO/EXP11", "HSV1 (VIRUS)")));
    }

    /**
     * Searches for an object with specified perm ID.
     * @param objects collection of objects to search in.
     * @param permId perm ID to search by.
     * @return the first found object with the perm ID or {@code null} if none is found.
     */
    private GlobalSearchObject findObjectByPermId(final Collection<GlobalSearchObject> objects, final String permId)
    {
        return objects.stream().filter((obj) -> obj.getObjectPermId().toString().equals(permId)).limit(1).findFirst()
                .orElse(null);
    }

    private void assertExperiment(final GlobalSearchObject object, final String permId, final String identifier,
            final String match, final boolean checkMatchAndScore)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(object.getObjectPermId(), new ExperimentPermId(permId));
        assertEquals(object.getObjectIdentifier(), new ExperimentIdentifier(identifier));

        if (checkMatchAndScore)
        {
            assertEquals(object.getMatch(), match);
            assertTrue(object.getScore() > 0);
        }
    }

    private void assertSample(final GlobalSearchObject object, final String permId, final String identifier,
            final String match, final boolean checkMatchAndScore)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.SAMPLE);

        if (permId != null)
        {
            assertEquals(object.getObjectPermId(), new SamplePermId(permId));
        }

        assertEquals(object.getObjectIdentifier(), new SampleIdentifier(identifier));

        if (checkMatchAndScore)
        {
            assertEquals(object.getMatch(), match);
            assertTrue(object.getScore() > 0);
        }
    }

    private void assertDataSet(final GlobalSearchObject object, final String code, final String match,
            final DataSetKind dataSetKind, final boolean checkMatchAndScore)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.DATA_SET);
        assertEquals(object.getObjectPermId(), new DataSetPermId(code));
        assertEquals(object.getObjectIdentifier(), new DataSetPermId(code));

        if (checkMatchAndScore)
        {
            assertEquals(object.getMatch(), match);
            assertTrue(object.getScore() > 0);
        }

        if (dataSetKind != null)
        {
            assertEquals(object.getDataSet().getKind(), dataSetKind);
        }
    }

    private void assertMaterial(final GlobalSearchObject object, final String code, final String typeCode,
            final String match, final boolean checkMatchAndScore)
    {
        assertNotNull(object);
        assertEquals(object.getObjectKind(), GlobalSearchObjectKind.MATERIAL);
        assertEquals(object.getObjectPermId(), new MaterialPermId(code, typeCode));
        assertEquals(object.getObjectIdentifier(), new MaterialPermId(code, typeCode));

        if (checkMatchAndScore)
        {
            assertEquals(object.getMatch(), match);
            assertTrue(object.getScore() > 0);
        }
    }

    private void assertExperimentNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getExperiment();
                }
            });
    }

    private void assertSampleNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getSample();
                }
            });
    }

    private void assertDataSetNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getDataSet();
                }
            });
    }

    private void assertMaterialNotFetched(final GlobalSearchObject object)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    object.getMaterial();
                }
            });
    }

}
