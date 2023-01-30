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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.EntityLinkElementKind;

/**
 * @author Piotr Buczek
 */
public class EntityLinkElementTranslator
{
    public static EntityKind translate(EntityLinkElementKind linkElementKind)
    {
        switch (linkElementKind)
        {
            case DATA_SET:
                return EntityKind.DATA_SET;
            case EXPERIMENT:
                return EntityKind.EXPERIMENT;
            case MATERIAL:
                return EntityKind.MATERIAL;
            case SAMPLE:
                return EntityKind.SAMPLE;
        }
        return null; // won't happen
    }
}
