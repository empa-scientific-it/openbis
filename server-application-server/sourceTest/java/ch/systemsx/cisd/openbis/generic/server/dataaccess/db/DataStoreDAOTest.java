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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link DataStoreDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
{ "db", "dataStore" })
public final class DataStoreDAOTest extends AbstractDAOTest
{
    private static final String DATA_STORE_CODE = "xxx";

    @Test
    public void testCreate()
    {
        DataStorePE dataStore = new DataStorePE();
        initializeDataStore(dataStore);
        String code = dataStore.getCode();
        dataStore.setServices(createDataStoreServices(code, getDataSetTypes()));
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
        assertNotNull(dataStore.getId());

        DataStorePE store = daoFactory.getDataStoreDAO().tryToFindDataStoreByCode(code);
        AssertJUnit.assertEquals(dataStore, store);
        // check if changing the registered services does not fail when the result of this test
        // method will be commited
        dataStore.setServices(createDataStoreServices("another", getDataSetTypes()));
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);

        dataStore.setServices(new HashSet<DataStoreServicePE>()); // delete all services
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
    }

    protected void initializeDataStore(DataStorePE dataStore)
    {
        dataStore.setDatabaseInstanceUUID(DATA_STORE_CODE);
        String code = DATA_STORE_CODE;
        dataStore.setCode(code);
        dataStore.setDownloadUrl(code);
        dataStore.setRemoteUrl(code);
        dataStore.setSessionToken(code);
    }

    @Test
    public void testCreateFotAllDataSetTypes()
    {
        DataStorePE dataStore = new DataStorePE();
        initializeDataStore(dataStore);
        String code = dataStore.getCode();
        dataStore.setServices(createDataStoreServices(code, null));
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
        assertNotNull(dataStore.getId());

        DataStorePE store = daoFactory.getDataStoreDAO().tryToFindDataStoreByCode(code);
        AssertJUnit.assertEquals(dataStore, store);
    }

    private Set<DataStoreServicePE> createDataStoreServices(String prefix,
            Set<DataSetTypePE> dataSetTypes)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        DataStoreServicePE service;
        for (int i = 0; i < 3; i++)
        {
            service = new DataStoreServicePE();
            service.setKey(prefix + "_key_" + i);
            service.setLabel("label");
            service.setKind(DataStoreServiceKind.PROCESSING);
            service.setDatasetTypes(dataSetTypes);
            service.setReportingPluginTypeOrNull(null);
            services.add(service);
        }

        int i = 3;
        // Add a query as well
        service = new DataStoreServicePE();
        service.setKey(prefix + "_key_" + i++);
        service.setLabel("label");
        service.setKind(DataStoreServiceKind.QUERIES);
        service.setDatasetTypes(dataSetTypes);
        service.setReportingPluginTypeOrNull(ReportingPluginType.TABLE_MODEL);
        services.add(service);

        // Add another query
        service = new DataStoreServicePE();
        service.setKey(prefix + "_key_" + i++);
        service.setLabel("label");
        service.setKind(DataStoreServiceKind.QUERIES);
        service.setDatasetTypes(dataSetTypes);
        service.setReportingPluginTypeOrNull(ReportingPluginType.DSS_LINK);
        services.add(service);

        return services;
    }

    private Set<DataSetTypePE> getDataSetTypes()
    {
        Set<DataSetTypePE> datasetTypes = new HashSet<DataSetTypePE>();
        DataSetTypePE type =
                daoFactory.getDataSetTypeDAO().tryToFindDataSetTypeByCode(
                        DataSetTypeCode.UNKNOWN.getCode());
        datasetTypes.add(type);
        return datasetTypes;
    }
}
