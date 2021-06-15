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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.DATA_SET;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class OrderTranslator
{
    public static final String PROPERTY_CODE_ALIAS = "property_code";

    public static final String TYPE_CODE_ALIAS = "type_code";

    private static final String IDENTIFIER = "IDENTIFIER";

    private static final String UNIQUE_PREFIX = OrderTranslator.class.getName() + ":";

    /** Column name in select for sorting by identifier (which is a generated string). */
    private static final String IDENTIFIER_SORTING_COLUMN = "i";

    private static final String QUERY_ALIAS = "q";

    private static final String VALUE_ALIAS = "value";

    public static SelectQuery translateToOrderQuery(final TranslationContext translationContext)
    {
        final SortOptions<?> sortOptions = translationContext.getSortOptions();
        if (sortOptions == null)
        {
            throw new IllegalArgumentException("Null sort options provided.");
        }
        final List<Sorting> sortings = sortOptions.getSortings();
        if (sortings.isEmpty())
        {
            throw new IllegalArgumentException("No sortings in sort options provided.");
        }

        final List<Object> args = translationContext.getArgs();

        // SELECT

        final StringBuilder query  = new StringBuilder(SELECT + SP);

        appendIdsCoalesce(query, sortings.size());
        query.append(SP).append(AS).append(SP).append(ID_COLUMN).append(NL);
        query.append(FROM).append(NL).append(LP);

        // TODO: maybe just string should be returned because args added automatically anyway.
        final SelectQuery subquery1 = translateToOrderSubquery(translationContext, sortings.get(0));

        query.append(subquery1.getQuery());
        query.append(NL).append(RP).append(SP).append(QUERY_ALIAS).append(1).append(NL);

        for (int i = 2; i <= sortings.size(); i++)
        {
            query.append(FULL_OUTER_JOIN).append(NL).append(LP);
            final SelectQuery subquery = translateToOrderSubquery(translationContext, sortings.get(i - 1));
            query.append(subquery.getQuery());
            query.append(NL).append(RP).append(SP).append(QUERY_ALIAS).append(i).append(SP).append(ON).append(SP);
            appendIdsCoalesce(query, i - 1);
            query.append(SP).append(EQ).append(SP).append(QUERY_ALIAS).append(i).append(PERIOD)
                    .append(ID_COLUMN);
            query.append(NL);
        }

        // WHERE

        query.append(WHERE).append(SP);
        appendIdsCoalesce(query, sortings.size());
        query.append(SP).append(IN).append(SP).append(SELECT_UNNEST);
        args.add(translationContext.getIds().toArray(new Long[0]));
        query.append(NL);

        //ORDER BY

        query.append(ORDER_BY).append(SP);
        query.append(QUERY_ALIAS).append(1).append(PERIOD).append(VALUE_ALIAS);
        query.append(SP);
        query.append(sortings.get(0).getOrder()).append(SP).append(NULLS_LAST);

        for (int i = 2; i <= sortings.size(); i++)
        {
            query.append(COMMA).append(SP).append(QUERY_ALIAS).append(i).append(PERIOD).append(VALUE_ALIAS);
            query.append(SP).append(sortings.get(i - 1).getOrder()).append(SP).append(NULLS_LAST);
        }

        return new SelectQuery(query.toString(), args);
    }

    private static void appendIdsCoalesce(final StringBuilder query, final int count)
    {
        query.append(COALESCE + LP + QUERY_ALIAS + 1 + PERIOD + ID_COLUMN);
        for (int i = 2; i <= count; i++)
        {
            query.append(COMMA).append(SP).append(QUERY_ALIAS).append(i).append(PERIOD).append(ID_COLUMN);
        }
        query.append(RP);
    }

    private static SelectQuery translateToOrderSubquery(final TranslationContext translationContext,
            final Sorting sorting)
    {
        final String from = buildFrom(translationContext, sorting);
        final String where = buildWhere(translationContext, sorting);
        final String select = buildSelect(translationContext, sorting);

        return new SelectQuery(select  + NL + from + NL + where, translationContext.getArgs());
    }

    private static String buildOrderBy(final TranslationContext translationContext)
    {
        final StringBuilder orderByBuilder = translationContext.getSortOptions().getSortings().stream().collect(
                StringBuilder::new,
                (stringBuilder, sorting) ->
                {
                    stringBuilder.append(COMMA + SP);
                    appendSortingColumn(translationContext, stringBuilder, sorting, false);
                },
                StringBuilder::append
        );

        return ORDER_BY + orderByBuilder.substring(COMMA.length());
    }

    private static String buildSelect(final TranslationContext translationContext, final Sorting sorting)
    {
        final StringBuilder sqlBuilder = new StringBuilder(
                SELECT + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN);

        final TableMapper tableMapper = translationContext.getTableMapper();
        final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();

        final String sortingCriterionFieldName = sorting.getField();
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriterionFieldName))
        {
            final Map<String, JoinInformation> joinInformationMap = getJoinInformationMap(sorting, aliases);
            sqlBuilder.append(COMMA).append(SP).append(joinInformationMap.get(tableMapper.getAttributeTypesTable())
                    .getSubTableAlias()).append(PERIOD).append(CODE_COLUMN);
            sqlBuilder.append(COMMA).append(SP);

            final String sortingCriteriaFieldName = sorting.getField();
            final String propertyName = sortingCriteriaFieldName.substring(
                    EntityWithPropertiesSortOptions.PROPERTY.length());
            final String casting = translationContext.getDataTypeByPropertyName().get(propertyName);
            if (casting != null)
            {
                sqlBuilder.append(joinInformationMap.get(tableMapper.getValuesTable()).getSubTableAlias()).append(PERIOD)
                        .append(VALUE_COLUMN).append(DOUBLE_COLON).append(casting.toLowerCase());
            } else
            {
                sqlBuilder.append(COALESCE).append(LP);
                sqlBuilder.append(joinInformationMap.get(tableMapper.getValuesTable()).getSubTableAlias()).append(PERIOD)
                        .append(VALUE_COLUMN);
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
        } else
        {
            sqlBuilder.append(COMMA).append(SP);
            appendSortingColumn(translationContext, sqlBuilder, sorting, true);
        }

        sqlBuilder.append(SP).append(AS).append(SP).append(VALUE_ALIAS);

        return sqlBuilder.toString();
    }

    private static String buildFrom(final TranslationContext translationContext, final Sorting sorting)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final StringBuilder sqlBuilder = new StringBuilder(FROM + SP + tableMapper.getEntitiesTable() + SP +
                SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        final AtomicInteger indexCounter = new AtomicInteger(1);

        final String sortingCriterionFieldName = sorting.getField();
        final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();
        final Map<String, JoinInformation> joinInformationMap;
        final Object aliasesMapKey;
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriterionFieldName))
        {
            final String propertyName = sortingCriterionFieldName.
                    substring(EntityWithPropertiesSortOptions.PROPERTY.length()).toLowerCase();
            joinInformationMap = TranslatorUtils.getPropertyJoinInformationMap(tableMapper,
                    () -> getOrderingAlias(indexCounter));
            aliasesMapKey = propertyName;
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

    private static String buildWhere(final TranslationContext translationContext,
            final Sorting sorting)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final StringBuilder sqlBuilder = new StringBuilder(WHERE + SP);

        final String sortingCriterionFieldName = sorting.getField();
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriterionFieldName))
        {
            final String fullPropertyName = sorting.getField().substring(EntityWithPropertiesSortOptions.PROPERTY.length());
            sqlBuilder.append(getJoinInformationMap(sorting, translationContext.getAliases())
                    .get(tableMapper.getAttributeTypesTable()).getSubTableAlias())
                    .append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
            translationContext.getArgs().add(TranslatorUtils.normalisePropertyName(fullPropertyName));
            return sqlBuilder.toString();
        } else
        {
            return "";
        }
    }

    private static Map<String, JoinInformation> getJoinInformationMap(final Sorting sorting,
            final Map<Object, Map<String, JoinInformation>> aliases)
    {
        return aliases.get(sorting.getField().substring(
                EntityWithPropertiesSortOptions.PROPERTY.length()).toLowerCase());
    }

    /**
     * Appends sorting column to SQL builder. In some cases it can be more columns. Adds type casting when needed.
     *
     * @param translationContext order translation context.
     * @param sqlBuilder string builder to which the column should be appended.
     * @param sorting sorting parameters.
     * @param inSelect {@code true} if this method is used in the {@code SELECT} clause.
     */
    private static void appendSortingColumn(final TranslationContext translationContext, final StringBuilder sqlBuilder, final Sorting sorting,
            final boolean inSelect)
    {
        final String sortingCriteriaFieldName = sorting.getField();
        final Map<String, JoinInformation> aliases = translationContext.getAliases().get(UNIQUE_PREFIX);
        final TableMapper tableMapper = translationContext.getTableMapper();
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriteriaFieldName))
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
            final String typesTableAlias = translationContext.getAliases().get(EntityWithPropertiesSortOptions.TYPE).get(tableMapper.getEntityTypesTable()).
                    getSubTableAlias();
            sqlBuilder.append(typesTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingByIdentifierCriterion(sortingCriteriaFieldName))
        {
            if (inSelect)
            {
                if (tableMapper != TableMapper.SAMPLE)
                {
                    final JoinInformation entitiesTableAlias = aliases.get(tableMapper.getEntitiesTable());
                    final JoinInformation spacesTableAlias = aliases.get(UNIQUE_PREFIX + SPACES_TABLE);
                    final JoinInformation projectsTableAlias = aliases.get(UNIQUE_PREFIX + PROJECTS_TABLE);
                    buildFullIdentifierConcatenationString(sqlBuilder,
                            (spacesTableAlias != null) ? spacesTableAlias.getSubTableAlias() : null,
                            (projectsTableAlias != null) ? projectsTableAlias.getSubTableAlias() : null,
                            (entitiesTableAlias != null) ? entitiesTableAlias.getSubTableAlias() : null, false);
                } else
                {
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN);
                }
            }
        } else if (isSortingByMaterialPermId(translationContext, sortingCriteriaFieldName))
        {
            final String materialTypeTableAlias = translationContext.getAliases().get(EntityWithPropertiesSortOptions.TYPE)
                    .get(tableMapper.getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);

            if (!inSelect)
            {
                sqlBuilder.append(SP).append(sorting.getOrder());
            }

            sqlBuilder.append(COMMA).append(SP).append(materialTypeTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingBySpaceModificationDate(translationContext, sortingCriteriaFieldName))
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(REGISTRATION_TIMESTAMP_COLUMN.toLowerCase());
        } else
        {
            final String lowerCaseSortingCriteriaFieldName = sortingCriteriaFieldName.toLowerCase();
            final String fieldName = AttributesMapper.getColumnName(lowerCaseSortingCriteriaFieldName, tableMapper.getEntitiesTable(),
                    lowerCaseSortingCriteriaFieldName);
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
        }

        if (!inSelect)
        {
            sqlBuilder.append(SP).append(sorting.getOrder());
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

    public static SelectQuery translateToSearchTypeQuery(final TranslationContext translationContext)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final String queryString = SELECT + SP + DISTINCT + SP + "o3" + PERIOD + CODE_COLUMN + SP + PROPERTY_CODE_ALIAS + COMMA + SP +
                "o4" + PERIOD + CODE_COLUMN + SP + TYPE_CODE_ALIAS + NL +
                FROM + SP + tableMapper.getAttributeTypesTable() + SP + "o3" + SP + NL +
                INNER_JOIN + SP + DATA_TYPES_TABLE + SP + "o4" + SP +
                ON + SP + "o3" + PERIOD + tableMapper.getAttributeTypesTableDataTypeIdField() + SP + EQ + SP + "o4" + PERIOD + ID_COLUMN + NL +
                WHERE + SP + "o4" + PERIOD + CODE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;

        return new SelectQuery(queryString, Collections.singletonList(translationContext.getTypesToFilter()));
    }

}
