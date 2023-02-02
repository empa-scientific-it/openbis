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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;

/**
 * This interface is a part of the official public screening API. It is forbidden to change it in a non-backward-compatible manner without discussing
 * it with all screening customers.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningApiServer extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "screening";

    /**
     * The major version of this service.
     */
    public static final int MAJOR_VERSION = 1;

    /**
     * Service part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-api-v1";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    /**
     * Authenticates the user with a given password.
     * 
     * @return sessionToken if authentication succeeded, <code>null</code> otherwise.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    String tryLoginScreening(String userId, String userPassword) throws IllegalArgumentException;

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    void logoutScreening(final String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all visible plates assigned to any experiment, along with their hierarchical context (space, project, experiment).
     */
    @Transactional(readOnly = true)
    List<Plate> listPlates(String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all plates assigned to the given experiment.
     * 
     * @since 1.5
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(5)
    List<Plate> listPlates(String sessionToken, ExperimentIdentifier experiment)
            throws IllegalArgumentException;

    /**
     * Fetches the contents of a given list of plates. The result will contain well and material properties.
     * 
     * @since 1.8
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(8)
    List<PlateMetadata> getPlateMetadataList(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException;

    /**
     * Return the list of all visible experiments, along with their hierarchical context (space, project).
     * 
     * @since 1.1
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(1)
    List<ExperimentIdentifier> listExperiments(String sessionToken);

    /**
     * Return the list of all experiments visible to user <var>userId</var>, along with their hierarchical context (space, project).
     * 
     * @since 1.6
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(6)
    List<ExperimentIdentifier> listExperiments(String sessionToken, String userId);

    /**
     * For a given set of plates (given by space / plate bar code), provide the list of all data sets containing feature vectors for each of these
     * plates.
     */
    @Transactional(readOnly = true)
    List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException;

    /**
     * For a given set of plates provide the list of all data sets containing images for each of these plates.
     */
    @Transactional(readOnly = true)
    List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException;

    /**
     * Returns information about logical image in the given image datasets.
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(11)
    public Map<String, LogicalImageInfo> getImageInfo(String sessionToken, List<String> datasetCode);

    /**
     * Returns information about available image resolutions for the given image datasets.
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(11)
    public Map<String, List<ImageResolution>> getImageResolutions(String sessionToken, List<String> datasetCode);

    /**
     * For a given set of plates provide the list of all data sets containing raw images for each of these plates.
     * 
     * @since 1.6
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(6)
    List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException;

    /**
     * For a given set of plates provide the list of all data sets containing segmentation images for each of these plates.
     * 
     * @since 1.6
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(6)
    List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException;

    /**
     * Converts a given list of dataset codes to dataset identifiers.
     */
    @Transactional(readOnly = true)
    List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken, List<String> datasetCodes);

    /**
     * For the given <var>experimentIdentifier</var>, find all plate locations that are connected to the specified <var>materialIdentifier</var>. If
     * <code>findDatasets == true</code>, find also the connected image and image analysis data sets for the relevant plates.
     * 
     * @since 1.1
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(1)
    List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets);

    /**
     * For the given <var>materialIdentifier</var>, find all plate locations that are connected to it. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     * 
     * @since 1.2
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(2)
    List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets);

    /**
     * For the given <var>plateIdentifier</var> find all wells that are connected to it.
     * 
     * @since 1.3
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(3)
    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier);

    /**
     * For a given <var>wellIdentifier</var>, return the corresponding {@link Sample} including properties.
     * 
     * @since 1.3
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(3)
    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier);

    /**
     * For a given <var>plateIdentifier</var>, return the corresponding {@link Sample}.
     * 
     * @since 1.7
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(7)
    public Sample getPlateSample(String sessionToken, PlateIdentifier plateIdentifier);

    /**
     * For a given list of <var>plates</var>, return the mapping of plate wells to materials contained in each well.
     * 
     * @param plates The list of plates to get the mapping for
     * @param materialTypeIdentifierOrNull If not <code>null</code>, consider only materials of the given type for the mapping.
     * @return A list of well to material mappings, one element for each plate.
     * @since 1.2
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(2)
    List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull);

    /**
     * Returns aggregated metadata for all images/plates within one experiment.
     * 
     * @since 1.9
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(1)
    ExperimentImageMetadata getExperimentImageMetadata(String sessionToken,
            ExperimentIdentifier experimentIdentifer);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#listAvailableFeatureCodes(String, List)} method for
     * each group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#listAvailableFeatures(String, List)} method for each
     * group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#loadFeatures(String, List, List)} method for each
     * group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#loadFeaturesForDatasetWellReferences(String, List, List)} method for each group of objects on appropriate data
     * store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#loadImagesBase64(String, List, boolean)} method for
     * each group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences, @JsonRpcParam("convertToPng") boolean convertToPng);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#loadThumbnailImagesBase64(String, List)} method for
     * each group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadThumbnailImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#loadImagesBase64(String, List, ImageSize)} method
     * for each group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences, @JsonRpcParam("size") ImageSize size);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#loadImagesBase64(String, List)} method for each
     * group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#loadImagesBase64(String, List, LoadImageConfiguration)} method for each group of objects on appropriate data
     * store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("configuration") LoadImageConfiguration configuration);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#loadImagesBase64(String, List, ImageRepresentationFormat)} method for each group of objects on appropriate data
     * store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences, @JsonRpcParam("format") ImageRepresentationFormat format);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#loadImagesBase64(String, List, IImageRepresentationFormatSelectionCriterion...)} method for each group of
     * objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadImagesBase64(@JsonRpcParam("sessionToken") String sessionToken,
            @JsonRpcParam("imageReferences") List<PlateImageReference> imageReferences,
            @JsonRpcParam("criteria") IImageRepresentationFormatSelectionCriterion... criteria);

    /**
     * Groups the specified objects by a data store code and calls {@link IDssServiceRpcScreening#listImageMetadata(String, List)} method for each
     * group of objects on appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#listAvailableImageRepresentationFormats(String, List)} method for each group of objects on appropriate data
     * store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets);

    /**
     * Groups the specified objects by a data store code and calls
     * {@link IDssServiceRpcScreening#loadPhysicalThumbnailsBase64(String, List, ImageRepresentationFormat)} method for each group of objects on
     * appropriate data store server. Results from the data stores are combined and returned as a result of this method.
     * 
     * @since 1.10
     */
    @Transactional(readOnly = true)
    @MinimalMinorVersion(10)
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format);

}
