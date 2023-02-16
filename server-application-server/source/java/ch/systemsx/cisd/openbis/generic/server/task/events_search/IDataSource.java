/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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

    void open();

    void close();

    Statistics executeInNewTransaction(TransactionCallback<?> callback);

    List<SpacePE> loadSpaces(List<String> codes);

    List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo);

    List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo);

    List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo);

    List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull, Integer limitOrNull);

    Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType);

    void createEventsSearch(EventsSearchPE eventsSearch);

}