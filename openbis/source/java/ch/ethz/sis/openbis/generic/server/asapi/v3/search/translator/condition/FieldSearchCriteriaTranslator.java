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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;

public class FieldSearchCriteriaTranslator implements IConditionTranslator<AbstractFieldSearchCriteria<?>>
{

    private static final Map<String, String> sampleAttributeIdToColumnName = new HashMap<>();
    static {
        sampleAttributeIdToColumnName.put("perm id", ColumnNames.PERM_ID_COLUMN);
    }

    @Override
    public void translate(final AbstractFieldSearchCriteria<?> criterion, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final SearchFieldType searchFieldType = criterion.getFieldType();
        final String fieldName = criterion.getFieldName();
        final Object fieldValue = criterion.getFieldValue();
        final Class fieldValueClass = (fieldValue != null) ? fieldValue.getClass() : null;

        switch (searchFieldType)
        {
            case PROPERTY:
                break;
            case ATTRIBUTE:
                sqlBuilder.append(sampleAttributeIdToColumnName.get(fieldName)).append(SP);
                break;
            case ANY_PROPERTY:
                break;
            case ANY_FIELD:
                break;
        }

        if (fieldValueClass == null)
        {
            sqlBuilder.append(IS_NOT_NULL);
        }
    }

}
