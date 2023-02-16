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
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SEMANTIC_ANNOTATIONS_TABLE;

public class PropertyAssignmentSearchDAO implements IPropertyAssignmentSearchDAO
{

    private ISQLExecutor sqlExecutor;

    public PropertyAssignmentSearchDAO(ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    @Override
    public Set<Long> findAssignmentsWithoutAnnotations(final Set<Long> semanticAnnotationsPropertyIds,
            final String idsColumnName)
    {
        final String sql = SELECT + SP + idsColumnName + NL
                + FROM + SP + SAMPLE_TYPE_PROPERTY_TYPE_TABLE + NL
                + WHERE + SP + ID_COLUMN + SP + NOT + SP + IN + SP + LP + NL
                + SP + SELECT + SP + SAMPLE_TYPE_PROPERTY_TYPE_COLUMN + NL
                + SP + FROM + SP + SEMANTIC_ANNOTATIONS_TABLE + NL
                + SP + WHERE + SP + SAMPLE_TYPE_PROPERTY_TYPE_COLUMN + SP + IS_NOT_NULL + NL
                + RP + SP + AND + SP + PROPERTY_TYPE_COLUMN + SP + IN + SP + LP
                + SELECT + SP + UNNEST + LP + QU + RP + RP + NL
                + AND + SP + PROPERTY_TYPE_COLUMN + SP + IS_NOT_NULL;

        final List<Object> args = new ArrayList<>(1);
        args.add(semanticAnnotationsPropertyIds.toArray(new Long[0]));

        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(idsColumnName))
                .collect(Collectors.toSet());
    }

}
