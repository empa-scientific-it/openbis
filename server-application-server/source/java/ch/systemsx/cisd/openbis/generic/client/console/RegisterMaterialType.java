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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * @author Pawel Glyzewski
 */
public class RegisterMaterialType extends AbstractRegisterEntityType<MaterialType> implements
        ICommand
{
    private enum MaterialTypeAttributeSetter implements AttributeSetter<MaterialType>
    {
        DESCRIPTION("description")
        {
            @Override
            public void setAttributeFor(MaterialType materialType, String value)
            {
                materialType.setDescription(value);
            }
        };

        private final String attributeName;

        private MaterialTypeAttributeSetter(String attributeName)
        {
            this.attributeName = attributeName;
        }

        @Override
        public String getAttributeName()
        {
            return attributeName;
        }

        @Override
        public void setDefaultFor(MaterialType type)
        {
        }
    }

    private static final Map<String, AttributeSetter<MaterialType>> attributeSetters =
            new HashMap<String, AttributeSetter<MaterialType>>();

    static
    {
        for (MaterialTypeAttributeSetter setter : MaterialTypeAttributeSetter.values())
        {
            attributeSetters.put(setter.getAttributeName(), setter);
        }
    }

    @Override
    protected Map<String, AttributeSetter<MaterialType>> attributeSetters()
    {
        return attributeSetters;
    }

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        server.registerMaterialType(sessionToken, prepareEntityType(new MaterialType(), argument));
    }
}
