/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.experimentlister;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public interface IExperimentLister
{

    public List<Experiment> listExperiments(List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

    public List<Experiment> listExperimentsForProjects(List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

}
