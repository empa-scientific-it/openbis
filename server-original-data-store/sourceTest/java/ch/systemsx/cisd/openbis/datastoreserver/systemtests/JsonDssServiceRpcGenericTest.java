/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * Verifies that the functionality of {@link IDssServiceRpcGeneric} is accessible over JSON-RPC.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups = { "slow" })
public class JsonDssServiceRpcGenericTest extends SystemTestCase
{
    private static final String DATA_SET_CODE = "20081105092159111-1";

    private static final String OPENBIS_URL = TestInstanceHostUtils.getOpenBISUrl()
            + IGeneralInformationService.JSON_SERVICE_URL;

    // TODO KE: put the suffix in a constant
    private static final String DSS_URL = TestInstanceHostUtils.getDSSUrl()
            + "/datastore_server/rmi-dss-api-v1.json";

    private IGeneralInformationService openbisService;

    private IDssServiceRpcGeneric dssRpcService;

    private String sessionToken;

    @BeforeClass
    public void beforeClass() throws IOException
    {
        openbisService = createOpenbisService();
        dssRpcService = createDssRpcService();

        sessionToken = openbisService.tryToAuthenticateForAllServices("test", "1");

        File resourceDir =
                new File("../datastore_server/resource/test-data/" + getClass().getSimpleName());
        FileUtils.copyDirectory(resourceDir, rootDir);
    }

    @AfterClass
    public void afterClass()
    {
        openbisService.logout(sessionToken);
    }

    @Test
    public void testListDataSetContents()
    {

        String validationScript = dssRpcService.getValidationScript(sessionToken, "HCS_IMAGE");
        System.out.println(validationScript);

        FileInfoDssDTO[] result =
                dssRpcService.listFilesForDataSet(sessionToken, DATA_SET_CODE, "", true);

        for (FileInfoDssDTO fileInfo : result)
        {
            System.out.println(fileInfo);
        }
    }

    @Test
    public void testGetDataSetContentsWithURL() throws MalformedURLException, IOException
    {

        FileInfoDssDTO fileInfoToDownload = getFileInfoToDownload();

        DataSetFileDTO fileToDownload =
                new DataSetFileDTO(DATA_SET_CODE, fileInfoToDownload.getPathInDataSet(), false);
        String url =
                dssRpcService.getDownloadUrlForFileForDataSetWithTimeout(sessionToken,
                        fileToDownload, -1);

        // Download the data into a file
        InputStream input = new URL(url).openStream();
        File file = new File(workingDirectory, "output");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copyLarge(input, output);
        assertEquals(file.length(), fileInfoToDownload.getFileSize());
    }

    protected FileInfoDssDTO getFileInfoToDownload()
    {
        FileInfoDssDTO[] result =
                dssRpcService.listFilesForDataSet(sessionToken, DATA_SET_CODE, "", true);
        assertTrue("Did not find any files for the data set " + DATA_SET_CODE, result.length > 0);

        FileInfoDssDTO fileInfoToDownload = null;
        for (FileInfoDssDTO fileInfo : result)
        {
            if (false == fileInfo.isDirectory())
            {
                fileInfoToDownload = fileInfo;
                break;
            }
        }

        assertNotNull("Could not find a file in the data set 20081105092159111-1 to download",
                fileInfoToDownload);
        return fileInfoToDownload;
    }

    @Test
    public void testGetDataSetContentsWithExpiredURL() throws MalformedURLException, IOException,
            InterruptedException
    {

        FileInfoDssDTO fileInfoToDownload = getFileInfoToDownload();

        DataSetFileDTO fileToDownload =
                new DataSetFileDTO(DATA_SET_CODE, fileInfoToDownload.getPathInDataSet(), false);
        String url =
                dssRpcService.getDownloadUrlForFileForDataSetWithTimeout(sessionToken,
                        fileToDownload, 5);

        // Wait until the URL is invalid
        Thread.sleep(6 * 1000L);

        // Download the data into a file
        try
        {
            new URL(url).openStream();
            fail("An exception should have been thrown.");
        } catch (IOException e)
        {
            assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 500"));
        }
    }

    public static IGeneralInformationService createOpenbisService()
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new GenericObjectMapper(), new URL(OPENBIS_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createClientProxy(JsonDssServiceRpcGenericTest.class.getClassLoader(),
                    IGeneralInformationService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    public static IDssServiceRpcGeneric createDssRpcService()
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new GenericObjectMapper(), new URL(DSS_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createClientProxy(JsonDssServiceRpcGenericTest.class.getClassLoader(),
                    IDssServiceRpcGeneric.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

}
