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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;

public class NameSearchConditionTranslator implements IConditionTranslator<NameSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final NameSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();

        final JoinInformation joinInformation = new JoinInformation();
        joinInformation.setJoinType(JoinType.LEFT);
        joinInformation.setMainTable(tableMapper.getEntitiesTable());
        joinInformation.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation.setMainTableIdField(OWNER_COLUMN);
        joinInformation.setSubTable(PERSONS_TABLE);
        joinInformation.setSubTableAlias(aliasFactory.createAlias());
        joinInformation.setSubTableIdField(ID_COLUMN);
        result.put(PERSONS_TABLE, joinInformation);

        return result;
    }

    @Override
    public void translate(final NameSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final AbstractStringValue value = criterion.getFieldValue();

                if (value != null && value.getValue() != null)
                {
                    final String stringValue = value.getValue();

                    // Building the following query part
                    // ('=' is used instead of 'LIKE' when there are no wildcards):
                    // concat('/', owner_name, '/', name) LIKE ?

                    sqlBuilder.append(CONCAT).append(LP)
                            .append(SQ).append('/').append(SQ).append(COMMA).append(SP)
                            .append(aliases.get(PERSONS_TABLE).getSubTableAlias()).append(PERIOD).append(USER_COLUMN)
                            .append(COMMA).append(SP)
                            .append(SQ).append('/').append(SQ).append(COMMA).append(SP)
                            .append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(NAME_COLUMN)
                            .append(RP);
                    TranslatorUtils.appendStringComparatorOp(value.getClass(), stringValue,
                            criterion.isUseWildcards(), sqlBuilder, args);
                } else
                {
                    sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(NAME_COLUMN)
                            .append(SP).append(IS_NOT_NULL);
                }
                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

}
