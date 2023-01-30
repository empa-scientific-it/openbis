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

package ch.systemsx.cisd.openbis.plugin.query.server.api.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTableModelTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryApiServer extends AbstractServer<IQueryApiServer> implements IQueryApiServer
{
    @Resource(name = ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames.QUERY_PLUGIN_SERVER)
    private IQueryServer queryServer;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    public QueryApiServer()
    {
    }

    public QueryApiServer(IQueryServer queryServer, ICommonServer commonServer,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.queryServer = queryServer;
        this.commonServer = commonServer;
    }

    @Override
    public IQueryApiServer createLogger(IInvocationLoggerContext context)
    {
        return new QueryApiLogger(sessionManager, context);
    }

    @Override
    public String tryToAuthenticateAtQueryServer(String userID, String userPassword)
    {
        SessionContextDTO session = tryAuthenticate(userID, userPassword);
        if (session != null)
        {
            queryServer.initDatabases(session.getSessionToken());
        }
        return session == null ? null : session.getSessionToken();
    }

    @Override
    public List<QueryDescription> listQueries(String sessionToken)
    {
        List<QueryDescription> result = new ArrayList<QueryDescription>();
        List<QueryExpression> queries =
                queryServer.listQueries(sessionToken, QueryType.GENERIC,
                        BasicEntityType.UNSPECIFIED);
        for (QueryExpression queryExpression : queries)
        {
            QueryDescription queryDescription = new QueryDescription();
            queryDescription.setId(queryExpression.getId());
            queryDescription.setName(queryExpression.getName());
            queryDescription.setDescription(queryExpression.getDescription());
            List<String> parameters = queryExpression.getParameters();
            List<String> parameterNames = new ArrayList<String>();
            for (String parameter : parameters)
            {
                int indexOfDelim = parameter.indexOf("::");
                parameterNames.add(indexOfDelim < 0 ? parameter : parameter.substring(0,
                        indexOfDelim));
            }
            queryDescription.setParameters(parameterNames);
            result.add(queryDescription);
        }
        return result;
    }

    @Override
    public QueryTableModel executeQuery(String sessionToken, long queryID,
            Map<String, String> parameterBindings)
    {
        QueryParameterBindings bindings = new QueryParameterBindings();
        for (Entry<String, String> entry : parameterBindings.entrySet())
        {
            bindings.addBinding(entry.getKey(), entry.getValue());
        }
        return translate(queryServer.queryDatabase(sessionToken, new TechId(queryID), bindings));
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions(String sessionToken)
    {
        checkSession(sessionToken);

        List<ReportDescription> services = new ArrayList<ReportDescription>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            for (DataStoreServicePE service : dataStore.getServices())
            {
                if (service.isTableReport())
                {
                    ReportDescription info = new ReportDescription();
                    info.setKey(service.getKey());
                    info.setLabel(service.getLabel());
                    info.setDataStoreCode(dataStore.getCode());
                    Set<DataSetTypePE> datasetTypes = service.getDatasetTypes();
                    List<String> dataSetTypeCodes = new ArrayList<String>();
                    for (DataSetTypePE dataSetType : datasetTypes)
                    {
                        dataSetTypeCodes.add(dataSetType.getCode());
                    }
                    info.setDataSetTypes(dataSetTypeCodes);
                    services.add(info);
                }
            }
        }
        return services;
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String dataStoreCode,
            String serviceKey, List<String> dataSetCodes)
    {
        DatastoreServiceDescription description =
                DatastoreServiceDescription.reporting(serviceKey, "", new String[0], dataStoreCode,
                        null);
        return translate(commonServer.createReportFromDatasets(sessionToken, description,
                dataSetCodes));
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String serviceKey,
            List<String> dataSetCodes)
    {
        return translate(commonServer.createReportFromDatasets(sessionToken, serviceKey,
                dataSetCodes));
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
    {
        checkSession(sessionToken);

        List<AggregationServiceDescription> services =
                new ArrayList<AggregationServiceDescription>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            for (DataStoreServicePE service : dataStore.getServices())
            {
                boolean reportingService = service.getKind() == DataStoreServiceKind.QUERIES;
                ReportingPluginType reportingPluginType = service.getReportingPluginTypeOrNull();
                boolean aggregationTableReport =
                        reportingPluginType != null
                                && reportingPluginType == ReportingPluginType.AGGREGATION_TABLE_MODEL;
                if (reportingService && aggregationTableReport)
                {
                    AggregationServiceDescription info = new AggregationServiceDescription();
                    info.setServiceKey(service.getKey());
                    info.setLabel(service.getLabel());
                    info.setDataStoreCode(dataStore.getCode());
                    info.setDataStoreBaseUrl(dataStore.getDownloadUrl());
                    services.add(info);
                }
            }
        }
        return services;
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String dataStoreCode, String serviceKey, Map<String, Object> parameters)
    {
        checkSession(sessionToken);

        DatastoreServiceDescription description =
                DatastoreServiceDescription.reporting(serviceKey, "", new String[0], dataStoreCode,
                        ReportingPluginType.AGGREGATION_TABLE_MODEL);
        return translate(commonServer.createReportFromAggregationService(sessionToken, description,
                parameters));
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 6;
    }

    private QueryTableModel translate(TableModel result)
    {
        return new QueryTableModelTranslator(result).translate();
    }
}
