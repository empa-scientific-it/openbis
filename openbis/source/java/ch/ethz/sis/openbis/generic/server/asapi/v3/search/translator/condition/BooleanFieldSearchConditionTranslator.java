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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.BooleanFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TERM_TABLE;

public class BooleanFieldSearchConditionTranslator implements IConditionTranslator<BooleanFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final BooleanFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                return null;
            }

            case PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory, JoinType.INNER);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final BooleanFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterionFieldName);
                final Boolean value = criterion.getFieldValue();

                sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(EQ).append(SP).append(QU);
                args.add(value);
                break;
            }

            case PROPERTY:
            {
                final Boolean value = criterion.getFieldValue();
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());
                final String entityTypesSubTableAlias = aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias();

                TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

                sqlBuilder.append(SP).append(entityTypesSubTableAlias).append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).
                        append(SP).append(QU);
                args.add(propertyName);

                if (value != null)
                {
                    sqlBuilder.append(SP).append(AND).append(SP).append(LP).append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                            .append(PERIOD).append(ColumnNames.VALUE_COLUMN);

                    sqlBuilder.append(SP).append(EQ).append(SP).append(QU);
                    args.add(value);

                    sqlBuilder.append(SP).append(OR).append(SP).append(aliases.get(CONTROLLED_VOCABULARY_TERM_TABLE).getSubTableAlias())
                            .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(QU).append(SP).append(QU);
                    args.add(value);
                    sqlBuilder.append(RP);
                }
                break;
            }

            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

}
