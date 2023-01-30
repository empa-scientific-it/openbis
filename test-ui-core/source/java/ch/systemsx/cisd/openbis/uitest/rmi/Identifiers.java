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

package ch.systemsx.cisd.openbis.uitest.rmi;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
public class Identifiers
{

    public static SampleIdentifier get(Sample sample)
    {
        return new SampleIdentifier(get(sample.getSpace()), sample.getCode());
    }

    public static SpaceIdentifier get(Space space)
    {
        return new SpaceIdentifier(space.getCode());
    }

    public static ExperimentIdentifier get(Experiment experiment)
    {
        return new ExperimentIdentifier(get(experiment.getProject()), experiment.getCode());
    }

    public static ProjectIdentifier get(Project project)
    {
        return new ProjectIdentifier(get(project.getSpace()), project.getCode());
    }

    public static MaterialIdentifier get(Material material)
    {
        return new MaterialIdentifier(new MaterialTypeIdentifier(material.getType().getCode()),
                material.getCode());
    }
}
