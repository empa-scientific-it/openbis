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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IAliasFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.DATE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.TIMESTAMP_WITHOUT_TZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.toTsQueryText;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class TranslatorUtils
{

    public static final String REGISTRATOR_JOIN_INFORMATION_KEY = "registrator";

    public static final String MODIFIER_JOIN_INFORMATION_KEY = "modifier";

    public static final String ENTITY_TYPE_JOIN_INFORMATION_KEY = "entity_type";

    public static final String UNIQUE_PREFIX = TranslatorUtils.class.getName();

    public static final DateTimeFormatter DATE_WITHOUT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(new ShortDateFormat().getFormat());

    public static final DateTimeFormatter DATE_WITH_SHORT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(new NormalDateFormat().getFormat());

    public static final DateTimeFormatter DATE_WITHOUT_TIMEZONE_FORMATTER =
            DateTimeFormatter.ofPattern(new LongDateFormat().getFormat());

    /** Indicator that the property is internal. */
    private static final String INTERNAL_PROPERTY_PREFIX = "$";

    private TranslatorUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void translateStringComparison(final String tableAlias, final String columnName,
            final AbstractStringValue value, final boolean useWildcards, final PSQLTypes casting,
            final StringBuilder sqlBuilder, final List<Object> args)
    {
        sqlBuilder.append(LOWER).append(LP).append(tableAlias).append(PERIOD).append(columnName).append(RP);
        if (casting != null)
        {
            sqlBuilder.append(DOUBLE_COLON).append(casting);
        }

        final String strippedValue = TranslatorUtils.stripQuotationMarks(value.getValue()).toLowerCase();
        appendStringComparatorOp(value.getClass(), strippedValue, useWildcards, sqlBuilder, args);
    }

    public static void appendStringComparatorOp(final AbstractStringValue value, final boolean useWildcards,
            final StringBuilder sqlBuilder, final List<Object> args)
    {
        appendStringComparatorOp(value.getClass(), value.getValue(), useWildcards, sqlBuilder, args);
    }

    public static void appendStringComparatorOp(final Class<?> valueClass, final String finalValue,
            final boolean useWildcards, final StringBuilder sqlBuilder, final List<Object> args)
    {
        sqlBuilder.append(SP);
        if (valueClass == StringEqualToValue.class)
        {
            if (useWildcards && containsWildcards(finalValue))
            {
                sqlBuilder.append(ILIKE).append(SP).append(QU);
                args.add(toPSQLWildcards(finalValue));
            } else
            {
                sqlBuilder.append(EQ).append(SP).append(QU);
                args.add(finalValue);
            }
        } else if (valueClass == StringLessThanValue.class)
        {
            sqlBuilder.append(LT).append(SP).append(QU);
            args.add(finalValue);
        } else if (valueClass == StringLessThanOrEqualToValue.class)
        {
            sqlBuilder.append(LE).append(SP).append(QU);
            args.add(finalValue);
        } else if (valueClass == StringGreaterThanValue.class)
        {
            sqlBuilder.append(GT).append(SP).append(QU);
            args.add(finalValue);
        } else if (valueClass == StringGreaterThanOrEqualToValue.class)
        {
            sqlBuilder.append(GE).append(SP).append(QU);
            args.add(finalValue);
        } else if (valueClass == StringStartsWithValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add((useWildcards ? toPSQLWildcards(finalValue) : escapePSQLWildcards(finalValue)) + PERCENT);
        } else if (valueClass == StringEndsWithValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add(PERCENT + (useWildcards ? toPSQLWildcards(finalValue) : escapePSQLWildcards(finalValue)));
        } else if (valueClass == StringContainsValue.class || valueClass == StringContainsExactlyValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add(PERCENT + (useWildcards ? toPSQLWildcards(finalValue) : escapePSQLWildcards(finalValue)) + PERCENT);
        } else if (valueClass == AnyStringValue.class)
        {
            sqlBuilder.append(IS_NOT_NULL);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractStringValue type: " + valueClass.getSimpleName());
        }
    }

    /**
     * Determines whether the string contains search wildcards.
     *
     * @param str string to be checked.
     * @return {@code true} if the string contains '*' or '?'.
     */
    private static boolean containsWildcards(final String str)
    {
        return str.matches(".*[*?].*");
    }

    /**
     * Changes '*' -> '%' and '?' -> '_' to match PSQL pattern matching standards. Escapes already existing '%', '_' and '\' characters with '\'.
     *
     * @param str string to be converted.
     * @return string that corresponds to the PSQL standard.
     */
    private static String toPSQLWildcards(final String str)
    {
        final StringBuilder sb = new StringBuilder();
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            final char ch = chars[i];
            if (i > 0 && chars[i - 1] == BACKSLASH)
            {
                sb.append(ch);
            } else
            {
                switch (ch)
                {
                    case UNDERSCORE:
                        // Fall through.
                    case PERCENT:
                    {
                        sb.append(BACKSLASH).append(ch);
                        break;
                    }
                    case BACKSLASH:
                    {
                        break;
                    }
                    case ASTERISK:
                    {
                        sb.append(PERCENT);
                        break;
                    }
                    case QU:
                    {
                        sb.append(UNDERSCORE);
                        break;
                    }
                    default:
                    {
                        sb.append(ch);
                        break;
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Escapes already existing '%', '_' and '\' characters with '\'.
     *
     * @param str string to be converted.
     * @return string that corresponds to the PSQL standard.
     */
    private static String escapePSQLWildcards(final String str)
    {
        final StringBuilder sb = new StringBuilder();
        final char[] chars = str.toCharArray();
        for (final char ch : chars)
        {
            switch (ch)
            {
                case UNDERSCORE:
                    // Fall through.
                case PERCENT:
                {
                    sb.append(BACKSLASH).append(ch);
                    break;
                }
                case BACKSLASH:
                {
                    break;
                }
                default:
                {
                    sb.append(ch);
                    break;
                }
            }
        }

        return sb.toString();
    }

    public static void appendNumberComparatorOp(final AbstractNumberValue value, final StringBuilder sqlBuilder)
    {
        if (value.getClass() == NumberEqualToValue.class)
        {
            sqlBuilder.append(EQ);
        } else if (value.getClass() == NumberLessThanValue.class)
        {
            sqlBuilder.append(LT);
        } else if (value.getClass() == NumberLessThanOrEqualToValue.class)
        {
            sqlBuilder.append(LE);
        } else if (value.getClass() == NumberGreaterThanValue.class)
        {
            sqlBuilder.append(GT);
        } else if (value.getClass() == NumberGreaterThanOrEqualToValue.class)
        {
            sqlBuilder.append(GE);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractNumberValue type: " + value.getClass().getSimpleName());
        }
        sqlBuilder.append(SP).append(QU);
    }

    public static Map<String, JoinInformation> getPropertyJoinInformationMap(final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String valuesTableAlias = aliasFactory.createAlias();
        final String entityTypesAttributeTypesTableAlias = aliasFactory.createAlias();
        final String attributeTypesTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.LEFT);
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getValuesTable());
        joinInformation1.setSubTableAlias(valuesTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getValuesTableEntityIdField());
        result.put(tableMapper.getValuesTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.LEFT);
        joinInformation2.setMainTable(tableMapper.getValuesTable());
        joinInformation2.setMainTableAlias(valuesTableAlias);
        joinInformation2.setMainTableIdField(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
        joinInformation2.setSubTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation2.setSubTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesAttributeTypesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setJoinType(JoinType.LEFT);
        joinInformation3.setMainTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation3.setMainTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation3.setMainTableIdField(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
        joinInformation3.setSubTable(tableMapper.getAttributeTypesTable());
        joinInformation3.setSubTableAlias(attributeTypesTableAlias);
        joinInformation3.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getAttributeTypesTable(), joinInformation3);

        final JoinInformation joinInformation4 = new JoinInformation();
        joinInformation4.setJoinType(JoinType.LEFT);
        joinInformation4.setMainTable(tableMapper.getAttributeTypesTable());
        joinInformation4.setMainTableAlias(attributeTypesTableAlias);
        joinInformation4.setMainTableIdField(tableMapper.getAttributeTypesTableDataTypeIdField());
        joinInformation4.setSubTable(TableNames.DATA_TYPES_TABLE);
        joinInformation4.setSubTableAlias(aliasFactory.createAlias());
        joinInformation4.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(TableNames.DATA_TYPES_TABLE, joinInformation4);

        final JoinInformation joinInformation5 = new JoinInformation();
        joinInformation5.setJoinType(JoinType.LEFT);
        joinInformation5.setMainTable(tableMapper.getValuesTable());
        joinInformation5.setMainTableAlias(valuesTableAlias);
        joinInformation5.setMainTableIdField(VOCABULARY_TERM_COLUMN);
        joinInformation5.setSubTable(CONTROLLED_VOCABULARY_TERM_TABLE);
        joinInformation5.setSubTableAlias(aliasFactory.createAlias());
        joinInformation5.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(CONTROLLED_VOCABULARY_TERM_TABLE, joinInformation5);

        final JoinInformation joinInformation6 = new JoinInformation();
        joinInformation6.setJoinType(JoinType.LEFT);
        joinInformation6.setMainTable(tableMapper.getValuesTable());
        joinInformation6.setMainTableAlias(valuesTableAlias);
        joinInformation6.setMainTableIdField(MATERIAL_PROP_COLUMN);
        joinInformation6.setSubTable(TableMapper.MATERIAL.getEntitiesTable());
        joinInformation6.setSubTableAlias(aliasFactory.createAlias());
        joinInformation6.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(TableMapper.MATERIAL.getEntitiesTable(), joinInformation6);

        if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                || tableMapper == TableMapper.DATA_SET)
        {
            final String samplePropertyAlias = aliasFactory.createAlias();
            final JoinInformation joinInformation7 = new JoinInformation();
            joinInformation7.setJoinType(JoinType.LEFT);
            joinInformation7.setMainTable(TableMapper.SAMPLE.getValuesTable());
            joinInformation7.setMainTableAlias(valuesTableAlias);
            joinInformation7.setMainTableIdField(SAMPLE_PROP_COLUMN);
            joinInformation7.setSubTable(TableMapper.SAMPLE.getEntitiesTable());
            joinInformation7.setSubTableAlias(samplePropertyAlias);
            joinInformation7.setSubTableIdField(ColumnNames.ID_COLUMN);
            result.put(SAMPLE_PROP_COLUMN, joinInformation7);
        }

        return result;
    }

    public static Map<String, JoinInformation> getTypeJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();

        final JoinInformation joinInformation = new JoinInformation();
        joinInformation.setJoinType(JoinType.LEFT);
        joinInformation.setMainTable(tableMapper.getEntitiesTable());
        joinInformation.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation.setMainTableIdField(tableMapper.getEntitiesTableEntityTypeIdField());
        joinInformation.setSubTable(tableMapper.getEntityTypesTable());
        joinInformation.setSubTableAlias(aliasFactory.createAlias());
        joinInformation.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesTable(), joinInformation);

        return result;
    }

    public static Map<String, JoinInformation> getRelationshipsJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String relationshipsTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.LEFT);
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getRelationshipsTable());
        joinInformation1.setSubTableAlias(relationshipsTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getRelationshipsTableParentIdField());
        result.put(tableMapper.getRelationshipsTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.LEFT);
        joinInformation2.setMainTable(tableMapper.getRelationshipsTable());
        joinInformation2.setMainTableAlias(relationshipsTableAlias);
        joinInformation2.setMainTableIdField(tableMapper.getRelationshipsTableChildIdField());
        joinInformation2.setSubTable(tableMapper.getEntitiesTable());
        joinInformation2.setSubTableAlias(aliasFactory.createAlias());
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntitiesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setJoinType(JoinType.LEFT);
        joinInformation3.setMainTable(tableMapper.getRelationshipsTable());
        joinInformation3.setMainTableAlias(relationshipsTableAlias);
        joinInformation3.setMainTableIdField(RELATIONSHIP_COLUMN);
        joinInformation3.setSubTable(RELATIONSHIP_TYPES_TABLE);
        joinInformation3.setSubTableAlias(aliasFactory.createAlias());
        joinInformation3.setSubTableIdField(ID_COLUMN);
        result.put(RELATIONSHIP_TYPES_TABLE, joinInformation3);

        return result;
    }

    public static void appendCastedTimestamp(final StringBuilder sqlBuilder, final String columnName, final IDate fieldValue)
    {
        sqlBuilder.append(columnName);
        if (fieldValue instanceof AbstractDateValue)
        {
            // String type date value.
            final String dateString = ((AbstractDateValue) fieldValue).getValue();
            try
            {
                DATE_WITHOUT_TIMEZONE_FORMATTER.parse(dateString);
            } catch (final DateTimeParseException e1)
            {
                try
                {
                    DATE_WITH_SHORT_TIME_FORMATTER.parse(dateString);
                } catch (final DateTimeParseException e2)
                {
                    try
                    {
                        DATE_WITHOUT_TIME_FORMATTER.parse(dateString);
                        sqlBuilder.append(DOUBLE_COLON).append(DATE);
                    } catch (final DateTimeParseException e3)
                    {
                        throw new IllegalArgumentException("Illegal date [dateString='" + dateString + "']", e3);
                    }
                }
            }
        }
    }

    public static void appendTimeZoneConversion(final IDate fieldValue, final StringBuilder sqlBuilder, final ITimeZone timeZone)
    {
        if (fieldValue instanceof AbstractDateValue && timeZone instanceof TimeZone)
        {
            final TimeZone timeZoneImpl = (TimeZone) timeZone;
            final ZoneId zoneId = ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-timeZoneImpl.getHourOffset()));

            sqlBuilder.append(SP).append(AT_TIME_ZONE).append(SP).append(SQ).append(zoneId.getId()).append(SQ);
        }
    }

    public static void addDateValueToArgs(final IDate fieldValue, final List<Object> args)
    {
        if (fieldValue instanceof AbstractDateValue)
        {
            // String type date value.
            args.add(parseDate(((AbstractDateValue) fieldValue).getValue()));
        } else
        {
            // Date type date value.
            args.add(((AbstractDateObjectValue) fieldValue).getValue());
        }
    }

    public static Date parseDate(final String dateString)
    {
        try
        {
            return DATE_HOURS_MINUTES_SECONDS_FORMAT.parse(dateString);
        } catch (final ParseException e1)
        {
            try
            {
                return DATE_HOURS_MINUTES_FORMAT.parse(dateString);
            } catch (ParseException e2)
            {
                try
                {
                    return DATE_FORMAT.parse(dateString);
                } catch (final ParseException e3)
                {
                    throw new IllegalArgumentException("Illegal date [dateString='" + dateString + "']", e3);
                }
            }
        }
    }

    public static boolean isDateWithoutTime(final String dateString)
    {
        try
        {
            DATE_WITHOUT_TIME_FORMATTER.parse(dateString);
            return true;
        } catch (final DateTimeParseException e)
        {
            return false;
        }
    }

    public static void appendDateComparatorOp(final IDate fieldValue, final StringBuilder sqlBuilder,
            final List<Object> args, final boolean castToDate)
    {
        if (fieldValue instanceof DateEqualToValue || fieldValue instanceof DateObjectEqualToValue)
        {
            sqlBuilder.append(EQ);
        } else if (fieldValue instanceof DateEarlierThanOrEqualToValue
                || fieldValue instanceof DateObjectEarlierThanOrEqualToValue)
        {
            sqlBuilder.append(LE);
        } else if (fieldValue instanceof DateLaterThanOrEqualToValue
                || fieldValue instanceof DateObjectLaterThanOrEqualToValue)
        {
            sqlBuilder.append(GE);
        } else if (fieldValue instanceof DateEarlierThanValue
                || fieldValue instanceof DateObjectEarlierThanValue)
        {
            sqlBuilder.append(LT);
        } else if (fieldValue instanceof DateLaterThanValue
                || fieldValue instanceof DateObjectLaterThanValue)
        {
            sqlBuilder.append(GT);
        } else
        {
            throw new IllegalArgumentException("Unsupported field value: " + fieldValue.getClass().getSimpleName());
        }
        sqlBuilder.append(SP);

        if (castToDate)
        {
            sqlBuilder.append(LP);
        }

        sqlBuilder.append(QU).append(DOUBLE_COLON).append(TIMESTAMP_WITHOUT_TZ.toString());

        if (castToDate)
        {
            sqlBuilder.append(RP).append(DOUBLE_COLON).append(DATE);
        }

        TranslatorUtils.addDateValueToArgs(fieldValue, args);
    }

    public static boolean isPropertyInternal(final String propertyName)
    {
        return propertyName.startsWith(INTERNAL_PROPERTY_PREFIX);
    }

    public static String normalisePropertyName(final String propertyName)
    {
        return isPropertyInternal(propertyName) ? propertyName.substring(INTERNAL_PROPERTY_PREFIX.length()) : propertyName;
    }

    public static void appendInternalExternalConstraint(final StringBuilder sqlBuilder, final List<Object> args,
            final String entityTypesSubTableAlias, final boolean internalProperty)
    {
        sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(ColumnNames.IS_MANAGED_INTERNALLY).append(SP)
                .append(EQ).append(SP).append(QU);
        args.add(internalProperty);
    }

    public static void appendJoin(final StringBuilder sqlBuilder, final JoinInformation joinInformation)
    {
        if (joinInformation.getSubTable() != null)
        {
            sqlBuilder.append(NL).append(joinInformation.getJoinType()).append(SP).append(JOIN).append(SP).append(joinInformation.getSubTable())
                    .append(SP).append(joinInformation.getSubTableAlias()).append(SP)
                    .append(ON).append(SP).append(joinInformation.getMainTableAlias())
                    .append(PERIOD).append(joinInformation.getMainTableIdField())
                    .append(SP)
                    .append(EQ).append(SP).append(joinInformation.getSubTableAlias()).append(PERIOD)
                    .append(joinInformation.getSubTableIdField());
        }
    }

    /**
     * Appends given string to string builder only when atomic boolean is false. Otherwise just sets atomic boolean to false.
     *
     * @param sb string builder to be updated.
     * @param value the value to be added when needed.
     * @param first atomic boolean, if {@code true} it will be set to false with no change to sb, otherwise the {@code value} will be appended to
     * {@code sb}.
     */
    public static void appendIfNotFirst(final StringBuilder sb, final String value, final AtomicBoolean first)
    {
        if (first.get())
        {
            first.set(false);
        } else
        {
            sb.append(value);
        }
    }

    public static boolean isPropertySearchFieldName(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.startsWith(EntityWithPropertiesSortOptions.PROPERTY);
    }

    public static Object convertStringToType(final String value, final Class<?> klass)
    {
        // Integer numbers need to be converted from string to a real number first, because they can be presented with decimal point.
        if (Boolean.class == klass)
        {
            return Boolean.parseBoolean(value);
        }
        if (Byte.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (byte) decimalValue;
        }
        if (Short.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (short) decimalValue;
        }
        if (Integer.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (int) decimalValue;
        }
        if (Long.class == klass)
        {
            final double decimalValue = Double.parseDouble(value);
            return (long) decimalValue;
        }
        if (Float.class == klass)
        {
            return Float.parseFloat(value);
        }
        if (Double.class == klass)
        {
            return Double.parseDouble(value);
        }
        if (Date.class == klass)
        {
            return parseDate(value);
        }

        return value;
    }

    public static String stripQuotationMarks(final String value)
    {
        if (value.startsWith("\"") && value.endsWith("\""))
        {
            return value.substring(1, value.length() - 1);
        } else
        {
            return value;
        }
    }

    /**
     * Appends the following test to {@code sqlBuilder}.
     * <pre>
     *     t0.code || '(' || [entityTypesTableAlias].code || ')'
     * </pre>
     * @param sqlBuilder query builder.
     * @param entityTypesTableAlias alias of the entity type table.
     */
    public static void buildTypeCodeIdentifierConcatenationString(final StringBuilder sqlBuilder, final String entityTypesTableAlias)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(BARS).append(SP)
                .append(SQ).append(" (").append(SQ).append(SP).append(BARS).append(SP).append(entityTypesTableAlias).append(PERIOD)
                .append(CODE_COLUMN).append(SP).append(BARS).append(SP).append(SQ).append(")").append(SQ);
    }

    /**
     * Appends one of the the following texts to {@code sqlBuilder} depending on the value of {@code samplesTableAlias}. If it is {@code null} the second
     * version will be appended.
     *
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || coalesce([samplesTableAlias].code || ':', '') || t0.code
     * </pre>
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || t0.code
     * </pre>
     * @param sqlBuilder query builder.
     * @param spacesTableAlias alias of the spaces table.
     * @param projectsTableAlias alias of the projects table.
     * @param samplesTableAlias alias of the samples table, {@code null} indicates that the table should not be included.
     * @param useLowerCase
     */
    public static void buildFullIdentifierConcatenationString(final StringBuilder sqlBuilder, final String spacesTableAlias,
            final String projectsTableAlias, final String samplesTableAlias, final boolean useLowerCase)
    {
        final String slash = "/";
        final String colon = ":";
        sqlBuilder.append(SQ).append(slash).append(SQ).append(SP).append(BARS);

        if (spacesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, spacesTableAlias, slash, useLowerCase);
        }
        if (projectsTableAlias != null)
        {
            appendCoalesce(sqlBuilder, projectsTableAlias, slash, useLowerCase);
        }
        if (samplesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, samplesTableAlias, colon, useLowerCase);
        }

        if (useLowerCase)
        {
            sqlBuilder.append(SP).append(LOWER).append(LP);
        }
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        if (useLowerCase)
        {
            sqlBuilder.append(RP);
        }
    }

    /**
     * Appends the following text to sqlBuilder.
     *
     * <pre>
     *     coalesce([alias].code || '[separator]', '') ||
     * </pre>
     * @param sqlBuilder query builder.
     * @param alias alias of the table.
     * @param separator string to be appender at the end in the first parameter.
     * @param useLowerCase whether to convert search column to lower case.
     */
    private static void appendCoalesce(final StringBuilder sqlBuilder, final String alias, final String separator,
            final boolean useLowerCase)
    {
        sqlBuilder.append(SP).append(COALESCE).append(LP);
        if (useLowerCase)
        {
            sqlBuilder.append(LOWER).append(LP);
        }
        sqlBuilder.append(alias).append(PERIOD).append(CODE_COLUMN);
        if (useLowerCase)
        {
            sqlBuilder.append(RP);
        }
        sqlBuilder.append(SP).append(BARS).append(SP)
                .append(SQ).append(separator).append(SQ).append(COMMA).append(SP).append(SQ).append(SQ).append(RP)
                .append(SP).append(BARS);
    }

    public static Map<String, JoinInformation> getIdentifierJoinInformationMap(final TableMapper tableMapper,
            final IAliasFactory aliasFactory, final String prefix)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, prefix);
        return result;
    }

    public static void appendIdentifierJoinInformationMap(final Map<String, JoinInformation> result, final TableMapper tableMapper,
            final IAliasFactory aliasFactory, final String prefix)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        final String samplesTableName = TableMapper.SAMPLE.getEntitiesTable();
        final String projectsTableName = TableMapper.PROJECT.getEntitiesTable();
        final String experimentsTableName = TableMapper.EXPERIMENT.getEntitiesTable();

        if (entitiesTable.equals(samplesTableName) || entitiesTable.equals(projectsTableName))
        {
            // Only samples and projects can have spaces.
            final JoinInformation spacesJoinInformation = createSpacesJoinInformation(aliasFactory, entitiesTable, MAIN_TABLE_ALIAS);
            result.put(prefix + SPACES_TABLE, spacesJoinInformation);
        }

        if (entitiesTable.equals(samplesTableName) || entitiesTable.equals(experimentsTableName)) {
            final String projectsTableAlias = aliasFactory.createAlias();
            final JoinInformation projectsJoinInformation = new JoinInformation();
            projectsJoinInformation.setJoinType(JoinType.LEFT);
            projectsJoinInformation.setMainTable(entitiesTable);
            projectsJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            projectsJoinInformation.setMainTableIdField(PROJECT_COLUMN);
            projectsJoinInformation.setSubTable(PROJECTS_TABLE);
            projectsJoinInformation.setSubTableAlias(projectsTableAlias);
            projectsJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(prefix + PROJECTS_TABLE, projectsJoinInformation);

            if (entitiesTable.equals(experimentsTableName)) {
                // Experiments link to spaces via projects.
                final JoinInformation experimentsSpacesJoinInformation = createSpacesJoinInformation(aliasFactory, entitiesTable, projectsTableAlias);
                result.put(prefix + SPACES_TABLE, experimentsSpacesJoinInformation);
            }
        }

        if (entitiesTable.equals(samplesTableName))
        {
            // Only samples can have containers.
            final JoinInformation containerSampleJoinInformation = new JoinInformation();
            containerSampleJoinInformation.setJoinType(JoinType.LEFT);
            containerSampleJoinInformation.setMainTable(entitiesTable);
            containerSampleJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            containerSampleJoinInformation.setMainTableIdField(PART_OF_SAMPLE_COLUMN);
            containerSampleJoinInformation.setSubTable(entitiesTable);
            containerSampleJoinInformation.setSubTableAlias(aliasFactory.createAlias());
            containerSampleJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(prefix + entitiesTable, containerSampleJoinInformation);
        }
    }

    private static JoinInformation createSpacesJoinInformation(IAliasFactory aliasFactory, String entitiesTable, String mainTableAlias)
    {
        final JoinInformation spacesJoinInformation = new JoinInformation();
        spacesJoinInformation.setJoinType(JoinType.LEFT);
        spacesJoinInformation.setMainTable(entitiesTable);
        spacesJoinInformation.setMainTableAlias(mainTableAlias);
        spacesJoinInformation.setMainTableIdField(SPACE_COLUMN);
        spacesJoinInformation.setSubTable(SPACES_TABLE);
        spacesJoinInformation.setSubTableAlias(aliasFactory.createAlias());
        spacesJoinInformation.setSubTableIdField(ID_COLUMN);
        return spacesJoinInformation;
    }

    public static void appendTsVectorMatch(final StringBuilder sqlBuilder, final AbstractStringValue stringValue, final String alias, final List<Object> args)
    {
        final String tsQueryValue = toTsQueryText(stringValue);
        sqlBuilder.append(alias).append(PERIOD)
                .append(TS_VECTOR_COLUMN).append(SP).append(DOUBLE_AT)
                .append(SP).append(LP).append(QU).append(DOUBLE_COLON).append(TSQUERY)
                .append(SP).append(BARS).append(SP)
                .append(TO_TSQUERY).append(LP).append(QU).append(RP).append(RP);
        args.add(tsQueryValue);
        args.add(tsQueryValue);
    }

    public static Map<String, JoinInformation> getFieldJoinInformationMap(final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = getPropertyJoinInformationMap(tableMapper,
                aliasFactory);
        appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, UNIQUE_PREFIX);

        if (tableMapper.hasRegistrator())
        {
            final JoinInformation registratorJoinInformation = new JoinInformation();
            registratorJoinInformation.setJoinType(JoinType.LEFT);
            registratorJoinInformation.setMainTable(tableMapper.getEntitiesTable());
            registratorJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            registratorJoinInformation.setMainTableIdField(PERSON_REGISTERER_COLUMN);
            registratorJoinInformation.setSubTable(PERSONS_TABLE);
            registratorJoinInformation.setSubTableAlias(aliasFactory.createAlias());
            registratorJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(REGISTRATOR_JOIN_INFORMATION_KEY, registratorJoinInformation);
        }

        if (tableMapper.hasModifier())
        {
            final JoinInformation registratorJoinInformation = new JoinInformation();
            registratorJoinInformation.setJoinType(JoinType.LEFT);
            registratorJoinInformation.setMainTable(tableMapper.getEntitiesTable());
            registratorJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            registratorJoinInformation.setMainTableIdField(PERSON_MODIFIER_COLUMN);
            registratorJoinInformation.setSubTable(PERSONS_TABLE);
            registratorJoinInformation.setSubTableAlias(aliasFactory.createAlias());
            registratorJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(MODIFIER_JOIN_INFORMATION_KEY, registratorJoinInformation);
        }

        final JoinInformation typeJoinInformation = new JoinInformation();
        typeJoinInformation.setJoinType(JoinType.LEFT);
        typeJoinInformation.setMainTable(tableMapper.getEntitiesTable());
        typeJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
        typeJoinInformation.setMainTableIdField(tableMapper.getEntitiesTableEntityTypeIdField());
        typeJoinInformation.setSubTable(tableMapper.getEntityTypesTable());
        typeJoinInformation.setSubTableAlias(aliasFactory.createAlias());
        typeJoinInformation.setSubTableIdField(ID_COLUMN);
        result.put(ENTITY_TYPE_JOIN_INFORMATION_KEY, typeJoinInformation);

        return result;
    }

    public static String getAlias(final AtomicInteger num)
    {
        return "t" + num.getAndIncrement();
    }

}
