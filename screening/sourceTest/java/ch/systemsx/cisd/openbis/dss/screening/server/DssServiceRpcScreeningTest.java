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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.SerializationUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.DatasetSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor.DssServiceRpcAuthorizationMethodInterceptor;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtilsTest;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtilTest;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;

/**
 * Test cases for the {@link DssServiceRpcScreening}.
 * 
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcScreeningTest extends AssertJUnit
{
    private static final class ImageTransformerFactory implements IImageTransformerFactory
    {
        private static final long serialVersionUID = 1L;

        private static int counter;

        private int id = counter++;

        public IImageTransformer createTransformer()
        {
            return null;
        }
    }

    private static final String DATASET_CODE = "ds1";

    private static final String CHANNEL_CODE = "GFP";

    private static final String EXPERIMENT_PERM_ID = "exp-123";

    private static final long EXPERIMENT_ID = 333;

    private static final String URL1 = "url1";

    private static final String SESSION_TOKEN = "session";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IImagingReadonlyQueryDAO dao;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier1;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier2;

    private TestMethodInterceptor testMethodInterceptor;

    private IDssServiceRpcScreening screeningService;

    private ImageTransformerFactory transformerFactory;

    private IImagingTransformerDAO transformerDAO;

    private IImagingDatasetLoader imageLoader;

    private IShareIdManager shareIdManager;

    private IHierarchicalContentProvider contentProvider;

    @BeforeMethod
    public void beforeMethod()
    {
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        final StaticListableBeanFactory applicationContext = new StaticListableBeanFactory();
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        applicationContext.addBean("openBIS-service", service);
        dao = context.mock(IImagingReadonlyQueryDAO.class);
        transformerDAO = context.mock(IImagingTransformerDAO.class);
        imageLoader = context.mock(IImagingDatasetLoader.class);
        shareIdManager = context.mock(IShareIdManager.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        transformerFactory = new ImageTransformerFactory();
        featureVectorDatasetIdentifier1 = create(DATASET_CODE);
        featureVectorDatasetIdentifier2 = create("ds2");
        final ImageDatasetParameters imageParameters = new ImageDatasetParameters();
        imageParameters.setRowsNum(7);
        imageParameters.setColsNum(4);
        imageParameters.setTileRowsNum(1);
        imageParameters.setTileColsNum(2);
        imageParameters.setDatasetCode(DATASET_CODE);
        ImageChannel imageChannel =
                new ImageChannel(CHANNEL_CODE, CHANNEL_CODE, null, null,
                        new ArrayList<ImageTransformationInfo>());
        imageParameters.setChannels(Arrays.asList(imageChannel));
        context.checking(new Expectations()
            {
                {
                    allowing(imageLoader).getImageParameters();
                    will(returnValue(imageParameters));
                }
            });
        testMethodInterceptor = new TestMethodInterceptor(shareIdManager);
        DssServiceRpcScreening rawScreeningService =
                new DssServiceRpcScreening("targets", dao, transformerDAO, service, shareIdManager,
                        contentProvider, false)
                    {
                        @Override
                        IImagingDatasetLoader createImageLoader(String datasetCode, File datasetRoot)
                        {
                            return imageLoader;
                        }
                    };
        ProxyFactory pf = new ProxyFactory();
        pf.addAdvisor(new DssServiceRpcAuthorizationAdvisor(testMethodInterceptor));
        pf.setTarget(rawScreeningService);
        pf.addInterface(IDssServiceRpcScreening.class);
        screeningService = (IDssServiceRpcScreening) pf.getProxy();
    }

    private IFeatureVectorDatasetIdentifier create(final String dataSetCode)
    {
        final IFeatureVectorDatasetIdentifier identifier =
                context.mock(IFeatureVectorDatasetIdentifier.class, dataSetCode);
        context.checking(new Expectations()
            {
                {
                    allowing(identifier).getDatasetCode();
                    will(returnValue(dataSetCode));
                }
            });
        return identifier;
    }

    @AfterMethod(alwaysRun = true)
    public final void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlateImageReferences()
    {
        final DatasetIdentifier ds = new DatasetIdentifier(DATASET_CODE, URL1);
        final List<WellPosition> wellPositions = Arrays.asList(new WellPosition(1, 3));
        final String channel = "dapi";
        prepareGetHomeDatabaseInstance();
        prepareAssetDataSetIsAccessible(ds.getPermId());
        prepareGetShareId();
        prepareLockDataSet(DATASET_CODE);

        List<PlateImageReference> plateImageReferences =
                screeningService
                        .listPlateImageReferences(SESSION_TOKEN, ds, wellPositions, channel);

        assertEquals("[Image for [dataset ds1, well [1, 3], channel DAPI, tile 0], "
                + "Image for [dataset ds1, well [1, 3], channel DAPI, tile 1]]",
                plateImageReferences.toString());

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    @Test
    public void testListAvailableFeatureNames()
    {
        prepareAssetDataSetsAreAccessible();
        prepareLockDataSet("ds1", "ds2");

        long[] dataSetIDs = new long[]
            { 1, 2 };
        String[][] featureCodesPerDataset = new String[][]
            {
                { "f1", "f2" },
                { "f2", "f3" } };
        prepareListDatasets(dataSetIDs);
        prepareGetFeatureDefinitions(dataSetIDs, featureCodesPerDataset);

        List<String> names =
                screeningService.listAvailableFeatureCodes(SESSION_TOKEN, Arrays.asList(
                        featureVectorDatasetIdentifier1, featureVectorDatasetIdentifier2));

        assertEquals("[F1, F2, F3]", names.toString());
        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadFeatures()
    {
        prepareAssetDataSetsAreAccessible();
        prepareLockDataSet("ds1", "ds2");
        FeatureVectorDatasetReference r1 = createFeatureVectorDatasetReference(DATASET_CODE);
        FeatureVectorDatasetReference r2 = createFeatureVectorDatasetReference("ds2");
        String[][] featureCodesPerDataset = new String[][]
            {
                { "F1", "F2" } };
        prepareLoadFeatures(new long[]
            { 1 }, featureCodesPerDataset);
        featureCodesPerDataset = new String[][]
            {
                { "F2" } };
        prepareLoadFeatures(new long[]
            { 2 }, featureCodesPerDataset);

        List<FeatureVectorDataset> dataSets =
                screeningService.loadFeatures(SESSION_TOKEN, Arrays.asList(r1, r2),
                        Arrays.asList("f1", "f2"));

        assertSame(r1, dataSets.get(0).getDataset());
        assertEquals("[F1, F2]", dataSets.get(0).getFeatureCodes().toString());
        assertEquals("[F1, F2]", dataSets.get(0).getFeatureLabels().toString());
        assertFeatureVector(1, 1, dataSets.get(0).getFeatureVectors().get(0), 244.5, 245.5);
        assertFeatureVector(1, 2, dataSets.get(0).getFeatureVectors().get(1), 242.25, 243.25);
        assertEquals(2, dataSets.get(0).getFeatureVectors().size());
        assertSame(r2, dataSets.get(1).getDataset());
        assertEquals("[F2]", dataSets.get(1).getFeatureCodes().toString());
        assertEquals("[F2]", dataSets.get(1).getFeatureLabels().toString());
        assertFeatureVector(1, 1, dataSets.get(1).getFeatureVectors().get(0), 249.0);
        assertFeatureVector(1, 2, dataSets.get(1).getFeatureVectors().get(1), 244.5);
        assertEquals(2, dataSets.get(1).getFeatureVectors().size());
        assertEquals(2, dataSets.size());

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    private void prepareLoadFeatures(long[] dataSetIDs, String[][] featureCodesPerDataset)
    {
        prepareListDatasets(dataSetIDs);
        prepareListContainers(dataSetIDs);
        prepareGetFeatureDefinitions(dataSetIDs, featureCodesPerDataset);
        prepareGetFeatureVocabularyTerms(dataSetIDs);
        prepareCreateFeatureVectorDataSet(dataSetIDs, featureCodesPerDataset);
    }

    @Test
    public void testLoadImages() throws IOException
    {
        final String channel = CHANNEL_CODE;
        prepareGetHomeDatabaseInstance();
        prepareAssetDataSetIsAccessible(DATASET_CODE);
        prepareGetShareId();
        prepareLockDataSet(DATASET_CODE);
        context.checking(new Expectations()
            {
                {
                    RequestedImageSize thumbnailSize =
                            new RequestedImageSize(new Size(2, 1), false);
                    one(imageLoader).tryGetImage(
                            channel,
                            ImageChannelStackReference.createHCSFromLocations(new Location(3, 1),
                                    new Location(1, 1)), thumbnailSize);
                    will(returnValue(new AbsoluteImageReference(image("img1.jpg"), "img1", null,
                            null, thumbnailSize, createBlueColor(),
                            new ImageTransfomationFactories(), null)));
                    one(imageLoader).tryGetImage(
                            channel,
                            ImageChannelStackReference.createHCSFromLocations(new Location(3, 1),
                                    new Location(2, 1)), thumbnailSize);
                    will(returnValue(new AbsoluteImageReference(image("img1.gif"), "img1", null,
                            null, thumbnailSize, createBlueColor(),
                            new ImageTransfomationFactories(), null)));
                }
            });

        InputStream images =
                screeningService.loadImages(SESSION_TOKEN, new DatasetIdentifier(DATASET_CODE,
                        "url1"), Arrays.asList(new WellPosition(1, 3)), channel,
                        new ImageSize(2, 1));

        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(images);
        BufferedImage image1 = extractNextImage(imagesWriter);
        assertEquals(1, image1.getWidth());
        assertEquals(1, image1.getHeight());
        BufferedImage image2 = extractNextImage(imagesWriter);
        assertEquals(1, image2.getWidth());
        assertEquals(1, image2.getHeight());
        images.close();

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    private static ChannelColorRGB createBlueColor()
    {
        return new ChannelColorRGB(0, 0, 255);
    }

    BufferedImage extractNextImage(ConcatenatedFileOutputStreamWriter imagesWriter)
            throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imagesWriter.writeNextBlock(outputStream);
        return ImageUtilTest.loadImage(new ByteArrayBasedContent(outputStream.toByteArray(),
                "UNKNOWN"));
    }

    @Test
    public void testGetImageTransformerFactoryForChannel()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier(DATASET_CODE, "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        final String channel = "dapi";
        prepareAssetDataSetsAreAccessible();
        prepareGetExperimentPermIDs(ds1, ds2);
        prepareLockDataSet("ds1", "ds2");
        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetChannelForExperimentPermId(EXPERIMENT_PERM_ID, channel);
                    ImgChannelDTO channelDTO =
                            new ImgChannelDTO("dapi", null, null, new Long(42), null, "dapi", 0, 0,
                                    255);
                    long channelId = 444;
                    channelDTO.setId(channelId);
                    will(returnValue(channelDTO));

                    one(dao).tryGetImageTransformation(channelId,
                            DssServiceRpcScreening.IMAGE_VIEWER_TRANSFORMATION_CODE);
                    ImgImageTransformationDTO transformationDTO =
                            new ImgImageTransformationDTO("tr_code", "tr_labal", null, false,
                                    channelId, transformerFactory, true);
                    will(returnValue(transformationDTO));
                }
            });

        IImageTransformerFactory result =
                screeningService.getImageTransformerFactoryOrNull(SESSION_TOKEN,
                        Arrays.<IDatasetIdentifier> asList(ds1, ds2), channel);

        assertEquals(transformerFactory.id, ((ImageTransformerFactory) result).id);
        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetImageTransformerFactoryForExperiment()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier(DATASET_CODE, "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        prepareAssetDataSetsAreAccessible();
        prepareGetExperimentPermIDs(ds1, ds2);
        prepareLockDataSet("ds1", "ds2");
        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetExperimentByPermId(EXPERIMENT_PERM_ID);
                    ImgExperimentDTO experiment = new ImgExperimentDTO();
                    experiment.setSerializedImageTransformerFactory(SerializationUtils
                            .serialize(transformerFactory));
                    will(returnValue(experiment));
                }
            });

        IImageTransformerFactory result =
                screeningService.getImageTransformerFactoryOrNull(SESSION_TOKEN,
                        Arrays.<IDatasetIdentifier> asList(ds1, ds2),
                        ScreeningConstants.MERGED_CHANNELS);

        assertEquals(transformerFactory.id, ((ImageTransformerFactory) result).id);
        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    @Test
    public void testSaveImageTransformerFactoryForDatasetChannelFailedDueToInvalidAuthorization()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier(DATASET_CODE, "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        final String channel = "dapi";
        prepareLockDataSet(DATASET_CODE, "ds2");
        context.checking(new Expectations()
            {
                {
                    one(service).checkSpacePowerUserAuthorization(SESSION_TOKEN);
                    will(throwException(new UserFailureException("You are not a space power user.")));
                }
            });

        try
        {
            screeningService.saveImageTransformerFactory(SESSION_TOKEN,
                    Arrays.<IDatasetIdentifier> asList(ds1, ds2), channel, transformerFactory);
            fail("Unauthorized access not detected.");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: You are not a space power user.", ex.getMessage());
        }

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    @Test
    public void testSaveImageTransformerFactoryForDatasetChannel()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier(DATASET_CODE, "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        final String channel = "dapi";
        prepareLockDataSet(DATASET_CODE, "ds2");
        prepareAssetDataSetsAreAccessible();
        context.checking(new Expectations()
            {
                {
                    one(service).checkSpacePowerUserAuthorization(SESSION_TOKEN);

                    long datasetId = 123;
                    ImgDatasetDTO dataset = createDataset(datasetId);
                    dataset.setPermId(DATASET_CODE);

                    one(dao).tryGetDatasetByPermId(DATASET_CODE);
                    will(returnValue(dataset));

                    one(dao).tryGetDatasetByPermId("ds2");
                    will(returnValue(dataset));

                    allowing(dao).hasDatasetChannels(DATASET_CODE);
                    will(returnValue(true));

                    long channelId = 5;
                    long transactionId = 123;
                    exactly(2).of(transformerDAO).getDatasetChannelId(datasetId, channel);
                    will(returnValue(channelId));

                    exactly(2).of(transformerDAO).tryGetImageTransformationId(channelId,
                            DssServiceRpcScreening.IMAGE_VIEWER_TRANSFORMATION_CODE);

                    will(returnValue(transactionId));

                    exactly(2).of(transformerDAO).updateImageTransformerFactory(transactionId,
                            transformerFactory);

                    one(transformerDAO).commit();
                }
            });

        screeningService.saveImageTransformerFactory(SESSION_TOKEN,
                Arrays.<IDatasetIdentifier> asList(ds1, ds2), channel, transformerFactory);

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    private ImgDatasetDTO createDataset(long datasetId)
    {
        ImgDatasetDTO dataset = new ImgDatasetDTO(null, null);
        dataset.setId(datasetId);
        return dataset;
    }

    @Test
    public void testSaveImageTransformerFactoryForExperiment()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier(DATASET_CODE, "url1");
        prepareLockDataSet(DATASET_CODE);
        prepareAssetDataSetsAreAccessible("ds1");
        context.checking(new Expectations()
            {
                {
                    one(service).checkSpacePowerUserAuthorization(SESSION_TOKEN);

                    Long containerId = 312L;

                    long datasetId = 123;
                    ImgDatasetDTO dataset = createDataset(datasetId);
                    dataset.setContainerId(containerId);
                    dataset.setPermId(DATASET_CODE);

                    long experimentId = 888;
                    ImgContainerDTO container = new ImgContainerDTO(null, null, null, experimentId);
                    container.setId(containerId);

                    one(dao).tryGetDatasetByPermId(DATASET_CODE);
                    will(returnValue(dataset));

                    allowing(dao).hasDatasetChannels(DATASET_CODE);
                    will(returnValue(false));

                    one(dao).getContainerById(containerId);
                    will(returnValue(container));

                    one(transformerDAO).saveTransformerFactoryForExperiment(experimentId,
                            transformerFactory);
                    one(transformerDAO).commit();
                }
            });

        screeningService.saveImageTransformerFactory(SESSION_TOKEN,
                Arrays.<IDatasetIdentifier> asList(ds1), ScreeningConstants.MERGED_CHANNELS,
                transformerFactory);

        assertTrue(testMethodInterceptor.methodInvoked);
        context.assertIsSatisfied();
    }

    public void prepareGetExperimentPermIDs(final DatasetIdentifier... dataSetIdentifiers)
    {
        context.checking(new Expectations()
            {
                {
                    ExternalData externalData = new ExternalData();
                    Experiment experiment = new Experiment();
                    experiment.setPermId(EXPERIMENT_PERM_ID);
                    experiment.setId(EXPERIMENT_ID);
                    externalData.setExperiment(experiment);
                    for (DatasetIdentifier datasetIdentifier : dataSetIdentifiers)
                    {
                        one(service).tryGetDataSet(SESSION_TOKEN,
                                datasetIdentifier.getDatasetCode());
                        will(returnValue(externalData));
                    }
                }
            });
    }

    private IContent image(String fileName)
    {
        return new FileBasedContent(new File(ImageChannelsUtilsTest.TEST_IMAGE_FOLDER, fileName));
    }

    private void assertFeatureVector(int expectedRowNumber, int expectedColumnNumber,
            FeatureVector featureVector, Object... expectedValues)
    {
        assertEquals(expectedRowNumber, featureVector.getWellPosition().getWellRow());
        assertEquals(expectedColumnNumber, featureVector.getWellPosition().getWellColumn());

        assertEquals(Arrays.asList(expectedValues).toString(), featureVector.getValueObjects()
                .toString());
    }

    private void prepareGetHomeDatabaseInstance()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).getHomeDatabaseInstance();
                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setUuid("12345");
                    will(returnValue(databaseInstance));
                }
            });
    }

    private void prepareCreateFeatureVectorDataSet(final long[] dataSetIDs,
            final String[]... featureCodesPerDataset)
    {
        context.checking(new Expectations()
            {
                {
                    List<ImgFeatureValuesDTO> values = new ArrayList<ImgFeatureValuesDTO>();
                    long[] featureDefIds = new long[countFeatureCodes(featureCodesPerDataset)];
                    int featureDefIx = 0;
                    int datasetIx = 0;

                    for (String[] featureCodes : featureCodesPerDataset)
                    {
                        long dataSetId = dataSetIDs[datasetIx++];
                        for (String featureCode : featureCodes)
                        {
                            featureDefIds[featureDefIx] = getFeatureDefId(featureCode);
                            int offset = getFeatureDefId(featureCode);
                            PlateFeatureValues matrixValues =
                                    new PlateFeatureValues(NativeTaggedArray
                                            .toByteArray(new MDFloatArray(new float[][]
                                                {
                                                    { 3.5f * dataSetId + offset },
                                                    { 1.25f * dataSetId + offset } })));
                            ImgFeatureValuesDTO value =
                                    new ImgFeatureValuesDTO(0.0, 0.0, matrixValues, 0L);
                            value.setFeatureDefId(featureDefIds[featureDefIx]);
                            values.add(value);

                            featureDefIx++;
                        }
                    }
                    one(dao).getFeatureValues(featureDefIds);
                    will(returnValue(values));
                }

            });
    }

    private void prepareListContainers(final long[] dataSetIDs)
    {
        context.checking(new Expectations()
            {
                {
                    long[] containerIds = new long[dataSetIDs.length];
                    List<ImgContainerDTO> containers = new ArrayList<ImgContainerDTO>();

                    for (int i = 0; i < dataSetIDs.length; i++)
                    {
                        long id = dataSetIDs[i];
                        containerIds[i] = getContainerId(id);
                        ImgContainerDTO container = new ImgContainerDTO("12-34", 1, 2, 0);
                        container.setId(containerIds[i]);
                        containers.add(container);

                        one(service).tryToGetSampleIdentifier("12-34");
                        will(returnValue(new SampleIdentifier(new SpaceIdentifier("1", "S"), "P1")));
                    }

                    one(dao).listContainersByIds(containerIds);
                    will(returnValue(containers));
                }
            });
    }

    private void prepareListDatasets(final long[] dataSetIDs)
    {
        context.checking(new Expectations()
            {
                {
                    String[] permIDs = new String[dataSetIDs.length];
                    List<ImgDatasetDTO> dataSets = new ArrayList<ImgDatasetDTO>();

                    for (int i = 0; i < dataSetIDs.length; i++)
                    {
                        long id = dataSetIDs[i];
                        permIDs[i] = "ds" + id;

                        ImgDatasetDTO dataSet = new ImgDatasetDTO(permIDs[i], getContainerId(id));
                        dataSet.setId(id);
                        dataSets.add(dataSet);
                    }

                    one(dao).listDatasetsByPermId(permIDs);
                    will(returnValue(dataSets));
                }
            });
    }

    private void prepareGetFeatureDefinitions(final long[] dataSetIDs,
            final String[]... featureCodesPerDataset)
    {
        context.checking(new Expectations()
            {
                {
                    List<ImgFeatureDefDTO> defs = new ArrayList<ImgFeatureDefDTO>();
                    int datasetIx = 0;
                    for (String[] featureCodes : featureCodesPerDataset)
                    {
                        long dataSetID = dataSetIDs[datasetIx];
                        for (String code : featureCodes)
                        {
                            ImgFeatureDefDTO def = new ImgFeatureDefDTO(code, code, "", 0);
                            def.setDataSetId(dataSetID);
                            def.setId(getFeatureDefId(code));
                            defs.add(def);
                        }
                        datasetIx++;
                    }

                    one(dao).listFeatureDefsByDataSetIds(dataSetIDs);
                    will(returnValue(defs));
                }
            });
    }

    private void prepareGetFeatureVocabularyTerms(final long[] dataSetIDs)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).listFeatureVocabularyTermsByDataSetId(dataSetIDs);
                    will(returnValue(new ArrayList<ImgFeatureVocabularyTermDTO>()));
                }
            });
    }

    private static long getContainerId(long datasetId)
    {
        return datasetId + 100;
    }

    private static int countFeatureCodes(String[][] featureCodesPerDataset)
    {
        int counter = 0;
        for (String[] featureCodes : featureCodesPerDataset)
        {
            counter += featureCodes.length;
        }
        return counter;
    }

    private static int getFeatureDefId(String code)
    {
        return Integer.parseInt(code, 16);
    }

    private void prepareAssetDataSetIsAccessible(final String dsCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetCollectionAccess(SESSION_TOKEN, Arrays.asList(dsCode));
                }
            });
    }

    private void prepareAssetDataSetsAreAccessible()
    {
        prepareAssetDataSetsAreAccessible("ds1", "ds2");
    }

    private void prepareAssetDataSetsAreAccessible(final String... dsCodes)
    {
        context.checking(new Expectations()
            {
                {
                    one(service)
                            .checkDataSetCollectionAccess(SESSION_TOKEN, Arrays.asList(dsCodes));
                }
            });
    }

    private void prepareGetShareId()
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId(DATASET_CODE);
                    will(returnValue(Constants.DEFAULT_SHARE_ID));
                }
            });
    }

    private void prepareLockDataSet(final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    for (String dataSetCode : dataSetCodes)
                    {
                        one(shareIdManager).lock(dataSetCode);
                    }
                    one(shareIdManager).releaseLocks();
                }
            });
    }

    private FeatureVectorDatasetReference createFeatureVectorDatasetReference(String dataSetCode)
    {
        return new FeatureVectorDatasetReference(dataSetCode, null, "", null, null, null, null,
                null, null);
    }

    // Used for the authorization test
    private static class TestMethodInterceptor extends DssServiceRpcAuthorizationMethodInterceptor
            implements MethodInterceptor
    {
        public TestMethodInterceptor(IShareIdManager shareIdManager)
        {
            super(shareIdManager);
        }

        private boolean methodInvoked = false;

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            methodInvoked = true;
            Object result = super.invoke(methodInvocation);
            return result;
        }
    }

}
