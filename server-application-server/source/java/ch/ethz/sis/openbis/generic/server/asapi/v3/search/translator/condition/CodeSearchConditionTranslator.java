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

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.TAG;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class CodeSearchConditionTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final StringFieldSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                final boolean useWildcards = criterion.isUseWildcards();
                if (value != null && value.getValue() != null)
                {
                    final String stringValue = value.getValue();
                    translateSearchByCodeCondition(sqlBuilder, tableMapper, value.getClass(), stringValue, useWildcards,
                            args);
                } else
                {
                    sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN)
                            .append(SP).append(IS_NOT_NULL);
                }
                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateSearchByCodeCondition(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final Class<?> valueClass, final String stringValue, final boolean useWildcards,
            final List<Object> args)
    {
        if (tableMapper == SAMPLE)
        {
            buildCodeQueryForSamples(sqlBuilder, () -> TranslatorUtils.appendStringComparatorOp(
                    valueClass, stringValue, useWildcards, sqlBuilder, args));
        } else
        {
            final String column = (tableMapper == TAG) ? NAME_COLUMN : CODE_COLUMN;
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD)
                    .append(column).append(SP);
            TranslatorUtils.appendStringComparatorOp(valueClass, stringValue,
                    useWildcards, sqlBuilder, args);
        }
    }

    /**
     * Builds the following query part
     * <pre>
     * CASE
     *     WHEN t0.samp_id_part_of IS NULL THEN code {comparisonBuilder.run()}
     *     ELSE substr(t0.sample_identifier, length(t0.sample_identifier)
     *             - strpos(reverse(t0.sample_identifier), '/') + 2) {comparisonBuilder.run()}
     * END
     * </pre>
     * {@code comparisonBuilder.run()} is executed to add string comparisons depending on the use case.
     *
     * @param sqlBuilder query builder.
     * @param comparisonBuilder runnable which adds comparison operators to the query builder.
     */
    public static void buildCodeQueryForSamples(final StringBuilder sqlBuilder, final Runnable comparisonBuilder)
    {
        sqlBuilder.append(CASE).append(NL)
                .append(SP).append(SP).append(WHEN).append(SP)
                .append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD)
                .append(PART_OF_SAMPLE_COLUMN).append(SP).append(IS_NULL).append(SP)
                .append(THEN).append(SP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                .append(PERIOD).append(CODE_COLUMN);
        comparisonBuilder.run();
        sqlBuilder.append(NL).append(SP).append(SP).append(ELSE).append(SP)
                .append(SUBSTR).append(LP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN).append(COMMA).append(SP)
                .append(LENGTH).append(LP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN).append(RP).append(SP)
                .append(MINUS).append(SP)
                .append(STRPOS).append(LP)
                .append(REVERSE).append(LP)
                .append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN)
                .append(RP).append(COMMA).append(SP).append(SQ).append('/')
                .append(SQ)
                .append(RP).append(SP).append(PLUS).append(SP).append(2)
                .append(RP);
        comparisonBuilder.run();
        sqlBuilder.append(NL).append(END);
    }

}
