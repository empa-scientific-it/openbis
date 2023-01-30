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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import jline.ConsoleReader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class QueryApiServerJsonTest
{

    private final IQueryApiServer queryApiService;

    private final String sessionToken;

    private static IQueryApiServer createService(String serverUrl)
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new GenericObjectMapper(), new URL(serverUrl
                            + IQueryApiServer.JSON_SERVICE_URL), new HashMap<String, String>());
            return ProxyUtil.createClientProxy(QueryApiServerJsonTest.class.getClassLoader(),
                    IQueryApiServer.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    private static ConsoleReader getConsoleReader()
    {
        try
        {
            return new ConsoleReader();
        } catch (final IOException ex)
        {
            throw new EnvironmentFailureException("ConsoleReader could not be instantiated.", ex);
        }
    }

    public static void main(String[] args) throws IOException
    {
        final String serverUrl;
        final String userId;
        final String password;
        if (args.length < 1)
        {
            serverUrl = getConsoleReader().readLine("Server URL: ");
        } else
        {
            serverUrl = args[0];
        }
        if (args.length < 2)
        {
            userId = getConsoleReader().readLine("User: ");
        } else
        {
            userId = args[1];
        }

        if (args.length < 3)
        {
            password = getConsoleReader().readLine("Password: ", Character.valueOf('*'));
        } else
        {
            password = args[2];
        }

        IQueryApiServer queryApiService = createService(serverUrl);
        String sessionToken = queryApiService.tryToAuthenticateAtQueryServer(userId, password);

        new QueryApiServerJsonTest(queryApiService, sessionToken).run();
    }

    private QueryApiServerJsonTest(IQueryApiServer queryApiService, String sessionToken)
    {
        this.queryApiService = queryApiService;
        this.sessionToken = sessionToken;
    }

    private void run()
    {
        QueryDescription queryToRun = null;
        List<QueryDescription> queries = queryApiService.listQueries(sessionToken);
        System.out.println("Queries:");
        System.out.println("Name\tDescription\tParameters");
        for (QueryDescription query : queries)
        {
            System.out.println("\t" + query.getName() + "\t[" + query.getDescription() + "]\t<"
                    + query.getParameters() + ">");
            if (null == queryToRun && query.getParameters().isEmpty())
            {
                queryToRun = query;
            }
        }

        System.out.println("\nRunning query " + queryToRun);
        @SuppressWarnings("null")
        QueryTableModel result =
                queryApiService.executeQuery(sessionToken, queryToRun.getId(),
                        new HashMap<String, String>());
        List<QueryTableColumn> columns = result.getColumns();
        for (int i = 0; i < columns.size(); ++i)
        {
            QueryTableColumn column = columns.get(i);
            System.out.print(column.getTitle());
            if (i < columns.size() - 1)
            {
                System.out.print("\t");
            }
        }
        System.out.print("\n");

        for (Serializable[] row : result.getRows())
        {
            for (int i = 0; i < row.length; ++i)
            {
                System.out.print(row[i]);
                if (i < row.length - 1)
                {
                    System.out.print("\t");
                }
            }
            System.out.print("\n");
        }

        queryApiService.logout(sessionToken);
    }
}
