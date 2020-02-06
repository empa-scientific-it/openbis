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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.CriteriaMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.METAPROJECT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECT_ASSIGNMENTS_ALL_TABLE;

public class CriteriaTranslator
{

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);

    public static final DateFormat DATE_WITHOUT_TIME_FORMAT = new SimpleDateFormat(BasicConstant.DATE_WITHOUT_TIME_FORMAT_PATTERN);

    public static final DateFormat DATE_WITH_SHORT_TIME_FORMAT = new SimpleDateFormat(BasicConstant.DATE_WITH_SHORT_TIME_PATTERN);

    public static final String MAIN_TABLE_ALIAS = getAlias(new AtomicInteger(0));

    public static SelectQuery translate(final TranslationVo vo)
    {
        if (vo.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final String from = buildFrom(vo);
        final String where = buildWhere(vo);
        final String select = buildSelect(vo);

        return new SelectQuery(select  + NL + from + NL + where, vo.getArgs());
    }

    private static String buildSelect(final TranslationVo vo)
    {
        return SELECT + SP + DISTINCT + SP + MAIN_TABLE_ALIAS + PERIOD + vo.getIdColumnName();
    }

    private static String getAlias(final AtomicInteger num)
    {
        return "t" + num.getAndIncrement();
    }

    private static String buildFrom(final TranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder();

        final String entitiesTableName = vo.getTableMapper().getEntitiesTable();
        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(SP).append(MAIN_TABLE_ALIAS);

        final AtomicInteger indexCounter = new AtomicInteger(1);
        vo.getCriteria().forEach(criterion ->
        {
            if (!CriteriaMapper.getCriteriaToManagerMap().containsKey(criterion.getClass()))
            {
                final IConditionTranslator conditionTranslator = CriteriaMapper.getCriteriaToConditionTranslatorMap().get(criterion.getClass());
                if (conditionTranslator != null)
                {
                    @SuppressWarnings("unchecked")
                    final Map<String, JoinInformation> joinInformationMap = conditionTranslator.getJoinInformationMap(criterion,
                            vo.getTableMapper(), () -> getAlias(indexCounter));

                    if (joinInformationMap != null)
                    {
                        joinInformationMap.values().forEach((joinInformation) ->
                                TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
                        vo.getAliases().put(criterion, joinInformationMap);
                    }
                } else
                {
                    throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                }
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildWhere(final TranslationVo vo)
    {
        final Collection<ISearchCriteria> criteria = vo.getCriteria();
        if (isSearchAllCriteria(criteria))
        {
            return WHERE + SP + TRUE;
        } else
        {
            final String logicalOperator = vo.getOperator().toString();
            final String separator = SP + logicalOperator + SP;

            final StringBuilder resultSqlBuilder = criteria.stream().collect(
                    StringBuilder::new,
                    (sqlBuilder, criterion) ->
                    {
                        sqlBuilder.append(separator);
                        appendCriterionCondition(vo, sqlBuilder, criterion, vo.getParentCriterion());
                    },
                    StringBuilder::append
            );

            return WHERE + SP + resultSqlBuilder.substring(separator.length());
        }
    }

    /**
     * Appends condition translated from a criterion.
     *
     * @param vo value object with miscellaneous information.
     * @param sqlBuilder string builder to append the condition to.
     * @param criterion criterion to be translated.
     * @param parentCriterion parent of {@code criterion}.
     */
    private static void appendCriterionCondition(final TranslationVo vo, final StringBuilder sqlBuilder, final ISearchCriteria criterion,
            final AbstractCompositeSearchCriteria parentCriterion)
    {
        final ISearchManager<ISearchCriteria, ?, ?> subqueryManager = CriteriaMapper.getCriteriaToManagerMap().get(criterion.getClass());
        final TableMapper tableMapper = vo.getTableMapper();
        if (subqueryManager != null)
        {
            final String column = CriteriaMapper.getCriteriaToInColumnMap().get(criterion.getClass());
            if (tableMapper != null && column != null)
            {
                final Set<Long> ids = subqueryManager.searchForIDs(vo.getUserId(), criterion, null, parentCriterion,
                        CriteriaMapper.getParentChildCriteriaToChildSelectIdMap().getOrDefault(
                                parentCriterion.getClass().toString() + criterion.getClass().toString(), ID_COLUMN));
                appendInStatement(sqlBuilder, criterion, column, tableMapper);
                vo.getArgs().add(ids.toArray(new Long[0]));
            } else
            {
                throw new NullPointerException("tableMapper = " + tableMapper + ", column = " + column + ", criterion.getClass() = " +
                        criterion.getClass());
            }
        } else
        {
            @SuppressWarnings("unchecked")
            final IConditionTranslator<ISearchCriteria> conditionTranslator =
                    (IConditionTranslator<ISearchCriteria>) CriteriaMapper.getCriteriaToConditionTranslatorMap().get(criterion.getClass());
            if (conditionTranslator != null)
            {
                conditionTranslator.translate(criterion, tableMapper, vo.getArgs(), sqlBuilder, vo.getAliases().get(criterion),
                        vo.getDataTypeByPropertyName());
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
            }
        }
    }

    private static void appendInStatement(final StringBuilder sqlBuilder, final ISearchCriteria criterion, final String column,
            final TableMapper tableMapper)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(column).append(SP).append(IN).append(SP).append(LP);
        if (!(criterion instanceof TagSearchCriteria))
        {
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
        } else
        {
            final String e = "e";
            final String mpa = "mpa";
            final String mp = "mp";
            sqlBuilder.append(SELECT).append(SP).append(e).append(PERIOD).append(column).append(NL).
                    append(FROM).append(SP).append(tableMapper.getEntitiesTable()).append(SP).append(e).append(NL).
                    append(INNER_JOIN).append(SP).append(METAPROJECT_ASSIGNMENTS_ALL_TABLE).append(SP).append(mpa).append(SP).
                    append(ON).append(SP).append(e).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP).append(mpa).append(PERIOD).
                    append(tableMapper.getMetaprojectAssignmentsEntityIdField()).append(NL).
                    append(INNER_JOIN).append(SP).append(METAPROJECTS_TABLE).append(SP).append(mp).append(SP).
                    append(ON).append(SP).append(mpa).append(PERIOD).append(METAPROJECT_ID_COLUMN).append(SP).append(EQ).append(SP).append(mp).
                    append(PERIOD).append(ID_COLUMN).append(NL).
                    append(WHERE).append(SP).append(mp).append(PERIOD).append(ID_COLUMN).append(SP).append(IN).append(SP).append(LP);
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
            sqlBuilder.append(RP);
        }
        sqlBuilder.append(RP);
    }

    /**
     * Checks whether the criteria is for searching all values.
     *
     * @param criteria the criteria to be checked.
     * @return {@code true} if the criteria contain only one entity search value which is empty.
     */
    private static boolean isSearchAllCriteria(final Collection<ISearchCriteria> criteria)
    {
        switch (criteria.size())
        {
            case 0:
            {
                return true;
            }

            case 1:
            {
                final ISearchCriteria criterion = criteria.iterator().next();
                return criterion instanceof AbstractEntitySearchCriteria<?> &&
                        ((AbstractEntitySearchCriteria<?>) criterion).getCriteria().isEmpty();
            }

            default:
            {
                return false;
            }
        }
    }

}
