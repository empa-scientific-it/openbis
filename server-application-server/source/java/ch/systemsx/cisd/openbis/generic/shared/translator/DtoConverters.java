/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A static class which converts <i>business</i> <i>DTOs</i> into <i>GWT</i> ones.
 * 
 * @author Christian Ribeaud
 */
public class DtoConverters
{

    private DtoConverters()
    {
        // Can not be instantiated.
    }

    public static final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind convertEntityKind(
            final EntityKind entityKind)
    {
        return ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.valueOf(entityKind
                .name());
    }

    public static final EntityKind convertEntityKind(
            final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind)
    {
        return EntityKind.valueOf(entityKind.name());
    }

    /**
     * Does not really create an unmodifiable empty list but this works with <i>GWT</i>.
     */
    static final <T> List<T> createUnmodifiableEmptyList()
    {
        return new ArrayList<T>();
    }
}
