/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;

/**
 * @author anttil
 */
public class AggregationReportRmi implements Command<QueryTableModel>
{

    @Inject
    private String session;

    @Inject
    private IQueryApiServer query;

    private String dataStoreName;

    private Map<String, Object> params;

    public AggregationReportRmi(String dataStoreName, Map<String, Object> params)
    {
        this.dataStoreName = dataStoreName;
        this.params = params;
    }

    @Override
    public QueryTableModel execute()
    {
        return query.createReportFromAggregationService(session, dataStoreName, "crud-ingestion-service", params);
    }
}
