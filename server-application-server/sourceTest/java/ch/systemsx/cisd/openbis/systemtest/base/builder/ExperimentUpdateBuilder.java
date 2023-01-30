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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ExperimentUpdateBuilder extends UpdateBuilder<ExperimentUpdatesDTO>
{
    private ExperimentUpdatesDTO updates;

    public ExperimentUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, Experiment exp)
    {
        super(commonServer, genericServer);
        updates = new ExperimentUpdatesDTO();
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken,
                        new ExperimentIdentifier(exp.getProject().getSpace().getCode(), exp
                                .getProject().getCode(), exp.getCode()));

        updates.setExperimentId(new TechId(experiment));
        updates.setVersion(experiment.getVersion());
        updates.setProperties(experiment.getProperties());
        updates.setAttachments(new ArrayList<NewAttachment>());
        updates.setNewSamples(new ArrayList<NewSamplesWithTypes>());
        updates.setProjectIdentifier(new ProjectIdentifier(experiment.getProject()
                .getSpace().getCode(), experiment.getProject().getCode()));
    }

    public ExperimentUpdateBuilder toProject(Project project)
    {
        updates.setProjectIdentifier(new ProjectIdentifier(project.getSpace().getCode(),
                project.getCode()));
        return this;
    }

    public ExperimentUpdateBuilder withSamples(Sample... samples)
    {
        String[] codes = new String[samples.length];
        for (int i = 0; i < samples.length; i++)
        {
            codes[i] = samples[i].getCode();
        }
        updates.setSampleCodes(codes);
        return this;
    }

    public ExperimentUpdateBuilder removingSamples()
    {
        updates.setSampleCodes(new String[0]);
        return this;
    }

    @Override
    public ExperimentUpdatesDTO create()
    {
        return updates;
    }

    @Override
    public void perform()
    {
        commonServer.updateExperiment(this.sessionToken, this.create());
    }
}