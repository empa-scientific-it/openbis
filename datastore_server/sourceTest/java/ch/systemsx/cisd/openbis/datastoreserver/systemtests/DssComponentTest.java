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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DssRegistrationLogDirectoryHelper;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "slow")
public class DssComponentTest extends SystemTestCase
{
    private static final Comparator<FileInfoDssDTO> FILE_INFO_COMPARATOR =
            new Comparator<FileInfoDssDTO>()
                {
                    public int compare(FileInfoDssDTO f1, FileInfoDssDTO f2)
                    {
                        return f1.getPathInDataSet().compareTo(f2.getPathInDataSet());
                    }
                };

    private static final String OPENBIS_URL = "http://localhost:8888";

    private IDssComponent dss;

    private File store;

    @BeforeMethod
    public void beforeMethod()
    {
        store = new File(rootDir, "store");
        store.mkdirs();
        dss = createDssComponent("test");
    }

    @Test
    public void testPutDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        IDataSetDss dataSet = dss.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet);
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testRegistrationLog() throws Exception
    {
        File registrationLogDir = getRegistrationLogDir();
        assertTrue(registrationLogDir.exists());

        File[] logDirContents = registrationLogDir.listFiles();
        // The log directory should have 3 sub directories for in-process, succeeded, failed.
        assertEquals(3, logDirContents.length);

        File succeededDir = new DssRegistrationLogDirectoryHelper(registrationLogDir).getSucceededDir();
        File[] succeededContents = succeededDir.listFiles();
        assertEquals(1, succeededContents.length);

    }

    @Test
    public void testValidateDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        List<ValidationError> errors = dss.validateDataSet(newDataset, exampleDataSet);
        assertEquals(0, errors.size());
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testPutDataSetWithParent() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();

        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        newDataset.setParentDataSetCodes(Arrays.asList(code));
        IDataSetDss dataSet = dss.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet);
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetFile() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();

        IDataSetDss ds = dss.getDataSet(code);

        assertEquals(code, ds.getCode());
        checkDataSet(ds);
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

    private static String fileInfoString(String startPath, String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                pathInListing, length);
    }

    private static String fileInfoString(String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s,%s,%d]", pathInListing, pathInListing, length);
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetLink() throws Exception
    {
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();
        File fileIntoStore =
                new File(new File(store,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                        dataSetInfo.getDataSetLocation());

        IDataSetDss ds = dss.getDataSet(code);

        File link = ds.tryLinkToContents(null);
        assertEquals(fileIntoStore.getAbsolutePath(), link.getAbsolutePath());
        File file = ds.getLinkOrCopyOfContents(null, workingDirectory);
        assertEquals(fileIntoStore.getAbsolutePath(), file.getAbsolutePath());
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetCopy() throws Exception
    {
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();

        IDataSetDss ds = dss.getDataSet(code);

        assertEquals(null, ds.tryLinkToContents("blabla"));
        File file = ds.getLinkOrCopyOfContents("blabla", workingDirectory);
        assertContent("hello world", file, "data.log");
        assertContent("1 2 3", file, "data/1.data");
        assertContent("4 5 6 7", file, "data/2.data");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoWritePermissions() throws Exception
    {
        dss = createDssComponent("observer");
        File exampleDataSet = new File(workingDirectory, "observer-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        dss.putDataSet(newDataset, exampleDataSet);
    }

    @Test(dependsOnMethods = "testPutDataSet", expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoReadPermissions() throws Exception
    {
        dss = createDssComponent("observer");
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();
        IDataSetDss dataSet = dss.getDataSet(code);
        dataSet.listFiles("/", true);
    }

    private IDssComponent createDssComponent(String userName)
    {
        return DssComponentFactory.tryCreate(userName, "a", OPENBIS_URL,
                5 * DateUtils.MILLIS_PER_MINUTE);
    }

    private NewDataSetDTO createNewDataSetDTO(File exampleDataSet) throws IOException
    {
        DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, "CISD:/CISD/CP-TEST-1");
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

    private SimpleDataSetInformationDTO getCodeOfLatestDataSet()
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> dataSets = openBISService.listDataSets();
        Collections.sort(dataSets, new Comparator<SimpleDataSetInformationDTO>()
            {
                public int compare(SimpleDataSetInformationDTO d1, SimpleDataSetInformationDTO d2)
                {
                    return d2.getDataSetCode().compareTo(d1.getDataSetCode());
                }
            });
        return dataSets.get(0);
    }

    private void checkDataSet(IDataSetDss dataSet) throws IOException
    {
        assertEquals("hello world", getContent(dataSet, "data.log"));
        assertEquals("1 2 3", getContent(dataSet, "data/1.data"));
        assertEquals("4 5 6 7", getContent(dataSet, "data/2.data"));
    }

    private String getContent(IDataSetDss dataSet, String path) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try
        {
            inputStream = dataSet.getFile("/original/my-data/" + path);
            IOUtils.copy(inputStream, output);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
        return output.toString();
    }

    private File getRegistrationLogDir()
    {
        return DssPropertyParametersUtil.getDssRegistrationLogDir(new Properties());
    }
}
