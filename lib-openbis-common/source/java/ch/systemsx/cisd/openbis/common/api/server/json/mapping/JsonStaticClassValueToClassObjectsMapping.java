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

package ch.systemsx.cisd.openbis.common.api.server.json.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a mapping between legacy @class field values and classes that used to correspond to these values when @class annotation was used (for
 * instance ".MaterialIdentifier" @class value used to represent ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier and
 * ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier classes).
 * 
 * @author pkupczyk
 */
public class JsonStaticClassValueToClassObjectsMapping implements
        IJsonClassValueToClassObjectsMapping
{

    private Map<String, List<Class<?>>> map = new HashMap<String, List<Class<?>>>();

    public void addClass(String classValue, Class<?> classObject)
    {
        List<Class<?>> existingClassObjects = getClasses(classValue);

        if (existingClassObjects == null)
        {
            existingClassObjects = new ArrayList<Class<?>>();
            map.put(classValue, existingClassObjects);
        }

        existingClassObjects.add(classObject);
    }

    @Override
    public List<Class<?>> getClasses(String classValue)
    {
        return map.get(classValue);
    }

}
