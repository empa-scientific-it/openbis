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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Test cases for the {@link SampleListingWorker}.
 * 
 * @author Bernd Rinn
 * @author Piotr Buczek
 */
public class SampleListingWorkerTest extends AbstractDAOTest
{

    private final String BASE_INDEX_URL = "baseIndexURL";

    private final String SPACE_CODE = "CISD";

    private final long CONTAINER_ID = 997L;

    private final long PARENT1_ID = 979L;

    private final long GRANDPARENT1_ID = 325L;

    private final long PARENT2_ID = 976L;

    private final long GRANDPARENT2_ID = 3L;

    private final long CHILD_WITH_2_PARENTS_ID = 987L;

    private final Long[] CHILDREN_IDS =
    { 986L, CHILD_WITH_2_PARENTS_ID, 988L, 989L };

    private SampleListerDAO sampleListerDAO;

    private SecondaryEntityDAO secondaryDAO;

    @BeforeMethod
    public void beforeMethod()
    {
        sampleListerDAO = SampleListingQueryTest.createSampleListerDAO(daoFactory);
        secondaryDAO = SecondaryEntityDAO.create(daoFactory);
    }

    @Test
    public void testListSamplesBySpace()
    {
        final ListSampleCriteria baseCriteria = new ListSampleCriteria();
        baseCriteria.setSpaceCode(SPACE_CODE);
        baseCriteria.setIncludeSpace(true);
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            if (Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0)
            {
                checkParentsAndGeneratedFrom(s);
            }
        }
    }

    @Test()
    public void testListSamplesById()
    {
        final LongSet sampleIds = new LongOpenHashSet();
        sampleIds.addAll(Arrays.asList(CHILDREN_IDS));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(sampleIds);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertEquals(CHILDREN_IDS.length, list.size());
        for (Sample s : list)
        {
            assertTrue(Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0);
            checkSpace(s);
            checkParentsAndGeneratedFrom(s);
        }
    }

    @Test
    public void testListSamplesForContainer()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForContainer(new TechId(CONTAINER_ID));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            assertNotNull("ID:" + s.getId(), s.getContainer());
            assertEquals("ID:" + s.getId(), CONTAINER_ID, s.getContainer().getId().longValue());
        }
    }

    @Test()
    public void testListSamplesForParent()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForParent(new TechId(PARENT1_ID));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertEquals(CHILDREN_IDS.length, list.size());
        for (Sample s : list)
        {
            assertTrue(Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0);
            checkSpace(s);
            checkParentsAndGeneratedFrom(s);
        }
    }

    @Test()
    public void testListSamplesForChild()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForChild(new TechId(CHILD_WITH_2_PARENTS_ID));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertEquals(2, list.size());
        for (Sample s : list)
        {
            long sId = s.getId().longValue();
            long pId = s.getGeneratedFrom().getId().longValue();
            if (sId == PARENT1_ID)
            {
                assertTrue(pId == GRANDPARENT1_ID);
            } else if (sId == PARENT2_ID)
            {
                assertTrue(pId == GRANDPARENT2_ID);
            } else
            {
                fail("unexpected parent id " + pId);
            }
            checkSpace(s);
        }
    }

    @Test()
    public void testListSamplesForChildren()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForChildren(Arrays.asList(CHILDREN_IDS));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertEquals(2, list.size());
        for (Sample s : list)
        {
            long sId = s.getId().longValue();
            long pId = s.getGeneratedFrom().getId().longValue();
            if (sId == PARENT1_ID)
            {
                assertTrue(pId == GRANDPARENT1_ID);
            } else if (sId == PARENT2_ID)
            {
                assertTrue(pId == GRANDPARENT2_ID);
            } else
            {
                fail("unexpected parent id " + pId);
            }
            checkSpace(s);
        }
    }

    @Test()
    public void testListSamplesForExperiment()
    {
        final long expId = 3L;
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForExperiment(new TechId(expId));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO,
                        null);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            assertNotNull("ID:" + s.getId(), s.getExperiment());
            assertEquals("ID:" + s.getId(), expId, s.getExperiment().getId().longValue());
        }
    }

    private void checkSpace(Sample s)
    {
        assertNotNull("ID:" + s.getId(), s.getSpace());
        assertEquals("ID:" + s.getId(), SPACE_CODE, s.getSpace().getCode());
    }

    private void checkParentsAndGeneratedFrom(Sample s)
    {
        if (s.getId().equals(CHILD_WITH_2_PARENTS_ID))
        {
            checkParents(s, Arrays.asList(PARENT1_ID, PARENT2_ID));
            checkGetGeneratedFromFail(s);
        } else
        {
            checkGeneratedFrom(s, PARENT1_ID);
        }
    }

    private void checkGetGeneratedFromFail(Sample s)
    {
        boolean fail = false;
        try
        {
            s.getGeneratedFrom();
        } catch (IllegalStateException e)
        {
            // expected
        }
        if (fail)
        {
            fail("Expected IllegalStateException");
        }
    }

    private void checkParents(Sample s, List<Long> expectedParentIds)
    {
        assertEquals(expectedParentIds.size(), s.getParents().size());
        for (Sample parent : s.getParents())
        {
            expectedParentIds.contains(parent.getId());
            assertTrue("ID:" + s.getId(), expectedParentIds.contains(parent.getId()));
        }
    }

    private void checkGeneratedFrom(Sample s, Long expectedParentId)
    {
        assertNotNull("ID:" + s.getId(), s.getGeneratedFrom());
        assertEquals("ID:" + s.getId(), expectedParentId, s.getGeneratedFrom().getId());
    }
}
