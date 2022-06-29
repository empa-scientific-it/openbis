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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.BOOLEAN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

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
            case ANY_PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final BooleanFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterionFieldName);
                final Boolean value = criterion.getFieldValue();

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(EQ).append(SP).append(QU);
                args.add(value);
                break;
            }

            case PROPERTY:
            {
                final String casting = dataTypeByPropertyCode.get(criterion.getFieldName());

                if (!DataTypeCode.BOOLEAN.toString().equals(casting))
                {
                    throw new UserFailureException(String.format(
                            "The data type of property %s has to be %s instead of %s.", criterion.getFieldName(),
                            DataTypeCode.BOOLEAN, casting));
                }

                translateBooleanProperty(tableMapper, args, sqlBuilder, aliases, criterion.getFieldValue(),
                        criterion.getFieldName());
                break;
            }

            case ANY_PROPERTY:
            {
                translateBooleanProperty(tableMapper, args, sqlBuilder, aliases, criterion.getFieldValue(), null);
                break;
            }

            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateBooleanProperty(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Boolean value,
            final String fullPropertyName)
    {
        final String propertyTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();
        TranslatorUtils.appendPropertiesExist(sqlBuilder, propertyTableAlias);
        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        sqlBuilder.append(LP);
        TranslatorUtils.appendDataTypesSubselect(tableMapper, sqlBuilder,
                aliases.get(tableMapper.getValuesTable()).getSubTableAlias());
        sqlBuilder.append(RP).append(SP).append(EQ)
                .append(SP).append(SQ).append(DataTypeCode.BOOLEAN).append(SQ);

        if (fullPropertyName != null)
        {
            sqlBuilder.append(SP).append(AND).append(SP);

            TranslatorUtils.appendEntityTypePropertyTypeSubselectConstraint(tableMapper, args, sqlBuilder,
                    fullPropertyName, propertyTableAlias);
        }

        if (value != null)
        {
            sqlBuilder.append(SP).append(AND).append(SP);
            sqlBuilder.append(aliases.get(tableMapper.getValuesTable())
                    .getSubTableAlias()).append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(SP)
                    .append(EQ).append(SP).append(QU);
            args.add(String.valueOf(value));
        }

        sqlBuilder.append(RP);
    }

}
