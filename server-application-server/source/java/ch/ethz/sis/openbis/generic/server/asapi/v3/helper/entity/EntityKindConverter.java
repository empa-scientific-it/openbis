/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

/**
 * @author pkupczyk
 */
public class EntityKindConverter
{

    public static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind convert(
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind)
    {
        if (entityKind == null)
        {
            return null;
        } else
        {
            return ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.valueOf(entityKind.name());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind convert(
            ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind entityKind)
    {
        if (entityKind == null)
        {
            return null;
        } else
        {
            return ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.valueOf(entityKind.name());
        }
    }

    public static EntityKind convert(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind)
    {
        if (entityKind == null)
        {
            return null;
        } else
        {
            return ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.valueOf(entityKind.name());
        }
    }

}
