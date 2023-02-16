/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.screening.systemtests;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IPlateImageHandler;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.json.ScreeningObjectMapper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = { "slow", "systemtest" })
public class TransformedImageRepresentationsTest extends AbstractScreeningSystemTestCase
{
    private IDssServiceRpcScreening screeningJsonApi;

    @BeforeTest
    public void dropAnExampleDataSet() throws IOException, Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported(FINISHED_POST_REGISTRATION_CONDITION);
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        JsonRpcHttpClient client =
                new JsonRpcHttpClient(new ScreeningObjectMapper(), new URL(
                        TestInstanceHostUtils.getDSSUrl()
                                + "/rmi-datastore-server-screening-api-v1.json/"),
                        new HashMap<String, String>());
        screeningJsonApi =
                ProxyUtil.createClientProxy(this.getClass().getClassLoader(),
                        IDssServiceRpcScreening.class, client);
    }

    @AfterMethod
    public void tearDown()
    {
        File[] files = getIncomingDirectory().listFiles();
        for (File file : files)
        {
            FileUtilities.deleteRecursively(file);
        }
    }

    private byte[] expectedThumbnailBytes(String dataSetCode, int size) throws IOException
    {
        IDataSetDss ds = screeningFacade.getDataSet(dataSetCode);
        return getImageBaseBytes(ds, "thumbnails_" + size + "x" + size
                + "__CONVERT_2.h5ar/wA1_d1-1_cCy5.jpg");
    }

    /**
     * get the base64 encoded data of the image taken directly from the file system.
     */
    private byte[] getImageBaseBytes(IDataSetDss ds, String pathInDataSet) throws IOException
    {
        return IOUtils.toByteArray(ds.getFile(pathInDataSet));
    }

    @Test
    public void testTransformedThumbnails() throws Exception
    {
        // The components of the plate identifier come from the dropbox code
        // (resource/test-data/TransformedImageRepresentationsTest/data-set-handler.py)
        PlateIdentifier plate = new PlateIdentifier("TRANSFORMED-THUMB-PLATE", "TEST", null);
        List<ImageDatasetReference> imageDataSets =
                screeningFacade.listRawImageDatasets(Arrays.asList(plate));
        List<DatasetImageRepresentationFormats> representationFormats =
                screeningFacade.listAvailableImageRepresentationFormats(imageDataSets);
        assertEquals(1, representationFormats.size());
        List<ImageRepresentationFormat> formats =
                representationFormats.get(0).getImageRepresentationFormats();

        List<PlateImageReference> plateRefs =
                screeningJsonApi.listPlateImageReferences(sessionToken, imageDataSets.get(0),
                        Arrays.asList(new WellPosition(1, 1)), "Cy5");

        HashSet<Dimension> expectedResolutions = new HashSet<Dimension>();
        expectedResolutions.addAll(Arrays.asList(new Dimension(64, 64), new Dimension(128, 128),
                new Dimension(256, 256), new Dimension(512, 512)));

        String dataSetCode = imageDataSets.get(0).getDatasetCode();

        for (ImageRepresentationFormat format : formats)
        {
            if (false == format.isOriginal())
            {
                final List<byte[]> thumbnails = new ArrayList<byte[]>(1);
                screeningFacade.loadPhysicalThumbnails(plateRefs, format, new IPlateImageHandler()
                {

                    @Override
                    public void handlePlateImage(PlateImageReference plateImageReference,
                            byte[] imageFileBytes)
                    {
                        thumbnails.add(imageFileBytes);
                    }

                });
                byte[] expectedThumbnailImage =
                        expectedThumbnailBytes(dataSetCode, format.getWidth());
                assertEquals(1, thumbnails.size());
                assertEquals(expectedThumbnailImage, thumbnails.get(0));
            } else
            {
                final List<byte[]> images = new ArrayList<byte[]>();
                screeningFacade.loadImages(plateRefs, false, new IPlateImageHandler()
                {

                    @Override
                    public void handlePlateImage(PlateImageReference plateImageReference,
                            byte[] imageFileBytes)
                    {
                        images.add(imageFileBytes);
                    }
                });
                assertEquals(1, images.size());

                assertTrue(images.get(0).length > 0);
            }
            if (format.getFileType() != null)
            {
                // jpg thumbnails
                assertEquals(Integer.valueOf(32), format.getColorDepth());
            } else
            {
                // original image
                assertEquals(Integer.valueOf(8), format.getColorDepth());
            }

            Dimension resolution = new Dimension(format.getWidth(), format.getHeight());
            // Make sure the resolution we specified was found
            assertTrue("" + resolution + " was not expected",
                    expectedResolutions.remove(resolution));
        }
        assertEquals(0, expectedResolutions.size());

        // Check that the representations are JPEG for the following resolutions: 64x64, 128x128,
        // 256x256
        HashSet<Dimension> jpegResolutions = new HashSet<Dimension>();
        jpegResolutions.addAll(Arrays.asList(new Dimension(64, 64), new Dimension(128, 128),
                new Dimension(256, 256)));
        for (ImageRepresentationFormat format : formats)
        {
            Dimension resolution = new Dimension(format.getWidth(), format.getHeight());
            if (jpegResolutions.contains(resolution))
            {
                assertEquals("jpg", format.getFileType().toLowerCase());
                assertEquals(3, format.getTransformations().size());
            }
        }
    }

    private File createTestDataContents() throws IOException
    {
        File dest = new File(workingDirectory, "test-data");
        dest.mkdirs();
        File src = new File(getTestDataFolder(), "TRANSFORMED-THUMB-PLATE");

        // Copy the test data set to the location for processing
        FileUtils.copyDirectory(src, dest);
        return dest;
    }

    private String getTestDataFolder()
    {
        return "../server-screening/resource/test-data/TransformedImageRepresentationsTest/";
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }

}
