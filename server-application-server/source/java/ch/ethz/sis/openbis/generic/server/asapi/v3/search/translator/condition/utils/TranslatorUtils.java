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

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.DATE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.TIMESTAMP_WITHOUT_TZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.DATA_SET;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.SAMPLE;
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

    public static final String DATA_TYPE_ALIAS = "dt";

    public static final String PROPERTY_TYPE_ALIAS = "pt";

    public static final String ENTITY_TYPE_PROPERTY_TYPE_ALIAS = "etpt";

    public static final DateTimeFormatter DATE_WITHOUT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(new ShortDateFormat().getFormat());

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
        if (tableAlias != null)
        {
            sqlBuilder.append(tableAlias).append(PERIOD);
        }
        sqlBuilder.append(columnName);
        if (casting != null)
        {
            sqlBuilder.append(DOUBLE_COLON).append(casting);
        }

        appendStringComparatorOp(value.getClass(), TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards,
                sqlBuilder, args);
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

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.LEFT);
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getValuesTable());
        joinInformation1.setSubTableAlias(valuesTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getValuesTableEntityIdField());
        result.put(tableMapper.getValuesTable(), joinInformation1);

        return result;
    }

    public static Map<String, JoinInformation> getPropertyJoinInformationMapForOrder(final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.LEFT);
        joinInformation1.setMainTable(tableMapper.getValuesTable());
        joinInformation1.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(VOCABULARY_TERM_COLUMN);
        joinInformation1.setSubTable(CONTROLLED_VOCABULARY_TERM_TABLE);
        joinInformation1.setSubTableAlias(aliasFactory.createAlias());
        joinInformation1.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(CONTROLLED_VOCABULARY_TERM_TABLE, joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.LEFT);
        joinInformation2.setMainTable(tableMapper.getValuesTable());
        joinInformation2.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation2.setMainTableIdField(MATERIAL_PROP_COLUMN);
        joinInformation2.setSubTable(TableMapper.MATERIAL.getEntitiesTable());
        joinInformation2.setSubTableAlias(aliasFactory.createAlias());
        joinInformation2.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(TableMapper.MATERIAL.getEntitiesTable(), joinInformation2);

        if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                || tableMapper == TableMapper.DATA_SET)
        {
            final JoinInformation joinInformation3 = new JoinInformation();
            joinInformation3.setJoinType(JoinType.LEFT);
            joinInformation3.setMainTable(tableMapper.getValuesTable());
            joinInformation3.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
            joinInformation3.setMainTableIdField(SAMPLE_PROP_COLUMN);
            joinInformation3.setSubTable(TableMapper.SAMPLE.getEntitiesTable());
            joinInformation3.setSubTableAlias(aliasFactory.createAlias());
            joinInformation3.setSubTableIdField(ColumnNames.ID_COLUMN);
            result.put(SAMPLE_PROP_COLUMN, joinInformation3);
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

    public static boolean isPropertySortingFieldName(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.startsWith(EntityWithPropertiesSortOptions.PROPERTY);
    }

    public static boolean isPropertyScoreSortingFieldName(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.startsWith(EntityWithPropertiesSortOptions.PROPERTY_SCORE);
    }

    public static boolean isAnyPropertyScoreSortingFieldName(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.startsWith(EntityWithPropertiesSortOptions.ANY_PROPERTY_SCORE);
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
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || coalesce([samplesTableAlias].code || ':', '') || t0.code
     * </pre>
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || t0.code
     * </pre>
     *
     * @param sqlBuilder         query builder.
     * @param spacesTableAlias   alias of the spaces table.
     * @param projectsTableAlias alias of the projects table.
     * @param samplesTableAlias  alias of the samples table, {@code null} indicates that the table should not be included.
     */
    public static void buildFullIdentifierConcatenationString(final StringBuilder sqlBuilder, final String spacesTableAlias,
            final String projectsTableAlias, final String samplesTableAlias)
    {
        final String slash = "/";
        final String colon = ":";
        sqlBuilder.append(SQ).append(slash).append(SQ).append(SP).append(BARS);

        if (spacesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, spacesTableAlias, slash);
        }
        if (projectsTableAlias != null)
        {
            appendCoalesce(sqlBuilder, projectsTableAlias, slash);
        }
        if (samplesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, samplesTableAlias, colon);
        }

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
    }

    /**
     * Appends the following text to sqlBuilder.
     * <pre>
     *     coalesce([alias].code || '[separator]', '') ||
     * </pre>
     *
     * @param sqlBuilder query builder.
     * @param alias      alias of the table.
     * @param separator  string to be appender at the end in the first parameter.
     */
    private static void appendCoalesce(final StringBuilder sqlBuilder, final String alias, final String separator)
    {
        sqlBuilder.append(SP).append(COALESCE).append(LP).append(alias).append(PERIOD).append(CODE_COLUMN)
                .append(SP).append(BARS).append(SP)
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

    public static void appendTsVectorMatch(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final String alias, final List<Object> args)
    {
        if ("".equals(stringValue.getValue()))
        {
            sqlBuilder.append(true);
        } else
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
    }

    public static Map<String, JoinInformation> getFieldJoinInformationMap(final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = getPropertyJoinInformationMap(tableMapper,
                aliasFactory);
        appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, "");

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

    public static void appendPropertyValueCoalesce(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final Map<String, JoinInformation> joinInformationMap)
    {
        sqlBuilder.append(COALESCE).append(LP);
        sqlBuilder.append(joinInformationMap.get(tableMapper.getValuesTable()).getSubTableAlias()).append(PERIOD)
                .append(VALUE_COLUMN);
        sqlBuilder.append(RP);
    }

    public static void appendPropertyValueCoalesceForOrder(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final Map<String, JoinInformation> joinInformationMap)
    {
        sqlBuilder.append(COALESCE).append(LP);
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(VALUE_COLUMN);
        sqlBuilder.append(COMMA).append(SP);
        sqlBuilder.append(joinInformationMap.get(CONTROLLED_VOCABULARY_TERM_TABLE).getSubTableAlias()).append(PERIOD)
                .append(CODE_COLUMN);
        sqlBuilder.append(COMMA).append(SP);
        sqlBuilder.append(joinInformationMap.get(MATERIALS_TABLE).getSubTableAlias()).append(PERIOD)
                .append(CODE_COLUMN);
        if (tableMapper == SAMPLE || tableMapper == EXPERIMENT || tableMapper == DATA_SET)
        {
            sqlBuilder.append(COMMA).append(SP);
            sqlBuilder.append(joinInformationMap.get(SAMPLE_PROP_COLUMN).getSubTableAlias()).append(PERIOD)
                    .append(CODE_COLUMN);
        }
        sqlBuilder.append(RP);
    }

    public static void appendSampleSubselectConstraint(final List<Object> args, final StringBuilder sqlBuilder,
            final AbstractStringValue value, final boolean useWildcards, final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(SAMPLE_PROP_COLUMN).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(SAMPLES_VIEW).append(SP)
                .append(WHERE).append(SP);

        translateStringComparison(null, CODE_COLUMN, value, useWildcards, null, sqlBuilder, args);

        sqlBuilder.append(SP).append(OR).append(SP);
        translateStringComparison(null, PERM_ID_COLUMN, value, useWildcards, null, sqlBuilder, args);

        sqlBuilder.append(SP).append(OR).append(SP);
        translateStringComparison(null, SAMPLE_IDENTIFIER_COLUMN, value, useWildcards, null, sqlBuilder, args);

        sqlBuilder.append(RP);
    }

    public static void appendSampleSubselectConstraint(final StringBuilder sqlBuilder, final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(SAMPLE_PROP_COLUMN).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(SAMPLES_VIEW);
        sqlBuilder.append(RP);
    }

    public static void appendMaterialSubselectConstraint(final List<Object> args, final StringBuilder sqlBuilder,
            final AbstractStringValue value, final boolean useWildcards, final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(MATERIAL_PROP_COLUMN).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);

        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(MATERIALS_TABLE).append(SP)
                .append(WHERE).append(SP);
        translateStringComparison(null, CODE_COLUMN, value, useWildcards, null, sqlBuilder, args);

        sqlBuilder.append(RP);
    }

    public static void appendMaterialSubselectConstraint(final StringBuilder sqlBuilder,
            final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(MATERIAL_PROP_COLUMN).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(MATERIALS_TABLE);
        sqlBuilder.append(RP);
    }

    public static void appendDataTypesSubselect(final TableMapper tableMapper, final StringBuilder sqlBuilder,
            final String propertyTableAlias)
    {
        sqlBuilder.append(SELECT).append(SP).append(DATA_TYPE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(NL)
                .append(FROM).append(SP).append(DATA_TYPES_TABLE).append(SP).append(DATA_TYPE_ALIAS).append(NL)
                .append(LEFT_JOIN).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(SP).append(ON).append(SP)
                .append(DATA_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(tableMapper.getAttributeTypesTableDataTypeIdField())
                .append(NL)
                .append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(SP).append(ON).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(NL)
                .append(WHERE).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP).append(EQ).append(SP).append(propertyTableAlias).append(PERIOD)
                .append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
    }

    public static void appendAttributeTypesSubselectConstraint(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final String fullPropertyName, final String propertyTableAlias)
    {
        sqlBuilder.append(LP);
        TranslatorUtils.appendAttributeTypesSubselect(tableMapper, sqlBuilder, propertyTableAlias);
        sqlBuilder.append(RP).append(SP).append(EQ).append(SP).append(QU);

        args.add(TranslatorUtils.normalisePropertyName(fullPropertyName));
    }

    public static void appendAttributeTypesSubselect(final TableMapper tableMapper, final StringBuilder sqlBuilder,
            final String propertyTableAlias)
    {
        sqlBuilder.append(SELECT).append(SP).append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(NL)
                .append(FROM).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(NL)
                .append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(SP).append(ON).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(NL)
                .append(WHERE).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP).append(EQ).append(SP).append(propertyTableAlias).append(PERIOD)
                .append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
    }

    public static void appendControlledVocabularyTermIdSubselectConstraint(final List<Object> args,
            final StringBuilder sqlBuilder, final AbstractStringValue value, final boolean useWildcards,
            final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD).append(VOCABULARY_TERM_COLUMN)
                .append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);

        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(CONTROLLED_VOCABULARY_TERM_TABLE).append(SP)
                .append(WHERE).append(SP);
        translateStringComparison(null, CODE_COLUMN, value, useWildcards, null, sqlBuilder, args);

        sqlBuilder.append(RP);
    }

    public static void appendControlledVocabularyTermSubselectConstraint(final StringBuilder sqlBuilder,
            final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(VOCABULARY_TERM_COLUMN).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);

        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(CONTROLLED_VOCABULARY_TERM_TABLE).append(SP);

        sqlBuilder.append(RP);
    }

    public static void appendEntityTypePropertyTypeSubselectConstraint(final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final String propertyTypeCode,
            final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP)
                .append(FROM).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(SP)
                .append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(SP)
                .append(ON).append(SP).append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP).append(EQ).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(SP)
                .append(WHERE).append(SP).append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(CODE_COLUMN)
                .append(SP).append(EQ).append(SP).append(QU);
        args.add(normalisePropertyName(propertyTypeCode));

        sqlBuilder.append(SP).append(AND).append(SP);
        sqlBuilder.append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(IS_MANAGED_INTERNALLY).append(SP).append(EQ)
                .append(SP).append(QU);
        args.add(isPropertyInternal(propertyTypeCode));

        sqlBuilder.append(RP);
    }

    public static void appendInternalExternalConstraint(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final boolean internal, final String propertyTableAlias)
    {
        sqlBuilder.append(propertyTableAlias).append(PERIOD)
                .append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP)
                .append(FROM).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                .append(PROPERTY_TYPE_ALIAS).append(SP)
                .append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                .append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(SP)
                .append(ON).append(SP).append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(SP).append(EQ).append(SP).append(ENTITY_TYPE_PROPERTY_TYPE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(SP)
                .append(WHERE).append(SP);

        sqlBuilder.append(PROPERTY_TYPE_ALIAS).append(PERIOD).append(IS_MANAGED_INTERNALLY).append(SP).append(EQ)
                .append(SP).append(QU);
        args.add(internal);

        sqlBuilder.append(RP);
    }

    public static void appendPropertiesExist(final StringBuilder sqlBuilder, final String propertiesTableAlias)
    {
        sqlBuilder.append(propertiesTableAlias).append(PERIOD).append(ID_COLUMN).append(SP).append(IS_NOT_NULL);
    }

}
