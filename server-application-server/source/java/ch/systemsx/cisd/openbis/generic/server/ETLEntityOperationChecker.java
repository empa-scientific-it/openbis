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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Implementation of {@link IETLEntityOperationChecker} which does nothing because checking is done by aspects.
 * 
 * @author Franz-Josef Elmer
 */
public class ETLEntityOperationChecker implements IETLEntityOperationChecker
{

    @Override
    public void assertSpaceCreationAllowed(IAuthSession session, List<NewSpace> newSpaces)
    {
    }

    @Override
    public void assertMaterialCreationAllowed(IAuthSession session,
            Map<String, List<NewMaterial>> materials)
    {
    }

    @Override
    public void assertMaterialUpdateAllowed(IAuthSession session, List<MaterialUpdateDTO> materials)
    {
    }

    @Override
    public void assertProjectCreationAllowed(IAuthSession session, List<NewProject> newProjects)
    {
    }

    @Override
    public void assertProjectUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = ProjectUpdatesPredicate.class) List<ProjectUpdatesDTO> projectsToUpdate)
    {
    }

    @Override
    public void assertExperimentCreationAllowed(IAuthSession session,
            List<NewExperiment> newExperiments)
    {
    }

    @Override
    public void assertInstanceSampleCreationAllowed(IAuthSession session,
            List<NewSample> instanceSamples)
    {
    }

    @Override
    public void assertSpaceSampleCreationAllowed(IAuthSession session, List<NewSample> spaceSamples)
    {
    }

    @Override
    public void assertInstanceSampleUpdateAllowed(IAuthSession session,
            List<SampleUpdatesDTO> instanceSamples)
    {
    }

    @Override
    public void assertSpaceSampleUpdateAllowed(IAuthSession session,
            List<SampleUpdatesDTO> spaceSamples)
    {
    }

    @Override
    public void assertDataSetCreationAllowed(IAuthSession session,
            List<? extends NewExternalData> dataSets)
    {
    }

    @Override
    public void assertDataSetUpdateAllowed(IAuthSession session,
            List<DataSetBatchUpdatesDTO> dataSets)
    {
    }

    @Override
    public void assertExperimentUpdateAllowed(IAuthSession session,
            List<ExperimentUpdatesDTO> experimentUpdates)
    {
    }

    @Override
    public void assertSpaceRoleAssignmentAllowed(IAuthSession session, SpaceIdentifier space)
    {

    }

    @Override
    public void assertVocabularyUpdateAllowed(IAuthSession session)
    {

    }
}
