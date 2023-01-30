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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;

/**
 * @author Franz-Josef Elmer
 */
/*
 * This test registers a new data store "ABC" for which a new entry in data_stores table is added and an entry in
 * DataStoreServiceRegistrator.dataStoreToServicesMap map is created. As the database transaction is always rolled back after each test, the entry in
 * data_stores table is removed. The rollback does not influence DataStoreServiceRegistrator.dataStoreToServicesMap which still contains "ABC" data
 * store. Because of this inconsistent state other test that use DataStoreServiceRegistrator may fail. That's why "ANewMetadataUITest" test was added
 * to be executed first (before the inconsistent state is created).
 */
@Test(dependsOnGroups = { "ANewMetadataUITest" })
public class DataStoreServiceRegistrationTest extends SystemTestCase
{
    private static final String DOWNLOAD_URL = "blabla";

    private static final String SESSION_TOKEN = "123";

    private static final String DATASTORE_CODE = "ABC";

    private Mockery context;

    @BeforeMethod
    public void addMockDataStoreService() throws Exception
    {
        context = new Mockery();
        final IDataStoreService dataStoreService = context.mock(IDataStoreService.class);
        Object bean = CommonServiceProvider.tryToGetBean("dss-factory");
        getDataStoreServices(bean).put("http://localhost:0", dataStoreService);
        context.checking(new Expectations()
            {
                {
                    allowing(dataStoreService).getVersion(SESSION_TOKEN);
                    will(returnValue(IDataStoreService.VERSION));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private Map<String, IDataStoreService> getDataStoreServices(Object bean) throws Exception
    {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (field.getType().getName().equals(Map.class.getName()))
            {
                field.setAccessible(true);
                return (Map<String, IDataStoreService>) field.get(bean);
            }
        }
        throw new IllegalArgumentException("Has no services map: " + bean);
    }

    @Test
    public void test()
    {
        DataStoreServerInfo dataStoreServerInfo = new DataStoreServerInfo();
        dataStoreServerInfo.setDataStoreCode(DATASTORE_CODE);
        dataStoreServerInfo.setSessionToken(SESSION_TOKEN);
        dataStoreServerInfo.setDownloadUrl(DOWNLOAD_URL);
        DatastoreServiceDescription r1 =
                DatastoreServiceDescription.reporting("R1", "r1", new String[]
                { "H.*", "UNKNOWN" }, DATASTORE_CODE, ReportingPluginType.TABLE_MODEL);
        DatastoreServiceDescription p1 =
                DatastoreServiceDescription.processing("P1", "p1", new String[]
                { "H.*", "C.*" }, DATASTORE_CODE);
        dataStoreServerInfo.setServicesDescriptions(new DatastoreServiceDescriptions(Arrays
                .asList(r1), Arrays.asList(p1)));
        dataStoreServerInfo.setDataSourceDefinitions(Arrays.<DataSourceDefinition> asList());

        // 1. register DSS
        etlService.registerDataStoreServer(systemSessionToken, dataStoreServerInfo);

        checkDataSetTypes("[HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA, UNKNOWN]",
                "[CONTAINER_TYPE, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]");

        // 2. register a new data set type
        DataSetType dataSetType =
                new DataSetTypeBuilder().code("H").getDataSetType();
        commonServer.registerDataSetType(systemSessionToken, dataSetType);

        checkDataSetTypes("[H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA, UNKNOWN]",
                "[CONTAINER_TYPE, H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]");

        // 3. re-register DSS
        etlService.registerDataStoreServer(systemSessionToken, dataStoreServerInfo);

        checkDataSetTypes("[H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA, UNKNOWN]",
                "[CONTAINER_TYPE, H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]");

        // 4. delete the new data set type and register it again
        commonServer.deleteDataSetTypes(systemSessionToken, Arrays.asList("H"));
        commonServer.registerDataSetType(systemSessionToken, dataSetType);

        checkDataSetTypes("[H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA, UNKNOWN]",
                "[CONTAINER_TYPE, H, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]");
    }

    private void checkDataSetTypes(String expectedReportingDataSetTypes,
            String expectedProcessingDataSetTypes)
    {
        List<DatastoreServiceDescription> services =
                commonServer
                        .listDataStoreServices(systemSessionToken, DataStoreServiceKind.QUERIES);
        Collections.sort(services);
        assertEquals("R1", services.get(0).getKey());
        assertEquals("r1", services.get(0).getLabel());
        assertEquals(DOWNLOAD_URL, services.get(0).getDownloadURL());
        assertEquals(DataStoreServiceKind.QUERIES, services.get(0).getServiceKind());
        assertEquals(DATASTORE_CODE, services.get(0).getDatastoreCode());
        assertEquals(expectedReportingDataSetTypes, getDataSetTypeCodes(services.get(0)).toString());
        assertEquals(1, services.size());
        services =
                commonServer.listDataStoreServices(systemSessionToken,
                        DataStoreServiceKind.PROCESSING);
        Collections.sort(services);
        assertEquals("P1", services.get(0).getKey());
        assertEquals("p1", services.get(0).getLabel());
        assertEquals(DOWNLOAD_URL, services.get(0).getDownloadURL());
        assertEquals(DataStoreServiceKind.PROCESSING, services.get(0).getServiceKind());
        assertEquals(DATASTORE_CODE, services.get(0).getDatastoreCode());
        assertEquals(expectedProcessingDataSetTypes, getDataSetTypeCodes(services.get(0))
                .toString());
        assertEquals(2, services.size());
    }

    private List<String> getDataSetTypeCodes(DatastoreServiceDescription description)
    {
        List<String> result =
                new ArrayList<String>(Arrays.asList(description.getDatasetTypeCodes()));
        Collections.sort(result);
        return result;
    }
}
