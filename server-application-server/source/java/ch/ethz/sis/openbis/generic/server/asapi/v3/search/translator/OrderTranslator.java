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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntitySortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringMatchesValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions.ANY_PROPERTY_SCORE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter.MATCH_VALUE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter.PREFIX_MATCH_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.RANK_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class OrderTranslator
{

    private static final String IDENTIFIER = "IDENTIFIER";

    private static final String UNIQUE_PREFIX = OrderTranslator.class.getName() + ":";

    private static final String QUERY_ALIAS = "q";

    private static final String VALUE_ALIAS = "value";

    public static SelectQuery translateToOrderQuery(final TranslationContext translationContext)
    {
        final SortOptions<?> sortOptions = translationContext.getSortOptions();
        if (sortOptions == null)
        {
            throw new IllegalArgumentException("Null sort options provided.");
        }
        if (sortOptions.getSortings().isEmpty())
        {
            throw new IllegalArgumentException("No sortings in sort options provided.");
        }

        final StringBuilder queryBuilder = new StringBuilder();

        appendSelect(queryBuilder, translationContext);
        appendFrom(queryBuilder, translationContext);
        appendWhere(queryBuilder, translationContext);
        appendOrderBy(queryBuilder, translationContext);

        return new SelectQuery(queryBuilder.toString(), translationContext.getArgs());
    }

    private static void appendSelect(final StringBuilder queryBuilder, final TranslationContext translationContext)
    {
        queryBuilder.append(SELECT).append(SP);
        final List<Sorting> sortings = translationContext.getSortOptions().getSortings();
        appendIdsCoalesce(queryBuilder, sortings.size());
        queryBuilder.append(SP).append(AS).append(SP).append(ID_COLUMN).append(NL);
    }

    private static void appendFrom(final StringBuilder queryBuilder, final TranslationContext translationContext)
    {
        final List<Sorting> sortings = translationContext.getSortOptions().getSortings();
        queryBuilder.append(FROM).append(NL).append(LP);

        appendOrderSubquery(queryBuilder, translationContext, sortings.get(0));

        queryBuilder.append(NL).append(RP).append(SP).append(QUERY_ALIAS).append(1).append(NL);

        for (int i = 2; i <= sortings.size(); i++)
        {
            queryBuilder.append(FULL_OUTER_JOIN).append(NL).append(LP);
            appendOrderSubquery(queryBuilder, translationContext, sortings.get(i - 1));
            queryBuilder.append(NL).append(RP).append(SP).append(QUERY_ALIAS).append(i).append(SP).append(ON)
                    .append(SP);
            appendIdsCoalesce(queryBuilder, i - 1);
            queryBuilder.append(SP).append(EQ).append(SP).append(QUERY_ALIAS).append(i).append(PERIOD)
                    .append(ID_COLUMN);
            queryBuilder.append(NL);
        }
    }

    private static void appendWhere(final StringBuilder queryBuilder, final TranslationContext translationContext)
    {
        final List<Sorting> sortings = translationContext.getSortOptions().getSortings();
        queryBuilder.append(WHERE).append(SP);
        appendIdsCoalesce(queryBuilder, sortings.size());
        queryBuilder.append(SP).append(IN).append(SP).append(SELECT_UNNEST);
        translationContext.getArgs().add(translationContext.getIds().toArray(new Long[0]));
        queryBuilder.append(NL);
    }

    private static void appendOrderBy(final StringBuilder queryBuilder, final TranslationContext translationContext)
    {
        final List<Sorting> sortings = translationContext.getSortOptions().getSortings();
        queryBuilder.append(ORDER_BY).append(SP);

        appendSorting(queryBuilder, translationContext, 1);
        for (int i = 2; i <= sortings.size(); i++)
        {
            queryBuilder.append(COMMA).append(SP);
            appendSorting(queryBuilder, translationContext, i);
        }
    }

    private static void appendSorting(final StringBuilder queryBuilder, final TranslationContext translationContext,
            final int i)
    {
        final List<Sorting> sortings = translationContext.getSortOptions().getSortings();
        final Sorting sorting = sortings.get(i - 1);

        final String field = sorting.getField();
        if (TranslatorUtils.isPropertyScoreSortingFieldName(field) ||
                TranslatorUtils.isAnyPropertyScoreSortingFieldName(field))
        {
            queryBuilder.append(RANK_ALIAS).append(SP).append(sorting.getOrder());
        } else
        {
            queryBuilder.append(QUERY_ALIAS).append(i).append(PERIOD).append(VALUE_ALIAS);
            queryBuilder.append(SP).append(sorting.getOrder()).append(SP).append(NULLS_LAST);
        }
    }

    private static void appendRankCalculation(final StringBuilder sqlBuilder,
            final TranslationContext translationContext, final Sorting sorting)
    {
        final Map<SortParameter, String> parameters = sorting.getParameters();
        final String matchValue = parameters.get(MATCH_VALUE);
        final String prefixMatchValue = parameters.get(PREFIX_MATCH_VALUE);
        final AbstractStringValue stringValue = matchValue != null
                ? new StringMatchesValue(matchValue)
                : new StringStartsWithValue(prefixMatchValue);

        final Map<String, JoinInformation> joinInformationMap = getJoinInformationMap(sorting,
                translationContext.getAliases());

        final TableMapper tableMapper = translationContext.getTableMapper();
        final JoinInformation samplesTableJoinInformation = joinInformationMap.get(SAMPLE_PROP_COLUMN);
        GlobalSearchCriteriaTranslator.appendRankCalculation(sqlBuilder, tableMapper, false, stringValue,
                translationContext.getArgs(), MAIN_TABLE_ALIAS,
                joinInformationMap.get(MATERIALS_TABLE).getSubTableAlias(),
                samplesTableJoinInformation != null ? samplesTableJoinInformation.getSubTableAlias() : null);
    }

    private static void appendIdsCoalesce(final StringBuilder query, final int count)
    {
        if (count >= 2)
        {
            query.append(COALESCE + LP + QUERY_ALIAS + 1 + PERIOD + ID_COLUMN);
            for (int i = 2; i <= count; i++)
            {
                query.append(COMMA).append(SP).append(QUERY_ALIAS).append(i).append(PERIOD).append(ID_COLUMN);
            }
            query.append(RP);
        } else
        {
            query.append(QUERY_ALIAS + 1 + PERIOD + ID_COLUMN);
        }
    }

    private static void appendOrderSubquery(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final Sorting sorting)
    {
        final boolean anyPropertySortingFieldName = TranslatorUtils.isAnyPropertyScoreSortingFieldName(
                sorting.getField());

        final String from = buildSubqueryFrom(translationContext, sorting);
        final String select = buildSubquerySelect(translationContext, sorting);
        final String where = buildSubqueryWhere(translationContext, sorting);

        sqlBuilder.append(select).append(NL).append(from).append(NL);

        if (anyPropertySortingFieldName)
        {
            final String groupBy = buildSubqueryGroupBy(translationContext);
            sqlBuilder.append(groupBy).append(NL);
        }

        sqlBuilder.append(where);
    }

    private static String buildSubqueryGroupBy(final TranslationContext translationContext)
    {
        return GROUP_BY + SP + MAIN_TABLE_ALIAS + PERIOD
                + translationContext.getTableMapper().getValuesTableEntityIdField();
    }

    private static String buildSubquerySelect(final TranslationContext translationContext, final Sorting sorting)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();

        final String sortingCriterionFieldName = sorting.getField();
        final boolean propertyScoreSortingFieldName = TranslatorUtils.isPropertyScoreSortingFieldName(
                sortingCriterionFieldName);
        final boolean propertySortingFieldName = TranslatorUtils.isPropertySortingFieldName(sortingCriterionFieldName);
        final boolean anyPropertyScoreSortingFieldName = TranslatorUtils.isAnyPropertyScoreSortingFieldName(
                sortingCriterionFieldName);

        final StringBuilder sqlBuilder = new StringBuilder(SELECT + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS
                + PERIOD
                + ((propertyScoreSortingFieldName || propertySortingFieldName || anyPropertyScoreSortingFieldName)
                        ? translationContext.getTableMapper().getValuesTableEntityIdField() : ID_COLUMN)
                + SP + AS + SP + ID_COLUMN);

        if (!anyPropertyScoreSortingFieldName)
        {
            if (propertyScoreSortingFieldName || propertySortingFieldName)
            {
                final Map<String, JoinInformation> joinInformationMap = getJoinInformationMap(sorting, aliases);
                sqlBuilder.append(COMMA).append(SP);

                final String sortingCriteriaFieldName = sorting.getField();
                final String propertyName = trimFieldName(sortingCriteriaFieldName);
                final String casting = translationContext.getDataTypeByPropertyName().get(propertyName);
                if (casting != null)
                {
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(VALUE_COLUMN).append(DOUBLE_COLON)
                            .append(casting.toLowerCase());
                } else
                {
                    TranslatorUtils.appendPropertyValueCoalesceForOrder(sqlBuilder, tableMapper, joinInformationMap);
                }
            } else
            {
                sqlBuilder.append(COMMA).append(SP);
                appendSortingColumn(translationContext, sqlBuilder, sorting);
            }

            sqlBuilder.append(SP).append(AS).append(SP).append(VALUE_ALIAS);
        }

        if (sorting.getParameters() != null)
        {
            sqlBuilder.append(COMMA).append(SP);

            if (anyPropertyScoreSortingFieldName)
            {
                sqlBuilder.append(SUM).append(LP);
            }

            appendRankCalculation(sqlBuilder, translationContext, sorting);

            if (anyPropertyScoreSortingFieldName)
            {
                sqlBuilder.append(RP);
            }

            sqlBuilder.append(SP).append(AS).append(SP).append(RANK_ALIAS);
        }

        return sqlBuilder.toString();
    }

    private static String trimFieldName(final String sortingCriterionFieldName)
    {
        if (TranslatorUtils.isAnyPropertyScoreSortingFieldName(sortingCriterionFieldName))
        {
            return sortingCriterionFieldName;
        } else
        {
            final boolean propertyScoreSortingFieldName = TranslatorUtils.isPropertyScoreSortingFieldName(
                    sortingCriterionFieldName);
            return sortingCriterionFieldName.substring(propertyScoreSortingFieldName
                    ? EntityWithPropertiesSortOptions.PROPERTY_SCORE.length()
                    : EntityWithPropertiesSortOptions.PROPERTY.length());
        }
    }

    private static String buildSubqueryFrom(final TranslationContext translationContext, final Sorting sorting)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final AtomicInteger indexCounter = new AtomicInteger(1);

        final String sortingCriterionFieldName = sorting.getField();
        final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();
        final Map<String, JoinInformation> joinInformationMap;
        final Object aliasesMapKey;
        final boolean propertyScoreSortingFieldName =
                TranslatorUtils.isPropertyScoreSortingFieldName(sortingCriterionFieldName);
        final boolean propertySortingFieldName = TranslatorUtils.isPropertySortingFieldName(sortingCriterionFieldName);
        final boolean anyPropertyScoreSortingFieldName =
                TranslatorUtils.isAnyPropertyScoreSortingFieldName(sortingCriterionFieldName);

        final StringBuilder sqlBuilder = new StringBuilder(FROM + SP +
                ((propertyScoreSortingFieldName || propertySortingFieldName || anyPropertyScoreSortingFieldName)
                        ? tableMapper.getValuesTable() : tableMapper.getEntitiesTable()) +
                SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS);

        if (propertyScoreSortingFieldName || propertySortingFieldName)
        {
            joinInformationMap = TranslatorUtils.getPropertyJoinInformationMapForOrder(tableMapper,
                    () -> getOrderingAlias(indexCounter));
            aliasesMapKey = trimFieldName(sortingCriterionFieldName).toLowerCase();
        } else if (anyPropertyScoreSortingFieldName)
        {
            joinInformationMap = TranslatorUtils.getPropertyJoinInformationMapForOrder(tableMapper,
                    () -> getOrderingAlias(indexCounter));
            aliasesMapKey = ANY_PROPERTY_SCORE;
        } else if (isTypeSearchCriterion(sortingCriterionFieldName) ||
                isSortingByMaterialPermId(translationContext, sortingCriterionFieldName))
        {
            joinInformationMap = TranslatorUtils.getTypeJoinInformationMap(tableMapper,
                    () -> getOrderingAlias(indexCounter));
            aliasesMapKey = EntityWithPropertiesSortOptions.TYPE;
        } else if (isSortingByIdentifierCriterion(sortingCriterionFieldName) && tableMapper != TableMapper.SAMPLE)
        {
            joinInformationMap = TranslatorUtils.getIdentifierJoinInformationMap(tableMapper,
                    () -> getOrderingAlias(indexCounter), UNIQUE_PREFIX);
            aliasesMapKey = UNIQUE_PREFIX;
        } else
        {
            joinInformationMap = null;
            aliasesMapKey = null;
        }

        if (joinInformationMap != null)
        {
            joinInformationMap.values().forEach(
                    (joinInformation) -> TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
            aliases.put(aliasesMapKey, joinInformationMap);
        }
        return sqlBuilder.toString();
    }

    private static String buildSubqueryWhere(final TranslationContext translationContext,
            final Sorting sorting)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final String sortingCriterionFieldName = sorting.getField();
        final boolean propertyScoreSortingFieldName =
                TranslatorUtils.isPropertyScoreSortingFieldName(sortingCriterionFieldName);
        final boolean propertySortingFieldName = TranslatorUtils.isPropertySortingFieldName(sortingCriterionFieldName);
        if (propertyScoreSortingFieldName || propertySortingFieldName)
        {
            final String fullPropertyName = trimFieldName(sorting.getField());

            final String entityTypePropertyTypesTableAlias = "etpt";
            final String attributeTypesTableAlias = "at";

            final String result = WHERE + SP + MAIN_TABLE_ALIAS + PERIOD
                    + tableMapper.getValuesTableEntityTypeAttributeTypeIdField() + SP + IN + NL
                    + LP + NL
                    + '\t' + SELECT + SP + entityTypePropertyTypesTableAlias + PERIOD + ID_COLUMN + NL
                    + '\t' + FROM + SP + tableMapper.getEntityTypesAttributeTypesTable() + SP
                    + entityTypePropertyTypesTableAlias + NL
                    + '\t' + LEFT_JOIN + SP + tableMapper.getAttributeTypesTable() + SP + attributeTypesTableAlias
                    + SP + ON + SP
                    + entityTypePropertyTypesTableAlias + PERIOD
                    + tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()
                    + SP + EQ + SP + attributeTypesTableAlias + PERIOD + ID_COLUMN + NL
                    + '\t' + WHERE + SP + attributeTypesTableAlias + PERIOD + CODE_COLUMN + SP + EQ + SP + QU
                    + NL + RP;
            translationContext.getArgs().add(TranslatorUtils.normalisePropertyName(fullPropertyName));
            return result;
        } else
        {
            return "";
        }
    }

    private static Map<String, JoinInformation> getJoinInformationMap(final Sorting sorting,
            final Map<Object, Map<String, JoinInformation>> aliases)
    {
        final String field = sorting.getField();
        final String trimmedField = field.equals(ANY_PROPERTY_SCORE) ? ANY_PROPERTY_SCORE
                : trimFieldName(field).toLowerCase();
        return aliases.get(trimmedField);
    }

    /**
     * Appends sorting column to SQL builder. In some cases it can be more columns. Adds type casting when needed.
     *
     * @param translationContext order translation context.
     * @param sqlBuilder string builder to which the column should be appended.
     * @param sorting sorting parameters.
     */
    private static void appendSortingColumn(final TranslationContext translationContext, final StringBuilder sqlBuilder,
            final Sorting sorting)
    {
        final String sortingCriteriaFieldName = sorting.getField();
        final Map<String, JoinInformation> aliases = translationContext.getAliases().get(UNIQUE_PREFIX);
        final TableMapper tableMapper = translationContext.getTableMapper();
        if (TranslatorUtils.isPropertySortingFieldName(sortingCriteriaFieldName))
        {
            final String propertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
            final String propertyNameLowerCase = propertyName.toLowerCase();
            final String valuesTableAlias = translationContext.getAliases().get(propertyNameLowerCase).get(tableMapper.getEntityTypesAttributeTypesTable()).getMainTableAlias();
            sqlBuilder.append(valuesTableAlias).append(PERIOD).append(VALUE_COLUMN);

            final String casting = translationContext.getDataTypeByPropertyName().get(propertyName);
            if (casting != null)
            {
                sqlBuilder.append(DOUBLE_COLON).append(casting.toLowerCase());
            }
        } else if (isTypeSearchCriterion(sortingCriteriaFieldName))
        {
            final String typesTableAlias = translationContext.getAliases().get(EntityWithPropertiesSortOptions.TYPE)
                    .get(tableMapper.getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(typesTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingByIdentifierCriterion(sortingCriteriaFieldName))
        {
            if (tableMapper != TableMapper.SAMPLE)
            {
                final JoinInformation entitiesTableAlias = aliases.get(tableMapper.getEntitiesTable());
                final JoinInformation spacesTableAlias = aliases.get(UNIQUE_PREFIX + SPACES_TABLE);
                final JoinInformation projectsTableAlias = aliases.get(UNIQUE_PREFIX + PROJECTS_TABLE);
                buildFullIdentifierConcatenationString(sqlBuilder,
                        (spacesTableAlias != null) ? spacesTableAlias.getSubTableAlias() : null,
                        (projectsTableAlias != null) ? projectsTableAlias.getSubTableAlias() : null,
                        (entitiesTableAlias != null) ? entitiesTableAlias.getSubTableAlias() : null);
            } else
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN);
            }
        } else if (isSortingByMaterialPermId(translationContext, sortingCriteriaFieldName))
        {
            final String materialTypeTableAlias = translationContext.getAliases()
                    .get(EntityWithPropertiesSortOptions.TYPE)
                    .get(tableMapper.getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
            sqlBuilder.append(COMMA).append(SP).append(materialTypeTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingBySpaceModificationDate(translationContext, sortingCriteriaFieldName))
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD)
                    .append(REGISTRATION_TIMESTAMP_COLUMN.toLowerCase());
        } else
        {
            final String lowerCaseSortingCriteriaFieldName = sortingCriteriaFieldName.toLowerCase();
            final String fieldName = AttributesMapper.getColumnName(lowerCaseSortingCriteriaFieldName,
                    tableMapper.getEntitiesTable(), lowerCaseSortingCriteriaFieldName);
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
        }
    }

    private static boolean isSortingBySpaceModificationDate(final TranslationContext translationContext, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.MODIFICATION_DATE.equals(sortingCriteriaFieldName) && translationContext.getTableMapper() == TableMapper.SPACE;
    }

    private static boolean isSortingByMaterialPermId(final TranslationContext translationContext, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.PERM_ID.equals(sortingCriteriaFieldName) && translationContext.getTableMapper() == TableMapper.MATERIAL;
    }

    private static String getOrderingAlias(final AtomicInteger num)
    {
        return "o" + num.getAndIncrement();
    }

    private static boolean isSortingByIdentifierCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(IDENTIFIER);
    }

    private static boolean isTypeSearchCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(EntityWithPropertiesSortOptions.TYPE);
    }

}
