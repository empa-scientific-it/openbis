/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EventType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.task.events_search.DataSource;
import ch.systemsx.cisd.openbis.generic.server.task.events_search.EventsSearchMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.task.events_search.Statistics;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * @author pkupczyk
 */
public class SearchEventTest extends AbstractTest
{

    private Date startDate;

    private Space spaceA;

    private Space spaceB;

    private Space spaceC;

    private Space spaceD;

    private Project projectAA;

    private Project projectBB;

    private Project projectCC;

    private Experiment experimentAAA;

    private Experiment experimentBBB;

    private Experiment experimentBBC;

    @BeforeClass
    public void beforeClass()
    {
        startDate = new Date();

        // Some dates within events are stored without milliseconds (e.g. entity registration date).
        // We sleep for 1 second to simplify test methods that search using dates and the startDate.

        ConcurrencyUtilities.sleep(1000);

        executeInTransaction(new TransactionCallback<Void>()
        {
            @Override public Void doInTransaction(final TransactionStatus status)
            {
                // process all existing events
                EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(new TestDataSource());
                task.execute();

                // create test data
                initSpaces();
                initProjects();
                initExperiments();

                String sessionToken = v3api.login(TEST_USER, PASSWORD);

                // generate new events (they will be created in a different transaction than the events
                // created in beforeMethod and therefore will have a different registration date)

                deleteSpaces(sessionToken, Collections.singletonList(spaceD.getPermId()), "delete spaces");

                return null;
            }
        }, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @AfterClass
    public void afterClass()
    {
        executeInTransaction(new TransactionCallback<Void>()
        {
            @Override public Void doInTransaction(final TransactionStatus status)
            {
                String sessionToken = v3api.login(TEST_USER, PASSWORD);

                // clean up test data
                deleteExperiments(sessionToken, Arrays.asList(experimentAAA.getPermId(), experimentBBB.getPermId(), experimentBBC.getPermId()),
                        "clean up experiments");
                deleteProjects(sessionToken, Arrays.asList(projectAA.getPermId(), projectBB.getPermId(), projectCC.getPermId()), "clean up projects");
                deleteSpaces(sessionToken, Arrays.asList(spaceA.getPermId(), spaceB.getPermId(), spaceC.getPermId()), "clean up spaces");

                return null;
            }
        }, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        executeInTransaction(new TransactionCallback<Void>()
        {
            @Override public Void doInTransaction(final TransactionStatus status)
            {
                String sessionToken = v3api.login(TEST_USER, PASSWORD);
                String systemSessionToken = v3api.loginAsSystem();

                // generate new events (they will be created in a different transaction than the events
                // created in beforeClass and therefore will have a different registration date)

                freezeExperiment(systemSessionToken, experimentAAA.getPermId());
                deleteExperiments(sessionToken, Arrays.asList(experimentBBB.getPermId(), experimentBBC.getPermId()), "delete experiments");
                deleteProjects(sessionToken, Arrays.asList(projectBB.getPermId(), projectCC.getPermId()), "delete projects");
                deleteSpaces(sessionToken, Collections.singletonList(spaceB.getPermId()), "delete spaces");

                // process new events
                EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(new TestDataSource());
                task.execute();

                return null;
            }
        }, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    private void initSpaces()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation spaceACreation = new SpaceCreation();
        spaceACreation.setCode("EVENT_TEST_SPACE_A_" + System.currentTimeMillis());

        SpaceCreation spaceBCreation = new SpaceCreation();
        spaceBCreation.setCode("EVENT_TEST_SPACE_B_" + System.currentTimeMillis());

        SpaceCreation spaceCCreation = new SpaceCreation();
        spaceCCreation.setCode("EVENT_TEST_SPACE_C_" + System.currentTimeMillis());

        SpaceCreation spaceDCreation = new SpaceCreation();
        spaceDCreation.setCode("EVENT_TEST_SPACE_D_" + System.currentTimeMillis());

        List<SpacePermId> spaceIds = v3api.createSpaces(sessionToken, Arrays.asList(spaceACreation, spaceBCreation, spaceCCreation, spaceDCreation));

        Map<ISpaceId, Space> spaceMap = v3api.getSpaces(sessionToken, spaceIds, new SpaceFetchOptions());
        spaceA = spaceMap.get(spaceIds.get(0));
        spaceB = spaceMap.get(spaceIds.get(1));
        spaceC = spaceMap.get(spaceIds.get(2));
        spaceD = spaceMap.get(spaceIds.get(3));
    }

    private void initProjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation projectAACreation = new ProjectCreation();
        projectAACreation.setCode("EVENT_TEST_PROJECT_A_" + System.currentTimeMillis());
        projectAACreation.setSpaceId(spaceA.getPermId());

        ProjectCreation projectBBCreation = new ProjectCreation();
        projectBBCreation.setCode("EVENT_TEST_PROJECT_B_" + System.currentTimeMillis());
        projectBBCreation.setSpaceId(spaceB.getPermId());

        ProjectCreation projectCCCreation = new ProjectCreation();
        projectCCCreation.setCode("EVENT_TEST_PROJECT_C_" + System.currentTimeMillis());
        projectCCCreation.setSpaceId(spaceC.getPermId());

        List<ProjectPermId> projectIds = v3api.createProjects(sessionToken, Arrays.asList(projectAACreation, projectBBCreation, projectCCCreation));

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();
        fo.withRegistrator();

        Map<IProjectId, Project> projectMap = v3api.getProjects(sessionToken, projectIds, fo);
        projectAA = projectMap.get(projectIds.get(0));
        projectBB = projectMap.get(projectIds.get(1));
        projectCC = projectMap.get(projectIds.get(2));
    }

    private void initExperiments()
    {
        String systemSessionToken = v3api.loginAsSystem();

        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode("EVENT_TEST_EXPERIMENT_TYPE_" + System.currentTimeMillis());
        List<EntityTypePermId> experimentTypeIds = v3api.createExperimentTypes(systemSessionToken, Collections.singletonList(experimentTypeCreation));

        ExperimentCreation experimentAAACreation = new ExperimentCreation();
        experimentAAACreation.setTypeId(experimentTypeIds.get(0));
        experimentAAACreation.setCode("EVENT_TEST_EXPERIMENT_A_" + System.currentTimeMillis());
        experimentAAACreation.setProjectId(projectAA.getPermId());

        ExperimentCreation experimentBBBCreation = new ExperimentCreation();
        experimentBBBCreation.setTypeId(experimentTypeIds.get(0));
        experimentBBBCreation.setCode("EVENT_TEST_EXPERIMENT_B_" + System.currentTimeMillis());
        experimentBBBCreation.setProjectId(projectBB.getPermId());

        ExperimentCreation experimentBBCCreation = new ExperimentCreation();
        experimentBBCCreation.setTypeId(experimentTypeIds.get(0));
        experimentBBCCreation.setCode("EVENT_TEST_EXPERIMENT_C_" + System.currentTimeMillis());
        experimentBBCCreation.setProjectId(projectBB.getPermId());

        List<ExperimentPermId> experimentIds =
                v3api.createExperiments(systemSessionToken, Arrays.asList(experimentAAACreation, experimentBBBCreation, experimentBBCCreation));

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProject().withSpace();
        fo.withRegistrator();

        Map<IExperimentId, Experiment> experimentMap = v3api.getExperiments(systemSessionToken, experimentIds, fo);
        experimentAAA = experimentMap.get(experimentIds.get(0));
        experimentBBB = experimentMap.get(experimentIds.get(1));
        experimentBBC = experimentMap.get(experimentIds.get(2));
    }

    @Test
    public void testSearchWithEmptyCriteria()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<Event> result = v3api.searchEvents(sessionToken, new EventSearchCriteria(), new EventFetchOptions());
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 7);
    }

    @Test
    public void testSearchWithEventType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEventType().thatEquals(EventType.FREEZING);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 1);
        assertExperimentFreezing(events.get(0), experimentAAA);
    }

    @Test
    public void testSearchWithEntityType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntityType().thatEquals(EntityType.EXPERIMENT);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 3);

        assertExperimentFreezing(events.get(0), experimentAAA);
        assertExperimentDeletion(events.get(1), experimentBBB);
        assertExperimentDeletion(events.get(2), experimentBBC);

    }

    @Test
    public void testSearchWithEntitySpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntitySpace().thatEquals(spaceB.getCode());

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 4);

        assertProjectDeletion(events.get(0), projectBB);
        assertExperimentDeletion(events.get(1), experimentBBB);
        assertExperimentDeletion(events.get(2), experimentBBC);
        assertSpaceDeletion(events.get(3), spaceB);
    }

    @Test
    public void testSearchWithEntitySpaceId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntitySpaceId().thatEquals(spaceC.getId().toString());

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 1);

        assertProjectDeletion(events.get(0), projectCC);
    }

    @Test
    public void testSearchWithEntityProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntityProject().thatEquals(projectBB.getIdentifier().getIdentifier());

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 3);

        assertProjectDeletion(events.get(0), projectBB);
        assertExperimentDeletion(events.get(1), experimentBBB);
        assertExperimentDeletion(events.get(2), experimentBBC);
    }

    @Test
    public void testSearchWithEntityProjectId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntityProjectId().thatEquals(projectCC.getPermId().getPermId());

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 1);

        assertProjectDeletion(events.get(0), projectCC);
    }

    @Test
    public void testSearchWithEntityRegistrationDate()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // >= start date
        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntityRegistrationDate().thatIsLaterThanOrEqualTo(startDate);

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, new EventFetchOptions());
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 4); // space deletions and freezes do not have the entity registration date

        // > now
        criteria = new EventSearchCriteria();
        criteria.withEntityRegistrationDate().thatIsLaterThanOrEqualTo(new Date());

        result = v3api.searchEvents(sessionToken, criteria, new EventFetchOptions());
        events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 0);
    }

    @Test
    public void testSearchWithEntityRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withEntityRegistrator().thatEquals(SYSTEM_USER);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 2); // space deletions and freezes do not have the entity registrator

        assertExperimentDeletion(events.get(0), experimentBBB);
        assertExperimentDeletion(events.get(1), experimentBBC);
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // >= start date
        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withRegistrationDate().thatIsLaterThanOrEqualTo(startDate);

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, new EventFetchOptions());
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 7);

        // > now
        criteria = new EventSearchCriteria();
        criteria.withRegistrationDate().thatIsLaterThanOrEqualTo(new Date());

        result = v3api.searchEvents(sessionToken, criteria, new EventFetchOptions());
        events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 0);
    }

    @Test
    public void testSearchWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals(SYSTEM_USER);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 1);

        assertExperimentFreezing(events.get(0), experimentAAA);
    }

    @Test
    public void testSearchWithSortingById()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().id();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 7);

        assertSpaceDeletion(events.get(0), spaceD);
        assertSpaceDeletion(events.get(1), spaceB);
        // we cannot assert on the exact order of project and experiment deletions as entities deleted
        // together are stored in random order in the original event table in JSON content column
        assertExperimentFreezing(events.get(6), experimentAAA);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 7);

        assertExperimentFreezing(events.get(0), experimentAAA); // experiment freeze has experiment identifier stored as identifier value
        assertProjectDeletion(events.get(1), projectBB); // project deletions have project perm id stored as identifier value
        assertProjectDeletion(events.get(2), projectCC);
        assertExperimentDeletion(events.get(3), experimentBBB); // experiment deletions have experiment perm id stored as identifier value
        assertExperimentDeletion(events.get(4), experimentBBC);
        assertSpaceDeletion(events.get(5), spaceB); // space deletions have space code stored as identifier value
        assertSpaceDeletion(events.get(6), spaceD);
    }

    @Test
    public void testSearchWithSortingByRegistrationDate()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().registrationDate();
        // all events created in one transaction have the same registration timestamp
        // therefore we need to also sort by identifier to have a stable order
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 7);

        assertSpaceDeletion(events.get(0), spaceD); // it is first as it was created in a different transaction in beforeClass method
        assertExperimentFreezing(events.get(1), experimentAAA);
        assertProjectDeletion(events.get(2), projectBB);
        assertProjectDeletion(events.get(3), projectCC);
        assertExperimentDeletion(events.get(4), experimentBBB);
        assertExperimentDeletion(events.get(5), experimentBBC);
        assertSpaceDeletion(events.get(6), spaceB);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withAndOperator();
        criteria.withEntityProject().thatEquals(projectBB.getIdentifier().getIdentifier());
        criteria.withEntityType().thatEquals(EntityType.EXPERIMENT);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 2);

        assertExperimentDeletion(events.get(0), experimentBBB);
        assertExperimentDeletion(events.get(1), experimentBBC);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.withOrOperator();
        criteria.withEntityProject().thatEquals(projectBB.getIdentifier().getIdentifier());
        criteria.withEntityType().thatEquals(EntityType.EXPERIMENT);

        EventFetchOptions fo = new EventFetchOptions();
        fo.withRegistrator();
        fo.sortBy().identifier();

        SearchResult<Event> result = v3api.searchEvents(sessionToken, criteria, fo);
        List<Event> events = getEventsAfterDate(result, startDate);

        assertEquals(events.size(), 4);

        assertExperimentFreezing(events.get(0), experimentAAA);
        assertProjectDeletion(events.get(1), projectBB);
        assertExperimentDeletion(events.get(2), experimentBBB);
        assertExperimentDeletion(events.get(3), experimentBBC);
    }

    @Test
    public void testSearchWithUnauthorized()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        EventSearchCriteria criteria = new EventSearchCriteria();
        EventFetchOptions fo = new EventFetchOptions();

        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override public void execute()
            {
                v3api.searchEvents(sessionToken, criteria, fo);
            }
        });
    }

    private static class TestDataSource extends DataSource
    {

        @Override public Statistics executeInNewTransaction(final TransactionCallback<?> callback)
        {
            // for testing execute everything in the main transaction instead of starting a new transaction
            return executeInTransaction(callback, TransactionDefinition.PROPAGATION_REQUIRED);
        }
    }

    private static Statistics executeInTransaction(TransactionCallback<?> callback, int propagation)
    {
        PlatformTransactionManager manager = CommonServiceProvider.getApplicationContext().getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(propagation);
        definition.setReadOnly(false);
        TransactionTemplate template = new TransactionTemplate(manager, definition);
        template.execute(callback);
        return new Statistics();
    }

    private void freezeExperiment(String sessionToken, IExperimentId id)
    {
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(id);
        update.freeze();
        v3api.updateExperiments(sessionToken, Collections.singletonList(update));
    }

    private void deleteExperiments(String sessionToken, List<IExperimentId> ids, String reason)
    {
        ExperimentDeletionOptions experimentOptions = new ExperimentDeletionOptions();
        experimentOptions.setReason(reason);
        IDeletionId experimentDeletionId = v3api.deleteExperiments(sessionToken, ids, experimentOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(experimentDeletionId));
    }

    private void deleteProjects(String sessionToken, List<IProjectId> ids, String reason)
    {
        ProjectDeletionOptions projectOptions = new ProjectDeletionOptions();
        projectOptions.setReason(reason);
        v3api.deleteProjects(sessionToken, ids, projectOptions);
    }

    private void deleteSpaces(String sessionToken, List<ISpaceId> ids, String reason)
    {
        SpaceDeletionOptions spaceOptions = new SpaceDeletionOptions();
        spaceOptions.setReason(reason);
        v3api.deleteSpaces(sessionToken, ids, spaceOptions);
    }

    private static void assertDateAfter(Date date, Date startDate)
    {
        assertTrue(date.compareTo(startDate) >= 0, "Date: " + date + ", start date: " + startDate);
    }

    private static void assertDateEquals(Date date, Date expectedDate)
    {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals(format.format(date), format.format(expectedDate));
    }

    private void assertExperimentFreezing(Event event, Experiment experiment)
    {
        assertNotNull(event.getId());
        assertEquals(event.getEventType(), EventType.FREEZING);
        assertEquals(event.getEntityType(), EntityType.EXPERIMENT);
        assertNull(event.getEntitySpace());
        assertNull(event.getEntitySpaceId());
        assertNull(event.getEntityProject());
        assertNull(event.getEntityProjectId());
        assertNull(event.getEntityRegistrator());
        assertNull(event.getEntityRegistrationDate());
        assertEquals(event.getIdentifier(), experiment.getIdentifier().getIdentifier());
        assertNull(event.getDescription());
        assertEquals(event.getReason(), "[\"freeze\"]");
        assertNull(event.getContent());
        assertEquals(event.getRegistrator().getUserId(), SYSTEM_USER);
        assertDateAfter(event.getRegistrationDate(), startDate);
    }

    private void assertExperimentDeletion(Event event, Experiment experiment)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> spaces =
                v3api.getSpaces(sessionToken, Arrays.asList(experiment.getProject().getSpace().getPermId()), new SpaceFetchOptions());

        assertNotNull(event.getId());
        assertEquals(event.getEventType(), EventType.DELETION);
        assertEquals(event.getEntityType(), EntityType.EXPERIMENT);
        assertEquals(event.getEntitySpace(), experiment.getProject().getSpace().getCode());

        if (spaces.size() > 0)
        {
            assertEquals(event.getEntitySpaceId(), experiment.getProject().getSpace().getId());
        } else
        {
            assertNull(event.getEntitySpaceId());
        }

        assertEquals(event.getEntityProject(), experiment.getProject().getIdentifier().getIdentifier());
        assertEquals(event.getEntityProjectId(), experiment.getProject().getPermId());
        assertEquals(event.getEntityRegistrator(), experiment.getRegistrator().getUserId());
        assertDateEquals(event.getEntityRegistrationDate(), experiment.getRegistrationDate());
        assertEquals(event.getIdentifier(), experiment.getPermId().getPermId());
        assertEquals(event.getDescription(), experiment.getPermId().getPermId());
        assertEquals(event.getReason(), "delete experiments");
        assertTrue(event.getContent().contains(experiment.getCode()));
        assertEquals(event.getRegistrator().getUserId(), TEST_USER);
        assertDateAfter(event.getRegistrationDate(), startDate);
    }

    private void assertProjectDeletion(Event event, Project project)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> spaces = v3api.getSpaces(sessionToken, Arrays.asList(project.getSpace().getPermId()), new SpaceFetchOptions());

        assertNotNull(event.getId());
        assertEquals(event.getEventType(), EventType.DELETION);
        assertEquals(event.getEntityType(), EntityType.PROJECT);
        assertEquals(event.getEntitySpace(), project.getSpace().getCode());

        if (spaces.size() > 0)
        {
            assertEquals(event.getEntitySpaceId(), project.getSpace().getId());
        } else
        {
            assertNull(event.getEntitySpaceId());
        }

        assertEquals(event.getEntityProject(), project.getIdentifier().getIdentifier());
        assertEquals(event.getEntityProjectId(), project.getPermId());
        assertEquals(event.getEntityRegistrator(), project.getRegistrator().getUserId());
        assertDateEquals(event.getEntityRegistrationDate(), project.getRegistrationDate());
        assertEquals(event.getIdentifier(), project.getPermId().getPermId());
        assertEquals(event.getDescription(), project.getIdentifier().getIdentifier());
        assertEquals(event.getReason(), "delete projects");
        assertTrue(event.getContent().contains(project.getCode()));
        assertEquals(event.getRegistrator().getUserId(), TEST_USER);
        assertDateAfter(event.getRegistrationDate(), startDate);
    }

    private void assertSpaceDeletion(Event event, Space space)
    {
        assertNotNull(event.getId());
        assertEquals(event.getEventType(), EventType.DELETION);
        assertEquals(event.getEntityType(), EntityType.SPACE);
        assertEquals(event.getEntitySpace(), space.getCode());
        assertNull(event.getEntitySpaceId());
        assertNull(event.getEntityProject());
        assertNull(event.getEntityProjectId());
        assertNull(event.getEntityRegistrator());
        assertNull(event.getEntityRegistrationDate());
        assertEquals(event.getIdentifier(), space.getCode());
        assertEquals(event.getDescription(), space.getCode());
        assertEquals(event.getReason(), "delete spaces");
        assertNull(event.getContent());
        assertEquals(event.getRegistrator().getUserId(), TEST_USER);
        assertDateAfter(event.getRegistrationDate(), startDate);
    }

    private static List<Event> getEventsAfterDate(SearchResult<Event> result, Date startDate)
    {
        return result.getObjects().stream().filter(e -> e.getRegistrationDate().compareTo(startDate) >= 0).collect(Collectors.toList());
    }

}
