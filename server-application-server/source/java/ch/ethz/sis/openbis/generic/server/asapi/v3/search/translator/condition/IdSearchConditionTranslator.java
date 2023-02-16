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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.FullSampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class IdSearchConditionTranslator implements IConditionTranslator<IdSearchCriteria<?>>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final IdSearchCriteria<?> criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory) {
        if (criterion.getId().getClass() == MaterialPermId.class)
        {
            return TranslatorUtils.getTypeJoinInformationMap(tableMapper, aliasFactory);
        } else
        {
            return null;
        }
    }

    @Override
    public void translate(final IdSearchCriteria<?> criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        final Object entityId = criterion.getId();

        if (entityId instanceof ObjectIdentifier || entityId.getClass() == TagPermId.class)
        {
            final FullSampleIdentifier fullObjectIdentifier = new FullSampleIdentifier(
                    (entityId.getClass() == TagPermId.class)
                            ? ((TagPermId) entityId).getPermId()
                            : ((ObjectIdentifier) entityId).getIdentifier(), null);
            final String objectCode = fullObjectIdentifier.getSampleCode();
            final SampleIdentifierParts identifierParts = fullObjectIdentifier.getParts();

            final String originalSpaceCode = identifierParts.getSpaceCodeOrNull();
            final String spaceCode = (originalSpaceCode == null || entityId.getClass() != TagPermId.class)
                    ? originalSpaceCode : originalSpaceCode.toLowerCase();
            final String projectCode = identifierParts.getProjectCodeOrNull();
            final String containerCode = identifierParts.getContainerCodeOrNull();

            String codeColumnName = CODE_COLUMN;
            if (spaceCode != null || projectCode != null || containerCode != null)
            {
                sqlBuilder.append(LP);

                if (spaceCode != null)
                {
                    if (entityId.getClass() == SampleIdentifier.class)
                    {
                        buildSelectByIdConditionWithSubquery(sqlBuilder, SPACE_COLUMN, SPACES_TABLE, CODE_COLUMN);
                    } else if (entityId.getClass() == ExperimentIdentifier.class)
                    {
                        buildSelectByIdConditionWithSubqueryExperiments(sqlBuilder);
                    } else if (entityId.getClass() == ProjectIdentifier.class)
                    {
                        buildSelectByIdConditionWithSubqueryProjects(sqlBuilder);
                    } else if (entityId.getClass() == TagPermId.class)
                    {
                        buildSelectByIdConditionWithSubquery(sqlBuilder, OWNER_COLUMN, PERSONS_TABLE, USER_COLUMN);
                        codeColumnName = NAME_COLUMN;
                    } else
                    {
                        throw new RuntimeException("Unsupported identifier: " + entityId.getClass());
                    }
                    sqlBuilder.append(SP).append(AND).append(SP);

                    args.add(spaceCode);
                }

                if (projectCode != null)
                {
                    buildSelectByIdConditionWithSubquery(sqlBuilder, PROJECT_COLUMN, IN, PROJECTS_TABLE, CODE_COLUMN);
                    sqlBuilder.append(SP).append(AND).append(SP);

                    args.add(projectCode);
                }

                if (containerCode != null)
                {
                    buildSelectByIdConditionWithSubquery(sqlBuilder, PART_OF_SAMPLE_COLUMN, TableMapper.SAMPLE.getEntitiesTable(), CODE_COLUMN);
                    sqlBuilder.append(SP).append(AND).append(SP);

                    args.add(containerCode);
                }

                sqlBuilder.setLength(sqlBuilder.length() - AND.length() - SP.length() * 2);
                sqlBuilder.append(RP).append(SP).append(AND).append(SP);
            }

            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(codeColumnName).append(SP).append(EQ).append(SP).append(QU);
            args.add(objectCode);
        } else if (entityId.getClass() == SamplePermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(EQ).append(QU);
            args.add(((SamplePermId) entityId).getPermId());
        } else if (entityId.getClass() == ExperimentPermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(EQ).append(QU);
            args.add(((ExperimentPermId) entityId).getPermId());
        } else if (entityId.getClass() == DataSetPermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            args.add(((DataSetPermId) entityId).getPermId());
        } else if (entityId.getClass() == ProjectPermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(EQ).append(QU);
            args.add(((ProjectPermId) entityId).getPermId());
        } else if (entityId.getClass() == SpacePermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            args.add(((SpacePermId) entityId).getPermId());
        } else if (entityId.getClass() == EntityTypePermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            args.add(((EntityTypePermId) entityId).getPermId());
        } else if (entityId.getClass() == TagCode.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(NAME_COLUMN).append(EQ).append(QU);
            args.add(((TagCode) entityId).getCode());
        } else if (entityId.getClass() == SemanticAnnotationPermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(EQ).append(QU);
            args.add(((SemanticAnnotationPermId) entityId).getPermId());
        } else if (entityId.getClass() == MaterialPermId.class)
        {
            final String materialTypeTableAlias = aliases.get(tableMapper.getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU)
                    .append(SP).append(AND).append(SP).append(materialTypeTableAlias).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            final MaterialPermId materialPermId = (MaterialPermId) entityId;
            args.add(materialPermId.getCode());
            args.add(materialPermId.getTypeCode());
        } else if (entityId.getClass() == PropertyTypePermId.class)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            args.add(((PropertyTypePermId) entityId).getPermId());
        } else
        {
            throw new IllegalArgumentException("Unknown id: class " + entityId.getClass().getSimpleName());
        }
    }

    private static void buildSelectByIdConditionWithSubqueryProjects(final StringBuilder sqlBuilder)
    {
        sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).
                append(FROM).append(SP).append(SPACES_TABLE).append(SP).
                append(WHERE).append(SP).append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU).
                append(RP);
    }

    private static void buildSelectByIdConditionWithSubqueryExperiments(final StringBuilder sqlBuilder)
    {
        final String p = "p";
        final String s = "s";
        sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(p).append(PERIOD).append(ID_COLUMN).append(SP).
                append(FROM).append(SP).append(PROJECTS_TABLE).append(SP).append(p).append(SP).
                append(LEFT_JOIN).append(SP).append(SPACES_TABLE).append(SP).append(s).append(SP).
                append(ON).append(SP).append(s).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).
                append(SP).append(p).append(PERIOD).append(SPACE_COLUMN).append(SP).
                append(WHERE).append(SP).append(s).append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU).
                append(RP);
    }

    /**
     * Builds the following query:<p/>
     * <code>
     *     t0.[columnName] = (SELECT id FROM [subqueryTable] WHERE [subqueryTableColumn] = ?)
     * </code>
     * @param sqlBuilder SQL builder to add the query part to.
     * @param columnName name of the column in the main table to be equal to the result in the subquery.
     * @param subqueryTable table which should be queried for code.
     * @param subqueryTableColumn name of the column in the subtable to search by.
     */
    private static void buildSelectByIdConditionWithSubquery(final StringBuilder sqlBuilder, final String columnName, final String subqueryTable,
            final String subqueryTableColumn)
    {
        buildSelectByIdConditionWithSubquery(sqlBuilder, columnName, EQ, subqueryTable, subqueryTableColumn);
    }

    private static void buildSelectByIdConditionWithSubquery(final StringBuilder sqlBuilder, final String columnName, String equalOrIn, final String subqueryTable,
            final String subqueryTableColumn)
    {
        sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(equalOrIn).append(SP).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).append(subqueryTable).append(SP).
                append(WHERE).append(SP).append(subqueryTableColumn).append(SP).append(EQ).append(SP).append(QU).
                append(RP);
    }

}
