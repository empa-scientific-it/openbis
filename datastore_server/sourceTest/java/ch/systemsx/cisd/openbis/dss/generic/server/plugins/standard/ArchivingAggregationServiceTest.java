/*
 * Copyright 2019 ETH Zuerich, SIS
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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ArchivingAggregationService.GET_ARCHIVING_INFO_METHOD;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ArchivingAggregationService.METHOD_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author Franz-Josef Elmer
 */
public class ArchivingAggregationServiceTest extends AbstractFileSystemTestCase
{
    private Mockery context;

    private IApplicationServerApi v3api;

    private IArchiverPlugin archiver;

    private ArchivingAggregationService service;

    private IMailClient mailClient;

    private IHierarchicalContentProvider contentProvider;

    private DataSetProcessingContext processingContext;

    @BeforeMethod
    public void setUpTest()
    {
        context = new Mockery();
        v3api = context.mock(IApplicationServerApi.class);
        archiver = context.mock(IArchiverPlugin.class);
        mailClient = context.mock(IMailClient.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        processingContext =
                new DataSetProcessingContext(contentProvider, null, new HashMap<String, String>(),
                        mailClient, "test-user", "test-user");

        File store = new File(workingDirectory, "store");
        store.mkdirs();
        service = new ArchivingAggregationService(new Properties(), store, v3api, archiver);
    }

    @Test
    public void testSingleDataSetCase()
    {
        // Given
        prepareGetContainer("ds1", "ds1");
        prepareGetContainer("ds2", "ds2");
        prepareGetContainer("ds3", "ds3");
        RecordingMatcher<List<DataSetPermId>> actualDataSetsMatcher = prepareGetDataSets("ds1", "ds2", "ds3", "ds4");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_KEY, GET_ARCHIVING_INFO_METHOD);
        parameters.put(ArchivingAggregationService.ARGS_KEY, String.join(", ", "ds3", "ds1", "ds2"));

        // When
        List<TableModelRow> rows = service.createAggregationReport(parameters, processingContext).getRows();

        // Then
        assertEquals("[[OK, Operation Successful, "
                + "{\"ds1\":{\"bundleId\":\"ds1\",\"bundleSize\":10000,\"numberOfDataSets\":1,\"size\":10000},"
                + "\"ds2\":{\"bundleId\":\"ds2\",\"bundleSize\":10010,\"numberOfDataSets\":1,\"size\":10010},"
                + "\"ds3\":{\"bundleId\":\"ds3\",\"bundleSize\":10020,\"numberOfDataSets\":1,\"size\":10020},"
                + "\"total size\":30030}]]\n", renderRows(rows));
        assertEquals("[DS1, DS2, DS3]", actualDataSetsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testMultiDataSetCase()
    {
        // Given
        prepareGetContainer("ds1", "ds10", "ds9", "ds1");
        prepareGetContainer("ds2", "ds12", "ds2", "ds4");
        prepareGetContainer("ds3", "ds11", "ds3");
        RecordingMatcher<List<DataSetPermId>> actualDataSetsMatcher = prepareGetDataSets("ds1", "ds10", "ds2", "ds4", "ds3", "ds9", "ds12", "ds11");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_KEY, GET_ARCHIVING_INFO_METHOD);
        parameters.put(ArchivingAggregationService.ARGS_KEY, String.join(", ", "ds3", "ds1", "ds2"));

        // When
        List<TableModelRow> rows = service.createAggregationReport(parameters, processingContext).getRows();

        // Then
        assertEquals("[[OK, Operation Successful, "
                + "{\"ds1\":{\"bundleId\":\"ds1\",\"bundleSize\":30060,\"numberOfDataSets\":3,\"size\":10000},"
                + "\"ds2\":{\"bundleId\":\"ds12\",\"bundleSize\":30110,\"numberOfDataSets\":3,\"size\":10020},"
                + "\"ds3\":{\"bundleId\":\"ds11\",\"bundleSize\":20110,\"numberOfDataSets\":2,\"size\":10040},"
                + "\"total size\":80280}]]\n", renderRows(rows));
        assertEquals("[DS1, DS10, DS11, DS12, DS2, DS3, DS4, DS9]", actualDataSetsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testBundleWithDeletedDataSet()
    {
        // Given
        prepareGetContainer("ds9", "ds10", "ds9", "ds1");
        RecordingMatcher<List<DataSetPermId>> actualDataSetsMatcher = prepareGetDataSets("ds1", "ds9");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_KEY, GET_ARCHIVING_INFO_METHOD);
        parameters.put(ArchivingAggregationService.ARGS_KEY, String.join(", ", "ds9"));

        // When
        List<TableModelRow> rows = service.createAggregationReport(parameters, processingContext).getRows();

        // Then
        assertEquals("[[OK, Operation Successful, "
                + "{\"ds9\":{\"bundleId\":\"ds1\",\"bundleSize\":20010,\"numberOfDataSets\":2,\"size\":10010},"
                + "\"total size\":20010}]]\n", renderRows(rows));
        assertEquals("[DS1, DS10, DS9]", actualDataSetsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testTooLargeToUnarchive()
    {
        // Given
        prepareGetContainer("ds1", new UserFailureException("Too large"));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_KEY, GET_ARCHIVING_INFO_METHOD);
        parameters.put(ArchivingAggregationService.ARGS_KEY, String.join(", ", "ds1"));

        // When
        List<TableModelRow> rows = service.createAggregationReport(parameters, processingContext).getRows();

        // Then
        assertEquals("[[{args=ds1, method=getArchivingInfo}, Too large]]\n", renderRows(rows));
        context.assertIsSatisfied();
    }

    private RecordingMatcher<List<DataSetPermId>> prepareGetDataSets(String... dataSetCodes)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        List<DataSetPermId> ids = new ArrayList<>();
        Map<IDataSetId, DataSet> dataSets = new HashMap<>();
        for (int i = 0; i < dataSetCodes.length; i++)
        {
            String dataSetCode = dataSetCodes[i];
            DataSetPermId permId = new DataSetPermId(dataSetCode);
            ids.add(permId);
            DataSet dataSet = new DataSet();
            dataSet.setCode(dataSetCode);
            PhysicalData physicalData = new PhysicalData();
            physicalData.setSize((long) (i * 10 + 10000));
            dataSet.setPhysicalData(physicalData);
            dataSet.setFetchOptions(fetchOptions);
            dataSets.put(permId, dataSet);
        }
        Arrays.asList(dataSetCodes).stream().map(DataSetPermId::new).collect(Collectors.toList());
        RecordingMatcher<List<DataSetPermId>> actualDataSets = new RecordingMatcher<List<DataSetPermId>>();
        context.checking(new Expectations()
            {
                {
                    allowing(v3api).getDataSets(with(any(String.class)), with(actualDataSets),
                            with(new BaseMatcher<DataSetFetchOptions>()
                                {

                                    @Override
                                    public boolean matches(Object arg0)
                                    {
                                        if (arg0 instanceof DataSetFetchOptions)
                                        {
                                            DataSetFetchOptions actulaFetchOptions = (DataSetFetchOptions) arg0;
                                            assertEquals(actulaFetchOptions.toString(), fetchOptions.toString());
                                            return true;
                                        }
                                        return false;
                                    }

                                    @Override
                                    public void describeTo(Description arg0)
                                    {
                                        arg0.appendText(fetchOptions.toString());
                                    }
                                }));
                    will(returnValue(dataSets));
                }
            });
        return actualDataSets;
    }

    private void prepareGetContainer(String dataSetCode, String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(archiver).getDataSetCodesForUnarchiving(Arrays.asList(dataSetCode));
                    will(returnValue(Arrays.asList(dataSetCodes)));
                }
            });
    }

    private void prepareGetContainer(String dataSetCode, UserFailureException exception)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(archiver).getDataSetCodesForUnarchiving(Arrays.asList(dataSetCode));
                    will(throwException(exception));
                }
            });
    }

    private String renderRows(List<TableModelRow> rows)
    {
        StringBuilder builder = new StringBuilder();
        for (TableModelRow row : rows)
        {
            builder.append(Arrays.asList(row.getValues())).append("\n");
        }
        return builder.toString();
    }

}
