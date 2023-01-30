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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Console;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class CreateSampleRmi implements Command<Sample>
{
    @Inject
    private String session;

    @Inject
    private IGenericServer genericServer;

    @Inject
    private Console console;

    private Sample sample;

    public CreateSampleRmi(Sample sample)
    {
        this.sample = sample;
    }

    @Override
    public Sample execute()
    {
        console.startBuffering();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample registerdSample 
                = genericServer.registerSample(session, convert(sample), new ArrayList<NewAttachment>());
        return sample;
    }

    private NewSample convert(Sample s)
    {
        NewSample data = new NewSample();
        data.setIdentifier(Identifiers.get(s).toString());
        data.setAttachments(new ArrayList<NewAttachment>());
        data.setContainerIdentifier(null);

        if (s.getExperiment() != null)
        {
            data.setExperimentIdentifier(Identifiers.get(s.getExperiment()).toString());
        }

        String[] parentIds = new String[s.getParents().size()];
        int i = 0;
        for (Sample parent : s.getParents())
        {
            parentIds[i] = Identifiers.get(parent).toString();
            i++;
        }

        data.setParentsOrNull(parentIds);
        data.setProperties(new IEntityProperty[0]);
        data.setSampleType(convert(s.getType()));
        return data;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType convert(SampleType type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType result =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType();

        result.setCode(type.getCode());
        return result;
    }

}
