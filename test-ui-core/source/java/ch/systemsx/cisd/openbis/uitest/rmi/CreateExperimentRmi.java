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

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class CreateExperimentRmi implements Command<Experiment>
{

    @Inject
    private String session;

    @Inject
    private IGenericServer genericServer;

    private Experiment experiment;

    public CreateExperimentRmi(Experiment experiment)
    {
        this.experiment = experiment;
    }

    @Override
    public Experiment execute()
    {
        genericServer.registerExperiment(session, convert(experiment),
                new ArrayList<NewAttachment>());
        return experiment;
    }

    private NewExperiment convert(Experiment exp)
    {
        String experimentId = Identifiers.get(exp).toString();
        NewExperiment data = new NewExperiment(experimentId, exp.getType().getCode());
        data.setAttachments(new ArrayList<NewAttachment>());
        data.setGenerateCodes(false);
        data.setNewSamples(null);
        data.setProperties(new IEntityProperty[0]);
        data.setRegisterSamples(false);

        String[] sampleIds = new String[exp.getSamples().size()];
        int i = 0;
        for (Sample sample : exp.getSamples())
        {
            sampleIds[i] = sample.getCode();
            i++;
        }

        data.setSamples(sampleIds);
        return data;
    }
}
