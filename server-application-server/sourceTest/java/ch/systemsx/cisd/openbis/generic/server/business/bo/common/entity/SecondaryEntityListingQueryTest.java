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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ ExperimentProjectSpaceCodeRecord.class, ISecondaryEntityListingQuery.class,
        SecondaryEntityDAO.class })
@Test(groups =
{ "db", "misc" })
public class SecondaryEntityListingQueryTest extends AbstractDAOTest
{

    private ExperimentPE firstExperiment;

    private PersonPE firstPerson;

    private SecondaryEntityDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        firstExperiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        firstPerson = daoFactory.getPersonDAO().getPerson(1);
        dao = createSecondaryEntityDAO(daoFactory);
    }

    public static SecondaryEntityDAO createSecondaryEntityDAO(IDAOFactory daoFactory)
    {
        ISecondaryEntityListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, ISecondaryEntityListingQuery.class);
        return SecondaryEntityDAO.create(daoFactory, query);
    }

    @Test
    public void testGetExperiment()
    {
        Experiment expFull = dao.tryGetExperiment(firstExperiment.getId());
        assertEquals(firstExperiment.getCode(), expFull.getCode());
        ProjectPE project = firstExperiment.getProject();
        assertEquals(project.getCode(), expFull.getProject().getCode());
        assertEquals(project.getSpace().getCode(), expFull.getProject().getSpace().getCode());
        assertEquals(firstExperiment.getEntityType().getCode(), expFull.getEntityType().getCode());
    }

    @Test
    public void testPerson()
    {
        Person person = dao.getPerson(firstPerson.getId());
        assertEquals(firstPerson.getFirstName(), person.getFirstName());
    }

    @Test
    public void testSamples()
    {
        Long2ObjectMap<Sample> samples = dao.getSamples(EntityListingTestUtils.createSet(1, 2));
        assertTrue(samples.size() > 0);
    }

    // id of a sample with a few levels of descendants
    private final static TechId SAMPLE_ROOT_ID = new TechId(1008L);

    @Test
    public void testListSamples()
    {
        LongSet idsOfSelfAndDescendants = dao.getSampleDescendantIdsAndSelf(SAMPLE_ROOT_ID.getId());
        SamplePE rootSample = daoFactory.getSampleDAO().getByTechId(SAMPLE_ROOT_ID);
        Set<SamplePE> descendantsAndSelf = new HashSet<SamplePE>();
        checkContainsDescendantIdsAndSelf(rootSample, idsOfSelfAndDescendants, descendantsAndSelf);
        assertEquals(descendantsAndSelf.size(), idsOfSelfAndDescendants.size());
        assertEquals(8, idsOfSelfAndDescendants.size());
    }

    /**
     * recursively checks that parent id and all ids of its descendants are among given ids
     * 
     * @param visitedSamples - collection of samples visited so far
     */
    private static void checkContainsDescendantIdsAndSelf(SamplePE parent, LongSet ids,
            Set<SamplePE> visitedSamples)
    {
        if (visitedSamples.contains(parent))
        {
            return;
        }
        visitedSamples.add(parent);
        assertTrue(parent.getId() + " not found among " + Arrays.toString(ids.toLongArray()),
                ids.contains(parent.getId()));
        for (SampleRelationshipPE r : parent.getChildRelationships())
        {
            SamplePE childPE = r.getChildSample();
            checkContainsDescendantIdsAndSelf(childPE, ids, visitedSamples);
        }
    }
}
