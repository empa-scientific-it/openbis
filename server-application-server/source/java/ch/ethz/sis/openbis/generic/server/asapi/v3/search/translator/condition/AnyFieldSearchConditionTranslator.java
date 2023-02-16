/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import java.util.*;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class AnyFieldSearchConditionTranslator implements IConditionTranslator<AnyFieldSearchCriteria>
{

    public static final Map<Class<? extends IDateFormat>, String> TRUNCATION_INTERVAL_BY_DATE_FORMAT =
            new HashMap<>(3);

    private static final String OR_SEPARATOR = SP + OR + SP;

    static
    {
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(SHORT_DATE_FORMAT.getClass(), "day");
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(NORMAL_DATE_FORMAT.getClass(), "minute");
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(LONG_DATE_FORMAT.getClass(), "second");
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final AnyFieldSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        return TranslatorUtils.getFieldJoinInformationMap(tableMapper, aliasFactory);
    }

    @Override
    public void translate(final AnyFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        if (criterion.getFieldType() == SearchFieldType.ANY_FIELD)
        {
            translateAnyField(criterion, criterion.isUseWildcards(), tableMapper, args, sqlBuilder, aliases);
        } else
        {
            throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
        }
    }

    public static void translateAnyField(final AbstractFieldSearchCriteria<AbstractStringValue> criterion,
            final boolean useWildcards, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases)
    {
        final AbstractStringValue value = criterion.getFieldValue();
        final String stringValue = TranslatorUtils.stripQuotationMarks(value.getValue());
        final Set<PSQLTypes> compatiblePSQLTypesForValue = findCompatibleSqlTypesForValue(stringValue);
        final Class<? extends AbstractStringValue> valueClass = value.getClass();
        final boolean equalsToComparison = (valueClass == StringEqualToValue.class);
        final int separatorLength = OR_SEPARATOR.length();

        sqlBuilder.append(LP);

        AnyPropertySearchConditionTranslator.doTranslate(criterion, useWildcards, tableMapper, args, sqlBuilder,
                aliases);
        sqlBuilder.append(OR_SEPARATOR);

        if (valueClass != StringMatchesValue.class)
        {
            IdentifierSearchConditionTranslator.doTranslate(criterion, useWildcards, tableMapper, args, sqlBuilder,
                    aliases, "");
            sqlBuilder.append(OR_SEPARATOR);

            translateEntityTypeMatch(value, args, sqlBuilder, aliases, useWildcards);
            sqlBuilder.append(OR_SEPARATOR);

            if (tableMapper.hasRegistrator())
            {
                translateUserMatch(value, args, sqlBuilder, aliases, REGISTRATOR_JOIN_INFORMATION_KEY, useWildcards);
                sqlBuilder.append(OR_SEPARATOR);
            }
            if (tableMapper.hasModifier())
            {
                translateUserMatch(value, args, sqlBuilder, aliases, MODIFIER_JOIN_INFORMATION_KEY, useWildcards);
                sqlBuilder.append(OR_SEPARATOR);
            }

            final StringBuilder resultSqlBuilder = tableMapper.getFieldToSQLTypeMap().entrySet().stream()
                    .collect(
                    StringBuilder::new,
                    (stringBuilder, fieldToSQLTypesEntry) ->
                            translateField(tableMapper, args, SearchCriteriaTranslator.MAIN_TABLE_ALIAS, value, useWildcards, stringValue,
                                    compatiblePSQLTypesForValue, valueClass, equalsToComparison,
                                    stringBuilder, fieldToSQLTypesEntry),
                StringBuilder::append);

            if (resultSqlBuilder.length() > separatorLength)
            {
                sqlBuilder.append(resultSqlBuilder.substring(separatorLength));
            }

            if (args.isEmpty() || resultSqlBuilder.length() <= separatorLength)
            {
                // When there are no columns selected (no values added), then the query should return nothing
                sqlBuilder.append(FALSE);
            }
        } else
        {
            appendTsVectorMatch(sqlBuilder, criterion.getFieldValue(), MAIN_TABLE_ALIAS, args);
        }

        sqlBuilder.append(RP).append(NL);
    }

    private static void translateField(final TableMapper tableMapper, final List<Object> args, final String alias,
            final AbstractStringValue value, final boolean useWildcards, final String stringValue,
            final Set<PSQLTypes> compatiblePSQLTypesForValue, final Class<? extends AbstractStringValue> valueClass,
            final boolean equalsToComparison, final StringBuilder stringBuilder,
            final Map.Entry<String, PSQLTypes> fieldToSQLTypesEntry)
    {
        final String fieldName = fieldToSQLTypesEntry.getKey();
        final PSQLTypes fieldSQLType = fieldToSQLTypesEntry.getValue();
        final boolean includeColumn = compatiblePSQLTypesForValue.contains(fieldSQLType);

        if (CODE_COLUMN.equals(fieldName))
        {
            // No need to include this expensive full code column string extraction for the
            // 'contains' and  'ends with' queries, because the sample identifier value
            // ends with the full code.
            if (tableMapper != TableMapper.SAMPLE || (valueClass != StringContainsValue.class
                    && valueClass != StringContainsExactlyValue.class && valueClass != StringEndsWithValue.class))
            {
                stringBuilder.append(AnyFieldSearchConditionTranslator.OR_SEPARATOR);
                CodeSearchConditionTranslator.translateSearchByCodeCondition(stringBuilder,
                        tableMapper, value.getClass(), stringValue, useWildcards, args);
            }
        } else
        {
            if (equalsToComparison || fieldSQLType == TIMESTAMP_WITH_TZ)
            {
                if (includeColumn)
                {
                    if (fieldSQLType == TIMESTAMP_WITH_TZ)
                    {
                        final Optional<Object[]> dateFormatWithResultOptional = DATE_FORMATS
                                .stream().map(dateFormat -> {
                                    final Date formattedValue = formatValue(stringValue, dateFormat);
                                    return (formattedValue == null) ? null
                                            : new Object[]{TRUNCATION_INTERVAL_BY_DATE_FORMAT
                                            .get(dateFormat.getClass()), formattedValue};
                                }).filter(Objects::nonNull).findFirst();

                        dateFormatWithResultOptional.ifPresent(dateFormatWithResult ->
                        {
                            stringBuilder.append(AnyFieldSearchConditionTranslator.OR_SEPARATOR).append(DATE_TRUNC)
                                    .append(LP);
                            stringBuilder.append(SQ).append(dateFormatWithResult[0]).append(SQ)
                                    .append(COMMA).append(SP).append(alias)
                                    .append(PERIOD).append(fieldName);
                            stringBuilder.append(RP).append(SP).append(EQ).append(SP).append(QU);
                            args.add(dateFormatWithResult[1]);
                        });
                    } else
                    {
                        stringBuilder.append(AnyFieldSearchConditionTranslator.OR_SEPARATOR).append(alias)
                                .append(PERIOD).append(fieldName);
                        stringBuilder.append(SP).append(EQ).append(SP).append(QU)
                                .append(DOUBLE_COLON).append(fieldSQLType.toString());
                        args.add(stringValue);
                    }
                }
            } else
            {
                stringBuilder.append(AnyFieldSearchConditionTranslator.OR_SEPARATOR);
                TranslatorUtils.translateStringComparison(alias, fieldName, value, useWildcards,
                        VARCHAR, stringBuilder, args);
            }
        }
    }

    private static void translateUserMatch(final AbstractStringValue value, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final String personJoinInformationKey, final boolean useWildcards)
    {
        sqlBuilder.append(aliases.get(personJoinInformationKey).getSubTableAlias())
                .append(PERIOD).append(USER_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(value.getClass(),
                TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards,
                sqlBuilder, args);
    }

    private static void translateEntityTypeMatch(final AbstractStringValue value, final List<Object> args,
            final StringBuilder sqlBuilder,
            final Map<String, JoinInformation> aliases, final boolean useWildcards)
    {
        sqlBuilder.append(aliases.get(ENTITY_TYPE_JOIN_INFORMATION_KEY).getSubTableAlias())
                .append(PERIOD).append(CODE_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(value.getClass(),
                TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards,
                sqlBuilder, args);
    }

    private static Set<PSQLTypes> findCompatibleSqlTypesForValue(final String value)
    {
        final SimplePropertyValidator validator = new SimplePropertyValidator();
        try
        {
            validator.validatePropertyValue(DataTypeCode.DATE, value);
            return EnumSet.of(PSQLTypes.DATE, TIMESTAMP_WITH_TZ);
        } catch (UserFailureException e1)
        {
            try
            {
                validator.validatePropertyValue(DataTypeCode.TIMESTAMP, value);
                return EnumSet.of(TIMESTAMP_WITH_TZ);
            } catch (UserFailureException e2)
            {
                try
                {
                    validator.validatePropertyValue(DataTypeCode.BOOLEAN, value);
                    return EnumSet.of(BOOLEAN);
                } catch (UserFailureException e3)
                {
                    try
                    {
                        validator.validatePropertyValue(DataTypeCode.INTEGER, value);
                        return EnumSet.of(INT8, INT4, INT2, FLOAT4, FLOAT8);
                    } catch (UserFailureException e4)
                    {
                        try
                        {
                            validator.validatePropertyValue(DataTypeCode.REAL, value);
                            return EnumSet.of(FLOAT4, FLOAT8);
                        } catch (UserFailureException e5)
                        {
                            validator.validatePropertyValue(DataTypeCode.VARCHAR, value);
                            return EnumSet.of(VARCHAR);
                        }
                    }
                }
            }
        }
    }

}
