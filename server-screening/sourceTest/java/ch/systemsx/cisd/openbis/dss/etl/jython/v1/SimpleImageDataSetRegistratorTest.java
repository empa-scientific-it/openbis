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

package ch.systemsx.cisd.openbis.dss.etl.jython.v1;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageContainerDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.IntensityRangeImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.jython.v1.SimpleImageDataSetRegistrator.IImageReaderFactory;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { SimpleImageDataSetRegistrator.class, SimpleImageDataSetRegistrator.IImageReaderFactory.class })
public class SimpleImageDataSetRegistratorTest extends AbstractFileSystemTestCase
{
    private static final File TEST_DATA_FOLDER = new File(
            "resource/test-data/SimpleImageDataSetRegistratorTest");

    private static final class MyConfigData extends SimpleImageContainerDataConfig
    {
        String channelCodes = "";

        @Override
        public ChannelColor getChannelColor(String channelCode)
        {
            channelCodes += channelCode;
            return ChannelColor.createFromIndex(channelCodes.length());
        }

    }

    private Mockery context;

    private IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory;

    private File incoming;

    private File imageFolder;

    private IImageReaderFactory readerFactory;

    private IImageReader reader;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        factory = context.mock(IDataSetRegistrationDetailsFactory.class);
        readerFactory = context.mock(IImageReaderFactory.class);
        reader = context.mock(IImageReader.class);
        incoming = new File(workingDirectory, "incoming");
        incoming.mkdirs();
        imageFolder = new File(incoming, "images");
        imageFolder.mkdirs();
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final File f1 = copyExampleFile("pond.tif");
        final DataSetRegistrationDetails<ImageDataSetInformation> details =
                new DataSetRegistrationDetails<ImageDataSetInformation>();
        details.setDataSetInformation(new ImageDataSetInformation());
        context.checking(new Expectations()
            {
                {
                    one(factory).createDataSetRegistrationDetails();
                    will(returnValue(details));

                    one(readerFactory).tryGetReaderForFile("MyLibrary", f1.getPath());
                    will(returnValue(reader));

                    one(reader).getName();
                    will(returnValue("MyReader"));

                    one(reader).getImageIDs(f1);
                    will(returnValue(Arrays.asList(ImageID.parse("0-1-2-3"),
                            ImageID.parse("1-2-3-9"), ImageID.parse("2-8-9-3"))));
                }
            });
        MyConfigData configData = new MyConfigData();
        configData.setMicroscopyData(true);
        configData.setPlate("space", "plate1");
        configData.setImageLibrary("MyLibrary");
        DataSetRegistrationDetails<ImageDataSetInformation> actualDetails =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(configData, incoming,
                        factory, readerFactory);

        assertSame(details, actualDetails);
        List<ImageFileInfo> images =
                actualDetails.getDataSetInformation().getImageDataSetStructure().getImages();
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=1.0, depth=2.0, seriesNumber=1]", images.get(0)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-10, "
                + "path=images/pond.tif, timepoint=2.0, depth=3.0, seriesNumber=2]", images.get(1)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=8.0, depth=9.0, seriesNumber=3]", images.get(2)
                .toString());
        assertEquals(3, images.size());
        assertEquals("CHANNEL-4CHANNEL-10", configData.channelCodes);
        context.assertIsSatisfied();
    }

    private File copyExampleFile(String fileName)
    {
        File destinationFile = new File(imageFolder, fileName);
        FileOperations.getInstance()
                .copyFile(new File(TEST_DATA_FOLDER, fileName), destinationFile);
        return destinationFile;
    }

    @Test
    public void testCommonFixedLevels()
    {
        final File f1 = copyExampleFile("pond.tif");
        final DataSetRegistrationDetails<ImageDataSetInformation> details =
                new DataSetRegistrationDetails<ImageDataSetInformation>();
        details.setDataSetInformation(new ImageDataSetInformation());
        context.checking(new Expectations()
            {
                {
                    one(factory).createDataSetRegistrationDetails();
                    will(returnValue(details));

                    one(readerFactory).tryGetReaderForFile("MyLibrary", f1.getPath());
                    will(returnValue(reader));

                    one(reader).getName();
                    will(returnValue("MyReader"));

                    one(reader).getImageIDs(f1);
                    will(returnValue(Arrays.asList(ImageID.parse("0-1-2-3"),
                            ImageID.parse("1-2-3-9"), ImageID.parse("2-8-9-3"))));
                }
            });
        MyConfigData configData = new MyConfigData();
        configData.setMicroscopyData(true);
        configData.setPlate("space", "plate1");
        configData.setImageLibrary("MyLibrary");
        assertFalse(configData.isFixedIntensityRangeForAllImagesDefined());
        configData.setDefaultFixedIntensityRangeForAllImages(99, 1001);
        assertTrue(configData.isFixedIntensityRangeForAllImagesDefined());
        DataSetRegistrationDetails<ImageDataSetInformation> actualDetails =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(configData, incoming,
                        factory, readerFactory);

        assertSame(details, actualDetails);
        List<ImageFileInfo> images =
                actualDetails.getDataSetInformation().getImageDataSetStructure().getImages();
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=1.0, depth=2.0, seriesNumber=1]", images.get(0)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-10, "
                + "path=images/pond.tif, timepoint=2.0, depth=3.0, seriesNumber=2]", images.get(1)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=8.0, depth=9.0, seriesNumber=3]", images.get(2)
                .toString());
        assertEquals(3, images.size());
        assertEquals("CHANNEL-4CHANNEL-10", configData.channelCodes);
        final List<Channel> channels = actualDetails.getDataSetInformation().getChannels();
        assertEquals(2, channels.size());
        final Channel channel1 = channels.get(0);
        final Channel channel2 = channels.get(1);
        assertEquals(1, channel1.getAvailableTransformations().length);
        assertTrue(channel1.getAvailableTransformations()[0].getImageTransformerFactory() instanceof IntensityRangeImageTransformerFactory);
        IntensityRangeImageTransformerFactory transformationFactory =
                (IntensityRangeImageTransformerFactory) channel1.getAvailableTransformations()[0]
                        .getImageTransformerFactory();
        assertEquals(99, transformationFactory.getBlackPointIntensity());
        assertEquals(1001, transformationFactory.getWhitePointIntensity());
        assertEquals(1, channel2.getAvailableTransformations().length);
        assertTrue(channel2.getAvailableTransformations()[0].getImageTransformerFactory() instanceof IntensityRangeImageTransformerFactory);
        transformationFactory =
                (IntensityRangeImageTransformerFactory) channel2.getAvailableTransformations()[0]
                        .getImageTransformerFactory();
        assertEquals(99, transformationFactory.getBlackPointIntensity());
        assertEquals(1001, transformationFactory.getWhitePointIntensity());
        context.assertIsSatisfied();
    }

    @Test
    public void testDifferentFixedLevels()
    {
        final File f1 = copyExampleFile("pond.tif");
        final DataSetRegistrationDetails<ImageDataSetInformation> details =
                new DataSetRegistrationDetails<ImageDataSetInformation>();
        details.setDataSetInformation(new ImageDataSetInformation());
        context.checking(new Expectations()
            {
                {
                    one(factory).createDataSetRegistrationDetails();
                    will(returnValue(details));

                    one(readerFactory).tryGetReaderForFile("MyLibrary", f1.getPath());
                    will(returnValue(reader));

                    one(reader).getName();
                    will(returnValue("MyReader"));

                    one(reader).getImageIDs(f1);
                    will(returnValue(Arrays.asList(ImageID.parse("0-1-2-3"),
                            ImageID.parse("1-2-3-9"), ImageID.parse("2-8-9-3"))));
                }
            });
        MyConfigData configData = new MyConfigData();
        configData.setMicroscopyData(true);
        configData.setPlate("space", "plate1");
        configData.setImageLibrary("MyLibrary");
        assertFalse(configData.isFixedIntensityRangeForAllImagesDefined());
        configData.setDefaultFixedIntensityRangeForAllImages(99, 1001);
        configData.addFixedIntensityRangeForAllImages("channel-4", 51, 1017);
        assertTrue(configData.isFixedIntensityRangeForAllImagesDefined());
        DataSetRegistrationDetails<ImageDataSetInformation> actualDetails =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(configData, incoming,
                        factory, readerFactory);

        assertSame(details, actualDetails);
        List<ImageFileInfo> images =
                actualDetails.getDataSetInformation().getImageDataSetStructure().getImages();
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=1.0, depth=2.0, seriesNumber=1]", images.get(0)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-10, "
                + "path=images/pond.tif, timepoint=2.0, depth=3.0, seriesNumber=2]", images.get(1)
                .toString());
        assertEquals("ImageFileInfo [well=null, tile=[x=1,y=1], channel=CHANNEL-4, "
                + "path=images/pond.tif, timepoint=8.0, depth=9.0, seriesNumber=3]", images.get(2)
                .toString());
        assertEquals(3, images.size());
        assertEquals("CHANNEL-4CHANNEL-10", configData.channelCodes);
        final List<Channel> channels = actualDetails.getDataSetInformation().getChannels();
        assertEquals(2, channels.size());
        final Channel channel1 = channels.get(0);
        final Channel channel2 = channels.get(1);
        assertEquals("CHANNEL-4", channel1.getCode());
        assertEquals(1, channel1.getAvailableTransformations().length);
        assertTrue(channel1.getAvailableTransformations()[0].getImageTransformerFactory() instanceof IntensityRangeImageTransformerFactory);
        IntensityRangeImageTransformerFactory transformationFactory =
                (IntensityRangeImageTransformerFactory) channel1.getAvailableTransformations()[0]
                        .getImageTransformerFactory();
        assertEquals(51, transformationFactory.getBlackPointIntensity());
        assertEquals(1017, transformationFactory.getWhitePointIntensity());
        assertEquals("CHANNEL-10", channel2.getCode());
        assertEquals(1, channel2.getAvailableTransformations().length);
        assertTrue(channel2.getAvailableTransformations()[0].getImageTransformerFactory() instanceof IntensityRangeImageTransformerFactory);
        transformationFactory =
                (IntensityRangeImageTransformerFactory) channel2.getAvailableTransformations()[0]
                        .getImageTransformerFactory();
        assertEquals(99, transformationFactory.getBlackPointIntensity());
        assertEquals(1001, transformationFactory.getWhitePointIntensity());
        context.assertIsSatisfied();
    }

}
