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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class CollectionFieldSearchConditionTranslator implements IConditionTranslator<CollectionFieldSearchCriteria<?>>
{

    private static final Map<Class<?>, Object[]> ARRAY_CASTING = new HashMap<>();

    static
    {
        ARRAY_CASTING.put(CodesSearchCriteria.class, new String[0]);
        ARRAY_CASTING.put(UserIdsSearchCriteria.class, new String[0]);
        ARRAY_CASTING.put(IdsSearchCriteria.class, new String[0]);
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final CollectionFieldSearchCriteria<?> criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final CollectionFieldSearchCriteria<?> criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyCode)
    {
        if (!ARRAY_CASTING.containsKey(criterion.getClass()))
        {
            throw new RuntimeException("Unsupported " + CollectionFieldSearchCriteria.class.getSimpleName() + ", this is a core error, contact the development team.");
        }
        if (criterion.getClass() == IdsSearchCriteria.class)
        {
            if (criterion.getFieldValue() != null && criterion.getFieldValue().size() > 1)
            {
                final Class<?> identifierClass = criterion.getFieldValue().iterator().next().getClass();
                for (Object identifier:criterion.getFieldValue())
                {
                    if (identifier.getClass() != identifierClass)
                    {
                        throw new IllegalArgumentException("Unsupported " + CollectionFieldSearchCriteria.class.getSimpleName() + ": " + IdsSearchCriteria.class + ", identifiers provided should be of the same type.");
                    }
                }
            }
        }

        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final Object fieldName = AttributesMapper.getColumnName(criterion.getFieldName(),
                        tableMapper.getEntitiesTable(), criterion.getFieldName());
                final Collection<?> initialFieldValue = criterion.getFieldValue();

                if (initialFieldValue != null)
                {
                    final Collection<?> fieldValue;
                    if (!initialFieldValue.isEmpty() && initialFieldValue.stream().anyMatch(
                            (o) -> o instanceof ObjectPermId))
                    {
                        fieldValue = initialFieldValue.stream().map(
                                (o) -> ((ObjectPermId) o).getPermId()).collect(Collectors.toList());
                    } else
                    {
                        fieldValue = initialFieldValue;
                    }

                    if (tableMapper == TableMapper.SAMPLE)
                    {
                        CodeSearchConditionTranslator.buildCodeQueryForSamples(sqlBuilder, () -> {
                            sqlBuilder.append(SP).append(IN).append(SP).append(SELECT_UNNEST);
                            args.add(fieldValue.toArray(ARRAY_CASTING.get(criterion.getClass())));
                        });
                    } else
                    {
                        sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName).
                                append(SP).append(IN).append(SP).append(LP).
                                append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                                append(RP);
                        args.add(fieldValue.toArray(ARRAY_CASTING.get(criterion.getClass())));
                    }
                } else
                {
                    sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName).
                            append(SP).append(IS_NOT_NULL);
                }
                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

}
