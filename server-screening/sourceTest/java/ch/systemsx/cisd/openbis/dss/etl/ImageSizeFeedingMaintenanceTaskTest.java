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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ImageSizeFeedingMaintenanceTaskTest extends AssertJUnit
{
    private static final long CONTAINER_ID = 42L;

    private static final class MockAbsoluteImageReference extends AbsoluteImageReference
    {
        private final int width;

        private final int height;

        private final int colorDepth;

        public MockAbsoluteImageReference(int width, int height, int colorDepth)
        {
            super(null, null, null, null, new RequestedImageSize(null, false), null,
                    new ImageTransfomationFactories(), null, null, null);
            this.width = width;
            this.height = height;
            this.colorDepth = colorDepth;
        }

        @Override
        public Size getUnchangedImageSize()
        {
            if (width < 0)
            {
                throw new RuntimeException("Negative width: " + width);
            }
            return new Size(width, height);
        }

        @Override
        public Integer getColorDepth()
        {
            if (colorDepth < 0)
            {
                throw new RuntimeException("Negative color depth: " + colorDepth);
            }
            return colorDepth;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IImagingQueryDAO dao;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider contentProvider;

    private ImageSizeFeedingMaintenanceTask maintenanceTask;

    private IHierarchicalContent ds1Content;

    private IHierarchicalContent ds2Content;

    private IHierarchicalContent ds3Content;

    private IImagingDatasetLoader imageLoader1;

    private IImagingDatasetLoader imageLoader2;

    private IImagingDatasetLoader imageLoader3;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder();
        context = new Mockery();
        dao = context.mock(IImagingQueryDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        ds1Content = context.mock(IHierarchicalContent.class, "ds1");
        ds2Content = context.mock(IHierarchicalContent.class, "ds2");
        ds3Content = context.mock(IHierarchicalContent.class, "ds3");
        imageLoader1 = context.mock(IImagingDatasetLoader.class, "ds1-loader");
        imageLoader2 = context.mock(IImagingDatasetLoader.class, "ds2-loader");
        imageLoader3 = context.mock(IImagingDatasetLoader.class, "ds3-loader");
        final Map<String, IImagingDatasetLoader> loaderMap =
                new HashMap<String, IImagingDatasetLoader>();
        loaderMap.put("ds1", imageLoader1);
        loaderMap.put("ds2", imageLoader2);
        loaderMap.put("ds3", imageLoader3);
        maintenanceTask = new ImageSizeFeedingMaintenanceTask(dao, service, contentProvider)
            {
                @Override
                protected IImagingDatasetLoader createImageLoader(String dataSetCode,
                        IHierarchicalContent content)
                {
                    IImagingDatasetLoader loader = loaderMap.get(dataSetCode);
                    if (loader == null)
                    {
                        fail("No loader for data set " + dataSetCode);
                    }
                    return loader;
                }
            };
        assertEquals(true, maintenanceTask.requiresDataStoreLock());
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        context.assertIsSatisfied();
    }

    @Test
    public void testNonMatchingOrUnknownDataSets()
    {
        final SimpleDataSetInformationDTO ds0 = dataSet("ds0", "HCS_ANALYSIS");
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", "HCS_IMAGE_ANALYSIS");
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds0, ds1)));

                    one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds1.getDataSetCode());
                    will(returnValue(ds1Content));

                    one(dao).tryGetImageDatasetByPermId(ds1.getDataSetCode());
                    will(returnValue(null));

                    one(ds1Content).close();
                }
            });

        maintenanceTask.execute();

        assertEquals("Scan 2 data sets.\n"
                + "0 original image sizes and 0 thumbnail image sizes are added to the database.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testImageDataSets()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", "HCS_IMAGE_ALPHA");
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", "HCS_IMAGE_RAW");
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", "HCS_IMAGE");
        final RecordingMatcher<ImgImageZoomLevelDTO> zoomLevelRecorder =
                new RecordingMatcher<ImgImageZoomLevelDTO>();
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                }
            });
        prepareListZoomLevels(ds1, ds1Content, new ImgImageZoomLevelDTO("", true, "", 1, 2, null,
                null, 12));
        prepareListZoomLevels(ds2, ds2Content);
        prepareForTryFindAnyOriginal(ds2, imageLoader2, new MockAbsoluteImageReference(144, 89, 8));
        prepareForAddZoomLevel(zoomLevelRecorder);
        prepareForTryFindAnyThumbnail(ds2, imageLoader2, null);
        prepareForCommit();
        prepareListZoomLevels(ds3, ds3Content);
        prepareForTryFindAnyOriginal(ds3, imageLoader3, null);
        prepareForTryFindAnyThumbnail(ds3, imageLoader3, new MockAbsoluteImageReference(21, 34, 24));
        prepareForAddZoomLevel(zoomLevelRecorder);
        prepareForCommit();

        maintenanceTask.execute();

        List<ImgImageZoomLevelDTO> zoomLevels = zoomLevelRecorder.getRecordedObjects();
        assertEquals("Scan 3 data sets.\n" + "Original size 144x89 added for data set ds2\n"
                + "Thumbnail size 21x34 added for data set ds3\n"
                + "1 original image sizes and 1 thumbnail image sizes are added to the database.",
                logRecorder.getLogContent());
        assertEquals(
                "[ImgImageZoomLevelDTO{colorDepth=8,containerDatasetId=99715,fileType=<null>,"
                        + "height=89,isOriginal=true,physicalDatasetPermId=ds2,rootPath=,width=144,id=0}, "
                        + "ImgImageZoomLevelDTO{colorDepth=24,containerDatasetId=99716,fileType=<null>,"
                        + "height=34,isOriginal=false,physicalDatasetPermId=ds3,rootPath=,width=21,id=0}]",
                zoomLevels.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExceptionHandling()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", "HCS_IMAGE_ALPHA");
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", "HCS_IMAGE_RAW");
        final RecordingMatcher<ImgImageZoomLevelDTO> zoomLevelRecorder =
                new RecordingMatcher<ImgImageZoomLevelDTO>();
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2)));
                }
            });
        prepareListZoomLevels(ds1, ds1Content);
        prepareForTryFindAnyOriginal(ds1, imageLoader1, new MockAbsoluteImageReference(-1, 0, 8));
        prepareForRollback();
        prepareListZoomLevels(ds2, ds2Content);
        prepareForTryFindAnyOriginal(ds2, imageLoader2, new MockAbsoluteImageReference(1, 2, 16));
        prepareForAddZoomLevel(zoomLevelRecorder);
        prepareForTryFindAnyThumbnail(ds2, imageLoader2, new MockAbsoluteImageReference(-13, 0, 24));
        prepareForRollback();

        maintenanceTask.execute();

        List<ImgImageZoomLevelDTO> zoomLevels = zoomLevelRecorder.getRecordedObjects();
        assertEquals("Scan 2 data sets.\n" + "2 exceptions occured:\n"
                + "Data set ds1: java.lang.RuntimeException: Negative width: -1\n"
                + "Data set ds2: java.lang.RuntimeException: Negative width: -13\n"
                + "0 original image sizes and 0 thumbnail image sizes are added to the database.",
                logRecorder.getLogContent());
        assertEquals(
                "[ImgImageZoomLevelDTO{colorDepth=16,containerDatasetId=99715,fileType=<null>,"
                        + "height=2,isOriginal=true,physicalDatasetPermId=ds2,rootPath=,width=1,id=0}]",
                zoomLevels.toString());
        context.assertIsSatisfied();
    }

    private void prepareListZoomLevels(final SimpleDataSetInformationDTO dataSet,
            final IHierarchicalContent content, final ImgImageZoomLevelDTO... zoomLevels)
    {
        context.checking(new Expectations()
            {
                {
                    String dataSetCode = dataSet.getDataSetCode();
                    one(contentProvider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(returnValue(content));

                    one(dao).tryGetImageDatasetByPermId(dataSetCode);
                    ImgImageDatasetDTO imageDataSet =
                            new ImgImageDatasetDTO(dataSetCode, null, null, CONTAINER_ID, false,
                                    null, null);
                    imageDataSet.setId(dataSetCode.hashCode());
                    will(returnValue(imageDataSet));

                    one(dao).listImageZoomLevels(imageDataSet.getId());
                    will(returnValue(Arrays.asList(zoomLevels)));

                    one(content).close();
                }
            });
    }

    private void prepareForTryFindAnyOriginal(final SimpleDataSetInformationDTO dataSet,
            final IImagingDatasetLoader loader, final AbsoluteImageReference originalImageOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(loader).tryFindAnyOriginalImage();
                    will(returnValue(originalImageOrNull));
                }
            });
    }

    private void prepareForTryFindAnyThumbnail(final SimpleDataSetInformationDTO dataSet,
            final IImagingDatasetLoader loader, final AbsoluteImageReference thumbnailOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(loader).tryFindAnyThumbnail();
                    will(returnValue(thumbnailOrNull));
                }
            });
    }

    private void prepareForAddZoomLevel(
            final RecordingMatcher<ImgImageZoomLevelDTO> zoomLevelRecorder)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).addImageZoomLevel(with(zoomLevelRecorder));
                }
            });
    }

    private void prepareForCommit()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).commit();
                }
            });
    }

    private void prepareForRollback()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).rollback();
                }
            });
    }

    private SimpleDataSetInformationDTO dataSet(String code, String type)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(code);
        dataSet.setDataSetType(type);
        return dataSet;
    }
}
