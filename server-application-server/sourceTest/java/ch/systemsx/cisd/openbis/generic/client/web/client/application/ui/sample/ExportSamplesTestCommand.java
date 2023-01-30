/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import junit.framework.Assert;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Client;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.TableExportType;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.ITestCommand;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * System tests for the server side of sample export.<BR>
 * We wait for the listing operation to complete and then initiate 2 subsequent server calls to get the exported table (when preparation is finished
 * it triggers getting the content).<BR>
 * It's hard to have complete tests of the client side due to the fact, that the final response is received from the servlet and is opened in a new
 * browser window which is hard to access.
 * 
 * @author Tomasz Pylak
 */
public class ExportSamplesTestCommand extends AbstractDefaultTestCommand
{
    private static final String LINE_SEPARATOR = "\n";

    private final Client client;

    private String receivedExportedFileContent = null;

    public ExportSamplesTestCommand(Client client)
    {
        this.client = client;
        this.receivedExportedFileContent = null;
    }

    @Override
    public void execute()
    {
        @SuppressWarnings("unchecked")
        AbstractEntityGrid<Sample> sampleBrowserGrid =
                (AbstractEntityGrid<Sample>) GWTTestUtil
                        .getWidgetWithID(SampleBrowserGrid.MAIN_BROWSER_ID);
        // we do not create view context earlier (e.g. in the class constructor), because we have to
        // wait until client is loaded and viewContext is available.
        IViewContext<ICommonClientServiceAsync> viewContext = getViewContext();
        sampleBrowserGrid.export(TableExportType.VISIBLE, new PrepareExportSamplesCallbackTest(
                viewContext));
    }

    private IViewContext<ICommonClientServiceAsync> getViewContext()
    {
        return client.tryToGetViewContext();
    }

    private final class PrepareExportSamplesCallbackTest extends AbstractAsyncCallback<String>
    {
        public PrepareExportSamplesCallbackTest(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(String exportDataKey)
        {
            assert viewContext != null : "viewContext is null";

            ICommonClientServiceAsync service = this.viewContext.getCommonService();
            service.getExportTable(exportDataKey, LINE_SEPARATOR,
                    new SaveExportedContentCallbackTest(this.viewContext));
        }
    }

    private final class SaveExportedContentCallbackTest extends AbstractAsyncCallback<String>
    {
        public SaveExportedContentCallbackTest(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(String exportedFileContent)
        {
            receivedExportedFileContent = exportedFileContent;
        }

    }

    /**
     * A command is executed when export preparation operation and saving the result operation have successfully completed. The result is compared
     * with the specified expected content.
     */
    public ITestCommand createCheckExportCommand(String expectedHeader,
            String expectedFirstLineOrNull, int expectedTotalLines)
    {
        return new CheckExportedContentCommand(expectedHeader, expectedFirstLineOrNull,
                expectedTotalLines);
    }

    // NOTE: shares the receivedExportedFileContent variable with SaveExportedContentCallbackTest
    private final class CheckExportedContentCommand extends AbstractDefaultTestCommand
    {
        private final String expectedHeader;

        private final String expectedFirstLineOrNull;

        private final int expectedTotalLines;

        private CheckExportedContentCommand(String expectedHeader, String expectedFirstLineOrNull,
                int expectedTotalLines)
        {
            this.expectedHeader = expectedHeader;
            this.expectedFirstLineOrNull = expectedFirstLineOrNull;
            this.expectedTotalLines = expectedTotalLines;
        }

        @Override
        public void execute()
        {
            Assert.assertNotNull("unexpected null export content", receivedExportedFileContent);

            String lines[] = parse(receivedExportedFileContent);
            Assert.assertEquals(expectedTotalLines, lines.length);
            Assert.assertEquals(expectedHeader, lines[0]);
            if (expectedFirstLineOrNull != null)
            {
                Assert.assertTrue(lines.length > 1);
                Assert.assertEquals(expectedFirstLineOrNull, lines[1]);
            }
        }

        private String[] parse(String content)
        {
            return content.split(LINE_SEPARATOR);
        }
    }
}
