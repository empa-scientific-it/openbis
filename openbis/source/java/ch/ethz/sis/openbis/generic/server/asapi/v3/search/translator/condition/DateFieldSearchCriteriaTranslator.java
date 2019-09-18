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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IDate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.CASE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DOUBLE_COLON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ELSE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.END;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.THEN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TIMESTAMPTZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHEN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;

public class DateFieldSearchCriteriaTranslator implements IConditionTranslator<DateFieldSearchCriteria>
{

    private static final String TIMESTAMP_DATA_TYPE_CODE = "TIMESTAMP";

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DateFieldSearchCriteria criterion, final EntityMapper entityMapper,
            final IAliasFactory aliasFactory)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                return null;
            }

            case PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(entityMapper, aliasFactory);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final DateFieldSearchCriteria criterion, final EntityMapper entityMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases)
    {
        switch (criterion.getFieldType()) {
            case ATTRIBUTE:
            {
                final Object fieldName = criterion.getFieldName();
                final IDate fieldValue = criterion.getFieldValue();

                if (criterion instanceof RegistrationDateSearchCriteria)
                {
                    sqlBuilder.append(REGISTRATION_TIMESTAMP_COLUMN);
                } else if (criterion instanceof ModificationDateSearchCriteria)
                {
                    sqlBuilder.append(MODIFICATION_TIMESTAMP_COLUMN);
                } else
                {
                    sqlBuilder.append(fieldName);
                }

                TranslatorUtils.appendDateComparatorOp(fieldValue, sqlBuilder);
                TranslatorUtils.addDateValueToArgs(fieldValue, args);
                break;
            }

            case PROPERTY:
            {
                final IDate value = criterion.getFieldValue();
                final String propertyName = criterion.getFieldName();
                final Map<String, JoinInformation> joinInformationMap = aliases.get(criterion);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

                sqlBuilder.append(joinInformationMap.get(entityMapper.getEntityTypesAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(propertyName);

                sqlBuilder.append(SP).append(AND);

                sqlBuilder.append(SP).append(joinInformationMap.get(entityMapper.getAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(TIMESTAMP_DATA_TYPE_CODE);

                sqlBuilder.append(SP).append(THEN).append(SP);
                sqlBuilder.append(joinInformationMap.get(entityMapper.getEntitiesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON).append(TIMESTAMPTZ).append(SP);
                TranslatorUtils.appendDateComparatorOp(value, sqlBuilder);
                TranslatorUtils.addDateValueToArgs(value, args);

                sqlBuilder.append(SP).append(ELSE).append(SP).append(false).append(SP).append(END);

                break;
            }
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

}
