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
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.List;

interface IDataSource
{

    <T> T executeInNewTransaction(TransactionCallback<T> callback);

    List<SpacePE> loadSpaces(List<String> codes);

    List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo);

    List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo);

    List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo);

    List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull);

    Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType);

    void createEventsSearch(EventsSearchPE eventsSearch);

}