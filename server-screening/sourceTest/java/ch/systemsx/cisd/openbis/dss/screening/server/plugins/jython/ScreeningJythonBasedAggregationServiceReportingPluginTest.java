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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.JythonEvaluatorSpringComponent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedAggregationServiceReportingPluginTest.ParametersBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * @author Franz-Josef Elmer
 */
public class ScreeningJythonBasedAggregationServiceReportingPluginTest extends
        AbstractFileSystemTestCase
{
    private Mockery context;

    private ISearchService searchService;

    private IAuthorizationService authorizationService;

    private IDataSourceQueryService queryService;

    private File store;

    private File scriptFolder;

    private DataSetProcessingContext processingContext;

    private IScreeningOpenbisServiceFacade screeningFacade;

    @BeforeMethod
    public void setUpTest()
    {
        context = new Mockery();
        searchService = context.mock(ISearchService.class);
        queryService = context.mock(IDataSourceQueryService.class);
        screeningFacade = context.mock(IScreeningOpenbisServiceFacade.class);
        processingContext =
                new DataSetProcessingContext(null, null, new HashMap<String, String>(), null,
                        "test-user", "test-user");
        store = new File(workingDirectory, "store");
        store.mkdirs();
        scriptFolder = new File("resource/test-data/" + getClass().getSimpleName());

        Properties properties = new Properties();
        properties.setProperty(JythonEvaluatorSpringComponent.JYTHON_VERSION_KEY, "2.7");
        new JythonEvaluatorSpringComponent(properties);
    }

    @AfterMethod
    public void afterTest()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        context.checking(new Expectations()
            {
                {
                    ExperimentIdentifier expId =
                            ExperimentIdentifier.createFromAugmentedCode("/A/B/C");
                    one(screeningFacade).listPlates(expId);
                    will(returnValue(Arrays.asList(new Plate("P1", "A", "1", expId))));
                }
            });
        Map<String, Object> parameters =
                new ParametersBuilder().parameter("experiment-identifier", "/A/B/C")
                        .getParameters();

        IReportingPluginTask plugin = createPlugin("script.py");
        TableModel tableModel = plugin.createAggregationReport(parameters, processingContext);

        assertEquals("[Plate]", tableModel.getHeader().toString());
        assertEquals("[P1]", tableModel.getRows().get(0).getValues().toString());
        assertEquals(1, tableModel.getRows().size());
        context.assertIsSatisfied();

    }

    private IReportingPluginTask createPlugin(String scriptFile)
    {
        return new ScreeningJythonAggregationService(
                new Properties(),
                store,
                new ScreeningPluginScriptRunnerFactory(new File(scriptFolder, scriptFile).getPath())
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected ISearchService createUnfilteredSearchService()
                        {
                            return searchService;
                        }

                        @Override
                        protected ISearchService createUserSearchService(
                                DataSetProcessingContext dscontext)
                        {
                            return searchService;
                        }

                        @Override
                        protected IAuthorizationService createAuthorizationService()
                        {
                            return authorizationService;
                        }

                        @Override
                        protected IDataSourceQueryService createDataSourceQueryService()
                        {
                            return queryService;
                        }

                        @Override
                        protected IScreeningOpenbisServiceFacade createScreeningFacade(
                                DataSetProcessingContext c)
                        {
                            return screeningFacade;
                        }

                    });
    }
}
