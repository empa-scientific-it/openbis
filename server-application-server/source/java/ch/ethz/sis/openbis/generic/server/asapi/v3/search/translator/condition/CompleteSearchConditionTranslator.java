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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.CompleteSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType.ATTRIBUTE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class CompleteSearchConditionTranslator implements IConditionTranslator<CompleteSearchCriteria>
{

    private static final Map<Complete, Character> CHARACTER_BY_COMPLETE = new EnumMap<>(Complete.class);

    static
    {
        CHARACTER_BY_COMPLETE.put(Complete.YES, BooleanOrUnknown.T.name().charAt(0));
        CHARACTER_BY_COMPLETE.put(Complete.NO, BooleanOrUnknown.F.name().charAt(0));
        CHARACTER_BY_COMPLETE.put(Complete.UNKNOWN, BooleanOrUnknown.U.name().charAt(0));
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final CompleteSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final CompleteSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        if (criterion.getFieldType() == ATTRIBUTE)
        {
            final Complete value = criterion.getFieldValue();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(ColumnNames.IS_COMPLETE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
            args.add(CHARACTER_BY_COMPLETE.get(value));
        } else
        {
            throw new IllegalArgumentException();
        }
    }

}
