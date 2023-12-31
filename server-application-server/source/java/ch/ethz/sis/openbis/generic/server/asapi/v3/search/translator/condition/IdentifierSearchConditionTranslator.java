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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.appendStringComparatorOp;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_IDENTIFIER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

public class IdentifierSearchConditionTranslator implements IConditionTranslator<IdentifierSearchCriteria>
{

    private static final String UNIQUE_PREFIX = IdentifierSearchConditionTranslator.class.getName() + ":";

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final IdentifierSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        TranslatorUtils.appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, UNIQUE_PREFIX);
        return result;
    }

    @Override
    public void translate(final IdentifierSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyCode)
    {
        doTranslate(criterion, criterion.isUseWildcards(), tableMapper, args, sqlBuilder, aliases, UNIQUE_PREFIX);
    }

    static void doTranslate(final AbstractFieldSearchCriteria<AbstractStringValue> criterion,
            final boolean useWildcards, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final String prefix)
    {
        final AbstractStringValue fieldValue = criterion.getFieldValue();
        final String entitiesTable = tableMapper.getEntitiesTable();
        final String samplesTableName = TableMapper.SAMPLE.getEntitiesTable();
        final String projectsTableName = TableMapper.PROJECT.getEntitiesTable();
        final String experimentsTableName = TableMapper.EXPERIMENT.getEntitiesTable();
        final boolean hasSpaces = entitiesTable.equals(samplesTableName) || entitiesTable.equals(experimentsTableName) ||
                entitiesTable.equals(projectsTableName);
        final boolean hasProjects = entitiesTable.equals(samplesTableName) || entitiesTable.equals(experimentsTableName);
        final String spacesTableAlias = hasSpaces
                ? aliases.get(prefix + SPACES_TABLE).getSubTableAlias() : null;
        final String projectsTableAlias = hasProjects ? aliases.get(prefix + PROJECTS_TABLE).getSubTableAlias() : null;
        final String samplesTableAlias = entitiesTable.equals(samplesTableName)
                ? aliases.get(prefix + entitiesTable).getSubTableAlias() : null;

        if (tableMapper != TableMapper.SAMPLE)
        {
            buildFullIdentifierConcatenationString(sqlBuilder, spacesTableAlias, projectsTableAlias, samplesTableAlias);
        } else
        {
            sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN);
        }
        appendStringComparatorOp(fieldValue.getClass(), fieldValue.getValue(), useWildcards, sqlBuilder, args);
    }

}
