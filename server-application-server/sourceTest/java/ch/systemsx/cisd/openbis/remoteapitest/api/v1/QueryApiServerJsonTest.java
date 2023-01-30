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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import static org.testng.AssertJUnit.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;

/**
 * Verifies that an instance of {@link IQueryApiServer} is published via JSON-RPC and that it is correctly functioning with external clients.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = { "remote api" }, dependsOnGroups = { "before remote api" })
public class QueryApiServerJsonTest extends RemoteApiTestCase
{
    private static final String SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + "/openbis/"
            + IQueryApiServer.JSON_SERVICE_URL;

    protected IQueryApiServer queryApiService;

    protected String sessionToken;

    protected IQueryApiServer createService()
    {
        try
        {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(SERVICE_URL));
            return ProxyUtil
                    .createClientProxy(getClass().getClassLoader(), IQueryApiServer.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        queryApiService = createService();
        sessionToken = queryApiService.tryToAuthenticateAtQueryServer("test", "a");
    }

    @AfterMethod
    public void afterMethod() throws MalformedURLException
    {
        queryApiService.logout(sessionToken);
    }

    @Test
    public void testListQueries()
    {
        List<QueryDescription> queries = queryApiService.listQueries(sessionToken);
        assertNotNull(queries);
    }

    @Test
    public void testExecuteQuery()
    {
        List<QueryDescription> queries = queryApiService.listQueries(sessionToken);
        assertNotNull(queries);
        QueryDescription queryToRun = null;
        for (QueryDescription query : queries)
        {
            if (query.getParameters().isEmpty())
            {
                queryToRun = query;
                break;
            }
        }
        if (null == queryToRun)
        {
            return;
        }

        QueryTableModel result =
                queryApiService.executeQuery(sessionToken, queryToRun.getId(),
                        new HashMap<String, String>());
        assertNotNull(result);
    }
}
