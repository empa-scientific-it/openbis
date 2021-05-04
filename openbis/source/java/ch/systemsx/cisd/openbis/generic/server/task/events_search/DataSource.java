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
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventsSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

class DataSource implements IDataSource
{

    @Override public <T> T executeInNewTransaction(TransactionCallback<T> callback)
    {
        PlatformTransactionManager manager = CommonServiceProvider.getApplicationContext().getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setReadOnly(false);
        TransactionTemplate template = new TransactionTemplate(manager, definition);
        return template.execute(callback);
    }

    @Override
    public List<SpacePE> loadSpaces(List<String> codes)
    {
        ISpaceDAO spaceDAO = CommonServiceProvider.getDAOFactory().getSpaceDAO();
        return spaceDAO.tryFindSpaceByCodes(codes);
    }

    @Override
    public List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<IProjectId, Project> result = v3.getProjects(sessionToken, ids, fo);
        return new ArrayList<>(result.values());
    }

    @Override
    public List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<IExperimentId, Experiment> result = v3.getExperiments(sessionToken, ids, fo);
        return new ArrayList<>(result.values());
    }

    @Override public List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo)
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        Map<ISampleId, Sample> result = v3.getSamples(sessionToken, ids, fo);
        return new ArrayList<>(result.values());
    }

    @Override
    public List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull)
    {
        IEventDAO eventDAO = CommonServiceProvider.getDAOFactory().getEventDAO();
        return eventDAO.listEvents(eventType, entityType, lastSeenTimestampOrNull);
    }

    @Override public Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType)
    {
        IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
        return eventsSearchDAO.getLastTimestamp(eventType, entityType);
    }

    @Override public void createEventsSearch(EventsSearchPE eventsSearch)
    {
        IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
        eventsSearchDAO.createOrUpdate(eventsSearch);
    }
}
