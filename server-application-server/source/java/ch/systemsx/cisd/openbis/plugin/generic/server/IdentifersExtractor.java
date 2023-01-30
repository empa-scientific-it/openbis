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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class which extract identifiers from a {@link NewSample} instance.
 * 
 * @author Franz-Josef Elmer
 */
class IdentifersExtractor
{
    private final SampleIdentifier oldSampleIdentifier;

    private SampleIdentifier newSampleIdentifier;

    private ExperimentIdentifier experimentIdentifierOrNull;
    
    private ProjectIdentifier projectIdentifierOrNull;

    private final String containerIdentifierOrNull;

    IdentifersExtractor(NewSample updatedSample)
    {
        oldSampleIdentifier = SampleIdentifierFactory.parse(updatedSample);
        newSampleIdentifier = oldSampleIdentifier;
        String experimentIdentifier = updatedSample.getExperimentIdentifier();
        String projectIdentifier = updatedSample.getProjectIdentifier();
        String defaultSpaceIdentifier = updatedSample.getDefaultSpaceIdentifier();
        String sampleCode = oldSampleIdentifier.getSampleCode();
        if (experimentIdentifier != null)
        {
            // experiment is provided - new sample identifier takes experiment space
            experimentIdentifierOrNull =
                    new ExperimentIdentifierFactory(experimentIdentifier)
                            .createIdentifier(defaultSpaceIdentifier);
            // TODO 2011-08-31, Tomasz Pylak: container is ignored, what does it break?
            newSampleIdentifier =
                    new SampleIdentifier(new SpaceIdentifier(
                            experimentIdentifierOrNull.getSpaceCode()),
                            sampleCode);
        }
        if (projectIdentifier != null)
        {
            projectIdentifierOrNull =
                    new ProjectIdentifierFactory(projectIdentifier).createIdentifier(defaultSpaceIdentifier);
            newSampleIdentifier = new SampleIdentifier(projectIdentifierOrNull, sampleCode);
        }
        containerIdentifierOrNull = updatedSample.getContainerIdentifier();
    }

    public SampleIdentifier getOldSampleIdentifier()
    {
        return oldSampleIdentifier;
    }

    public SampleIdentifier getNewSampleIdentifier()
    {
        return newSampleIdentifier;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }
    
    public ProjectIdentifier getProjectIdentifier()
    {
        return projectIdentifierOrNull;
    }

    public String getContainerIdentifierOrNull()
    {
        return containerIdentifierOrNull;
    }
}