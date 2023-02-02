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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.IDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.TypeBasedDataSetFilter;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * The service facade for the openBIS public remote API for screening and imaging.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IScreeningOpenbisServiceFacade
{

    /**
     * Return the session token for this authenticated user.
     */
    @Retry
    public String getSessionToken();

    /** Closes connection with the server. After calling this method this facade cannot be used. */
    public void logout();

    /**
     * Removes all images loaded by {@link #loadImageWellCaching(PlateImageReference, ImageSize)} and
     * {@link #loadThumbnailImageWellCaching(PlateImageReference)} from the image cache, thus freeing the memory.
     */
    public void clearWellImageCache();

    /**
     * Exposes material search functionaliy (used for ScreeningML)
     */
    @Retry
    public List<Material> searchForMaterials(SearchCriteria searchCriteria);

    /**
     * Exposes material search functionaliy (used for ScreeningML)
     */
    @Retry
    public List<Sample> searchForSamples(SearchCriteria searchCriteria);

    /**
     * Return the list of all visible plates assigned to any experiment, along with their hierarchical context (space, project, experiment).
     */
    @Retry
    public List<Plate> listPlates();

    /**
     * Return full metadata for each specified plate, including wells and their properties. If a well contains a material, its properties are also
     * available.
     */
    @Retry
    public List<PlateMetadata> getPlateMetadataList(List<? extends PlateIdentifier> plateIdentifiers);

    /**
     * Return the list of all plates for the given <var>experiment</var>.
     */
    @Retry
    public List<Plate> listPlates(ExperimentIdentifier experiment);

    /**
     * Return the list of all plates for the given <var>experiment</var> and analysis procedure. Each returned plate has at least one data set with
     * the specified analysis procedure.
     */
    @Retry
    public List<Plate> listPlates(ExperimentIdentifier experiment, String analysisProcedure);

    /**
     * Return the list of all visible experiments, along with their hierarchical context (space, project).
     */
    @Retry
    public List<ExperimentIdentifier> listExperiments();

    /**
     * Return the list of all experiments visible to user <var>userId</var>, along with their hierarchical context (space, project).
     * <p>
     * The user calling this method needs to have a role <code>INSTANCE_OBSERVER</code> on the openBIS instance.
     * 
     * @since 1.6
     */
    @Retry
    public List<ExperimentIdentifier> listExperiments(String userId);

    /**
     * For a given set of plates provides the list of all connected data sets containing feature vectors.
     */
    @Retry
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates);

    /**
     * For a given set of plates provides the list of all connected data sets containing feature vectors and having specified analysis procedure
     * property.
     * 
     * @param analysisProcedureOrNull If <code>null</code> returned list isn't filtered on analysis procedure property.
     */
    @Retry
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates, String analysisProcedureOrNull);

    /**
     * For a given set of plates provides the list of all connected data sets containing images which are not segmentation images.
     * 
     * @deprecated Use {@link #listRawImageDatasets(List)} instead.
     */
    @Retry
    @Deprecated
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates);

    /**
     * For a given set of plates provides the list of all connected data sets containing raw images.
     */
    @Retry
    public List<ImageDatasetReference> listRawImageDatasets(List<? extends PlateIdentifier> plates);

    /**
     * For a given set of plates provides the list of all connected data sets containing segmentation images (overlays).
     */
    @Retry
    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates);

    /**
     * For a given set of plates provides the list of all connected data sets containing segmentation images (overlays) and calculated by specified
     * analysis procedure.
     * 
     * @param analysisProcedureOrNull If <code>null</code> no restriction applies.
     */
    @Retry
    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates, String analysisProcedureOrNull);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to the specified <var>materialIdentifier</var>. If
     * <code>findDatasets == true</code>, find also the connected image and image analysis data sets for the relevant plates.
     * <p>
     * For how to get the feature vectors, see {@link #convertToFeatureVectorDatasetWellIdentifier(List)}.
     */
    @Retry
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets);

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to it. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     */
    @Retry
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            MaterialIdentifier materialIdentifier, boolean findDatasets);

    /**
     * For the given <var>plateIdentifier</var> find all wells that are connected to it.
     */
    @Retry
    public List<WellIdentifier> listPlateWells(PlateIdentifier plateIdentifier);

    /**
     * Returns all properties of specified plate as a map.
     */
    @Retry
    public Map<String, String> getPlateProperties(PlateIdentifier plateIdentifier);

    /**
     * Updates properties of specified plate.
     */
    public void updatePlateProperties(PlateIdentifier plateIdentifier, Map<String, String> properties);

    /**
     * Returns all properties of specified well as a map.
     */
    @Retry
    public Map<String, String> getWellProperties(WellIdentifier wellIdentifier);

    /**
     * Updates properties of specified well.
     */
    public void updateWellProperties(WellIdentifier wellIdentifier, Map<String, String> properties);

    /**
     * Gets proxies to the data sets owned by specified well.
     * 
     * @deprecated use {@link #getDataSets(WellIdentifier, IDataSetFilter)} with {@link TypeBasedDataSetFilter}.
     * @param datasetTypeCodePattern only datasets of the type which matche the specified pattern will be returned. To fetch all datasets specify
     *            ".*".
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Deprecated
    @Retry
    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier,
            String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Gets proxies to the data sets owned by specified well and passing specified filter..
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier, IDataSetFilter dataSetFilter)
            throws IllegalStateException, EnvironmentFailureException;

    @Retry
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Upload a new data set to the DSS for a well.
     * 
     * @param wellIdentifier Identifier of a well that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    public IDataSetDss putDataSet(WellIdentifier wellIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException;

    /**
     * Gets proxies to the data sets of the specified type owned by specified plate.
     * 
     * @deprecated use {@link #getDataSets(PlateIdentifier, IDataSetFilter)} with {@link TypeBasedDataSetFilter}.
     * @param datasetTypeCodePattern only datasets of the type which matche the specified pattern will be returned. To fetch all datasets specify
     *            ".*".
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Deprecated
    @Retry
    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Gets proxies to the data sets owned by specified plate and passing specified filter.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException;

    /**
     * A list of data sets owned by specified plate and passing specified filter. The data set objects provide metadata (e.g. code, properties etc.
     * from the openBIS AS) as well as data (e.g. files from openBIS DSS).
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<DataSet> getFullDataSets(PlateIdentifier plateIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException;

    /**
     * Gets proxies to the data sets owned by specified experiment and passing specified filter.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<IDataSetDss> getDataSets(ExperimentIdentifier plateIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException;

    /**
     * A list of data sets owned by specified experiment and passing specified filter. The data set objects provide metadata (e.g. code, properties
     * etc. from the openBIS AS) as well as data (e.g. files from openBIS DSS).
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<DataSet> getFullDataSets(ExperimentIdentifier experimentIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException;

    /**
     * Returns meta data for all specified data set codes. This contains data set type, properties, and codes of linked parent and children data sets.
     * 
     * @return result in the same order as the list of data set codes.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<DataSet> getDataSetMetaData(List<String> dataSetCodes)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Upload a new data set to the DSS for a plate.
     * 
     * @param plateIdentifier Identifier of a plate that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    public IDataSetDss putDataSet(PlateIdentifier plateIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException;

    /**
     * Upload a new data set to the DSS for a plate.
     * 
     * @param experimentIdentifier Identifier of a experiment that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    public IDataSetDss putDataSet(ExperimentIdentifier experimentIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException;

    /**
     * Converts the given list of {@link PlateWellReferenceWithDatasets} into a list of {@link FeatureVectorDatasetWellReference}.
     * 
     * @see #listPlateWells(ExperimentIdentifier, MaterialIdentifier, boolean)
     * @see #loadFeaturesForDatasetWellReferences(List, List)
     */
    @Retry
    public List<FeatureVectorDatasetWellReference> convertToFeatureVectorDatasetWellIdentifier(
            List<PlateWellReferenceWithDatasets> plateWellReferenceWithDataSets);

    /**
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API calls.
     */
    @Retry
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes);

    /**
     * For a given set of feature vector data sets provides the list of all available features. This is just the name of the feature. If for different
     * data sets different sets of features are available, provides the union of the feature names of all data sets.
     * 
     * @deprecated Use {@link #listAvailableFeatureCodes(List)} instead. Since version 1.2 of the API features are no longer identified by a name but
     *             by a code. Previous client code still works but the name will be normalized internally. "Normalized" means that the original name
     *             argument is converted to upper case and any symbol which isn't from A-Z or 0-9 is replaced by an underscore character. The
     *             {@link FeatureVectorDataset} will provide feature codes and feature labels.
     */
    @Deprecated
    @Retry
    public List<String> listAvailableFeatureNames(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of feature vector data sets provides the list of all available features. This is just the code of the feature. If for different
     * data sets different sets of features are available, provides the union of the feature names of all data sets.
     */
    @Retry
    public List<String> listAvailableFeatureCodes(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of feature vector data sets provide the list of all available features. This contains the code, label and description of the
     * feature. If for different data sets different sets of features are available, provide the union of the features of all data sets. Only
     * available when all data store services have minor version 9 or newer.
     */
    @Retry
    public List<FeatureInformation> listAvailableFeatures(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given feature vector data set return a list of available feature lists.
     */
    public List<String> listAvailableFeatureLists(IFeatureVectorDatasetIdentifier featureDataset);

    /**
     * Return the feature codes from the specified feature list of a specified feature data set
     */
    @Retry
    public List<String> getFeatureList(IFeatureVectorDatasetIdentifier featureDataset,
            String featureListCode);

    /**
     * For a given set of plates and a set of features (given by their code), provide all the feature vectors.
     * 
     * @param plates The plates to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @Retry
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            final List<String> featureCodesOrNull);

    /**
     * For a given set of plates and a set of features (given by their code), provide all the feature vectors created by specified analysis procedure.
     * 
     * @param plates The plates to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all available features should be loaded.
     * @param analysisProcedureOrNull If <code>null</code> result isn't restricted to any analysis procedure.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @Retry
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            final List<String> featureCodesOrNull, String analysisProcedureOrNull);

    /**
     * For a given set of data sets and a set of features (given by their code), provide all the feature vectors.
     * 
     * @param featureDatasets The data sets to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @Retry
    public List<FeatureVectorDataset> loadFeatures(
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodesOrNull);

    /**
     * Conceptually, for a given list of dataset well references (i.e. specified wells on specified feature vector data sets) and a set of features
     * (given by their code) provide the feature matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vectors. Each entry in this list corresponds to one well in one dataset.
     * 
     * @param datasetWellReferences The references for datasets / wells to get the feature vectors for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or <code>null</code>, if all available features should
     *            be included. Note that for an empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s, each element corresponds to one of the <var>datasetWellReferences</var>. <b>Note
     *         that the order of the returned list is <i>not</i> guaranteed to be the same as the order of the list <var>datasetWellReferences</var>.
     *         Use {@link FeatureVectorWithDescription#getDatasetWellReference()} to find the corresponding dataset / well.</b>
     */
    @Retry
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodesOrNull);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to the specified <var>materialIdentifier</var> and
     * load the feature vectors for the given feature code if not <code>null</code>, or all available features otherwise.
     * 
     * @deprecated use {@link #loadFeaturesForPlateWells(ExperimentIdentifier, MaterialIdentifier, String, List)} with third argument set to
     *             <code>null</code>.
     * @param experimentIdentifer The identifier of the experiment to get the feature vectors for
     * @param materialIdentifier The identifier of the material contained in the wells to get the feature vectors for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or <code>null</code>, if all available features should
     *            be included. Note that for an empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    @Deprecated
    @Retry
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            List<String> featureCodesOrNull);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to the specified <var>materialIdentifier</var> and
     * load the feature vectors for the given feature code if not <code>null</code>, or all available features otherwise. Do this only for data sets
     * with specified value of property <code>ANALYSIS_PROCEDURE</code>, if not <code>null</code>.
     * 
     * @param experimentIdentifer The identifier of the experiment to get the feature vectors for
     * @param materialIdentifier The identifier of the material contained in the wells to get the feature vectors for.
     * @param analysisProcedureOrNull If not <code>null</code> result is restricted to data sets with property <code>ANALYSIS_PROCEDURE</code> set to
     *            this value.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or <code>null</code>, if all available features should
     *            be included. Note that for an empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    @Retry
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            String analysisProcedureOrNull, List<String> featureCodesOrNull);

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to it and load the feature vectors for the given
     * feature code if not <code>null</code>, or all available features otherwise.
     * 
     * @deprecated use {@link #loadFeaturesForPlateWells(MaterialIdentifier, String, List)} with second argument set to <code>null</code>.
     * @param materialIdentifier The identifier of the material contained in the wells to get the feature vectors for.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or <code>null</code>, if all available features should
     *            be included. Note that for an empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    @Deprecated
    @Retry
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, List<String> featureCodesOrNull);

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to it and load the feature vectors for the given
     * feature code if not <code>null</code>, or all available features otherwise. Do this only for data sets with specified value of property
     * <code>ANALYSIS_PROCEDURE</code>, if not <code>null</code>.
     * 
     * @param materialIdentifier The identifier of the material contained in the wells to get the feature vectors for.
     * @param analysisProcedureOrNull If not <code>null</code> result is restricted to data sets with property <code>ANALYSIS_PROCEDURE</code> set to
     *            this value.
     * @param featureCodesOrNull The codes of the features to build the feature vectors from, or <code>null</code>, if all available features should
     *            be included. Note that for an empty list as well all features will be included.
     * @return The list of {@link FeatureVectorWithDescription}s found in the given <var>experimentIdentifer</var> and connected with the given
     *         <var>materialIdentifier</var>.
     */
    @Retry
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, String analysisProcedureOrNull,
            List<String> featureCodesOrNull);

    /**
     * Converts the given <var>WellIdentifiers</var> to <var>WellPositions</var>
     */
    @Retry
    public List<WellPosition> convertToWellPositions(List<WellIdentifier> wellIds);

    /**
     * Returns the list of all plate image references for the given <var>imageDatasetRef</var>.
     */
    @Retry
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef);

    /**
     * Returns the list of all plate image references for the given <var>imageDatasetRef</var> (all tiles), the given <var>channelCodesOrNull</var>
     * and <var>wellsOrNull</var>.
     * 
     * @param channelCodesOrNull The channel codes for which to create image references. If <code>null</code> or empty, references for all channels
     *            will be created.
     * @param wellsOrNull The wells to create image references for. If <code>null</code> or empty, references for all wells will be created.
     */
    @Retry
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, List<String> channelCodesOrNull,
            List<WellPosition> wellsOrNull);

    /**
     * Returns the list of all plate image references for the given <var>imageDatasetRef</var> (all tiles), the given <var>channelCodesOrNull</var>
     * and <var>wellsOrNull</var>.
     * 
     * @param metadataOrNull The metadata of the image dataset. If <code>null</code>, the metadata will be fetched from the server.
     * @param channelCodesOrNull The channel codes for which to create image references. If <code>null</code> or empty, references for all channels
     *            will be created.
     * @param wellsOrNull The wells to create image references for. If <code>null</code> or empty, references for all wells will be created.
     */
    @Retry
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsOrNull);

    /**
     * Returns the list of all plate image references for the given <var>imageDatasetId</var> (all tiles), and the given <var>channelCodesOrNull</var>
     * and <var>wellsToUse</var>.
     * 
     * @param channelCodesOrNull The channel codes for which to create image references. If <code>null</code> or empty, references for all channels
     *            will be created.
     * @param wellsToUse The wells to create image references for. Must not be <code>null</code>.
     */
    @Retry
    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetRef, List<String> channelCodesOrNull,
            List<WellPosition> wellsToUse);

    /**
     * Returns the list of all plate image references for the given <var>imageDatasetId</var> (all tiles), and the given <var>channelCodesOrNull</var>
     * and <var>wellsToUse</var>.
     * 
     * @param metadataOrNull The metadata of the image dataset. If <code>null</code>, the metadata will be fetched from the server.
     * @param channelCodesOrNull The channel codes for which to create image references. If <code>null</code> or empty, references for all channels
     *            will be created.
     * @param wellsToUse The wells to create image references for. Must not be <code>null</code>.
     */
    @Retry
    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetRef, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsToUse);

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) in the provided output streams.
     * Output streams will not be closed automatically.<br/>
     * The images will be converted to PNG format before being shipped.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IImageOutputStreamProvider outputStreamProvider) throws IOException;

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) in the provided output streams.
     * Output streams will not be closed automatically.<br/>
     * If <code>convertToPng==true</code>, the images will be converted to PNG format before being shipped, otherwise they will be shipped in the
     * format that they are stored on the server.<br/>
     * 
     * @throws IOException when reading images from the server or writing them to the output streams fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IImageOutputStreamProvider outputStreamProvider, boolean convertToPNG)
            throws IOException;

    /**
     * Loads images for a given list of image references (given by data set code, well position, channel and tile) and hands it over to the
     * <var>plateImageHandler</var>.<br/>
     * If <code>convertToPng==true</code>, the images will be converted to PNG format before being shipped, otherwise they will be shipped in the
     * format that they are stored on the server.<br/>
     * 
     * @param plateImageHandler handles delivered images.
     * @throws IOException when reading images from the server or writing them to the output streams fails
     */
    public void loadImages(List<PlateImageReference> imageReferences, boolean convertToPNG,
            IPlateImageHandler plateImageHandler) throws IOException;

    /**
     * Loads original images or thumbnails for a specified data set, a list of well positions (empty list means all wells), a channel, and an optional
     * thumb nail size. Images of all tiles are delivered. If thumb nail size isn't specified the original image is delivered otherwise a thumb nail
     * image with same aspect ratio as the original image but which fits into specified size will be delivered.
     * 
     * @return a list of byte arrays where each array contains a PNG encoded image.
     */
    @Retry
    public List<byte[]> loadImages(IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
            throws IOException;

    /**
     * Loads PNG-encoded images for specified data set, list of well positions (empty list means all wells), channel, and optional thumb nail size.
     * Images of all tiles are delivered. If thumb nail size isn't specified the original image is delivered otherwise a thumb nail image with same
     * aspect ratio as the original image but which fits into specified size will be delivered.
     * 
     * @param plateImageHandler handles delivered images.
     */
    public void loadImages(IDatasetIdentifier datasetIdentifier, List<WellPosition> wellPositions,
            String channel, ImageSize thumbnailSizeOrNull, IPlateImageHandler plateImageHandler)
            throws IOException;

    /**
     * Loads an PNG-encoded image for the specified image reference and, optionally, image size. If the image size isn't specified, the original image
     * is delivered, otherwise a scaled image with same aspect ratio as the original image but which fits into specified size will be delivered.
     */
    @Retry
    public byte[] loadImageWellCaching(final PlateImageReference imageReference,
            final ImageSize imageSizeOrNull) throws IOException;

    /**
     * Loads PNG-encoded images for specified image references and, optionally, image size. If the image size isn't specified, the original image is
     * delivered, otherwise a scaled image with same aspect ratio as the original image but which fits into specified size will be delivered.
     * 
     * @param plateImageHandler handles delivered images.
     */
    public void loadImages(List<PlateImageReference> imageReferences, ImageSize imageSizeOrNull,
            IPlateImageHandler plateImageHandler) throws IOException;

    /**
     * Loads images where the desired properties of the images are specified by a LoadImageConfiguration. The options and their behavior are described
     * in the {@link LoadImageConfiguration} documentation.
     * 
     * @param configuration The configuration of the images to load.
     * @param plateImageHandler Handler for the delivered images.
     * @see LoadImageConfiguration
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            LoadImageConfiguration configuration, IPlateImageHandler plateImageHandler)
            throws IOException;

    /**
     * Provides images for the specified list of image references (specified by data set code, well position, channel and tile) and specified image
     * representation format. The {@link ImageRepresentationFormat} argument should be an object returned by
     * {@link #listAvailableImageRepresentationFormats(List)}. This method assumes that all image references belong to the same data set which has
     * image representations of specified format.
     * 
     * @param plateImageHandler Handler for the delivered images.
     * @throws UserFailureException if the specified format refers to an image representations unknown by at least one plate image reference.
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IPlateImageHandler plateImageHandler, ImageRepresentationFormat format)
            throws IOException;

    /**
     * Provides images for the specified list of image references (specified by data set code, well position, channel and tile) and image selection
     * criteria. These criteria are applied to the {@link ImageRepresentationFormat} sets of each data set. Beside of the set of original images a
     * data set can have other image representations like thumbnails of various sizes. The provided array of
     * {@link IImageRepresentationFormatSelectionCriterion} are applied one after another onto the set of {@link ImageRepresentationFormat} until its
     * size is reduced to one.
     * 
     * @param plateImageHandler Handler for the delivered images.
     * @throws UserFailureException if no criterion has been specified (i.e. <code>criteria</code> is an empty array) or if for at least one data set
     *             the filtered {@link ImageRepresentationFormat} set has size zero or greater than one.
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IPlateImageHandler plateImageHandler,
            IImageRepresentationFormatSelectionCriterion... criteria) throws IOException;

    /**
     * Loads the PNG-encoded image for the specified <var>imageReference</var>.
     * <p>
     * This method triggers loading the thumbnail images for all tiles and all channels for the given well and image data set. It is a method to be
     * used in code that has to get one image at a time but eventually needs all images for a well and can increase performance of image loading
     * considerably.
     */
    @Retry
    public byte[] loadThumbnailImageWellCaching(final PlateImageReference imageReference)
            throws IOException;

    /**
     * Loads thumbnail images for specified data set, for a given list of image references (given by data set code, well position, channel and tile)
     * in the provided output streams. Output streams will not be closed automatically.<br/>
     * If no thumbnails are stored for this data set and well positions, empty images (length 0) will be returned.
     * 
     * @param plateImageHandler Handles delivered images.
     */
    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IPlateImageHandler plateImageHandler) throws IOException;

    /**
     * Loads thumbnail images for specified data set, for a given list of image references (given by data set code, well position, channel and tile)
     * in the provided output streams. Output streams will not be closed automatically.<br/>
     * If no thumbnails are stored for this data set and well positions, empty images (length 0) will be returned.
     * 
     * @param outputStreamProvider Handles delivered images.
     */
    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException;

    /**
     * Saves the specified transformer factory for the specified channel and the experiment to which the specified data sets belong.
     */
    public void saveImageTransformerFactory(List<IDatasetIdentifier> dataSetIdentifiers,
            String channel, IImageTransformerFactory transformerFactoryOrNull);

    /**
     * Returns the transformer factory for the specified channel and the experiment to which the specified data sets belong.
     * 
     * @return <code>null</code> if such a factory has been defined yet.
     */
    @Retry
    public IImageTransformerFactory getImageTransformerFactoryOrNull(
            List<IDatasetIdentifier> dataSetIdentifiers, String channel);

    /**
     * For a given image data set, provide meta like like which image channels have been acquired, what is the tile geometry, the available (natural)
     * image size(s) and the like.
     */
    @Retry
    public ImageDatasetMetadata listImageMetadata(IImageDatasetIdentifier imageDataset);

    /**
     * For a given set of image data sets, provide meta like like which image channels have been acquired, what is the tile geometry, the available
     * (natural) image size(s) and the like.
     */
    @Retry
    public List<ImageDatasetMetadata> listImageMetadata(
            List<? extends IImageDatasetIdentifier> imageDatasets);

    /**
     * For a given list of <var>plates</var>, return the mapping of plate wells to materials contained in each well.
     * 
     * @param plates The list of plates to get the mapping for
     * @param materialTypeIdentifierOrNull If not <code>null</code>, consider only materials of the given type for the mapping.
     * @return A list of well to material mappings, one element for each plate.
     */
    @Retry
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull);

    /**
     * Returns an alphabetically sorted list of analysis procedure codes of all data sets of the specified experiment.
     */
    @Retry
    public List<String> listAnalysisProcedures(ExperimentIdentifier experimentIdentifier);

    /**
     * Returns aggregated metadata for all images/plates within one experiment.
     */
    @Retry
    public ExperimentImageMetadata getExperimentImageMetadata(
            ExperimentIdentifier experimentIdentifier);

    /**
     * @return Information about the image representations available for the sepecified data sets.
     */
    @Retry
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            List<? extends IDatasetIdentifier> dataSetIdentifiers);

    /**
     * Retrieves images for the specified image references and invokes the plateImageHandler passing in image data. <br/>
     * If no images are stored for a particular combination of image reference and format, empty images (length 0) will be returned.
     * 
     * @param imageReferences The data set, well, channel, and tile of the image to be returned.
     * @param format The format of the image to return.
     * @param plateImageHandler Handles delivered images.
     */
    void loadPhysicalThumbnails(List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format, IPlateImageHandler plateImageHandler)
            throws IOException;

    /**
     * Retrieves images for the specified image references and invokes the IImageOutputStreamProvider passing in image data. <br/>
     * If no images are stored for a particular combination of image reference and format, empty images (length 0) will be returned.
     * 
     * @param imageReferences The data set, well, channel, and tile of the image to be returned.
     * @param format The format of the image to return.
     */
    void loadPhysicalThumbnails(List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format, IImageOutputStreamProvider outputStreamProvider)
            throws IOException;

}