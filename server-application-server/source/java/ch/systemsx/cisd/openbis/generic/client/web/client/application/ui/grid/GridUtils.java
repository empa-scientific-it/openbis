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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.addAny;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Franz-Josef Elmer
 */
public class GridUtils
{
    public static DatabaseModificationKind[] getRelevantModifications(ObjectKind entityKind,
            ICriteriaProvider<?> criteriaProvider)
    {
        List<DatabaseModificationKind> relevantModifications =
                new ArrayList<DatabaseModificationKind>();
        SetUtils.addAll(relevantModifications, criteriaProvider.getRelevantModifications());
        relevantModifications.addAll(GridUtils.getGridRelevantModifications(entityKind, true));
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    public final static Set<DatabaseModificationKind> getGridRelevantModifications(
            ObjectKind entity, boolean withProjectAndSpace)
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        result.add(createOrDelete(entity));
        result.add(edit(entity));
        result.add(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.VOCABULARY_TERM));
        result.add(createOrDelete(ObjectKind.METAPROJECT));
        if (withProjectAndSpace)
        {
            addAny(result, ObjectKind.PROJECT);
            addAny(result, ObjectKind.SPACE);
        }
        return result;
    }

}
