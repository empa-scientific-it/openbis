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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.VARCHAR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.appendTsVectorMatch;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class StringFieldSearchConditionTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{

    private static final Map<Class<? extends AbstractStringValue>, String> OPERATOR_NAME_BY_CLASS = new HashMap<>();

    static
    {
        OPERATOR_NAME_BY_CLASS.put(StringContainsExactlyValue.class, "ContainsExactly");
        OPERATOR_NAME_BY_CLASS.put(StringContainsValue.class, "Contains");
        OPERATOR_NAME_BY_CLASS.put(StringEndsWithValue.class, "EndsWith");
        OPERATOR_NAME_BY_CLASS.put(StringEqualToValue.class, "EqualTo");
        OPERATOR_NAME_BY_CLASS.put(StringGreaterThanOrEqualToValue.class, "GreaterThanOrEqualTo");
        OPERATOR_NAME_BY_CLASS.put(StringGreaterThanValue.class, "GreaterThan");
        OPERATOR_NAME_BY_CLASS.put(StringLessThanOrEqualToValue.class, "LessThanOrEqualTo");
        OPERATOR_NAME_BY_CLASS.put(StringLessThanValue.class, "LessThan");
        OPERATOR_NAME_BY_CLASS.put(StringStartsWithValue.class, "StartsWith");
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion, final TableMapper tableMapper,
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
    public void translate(final StringFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName,
                        tableMapper.getEntitiesTable(), criterionFieldName);
                final AbstractStringValue value = criterion.getFieldValue();
                normalizeValue(value, columnName);

                TranslatorUtils.translateStringComparison(SearchCriteriaTranslator.MAIN_TABLE_ALIAS, columnName, value,
                        criterion.isUseWildcards(), VARCHAR, sqlBuilder, args);
                break;
            }

            case PROPERTY:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                translateStringProperty(criterion, tableMapper, args, sqlBuilder, aliases, dataTypeByPropertyCode,
                        value, criterion.getFieldName());
                break;
            }

            case ANY_PROPERTY:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                translateStringProperty(criterion, tableMapper, args, sqlBuilder, aliases, dataTypeByPropertyCode,
                        value, null);
                break;
            }

            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    private static void translateStringProperty(final StringFieldSearchCriteria criterion,
            final TableMapper tableMapper, final List<Object> args, final StringBuilder sqlBuilder,
            final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyCode,
            final AbstractStringValue value, final String fullPropertyName)
    {
        final JoinInformation joinInformation = aliases.get(tableMapper.getAttributeTypesTable());
        final String entityTypesSubTableAlias = joinInformation.getSubTableAlias();

        sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(joinInformation.getSubTableIdField())
                .append(SP).append(IS_NOT_NULL);

        if (fullPropertyName == null)
        {
            sqlBuilder.append(SP).append(AND).append(SP);
            sqlBuilder.append(aliases.get(DATA_TYPES_TABLE).getSubTableAlias()).append(PERIOD).append(CODE_COLUMN)
                    .append(SP).append(IN).append(SP).append(LP)
                        .append(SQ).append(DataTypeCode.VARCHAR).append(SQ).append(SP).append(COMMA)
                        .append(SQ).append(DataTypeCode.MULTILINE_VARCHAR).append(SQ).append(SP).append(COMMA)
                        .append(SQ).append(DataTypeCode.HYPERLINK).append(SQ).append(SP).append(COMMA)
                        .append(SQ).append(DataTypeCode.XML).append(SQ)
                    .append(RP);
        }

        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        final String casting;
        if (value.getClass() != AnyStringValue.class)
        {
            casting = dataTypeByPropertyCode.get(fullPropertyName);

            if (casting != null)
            {
                verifyCriterionValidity(criterion, value, casting);

                // Delegating translation for boolean properties
                if (casting.equals(DataTypeCode.BOOLEAN.toString()))
                {
                    BooleanFieldSearchConditionTranslator.translateBooleanProperty(tableMapper, args,
                            sqlBuilder, aliases, convertStringValueToBooleanValue(value), fullPropertyName);
                    sqlBuilder.append(RP);
                    return;
                }

                // Delegating translation for number properties
                if (casting.equals(DataTypeCode.INTEGER.toString()) || casting.equals(DataTypeCode.REAL.toString()))
                {

                    NumberFieldSearchConditionTranslator.translateNumberProperty(tableMapper, args, sqlBuilder,
                            aliases, convertStringValueToNumberValue(value), fullPropertyName);
                    sqlBuilder.append(RP);
                    return;
                }

                // Building separate case for timestamps and dates
                if (casting.equals(DataTypeCode.TIMESTAMP.toString())
                        || casting.equals(DataTypeCode.DATE.toString()))
                {
                    final DataType dataType = casting.equals(DataTypeCode.TIMESTAMP.toString())
                            ? DataType.TIMESTAMP : DataType.DATE;
                    final boolean bareDateValue = TranslatorUtils.isDateWithoutTime(value.getValue());

                    sqlBuilder.append(CASE);
                    DateFieldSearchConditionTranslator.appendWhenForDateOrTimestampProperties(sqlBuilder, args,
                            tableMapper, convertStringValueToDateValue(value), aliases, null, fullPropertyName,
                            entityTypesSubTableAlias, bareDateValue, dataType.toString());
                    sqlBuilder.append(SP).append(END);
                    sqlBuilder.append(RP);
                    return;
                }
            }

            sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

            TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias,
                    TranslatorUtils.isPropertyInternal(criterion.getFieldName()));

            if (fullPropertyName != null)
            {
                sqlBuilder.append(SP).append(AND).append(SP);
            }
        } else
        {
            casting = null;
        }

        if (fullPropertyName != null)
        {
            sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD)
                    .append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
            args.add(TranslatorUtils.normalisePropertyName(fullPropertyName));
        }

        final boolean useWildcards = criterion.isUseWildcards();
        if (value.getClass() != AnyStringValue.class)
        {
            sqlBuilder.append(SP).append(THEN).append(NL);

            if (value.getClass() != StringMatchesValue.class)
            {
                sqlBuilder.append(CASE);
                if (fullPropertyName != null)
                {
                    sqlBuilder.append(NL).append(WHEN).append(SP)
                            .append(aliases.get(DATA_TYPES_TABLE).getSubTableAlias()).append(PERIOD).append(CODE_COLUMN)
                            .append(SP).append(EQ).append(SP).append(SQ).append(DataTypeCode.CONTROLLEDVOCABULARY)
                            .append(SQ).append(SP).append(THEN).append(SP);
                    TranslatorUtils.translateStringComparison(
                            aliases.get(CONTROLLED_VOCABULARY_TERM_TABLE).getSubTableAlias(),
                            CODE_COLUMN, value, useWildcards, null, sqlBuilder, args);
                }

                final String materialsTableAlias = aliases.get(MATERIALS_TABLE).getSubTableAlias();
                sqlBuilder.append(NL).append(WHEN).append(SP).append(materialsTableAlias).append(PERIOD)
                        .append(CODE_COLUMN).append(SP).append(IS_NOT_NULL).append(SP).append(THEN).append(SP);
                TranslatorUtils.translateStringComparison(materialsTableAlias, CODE_COLUMN, value, useWildcards, null,
                        sqlBuilder, args);

                final JoinInformation samplesPropertyTable = aliases.get(SAMPLE_PROP_COLUMN);
                if (samplesPropertyTable != null)
                {
                    sqlBuilder.append(NL).append(WHEN).append(SP).append(samplesPropertyTable.getSubTableAlias())
                            .append(PERIOD).append(CODE_COLUMN).append(SP).append(IS_NOT_NULL).append(SP)
                            .append(THEN).append(SP);

                    TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                            CODE_COLUMN, value, useWildcards, null, sqlBuilder, args);

                    sqlBuilder.append(SP).append(OR).append(SP);
                    TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                            PERM_ID_COLUMN, value, useWildcards, null, sqlBuilder, args);

                    sqlBuilder.append(SP).append(OR).append(SP);
                    TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                            SAMPLE_IDENTIFIER_COLUMN, value, useWildcards, null, sqlBuilder, args);
                }

                sqlBuilder.append(NL).append(ELSE).append(SP);

                if (casting != null)
                {
                    final boolean equalsToComparison = (value.getClass() == StringEqualToValue.class);
                    if (equalsToComparison)
                    {
                        sqlBuilder.append(LOWER).append(LP);
                    }
                    sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias()).append(PERIOD)
                            .append(VALUE_COLUMN);
                    if (equalsToComparison)
                    {
                        sqlBuilder.append(RP);
                    }

                    final String strippedValue = TranslatorUtils.stripQuotationMarks(value.getValue())
                            .toLowerCase();

                    TranslatorUtils.appendStringComparatorOp(value.getClass(), strippedValue, useWildcards,
                            sqlBuilder, args);
                } else
                {
                    TranslatorUtils.translateStringComparison(aliases.get(tableMapper.getValuesTable()).getSubTableAlias(),
                            VALUE_COLUMN, value, useWildcards, null, sqlBuilder, args);
                }

                sqlBuilder.append(NL).append(END);
            } else
            {
                appendTsVectorMatch(sqlBuilder, criterion.getFieldValue(),
                        aliases.get(tableMapper.getValuesTable()).getSubTableAlias(), args);
            }

            sqlBuilder.append(NL).append(ELSE).append(SP).append(FALSE).append(NL).append(END);
        }
        sqlBuilder.append(RP);
    }

    private static void verifyCriterionValidity(final StringFieldSearchCriteria criterion,
            final AbstractStringValue value, final String casting)
    {
        AbstractStringValue fieldValue = criterion.getFieldValue();
        if ((fieldValue instanceof StringStartsWithValue ||
                fieldValue instanceof StringEndsWithValue ||
                fieldValue instanceof StringContainsValue ||
                fieldValue instanceof StringContainsExactlyValue) &&
                (casting.equals(DataTypeCode.INTEGER.toString())
                        || casting.equals(DataTypeCode.REAL.toString())
                        || casting.equals(DataTypeCode.TIMESTAMP.toString())
                        || casting.equals(DataTypeCode.DATE.toString())
                        || casting.equals(DataTypeCode.BOOLEAN.toString())))
        {
            throw new UserFailureException(String.format("Operator %s undefined for datatype %s.",
                    OPERATOR_NAME_BY_CLASS.get(value.getClass()), casting));
        }

        if ((fieldValue instanceof StringLessThanValue ||
                fieldValue instanceof StringLessThanOrEqualToValue ||
                fieldValue instanceof StringGreaterThanOrEqualToValue ||
                fieldValue instanceof StringGreaterThanValue) &&
                (casting.equals(DataTypeCode.BOOLEAN.toString())))
        {
            throw new UserFailureException(String.format("Operator %s undefined for datatype %s.",
                    OPERATOR_NAME_BY_CLASS.get(value.getClass()), casting));
        }
    }

    private static IDate convertStringValueToDateValue(final AbstractStringValue stringValue)
    {
        final String value = stringValue.getValue();

        // String validity check.
        try
        {
            TranslatorUtils.parseDate(value);
        } catch (final IllegalArgumentException e)
        {
            throw new UserFailureException(String.format("String does not represent a date: [%s]", value));
        }

        if (stringValue instanceof StringEqualToValue)
        {
            return new DateEqualToValue(value);
        } else if (stringValue instanceof StringLessThanOrEqualToValue)
        {
            return new DateEarlierThanOrEqualToValue(value);
        } else if (stringValue instanceof StringGreaterThanOrEqualToValue)
        {
            return new DateLaterThanOrEqualToValue(value);
        } else if (stringValue instanceof StringLessThanValue)
        {
            return new DateEarlierThanValue(value);
        } else if (stringValue instanceof StringGreaterThanValue)
        {
            return new DateLaterThanValue(value);
        } else
        {
            throw new IllegalArgumentException(String.format("Cannot convert string value of class %s to date value",
                    stringValue.getClass()));
        }
    }

    private static boolean convertStringValueToBooleanValue(final AbstractStringValue stringValue)
    {
        final String value = stringValue.getValue();
        if ("true".equals(value))
        {
            return true;
        }
        if ("false".equals(value))
        {
            return false;
        }

        throw new UserFailureException(String.format("String does not represent a boolean: [%s]", value));
    }

    private static AbstractNumberValue convertStringValueToNumberValue(final AbstractStringValue stringValue)
    {
        final String value = stringValue.getValue();
        Number numberValue;
        try
        {
            numberValue = Long.parseLong(value);
        } catch (final NumberFormatException e1)
        {
            try {
                numberValue = Double.parseDouble(value);
            } catch (final NumberFormatException e2) {
                throw new UserFailureException(String.format("String does not represent a number: [%s]", value));
            }
        }

        if (stringValue instanceof StringEqualToValue)
        {
            return new NumberEqualToValue(numberValue);
        } else if (stringValue instanceof StringLessThanOrEqualToValue)
        {
            return new NumberLessThanOrEqualToValue(numberValue);
        } else if (stringValue instanceof StringGreaterThanOrEqualToValue)
        {
            return new NumberGreaterThanOrEqualToValue(numberValue);
        } else if (stringValue instanceof StringLessThanValue)
        {
            return new NumberLessThanValue(numberValue);
        } else if (stringValue instanceof StringGreaterThanValue)
        {
            return new NumberGreaterThanValue(numberValue);
        } else
        {
            throw new IllegalArgumentException(String.format("Cannot convert string value of class %s to number value",
                    stringValue.getClass()));
        }
    }

    private static void normalizeValue(final AbstractStringValue value, final String columnName)
    {
        if (columnName.equals(CODE_COLUMN) && value.getValue().startsWith("/"))
        {
            value.setValue(value.getValue().substring(1));
        }
    }

}
