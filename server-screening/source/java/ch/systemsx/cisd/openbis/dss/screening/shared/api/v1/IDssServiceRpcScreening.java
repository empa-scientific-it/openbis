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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.v1;

import java.io.InputStream;
import java.util.List;

import com.googlecode.jsonrpc4j.JsonRpcParam;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.PrivilegeLevel;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.authorization.DatasetIdentifierPredicate;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.authorization.SingleDataSetIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * Public DSS API for screening. Since version 1.2 features are no longer identified by a name but by a code. Previous client code still works but all
 * name will be normalized internally. Normalized means that the original code arguments turn to upper case and any symbol which isn't from A-Z or 0-9
 * is replaced by an underscore character. {@link FeatureVectorDataset} will provide feature codes and feature labels.
 * 
 * @author Tomasz Pylak
 */
// Non-compatible changes without consultation are forbidden.
public interface IDssServiceRpcScreening extends IRpcService
{

    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "screening-dss";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-datastore-server-screening-api-v1";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    /**
     * The major version of this service.
     */
    public static final int MAJOR_VERSION = 1;

    /**
     * For a given set of feature vector data sets provide the list of all available features. This is just the name of the feature. If for different
     * data sets different sets of features are available, provide the union of the features of all data sets.
     * 
     * @deprecated Use {@link #listAvailableFeatureCodes(String, List)} instead.
     */
    @Deprecated
    @DataSetAccessGuard
    public List<String> listAvailableFeatureNames(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * Return the codes of available feature lists for a given feature vector data set.
     */
    @MinimalMinorVersion(14)
    @DataSetAccessGuard
    public List<String> listAvailableFeatureLists(String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IFeatureVectorDatasetIdentifier featureDataset);

    /**
     * Return the feature codes of a specified feature list for a specified feature vector data set
     */
    @MinimalMinorVersion(13)
    @DataSetAccessGuard
    public List<String> getFeatureList(String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IFeatureVectorDatasetIdentifier featureDataset,
            String featureListCode);

    /**
     * For a given set of feature vector data sets provide the list of all available features. This is just the code of the feature. If for different
     * data sets different sets of features are available, provide the union of the features of all data sets.
     */
    @MinimalMinorVersion(2)
    @DataSetAccessGuard
    public List<String> listAvailableFeatureCodes(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of feature vector data sets provide the list of all available features. This contains the code, label and description of the
     * feature. If for different data sets different sets of features are available, provide the union of the features of all data sets.
     */
    @MinimalMinorVersion(9)
    @DataSetAccessGuard
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * Conceptually, for a given list of data well references (i.e. specified wells on specified feature vector data sets) and a set of features
     * (given by their name) provide the feature matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vector datasets. Each entry in this list corresponds to one well in one dataset.
     * 
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @DataSetAccessGuard
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<FeatureVectorDatasetReference> featureDatasets,
            List<String> featureCodes);

    /**
     * Conceptually, for a given list of dataset well references (i.e. specified wells on specified feature vector data sets) and a set of features
     * (given by their name) provide the feature matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vectors. Each entry in this list corresponds to one well in one dataset.
     * 
     * @return The list of {@link FeatureVectorWithDescription}s, each element corresponds to one of the <var>datasetWellReferences</var>. <b>Note
     *         that the order of the returned is <i>not</i> guaranteed to be the same as the order of the list <var>datasetWellReferences</var>. Use
     *         {@link FeatureVectorWithDescription#getDatasetWellReference()} to find the corresponding dataset / well.</b>
     * @since 1.1
     */
    @MinimalMinorVersion(1)
    @DataSetAccessGuard
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes);

    /**
     * Provide images for a given list of image references (specified by data set code, well position, channel and tile). The result is encoded into
     * one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>), where block-size is the block size in bytes encoded
     * as one long number. The number of blocks is equal to the number of specified references and the order of blocks corresponds to the order of
     * image references. If <code>convertToPng==true</code>, the images will be converted to PNG format before being shipped, otherwise they will be
     * shipped in the format that they are stored on the server.
     * 
     * @since 1.3
     */
    @MinimalMinorVersion(3)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences, boolean convertToPng);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, List, boolean)} but the result is a list of base64 encoded strings
     * that contain the image data.
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("convertToPng") boolean convertToPng);

    /**
     * Provide thumbnail images for a given list of image references (specified by data set code, well position, channel and tile). The result is
     * encoded into one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>), where block-size is the block size in
     * bytes encoded as one long number. The number of blocks is equal to the number of specified references and the order of blocks corresponds to
     * the order of image references.
     * <p>
     * If no thumbnails are stored for this data set and well positions, empty images (length 0) will be returned.
     * 
     * @since 1.6
     */
    @MinimalMinorVersion(6)
    @DataSetAccessGuard
    public InputStream loadThumbnailImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadThumbnailImages(String, List)} but the result is a list of base64 encoded strings
     * that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadThumbnailImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences);

    /**
     * Provide images (PNG encoded) for a given list of image references (given by data set code, well position, channel and tile). The result is
     * encoded into one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>)*, where block-size is the block size in
     * bytes encoded as one long number. The number of blocks is equal to the number of specified references and the order of blocks corresponds to
     * the order of image references. If <code>size</code> is specified, the images will be scaled conserving aspect ratio in order to fit into
     * specified size. Otherwise images of original size are delivered.
     * 
     * @since 1.4
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences, ImageSize size);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, List, ImageSize)} but the result is a list of base64 encoded
     * strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("size") ImageSize size);

    /**
     * Provide images for a given list of image references (given by data set code, well position, channel and tile). The result is encoded into one
     * stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>)*, where block-size is the block size in bytes encoded as
     * one long number. The number of blocks is equal to the number of specified references and the order of blocks corresponds to the order of image
     * references. The images will be converted to PNG format before being shipped.
     */
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, List)} but the result is a list of base64 encoded strings that
     * contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences);

    /**
     * Provide images for specified data set, list of well positions, channel, and optional thumb nail size. Images of all tiles are delivered. If
     * thumb nail size isn't specified the original image is delivered otherwise a thumb nail image with same aspect ratio as the original image but
     * which fits into specified size will be delivered.
     * <p>
     * The result is encoded into one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>)*, where block-size is the
     * block size in bytes encoded as one long number. The number of blocks is equal to the number of specified references and the order of blocks
     * corresponds to the order of image references. The images will be converted to PNG format before being shipped.
     * 
     * @since 1.4
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel,
            ImageSize thumbnailSizeOrNull);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, IDatasetIdentifier, List, String, ImageSize)} but the result is a
     * list of base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("wellPositions") List<WellPosition> wellPositions, @JsonRpcParam("channel") String channel,
            @JsonRpcParam("thumbnailSizeOrNull") ImageSize thumbnailSizeOrNull);

    /**
     * Provide images for specified microscopy data set, channel and optional thumb nail size. Images of all tiles are delivered. If thumb nail size
     * isn't specified the original image is delivered otherwise a thumb nail image with same aspect ratio as the original image but which fits into
     * specified size will be delivered.
     * <p>
     * Note that this method will not work for datasets connected to plates (in this case the wells would have to be specified additionally).
     * <p>
     * The result is encoded into one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>)*, where block-size is the
     * block size in bytes encoded as one long number. The number of blocks is equal to the number of specified references and the order of blocks
     * corresponds to the order of image references. The images will be converted to PNG format before being shipped.
     * 
     * @since 1.5
     */
    @MinimalMinorVersion(5)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IDatasetIdentifier dataSetIdentifier, String channel,
            ImageSize thumbnailSizeOrNull);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, IDatasetIdentifier, String, ImageSize)} but the result is a list
     * of base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("channel") String channel, @JsonRpcParam("thumbnailSizeOrNull") ImageSize thumbnailSizeOrNull);

    /**
     * Provide images for a given list of image references (specified by data set code, well position, channel and tile). The format and properties of
     * the returned images are configured by the configuration.
     * <p>
     * The options are described in {@link ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration}.
     * <p>
     * The encoding of the result is described in {@link IDssServiceRpcScreening#loadImages(String, List)}.
     * 
     * @see IDssServiceRpcScreening#loadImages(String, List)
     * @see ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration
     * @since 1.8
     */
    @MinimalMinorVersion(8)
    @DataSetAccessGuard
    public InputStream loadImages(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration);

    /**
     * Returns the same images as
     * {@link IDssServiceRpcScreening#loadImages(String, List, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration)}
     * but the result is a list of base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("configuration") ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration);

    /**
     * Provides images for the specified list of image references (specified by data set code, well position, channel and tile) and specified image
     * representation format. The {@link ImageRepresentationFormat} argument should be an object returned by
     * {@link #listAvailableImageRepresentationFormats(String, List)}. This method assumes that all image references belong to the same data set which
     * has image representations of specified format.
     * 
     * @throws UserFailureException if the specified format refers to an image representations unknown by at least one plate image reference.
     * @since 1.10
     */
    @MinimalMinorVersion(10)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, List, ImageRepresentationFormat)} but the result is a list of
     * base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("format") ImageRepresentationFormat format);

    /**
     * Provides images for the specified list of image references (specified by data set code, well position, channel and tile) and image selection
     * criteria. These criteria are applied to the {@link ImageRepresentationFormat} sets of each data set. Beside of the set of original images a
     * data set can have other image representations like thumbnails of various sizes. The provided array of
     * {@link IImageRepresentationFormatSelectionCriterion} are applied one after another onto the set of {@link ImageRepresentationFormat} until its
     * size is reduced to one.
     * 
     * @throws UserFailureException if no criterion has been specified (i.e. <code>criteria</code> is an empty array) or if for at least one data set
     *             the filtered {@link ImageRepresentationFormat} set has size zero or greater than one.
     * @since 1.10
     */
    @MinimalMinorVersion(10)
    @DataSetAccessGuard
    public InputStream loadImages(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadImages(String, List, IImageRepresentationFormatSelectionCriterion...)} but the
     * result is a list of base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("criteria") IImageRepresentationFormatSelectionCriterion... criteria);

    /**
     * Provide thumbnail images for specified microscopy data set. If no thumbnails are stored on the server, this method will return an empty stream.
     * Images of all tiles are delivered.
     * <p>
     * Note that this method will not work for datasets connected to plates (in this case the wells would have to be specified additionally).
     * <p>
     * The result is encoded into one stream, which consist of multiple blocks in a format: (<block-size><block-of-bytes>)*, where block-size is the
     * block size in bytes encoded as one long number. The number of blocks is equal to the number of specified references and the order of blocks
     * corresponds to the order of image references. The thumbnail images will be shipped exactly as stored, not conversion will be applied.
     * 
     * @since 1.6
     */
    @MinimalMinorVersion(6)
    @DataSetAccessGuard
    public InputStream loadThumbnailImages(String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IDatasetIdentifier dataSetIdentifier, List<String> channels);

    /**
     * Returns the same images as {@link IDssServiceRpcScreening#loadThumbnailImages(String, IDatasetIdentifier, List)} but the result is a list of
     * base64 encoded strings that contain the image data.
     * 
     * @since 1.11
     */
    @MinimalMinorVersion(11)
    @DataSetAccessGuard
    public List<String> loadThumbnailImagesBase64(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("channels") List<String> channels);

    /**
     * Lists plate image references for specified data set, list of well positions and channel.
     * 
     * @since 1.4
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard
    public List<PlateImageReference> listPlateImageReferences(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("wellPositions") List<WellPosition> wellPositions, @JsonRpcParam("channel") String channel);

    /**
     * Lists plate image references for specified data set, list of well positions and channels.
     * 
     * @since 1.6
     */
    @MinimalMinorVersion(6)
    @DataSetAccessGuard
    public List<PlateImageReference> listPlateImageReferences(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("wellPositions") List<WellPosition> wellPositions, @JsonRpcParam("channels") List<String> channels);

    /**
     * Lists microscopy image references for specified data set and channel.
     * 
     * @since 1.5
     */
    @MinimalMinorVersion(5)
    @DataSetAccessGuard
    public List<MicroscopyImageReference> listImageReferences(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("channel") String channel);

    /**
     * Lists microscopy image references for specified data set and channels.
     * 
     * @since 1.6
     */
    @MinimalMinorVersion(6)
    @DataSetAccessGuard
    public List<MicroscopyImageReference> listImageReferences(
            @JsonRpcParam("sessionToken") String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) @JsonRpcParam("dataSetIdentifier") IDatasetIdentifier dataSetIdentifier,
            @JsonRpcParam("channels") List<String> channels);

    /**
     * Saves the specified transformer factory for the specified channel of the specified data. Note that the channel can be stored at the dataset or
     * experiment level.
     * 
     * @since 1.4
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard(privilegeLevel = PrivilegeLevel.PROJECT_POWER_USER)
    public void saveImageTransformerFactory(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory);

    /**
     * Returns the transformer factory for the specified channel and the experiment to which the specified data sets belong. If there is exactly one
     * dataset identifier, checks first if channels are defined on the dataset level.
     * 
     * @return <code>null</code> if such a factory has been defined yet.
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard
    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<IDatasetIdentifier> dataSetIdentifiers, String channel);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and the available (natural) image size(s).
     */
    @DataSetAccessGuard
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IImageDatasetIdentifier> imageDatasets);

    /**
     * Return image representation formats available for the specified image data sets.
     * 
     * @param imageDatasets The image data sets for which the representation formats are requested.
     * @return A list with one entry for each in <b>imageDatasets</b>.
     * @since 1.10
     */
    @MinimalMinorVersion(10)
    @DataSetAccessGuard
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IDatasetIdentifier> imageDatasets);

    /**
     * /** Returns the same images as {@link IDssServiceRpcScreening#loadPhysicalThumbnails(String, List, ImageRepresentationFormat)} but the result
     * is a list of base64 encoded strings that contain the image data.
     * 
     * @since 1.12
     */
    @MinimalMinorVersion(12)
    @DataSetAccessGuard
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format);

    /**
     * The fast method to provide registered thumbnail images (without calculating them) for the specified list of image references (specified by data
     * set code, well position, channel and tile) and specified image representation format. The {@link ImageRepresentationFormat} argument should be
     * an object returned by {@link #listAvailableImageRepresentationFormats(String, List)}. This method assumes that all image references belong to
     * the same data set which has image representations of specified format.
     * <p>
     * This method gets the images directly from the data store in the format in which they are stored there.
     * 
     * @throws UserFailureException if the specified format refers to an image representations unknown by at least one plate image reference.
     * @since 1.12
     */
    @MinimalMinorVersion(12)
    @DataSetAccessGuard
    public InputStream loadPhysicalThumbnails(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format);
}
