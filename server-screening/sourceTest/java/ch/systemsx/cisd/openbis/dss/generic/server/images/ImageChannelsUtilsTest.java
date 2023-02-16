/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReadersTestHelper;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageTransformationParams;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtilTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { ImageLoadingHelper.class, ImageChannelsUtils.class })
public class ImageChannelsUtilsTest extends AssertJUnit
{
    public static final File TEST_IMAGE_FOLDER = new File("../server-screening/sourceTest/java/"
            + ImageChannelsUtilsTest.class.getPackage().getName().replace('.', '/'));

    private static final String DATASET_CODE = "dataset-123";

    private static final String CHANNEL = "GFP";

    private static final IImageTransformer TRANSFORMER = new IImageTransformer()
    {
        @Override
        public BufferedImage transform(BufferedImage image)
        {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height; y++)
                {
                    int rgb = image.getRGB(x, y);
                    System.out.println(x + " " + y + " " + Integer.toHexString(rgb));
                    output.setRGB(x, y, (rgb & 0xff) << 16);
                }
            }
            return output;
        }
    };

    private Mockery context;

    private IImagingDatasetLoader loader;

    private IImageTransformerFactory transformerFactory;

    @BeforeMethod
    public void setUp() throws Exception
    {
        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.IMAGEIO_LIBRARY,
                ImageReaderConstants.BIOFORMATS_LIBRARY, ImageReaderConstants.IMAGEJ_LIBRARY);
        context = new Mockery();
        loader = context.mock(IImagingDatasetLoader.class);
        transformerFactory = context.mock(IImageTransformerFactory.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUnkownImage()
    {
        final DatasetAcquiredImagesReference imageRef = createDatasetAcquiredImagesReference();
        prepareExpectations(null, imageRef);

        try
        {
            createImage(imageRef, createSingleChannelTransformationParams(), null);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("No image found for channel stack StackReference{id=4711} "
                    + "and channel GFP", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private ImageTransformationParams createSingleChannelTransformationParams()
    {
        return new ImageTransformationParams(true, false, null, new HashMap<String, String>());
    }

    @Test
    public void testGetTiffImage()
    {
        AbsoluteImageReference absoluteImageReference =
                createAbsoluteImageReference("img1.tiff", RequestedImageSize.createOriginal());
        performTest(absoluteImageReference, getImageContentDescription(image("img1.png")), null);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetJpgImageAsPngThumbnail()
    {
        Size size = new Size(4, 2);
        AbsoluteImageReference absoluteImageReference =
                createAbsoluteImageReference("img1.jpg", new RequestedImageSize(size, false));
        performTest(absoluteImageReference, "9bc def\ndef cde\n", size);
        context.assertIsSatisfied();
    }

    private void performTest(final AbsoluteImageReference absoluteImageReference,
            String expectedImageContentDescription, Size thumbnailSizeOrNull)
    {
        final DatasetAcquiredImagesReference imageRef = createDatasetAcquiredImagesReference();
        prepareExpectations(absoluteImageReference, imageRef);

        ImageTransformationParams transformationParams = createSingleChannelTransformationParams();
        BufferedImage image =
                createImage(imageRef, transformationParams, thumbnailSizeOrNull);
        assertEquals(expectedImageContentDescription, getImageContentDescription(image));

        context.assertIsSatisfied();
    }

    private BufferedImage createImage(final DatasetAcquiredImagesReference imageRef,
            ImageTransformationParams transformationParams, Size thumbnailSizeOrNull)
    {
        ImageLoadingHelper imageHelper = new ImageLoadingHelper(
                ImagingLoaderStrategyFactory.createImageLoaderStrategy(loader),
                new RequestedImageSize(thumbnailSizeOrNull, false), null);
        boolean mergeAllChannels = imageHelper.isMergeAllChannels(imageRef);
        List<AbsoluteImageReference> imageContents =
                imageHelper.fetchImageContents(imageRef, mergeAllChannels, false,
                        transformationParams);
        return ImageChannelsUtils.calculateBufferedImage(imageContents, transformationParams);
    }

    private void prepareExpectations(final AbsoluteImageReference absoluteImageReferenceOrNull,
            final DatasetAcquiredImagesReference imageRef)
    {
        context.checking(new Expectations()
        {
            {
                allowing(loader).getImageParameters();
                ImageDatasetParameters imgParams = new ImageDatasetParameters();
                imgParams.setInternalChannels(Arrays.asList(new InternalImageChannel(CHANNEL,
                        CHANNEL, null, null, new ArrayList<InternalImageTransformationInfo>())));
                will(returnValue(imgParams));

                RequestedImageSize requestedSize =
                        absoluteImageReferenceOrNull == null ? RequestedImageSize
                                .createOriginal() : absoluteImageReferenceOrNull
                                .getRequestedSize();
                one(loader).tryGetImage(imageRef.getChannelCodes(null).get(0),
                        imageRef.getChannelStackReference(), requestedSize, null);
                will(returnValue(absoluteImageReferenceOrNull));
            }
        });
    }

    private DatasetAcquiredImagesReference createDatasetAcquiredImagesReference()
    {
        ImageChannelStackReference channelStackReference =
                ImageChannelStackReference.createFromId(4711);
        final DatasetAcquiredImagesReference imageRef =
                DatasetAcquiredImagesReference.createForSingleChannel(DATASET_CODE,
                        channelStackReference, CHANNEL);
        return imageRef;
    }

    @Test
    public void testGetTransformedImage()
    {
        final DatasetAcquiredImagesReference imageRef = createDatasetAcquiredImagesReference();
        AbsoluteImageReference imgRef =
                createAbsoluteImageReference("img1.gif", RequestedImageSize.createOriginal());
        String transformationCode = "MY_TRANSFORMATION";
        Map<String, IImageTransformerFactory> transformations =
                createImageTransformationsMap(transformationCode, transformerFactory);
        imgRef.getImageTransformationFactories().setForChannel(transformations);

        prepareExpectations(imgRef, imageRef);
        context.checking(new Expectations()
        {
            {
                one(transformerFactory).createTransformer();
                will(returnValue(TRANSFORMER));
            }
        });

        ImageTransformationParams transformationParams =
                new ImageTransformationParams(true, false, transformationCode,
                        new HashMap<String, String>());
        BufferedImage image = createImage(imageRef, transformationParams, null);
        assertEquals("0e0 0f0 0f0 0e0\n" + "0f0 0c0 0c0 0f0\n" + "0f0 0c0 0c0 0f0\n"
                + "0e0 0f0 0f0 0e0\n", getImageContentDescription(image));

        context.assertIsSatisfied();
    }

    private static Map<String, IImageTransformerFactory> createImageTransformationsMap(
            String transformationCode, IImageTransformerFactory transformerFactory)
    {
        Map<String, IImageTransformerFactory> transformations =
                new HashMap<String, IImageTransformerFactory>();
        transformations.put(transformationCode, transformerFactory);
        return transformations;
    }

    private static AbsoluteImageReference createAbsoluteImageReference(String fileName,
            RequestedImageSize imageSize)
    {
        return new AbsoluteImageReference(image(fileName), "id42", null, null, imageSize,
                new ChannelColorRGB(0, 255, 0), new ImageTransfomationFactories(), null, null,
                "ch2");
    }

    public void assertPNG(IHierarchicalContentNode image)
    {
        InputStream inputStream = image.getInputStream();
        try
        {
            byte[] signature = new byte[3];
            inputStream.read(); // skip first byte
            inputStream.read(signature);
            inputStream.close();
            assertEquals("PNG", new String(signature));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static IHierarchicalContentNode image(String fileName)
    {
        return new FileBasedContentNode(new File(TEST_IMAGE_FOLDER, fileName));
    }

    private String getImageContentDescription(IHierarchicalContentNode image)
    {
        BufferedImage bufferedImage = ImageUtilTest.loadImage(image);
        return getImageContentDescription(bufferedImage);
    }

    private String getImageContentDescription(BufferedImage bufferedImage)
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int pixel = bufferedImage.getRGB(i, j);
                if (i > 0)
                {
                    builder.append(' ');
                }
                String hexcode = Integer.toHexString(pixel).substring(2);
                builder.append(hexcode.charAt(0));
                builder.append(hexcode.charAt(2));
                builder.append(hexcode.charAt(4));
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
