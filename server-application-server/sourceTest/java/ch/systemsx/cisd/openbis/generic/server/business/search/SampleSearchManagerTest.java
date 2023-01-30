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

package ch.systemsx.cisd.openbis.generic.server.business.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public class SampleSearchManagerTest extends AssertJUnit
{
    private static final String USER_ID = "test";

    private Mockery context;

    private IHibernateSearchDAO searchDAO;

    private ISampleLister sampleLister;

    private SampleSearchManager searchManager;

    @BeforeMethod
    public void setUpMocks()
    {
        context = new Mockery();
        searchDAO = context.mock(IHibernateSearchDAO.class);
        sampleLister = context.mock(ISampleLister.class);
        searchManager = new SampleSearchManager(searchDAO, sampleLister);
    }

    @AfterMethod
    public void assertIsSatisfied()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testSamplesRestrictedByParentSamplesMoreParentsThanChildren()
    {
        DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
        searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        DetailedSearchCriterion c1 =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE),
                        "A");
        searchCriteria.setCriteria(Arrays.asList(c1));
        DetailedSearchCriteria parentSearchCriteria = new DetailedSearchCriteria();
        DetailedSearchCriterion c2 =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE),
                        "B");
        parentSearchCriteria.setCriteria(Arrays.asList(c2));
        searchCriteria.addSubCriteria(new DetailedSearchSubCriteria(
                AssociatedEntityKind.SAMPLE_PARENT, parentSearchCriteria));
        final RecordingMatcher<DetailedSearchCriteria> mainCriteriaMatcher =
                new RecordingMatcher<DetailedSearchCriteria>();
        final RecordingMatcher<DetailedSearchCriteria> parentCriteriaMatcher =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(searchDAO).getResultSetSizeLimit();
                    will(returnValue(100));

                    one(searchDAO).searchForEntityIds(with(USER_ID), with(mainCriteriaMatcher),
                            with(EntityKind.SAMPLE));
                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L))));
                    one(searchDAO).searchForEntityIds(with(USER_ID), with(parentCriteriaMatcher),
                            with(EntityKind.SAMPLE));
                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L))));

                    one(sampleLister).getChildToParentsIdsMap(Arrays.asList(1L, 2L, 3L, 4L));
                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    result.put(1L, new HashSet<Long>(Arrays.asList(4L, 6L)));
                    result.put(2L, new HashSet<Long>(Arrays.asList(4L, 3L)));
                    result.put(3L, new HashSet<Long>(Arrays.asList(7L, 8L)));
                    will(returnValue(result));
                }
            });

        List<Long> sampleIds =
                new ArrayList<Long>(searchManager.searchForSampleIDs(USER_ID, searchCriteria));

        Collections.sort(sampleIds);
        assertEquals("[1, 2]", sampleIds.toString());
        assertEquals("ATTRIBUTE SAMPLE_TYPE: A, [SAMPLE_PARENT: ATTRIBUTE SAMPLE_TYPE: B] "
                + "(without wildcards)", mainCriteriaMatcher.recordedObject().toString());
        assertEquals("ATTRIBUTE SAMPLE_TYPE: B (without wildcards)", parentCriteriaMatcher
                .recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testSamplesRestrictedByParentSamplesMoreChildrenThanParents()
    {
        DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
        searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        DetailedSearchCriterion c1 =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE),
                        "A");
        searchCriteria.setCriteria(Arrays.asList(c1));
        DetailedSearchCriteria parentSearchCriteria = new DetailedSearchCriteria();
        DetailedSearchCriterion c2 =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE),
                        "B");
        parentSearchCriteria.setCriteria(Arrays.asList(c2));
        searchCriteria.addSubCriteria(new DetailedSearchSubCriteria(
                AssociatedEntityKind.SAMPLE_PARENT, parentSearchCriteria));
        final RecordingMatcher<DetailedSearchCriteria> mainCriteriaMatcher =
                new RecordingMatcher<DetailedSearchCriteria>();
        final RecordingMatcher<DetailedSearchCriteria> parentCriteriaMatcher =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(searchDAO).getResultSetSizeLimit();
                    will(returnValue(100));

                    one(searchDAO).searchForEntityIds(with(USER_ID), with(mainCriteriaMatcher),
                            with(EntityKind.SAMPLE));
                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L))));
                    one(searchDAO).searchForEntityIds(with(USER_ID), with(parentCriteriaMatcher),
                            with(EntityKind.SAMPLE));
                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L))));

                    one(sampleLister).getParentToChildrenIdsMap(Arrays.asList(1L, 2L, 3L, 4L));
                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    result.put(3L, new HashSet<Long>(Arrays.asList(2L)));
                    result.put(4L, new HashSet<Long>(Arrays.asList(1L, 2L)));
                    will(returnValue(result));
                }
            });

        List<Long> sampleIds =
                new ArrayList<Long>(searchManager.searchForSampleIDs(USER_ID, searchCriteria));

        Collections.sort(sampleIds);
        assertEquals("[1, 2]", sampleIds.toString());
        assertEquals("ATTRIBUTE SAMPLE_TYPE: A, [SAMPLE_PARENT: ATTRIBUTE SAMPLE_TYPE: B] "
                + "(without wildcards)", mainCriteriaMatcher.recordedObject().toString());
        assertEquals("ATTRIBUTE SAMPLE_TYPE: B (without wildcards)", parentCriteriaMatcher
                .recordedObject().toString());
        context.assertIsSatisfied();
    }
}
