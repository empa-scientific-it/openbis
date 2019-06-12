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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.NumberFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DISTINCT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INNER_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NEW_LINE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;

public class Translator
{

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            SimplePropertyValidator.SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());

    public static final Map<Class<? extends ISearchCriteria>, IConditionTranslator<? extends ISearchCriteria>> CRITERIA_TO_CONDITION_TRANSLATOR_MAP =
            new HashMap<>();

    static
    {
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodeSearchCriteria.class, new StringFieldSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberPropertySearchCriteria.class, new NumberFieldSearchCriteriaTranslator());
    }

    private static final AtomicBoolean FIRST = new AtomicBoolean();

    public static SelectQuery translate(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        if (criteria == null && criteria.isEmpty())
        {
            throw new IllegalArgumentException("Empty or null criteria provided.");
        }

        final EntityMapper dbEntityKind = EntityMapper.toEntityMapper(entityKind);

        final Map<Object, JoinInformation> aliases = new HashMap<>();
        final List<Object> args = new ArrayList<>();

        final String where = buildWhere(dbEntityKind, criteria, args, operator);
        final String from = buildFrom(dbEntityKind, criteria, aliases);
        final String select = buildSelect(dbEntityKind);

        return new SelectQuery(select + from + where, args);
    }

    private static String buildSelect(final EntityMapper dbEntityKind)
    {
        return SELECT + SP + DISTINCT + SP + dbEntityKind.getEntitiesTableIdField() + NEW_LINE;
    }

    public static String getAlias(final int num) {
        return "t" + num;
    }

    private static String buildFrom(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, Map<Object, JoinInformation> aliases)
    {
        final StringBuilder sqlBuilder = new StringBuilder();

        final String entitiesTableName = entityMapper.getEntitiesTable();
        JoinInformation mainTable = new JoinInformation();
        mainTable.setMainTable(entitiesTableName);
        mainTable.setMainTableId(entityMapper.getEntitiesTableIdField());
        mainTable.setMainTableAlias(getAlias(aliases.size()));
        aliases.put(entitiesTableName, mainTable);

        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(SP).append(AS).append(
                SP).append(mainTable.getMainTableAlias()).append(NEW_LINE);

        for (final ISearchCriteria criterion : criteria)
        {
            final IConditionTranslator conditionTranslator = CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());

            if (conditionTranslator != null)
            {
                final JoinInformation joinInformation = conditionTranslator.getJoinInformation(criterion, entityMapper);

                if (joinInformation != null)
                {
                    if (joinInformation.getSubTable() != null)
                    { // Join required
                        joinInformation.setSubTableAlias(getAlias(aliases.size()));
                        sqlBuilder.append(INNER_JOIN).append(SP).append(joinInformation.getSubTable()).append(SP).append(AS)
                                .append(joinInformation.getSubTableAlias()).append(SP)
                                .append(ON).append(SP).append(mainTable.getMainTableAlias()).append(PERIOD).append(joinInformation.getMainTableId())
                                .append(SP)
                                .append(EQ).append(SP).append(joinInformation.getSubTableAlias()).append(PERIOD)
                                .append(joinInformation.getSubTableId()).append(RP).append(NEW_LINE);
                    }
                    aliases.put(criterion, joinInformation);
                }
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
            }
        }
        return sqlBuilder.toString();
    }


    private static String buildWhere(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, final List<Object> args, final SearchOperator operator)
    {
        final StringBuilder sqlBuilder = new StringBuilder();
        if (isSearchAllCriteria(criteria))
        {
            return "";
        }

        sqlBuilder.append(WHERE).append(SP);

        FIRST.set(true);
        final String logicalOperator = operator.toString();

        criteria.forEach((criterion) ->
        {
            if (FIRST.get())
            {
                FIRST.set(false);
            } else
            {
                sqlBuilder.append(SP).append(logicalOperator).append(SP);
            }

            @SuppressWarnings("unchecked")
            final IConditionTranslator<ISearchCriteria> conditionTranslator =
                    (IConditionTranslator<ISearchCriteria>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
            if (conditionTranslator != null)
            {
                conditionTranslator.translate(criterion, entityMapper, args, sqlBuilder);
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
            }
        });

        return sqlBuilder.toString();
    }

    /**
     * Checks whether the criteria is for searching all values.
     *
     * @param criteria the criteria to be checked.
     * @return {@code true} if the criteria contain only one entity search value which is empty.
     */
    private static boolean isSearchAllCriteria(final List<ISearchCriteria> criteria)
    {
        if (criteria.size() == 1)
        {
            final ISearchCriteria criterion = criteria.get(0);
            if (criterion instanceof AbstractEntitySearchCriteria<?> &&
                    ((AbstractEntitySearchCriteria<?>) criterion).getCriteria().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public static class TranslatorAlias
    {

        private String table;

        private String tableAlias; // table + "_" + <alias_idx>

        private ISearchCriteria reasonForAlias;

    }

}
