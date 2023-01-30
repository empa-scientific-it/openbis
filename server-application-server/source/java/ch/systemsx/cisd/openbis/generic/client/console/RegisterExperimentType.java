/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.console;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * @author Kaloyan Enimanev
 */
public class RegisterExperimentType extends AbstractRegisterEntityType<ExperimentType> implements
        ICommand
{
    private enum ExperimentTypeAttributeSetter implements AttributeSetter<ExperimentType>
    {
        DESCRIPTION("description")
        {
            @Override
            public void setAttributeFor(ExperimentType experimentType, String value)
            {
                experimentType.setDescription(value);
            }
        };

        private final String attributeName;

        private ExperimentTypeAttributeSetter(String attributeName)
        {
            this.attributeName = attributeName;
        }

        @Override
        public String getAttributeName()
        {
            return attributeName;
        }

        @Override
        public void setDefaultFor(ExperimentType type)
        {
        }
    }

    private static final Map<String, AttributeSetter<ExperimentType>> attributeSetters =
            new HashMap<String, AttributeSetter<ExperimentType>>();

    static
    {
        for (ExperimentTypeAttributeSetter setter : ExperimentTypeAttributeSetter.values())
        {
            attributeSetters.put(setter.getAttributeName(), setter);
        }
    }

    @Override
    protected Map<String, AttributeSetter<ExperimentType>> attributeSetters()
    {
        return attributeSetters;
    }

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        server.registerExperimentType(sessionToken,
                prepareEntityType(new ExperimentType(), argument));
    }
}
