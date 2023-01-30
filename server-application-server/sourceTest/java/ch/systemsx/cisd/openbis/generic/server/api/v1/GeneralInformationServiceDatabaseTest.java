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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import static ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption.CHILDREN;
import static ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption.CONTAINED;
import static ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption.CONTAINER;
import static ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption.PARENTS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;

/**
 * @author pkupczyk
 */
public class GeneralInformationServiceDatabaseTest extends AbstractDAOTest
{
    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    IGeneralInformationService service;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticateForAllServices("test", "password");
    }

    @Test
    public void testGetDataSetMetaDataWithBasicFetchOptions()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092259000-19");
        EnumSet<DataSetFetchOption> fetchOptions = EnumSet.noneOf(DataSetFetchOption.class);

        List<DataSet> result = service.getDataSetMetaData(sessionToken, codes, fetchOptions);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(DataSetFetchOption.BASIC));
        assertTrue(result.get(1).getFetchOptions().isSetOf(DataSetFetchOption.BASIC));
    }

    @Test
    public void testGetDataSetMetaDataWithAllFetchOptions()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("CONTAINER_1");
        codes.add("20081105092259000-19");
        EnumSet<DataSetFetchOption> fetchOptions = EnumSet.allOf(DataSetFetchOption.class);

        List<DataSet> result = service.getDataSetMetaData(sessionToken, codes, fetchOptions);

        Collections.sort(result, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet ds0, DataSet ds1)
                {
                    return ds0.getCode().compareTo(ds1.getCode());
                }
            });
        assertEquals("20081105092259000-19", result.get(0).getCode());
        assertChildrenAndParents("[20081105092259000-20, 20081105092259000-21]", "[]", result.get(0));
        assertComponentsAndContainers("[]", "[]", result.get(0));
        assertTrue(result.get(0).getFetchOptions().isSetOf(DataSetFetchOption.values()));
        assertEquals("CONTAINER_1", result.get(1).getCode());
        assertChildrenAndParents("[]", "[]", result.get(1));
        assertComponentsAndContainers("[COMPONENT_1A, COMPONENT_1B]", "[ROOT_CONTAINER]", result.get(1));
        assertTrue(result.get(1).getFetchOptions().isSetOf(DataSetFetchOption.values()));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetDataSetMetaDataWithParentsChildren()
    {
        assertChildrenAndParents("[20081105092359990-2]", "[20081105092259000-9]", Arrays.asList("20081105092259900-0"));
        assertChildrenAndParents("[]", "[]", Arrays.asList("CONTAINER_1"));
    }

    @Test
    public void testGetDataSetMetaDataWithContainersComponents()
    {
        assertComponentsAndContainers("[]", "[]", Arrays.asList("20081105092259900-0"));
        assertComponentsAndContainers("[COMPONENT_1A, COMPONENT_1B]", "[ROOT_CONTAINER]", Arrays.asList("CONTAINER_1"));
    }

    private void assertComponentsAndContainers(String expecedComponents, String expectedContainers, List<String> dataSetCodes)
    {
        List<DataSet> dataSets = service.getDataSetMetaData(sessionToken, dataSetCodes, EnumSet.of(CONTAINED, CONTAINER));

        DataSet dataSet = dataSets.get(0);
        assertComponentsAndContainers(expecedComponents, expectedContainers, dataSet);
        assertEquals(1, dataSets.size());
    }

    private void assertChildrenAndParents(String expecedChildren, String expectedParents, List<String> dataSetCodes)
    {
        List<DataSet> dataSets = service.getDataSetMetaData(sessionToken, dataSetCodes, EnumSet.of(CHILDREN, PARENTS));

        assertChildrenAndParents(expecedChildren, expectedParents, dataSets.get(0));
        assertEquals(1, dataSets.size());
    }

    private void assertChildrenAndParents(String expecedChildren, String expectedParents, DataSet dataSet)
    {
        List<String> childrenCodes = new ArrayList<String>(dataSet.getChildrenCodes());
        Collections.sort(childrenCodes);
        assertEquals(expecedChildren, childrenCodes.toString());
        List<String> parentCodes = new ArrayList<String>(dataSet.getParentCodes());
        Collections.sort(parentCodes);
        assertEquals(expectedParents, parentCodes.toString());
    }

    private void assertComponentsAndContainers(String expecedComponents, String expectedContainers, DataSet dataSet)
    {
        List<DataSet> containedDataSets = dataSet.getContainedDataSets();
        assertEquals(expecedComponents, extractCodes(containedDataSets).toString());
        List<DataSet> containerDataSets = dataSet.getContainerDataSets();
        assertEquals(expectedContainers, extractCodes(containerDataSets).toString());
    }

    private List<String> extractCodes(List<DataSet> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        Collections.sort(codes);
        return codes;
    }

}
