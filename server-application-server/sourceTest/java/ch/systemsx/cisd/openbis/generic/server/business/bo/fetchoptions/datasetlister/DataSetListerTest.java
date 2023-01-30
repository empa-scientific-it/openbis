/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import net.lemnik.eodsql.DynamicQuery;
import net.lemnik.eodsql.TransactionQuery;

/**
 * Test cases for {@link IDataSetListingQuery}.
 * 
 * @author Piotr Kupczyk
 */
@Test(groups = { "db", "dataset" })
public class DataSetListerTest extends AbstractDAOTest
{

    interface IDataSetListingQueryDynamic extends IDataSetListingQuery, DynamicQuery,
            TransactionQuery
    {
    }

    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    IGeneralInformationService service;

    private IDataSetListingQueryDynamic query;

    private IDataSetLister lister;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticateForAllServices("test", "password");
        query = EntityListingTestUtils.createQuery(daoFactory, IDataSetListingQueryDynamic.class);
        lister = new DataSetLister(query, daoFactory, getTestPerson());
    }

    @Test
    public void testGetDataSetMetaDataForEmptyDataSetCodesShouldReturnEmptyList()
    {
        List<String> codes = new ArrayList<String>();
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodeShouldReturnDataSet()
    {
        List<String> codes = Collections.singletonList("20081105092159188-3");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesShouldReturnDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForContainedDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20110509092359990-11");
        codes.add("20110509092359990-12");
        codes.add("VALIDATIONS_IMPOS-27");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertNotNull(result.get(0).getContainerOrNull());
        assertNotNull(result.get(1).getContainerOrNull());
        assertNotNull(result.get(2).getContainerOrNull());

        assertEquals(13, result.get(0).getContainerOrNull().getId().longValue());
        assertEquals("20110509092359990-10", result.get(0).getContainerOrNull().getCode());
        assertEquals(13, result.get(1).getContainerOrNull().getId().longValue());
        assertEquals("20110509092359990-10", result.get(1).getContainerOrNull().getCode());
        assertEquals(26, result.get(2).getContainerOrNull().getId().longValue());
        assertEquals("VALIDATIONS_CNTNR-26", result.get(2).getContainerOrNull().getCode());
    }

    @Test(groups = "broken")
    public void testGetDataStoreURLs()
    {
        try
        {
        	// given
            query.update("update data_stores set download_url='http://download_1',remote_url='http://remote_1'"
                    + " where code='STANDARD'");
            final long newDataStoreId =
                    (Long) query
                            .select("insert into data_stores (id,code,download_url,remote_url,session_token, uuid)"
                                    + " values (nextval('data_store_id_seq'),'DSS2','http://download_2','http://remote_2','', 'DSS2') returning id")
                            .get(0).get("id");
            query.update("update data set dast_id = ?{1} where code = ?{2}", newDataStoreId,
                    "20081105092259000-20");
            query.update("update data set dast_id = ?{1} where code = ?{2}", newDataStoreId,
                    "20081105092259000-21");

            List<String> codes = new ArrayList<String>();
            codes.add("20081105092159188-3");
            codes.add("20081105092159111-1");
            codes.add("20081105092259000-19");
            codes.add("20081105092259000-20");
            codes.add("20081105092259000-21");

            // when
            List<DataStoreURLForDataSets> result = lister.getDataStoreDownloadURLs(codes);

            // then
            assertEquals(2, result.size());
            for (DataStoreURLForDataSets url : result)
            {
                if (url.getDataStoreURL().equals("http://download_1"))
                {
                	assertListContains(Arrays.asList("20081105092159188-3", "20081105092159111-1",
                            "20081105092259000-19"), url.getDataSetCodes());
                } else if (url.getDataStoreURL().equals("http://download_2"))
                {
                	assertListContains(Arrays.asList("20081105092259000-20", "20081105092259000-21"), url.getDataSetCodes());
                } else
                {
                    fail("URL " + url + " not expected.");
                }
            }

            result = lister.getDataStoreRemoteURLs(codes);
            assertEquals(2, result.size());
            for (DataStoreURLForDataSets url : result)
            {
                if (url.getDataStoreURL().equals("http://remote_1"))
                {
                	assertListContains(Arrays.asList("20081105092159188-3", "20081105092159111-1",
                            "20081105092259000-19"), url.getDataSetCodes());
                } else if (url.getDataStoreURL().equals("http://remote_2"))
                {
                	assertListContains(Arrays.asList("20081105092259000-20", "20081105092259000-21"), url.getDataSetCodes());
                } else
                {
                    fail("URL " + url + " not expected.");
                }
            }
        } finally
        {
            query.rollback();
        }
    }

    private void assertListContains(List<String> expectedCodes, List<String> dataSetCodes)
	{
    	for (String expectedCode : expectedCodes)
    	{
    		if (false == dataSetCodes.contains(expectedCode))
    		{
    			fail("Data set code " + expectedCode + " is expected to be in collection " + dataSetCodes);
    		}
    	}
	}

	@Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithParentsShouldReturnDataSetsWithParents()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithChildrenShouldReturnDataSetsWithChildren()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.CHILDREN);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testGetDataSetMetaDataForExistingAndNotExistingDataSetCodesShouldThrowException()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("UNKNOWN-DATASET-CODE");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        lister.getDataSetMetaData(codes, fetchOptions);
    }

    @Test
    public void testGetDataSetMetaDataForLinkDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20120628092259000-23");
        codes.add("20120628092259000-24");
        codes.add("20120628092259000-25");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> results = lister.getDataSetMetaData(codes, fetchOptions);

        assertEquals(3, results.size());
        assertTrue(results.get(0).isLinkDataSet());
        assertTrue(results.get(1).isLinkDataSet());
        assertTrue(results.get(2).isLinkDataSet());
        assertFalse(results.get(0).isContainerDataSet());
        assertFalse(results.get(1).isContainerDataSet());
        assertFalse(results.get(2).isContainerDataSet());
        assertEquals("CODE1", results.get(0).getExternalDataSetCode());
        assertEquals("CODE2", results.get(1).getExternalDataSetCode());
        assertEquals("CODE3", results.get(2).getExternalDataSetCode());
        assertEquals("http://example.edms.pl/code=CODE1", results.get(0).getExternalDataSetLink());
        assertEquals("http://example.edms.pl/code=CODE2", results.get(1).getExternalDataSetLink());
        assertEquals("http://www.openbis.ch/perm_id=CODE3", results.get(2).getExternalDataSetLink());

        ExternalDataManagementSystem dms1 = new ExternalDataManagementSystem();
        dms1.setId(1L);
        dms1.setCode("DMS_1");
        dms1.setLabel("Test EDMS");
        dms1.setAddressType(ExternalDataManagementSystemType.URL);
        dms1.setOpenBIS(false);
        dms1.setUrlTemplate("http://example.edms.pl/code=${code}");

        ExternalDataManagementSystem dms2 = new ExternalDataManagementSystem();
        dms2.setId(2L);
        dms2.setCode("DMS_2");
        dms2.setLabel("Test External openBIS instance");
        dms2.setAddressType(ExternalDataManagementSystemType.OPENBIS);
        dms2.setOpenBIS(true);
        dms2.setUrlTemplate("http://www.openbis.ch/perm_id=${code}");

        assertEqualsToExternalDMS(dms1, results.get(0).getExternalDataManagementSystem());
        assertEqualsToExternalDMS(dms1, results.get(1).getExternalDataManagementSystem());
        assertEqualsToExternalDMS(dms2, results.get(2).getExternalDataManagementSystem());
    }

    private static void sortDataSetsByCode(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet o1, DataSet o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
    }

    private void assertEqualToDataSetMetaDataWithCodes(List<String> expectedDataSetsCodes,
            DataSetFetchOptions expectedFetchOptions, List<DataSet> actualDataSets)
    {
        assertEquals(expectedDataSetsCodes.size(), actualDataSets.size());

        List<DataSet> expectedDataSets =
                service.getDataSetMetaData(sessionToken, expectedDataSetsCodes);

        sortDataSetsByCode(expectedDataSets);
        sortDataSetsByCode(actualDataSets);

        Iterator<DataSet> expectedIterator = expectedDataSets.iterator();
        Iterator<DataSet> actualIterator = actualDataSets.iterator();

        // results should be returned in the same order
        while (expectedIterator.hasNext() && actualIterator.hasNext())
        {
            DataSet expectedDataSet = expectedIterator.next();
            DataSet actualDataSet = actualIterator.next();
            assertNotNull(expectedDataSet);
            assertNotNull(actualDataSet);
            assertEqualsToDataSet(expectedDataSet, expectedFetchOptions, actualDataSet);
        }
    }

    private void assertEqualsToDataSet(DataSet expected, DataSetFetchOptions expectedFetchOptions,
            DataSet actual)
    {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getExperimentIdentifier(), actual.getExperimentIdentifier());
        assertEquals(expected.getSampleIdentifierOrNull(), actual.getSampleIdentifierOrNull());
        assertEquals(expected.getDataSetTypeCode(), actual.getDataSetTypeCode());
        assertEquals(expected.isContainerDataSet(), actual.isContainerDataSet());
        assertEquals(expected.isLinkDataSet(), actual.isLinkDataSet());
        assertEqualsToRegistrationDetails(expected.getRegistrationDetails(),
                expected.getRegistrationDetails());

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.PARENTS))
        {
            assertCollections(expected.getParentCodes(), actual.getParentCodes());
        }

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.CHILDREN))
        {
            assertCollections(expected.getChildrenCodes(), actual.getChildrenCodes());
        }

        assertEquals(expectedFetchOptions, actual.getFetchOptions());
    }

    private static <T extends Comparable<T>> void assertCollections(Collection<T> col1,
            Collection<T> col2)
    {
        ArrayList<T> a1 = new ArrayList<T>(col1);
        ArrayList<T> a2 = new ArrayList<T>(col2);

        Collections.sort(a1);
        Collections.sort(a2);

        assertEquals(a1, a2);
    }

    private void assertEqualsToRegistrationDetails(EntityRegistrationDetails expected,
            EntityRegistrationDetails actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getUserId(), actual.getUserId());
            assertEquals(expected.getUserFirstName(), actual.getUserFirstName());
            assertEquals(expected.getUserLastName(), actual.getUserLastName());
            assertEquals(expected.getUserEmail(), actual.getUserEmail());
            assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        }

    }

    private void assertEqualsToExternalDMS(ExternalDataManagementSystem expected,
            ExternalDataManagementSystem actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getCode(), actual.getCode());
            assertEquals(expected.getLabel(), actual.getLabel());
            assertEquals(expected.getUrlTemplate(), actual.getUrlTemplate());
            assertEquals(expected.isOpenBIS(), actual.isOpenBIS());
        }
    }
}