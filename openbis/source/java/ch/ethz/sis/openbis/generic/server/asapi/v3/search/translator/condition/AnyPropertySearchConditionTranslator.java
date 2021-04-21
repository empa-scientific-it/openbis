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

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.appendTsVectorMatch;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TERM_TABLE;

public class AnyPropertySearchConditionTranslator implements IConditionTranslator<AnyPropertySearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final AnyPropertySearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        if (criterion.getFieldType() == SearchFieldType.ANY_PROPERTY)
        {
            return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void translate(final AnyPropertySearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ANY_PROPERTY:
            {
                doTranslate(criterion, criterion.isUseWildcards(), tableMapper, args, sqlBuilder, aliases);
                break;
            }

            case ANY_FIELD:
            case PROPERTY:
            case ATTRIBUTE:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    static void doTranslate(final AbstractFieldSearchCriteria<AbstractStringValue> criterion,
            final boolean useWildcards, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases)
    {
        final AbstractStringValue value = criterion.getFieldValue();
        final JoinInformation joinInformation = aliases.get(tableMapper.getAttributeTypesTable());
        final String entityTypesSubTableAlias = joinInformation.getSubTableAlias();

        if (value.getClass() != AnyStringValue.class)
        {
            if (value.getClass() != StringMatchesValue.class)
            {
                sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(joinInformation.getSubTableIdField())
                        .append(SP).append(IS_NOT_NULL).append(SP).append(AND).append(SP).append(LP);

                sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(SP);
                final String finalValue = TranslatorUtils.stripQuotationMarks(value.getValue());
                TranslatorUtils.appendStringComparatorOp(value.getClass(), finalValue, useWildcards, sqlBuilder, args);

                sqlBuilder.append(SP).append(OR).append(SP).append(aliases.get(CONTROLLED_VOCABULARY_TERM_TABLE)
                        .getSubTableAlias()).append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP);
                TranslatorUtils.appendStringComparatorOp(value.getClass(), finalValue, useWildcards, sqlBuilder,
                        args);

                if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                        || tableMapper == TableMapper.DATA_SET)
                {
                    appendSamplePropertyComparison(sqlBuilder, value, useWildcards, aliases, CODE_COLUMN, args);
                    appendSamplePropertyComparison(sqlBuilder, value, useWildcards, aliases, PERM_ID_COLUMN, args);
                    appendSamplePropertyComparison(sqlBuilder, value, useWildcards, aliases, SAMPLE_IDENTIFIER_COLUMN,
                            args);
                }

                sqlBuilder.append(RP);
            } else
            {
                appendTsVectorMatch(sqlBuilder, criterion.getFieldValue(),
                        aliases.get(tableMapper.getValuesTable()).getSubTableAlias(), args);
            }
        } else
        {
            sqlBuilder.append(TRUE);
        }
    }

    private static void appendSamplePropertyComparison(final StringBuilder sqlBuilder,
            final AbstractStringValue value, final boolean useWildcards, final Map<String, JoinInformation> aliases,
            final String columnName, final List<Object> args)
    {
        final String finalValue = value.getValue();
        sqlBuilder.append(SP).append(OR).append(SP).append(aliases.get(SAMPLE_PROP_COLUMN).getSubTableAlias())
                .append(PERIOD).append(columnName);
        TranslatorUtils.appendStringComparatorOp(value.getClass(), finalValue, useWildcards, sqlBuilder, args);
    }

}
