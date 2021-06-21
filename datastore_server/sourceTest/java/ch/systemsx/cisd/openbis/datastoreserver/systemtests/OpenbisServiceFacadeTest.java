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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class OpenbisServiceFacadeTest extends SystemTestCase
{
    private static final Comparator<FileInfoDssDTO> FILE_INFO_COMPARATOR =
            new Comparator<FileInfoDssDTO>()
                {
                    @Override
                    public int compare(FileInfoDssDTO f1, FileInfoDssDTO f2)
                    {
                        return f1.getPathInDataSet().compareTo(f2.getPathInDataSet());
                    }
                };

    private IOpenbisServiceFacade serviceFacade;

    @BeforeMethod
    public void beforeMethod()
    {
        serviceFacade = createServiceFacade("test");
    }

    @Test
    public void testPutDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet, "my-data");
    }

    @Test
    public void testPutDataSetWithDynamicPropertyTest() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data-with-dyna-test");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet, "my-data-with-dyna-test");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testPutDataSetWithExperimentWithProjectAuthorization(ProjectAuthorizationUser user) throws IOException
    {
        IOpenbisServiceFacade userServiceFacade = createServiceFacade(user.getUserId());

        File exampleDataSet = new File(workingDirectory, "my-data-pa");
        DataSetOwner dataSetOwner = new DataSetOwner(DataSetOwnerType.EXPERIMENT, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet, dataSetOwner);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            DataSet dataSet = userServiceFacade.putDataSet(newDataset, exampleDataSet);
            assertNotNull(dataSet);
        } else
        {
            try
            {
                userServiceFacade.putDataSet(newDataset, exampleDataSet);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testPutDataSetWithSampleWithProjectAuthorization(ProjectAuthorizationUser user) throws IOException
    {
        IOpenbisServiceFacade userServiceFacade = createServiceFacade(user.getUserId());

        File exampleDataSet = new File(workingDirectory, "my-data-pa");

        DataSetOwner dataSetOwner = new DataSetOwner(DataSetOwnerType.SAMPLE, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet, dataSetOwner);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            DataSet dataSet = userServiceFacade.putDataSet(newDataset, exampleDataSet);
            assertNotNull(dataSet);
        } else
        {
            try
            {
                userServiceFacade.putDataSet(newDataset, exampleDataSet);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test
    public void testPutDataSetWithParent() throws Exception
    {
        String code = createAndLoadADataSet().getCode();

        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        newDataset.setParentDataSetCodes(Arrays.asList(code));
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet, "my-data");

        // We need to take a different route to get the data set we just registered to check if it
        // has a parent.
        List<Sample> samples = serviceFacade.getSamples(Arrays.asList("/CISD/NEMO/CP-TEST-1"));
        List<DataSet> dataSets =
                serviceFacade.listDataSets(samples, EnumSet.allOf(Connections.class));

        List<String> parentCodes = null;
        for (DataSet sampleDataSet : dataSets)
        {
            if (dataSet.getCode().equals(sampleDataSet.getCode()))
            {
                parentCodes = sampleDataSet.getParentCodes();
                break;
            }
        }

        assertEquals(Arrays.asList(code), parentCodes);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Parent codes were not retrieved for data set .*")
    public void testFailureAccessingParentFromSearchResult() throws Exception
    {
        DataSet dataSet = createAndLoadADataSet();
        dataSet.getParentCodes();
    }

    @Test
    public void testGetDataSetGetFile() throws Exception
    {
        DataSet ds = createAndLoadADataSet();

        String path = "original/my-data/data";
        FileInfoDssDTO[] files = ds.listFiles(path, false);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(2, files.length);
        assertEquals(fileInfoString(path, "1.data", 5), files[0].toString());
        assertEquals(fileInfoString(path, "2.data", 7), files[1].toString());

        files = ds.listFiles("/", true);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(7, files.length);
        assertEquals(fileInfoString("original", -1), files[0].toString());
        assertEquals(fileInfoString("original/my-data", -1), files[1].toString());
        assertEquals(fileInfoString("original/my-data/data", -1), files[2].toString());
        assertEquals(fileInfoString("original/my-data/data-set.properties", 28),
                files[3].toString());
        assertEquals(fileInfoString("original/my-data/data.log", 11), files[4].toString());
        assertEquals(fileInfoString("original/my-data/data/1.data", 5), files[5].toString());
        assertEquals(fileInfoString("original/my-data/data/2.data", 7), files[6].toString());
    }

    @Test
    public void testGetDataSetContainedAndContainerDataSets() throws Exception
    {
        DataSet ds = createAndLoadADataSet();
        List<DataSet> contained = ds.getContainedDataSets();
        assertEquals(0, contained.size());
        List<DataSet> containers = ds.getContainerDataSets();
        assertEquals(0, containers.size());

        // The primary data set for a normal (non-container) data set is itself
        assertNotNull(ds.getPrimaryDataSetOrNull());
        assertEquals(ds, ds.getPrimaryDataSetOrNull());
    }

    private static String fileInfoString(String startPath, String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                pathInListing, length);
    }

    private static String fileInfoString(String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s,%s,%d]", pathInListing, pathInListing, length);
    }

    @Test
    public void testGetDataSetGetLink() throws Exception
    {
        DataSet ds = createAndLoadADataSet();
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet(ds.getCode());
        File fileIntoStore =
                new File(new File(store,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                        dataSetInfo.getDataSetLocation());

        File link = ds.tryLinkToContents(null);
        assertEquals(fileIntoStore.getAbsolutePath(), link.getAbsolutePath());
        File file = ds.getLinkOrCopyOfContents(null, workingDirectory);
        assertEquals(fileIntoStore.getAbsolutePath(), file.getAbsolutePath());
    }

    @Test
    public void testGetDataSetGetCopy() throws Exception
    {
        DataSet ds = createAndLoadADataSet();

        assertEquals(null, ds.tryLinkToContents("blabla"));
        File file = ds.getLinkOrCopyOfContents("blabla", workingDirectory);
        assertContent("hello world", file, "data.log");
        assertContent("1 2 3", file, "data/1.data");
        assertContent("4 5 6 7", file, "data/2.data");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoWritePermissions() throws Exception
    {
        serviceFacade = createServiceFacade("observer");
        File exampleDataSet = new File(workingDirectory, "observer-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        serviceFacade.putDataSet(newDataset, exampleDataSet);
    }

    @Test
    public void testSearchForLinkedDataSets() throws Exception
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "LINK_TYPE"));
        List<DataSet> foundDataSets = serviceFacade.searchForDataSets(sc);
        List<DataSet> dataSets = filterToExpected(foundDataSets);
        assertEquals("Should have found three data sets. Found " + dataSets.size(), 3,
                dataSets.size());

        assertEquals("CODE1", dataSets.get(0).getExternalDataSetCode());
        assertEquals("CODE2", dataSets.get(1).getExternalDataSetCode());
        assertEquals("CODE3", dataSets.get(2).getExternalDataSetCode());
        assertEquals("http://example.edms.pl/code=CODE1", dataSets.get(0).getExternalDataSetLink());
        assertEquals("http://example.edms.pl/code=CODE2", dataSets.get(1).getExternalDataSetLink());
        assertEquals("http://www.openbis.ch/perm_id=CODE3", dataSets.get(2)
                .getExternalDataSetLink());
        assertEquals("DMS_1", dataSets.get(0).getExternalDataManagementSystem().getCode());
        assertEquals("DMS_1", dataSets.get(1).getExternalDataManagementSystem().getCode());
        assertEquals("DMS_2", dataSets.get(2).getExternalDataManagementSystem().getCode());
    }

    @Test
    public void testListSamplesForProjects()
    {
        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/CISD/NOE", "/TEST-SPACE/TEST-PROJECT"));

        assertIdentifiers(samples, "/CISD/NOE/CP-TEST-2", "/TEST-SPACE/TEST-PROJECT/FV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-INVALID",
                "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID", "/TEST-SPACE/TEST-PROJECT/EV-PARENT", "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL", "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE");
    }

    @Test
    public void testListSamplesForProjectsWithProjectThatHasSameCodeAsProjectInDifferentSpace()
    {
        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/TEST-SPACE/NOE"));

        assertIdentifiers(samples, "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testListSamplesForProjectsWithProjectNotInMySpace()
    {
        serviceFacade = createServiceFacade("test_space");

        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/CISD/NOE", "/TEST-SPACE/TEST-PROJECT"));

        assertIdentifiers(samples, "/TEST-SPACE/TEST-PROJECT/FV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-INVALID", "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID",
                "/TEST-SPACE/TEST-PROJECT/EV-PARENT", "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL", "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE");
    }

    @Test
    public void testListSamplesForProjectsWithNotExistingProject()
    {
        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/CISD/NOT_EXISTING_PROJECT"));

        assertEquals(0, samples.size());
    }

    @Test
    public void testListSamplesForProjectsWithProperties()
    {
        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/CISD/NOE"), EnumSet.of(SampleFetchOption.PROPERTIES));

        assertIdentifiers(samples, "/CISD/NOE/CP-TEST-2");

        Sample sample = samples.get(0);
        Map<String, String> properties = sample.getProperties();
        assertEquals(5, properties.size());
    }

    @Test
    public void testListSamplesForProjectsWithoutProperties()
    {
        List<Sample> samples = serviceFacade.listSamplesForProjects(Arrays.asList("/CISD/NOE"));

        assertIdentifiers(samples, "/CISD/NOE/CP-TEST-2");

        Sample sample = samples.get(0);
        try
        {
            sample.getProperties();
            Assert.fail();
        } catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().startsWith("Properties were not retrieved"));
        }
    }

    private List<DataSet> filterToExpected(List<DataSet> foundDataSets)
    {
        DataSet[] expected = new DataSet[3];
        for (DataSet foundDataSet : foundDataSets)
        {
            if ("CODE1".equals(foundDataSet.getExternalDataSetCode()))
            {
                expected[0] = foundDataSet;
            } else if ("CODE2".equals(foundDataSet.getExternalDataSetCode()))
            {
                expected[1] = foundDataSet;
            } else if ("CODE3".equals(foundDataSet.getExternalDataSetCode()))
            {
                expected[2] = foundDataSet;
            }
        }
        return Arrays.asList(expected);
    }

    private IOpenbisServiceFacade createServiceFacade(String userName)
    {
        return OpenbisServiceFacadeFactory.tryCreate(userName, "a",
                TestInstanceHostUtils.getOpenBISUrl(), 5 * DateUtils.MILLIS_PER_MINUTE);
    }

    private NewDataSetDTO createNewDataSetDTO(File exampleDataSet) throws IOException
    {
        DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, "/CISD/NEMO/CP-TEST-1");
        return createNewDataSetDTO(exampleDataSet, dataSetOwner);
    }

    private NewDataSetDTO createNewDataSetDTO(File exampleDataSet, DataSetOwner dataSetOwner) throws IOException
    {
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "data.log"), "hello world");
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set.properties"),
                "property\tvalue\nCOMMENT\thello");
        File subFolder = new File(exampleDataSet, "data");
        subFolder.mkdirs();
        FileUtilities.writeToFile(new File(subFolder, "1.data"), "1 2 3");
        FileUtilities.writeToFile(new File(subFolder, "2.data"), "4 5 6 7");
        String rootPath = exampleDataSet.getCanonicalPath();
        FileInfoDssBuilder builder = new FileInfoDssBuilder(rootPath, rootPath);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        builder.appendFileInfosForFile(exampleDataSet, list, true);
        NewDataSetDTO newDataset = new NewDataSetDTO(dataSetOwner, exampleDataSet.getName(), list);
        return newDataset;
    }

    private void assertContent(String expectedContent, File root, String path)
    {
        assertEquals(expectedContent,
                FileUtilities.loadToString(new File(root, "original/my-data/" + path)).trim());
    }

    private void assertIdentifiers(List<Sample> actualSamples, String... expectedIdentifiers)
    {
        assertEquals(expectedIdentifiers != null ? expectedIdentifiers.length : 0, actualSamples != null ? actualSamples.size() : 0);

        if (expectedIdentifiers != null && actualSamples != null)
        {
            Set<String> expectedIdentifiersSet = new HashSet<String>(Arrays.asList(expectedIdentifiers));

            for (Sample actualSample : actualSamples)
            {
                expectedIdentifiersSet.remove(actualSample.getIdentifier());
            }

            assertTrue(expectedIdentifiersSet.isEmpty());
        }
    }

    private SimpleDataSetInformationDTO getCodeOfLatestDataSet(String dataSetCode)
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> dataSets = openBISService.listPhysicalDataSets();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (dataSet.getDataSetCode().equals(dataSetCode))
            {
                return dataSet;
            }
        }
        return dataSets.get(0);
    }

    private void checkDataSet(DataSet dataSet, String folderName) throws IOException
    {
        assertEquals("hello world", getContent(dataSet, folderName + "/data.log"));
        assertEquals("1 2 3", getContent(dataSet, folderName + "/data/1.data"));
        assertEquals("4 5 6 7", getContent(dataSet, folderName + "/data/2.data"));
    }

    private String getContent(DataSet dataSet, String path) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try
        {
            inputStream = dataSet.getFile("/original/" + path);
            IOUtils.copy(inputStream, output);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
        return output.toString();
    }

    private DataSet createAndLoadADataSet() throws IOException
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        String code = dataSet.getCode();
        // Because serviceFacade.getDataSet() relies on updated lucene index (which is done
        // asynchronously) we tried it for 60 second to retrieve the just created data set.
        for (int i = 0; i < 60; i++)
        {
            DataSet retrievedDataSet = serviceFacade.getDataSet(code);
            if (retrievedDataSet != null)
            {
                return retrievedDataSet;
            }
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        throw new AssertionError("Couldn't retrieve back the just created data set " + code + ".");
    }

}
