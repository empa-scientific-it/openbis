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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.lemnik.eodsql.DataSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetImmutable;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class JythonBasedAggregationServiceReportingPluginTest extends AbstractFileSystemTestCase
{
    public static final class ParametersBuilder
    {
        private final Map<String, Object> parameters = new HashMap<String, Object>();

        public ParametersBuilder parameter(String name, Object value)
        {
            parameters.put(name, value);
            return this;
        }

        public Map<String, Object> getParameters()
        {
            return parameters;
        }
    }

    private Mockery context;

    private ISearchService searchService;

    private IDataSourceQueryService queryService;

    private IAuthorizationService authorizationService;

    private File store;

    private File scriptFolder;

    private DataSetProcessingContext processingContext;

    private IMailClient mailClient;

    private IHierarchicalContentProvider contentProvider;

    private IHierarchicalContent content;

    private IHierarchicalContentNode rootNode;

    private DataSet<?> dbDataSet;

    @BeforeMethod
    public void setUpTest()
    {
        context = new Mockery();
        searchService = context.mock(ISearchService.class);
        queryService = context.mock(IDataSourceQueryService.class);
        mailClient = context.mock(IMailClient.class);
        authorizationService = context.mock(IAuthorizationService.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        content = context.mock(IHierarchicalContent.class);
        rootNode = context.mock(IHierarchicalContentNode.class);
        dbDataSet = context.mock(DataSet.class);
        processingContext =
                new DataSetProcessingContext(contentProvider, null, new HashMap<String, String>(),
                        mailClient, "test-user", "test-user");
        store = new File(workingDirectory, "store");
        store.mkdirs();
        scriptFolder =
                new File("../datastore_server/resource/test-data/" + getClass().getSimpleName());
    }

    @AfterMethod
    public void afterTest()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCase()
    {
        final RecordingMatcher<SearchCriteria> searchCriteriaRecorder =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(searchService).searchForDataSets(with(searchCriteriaRecorder));
                    will(returnValue(Arrays.asList(new DataSetImmutable(new DataSetBuilder()
                            .code("ds1")
                            .experiment(
                                    new ExperimentBuilder().identifier("/A/B/ABC").getExperiment())
                            .getDataSet(), null))));

                    one(contentProvider).asContent("ds1");
                    will(returnValue(content));

                    one(content).getRootNode();
                    will(returnValue(rootNode));

                    one(rootNode).isDirectory();
                    will(returnValue(false));

                    one(content).close();

                    one(queryService).select("protein-db",
                            "select count(*) as count from proteins where data_set = ?{1}", "ds1");
                    will(returnValue(dbDataSet));

                    one(dbDataSet).size();
                    will(returnValue(1));

                    one(dbDataSet).get(0);
                    will(returnValue(new ParametersBuilder().parameter("count", 42L)
                            .getParameters()));
                }
            });
        Map<String, Object> parameters =
                new ParametersBuilder().parameter("experiment-code", "ABC").getParameters();

        IReportingPluginTask plugin = createPlugin("script.py");
        TableModel tableModel = plugin.createAggregationReport(parameters, processingContext);

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],"
                + "{SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "{SearchCriteria.AttributeMatchClause[ATTRIBUTE,CODE,ABC,EQUALS]},[]]]}]",
                searchCriteriaRecorder.recordedObject().toString());
        assertEquals("[Experiment, Data Set Code, Number of Files, Number of Proteins]", tableModel
                .getHeader().toString());
        assertEquals("[/A/B/ABC, ds1, 1, 42]", tableModel.getRows().get(0).getValues().toString());
        assertEquals(1, tableModel.getRows().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchServiceThrowsException()
    {
        final RecordingMatcher<SearchCriteria> searchCriteriaRecorder =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(searchService).searchForDataSets(with(searchCriteriaRecorder));
                    will(throwException(new IllegalArgumentException("Invalid")));
                }
            });
        Map<String, Object> parameters = new ParametersBuilder().getParameters();

        IReportingPluginTask plugin = createPlugin("script.py");
        try
        {
            plugin.createAggregationReport(parameters, processingContext);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Chosen plugin failed to create a report: "
                    + "java.lang.IllegalArgumentException: Invalid", ex.getMessage());
            String message = ex.getCause().getMessage();
            String prefix =
                    "Error occurred in line 28 of the script when evaluating 'aggregate({}, ";
            assertTrue("Message '" + message + "' doesn't starts with '" + prefix + "'.",
                    message.startsWith(prefix));
        }

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],"
                + "{SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "{SearchCriteria.AttributeMatchClause[ATTRIBUTE,CODE,<null>,EQUALS]},[]]]}]",
                searchCriteriaRecorder.recordedObject().toString());
        context.assertIsSatisfied();
    }

    private IReportingPluginTask createPlugin(String scriptFile)
    {
        return new JythonAggregationService(new Properties(), store, new PluginScriptRunnerFactory(
                new File(scriptFolder, scriptFile).getPath())
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected ISearchService createUnfilteredSearchService()
                {
                    return searchService;
                }

                @Override
                protected ISearchService createUserSearchService(DataSetProcessingContext dscontext)
                {
                    return searchService;
                }

                @Override
                protected IDataSourceQueryService createDataSourceQueryService()
                {
                    return queryService;
                }

                @Override
                protected IAuthorizationService createAuthorizationService()
                {
                    return authorizationService;
                }

            });
    }

}
