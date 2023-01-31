/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import net.lemnik.eodsql.DataSet;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractEntityWithPermIdDeliverer extends AbstractEntityDeliverer<String>
{
    private final String databasePermIdColumn;

    private final String sql;

    AbstractEntityWithPermIdDeliverer(DeliveryContext context, String entityKind, String databaseTable)
    {
        this(context, entityKind, databaseTable, "perm_id");
    }

    AbstractEntityWithPermIdDeliverer(DeliveryContext context, String entityKind, String databaseTable, String databasePermIdColumn)
    {
        super(context, entityKind);
        this.databasePermIdColumn = databasePermIdColumn;
        sql = "select " + databasePermIdColumn + " from " + databaseTable + " order by " + databasePermIdColumn;
    }

    @Override
    protected List<String> getAllEntities(DeliveryExecutionContext executionContext, String sessionToken)
    {
        return getAllEntities(executionContext, sessionToken, sql);
    }

    protected List<String> getAllEntities(DeliveryExecutionContext executionContext, String sessionToken, String query)
    {
        IDataSourceQueryService queryService = executionContext.getQueryService();
        List<String> permIds = new ArrayList<>();
        String dataSourceName = context.getOpenBisDataSourceName();
        DataSet<Map<String, Object>> select = queryService.select(dataSourceName, query);
        for (Map<String, Object> row : select)
        {
            permIds.add((String) row.get(databasePermIdColumn));
        }
        return permIds;
    }

}
