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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.DATE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.TIMESTAMP_WITH_TZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;

public class DateFieldSearchConditionTranslator implements IConditionTranslator<DateFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DateFieldSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                return null;
            }

            case ANY_PROPERTY:
            case PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final DateFieldSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final IDate value = criterion.getFieldValue();
                final ITimeZone timeZone = criterion.getTimeZone();
                final boolean bareDateValue = value instanceof AbstractDateValue &&
                        TranslatorUtils.isDateWithoutTime(((AbstractDateValue) value).getValue());
                final String fieldName = criterion.getFieldName();

                if (bareDateValue)
                {
                    sqlBuilder.append(LP);
                }

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD);
                if (criterion instanceof RegistrationDateSearchCriteria)
                {
                    sqlBuilder.append(REGISTRATION_TIMESTAMP_COLUMN);
                } else if (criterion instanceof ModificationDateSearchCriteria)
                {
                    sqlBuilder.append(MODIFICATION_TIMESTAMP_COLUMN);
                } else
                {
                    final String columnName = AttributesMapper.getColumnName(fieldName,
                            tableMapper.getEntitiesTable(), fieldName);
                    sqlBuilder.append(columnName);
                }
                sqlBuilder.append(SP);
                TranslatorUtils.appendTimeZoneConversion(value, sqlBuilder, timeZone);

                if (bareDateValue)
                {
                    sqlBuilder.append(RP).append(DOUBLE_COLON).append(DATE);
                }

                sqlBuilder.append(SP);

                TranslatorUtils.appendDateComparatorOp(value, sqlBuilder, args, bareDateValue);
                break;
            }

            case PROPERTY:
            {
                translateDateProperty(criterion, tableMapper, args, sqlBuilder, aliases, dataTypeByPropertyCode, false);
                break;
            }

            case ANY_PROPERTY:
            {
                translateDateProperty(criterion, tableMapper, args, sqlBuilder, aliases, dataTypeByPropertyCode, true);
                break;
            }

            case ANY_FIELD:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    private static void translateDateProperty(final DateFieldSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode, boolean isAnyProperty)
    {
        final String fullPropertyName = isAnyProperty ? null : criterion.getFieldName();

        final IDate value = criterion.getFieldValue();
        final ITimeZone timeZone = criterion.getTimeZone();
        final boolean bareDateValue = value instanceof AbstractDateValue &&
                TranslatorUtils.isDateWithoutTime(((AbstractDateValue) value).getValue());

        final String propertyTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();
        TranslatorUtils.appendPropertiesExist(sqlBuilder, propertyTableAlias);
        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        final String casting = dataTypeByPropertyCode.get(fullPropertyName);

        if (fullPropertyName == null)
        {
            // Search by any date/timestamp field

            if (value != null)
            {
                if (value instanceof AbstractDateObjectValue)
                {
                    sqlBuilder.append(LP);
                    appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases, timeZone,
                            null, true, DataType.DATE.toString());
                    sqlBuilder.append(SP).append(OR).append(SP);
                    appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases, timeZone,
                            null, false, DataType.TIMESTAMP.toString());
                    sqlBuilder.append(RP);
                } else
                {
                    if (bareDateValue)
                    {
                        appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases,
                                timeZone, null, true,
                                DataType.DATE.toString(), DataType.TIMESTAMP.toString());
                    } else
                    {
                        appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases,
                                timeZone, null, false, DataType.TIMESTAMP.toString());
                    }
                }
            } else
            {
                appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases, timeZone,
                        null, false, DataType.TIMESTAMP.toString(), DataType.DATE.toString());
            }
        } else
        {
            // Search by specific date/timestamp field

            if (DataType.DATE.toString().equals(casting))
            {
                if (timeZone instanceof TimeZone)
                {
                    throw new UserFailureException(String.format(
                            "Search criteria with time zone doesn't make sense for property %s of data type %s.",
                            fullPropertyName, DataType.DATE));
                }
                appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases, null,
                        fullPropertyName, true, DataType.DATE.toString());
            } else if (DataType.TIMESTAMP.toString().equals(casting))
            {
                appendConditionForDateOrTimestampProperties(sqlBuilder, args, tableMapper, value, aliases, timeZone,
                        fullPropertyName, bareDateValue,
                        DataType.TIMESTAMP.toString());
            } else
            {
                throw new UserFailureException(String.format("Property %s is neither of data type %s nor %s.",
                        fullPropertyName, DataType.DATE, DataType.TIMESTAMP));
            }
        }
        sqlBuilder.append(RP);
    }

    static void appendConditionForDateOrTimestampProperties(final StringBuilder sqlBuilder, final List<Object> args,
            final TableMapper tableMapper, final IDate value, final Map<String, JoinInformation> aliases,
            final ITimeZone timeZone, final String fullPropertyName, final boolean castToDate,
            final String... dataTypeStrings)
    {
        if (fullPropertyName != null)
        {
            TranslatorUtils.appendEntityTypePropertyTypeSubselectConstraint(tableMapper, args, sqlBuilder,
                    fullPropertyName, aliases.get(tableMapper.getValuesTable()).getSubTableAlias());

            sqlBuilder.append(SP).append(AND);
        }

        sqlBuilder.append(SP).append(LP);
        TranslatorUtils.appendDataTypesSubselect(tableMapper, sqlBuilder,
                aliases.get(tableMapper.getValuesTable()).getSubTableAlias());
        sqlBuilder.append(RP).append(SP).append(IN).append(SP).append(LP)
                .append(SQ).append(String.join(SQ + COMMA + SP + SQ, dataTypeStrings)).append(SQ).append(RP);

        sqlBuilder.append(SP).append(AND).append(SP);

        if (castToDate)
        {
            sqlBuilder.append(LP);
        }

        if (value != null)
        {
            sqlBuilder.append(SAFE_TIMESTAMP).append(LP)
                    .append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                    .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(RP).append(SP);
            TranslatorUtils.appendTimeZoneConversion(value, sqlBuilder, timeZone);

            if (castToDate)
            {
                sqlBuilder.append(RP).append(DOUBLE_COLON).append(DATE).append(SP);
            }

            TranslatorUtils.appendDateComparatorOp(value, sqlBuilder, args, castToDate);
        } else
        {
            sqlBuilder.append(TRUE);
        }
    }

}
