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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceRegistratorTest extends AssertJUnit
{
    private static final String DATASTORE_CODE = "DSS";

    private static final Comparator<DataStoreServicePE> SERVICE_COMPARATOR =
            new Comparator<DataStoreServicePE>()
                {
                    @Override
                    public int compare(DataStoreServicePE s1, DataStoreServicePE s2)
                    {
                        return s1.getKey().compareTo(s2.getKey());
                    }
                };

    private BufferedAppender logRecorder;

    private Mockery context;

    private IDAOFactory daoFactory;

    private IDataStoreDAO dataStoreDAO;

    private IDataSetTypeDAO dataSetTypeDAO;

    private DataStoreServiceRegistrator dataStoreServiceRegistrator;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder();
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        dataStoreDAO = context.mock(IDataStoreDAO.class);
        dataSetTypeDAO = context.mock(IDataSetTypeDAO.class);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));

                    allowing(daoFactory).getDataSetTypeDAO();
                    will(returnValue(dataSetTypeDAO));
                }
            });
        dataStoreServiceRegistrator = new DataStoreServiceRegistrator(daoFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        context.assertIsSatisfied();
    }

    @Test
    public void testSetServiceDescriptions()
    {
        final DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(DATASTORE_CODE);
        DatastoreServiceDescription r1 =
                DatastoreServiceDescription.reporting("R1", "r1", new String[]
                { "A.*", "B" }, DATASTORE_CODE, ReportingPluginType.TABLE_MODEL);
        DatastoreServiceDescription p1 =
                DatastoreServiceDescription.processing("P1", "p1", new String[]
                { "A.*", "C.*", "D" }, DATASTORE_CODE);
        context.checking(new Expectations()
            {
                {
                    one(dataSetTypeDAO).listAllEntities();
                    will(returnValue(Arrays.asList(dataSetType("A1"), dataSetType("B1"),
                            dataSetType("C1"), dataSetType("D"), dataSetType("D1"))));

                    one(dataStoreDAO).createOrUpdateDataStore(dataStore);
                }
            });

        dataStoreServiceRegistrator.setServiceDescriptions(dataStore,
                new DatastoreServiceDescriptions(Arrays.asList(r1), Arrays.asList(p1)));

        List<DataStoreServicePE> services =
                new ArrayList<DataStoreServicePE>(dataStore.getServices());
        Collections.sort(services, SERVICE_COMPARATOR);
        assertEquals("P1", services.get(0).getKey());
        assertEquals("p1", services.get(0).getLabel());
        assertEquals(DataStoreServiceKind.PROCESSING, services.get(0).getKind());
        assertEquals(null, services.get(0).getReportingPluginTypeOrNull());
        assertEquals("[A1, C1, D]", extractedDataSetTypes(services.get(0)).toString());
        assertEquals("R1", services.get(1).getKey());
        assertEquals("r1", services.get(1).getLabel());
        assertEquals(DataStoreServiceKind.QUERIES, services.get(1).getKind());
        assertEquals(ReportingPluginType.TABLE_MODEL, services.get(1)
                .getReportingPluginTypeOrNull());
        assertEquals("[A1]", extractedDataSetTypes(services.get(1)).toString());
        assertEquals(2, services.size());
        assertEquals("The Datastore Server Plugin '[QUERIES; R1; DSS; r1; A.* B ; TABLE_MODEL]' "
                + "is misconfigured. It refers to the dataset types "
                + "which do not exist in openBIS: [B]", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegister()
    {
        final DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(DATASTORE_CODE);
        DatastoreServiceDescription r1 =
                DatastoreServiceDescription.reporting("R1", "r1", new String[]
                { "A.*", "B" }, DATASTORE_CODE, ReportingPluginType.TABLE_MODEL);
        DatastoreServiceDescription p1 =
                DatastoreServiceDescription.processing("P1", "p1", new String[]
                { "A.*", "C.*", "D" }, DATASTORE_CODE);
        context.checking(new Expectations()
            {
                {
                    one(dataSetTypeDAO).listAllEntities();
                    will(returnValue(Arrays.asList(dataSetType("A1"), dataSetType("B1"),
                            dataSetType("C1"), dataSetType("D"), dataSetType("D1"))));

                    exactly(3).of(dataStoreDAO).createOrUpdateDataStore(dataStore);

                    exactly(2).of(dataStoreDAO).tryToFindDataStoreByCode(DATASTORE_CODE);
                    will(returnValue(dataStore));

                    exactly(2).of(dataSetTypeDAO).tryToFindDataSetTypeByCode("A3");
                    will(returnValue(dataSetType("A3")));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode("B");
                    will(returnValue(dataSetType("B")));
                }
            });
        dataStoreServiceRegistrator.setServiceDescriptions(dataStore,
                new DatastoreServiceDescriptions(Arrays.asList(r1), Arrays.asList(p1)));

        dataStoreServiceRegistrator.register(new DataSetTypeBuilder().code("A3").getDataSetType());
        dataStoreServiceRegistrator.register(new DataSetTypeBuilder().code("B").getDataSetType());

        List<DataStoreServicePE> services =
                new ArrayList<DataStoreServicePE>(dataStore.getServices());
        Collections.sort(services, SERVICE_COMPARATOR);
        assertEquals("P1", services.get(0).getKey());
        assertEquals("p1", services.get(0).getLabel());
        assertEquals(DataStoreServiceKind.PROCESSING, services.get(0).getKind());
        assertEquals(null, services.get(0).getReportingPluginTypeOrNull());
        assertEquals("[A1, A3, C1, D]", extractedDataSetTypes(services.get(0)).toString());
        assertEquals("R1", services.get(1).getKey());
        assertEquals("r1", services.get(1).getLabel());
        assertEquals(DataStoreServiceKind.QUERIES, services.get(1).getKind());
        assertEquals(ReportingPluginType.TABLE_MODEL, services.get(1)
                .getReportingPluginTypeOrNull());
        assertEquals("[A1, A3, B]", extractedDataSetTypes(services.get(1)).toString());
        assertEquals(2, services.size());
        context.assertIsSatisfied();
    }

    private DataSetTypePE dataSetType(String code)
    {
        DataSetTypePE type = new DataSetTypePE();
        type.setCode(code);
        return type;
    }

    private List<String> extractedDataSetTypes(DataStoreServicePE service)
    {
        Set<DataSetTypePE> datasetTypes = service.getDatasetTypes();
        List<String> list = new ArrayList<String>();
        for (DataSetTypePE type : datasetTypes)
        {
            list.add(type.getCode());
        }
        Collections.sort(list);
        return list;
    }

}
