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

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractRegisterEntityType<T extends EntityType> implements ICommand
{
    protected abstract Map<String, AttributeSetter<T>> attributeSetters();

    private void fillWithDefaults(T entityType)
    {
        for (AttributeSetter<T> setter : attributeSetters().values())
        {
            setter.setDefaultFor(entityType);
        }
    }

    protected T prepareEntityType(T entityType, String argument)
    {
        List<String> tokens = Lexer.extractTokens(argument);
        entityType.setCode(tokens.get(0));
        fillWithDefaults(entityType);

        for (int i = 1; i < tokens.size(); i++)
        {
            String token = tokens.get(i);
            int indexOfEqualSign = token.indexOf('=');
            if (indexOfEqualSign < 0)
            {
                throw new IllegalArgumentException("Missing '=': " + token);
            }
            String key = token.substring(0, indexOfEqualSign);
            String value = token.substring(indexOfEqualSign + 1);
            AttributeSetter<T> attributeSetter = attributeSetters().get(key);
            if (attributeSetter == null)
            {
                throw new IllegalArgumentException("Unknown attribute '" + key + "': " + token);
            }
            attributeSetter.setAttributeFor(entityType, value);
        }

        return entityType;
    }
}
