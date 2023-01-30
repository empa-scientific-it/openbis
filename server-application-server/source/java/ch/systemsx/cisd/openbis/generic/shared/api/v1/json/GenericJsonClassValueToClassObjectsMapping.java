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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.json;

import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class GenericJsonClassValueToClassObjectsMapping extends
        JsonStaticClassValueToClassObjectsMapping
{

    private static GenericJsonClassValueToClassObjectsMapping instance;

    private GenericJsonClassValueToClassObjectsMapping()
    {
        addClass(".MaterialIdentifier",
                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier.class);
        addClass(".Material", ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material.class);
        addClass(".PropertyType",
                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType.class);
        addClass(
                ".ControlledVocabularyPropertyType",
                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.class);
    }

    public static GenericJsonClassValueToClassObjectsMapping getInstance()
    {
        synchronized (GenericJsonClassValueToClassObjectsMapping.class)
        {
            if (instance == null)
            {
                instance = new GenericJsonClassValueToClassObjectsMapping();
            }
            return instance;
        }
    }

}
