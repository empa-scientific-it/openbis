/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.globalsearch;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.AbstractComparator;

/**
 * @author pkupczyk
 */
public class ObjectKindComparator extends AbstractComparator<GlobalSearchObject, Integer>
{

    @Override
    protected Integer getValue(GlobalSearchObject o)
    {
        GlobalSearchObjectKind kind = o.getObjectKind();

        switch (kind)
        {
            case EXPERIMENT:
                return 0;
            case SAMPLE:
                return 1;
            case DATA_SET:
                return 2;
            case MATERIAL:
                return 3;
            default:
                throw new UnsupportedOperationException("Unsupported object kind " + kind);
        }
    }

}
