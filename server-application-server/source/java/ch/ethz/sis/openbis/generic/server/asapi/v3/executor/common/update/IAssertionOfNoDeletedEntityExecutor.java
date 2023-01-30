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

/**
 * @author Franz-Josef Elmer
 *
 */
public interface IAssertionOfNoDeletedEntityExecutor
{

    void assertSpaceHasNoDeletedSamples(String spaceCode);

    void assertProjectHasNoDeletedExperiments(String projectPermId);

    void assertProjectHasNoDeletedSamples(String projectPermId);

    void assertExperimentHasNoDeletedSamples(String experimentPermId);

    void assertExperimentHasNoDeletedDataSets(String experimentPermId);

    void assertDataSetHasNoDeletedChildren(IDataSetId dataSetId);

    void assertDataSetHasNoDeletedParents(IDataSetId dataSetId);

    void assertDataSetHasNoDeletedComponents(IDataSetId dataSetId);

    void assertSampleHasNoDeletedChildren(String samplePermId);

    void assertSampleHasNoDeletedParents(String samplePermId);

    void assertSampleHasNoDeletedComponents(String samplePermId);

    void assertSampleHasNoDeletedDataSets(String samplePermId);



}
