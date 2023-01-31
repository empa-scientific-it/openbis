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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AuthorizationService;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.SearchService;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Implementation of {@link IPluginScriptRunnerFactory} based on Jython scripts.
 * 
 * @author Piotr Buczek
 */
public class PluginScriptRunnerFactory implements IPluginScriptRunnerFactory
{

    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PluginScriptRunnerFactory.class);

    private final static String SEARCH_SERVICE_VARIABLE_NAME = "searchService";

    private final static String SEARCH_SERVICE_UNFILTERED_VARIABLE_NAME = "searchServiceUnfiltered";

    private final static String DATA_SOURCE_QUERY_SERVICE_VARIABLE_NAME = "queryService";

    private final static String MAIL_SERVICE_VARIABLE_NAME = "mailService";

    private final static String AUTHORIZATION_SERVICE = "authorizationService";

    private static final String CONTENT_PROVIDER_VARIABLE_NAME = "contentProvider";

    private static final String CONTENT_PROVIDER_UNFILTERED_VARIABLE_NAME =
            "contentProviderUnfiltered";

    private static final String SESSION_WORKSPACE_PROVIDER_NAME = "sessionWorkspaceProvider";

    private static final String USER_ID = "userId";

    private static final String USER_SESSION_TOKEN = "userSessionToken";

    private final String scriptPath;

    public PluginScriptRunnerFactory(String scriptPath)
    {
        this.scriptPath = scriptPath;
        Evaluator.getFactory().initialize();
    }

    /**
     * Factory method for creating an IAggregationServiceReportingPluginScriptRunner for a given processing context.
     */
    @Override
    public IAggregationServiceReportingPluginScriptRunner createAggregationServiceReportingPluginRunner(
            DataSetProcessingContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            IJythonEvaluator evaluator =
                    createEvaluatorWithContentProviders(context, scriptString, pythonPath);

            return new AggregationServiceReportingPluginScriptRunner(evaluator);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    @Override
    public IDbModifyingAggregationServiceReportingPluginScriptRunner createDbModifyingAggregationServiceReportingPluginRunner(
            DataSetProcessingContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            IJythonEvaluator evaluator =
                    createEvaluatorWithContentProviders(context, scriptString, pythonPath);

            return new DbModifyingAggregationServiceReportingPluginScriptRunner(evaluator);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IReportingPluginScriptRunner for a given processing context.
     */
    @Override
    public IReportingPluginScriptRunner createReportingPluginRunner(DataSetProcessingContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            return new ReportingPluginScriptRunner(createEvaluator(scriptString, pythonPath, context));
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IProcessingPluginScriptRunner for a given processing context.
     */
    @Override
    public IProcessingPluginScriptRunner createProcessingPluginRunner(
            DataSetProcessingContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            return new ProcessingPluginScriptRunner(createEvaluator(scriptString, pythonPath, context));
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    @Override
    public IRequestHandlerPluginScriptRunner createRequestHandlerPluginRunner(DataSetProcessingContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            IJythonEvaluator evaluator = createEvaluatorWithContentProviders(context, scriptString, pythonPath);

            return new RequestHandlerPluginScriptRunner(evaluator);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    protected IJythonEvaluator createEvaluator(String scriptString, String[] pythonPath, DataSetProcessingContext context)
    {
        final IJythonEvaluator evaluator = Evaluator.getFactory().create("", pythonPath, scriptPath, null, scriptString, false);

        evaluator.set(SEARCH_SERVICE_VARIABLE_NAME, createUserSearchService(context));
        evaluator.set(SEARCH_SERVICE_UNFILTERED_VARIABLE_NAME, createUnfilteredSearchService());

        evaluator.set(MAIL_SERVICE_VARIABLE_NAME, createMailService(context));
        evaluator.set(DATA_SOURCE_QUERY_SERVICE_VARIABLE_NAME, createDataSourceQueryService());
        evaluator.set(AUTHORIZATION_SERVICE, createAuthorizationService());
        evaluator.set(USER_ID, context.getUserId());
        evaluator.set(USER_SESSION_TOKEN, context.trySessionToken());
        final ISessionWorkspaceProvider workspaceProvider =
                context.tryGetSessionWorkspaceProvider();
        if (workspaceProvider != null)
        {
            evaluator.set(SESSION_WORKSPACE_PROVIDER_NAME, workspaceProvider);
        }
        return evaluator;
    }

    private IJythonEvaluator createEvaluatorWithContentProviders(
            DataSetProcessingContext context, String scriptString, String[] pythonPath)
    {
        IJythonEvaluator evaluator = createEvaluator(scriptString, pythonPath, context);

        DataSetContentProvider contentProvider =
                new DataSetContentProvider(context.getHierarchicalContentProvider());
        evaluator.set(CONTENT_PROVIDER_VARIABLE_NAME, contentProvider);

        DataSetContentProvider contentProviderUnfiltered =
                new DataSetContentProvider(context.getHierarchicalContentProviderUnfiltered());
        evaluator.set(CONTENT_PROVIDER_UNFILTERED_VARIABLE_NAME, contentProviderUnfiltered);

        return evaluator;
    }

    protected ISearchService createUserSearchService(DataSetProcessingContext context)
    {
        if (context.getUserId() == null)
            return createUnfilteredSearchService();
        else
            return new SearchService(ServiceProvider.getOpenBISService()
                    .getBasicFilteredOpenBISService(context.getUserId()));
    }

    protected ISearchService createUnfilteredSearchService()
    {
        return new SearchService(ServiceProvider.getOpenBISService());
    }

    protected IAuthorizationService createAuthorizationService()
    {
        return new AuthorizationService(ServiceProvider.getOpenBISService());
    }

    protected IDataSourceQueryService createDataSourceQueryService()
    {
        return new DataSourceQueryService();
    }

    private static IMailService createMailService(DataSetProcessingContext context)
    {
        return new MailService(context.getMailClient(), context.getUserEmailOrNull());
    }

    private static final class DataSetContentProvider implements IDataSetContentProvider, IReleasable
    {
        private final IHierarchicalContentProvider contentProvider;

        private final Map<String, IHierarchicalContent> contents =
                new HashMap<String, IHierarchicalContent>();

        public DataSetContentProvider(IHierarchicalContentProvider contentProvider)
        {
            this.contentProvider = contentProvider;
        }

        @Override
        public IHierarchicalContent getContent(String dataSetCode)
        {
            IHierarchicalContent content = contents.get(dataSetCode);
            if (content == null)
            {
                content = contentProvider.asContent(dataSetCode);
                contents.put(dataSetCode, content);
            }
            return content;
        }

        @Override
        public void release()
        {
            for (IHierarchicalContent content : contents.values())
            {
                content.close();
            }
        }
    }

    private abstract static class AbstractAggregationServiceReportingPluginScriptRunner
    {

        final IJythonEvaluator evaluator;

        protected abstract String getFunctionName();

        public AbstractAggregationServiceReportingPluginScriptRunner(IJythonEvaluator evaluator)
        {
            this.evaluator = evaluator;

            if (false == evaluator.hasFunction(getFunctionName()))
            {
                throw new EvaluatorException("Function '" + getFunctionName()
                        + "' was not defined in the reporting plugin script");
            }
        }

        public void releaseResources()
        {
            evaluator.releaseResources();
        }
    }

    private static class AggregationServiceReportingPluginScriptRunner extends
            AbstractAggregationServiceReportingPluginScriptRunner implements
            IAggregationServiceReportingPluginScriptRunner
    {
        private final static String FUNCTION_NAME = "aggregate";

        @Override
        protected String getFunctionName()
        {
            return FUNCTION_NAME;
        }

        AggregationServiceReportingPluginScriptRunner(
                IJythonEvaluator evaluator)
        {
            super(evaluator);
        }

        @Override
        public void aggregate(Map<String, Object> parameters,
                ISimpleTableModelBuilderAdaptor tableBuilder) throws EvaluatorException
        {
            evaluator.evalFunction(FUNCTION_NAME, parameters, tableBuilder);
        }

    }

    private static class DbModifyingAggregationServiceReportingPluginScriptRunner extends
            AbstractAggregationServiceReportingPluginScriptRunner implements
            IDbModifyingAggregationServiceReportingPluginScriptRunner
    {
        private final static String FUNCTION_NAME = "process";

        DbModifyingAggregationServiceReportingPluginScriptRunner(
                IJythonEvaluator evaluator)
        {
            super(evaluator);
        }

        @Override
        protected String getFunctionName()
        {
            return FUNCTION_NAME;
        }

        @Override
        public void process(IDataSetRegistrationTransactionV2 transaction,
                Map<String, Object> parameters, ISimpleTableModelBuilderAdaptor tableBuilder)
                throws EvaluatorException
        {
            evaluator.evalFunction(FUNCTION_NAME, transaction, parameters, tableBuilder);
        }

    }

    private static class ReportingPluginScriptRunner implements IReportingPluginScriptRunner
    {
        private final static String DESCRIBE_FUNCTION_NAME = "describe";

        private final IJythonEvaluator evaluator;

        ReportingPluginScriptRunner(IJythonEvaluator evaluator)
        {
            this.evaluator = evaluator;
            if (false == evaluator.hasFunction(DESCRIBE_FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + DESCRIBE_FUNCTION_NAME
                        + "' was not defined in the reporting plugin script");
            }
        }

        @Override
        public void describe(List<IDataSet> dataSets, ISimpleTableModelBuilderAdaptor tableBuilder)
        {
            evaluator.evalFunction(DESCRIBE_FUNCTION_NAME, dataSets, tableBuilder);
        }

        @Override
        public void releaseResources()
        {
            evaluator.releaseResources();
        }
    }

    private static class ProcessingPluginScriptRunner implements IProcessingPluginScriptRunner
    {
        private final static String PROCESS_FUNCTION_NAME = "process";

        private final IJythonEvaluator evaluator;

        ProcessingPluginScriptRunner(IJythonEvaluator evaluator)
        {
            this.evaluator = evaluator;
            if (false == evaluator.hasFunction(PROCESS_FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                        + "' was not defined in the processing plugin script");
            }
        }

        @Override
        public Status process(IDataSet dataSet)
        {
            try
            {
                evaluator.evalFunction(PROCESS_FUNCTION_NAME, dataSet);
            } catch (EvaluatorException ex)
            {
                operationLog.error(ex.getMessage());
                return Status.createError(ex.getMessage());
            }
            return Status.OK;
        }

        @Override
        public void releaseResources()
        {
            evaluator.releaseResources();
        }

    }

    private static class RequestHandlerPluginScriptRunner implements IRequestHandlerPluginScriptRunner
    {
        private final static String FUNCTION_NAME = "handle";

        private final IJythonEvaluator evaluator;

        RequestHandlerPluginScriptRunner(IJythonEvaluator evaluator)
        {
            this.evaluator = evaluator;
            if (false == evaluator.hasFunction(FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + FUNCTION_NAME
                        + "' was not defined in the request handler");
            }
        }

        @Override
        public void setVariable(String name, Object value)
        {
            evaluator.set(name, value);
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response)
        {
            evaluator.evalFunction(FUNCTION_NAME, request, response);
        }

        @Override
        public void releaseResources()
        {
            evaluator.releaseResources();
        }
    }

    @Override
    public String getScriptPath()
    {
        return scriptPath;
    }

}
