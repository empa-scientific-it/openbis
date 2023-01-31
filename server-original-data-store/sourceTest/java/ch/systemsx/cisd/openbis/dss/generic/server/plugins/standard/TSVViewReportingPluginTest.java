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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractFileTableReportingPlugin.IGNORE_COMMENTS_PROPERTY_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractFileTableReportingPlugin.IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class TSVViewReportingPluginTest extends AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "42";

    private static final String TEST_FILE = "test.txt";

    private File store;

    private File dataSetInStore;

    private DatasetDescription datasetDescription;

    private DataSetProcessingContext processingContext;

    @BeforeMethod
    public void beforeMethod()
    {
        store = new File(workingDirectory, "store");
        store.mkdirs();
        dataSetInStore = new File(new File(store, SHARE_ID), "dataset-ds1");
        dataSetInStore.mkdirs();
        datasetDescription = new DatasetDescription();
        datasetDescription.setDataSetCode("ds1");
        datasetDescription.setMainDataSetPattern(".*");
        datasetDescription.setDataSetLocation(dataSetInStore.getName());

        processingContext =
                new DataSetProcessingContext(getMockHierarchicalContentProvider(),
                        new MockDataSetDirectoryProvider(store, SHARE_ID), null, null, "test-user",
                        null);
    }

    private IHierarchicalContentProvider getMockHierarchicalContentProvider()
    {
        return new IHierarchicalContentProvider()
            {
                private DefaultFileBasedHierarchicalContentFactory hierarchicalContentFactory =
                        new DefaultFileBasedHierarchicalContentFactory();

                @Override
                public IHierarchicalContent asContent(File datasetDirectory)
                {
                    return hierarchicalContentFactory.asHierarchicalContent(datasetDirectory,
                            IDelegatedAction.DO_NOTHING);
                }

                @Override
                public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
                {
                    return getContent(datasetLocation.getDataSetLocation());
                }

                @Override
                public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode) throws IllegalArgumentException
                {
                    return null; // not necessary for this test
                }

                @Override
                public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet)
                {
                    return null; // not necessary for this test
                }

                public IHierarchicalContent getContent(String location)
                {
                    return asContent(new File(new File(store, SHARE_ID), location));
                }

                @Override
                public IHierarchicalContent asContent(AbstractExternalData dataSet)
                {
                    return getContent(dataSet.getCode());
                }

                @Override
                public IHierarchicalContent asContent(String dataSetCode)
                        throws IllegalArgumentException
                {
                    return getContent("dataset-" + dataSetCode);
                }

                @Override
                public IHierarchicalContentProvider cloneFor(
                        ISessionTokenProvider sessionTokenProvider)
                {
                    return null;
                }
            };
    }

    @Test
    public void testCreateReport()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), "a\t<1?a:b>b\n1\t2\n\t4");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(new Properties(), store);
        TableModel tableModel =
                plugin.createReport(Arrays.asList(datasetDescription), processingContext);

        List<TableModelColumnHeader> headers = tableModel.getHeader();
        assertEquals("a", headers.get(0).getTitle());
        assertEquals("A", headers.get(0).getId());
        assertEquals("b", headers.get(1).getTitle());
        assertEquals("1_A_B", headers.get(1).getId());
        assertEquals(2, headers.size());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals("[1, 2]", rows.get(0).getValues().toString());
        assertEquals("[, 4]", rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
    }

    @Test
    public void testCreateReportFailingBecauseOfInvalidNumberOfCells()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), "a\tb\n1\t2\t3\t\n");
        Properties properties = new Properties();
        properties.setProperty(IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY, "true");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(properties, store);

        try
        {
            plugin.createReport(Arrays.asList(datasetDescription), processingContext);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number of columns in header (2) does not match number of columns in "
                    + "1. data row (3) in Data Set 'ds1' file.", ex.getMessage());
        }
    }

    @Test
    public void testIgnoringCommentAndTrailingEmptyCells()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), "a\tb\n" + "#comment\n"
                + "1\ta\t\t\n" + "2\tb\t\n" + "3\tc\n" + "4\t\n");
        Properties properties = new Properties();
        properties.setProperty(IGNORE_COMMENTS_PROPERTY_KEY, "true");
        properties.setProperty(IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY, "true");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(properties, store);
        TableModel tableModel =
                plugin.createReport(Arrays.asList(datasetDescription), processingContext);

        assertEquals("[a, b]", tableModel.getHeader().toString());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals("[1, a]", rows.get(0).getValues().toString());
        assertEquals("[2, b]", rows.get(1).getValues().toString());
        assertEquals("[3, c]", rows.get(2).getValues().toString());
        assertEquals("[4, ]", rows.get(3).getValues().toString());
        assertEquals(4, rows.size());
    }
}
