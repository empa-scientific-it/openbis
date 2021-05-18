package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventsSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class DataSource implements IDataSource
{

    private static final int LOG_LIMIT = 10;

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final Stack<Statistics> statisticsStack = new Stack<>();

    @Override public Statistics executeInNewTransaction(TransactionCallback<?> callback)
    {
        Statistics statistics = null;

        try
        {
            statistics = statisticsStack.push(new Statistics());
            PlatformTransactionManager manager = CommonServiceProvider.getApplicationContext().getBean(PlatformTransactionManager.class);
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            definition.setReadOnly(false);
            TransactionTemplate template = new TransactionTemplate(manager, definition);
            template.execute(callback);
            return statistics;
        } finally
        {
            if (statistics != null)
            {
                statisticsStack.pop();
            }
        }
    }

    @Override
    public List<SpacePE> loadSpaces(List<String> codes)
    {
        ISpaceDAO spaceDAO = CommonServiceProvider.getDAOFactory().getSpaceDAO();
        List<SpacePE> result = spaceDAO.tryFindSpaceByCodes(codes);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseLoadedSpaces(result.size());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found " + result.size() + " space(s) for code(s): " + CollectionUtils.abbreviate(codes, LOG_LIMIT));
        }

        return result;
    }

    @Override
    public List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<IProjectId, Project> result = v3.getProjects(sessionToken, ids, fo);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseLoadedProjects(result.size());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found " + result.size() + " project(s) for id(s): " + CollectionUtils.abbreviate(ids, LOG_LIMIT));
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<IExperimentId, Experiment> result = v3.getExperiments(sessionToken, ids, fo);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseLoadedExperiments(result.size());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found " + result.size() + " experiment(s) for id(s): " + CollectionUtils.abbreviate(ids, LOG_LIMIT));
        }

        return new ArrayList<>(result.values());
    }

    @Override public List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<ISampleId, Sample> result = v3.getSamples(sessionToken, ids, fo);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseLoadedSamples(result.size());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found " + result.size() + " sample(s) for id(s): " + CollectionUtils.abbreviate(ids, LOG_LIMIT));
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull, Integer limitOrNull)
    {
        IEventDAO eventDAO = CommonServiceProvider.getDAOFactory().getEventDAO();
        List<EventPE> result = eventDAO.listEvents(eventType, entityType, lastSeenTimestampOrNull, limitOrNull);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseLoadedEvents(result.size());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug("Found " + result.size() + " event(s) for eventType: " + eventType + ", entityType: " + entityType
                            + ", lastSeenTimestamp: "
                            + lastSeenTimestampOrNull);
        }

        return result;
    }

    @Override public Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType)
    {
        IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
        Date result = eventsSearchDAO.getLastTimestamp(eventType, entityType);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found " + result + " last seen timestamp for eventType: " + eventType + ", entityType: " + entityType);
        }

        return result;
    }

    @Override public void createEventsSearch(EventsSearchPE eventsSearch)
    {
        IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
        eventsSearchDAO.createOrUpdate(eventsSearch);

        for (Statistics statistics : statisticsStack)
        {
            statistics.increaseCreatedEvents(1);
        }
    }

}
