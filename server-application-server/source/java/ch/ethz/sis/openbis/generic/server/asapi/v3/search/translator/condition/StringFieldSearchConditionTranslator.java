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

import java.util.Arrays;
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
        final String propertyTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();
        TranslatorUtils.appendPropertiesExist(sqlBuilder, propertyTableAlias);

        if (fullPropertyName == null)
        {
            sqlBuilder.append(SP).append(AND).append(SP);
            sqlBuilder.append(LP);
            TranslatorUtils.appendDataTypesSubselect(tableMapper, sqlBuilder, propertyTableAlias);
            sqlBuilder.append(RP).append(SP).append(IN).append(SP).append(LP)
                    .append(SQ).append(String.join(SQ + COMMA + SP + SQ,
                            Arrays.asList(DataTypeCode.VARCHAR.toString(), DataTypeCode.MULTILINE_VARCHAR.toString(),
                                    DataTypeCode.HYPERLINK.toString(), DataTypeCode.XML.toString())
                                    .toArray(new String[0])))
                    .append(SQ).append(RP);
        }

        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        final String casting;
        if (!(value instanceof AnyStringValue))
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

                    DateFieldSearchConditionTranslator.appendConditionForDateOrTimestampProperties(sqlBuilder, args,
                            tableMapper, convertStringValueToDateValue(value), aliases, null, fullPropertyName,
                            bareDateValue, dataType.toString());
                    sqlBuilder.append(RP);
                    return;
                }
            }

            if (fullPropertyName != null)
            {
                TranslatorUtils.appendEntityTypePropertyTypeSubselectConstraint(tableMapper, args, sqlBuilder,
                        fullPropertyName, propertyTableAlias);

            } else
            {
                TranslatorUtils.appendInternalExternalConstraint(tableMapper, args, sqlBuilder,
                        TranslatorUtils.isPropertyInternal(criterion.getFieldName()), propertyTableAlias);
            }
        } else
        {
            casting = null;
        }

        if (fullPropertyName != null)
        {
            if (value instanceof AnyStringValue)
            {
                TranslatorUtils.appendAttributeTypesSubselectConstraint(tableMapper, args, sqlBuilder, fullPropertyName,
                        propertyTableAlias);
            }
        } else if (value instanceof AnyStringValue)
        {
            TranslatorUtils.appendPropertyValueCoalesce(sqlBuilder, tableMapper, aliases);
            sqlBuilder.append(SP).append(IS_NOT_NULL);
            sqlBuilder.append(SP).append(OR).append(SP);
            TranslatorUtils.appendControlledVocabularyTermSubselectConstraint(sqlBuilder, propertyTableAlias);
            sqlBuilder.append(SP).append(OR).append(SP);
            TranslatorUtils.appendMaterialSubselectConstraint(sqlBuilder, propertyTableAlias);
            sqlBuilder.append(SP).append(OR).append(SP);
            TranslatorUtils.appendSampleSubselectConstraint(sqlBuilder, propertyTableAlias);
        }

        final boolean useWildcards = criterion.isUseWildcards();
        if (!(value instanceof AnyStringValue))
        {
            sqlBuilder.append(SP).append(AND).append(NL);

            if (value.getClass() != StringMatchesValue.class)
            {
                sqlBuilder.append(CASE);

                sqlBuilder.append(NL).append(WHEN).append(SP).append(propertyTableAlias).append(PERIOD)
                        .append(VALUE_COLUMN).append(SP).append(IS_NOT_NULL).append(SP).append(THEN).append(SP);

                if (casting != null)
                {
                    sqlBuilder.append(propertyTableAlias).append(PERIOD).append(VALUE_COLUMN);

                    TranslatorUtils.appendStringComparatorOp(value.getClass(),
                            TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards, sqlBuilder, args);
                } else
                {
                    TranslatorUtils.translateStringComparison(propertyTableAlias,
                            VALUE_COLUMN, value, useWildcards, null, sqlBuilder, args);
                }

                if (fullPropertyName != null)
                {
                    sqlBuilder.append(NL).append(WHEN).append(SP)
                            .append(propertyTableAlias).append(PERIOD).append(VOCABULARY_TERM_COLUMN).append(SP)
                            .append(IS_NOT_NULL).append(SP).append(THEN).append(SP);
                    TranslatorUtils.appendControlledVocabularyTermIdSubselectConstraint(args, sqlBuilder, value, useWildcards,
                            propertyTableAlias);
                }

                sqlBuilder.append(NL).append(WHEN).append(SP).append(propertyTableAlias).append(PERIOD)
                        .append(MATERIAL_PROP_COLUMN).append(SP).append(IS_NOT_NULL).append(SP).append(THEN).append(SP);
                TranslatorUtils.appendMaterialSubselectConstraint(args, sqlBuilder, value, useWildcards, propertyTableAlias);

                if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                        || tableMapper == TableMapper.DATA_SET)
                {
                    sqlBuilder.append(NL).append(WHEN).append(SP).append(propertyTableAlias).append(PERIOD)
                            .append(SAMPLE_PROP_COLUMN).append(SP).append(IS_NOT_NULL).append(SP).append(THEN)
                            .append(SP);

                    TranslatorUtils.appendSampleSubselectConstraint(args, sqlBuilder, value, useWildcards, propertyTableAlias);
                }

                sqlBuilder.append(NL).append(ELSE).append(SP).append(FALSE);

                sqlBuilder.append(NL).append(END);
            } else
            {
                appendTsVectorMatch(sqlBuilder, criterion.getFieldValue(),
                        propertyTableAlias, args);
            }

            sqlBuilder.append(NL);
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
