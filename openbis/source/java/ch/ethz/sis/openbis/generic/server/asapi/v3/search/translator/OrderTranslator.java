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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.Attributes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COMMA;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DISTINCT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INNER_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LEFT_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ORDER_BY;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.UNNEST;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.VALUE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_TYPES_TABLE;

public class OrderTranslator
{
    public static final String PROPERTY_CODE_ALIAS = "property_code";

    public static final String TYPE_CODE_ALIAS = "type_code";

    private static String getOrderingAlias(final AtomicInteger num)
    {
        return "o" + num.getAndIncrement();
    }

    public static SelectQuery translateToOrderQuery(final OrderTranslationVo vo)
    {
        if (vo.getSortOptions() == null)
        {
            throw new IllegalArgumentException("Null sort options provided.");
        }

        final String from = buildOrderFrom(vo);
        final String where = buildOrderWhere(vo);
        final String select = buildOrderSelect(vo);
        final String orderBy = buildOrderOrderBy(vo);

        return new SelectQuery(select  + NL + from + NL + where + NL + orderBy, vo.getArgs());
    }

    private static String buildOrderOrderBy(final OrderTranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder(ORDER_BY + SP);
        final AtomicBoolean first = new AtomicBoolean(true);

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            appendIfFirst(sqlBuilder, COMMA + SP, first);
            appendSortingColumn(vo, sqlBuilder, sorting.getField());
            sqlBuilder.append(SP).append(sorting.getOrder());
        });

        return sqlBuilder.toString();
    }

    /**
     * Appends given string to string builder only when atomic boolean is false. Otherwise just sets atomic boolean to false.
     *
     * @param sb string builder to be updated.
     * @param value the value to be added when needed.
     * @param first atomic boolean, if {@code true} it will be set to false with no change to sb, otherwise the {@code value} will be appended to
     * {@code sb}.
     */
    static void appendIfFirst(final StringBuilder sb, final String value, final AtomicBoolean first)
    {
        if (first.get())
        {
            first.set(false);
        } else
        {
            sb.append(value);
        }
    }

    private static String buildOrderSelect(final OrderTranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder(SELECT + SP + DISTINCT + SP + CriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN);

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            sqlBuilder.append(COMMA).append(SP);
            appendSortingColumn(vo, sqlBuilder, sorting.getField());
        });

        return sqlBuilder.toString();
    }

    private static String buildOrderFrom(final OrderTranslationVo vo)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final StringBuilder sqlBuilder = new StringBuilder(FROM + SP + tableMapper.getEntitiesTable() + SP + CriteriaTranslator.MAIN_TABLE_ALIAS);
        final AtomicInteger indexCounter = new AtomicInteger(1);

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriterionFieldName = sorting.getField();
            if (CriteriaTranslator.isPropertySearchCriterion(sortingCriterionFieldName))
            {
                final String propertyName = sortingCriterionFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length()).toLowerCase();
                final Map<String, JoinInformation> joinInformationMap = TranslatorUtils.getPropertyJoinInformationMap(tableMapper,
                        () -> getOrderingAlias(indexCounter));

                joinInformationMap.values().forEach((joinInformation) ->
                {
                    TranslatorUtils.appendJoin(sqlBuilder, joinInformation, LEFT_JOIN);
                });
                vo.getAliases().put(propertyName, joinInformationMap);
            } else if (isTypeSearchCriterion(sortingCriterionFieldName))
            {
                final Map<String, JoinInformation> joinInformationMap = TranslatorUtils.getTypeJoinInformationMap(tableMapper,
                        () -> getOrderingAlias(indexCounter));
                joinInformationMap.values().forEach((joinInformation) ->
                {
                    TranslatorUtils.appendJoin(sqlBuilder, joinInformation, LEFT_JOIN);
                });
                vo.getAliases().put(EntityWithPropertiesSortOptions.TYPE, joinInformationMap);
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildOrderWhere(final OrderTranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder(WHERE + SP + CriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN + SP + IN + SP +
                LP + SELECT + SP + UNNEST + LP + QU + RP + RP);
        final Map<Object, Map<String, JoinInformation>> aliases = vo.getAliases();
        final TableMapper tableMapper = vo.getTableMapper();
        final List<Object> args = vo.getArgs();

        args.add(vo.ids.toArray(new Long[0]));

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriteriaFieldName = sorting.getField();
            if (CriteriaTranslator.isPropertySearchCriterion(sortingCriteriaFieldName))
            {
                final String propertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
                final String attributeTypesTableAlias = aliases.get(propertyName.toLowerCase()).get(tableMapper.getAttributeTypesTable()).
                        getMainTableAlias();
                sqlBuilder.append(SP).append(AND).append(SP).append(attributeTypesTableAlias).append(PERIOD).append(CODE_COLUMN).append(SP).
                        append(EQ).append(SP).append(QU);
                args.add(propertyName);
            }
        });

        return sqlBuilder.toString();
    }

    /**
     * Appends sorting column to SQL builder. Adds type casting when needed.
     *
     * @param vo order translation value object.
     * @param sqlBuilder string builder to which the column should be appended.
     * @param sortingCriteriaFieldName the name of the field to sort by.
     */
    private static void appendSortingColumn(final OrderTranslationVo vo, final StringBuilder sqlBuilder, final String sortingCriteriaFieldName)
    {
        if (CriteriaTranslator.isPropertySearchCriterion(sortingCriteriaFieldName))
        {
            final String propertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
            final String propertyNameLowerCase = propertyName.toLowerCase();
            final String valuesTableAlias = vo.getAliases().get(propertyNameLowerCase).get(vo.getTableMapper().getValuesTable()).getMainTableAlias();
            sqlBuilder.append(valuesTableAlias).append(PERIOD).append(VALUE_COLUMN);

            final String casting = vo.getDataTypeByPropertyName().get(propertyName);
            if (casting != null)
            {
                sqlBuilder.append("::").append(casting.toLowerCase());
            }
        } else if (isTypeSearchCriterion(sortingCriteriaFieldName))
        {
            final String typesTableAlias = vo.getAliases().get(EntityWithPropertiesSortOptions.TYPE).get(vo.getTableMapper().getEntityTypesTable()).
                    getSubTableAlias();
            sqlBuilder.append(typesTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else
        {
            final String lowerCaseSortingCriteriaFieldName = sortingCriteriaFieldName.toLowerCase();
            final String fieldName = Attributes.ATTRIBUTE_ID_TO_COLUMN_NAME.getOrDefault(lowerCaseSortingCriteriaFieldName,
                    lowerCaseSortingCriteriaFieldName);
            sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
        }
    }

    private static boolean isTypeSearchCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(EntityWithPropertiesSortOptions.TYPE);
    }

    public static SelectQuery translateToSearchTypeQuery(final OrderTranslationVo vo)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final String result = SELECT + SP + DISTINCT + SP + "o3" + PERIOD + CODE_COLUMN + SP + PROPERTY_CODE_ALIAS + COMMA + SP +
                "o4" + PERIOD + CODE_COLUMN + SP + TYPE_CODE_ALIAS + NL +
                FROM + SP + tableMapper.getEntitiesTable() + SP + CriteriaTranslator.MAIN_TABLE_ALIAS + NL +
                INNER_JOIN + SP + tableMapper.getValuesTable() + SP + "o1" + SP +
                ON + SP + CriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN + SP + EQ + SP + "o1" + PERIOD + tableMapper.getValuesTableEntityIdField() + NL +
                INNER_JOIN + SP + tableMapper.getEntityTypesAttributeTypesTable() + SP + "o2" + SP +
                ON + SP + "o1" + PERIOD + tableMapper.getValuesTableEntityTypeAttributeTypeIdField() + SP + EQ + SP + "o2" + PERIOD + ID_COLUMN + NL +
                INNER_JOIN + SP + tableMapper.getAttributeTypesTable() + SP + "o3" + SP +
                ON + SP + "o2" + PERIOD + tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField() + SP + EQ + SP + "o3" + PERIOD +
                ID_COLUMN + NL +
                INNER_JOIN + SP + DATA_TYPES_TABLE + SP + "o4" + SP +
                ON + SP + "o3" + PERIOD + tableMapper.getAttributeTypesTableDataTypeIdField() + SP + EQ + SP + "o4" + PERIOD + ID_COLUMN + NL +
                WHERE + SP + "o4" + PERIOD + CODE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;

        return new SelectQuery(result, Collections.singletonList(vo.getTypesToFilter()));
    }

    public static class OrderTranslationVo
    {

        private Long userId;

        private TableMapper tableMapper;

        private Set<Long> ids;

        private SortOptions<?> sortOptions;

        private Map<Object, Map<String, JoinInformation>> aliases = new HashMap<>();

        private List<Object> args = new ArrayList<>();

        private String[] typesToFilter;

        private Map<String, String> dataTypeByPropertyName;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(final Long userId)
        {
            this.userId = userId;
        }

        public TableMapper getTableMapper()
        {
            return tableMapper;
        }

        public void setTableMapper(final TableMapper tableMapper)
        {
            this.tableMapper = tableMapper;
        }

        public Set<Long> getIDs()
        {
            return ids;
        }

        public void setIDs(final Set<Long> filteredIDs)
        {
            this.ids = filteredIDs;
        }

        public SortOptions<?> getSortOptions()
        {
            return sortOptions;
        }

        public void setSortOptions(final SortOptions<?> sortOptions)
        {
            this.sortOptions = sortOptions;
        }

        public Map<Object, Map<String, JoinInformation>> getAliases()
        {
            return aliases;
        }

        public void setAliases(final Map<Object, Map<String, JoinInformation>> aliases)
        {
            this.aliases = aliases;
        }

        public List<Object> getArgs()
        {
            return args;
        }

        public String[] getTypesToFilter()
        {
            return typesToFilter;
        }

        public void setTypesToFilter(final String[] typesToFilter)
        {
            this.typesToFilter = typesToFilter;
        }

        public Map<String, String> getDataTypeByPropertyName()
        {
            return dataTypeByPropertyName;
        }

        public void setDataTypeByPropertyName(final Map<String, String> dataTypeByPropertyName)
        {
            this.dataTypeByPropertyName = dataTypeByPropertyName;
        }

    }
}
