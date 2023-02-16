/*
 * Copyright ETH 2023 - 2023 Zürich, Scientific IT Services
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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LEFT_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CONTROLLED_VOCABULARY_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.VOCABULARY_TERM_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TERM_TABLE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ControlledVocabularyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

public class ControlledVocabularyPropertySearchConditionTranslator
        implements IConditionTranslator<ControlledVocabularyPropertySearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(
            final ControlledVocabularyPropertySearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        if (criterion.getFieldType() == SearchFieldType.PROPERTY)
        {
            final Map<String, JoinInformation> joinInformationMap = new LinkedHashMap<>();
            final String valuesTableAlias = aliasFactory.createAlias();

            final JoinInformation joinInformation1 = new JoinInformation();
            joinInformation1.setJoinType(JoinType.LEFT);
            joinInformation1.setMainTable(tableMapper.getEntitiesTable());
            joinInformation1.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
            joinInformation1.setMainTableIdField(ID_COLUMN);
            joinInformation1.setSubTable(tableMapper.getValuesTable());
            joinInformation1.setSubTableAlias(valuesTableAlias);
            joinInformation1.setSubTableIdField(tableMapper.getValuesTableEntityIdField());
            joinInformationMap.put(tableMapper.getValuesTable(), joinInformation1);

            final String entityTypeAttributeTypeTableAlias = aliasFactory.createAlias();
            final JoinInformation joinInformation2 = new JoinInformation();
            joinInformation2.setJoinType(JoinType.LEFT);
            joinInformation2.setMainTable(tableMapper.getValuesTable());
            joinInformation2.setMainTableAlias(valuesTableAlias);
            joinInformation2.setMainTableIdField(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
            joinInformation2.setSubTable(tableMapper.getEntityTypesAttributeTypesTable());
            joinInformation2.setSubTableAlias(entityTypeAttributeTypeTableAlias);
            joinInformation2.setSubTableIdField(ID_COLUMN);
            joinInformationMap.put(tableMapper.getEntityTypesAttributeTypesTable(), joinInformation2);

            final JoinInformation joinInformation3 = new JoinInformation();
            joinInformation3.setJoinType(JoinType.LEFT);
            joinInformation3.setMainTable(tableMapper.getEntityTypesAttributeTypesTable());
            joinInformation3.setMainTableAlias(entityTypeAttributeTypeTableAlias);
            joinInformation3.setMainTableIdField(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
            joinInformation3.setSubTable(tableMapper.getAttributeTypesTable());
            joinInformation3.setSubTableAlias(aliasFactory.createAlias());
            joinInformation3.setSubTableIdField(ID_COLUMN);
            joinInformationMap.put(tableMapper.getAttributeTypesTable(), joinInformation3);

            return joinInformationMap;
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void translate(final ControlledVocabularyPropertySearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case PROPERTY:
            {
                doTranslate(criterion, tableMapper, args, sqlBuilder, aliases);
                break;
            }

            case ANY_FIELD:
            case ANY_PROPERTY:
            case ATTRIBUTE:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    static void doTranslate(final AbstractFieldSearchCriteria<String> criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases)
    {
        final String name = criterion.getFieldName();
        final String value = criterion.getFieldValue();
        final String valuesTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();

        TranslatorUtils.appendPropertiesExist(sqlBuilder, valuesTableAlias);
        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        sqlBuilder.append(aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias()).append(PERIOD)
                .append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(name);
        sqlBuilder.append(SP).append(AND).append(SP);

        appendControlledVocabularySubselectConstraint(args, sqlBuilder, value, valuesTableAlias);

        sqlBuilder.append(RP);
    }

    private static void appendControlledVocabularySubselectConstraint(final List<Object> args,
            final StringBuilder sqlBuilder, final String value, final String propertyTableAlias)
    {
        final String controlledVocabularyTableAlias = "cv";
        final String controlledVocabularyTermTableAlias = "cvt";

        sqlBuilder.append(propertyTableAlias).append(PERIOD).append(VOCABULARY_TERM_COLUMN)
                .append(SP).append(IN).append(SP);
        sqlBuilder.append(LP);

        sqlBuilder.append(SELECT).append(SP).append(controlledVocabularyTermTableAlias).append(PERIOD)
                .append(ID_COLUMN).append(SP)
                .append(FROM).append(SP).append(CONTROLLED_VOCABULARY_TERM_TABLE).append(SP)
                .append(controlledVocabularyTermTableAlias).append(SP)
                .append(LEFT_JOIN).append(SP).append(CONTROLLED_VOCABULARY_TABLE).append(SP)
                .append(controlledVocabularyTableAlias).append(SP).append(ON).append(SP)
                .append(controlledVocabularyTermTableAlias).append(PERIOD).append(CONTROLLED_VOCABULARY_COLUMN)
                .append(SP).append(EQ).append(SP).append(controlledVocabularyTableAlias).append(PERIOD)
                .append(ID_COLUMN);

        if (value != null)
        {
            sqlBuilder.append(SP).append(WHERE).append(SP).append(controlledVocabularyTermTableAlias).append(PERIOD)
                    .append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
            args.add(value);
        }

        sqlBuilder.append(RP);
    }

}
