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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.openbis.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.openbis.common.io.HierarchicalContentNodeBasedHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.IImagingLoaderStrategy;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.images.RepresentationUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.screening.server.logic.ImageRepresentationFormatFinder;
import ch.systemsx.cisd.openbis.dss.screening.server.util.FeatureVectorLoaderMetadataProviderFactory;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.AbstractFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat.ImageRepresentationTransformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractImgIdentifiable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import net.iharder.Base64;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring and published as a HTTP invoker servlet configured
 * in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc<IDssServiceRpcScreening>
        implements IDssServiceRpcScreening
{

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 14;

    // this dao will hold one connection to the database
    private final IImagingReadonlyQueryDAO dao;

    private final IImagingTransformerDAO transformerDAO;

    /**
     * The designated constructor.
     */
    public DssServiceRpcScreening(String storeRootDir)
    {
        this(storeRootDir, DssScreeningUtils.getQuery(), DssScreeningUtils.createImagingTransformerDAO(), ServiceProvider
                .getOpenBISService(), null, null, null, true);
    }

    DssServiceRpcScreening(String storeRootDir, IImagingReadonlyQueryDAO dao,
            IImagingTransformerDAO transformerDAO, IEncapsulatedOpenBISService service,
            IStreamRepository streamRepository, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider, boolean registerAtNameService)
    {
        super(service, streamRepository, shareIdManager, contentProvider);
        this.dao = dao;
        this.transformerDAO = transformerDAO;
        setStoreDirectory(new File(storeRootDir));
        if (registerAtNameService)
        {
            // Register the service with the name server
            RpcServiceInterfaceVersionDTO ifaceVersion =
                    new RpcServiceInterfaceVersionDTO(IDssServiceRpcScreening.SERVICE_NAME,
                            IDssServiceRpcScreening.SERVICE_URL, getMajorVersion(),
                            getMinorVersion());
            RpcServiceInterfaceVersionDTO jsonVersion =
                    new RpcServiceInterfaceVersionDTO(IDssServiceRpcScreening.SERVICE_NAME,
                            IDssServiceRpcScreening.JSON_SERVICE_URL, getMajorVersion(),
                            getMinorVersion());

            HttpInvokerServiceExporter nameServiceExporter =
                    ServiceProvider.getRpcNameServiceExporter();
            RpcServiceNameServer nameServer =
                    (RpcServiceNameServer) nameServiceExporter.getService();
            nameServer.addSupportedInterfaceVersion(ifaceVersion);
            nameServer.addSupportedInterfaceVersion(jsonVersion);

            operationLog.info("[rpc] Started DSS RPC screening service V1.");
        }
    }

    // ------------------ impl -----------------

    @Override
    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    @Override
    public IDssServiceRpcScreening createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcScreeningLogger(context);
    }

    @Override
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        List<ImgFeatureDefDTO> featureDefinitions =
                getFeatureDefinitionsWithContained(featureDatasets);

        // add only new feature names
        List<String> result = new ArrayList<String>(); // keep the order
        for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
        {
            String featureCode = featureDefinition.getCode();
            if (result.contains(featureCode) == false)
            {
                result.add(featureCode);
            }
        }
        return result;
    }

    @Override
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        List<ImgFeatureDefDTO> featureDefinitions =
                getFeatureDefinitionsWithContained(featureDatasets);

        // add only new feature names
        List<FeatureInformation> result = new ArrayList<FeatureInformation>(); // keep the order
        for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
        {
            FeatureInformation description =
                    new FeatureInformation(featureDefinition.getCode(),
                            featureDefinition.getLabel(), featureDefinition.getDescription());
            if (result.contains(description) == false)
            {
                result.add(description);
            }
        }
        return result;
    }

    @Override
    public List<String> listAvailableFeatureLists(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset)
    {
        IHierarchicalContent content =
                ServiceProvider.getHierarchicalContentProvider().asContent(
                        featureDataset.getDatasetCode());
        IHierarchicalContentNode node =
                content.tryGetNode(ScreeningConstants.ANALYSIS_FEATURE_LIST_TOP_LEVEL_DIRECTORY_NAME);

        List<String> result = new LinkedList<String>();
        if (node != null && node.exists() && node.isDirectory())
        {
            List<IHierarchicalContentNode> children = node.getChildNodes();
            for (IHierarchicalContentNode child : children)
            {
                result.add(child.getName());
            }
        }
        return result;
    }

    @Override
    public List<String> getFeatureList(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset, String featureListCode)
    {
        try
        {
            String value = readFeatureListContent(featureDataset.getDatasetCode(), featureListCode);
            String[] values = value.split("\n");
            return Arrays.asList(values);
        } catch (IOException ioe)
        {
            throw new IllegalStateException(
                    "Cannot get the feature list of feature list doesn't exist", ioe);
        }
    }

    protected String readFeatureListContent(String dataSetCode, String featureListCode)
            throws IOException
    {
        IHierarchicalContent content =
                ServiceProvider.getHierarchicalContentProvider().asContent(dataSetCode);
        IHierarchicalContentNode node =
                content.tryGetNode(ScreeningConstants.ANALYSIS_FEATURE_LIST_TOP_LEVEL_DIRECTORY_NAME
                        + "/" + featureListCode);

        if (node == null)
        {
            throw new UserFailureException("The specified feature list <" + featureListCode
                    + "> is not defined for data set " + dataSetCode);
        }

        InputStream is = null;
        String value = null;
        try
        {
            is = node.getInputStream();
            value = IOUtils.toString(is);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
        return value;
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        final IShareIdManager shareIdManager = getShareIdManager();
        final List<String> dataSetCodes = new ArrayList<String>(imageDatasets.size());
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            dataSetCodes.add(dataset.getDatasetCode());
        }
        shareIdManager.lock(dataSetCodes);
        try
        {
            List<ImageDatasetMetadata> result = new ArrayList<ImageDatasetMetadata>();
            for (IImageDatasetIdentifier dataset : imageDatasets)
            {
                final IHierarchicalContent content =
                        getHierarchicalContent(sessionToken, dataset.getDatasetCode());
                try
                {
                    result.add(extractImageMetadata(dataset, content));
                } finally
                {
                    content.close();
                }
            }
            return result;
        } finally
        {
            shareIdManager.releaseLocks();
        }
    }

    private ImageDatasetMetadata extractImageMetadata(IImageDatasetIdentifier dataset,
            IHierarchicalContent content)
    {
        final IImagingDatasetLoader imageAccessor =
                createImageLoader(dataset.getDatasetCode(), content);
        final Size imageSize = getOriginalImageSize(dataset, imageAccessor);
        final Size thumbnailSize = getThumbnailImageSize(dataset, imageAccessor);
        final ImageDatasetParameters params = imageAccessor.getImageParameters();
        return new ImageDatasetMetadata(dataset, toPublicChannels(params.getInternalChannels()),
                params.getTileRowsNum(), params.getTileColsNum(), imageSize.getWidth(),
                imageSize.getHeight(), thumbnailSize.getWidth(), thumbnailSize.getHeight());
    }

    private List<ImageChannel> toPublicChannels(List<InternalImageChannel> internalChannels)
    {
        final List<ImageChannel> publicChannels =
                new ArrayList<ImageChannel>(internalChannels.size());
        for (InternalImageChannel channel : internalChannels)
        {
            publicChannels.add(new ImageChannel(channel.getCode(), channel.getLabel(), channel
                    .tryGetDescription(), channel.tryGetWavelength(),
                    toPublicImageTransformationInfos(channel.getAvailableImageTransformations())));
        }
        return publicChannels;

    }

    private List<ImageTransformationInfo> toPublicImageTransformationInfos(
            List<InternalImageTransformationInfo> internalTrafos)
    {
        final List<ImageTransformationInfo> publicTrafos =
                new ArrayList<ImageTransformationInfo>(internalTrafos.size());
        for (InternalImageTransformationInfo info : internalTrafos)
        {
            publicTrafos.add(new ImageTransformationInfo(info.getCode(), info.getLabel(), info
                    .getDescription(), info.isDefault()));
        }
        return publicTrafos;
    }

    /**
     * Gets the size of the image by first looking at the zoomLevel table or reads the whole image as a fallback
     */
    private Size getOriginalImageSize(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        List<ImgImageZoomLevelDTO> zoomLevelLists =
                dao.listOriginalImageZoomLevelsByPermId(dataset.getPermId());

        if (zoomLevelLists.isEmpty())
        {
            operationLog
                    .warn("No zoom-level entry found for the original image of specified dataset "
                            + dataset.getPermId());
            return getOriginalImageSizeFetchingImage(dataset, imageAccessor);
        }

        ImgImageZoomLevelDTO first = zoomLevelLists.get(0);

        if (first == null || first.getWidth() == null || first.getHeight() == null)
        {
            operationLog
                    .warn("Image dimensions not found for the zoom level of the original image of specified dataset "
                            + dataset.getPermId());
            return getOriginalImageSizeFetchingImage(dataset, imageAccessor);
        }

        Size imageSize = new Size(first.getWidth(), first.getHeight());
        return imageSize;
    }

    /**
     * Gets the size of the thumbnail image by first looking at the zoomLevel table or reads the whole image as a fallback
     */
    private Size getThumbnailImageSize(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        List<ImgImageZoomLevelDTO> zoomLevelLists =
                dao.listThumbImageZoomLevelsByPermId(dataset.getPermId());
        if (zoomLevelLists.isEmpty())
        {
            operationLog.warn("No zoom-level entry found for the thumbnail of specified dataset "
                    + dataset.getPermId());
            return getThumbnailImageSizeFetchingImage(dataset, imageAccessor);
        }

        ImgImageZoomLevelDTO first = zoomLevelLists.get(0);

        if (first == null || first.getWidth() == null || first.getHeight() == null)
        {
            operationLog
                    .warn("Image dimensions not found for the zoom level of the thumbnail of specified dataset "
                            + dataset.getPermId());
            return getThumbnailImageSizeFetchingImage(dataset, imageAccessor);
        }

        Size imageSize = new Size(first.getWidth(), first.getHeight());
        return imageSize;
    }

    /**
     * gets the size of the original image by reading the whole image.
     */
    private static Size getOriginalImageSizeFetchingImage(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        BufferedImage image = getAnyImage(imageAccessor, dataset);
        Size imageSize = new Size(image.getWidth(), image.getHeight());
        return imageSize;
    }

    /**
     * gets the size of the thumbnail image by reading the whole image.
     */
    private static Size getThumbnailImageSizeFetchingImage(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        BufferedImage image = getAnyThumbnailImage(imageAccessor, dataset);
        if (image != null)
        {
            return new Size(image.getWidth(), image.getHeight());
        } else
        {
            return Size.NULL_SIZE;
        }
    }

    private static BufferedImage getAnyImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        if (imageAccessor.getImageParameters().tryGetRowsNum() == null)
        {
            return getAnyMicroscopyImage(imageAccessor, dataset);
        } else
        {
            return getAnyHCSImage(imageAccessor, dataset);
        }
    }

    private static BufferedImage getAnyThumbnailImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        if (imageAccessor.getImageParameters().tryGetRowsNum() == null)
        {
            return getAnyMicroscopyThumbnail(imageAccessor, dataset);
        } else
        {
            return getAnyHCSThumbnail(imageAccessor, dataset);
        }
    }

    private static BufferedImage getAnyMicroscopyImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        ImageDatasetParameters params = imageAccessor.getImageParameters();

        RequestedImageSize originalOrThumbnail = RequestedImageSize.createOriginal();
        for (String channelCode : params.getChannelsCodes())
        {
            AbsoluteImageReference image =
                    imageAccessor.tryGetRepresentativeImage(channelCode, null, originalOrThumbnail,
                            null);
            if (image != null)
            {
                return image.getUnchangedImage();
            }
        }
        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    private static BufferedImage getAnyMicroscopyThumbnail(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        ImageDatasetParameters params = imageAccessor.getImageParameters();
        for (String channelCode : params.getChannelsCodes())
        {
            AbsoluteImageReference image =
                    imageAccessor.tryGetRepresentativeThumbnail(channelCode, null, null, null);
            if (image != null)
            {
                return image.getUnchangedImage();
            }
        }
        return null;
    }

    private static BufferedImage getAnyHCSThumbnail(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        AbsoluteImageReference image = imageAccessor.tryFindAnyThumbnail();
        if (image != null)
        {
            return image.getUnchangedImage();
        }

        return null;
    }

    private static BufferedImage getAnyHCSImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        AbsoluteImageReference image = imageAccessor.tryFindAnyOriginalImage();

        if (image != null)
        {
            return image.getUnchangedImage();
        }

        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    @Override
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames)
    {
        List<String> codes = normalize(featureNames);
        List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();

        IMetadataProvider metadataProvider =
                FeatureVectorLoaderMetadataProviderFactory
                        .createMetadataProviderFromFeatureVectors(getOpenBISService(),
                                featureDatasets);

        prefetchSampleIdentifiers(featureDatasets, metadataProvider);

        for (FeatureVectorDatasetReference dataset : featureDatasets)
        {
            result.add(createFeatureVectorDataset(sessionToken, dataset, codes, metadataProvider));
        }
        return result;
    }

    private void prefetchSampleIdentifiers(List<FeatureVectorDatasetReference> featureDatasets,
            IMetadataProvider metadataProvider)
    {
        List<String> dataSetIds = new LinkedList<String>();
        for (FeatureVectorDatasetReference featureVectorDatasetReference : featureDatasets)
        {
            dataSetIds.add(featureVectorDatasetReference.getDatasetCode());
            dataSetIds.addAll(metadataProvider
                    .tryGetContainedDatasets(featureVectorDatasetReference.getDatasetCode()));
        }

        List<ImgAnalysisDatasetDTO> dataSets =
                getDAO().listAnalysisDatasetsByPermId(dataSetIds.toArray(new String[0]));
        long[] containerIds = new long[dataSets.size()];
        int i = 0;
        for (ImgAnalysisDatasetDTO adto : dataSets)
        {
            containerIds[i++] = adto.getContainerId();
        }

        List<ImgContainerDTO> containers = getDAO().listContainersByIds(containerIds);

        List<String> samplePermIds = new LinkedList<String>();

        for (ImgContainerDTO container : containers)
        {
            samplePermIds.add(container.getPermId());
        }

        metadataProvider.getSampleIdentifiers(samplePermIds);
    }

    private FeatureVectorDataset createFeatureVectorDataset(String sessionToken,
            FeatureVectorDatasetReference dataset, List<String> featureCodes,
            IMetadataProvider metadataProvider)
    {
        WellFeatureCollection<FeatureTableRow> datasetFeatures =
                FeatureVectorLoader.fetchDatasetFeatures(Arrays.asList(dataset.getDatasetCode()),
                        featureCodes, getDAO(), metadataProvider);
        List<FeatureVector> featureVectors = new ArrayList<FeatureVector>();
        for (FeatureTableRow featureTableRow : datasetFeatures.getFeatures())
        {
            WellLocation wellPosition = featureTableRow.getWellLocation();
            featureVectors.add(getFeatureVector(convert(wellPosition), featureTableRow));
        }
        return new FeatureVectorDataset(dataset, datasetFeatures.getFeatureCodes(),
                datasetFeatures.getFeatureLabels(), featureVectors);
    }

    private static FeatureVector getFeatureVector(WellPosition position, FeatureTableRow featureTableRow)
    {
        FeatureValue[] featureValues = featureTableRow.getFeatureValues();
        double[] doubleValues = new double[featureValues.length];
        String[] vocabularyTerms = new String[featureValues.length];
        boolean[] vocabularyFeatureFlags = new boolean[featureValues.length];

        for (int i = 0; i < featureValues.length; ++i)
        {
            FeatureValue featureValue = featureValues[i];
            if (featureValue.isFloat())
            {
                doubleValues[i] = featureValue.asFloat();
            } else if (featureValue.isVocabularyTerm())
            {
                vocabularyFeatureFlags[i] = true;
                vocabularyTerms[i] = featureValue.tryAsVocabularyTerm();
                doubleValues[i] = Double.NaN;
            } else
            {
                throw new IllegalStateException("Feature can be only either a float or a vocabulary term");
            }
        }
        return new FeatureVector(position, doubleValues, vocabularyFeatureFlags, vocabularyTerms);
    }

    private static WellPosition convert(WellLocation wellPosition)
    {
        return new WellPosition(wellPosition.getRow(), wellPosition.getColumn());
    }

    private List<String> normalize(List<String> names)
    {
        ArrayList<String> codes = new ArrayList<String>(names.size());
        for (String name : names)
        {
            codes.add(CodeNormalizer.normalize(name));
        }
        return codes;
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureNames)
    {
        WellFeatureCollection<FeatureTableRow> features =
                FeatureVectorLoader.fetchWellFeatures(datasetWellReferences, featureNames, getDAO(),
                        FeatureVectorLoaderMetadataProviderFactory
                                .createMetadataProviderFromFeatureVectors(getOpenBISService(),
                                        datasetWellReferences));
        return createFeatureVectorList(features);
    }

    private List<FeatureVectorWithDescription> createFeatureVectorList(
            final WellFeatureCollection<FeatureTableRow> features)
    {
        final List<String> featureCodes = features.getFeatureCodes();
        final List<FeatureTableRow> featureTableRows = features.getFeatures();
        final List<FeatureVectorWithDescription> result =
                new ArrayList<FeatureVectorWithDescription>(featureTableRows.size());
        for (FeatureTableRow featureTableRow : featureTableRows)
        {
            result.add(createFeatureVector(featureTableRow, featureCodes));
        }
        return result;
    }

    private FeatureVectorWithDescription createFeatureVector(FeatureTableRow featureTableRow,
            final List<String> featureCodes)
    {
        FeatureVector feature = getFeatureVector(featureTableRow.getReference().getWellPosition(), featureTableRow);
        return new FeatureVectorWithDescription(featureTableRow.getReference(), featureCodes,
                feature.getValues());
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        return loadImages(sessionToken, imageReferences, null, null, convertToPng);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, boolean convertToPng)
    {

        return convertToBase64(loadImages(sessionToken, imageReferences, convertToPng));
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize thumbnailSizeOrNull)
    {
        return loadImages(sessionToken, imageReferences, tryAsSize(thumbnailSizeOrNull), null, true);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageSize size)
    {
        return convertToBase64(loadImages(sessionToken, imageReferences, size));
    }

    @Override
    public InputStream loadImages(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        return loadImages(imageReferences, tryAsSize(configuration.getDesiredImageSize()),
                configuration.getSingleChannelImageTransformationCode(),
                configuration.isDesiredImageFormatPng(),
                configuration.isOpenBisImageTransformationApplied(), imageLoadersMap);
    }

    @Override
    public List<String> loadImagesBase64(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        return convertToBase64(loadImages(sessionToken, imageReferences, configuration));
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            final ImageRepresentationFormat format)
    {
        for (PlateImageReference plateImageReference : imageReferences)
        {
            if (plateImageReference.getDatasetCode().equals(format.getDataSetCode()) == false)
            {
                throw new UserFailureException(
                        "At least for one plate image reference the image representation format "
                                + "is unknown: Plate image reference: " + plateImageReference
                                + ", format: " + format);
            }
        }
        IImageRepresentationFormatSelectionCriterion criterion =
                new AbstractFormatSelectionCriterion()
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected boolean accept(ImageRepresentationFormat availableFormat)
                        {
                            return format.getId() == availableFormat.getId();
                        }
                    };
        return loadImages(sessionToken, imageReferences, criterion);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return convertToBase64(loadImages(sessionToken, imageReferences, format));
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        Map<String, ImgImageDatasetDTO> imageDataSetMap =
                createDataSetCodeToImageDataSetMap(imageReferences);
        ImageRepresentationFormatFinder finder = new ImageRepresentationFormatFinder(criteria);
        Map<String, ImageRepresentationFormat> dataSetToImageReferenceFormatMap =
                new HashMap<String, ImageRepresentationFormat>();
        for (Entry<String, ImgImageDatasetDTO> entry : imageDataSetMap.entrySet())
        {
            String dataSetCode = entry.getKey();
            List<ImageRepresentationFormat> filteredFormats =
                    finder.find(RepresentationUtil.getImageRepresentationFormats(entry.getValue(),
                            getDAO()));
            if (filteredFormats.isEmpty())
            {
                throw new UserFailureException(
                        "No image representation format fitting criteria found for data set "
                                + dataSetCode + ".");
            }
            if (filteredFormats.size() > 1)
            {
                throw new UserFailureException(
                        "To many image representation formats fitting criteria for data set "
                                + dataSetCode + ": " + filteredFormats);
            }
            dataSetToImageReferenceFormatMap.put(dataSetCode, filteredFormats.get(0));
        }
        List<IHierarchicalContentNode> imageContents = new ArrayList<IHierarchicalContentNode>();
        for (PlateImageReference imageReference : imageReferences)
        {
            String datasetCode = imageReference.getDatasetCode();
            IImagingDatasetLoader loader = imageLoadersMap.get(datasetCode);
            ImageRepresentationFormat format = dataSetToImageReferenceFormatMap.get(datasetCode);
            Size size = new Size(format.getWidth(), format.getHeight());
            addImageContentTo(imageContents, loader, imageReference, size, null, false, false);
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        return convertToBase64(loadImages(sessionToken, imageReferences, criteria));
    }

    private InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            final Size sizeOrNull, String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        return loadImages(imageReferences, sizeOrNull, singleChannelImageTransformationCodeOrNull,
                convertToPng, false, imageLoadersMap);
    }

    private InputStream loadImages(List<PlateImageReference> imageReferences,
            final Size sizeOrNull, final String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng, final boolean transform,
            final Map<String, IImagingDatasetLoader> imageLoadersMap)
    {
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final PlateImageReference imageReference : imageReferences)
        {
            final IImagingDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;

            addImageContentTo(imageContents, imageAccessor, imageReference, sizeOrNull,
                    singleChannelImageTransformationCodeOrNull, convertToPng, transform);
        }

        return new ConcatenatedContentInputStream(true, imageContents);
    }

    private void addImageContentTo(final List<IHierarchicalContentNode> imageContents,
            final IImagingDatasetLoader imageAccessor, final PlateImageReference imageReference,
            final Size sizeOrNull, final String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng, final boolean transform)
    {
        final IImagingLoaderStrategy imageLoaderStrategy =
                ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor);

        final ImageChannelStackReference channelStackRef =
                getImageChannelStackReference(imageAccessor, imageReference);
        final String channelCode = imageReference.getChannel();

        imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, sizeOrNull,
                        singleChannelImageTransformationCodeOrNull, convertToPng, transform)));
    }

    private InputStream loadThumbnailImages(List<PlateImageReference> imageReferences,
            final Map<String, IImagingDatasetLoader> imageLoadersMap)
    {
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final PlateImageReference imageReference : imageReferences)
        {
            final IImagingDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;

            final IImagingLoaderStrategy imageLoaderStrategy =
                    ImagingLoaderStrategyFactory.createThumbnailLoaderStrategy(imageAccessor);

            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, null,
                            null, false, false)));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        return loadImages(sessionToken, imageReferences, true);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return convertToBase64(loadImages(sessionToken, imageReferences));
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        final IImagingDatasetLoader imageAccessor =
                createImageLoader(sessionToken, dataSetIdentifier);

        final List<PlateImageReference> imageReferences =
                createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
        final Size size = tryAsSize(thumbnailSizeOrNull);

        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                new HashMap<String, IImagingDatasetLoader>();
        imageLoadersMap.put(dataSetIdentifier.getDatasetCode(), imageAccessor);

        return loadImages(imageReferences, size, null, true, false, imageLoadersMap);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        return convertToBase64(loadImages(sessionToken, dataSetIdentifier, wellPositions, channel,
                thumbnailSizeOrNull));
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);

        return loadThumbnailImages(imageReferences, imageLoadersMap);
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return convertToBase64(loadThumbnailImages(sessionToken, imageReferences));
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        final IImagingDatasetLoader imageAccessor =
                createImageLoader(sessionToken, dataSetIdentifier);
        List<MicroscopyImageReference> imageReferences =
                listImageReferences(dataSetIdentifier, channel, imageAccessor);
        final Size sizeOrNull = tryAsSize(thumbnailSizeOrNull);

        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final MicroscopyImageReference imageReference : imageReferences)
        {
            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(
                            ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                            channelStackRef, channelCode, sizeOrNull, null, true, false)));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        return convertToBase64(loadImages(sessionToken, dataSetIdentifier, channel,
                thumbnailSizeOrNull));
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        final IImagingDatasetLoader imageAccessor =
                createImageLoader(sessionToken, dataSetIdentifier);
        assert imageAccessor != null : "imageAccessor not found for: " + dataSetIdentifier;
        final List<MicroscopyImageReference> imageReferences =
                listImageReferences(dataSetIdentifier, channels, imageAccessor);
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final MicroscopyImageReference imageReference : imageReferences)
        {
            final IImagingLoaderStrategy imageLoaderStrategy =
                    ImagingLoaderStrategyFactory.createThumbnailLoaderStrategy(imageAccessor);

            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, null,
                            null, false, false)));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return convertToBase64(loadThumbnailImages(sessionToken, dataSetIdentifier, channels));
    }

    @Override
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            java.util.List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return convertToBase64(loadPhysicalThumbnails(sessionToken, imageReferences, format));
    }

    @Override
    public InputStream loadPhysicalThumbnails(String sessionToken,
            List<PlateImageReference> imageReferences, final ImageRepresentationFormat format)
    {
        for (PlateImageReference plateImageReference : imageReferences)
        {
            if (plateImageReference.getDatasetCode().equals(format.getDataSetCode()) == false)
            {
                throw new UserFailureException(
                        "At least for one plate image reference the image representation format "
                                + "is unknown: Plate image reference: " + plateImageReference
                                + ", format: " + format);
            }
        }
        IImageRepresentationFormatSelectionCriterion criterion =
                new AbstractFormatSelectionCriterion()
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected boolean accept(ImageRepresentationFormat availableFormat)
                        {
                            return format.getId() == availableFormat.getId();
                        }
                    };
        return loadPhysicalThumbnails(sessionToken, imageReferences, criterion);
    }

    public InputStream loadPhysicalThumbnails(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        Map<String, ImgImageDatasetDTO> imageDataSetMap =
                createDataSetCodeToImageDataSetMap(imageReferences);
        ImageRepresentationFormatFinder finder = new ImageRepresentationFormatFinder(criteria);
        Map<String, ImageRepresentationFormat> dataSetToImageReferenceFormatMap =
                new HashMap<String, ImageRepresentationFormat>();
        for (Entry<String, ImgImageDatasetDTO> entry : imageDataSetMap.entrySet())
        {
            String dataSetCode = entry.getKey();
            List<ImageRepresentationFormat> filteredFormats =
                    finder.find(RepresentationUtil.getImageRepresentationFormats(entry.getValue(),
                            getDAO()));
            if (filteredFormats.isEmpty())
            {
                throw new UserFailureException(
                        "No image representation format fitting criteria found for data set "
                                + dataSetCode + ".");
            }
            if (filteredFormats.size() > 1)
            {
                throw new UserFailureException(
                        "To many image representation formats fitting criteria for data set "
                                + dataSetCode + ": " + filteredFormats);
            }
            dataSetToImageReferenceFormatMap.put(dataSetCode, filteredFormats.get(0));
        }
        List<IHierarchicalContentNode> imageContents = new ArrayList<IHierarchicalContentNode>();
        for (PlateImageReference imageReference : imageReferences)
        {
            String datasetCode = imageReference.getDatasetCode();
            IImagingDatasetLoader loader = imageLoadersMap.get(datasetCode);
            ImageRepresentationFormat format = dataSetToImageReferenceFormatMap.get(datasetCode);
            Size size = new Size(format.getWidth(), format.getHeight());

            String transformation = tryGetTransformation(imageReference, format);

            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(loader, imageReference);

            AbsoluteImageReference imr =
                    loader.tryGetThumbnail(imageReference.getChannel(), channelStackRef,
                            new RequestedImageSize(size, false, false), transformation);

            IHierarchicalContentNode content = imr != null ? imr.tryGetRawContent() : null;

            if (content == null)
            {
                throw new UserFailureException(
                        "Couldn't fetch the image as raw content, as it is only a partial content of an image");
            }
            imageContents.add(content);
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    protected String tryGetTransformation(PlateImageReference imageReference,
            ImageRepresentationFormat format)
    {
        for (ImageRepresentationTransformation t : format.getTransformations())
        {
            if (t.getChannelCode().equals(imageReference.getChannel()))
            {
                return t.getTransformationCode();
            }
        }
        return null;
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, String channel)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(sessionToken, dataSetIdentifier);
        return listImageReferences(dataSetIdentifier, channel, imageAccessor);
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(sessionToken, dataSetIdentifier);
        return listImageReferences(dataSetIdentifier, channels, imageAccessor);
    }

    private List<MicroscopyImageReference> listImageReferences(
            IDatasetIdentifier dataSetIdentifier, String channel,
            IImagingDatasetLoader imageAccessor)
    {
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<MicroscopyImageReference> imageReferences = new ArrayList<MicroscopyImageReference>();
        for (int i = 0; i < numberOfTiles; i++)
        {
            imageReferences.add(new MicroscopyImageReference(i, channel, dataSetIdentifier));
        }
        return imageReferences;
    }

    private List<MicroscopyImageReference> listImageReferences(
            IDatasetIdentifier dataSetIdentifier, List<String> channels,
            IImagingDatasetLoader imageAccessor)
    {
        final int numberOfTiles = getNumberOfTiles(imageAccessor);
        final List<MicroscopyImageReference> imageReferences =
                new ArrayList<MicroscopyImageReference>(numberOfTiles * channels.size());
        for (int i = 0; i < numberOfTiles; i++)
        {
            for (String channel : channels)
            {
                imageReferences.add(new MicroscopyImageReference(i, channel, dataSetIdentifier));
            }
        }
        return imageReferences;
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(sessionToken, dataSetIdentifier);
        return createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(sessionToken, dataSetIdentifier);
        return createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channels);
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        ArrayList<DatasetImageRepresentationFormats> result =
                new ArrayList<DatasetImageRepresentationFormats>(imageDatasets.size());

        Map<String, ImgImageDatasetDTO> imageDataSetMap =
                createDataSetCodeToImageDataSetMap(imageDatasets);

        // Not an efficient way to execute this query, but it reuses the queries at our disposal
        for (IDatasetIdentifier imageDataset : imageDatasets)
        {
            String dataSetCode = imageDataset.getPermId();
            ImgImageDatasetDTO primImageDataSet = imageDataSetMap.get(dataSetCode);
            if (null == primImageDataSet)
            {
                List<ImageRepresentationFormat> emptyList = Collections.emptyList();
                DatasetImageRepresentationFormats datasetResult =
                        new DatasetImageRepresentationFormats(imageDataset, emptyList);
                result.add(datasetResult);
            } else
            {
                List<ImageRepresentationFormat> formats =
                        RepresentationUtil
                                .getImageRepresentationFormats(primImageDataSet, getDAO());
                DatasetImageRepresentationFormats datasetResult =
                        new DatasetImageRepresentationFormats(imageDataset, formats);
                result.add(datasetResult);
            }
        }
        return result;
    }

    private Map<String, ImgImageDatasetDTO> createDataSetCodeToImageDataSetMap(
            List<? extends IDatasetIdentifier> imageDatasets)
    {
        Set<String> permIds = new HashSet<String>();
        for (IDatasetIdentifier identifier : imageDatasets)
        {
            permIds.add(identifier.getPermId());
        }
        List<ImgImageDatasetDTO> primImageDatasets =
                getDAO().listImageDatasetsByPermId(permIds.toArray(new String[permIds.size()]));

        // Convert this to a hash map for faster indexing
        HashMap<String, ImgImageDatasetDTO> imageDataSetMap =
                new HashMap<String, ImgImageDatasetDTO>();
        for (ImgImageDatasetDTO primImageDataSet : primImageDatasets)
        {
            imageDataSetMap.put(primImageDataSet.getPermId(), primImageDataSet);
        }
        return imageDataSetMap;
    }

    private IImagingDatasetLoader createImageLoader(String sessionToken,
            IDatasetIdentifier dataSetIdentifier)
    {
        final String datasetCode = dataSetIdentifier.getDatasetCode();
        return createImageLoader(sessionToken, datasetCode);
    }

    @Override
    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        for (IDatasetIdentifier datasetIdentifier : dataSetIdentifiers)
        {
            ImgImageDatasetDTO dataset = getImagingImageDataset(datasetIdentifier);
            if (dataset == null)
            {
                throw new UserFailureException("Unkown data set " + datasetIdentifier);
            }
            if (dao.hasDatasetChannels(dataset.getPermId()))
            {
                saveImageTransformerFactoryForDataset(dataset.getId(), channel, transformerFactory);
            } else
            {
                Long containerId = dataset.getContainerId();
                ImgContainerDTO container = getDAO().getContainerById(containerId);
                saveImageTransformerFactoryForExperiment(container.getExperimentId(), channel,
                        transformerFactory);
            }
        }
        transformerDAO.commit();
    }

    private static boolean isMergedChannel(String channel)
    {
        return ScreeningConstants.MERGED_CHANNELS.equals(channel);
    }

    static final String IMAGE_VIEWER_TRANSFORMATION_CODE = "_CUSTOM";

    private static final String IMAGE_VIEWER_TRANSFORMATION_LABEL = "Custom";

    private static final String IMAGE_VIEWER_TRANSFORMATION_DESCRIPTION =
            "Custom image transformation defined with the Color Adjustment tool.";

    private void saveImageTransformerFactoryForExperiment(long experimentId, String channel,
            IImageTransformerFactory transformerFactory)
    {
        if (isMergedChannel(channel))
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for experiment " + experimentId);
            }
            transformerDAO.saveTransformerFactoryForExperiment(experimentId, transformerFactory);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for experiment " + experimentId + " and channel '" + channel + "'.");
            }
            Long channelId = transformerDAO.getExperimentChannelId(experimentId, channel);
            createOrUpdateImageViewerTransformation(channelId, transformerFactory);
        }
    }

    private void createOrUpdateImageViewerTransformation(long channelId,
            IImageTransformerFactory transformerFactory)
    {
        Long transformationIdOrNull =
                transformerDAO.tryGetImageTransformationId(channelId,
                        IMAGE_VIEWER_TRANSFORMATION_CODE);
        if (transformationIdOrNull == null)
        {
            transformerDAO.addImageTransformation(createImageViewerTransformation(channelId,
                    transformerFactory));
        } else
        {
            if (transformerFactory != null)
            {
                transformerDAO.updateImageTransformerFactory(transformationIdOrNull,
                        transformerFactory);
            } else
            {
                transformerDAO.removeImageTransformation(transformationIdOrNull);
            }
        }
    }

    private ImgImageTransformationDTO createImageViewerTransformation(long channelId,
            IImageTransformerFactory transformerFactory)
    {
        return new ImgImageTransformationDTO(IMAGE_VIEWER_TRANSFORMATION_CODE,
                IMAGE_VIEWER_TRANSFORMATION_LABEL, IMAGE_VIEWER_TRANSFORMATION_DESCRIPTION, false,
                channelId, transformerFactory, true);
    }

    private void saveImageTransformerFactoryForDataset(long datasetId, String channel,
            IImageTransformerFactory transformerFactory)
    {
        if (isMergedChannel(channel))
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for dataset " + datasetId);
            }
            transformerDAO.saveTransformerFactoryForImageDataset(datasetId, transformerFactory);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for dataset " + datasetId + " and channel '" + channel + "'.");
            }
            long channelId = transformerDAO.getDatasetChannelId(datasetId, channel);
            createOrUpdateImageViewerTransformation(channelId, transformerFactory);
        }
    }

    @Override
    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        if (dataSetIdentifiers.size() == 1)
        {
            IDatasetIdentifier datasetIdentifier = dataSetIdentifiers.get(0);
            if (getDAO().hasDatasetChannels(datasetIdentifier.getPermId()))
            {
                return tryGetImageTransformerFactoryForDataset(datasetIdentifier, channel);
            }
        }
        Set<String> experimentPermIDs = getExperimentPermIDs(sessionToken, dataSetIdentifiers);
        if (experimentPermIDs.isEmpty())
        {
            throw new UserFailureException("No data set identifers specified.");
        }
        if (experimentPermIDs.size() > 1)
        {
            throw new UserFailureException("All data sets have to belong to the same experiment: "
                    + dataSetIdentifiers);
        }
        String experimentPermID = experimentPermIDs.iterator().next();
        return tryGetImageTransformerFactoryForExperiment(experimentPermID, channel);
    }

    private Set<String> getExperimentPermIDs(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers)
    {
        Set<String> experimentPermIDs = new HashSet<String>();
        for (IDatasetIdentifier dataSetIdentifier : dataSetIdentifiers)
        {
            AbstractExternalData dataSet =
                    getOpenBISService().tryGetDataSet(sessionToken,
                            dataSetIdentifier.getDatasetCode());
            if (dataSet == null)
            {
                throw new UserFailureException("Unkown data set " + dataSetIdentifier);
            }
            experimentPermIDs.add(dataSet.getExperiment().getPermId());
        }
        return experimentPermIDs;
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForDataset(
            IDatasetIdentifier datasetIdentifier, String channel)
    {
        ImgImageDatasetDTO dataset = getImagingImageDataset(datasetIdentifier);
        if (isMergedChannel(channel))
        {
            return dataset.tryGetImageTransformerFactory();
        } else
        {
            long channelId = getDAO().getDatasetChannelId(dataset.getId(), channel);
            return tryGetImageViewerTransformation(channelId);
        }
    }

    private IImageTransformerFactory tryGetImageViewerTransformation(long channelId)
    {
        ImgImageTransformationDTO transformation =
                getDAO().tryGetImageTransformation(channelId, IMAGE_VIEWER_TRANSFORMATION_CODE);
        if (transformation == null)
        {
            return null;
        }
        return transformation.getImageTransformerFactory();
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForExperiment(
            String experimentPermID, String channel)
    {
        if (isMergedChannel(channel))
        {
            return getDAO().tryGetExperimentByPermId(experimentPermID)
                    .tryGetImageTransformerFactory();
        } else
        {
            ImgChannelDTO channelDTO =
                    getDAO().tryGetChannelForExperimentPermId(experimentPermID, channel);
            assert channelDTO != null : String.format("No channel '%s' for experiment '%s'.",
                    channel, experimentPermID);
            return tryGetImageViewerTransformation(channelDTO.getId());
        }
    }

    private List<ImgAnalysisDatasetDTO> getAnalysisDatasets(
            List<? extends IDatasetIdentifier> datasetIdents)
    {
        String[] permIds = extractPermIds(datasetIdents);
        List<ImgAnalysisDatasetDTO> datasets = getDAO().listAnalysisDatasetsByPermId(permIds);

        if (datasets.size() == 0 && datasetIdents.size() > 0)
        {
            throw new UserFailureException(
                    "Couldn't find any analysis dataset for given datasets: "
                            + Arrays.asList(permIds));
        }
        return datasets;
    }

    private static <T extends AbstractImgIdentifiable> long[] extractIds(List<T> dataSets)
    {
        long[] ids = new long[dataSets.size()];
        for (int i = 0; i < ids.length; i++)
        {
            ids[i] = dataSets.get(i).getId();
        }
        return ids;
    }

    private static String[] extractPermIds(List<? extends IDatasetIdentifier> datasets)
    {
        String[] permIds = new String[datasets.size()];
        for (int i = 0; i < permIds.length; i++)
        {
            permIds[i] = datasets.get(i).getDatasetCode();
        }
        return permIds;
    }

    private ImgImageDatasetDTO getImagingImageDataset(IDatasetIdentifier datasetIdentifier)
    {
        ImgImageDatasetDTO dataset =
                getDAO().tryGetImageDatasetByPermId(datasetIdentifier.getDatasetCode());
        if (dataset == null)
        {
            throw new UserFailureException("Unknown data set: "
                    + datasetIdentifier.getDatasetCode());
        }
        return dataset;
    }

    private List<PlateImageReference> createPlateImageReferences(
            IImagingDatasetLoader imageAccessor, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel)
    {
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        if (wellPositions == null || wellPositions.isEmpty())
        {
            for (int i = 0; i < numberOfTiles; i++)
            {
                imageReferences.add(new PlateImageReference(i, channel, null, dataSetIdentifier));
            }
        } else
        {
            for (WellPosition wellPosition : wellPositions)
            {
                addImageReferencesForAllTiles(imageReferences, wellPosition.getWellRow(),
                        wellPosition.getWellColumn(), channel, dataSetIdentifier, numberOfTiles);
            }
        }
        return imageReferences;
    }

    private List<PlateImageReference> createPlateImageReferences(
            IImagingDatasetLoader imageAccessor, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, List<String> channels)
    {
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        if (wellPositions == null || wellPositions.isEmpty())
        {
            for (String channel : channels)
            {
                for (int i = 0; i < numberOfTiles; i++)
                {
                    imageReferences
                            .add(new PlateImageReference(i, channel, null, dataSetIdentifier));
                }
            }
        } else
        {
            for (WellPosition wellPosition : wellPositions)
            {
                for (String channel : channels)
                {
                    addImageReferencesForAllTiles(imageReferences, wellPosition.getWellRow(),
                            wellPosition.getWellColumn(), channel, dataSetIdentifier, numberOfTiles);
                }
            }
        }
        return imageReferences;
    }

    private int getNumberOfTiles(IImagingDatasetLoader imageAccessor)
    {
        ImageDatasetParameters imageParameters = imageAccessor.getImageParameters();
        int numberOfTiles = imageParameters.getTileRowsNum() * imageParameters.getTileColsNum();
        return numberOfTiles;
    }

    private void addImageReferencesForAllTiles(List<PlateImageReference> imageReferences,
            int wellRow, int wellColumn, String channel, IDatasetIdentifier dataset,
            int numberOfTiles)
    {
        for (int i = 0; i < numberOfTiles; i++)
        {
            imageReferences.add(new PlateImageReference(wellRow, wellColumn, i, channel, dataset));
        }
    }

    // throws exception if some datasets cannot be found
    private Map<String/* image or feature vector dataset code */, IImagingDatasetLoader> getImageDatasetsMap(
            String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String/* dataset code */, IImagingDatasetLoader> imageDatasetsMap =
                new HashMap<String, IImagingDatasetLoader>();
        for (PlateImageReference imageReference : imageReferences)
        {
            if (imageDatasetsMap.containsKey(imageReference.getDatasetCode()) == false)
            {
                IImagingDatasetLoader imageAccessor =
                        tryCreateImageLoader(sessionToken, imageReference.getDatasetCode());
                if (imageAccessor == null) // Check whether this is a feature vector data set
                {
                    final AbstractExternalData imageDataset =
                            tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
                    if (imageDataset != null)
                    {
                        imageAccessor = createImageLoader(sessionToken, imageDataset.getCode());
                    } else
                    {
                        throw UserFailureException.fromTemplate(
                                "Cannot find an image dataset for the reference: %s",
                                imageReference);
                    }
                }
                imageDatasetsMap.put(imageReference.getDatasetCode(), imageAccessor);
            }
        }
        return imageDatasetsMap;
    }

    private IHierarchicalContentNode tryGetImageContent(IImagingLoaderStrategy imageLoaderStrategy,
            final ImageChannelStackReference channelStackReference, String channelCode,
            Size thumbnailSizeOrNull, String singleChannelImageTransformationCodeOrNull,
            boolean convertToPng, boolean transform)
    {
        try
        {
            return ImageChannelsUtils.getImage(imageLoaderStrategy, channelStackReference,
                    channelCode, thumbnailSizeOrNull, singleChannelImageTransformationCodeOrNull,
                    convertToPng, transform);
        } catch (EnvironmentFailureException e)
        {
            operationLog.error("Error reading image.", e);
            return null; // no image found
        }
    }

    private ImageChannelStackReference getImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, MicroscopyImageReference imageReference)
    {
        return getMicroscopyImageChannelStackReference(imageAccessor, imageReference.getTile());
    }

    private static ImageChannelStackReference getImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, PlateImageReference imageRef)
    {
        int tile = imageRef.getTile();
        if (imageRef.getWellPosition() != null)
        {
            Location tileLocation = getTileLocation(imageAccessor, tile);
            Location wellLocation = asLocation(imageRef.getWellPosition());
            return ImageChannelStackReference.createHCSFromLocations(wellLocation, tileLocation);
        } else
        {
            return getMicroscopyImageChannelStackReference(imageAccessor, tile);
        }
    }

    private static ImageChannelStackReference getMicroscopyImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, int tileIx)
    {
        Location tileLocation = getTileLocation(imageAccessor, tileIx);
        return ImageChannelStackReference.createMicroscopyFromLocations(tileLocation);
    }

    private static Location getTileLocation(IImagingDatasetLoader imageAccessor, int tile)
    {
        return getTileLocation(tile, imageAccessor.getImageParameters().getTileColsNum());
    }

    // tile - start from 0
    private static Location getTileLocation(int tile, int tileColumnsNum)
    {
        int row = (tile / tileColumnsNum) + 1;
        int col = (tile % tileColumnsNum) + 1;
        return new Location(col, row);
    }

    private static Location asLocation(WellPosition wellPosition)
    {
        return new Location(wellPosition.getWellColumn(), wellPosition.getWellRow());
    }

    private Size tryAsSize(ImageSize thumbnailSizeOrNull)
    {
        if (thumbnailSizeOrNull == null)
        {
            return null;
        }
        return new Size(thumbnailSizeOrNull.getWidth(), thumbnailSizeOrNull.getHeight());
    }

    private IImagingDatasetLoader tryCreateImageLoader(String sessionToken, String datasetCode)
    {
        final IHierarchicalContent content = getHierarchicalContent(sessionToken, datasetCode);
        return tryCreateImageLoader(datasetCode, content, false);
    }

    private IImagingDatasetLoader createImageLoader(String sessionToken, String datasetCode)
    {
        final IHierarchicalContent content = getHierarchicalContent(sessionToken, datasetCode);
        return createImageLoader(datasetCode, content);
    }

    IImagingDatasetLoader createImageLoader(String dataSetCode, IHierarchicalContent content)
    {
        return tryCreateImageLoader(dataSetCode, content, true);
    }

    IImagingDatasetLoader tryCreateImageLoader(String dataSetCode, IHierarchicalContent content,
            boolean check)
    {
        IImagingDatasetLoader loader = HCSImageDatasetLoaderFactory.tryCreate(content, dataSetCode);
        if (check && loader == null)
        {
            throw new IllegalStateException(String.format(
                    "Dataset '%s' not found in the imaging database.", dataSetCode));
        }
        return loader;
    }

    private AbstractExternalData tryFindImageDataset(String sessionToken, String datasetCode)
    {
        AbstractExternalData dataset = tryGetDataSet(sessionToken, datasetCode);
        if (dataset == null)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode + " cannot be found.");
        }
        if (isImageDataset(dataset))
        {
            return dataset;
        }
        // it may be the feature dataset
        Collection<AbstractExternalData> parents = dataset.getParents();
        if (parents.size() > 1)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode
                    + " should have at most 1 parent, but has: " + parents.size());
        }
        if (parents.size() == 1)
        {
            AbstractExternalData parent = parents.iterator().next();
            if (isImageDataset(parent))
            {
                return parent;
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    private List<String> convertToBase64(InputStream stream)
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(stream);

        List<String> result = new ArrayList<String>();
        try
        {
            for (byte[] bytes = extractNextImage(imagesWriter); bytes.length > 0; bytes =
                    extractNextImage(imagesWriter))
            {
                result.add(Base64.encodeBytes(bytes));
            }
        } catch (IOException ex)
        {
            operationLog.error("Error reading image.", ex);
        }
        return result;
    }

    private byte[] extractNextImage(ConcatenatedFileOutputStreamWriter imagesWriter)
            throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imagesWriter.writeNextBlock(outputStream);
        return outputStream.toByteArray();
    }

    private boolean isImageDataset(AbstractExternalData dataset)
    {
        String datasetTypeCode = dataset.getDataSetType().getCode();
        return datasetTypeCode.matches(ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN)
                || datasetTypeCode
                        .matches(ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);
    }

    private List<ImgFeatureDefDTO> getFeatureDefinitions(
            List<? extends IDatasetIdentifier> featureDatasets)
    {
        List<ImgAnalysisDatasetDTO> dataSets = getAnalysisDatasets(featureDatasets);
        return getDAO().listFeatureDefsByDataSetIds(extractIds(dataSets));
    }

    private List<ImgFeatureDefDTO> getFeatureDefinitionsWithContained(
            List<? extends IDatasetIdentifier> featureDatasets)
    {
        IMetadataProvider metadataProvider =
                FeatureVectorLoaderMetadataProviderFactory
                        .createMetadataProviderFromFeatureVectors(getOpenBISService(),
                                featureDatasets);

        List<IDatasetIdentifier> featureDatasetsWithContained = new ArrayList<IDatasetIdentifier>();

        for (IDatasetIdentifier featureDataset : featureDatasets)
        {
            featureDatasetsWithContained.add(featureDataset);

            List<String> containedCodes =
                    metadataProvider.tryGetContainedDatasets(featureDataset.getDatasetCode());

            for (String containedCode : containedCodes)
            {
                featureDatasetsWithContained.add(new DatasetIdentifier(containedCode,
                        featureDataset.getDatastoreServerUrl()));
            }
        }

        return getFeatureDefinitions(featureDatasetsWithContained);
    }

    private IImagingReadonlyQueryDAO getDAO()
    {
        return dao;
    }

    @Override
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

}
