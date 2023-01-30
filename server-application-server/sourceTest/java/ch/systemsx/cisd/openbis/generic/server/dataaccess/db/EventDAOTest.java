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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.test.context.transaction.TestTransaction;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for {@link EventDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups = { "db", "event" })
public class EventDAOTest extends AbstractDAOTest
{

    private static final long SINCE_LONG = 10000;

    private static final long BEFORE = SINCE_LONG - 5;

    private static final long SINCE = SINCE_LONG;

    private static final long AFTER = SINCE_LONG + 5;

    private static final String KEEP_ME = "KEEP-ME";

    private static final String DELETE_ME = "DELETE-ME";

    private void assertCorrectResult(int numberOfDataSets, List<DeletedDataSet> result)
    {
        AssertJUnit.assertEquals(numberOfDataSets, result.size());
        for (DeletedDataSet d : result)
        {
            AssertJUnit.assertTrue(d.getIdentifier().startsWith(DELETE_ME));
        }
    }

    private List<DeletedDataSet> listDataDeletionEvents(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDateOrNull)
    {
        return daoFactory.getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull,
                maxDeletionDateOrNull);
    }

    private List<DeletedDataSet> listDataDeletionEvents(Long lastSeenDeletionEventIdOrNull)
    {
        return listDataDeletionEvents(lastSeenDeletionEventIdOrNull, null);
    }

    @Test
    public void testListDeletedDataSetsWithoutSinceDate() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER + 1);
        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {

            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
        }
        saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME, BEFORE);

        List<DeletedDataSet> result = listDataDeletionEvents(null);
        assertCorrectResult(numberOfDataSets + 1, result);
    }

    @Test
    public void testListSomeDataSets() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME, BEFORE);
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER + 1);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDataSetsWithNothingNew() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME, BEFORE);
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);
        assertCorrectResult(0, result);
    }

    @Test
    public void testListDataSetsAndNoSamples() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.SAMPLE, KEEP_ME, AFTER + 1);
        saveEvent(EventType.MOVEMENT, EntityType.SAMPLE, KEEP_ME, AFTER + 2);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 3 + i);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDeletedDataSetsAndNotCreated() throws Exception
    {
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER + 1);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDeletedDataSetsWithoutSince() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME + 1, BEFORE);
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME + 2, SINCE);
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);
        assertCorrectResult(0, result);
    }

    @Test
    public void testListDeletedDataSetsWithSinceDate() throws Exception
    {
        Date beforeDate = new Date(0);
        Date afterDate = new Date();
        Date queryDate = new Date(afterDate.getTime() / 2);

        saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + 1, AFTER + 1, beforeDate);
        saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + 2, AFTER + 2, beforeDate);
        saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + 3, AFTER + 3, afterDate);
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE, queryDate);
        assertCorrectResult(2, result);
    }

    @BeforeClass
    public void m()
    {
        System.out.println("ASDFads");
    }

    @BeforeMethod
    public void x()
    {
        System.out.println("ASDFads");
    }

    @Test
    // @Rollback
    public void testListDeletedDataSetsWithAContainerDataSet()
    {
        System.out.println("ACTIVE: " + TestTransaction.isActive());
        daoFactory.getDataDAO().delete(
                Arrays.asList(new TechId(13), new TechId(14), new TechId(15)),
                daoFactory.getPersonDAO().getByTechId(new TechId(1)),
                "Test deletion of data set container");
        List<DeletedDataSet> events = listDataDeletionEvents(null);
        Collections.sort(events, new Comparator<DeletedDataSet>()
            {
                @Override
                public int compare(DeletedDataSet s1, DeletedDataSet s2)
                {
                    return s1.getIdentifier().compareTo(s2.getIdentifier());
                }
            });
        assertEquals("[DeletedDataSet [identifier=20110509092359990-10], "
                + "DeletedDataSet [identifier=20110509092359990-11], "
                + "DeletedDataSet [identifier=20110509092359990-12]]", events.toString());
        assertEquals(null, events.get(0).getLocationOrNull());
        assertEquals("contained/20110509092359990-11", events.get(1).getLocationOrNull());
        assertEquals("contained/20110509092359990-12", events.get(2).getLocationOrNull());
    }

    private void saveEvent(EventType eventType, EntityType entityType, String identifiers,
            long eventId, Date date)
    {
        String description = eventType.name() + " " + entityType.name();
        PersonPE person = getSystemPerson();
        Long personId = HibernateUtils.getId(person);
        jdbcTemplate
                .update("insert into events "
                        + "(id, event_type, description, reason, pers_id_registerer, registration_timestamp, identifiers, entity_type) "
                        + "values(?, ?, ?, ?, ?, ?, ?, ?)", eventId, eventType.name(), description,
                        description, personId, date, identifiers, entityType.name());
    }

    private void saveEvent(EventType eventType, EntityType entityType, String identifier,
            long eventId)
    {
        saveEvent(eventType, entityType, identifier, eventId, new Date());
    }
}