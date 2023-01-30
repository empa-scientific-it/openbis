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

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;

/**
 * @author anttil
 */
public class CreateExperimentTypeRmi implements Command<ExperimentType>
{
    @Inject
    private String session;

    @Inject
    private ICommonServer commonServer;

    private ExperimentType type;

    public CreateExperimentTypeRmi(ExperimentType type)
    {
        this.type = type;
    }

    @Override
    public ExperimentType execute()
    {
        commonServer.registerExperimentType(session, convert(type));
        return type;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType convert(
            @SuppressWarnings("hiding") ExperimentType type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType experimentType =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType();
        experimentType.setCode(type.getCode());
        experimentType.setDescription(type.getDescription());
        experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
        return experimentType;
    }
}
