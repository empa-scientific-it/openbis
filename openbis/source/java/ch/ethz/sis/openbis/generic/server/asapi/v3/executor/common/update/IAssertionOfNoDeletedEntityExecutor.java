/*
 * Copyright 2022 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;

/**
 * @author Franz-Josef Elmer
 *
 */
public interface IAssertionOfNoDeletedEntityExecutor
{

    void assertSpaceHasNoDeletedSamples(String spaceCode);

    void assertProjectHasNoDeletedExperiments(IProjectId projectId);

    void assertProjectHasNoDeletedSamples(IProjectId projectId);

    void assertExperimentHasNoDeletedSamples(IExperimentId experimentId);

    void assertExperimentHasNoDeletedDataSets(IExperimentId experimentId);

    void assertDataSetHasNoDeletedChildren(IDataSetId dataSetId);

    void assertDataSetHasNoDeletedParents(IDataSetId dataSetId);

    void assertDataSetHasNoDeletedComponents(IDataSetId dataSetId);

    void assertSampleHasNoDeletedChildren(ISampleId sampleId);

    void assertSampleHasNoDeletedParents(ISampleId sampleId);

    void assertSampleHasNoDeletedComponents(ISampleId sampleId);

    void assertSampleHasNoDeletedDataSets(ISampleId sampleId);



}
