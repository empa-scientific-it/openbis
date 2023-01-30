/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * Example of usage of Query API.
 * 
 * @author Franz-Josef Elmer
 */
public class QueryApiTest
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.err.println("Usage: <openbis-server-url> <user> <password>");
            return;
        }

        String serverURL = args[0];
        String userID = args[1];
        String password = args[2];

        IQueryApiFacade facade = FacadeFactory.create(serverURL, userID, password);

        List<QueryDescription> queries = facade.listQueries();
        for (QueryDescription queryDescription : queries)
        {
            System.out.println(queryDescription.getName() + ": " + queryDescription.getParameters());
        }

        List<AggregationServiceDescription> aggregationServices = facade.listAggregationServices();
        for (AggregationServiceDescription service : aggregationServices)
        {
            System.out.println(service.getServiceKey() + " : " + service.getType());
        }

    }
}
