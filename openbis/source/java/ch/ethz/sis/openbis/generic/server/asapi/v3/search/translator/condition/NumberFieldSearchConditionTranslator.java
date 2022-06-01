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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COMMA;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SAFE_DOUBLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

public class NumberFieldSearchConditionTranslator implements IConditionTranslator<NumberFieldSearchCriteria>
{

    private static final String INTEGER_DATA_TYPE_CODE = DataTypeCode.INTEGER.toString();

    private static final String REAL_DATA_TYPE_CODE = DataTypeCode.REAL.toString();

    private static final Set<String> VALID_DATA_TYPES = new HashSet<>(Arrays.asList(
            INTEGER_DATA_TYPE_CODE, REAL_DATA_TYPE_CODE));

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper,
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
    public void translate(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterion.getFieldName());
                final AbstractNumberValue value = criterion.getFieldValue();

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP);
                TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
                sqlBuilder.append(NL);
                args.add(value.getValue());
                break;
            }

            case PROPERTY:
            {
                final AbstractNumberValue value = criterion.getFieldValue();
                final String casting = dataTypeByPropertyCode.get(criterion.getFieldName());
                if (VALID_DATA_TYPES.contains(casting) == false)
                {
                    throw new UserFailureException("The data type of property " + criterion.getFieldName() +
                            " has to be one of " + VALID_DATA_TYPES + " instead of " + casting + ".");
                }
                translateNumberProperty(tableMapper, args, sqlBuilder, aliases, value,
                        criterion.getFieldName());
                break;
            }

            case ANY_PROPERTY:
            {
                final AbstractNumberValue value = criterion.getFieldValue();
                translateNumberProperty(tableMapper, args, sqlBuilder, aliases, value, null);
                break;
            }

            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateNumberProperty(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final AbstractNumberValue value,
            final String fullPropertyName)
    {
        final String propertyTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();
        TranslatorUtils.appendPropertiesExist(sqlBuilder, propertyTableAlias);
        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        if (fullPropertyName != null)
        {
            TranslatorUtils.appendEntityTypePropertyTypeSubselectConstraint(tableMapper, args, sqlBuilder,
                    fullPropertyName, propertyTableAlias);

            sqlBuilder.append(SP).append(AND);
        }

        sqlBuilder.append(SP).append(LP);

        sqlBuilder.append(LP);
        TranslatorUtils.appendDataTypesSubselect(tableMapper, sqlBuilder,
                aliases.get(tableMapper.getValuesTable()).getSubTableAlias());
        sqlBuilder.append(RP).append(SP).append(IN).append(SP).append(LP);
        sqlBuilder.append(SQ).append(DataTypeCode.INTEGER).append(SQ).append(COMMA).append(SP)
                .append(SQ).append(DataTypeCode.REAL).append(SQ);
        sqlBuilder.append(RP);
        sqlBuilder.append(RP);

        if (value != null)
        {
            sqlBuilder.append(SP).append(AND).append(SP).append(SAFE_DOUBLE).append(LP)
                    .append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                    .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(RP).append(SP);
            TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
            args.add(value.getValue());
        }

        sqlBuilder.append(RP);
    }

}
