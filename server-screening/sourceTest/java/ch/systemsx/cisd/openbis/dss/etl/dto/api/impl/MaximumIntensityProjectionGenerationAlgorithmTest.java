/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.image.ImageHistogram;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.IImageProvider;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = MaximumIntensityProjectionGenerationAlgorithm.class)
public class MaximumIntensityProjectionGenerationAlgorithmTest extends AbstractFileSystemTestCase
{
    private static final class ImageBuilder
    {
        private BufferedImage image;

        private Graphics graphics;

        ImageBuilder(int imageType)
        {
            image = new BufferedImage(10, 6, BufferedImage.TYPE_INT_RGB);
            graphics = image.getGraphics();
        }

        BufferedImage getImage()
        {
            return image;
        }

        ImageBuilder rect(int x, int y, int width, int height, Color color)
        {
            graphics.setColor(color);
            graphics.fillRect(x, y, width, height);
            return this;
        }
    }

    private static final class MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading
            extends MaximumIntensityProjectionGenerationAlgorithm
    {
        private static final long serialVersionUID = 1L;

        private final List<String> recorder = new ArrayList<String>();

        private final Map<String, BufferedImage> images;

        private final Set<String> imagesToIgnore;

        public MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading(String dataSetTypeCode,
                int width, int height, String filename, Map<String, BufferedImage> images, String... imagesToIgnore)
        {
            super(dataSetTypeCode, width, height, filename);
            this.images = images;
            this.imagesToIgnore = new HashSet<String>(Arrays.asList(imagesToIgnore));
        }

        @Override
        BufferedImage loadImage(IImageProvider imageProvider, File incomingDirectory, String imagePath, String identifier, ImageLibraryInfo library)
        {
            assertSame(DUMMY_IMAGE_PROVIDER, imageProvider);
            recorder.add(incomingDirectory.getName() + "/" + imagePath + ": " + identifier + " [" + library + "]");
            BufferedImage bufferedImage = images.get(identifier);
            assertNotNull("image " + identifier, bufferedImage);
            return bufferedImage;
        }

        @Override
        protected boolean imageToBeIgnored(ImageFileInfo image)
        {
            return imagesToIgnore.contains(image.tryGetUniqueStringIdentifier());
        }
    }

    private static final IImageProvider DUMMY_IMAGE_PROVIDER = new IImageProvider()
        {
            @Override
            public BufferedImage getImage(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
            {
                return null;
            }

            @Override
            public Size getImageSize(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
            {
                return null;
            }

            @Override
            public int getImageColorDepth(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
            {
                return 0;
            }

            @Override
            public List<ImageIdentifier> getImageIdentifiers(IImageReader imageReaderOrNull, File file)
            {
                return null;
            }
        };

    private Map<String, BufferedImage> images;

    private ImageDataSetInformation information;

    private ImageDataSetStructure structure;

    @BeforeMethod
    public void createImages()
    {
        images = new LinkedHashMap<String, BufferedImage>();
        images.put("i1", new ImageBuilder(BufferedImage.TYPE_INT_RGB)
                .rect(1, 0, 3, 2, new Color(255, 128, 128))
                .rect(5, 4, 3, 2, new Color(80, 255, 128)).getImage());
        images.put("i2", new ImageBuilder(BufferedImage.TYPE_USHORT_GRAY)
                .rect(2, 1, 5, 4, new Color(100, 100, 100)).getImage());
        images.put("i3", new ImageBuilder(BufferedImage.TYPE_USHORT_GRAY)
                .rect(3, 2, 5, 4, new Color(30, 30, 30)).getImage());
        images.put("i4", new ImageBuilder(BufferedImage.TYPE_INT_RGB)
                .rect(0, 0, 10, 6, new Color(0, 0, 0)).getImage());
        information = new ImageDataSetInformation();
        information.setIncomingDirectory(new File(workingDirectory, "incoming"));
        structure = new ImageDataSetStructure();
        information.setImageDataSetStructure(structure);
        ImageStorageConfiguraton imageStorageConfiguraton = new ImageStorageConfiguraton();
        imageStorageConfiguraton.setImageLibrary(new ImageLibraryInfo("lib", "reader"));
        structure.setImageStorageConfiguraton(imageStorageConfiguraton);
        Set<String> keySet = images.keySet();
        List<ImageFileInfo> imageFileInfos = new ArrayList<ImageFileInfo>();
        for (String key : keySet)
        {
            ImageFileInfo fileInfo = new ImageFileInfo("C" + key.toUpperCase(), 1, 1, "images/" + key + ".png");
            fileInfo.setUniqueImageIdentifier(key);
            imageFileInfos.add(fileInfo);
        }
        structure.setImages(imageFileInfos);
    }

    @Test
    public void test8BitColorImageAnd16BitGrayImage()
    {

        MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading generationAlgorithm =
                new MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading("ABC", 10, 6, "r.png", images, "i3", "i4");
        assertEquals("ABC", generationAlgorithm.getDataSetTypeCode());
        assertEquals("r.png", generationAlgorithm.getImageFileName(0));
        Channel channel1 = new Channel("CI1", "i1", new ChannelColorRGB(200, 100, 80));
        Channel channel2 = new Channel("CI2", "i2", new ChannelColorRGB(0, 120, 180));
        structure.setChannels(Arrays.asList(channel1, channel2));

        List<BufferedImage> generatedImages = generationAlgorithm.generateImages(information, null, DUMMY_IMAGE_PROVIDER);

        assertEquals("[incoming/images/i1.png: i1 [lib (reader: reader)], "
                + "incoming/images/i2.png: i2 [lib (reader: reader)]]", generationAlgorithm.recorder.toString());

        ImageHistogram histogram = ImageHistogram.calculateHistogram(generatedImages.get(0));
        assertEquals("[0=48, 80=6, 255=6]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=32, 47=16, 128=6, 255=6]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=32, 71=16, 128=12]", renderHistogram(histogram.getBlueHistogram()));
        assertEquals(1, generatedImages.size());
    }

    @Test
    public void testTwo16BitGrayImages()
    {

        MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading generationAlgorithm =
                new MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading("ABC", 10, 6, "r.png", images, "i1", "i4");
        Channel channel1 = new Channel("CI2", "i2", new ChannelColorRGB(200, 100, 80));
        Channel channel2 = new Channel("CI3", "i3", new ChannelColorRGB(0, 120, 180));
        structure.setChannels(Arrays.asList(channel1, channel2));

        List<BufferedImage> generatedImages = generationAlgorithm.generateImages(information, null, DUMMY_IMAGE_PROVIDER);

        assertEquals("[incoming/images/i2.png: i2 [lib (reader: reader)], "
                + "incoming/images/i3.png: i3 [lib (reader: reader)]]", generationAlgorithm.recorder.toString());

        ImageHistogram histogram = ImageHistogram.calculateHistogram(generatedImages.get(0));
        assertEquals("[0=40, 216=20]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=32, 39=8, 108=20]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=32, 58=8, 86=20]", renderHistogram(histogram.getBlueHistogram()));
        assertEquals(1, generatedImages.size());
    }

    @Test
    public void testOneBlackImage()
    {
        MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading generationAlgorithm =
                new MaximumIntensityProjectionGenerationAlgorithmWithMockedLoading("ABC", 10, 6, "r.png", images, "i1", "i2", "i3");
        Channel channel1 = new Channel("CI4", "i4", new ChannelColorRGB(0, 0, 0));
        structure.setChannels(Arrays.asList(channel1));
        
        List<BufferedImage> generatedImages = generationAlgorithm.generateImages(information, null, DUMMY_IMAGE_PROVIDER);
        
        assertEquals("[incoming/images/i4.png: i4 [lib (reader: reader)]]", generationAlgorithm.recorder.toString());
        
        ImageHistogram histogram = ImageHistogram.calculateHistogram(generatedImages.get(0));
        assertEquals("[0=60]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=60]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=60]", renderHistogram(histogram.getBlueHistogram()));
        assertEquals(1, generatedImages.size());
    }
    
    private String renderHistogram(int[] histogram)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < histogram.length; i++)
        {
            int value = histogram[i];
            if (value > 0)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(i).append("=").append(value);
            }
        }
        return "[" + builder.toString() + "]";
    }
}
