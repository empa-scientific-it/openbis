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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang3.ArrayUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NamedFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * Loades selected wells content: metadata and (if available) image dataset.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class WellContentLoader extends AbstractContentLoader
{
    private static final int MAX_NUMBERS_OF_MATERIALS = 1000;

    /**
     * Finds wells containing the specified material and belonging to the specified experiment. Loads wells metadata, but no information about
     * connected datasets.
     */
    public static List<WellContent> loadOnlyMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId experimentId)
    {
        WellContentLoader loader =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null);
        return loader.loadLocations(materialId, experimentId);
    }

    /** loads wells metadata, but no information about image or image analysis datasets */
    public static List<WellContent> loadOnlyMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId)
    {
        final WellContentLoader loader =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null);
        return loader.loadLocations(materialId);
    }

    /** loads wells metadata, but no information about image or image analysis datasets */
    public static List<WellContent> loadOnlyMetadataForProject(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId projectId)
    {
        final WellContentLoader loader =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null);
        return loader.loadLocationsForProject(materialId, projectId);
    }

    /**
     * Finds wells matching the specified criteria. containing the specified materials and belonging to the specified experiment. Loads wells content:
     * metadata and (if available) image dataset and feature vectors.
     */
    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            WellSearchCriteria materialCriteria)
    {
        long start = System.currentTimeMillis();
        WellContentLoader loader =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null);
        List<WellContent> locations = loader.loadLocations(materialCriteria);

        operationLog.info(String.format("[%d msec] Load %d locations.",
                (System.currentTimeMillis() - start), locations.size()));

        List<WellContent> withPropsAndDataSets =
                loader.enrichWithDatasets(locations,
                        materialCriteria.getAnalysisProcedureCriteria());
        List<WellContent> byAnalysisProcedure =
                loader.filterByAnalysisProcedure(withPropsAndDataSets,
                        materialCriteria.getAnalysisProcedureCriteria());
        List<WellContent> withFeatureVectors = loader.enrichWithFeatureVectors(byAnalysisProcedure);
        return withFeatureVectors;
    }

    /**
     * Finds wells containing the specified material and belonging to the specified experiment. Loads wells metadata and single image dataset for each
     * well. If there are many image datasets for the well, all but the first one are ignored. If there is no image dataset for the well, the whole
     * well is ignored.
     */
    public static List<WellReplicaImage> loadWithImages(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId experimentId, MaterialSummarySettings settings)
    {
        WellContentLoader loader =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null);
        List<WellContent> locations = loader.loadLocations(materialId, experimentId);
        locations = loader.enrichWithSingleImageDatasets(locations);
        return annotateWithReplicaLabels(locations, settings);
    }

    private static List<WellReplicaImage> annotateWithReplicaLabels(
            List<WellContent> wellsWithImages, MaterialSummarySettings settings)
    {
        ReplicateSequenceProvider replicaSequences =
                new ReplicateSequenceProvider(wellsWithImages,
                        settings.getBiologicalReplicatePropertyTypeCodes());
        List<WellReplicaImage> wellReplicaImages = new ArrayList<WellReplicaImage>();
        for (WellContent wellContent : wellsWithImages)
        {
            wellReplicaImages.add(annotateWithReplicaLabels(wellContent, replicaSequences));
        }
        return wellReplicaImages;
    }

    private static WellReplicaImage annotateWithReplicaLabels(WellContent wellContent,
            ReplicateSequenceProvider replicaSequences)
    {
        int technicalReplicaSequenceNumber =
                replicaSequences.getTechnicalReplicateSequence(wellContent);
        String biologicalReplicateLabel =
                replicaSequences.tryGetBiologicalReplicateLabel(wellContent);
        return new WellReplicaImage(wellContent, technicalReplicaSequenceNumber,
                biologicalReplicateLabel);
    }

    private List<WellContent> enrichWithSingleImageDatasets(List<WellContent> locations)
    {
        Set<PlateIdentifier> plateIdentifiers = extractPlates(locations);
        Map<Long, DatasetImagesReference> plateToDatasetReferenceMap =
                loadPlateToSingleImageDatasetMap(plateIdentifiers);
        return enrichWithSingleImageDatasets(locations, plateToDatasetReferenceMap);
    }

    private Map<Long, DatasetImagesReference> loadPlateToSingleImageDatasetMap(
            Set<PlateIdentifier> plateIdentifiers)
    {
        HCSImageDatasetLoader datasetsRetriever = createImageDatasetsRetriever(plateIdentifiers);
        Collection<AbstractExternalData> imageDatasets = datasetsRetriever.getImageDatasets();
        if (imageDatasets.isEmpty())
        {
            return new HashMap<Long, DatasetImagesReference>();
        }
        return createPlateToSingleDatasetReferenceMap(imageDatasets);
    }

    /** Note: locations without a corresponding image dataset are removed */
    private static List<WellContent> enrichWithSingleImageDatasets(List<WellContent> locations,
            Map<Long, DatasetImagesReference> plateToDatasetReferenceMap)
    {
        List<WellContent> wellContentsWithImageDatasets = new ArrayList<WellContent>();
        for (WellContent wellContent : locations)
        {
            Long plateId = wellContent.getPlate().getId();
            DatasetImagesReference imageDatasetReference = plateToDatasetReferenceMap.get(plateId);
            if (imageDatasetReference != null)
            {
                WellContent enrichedWellContent =
                        wellContent.cloneWithImageDatasets(imageDatasetReference, null);
                wellContentsWithImageDatasets.add(enrichedWellContent);
            }
        }
        return wellContentsWithImageDatasets;
    }

    // TODO 2011-04-04, Tomasz Pylak: here if the plate has more than one dataset assigned, we
    // take the first and ignore the rest. The clean solution would be to ensure somehow that each
    // plate has at most one image dataset (can be possible only when there will be abstraction
    // which groups raw/overview/thumbnail images into one dataset).
    private Map<Long, DatasetImagesReference> createPlateToSingleDatasetReferenceMap(
            Collection<AbstractExternalData> imageDatasets)
    {
        TableMap<Long, AbstractExternalData> plateToDatasetMap =
                new TableMap<Long/* plate id */, AbstractExternalData>(imageDatasets,
                        new IKeyExtractor<Long, AbstractExternalData>()
                            {
                                @Override
                                public Long getKey(AbstractExternalData externalData)
                                {
                                    return externalData.getSample().getId();
                                }
                            }, UniqueKeyViolationStrategy.KEEP_FIRST);

        return asDatasetImagesReferenceMap(plateToDatasetMap);
    }

    private Map<Long, DatasetImagesReference> asDatasetImagesReferenceMap(
            TableMap<Long, AbstractExternalData> plateToDatasetMap)
    {
        Map<String, ImageDatasetParameters> imageParams = loadImagesReport(plateToDatasetMap);
        Map<Long/* plate id */, DatasetImagesReference> plateToDatasetReferenceMap =
                new HashMap<Long, DatasetImagesReference>();
        for (Long plateId : plateToDatasetMap.keySet())
        {
            AbstractExternalData imageDataset = plateToDatasetMap.getOrDie(plateId);
            DatasetImagesReference imaageDatasetReference =
                    createDatasetImagesReference(imageDataset, imageParams);
            plateToDatasetReferenceMap.put(plateId, imaageDatasetReference);
        }
        return plateToDatasetReferenceMap;
    }

    /**
     * @return list of unique materials with codes or properties matching to the query. If the experiment is specified, only materials inside well
     *         locations connected through the plate to this specified experiment(s) will be returned.
     */
    public static List<Material> loadMaterials(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            WellSearchCriteria materialCriteria)
    {
        Iterable<WellContentQueryResult> locations =
                new WellContentLoader(session, businessObjectFactory, daoFactory, null)
                        .loadRawLocations(materialCriteria);
        Collection<Long> materialIds = extractMaterialIds(locations);
        return businessObjectFactory.createMaterialLister(session).list(
                ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }

    @SuppressWarnings("deprecation")
    private static Collection<Long> extractMaterialIds(Iterable<WellContentQueryResult> locations)
    {
        List<Long> materialIds = new ArrayList<Long>();
        for (WellContentQueryResult location : locations)
        {
            materialIds.add(location.material_content_id);
        }
        return materialIds;
    }

    private WellContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            IScreeningQuery screeningQuery)
    {
        super(session, businessObjectFactory, daoFactory, screeningQuery);
    }

    private List<WellContent> enrichWithWellProperties(List<WellContent> locations)
    {
        Map<Long /* id */, WellContent> wellContents = new HashMap<Long/* id */, WellContent>();
        for (WellContent wellContent : locations)
        {
            EntityReference wellReference = wellContent.getWell();
            if (wellReference != null)
            {
                wellContents.put(wellReference.getId(), wellContent);
            }
        }

        // load the wells with properties
        ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(wellContents.keySet());
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        List<Sample> wells = sampleLister.list(criteria);

        for (Sample well : wells)
        {
            WellContent content = wellContents.get(well.getId());
            content.setWellProperties(well.getProperties());
        }

        return locations;
    }

    private List<WellContent> enrichWithDatasets(List<WellContent> locations,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        long start = System.currentTimeMillis();

        FeatureVectorDatasetLoader datasetsRetriever =
                createFeatureVectorDatasetsRetriever(locations, analysisProcedureCriteria);
        Collection<AbstractExternalData> imageDatasets = datasetsRetriever.getImageDatasets();
        Collection<AbstractExternalData> featureVectorDatasets =
                datasetsRetriever.getFeatureVectorDatasets();

        operationLog.info(String.format("[%d msec] load datasets (%d image, %d fv).",
                (System.currentTimeMillis() - start), imageDatasets.size(),
                featureVectorDatasets.size()));
        start = System.currentTimeMillis();

        Map<String, ImageDatasetParameters> imageParams = loadImagesReport(imageDatasets);
        operationLog.info(String.format("[%d msec] loadImagesReport",
                (System.currentTimeMillis() - start)));

        Collection<AbstractExternalData> childlessImageDatasets =
                selectChildlessImageDatasets(imageDatasets, featureVectorDatasets);

        Map<Long/* plate id */, List<AbstractExternalData>> plateToChildlessImageDatasetMap =
                createPlateToDatasetMap(childlessImageDatasets);
        Map<Long/* plate id */, List<AbstractExternalData>> plateToFeatureVectoreDatasetMap =
                createPlateToDatasetMap(featureVectorDatasets);

        return enrichWithDatasets(locations, plateToChildlessImageDatasetMap,
                plateToFeatureVectoreDatasetMap, imageParams);
    }

    private FeatureVectorDatasetLoader createFeatureVectorDatasetsRetriever(
            List<WellContent> locations, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        Set<PlateIdentifier> plates = extractPlates(locations);
        return createFeatureVectorDatasetsRetriever(plates, analysisProcedureCriteria);
    }

    private static Collection<AbstractExternalData> selectChildlessImageDatasets(
            Collection<AbstractExternalData> imageDatasets, Collection<AbstractExternalData> featureVectorDatasets)
    {
        Collection<AbstractExternalData> childlessImageDatasets = new ArrayList<AbstractExternalData>();
        Set<String> parentImageDatasetCodes = extractParentDatasetCodes(featureVectorDatasets);
        for (AbstractExternalData imageDataset : imageDatasets)
        {
            if (parentImageDatasetCodes.contains(imageDataset.getCode()) == false)
            {
                childlessImageDatasets.add(imageDataset);
            }
        }
        return childlessImageDatasets;
    }

    private static Set<String> extractParentDatasetCodes(Collection<AbstractExternalData> datasets)
    {
        Set<String> codes = new HashSet<String>();
        for (AbstractExternalData dataset : datasets)
        {
            Collection<AbstractExternalData> parents = dataset.getParents();
            if (parents != null)
            {
                for (AbstractExternalData parent : parents)
                {
                    codes.add(parent.getCode());
                }
            }
        }
        return codes;
    }

    private static Set<PlateIdentifier> extractPlates(Collection<WellContent> locations)
    {
        Set<PlateIdentifier> plates = new HashSet<PlateIdentifier>();
        for (WellContent location : locations)
        {
            plates.add(PlateIdentifier.createFromPermId(location.getPlate().getPermId()));
        }
        return plates;
    }

    /**
     * Connects wells with datasets.
     */
    private static List<WellContent> enrichWithDatasets(List<WellContent> wellContents,
            Map<Long/* plate id */, List<AbstractExternalData>> plateToChildlessImageDatasetMap,
            Map<Long/* plate id */, List<AbstractExternalData>> plateToFeatureVectoreDatasetMap,
            Map<String, ImageDatasetParameters> imageParams)
    {
        final Map<AbstractExternalData, DatasetImagesReference> childlessImageDatasetsToImageReference =
                new HashMap<AbstractExternalData, DatasetImagesReference>();
        final Map<AbstractExternalData, DatasetImagesReference> featureVectorDatasetsToImageReference =
                new HashMap<AbstractExternalData, DatasetImagesReference>();
        final Map<Long, DatasetImagesReference> plateToSingleImageDatasetReference =
                new HashMap<Long, DatasetImagesReference>();
        final Map<AbstractExternalData, DatasetReference> featureVectorDatasetsToReference =
                new HashMap<AbstractExternalData, DatasetReference>();
        for (WellContent wellContent : wellContents)
        {
            Long plateId = wellContent.getPlate().getId();
            if (plateToSingleImageDatasetReference.containsKey(plateId))
            {
                continue;
            }
            final List<AbstractExternalData> featureVectoreDatasets =
                    plateToFeatureVectoreDatasetMap.get(plateId);
            final List<AbstractExternalData> childlessImageDatasets =
                    plateToChildlessImageDatasetMap.get(plateId);
            final DatasetImagesReference singleImageDatasetOrNull =
                    tryGetSingleImageDataset(childlessImageDatasets, imageParams);
            plateToSingleImageDatasetReference.put(plateId, singleImageDatasetOrNull);

            boolean onlySingleImageExists = false;
            if (featureVectoreDatasets != null)
            {
                for (AbstractExternalData featureVectoreDataset : featureVectoreDatasets)
                {
                    DatasetReference featureVectoreDatasetReference =
                            ScreeningUtils.createDatasetReference(featureVectoreDataset);
                    featureVectorDatasetsToReference.put(featureVectoreDataset,
                            featureVectoreDatasetReference);
                    DatasetImagesReference imagesDatasetReference =
                            tryGetImageDatasetReference(featureVectoreDataset, imageParams);
                    featureVectorDatasetsToImageReference.put(featureVectoreDataset,
                            imagesDatasetReference);
                    if (imagesDatasetReference == null && singleImageDatasetOrNull != null)
                    {
                        onlySingleImageExists = true;
                    }
                }
            }
            if (childlessImageDatasets != null && onlySingleImageExists == false)
            {
                for (AbstractExternalData childlessImageDataset : childlessImageDatasets)
                {
                    final DatasetImagesReference imagesDatasetReference =
                            createDatasetImagesReference(childlessImageDataset, imageParams);
                    childlessImageDatasetsToImageReference.put(childlessImageDataset,
                            imagesDatasetReference);
                }
            }
        }

        List<WellContent> wellsWithDatasets = new ArrayList<WellContent>();
        for (WellContent wellContent : wellContents)
        {
            Long plateId = wellContent.getPlate().getId();
            List<WellContent> clonedWellContents = new ArrayList<WellContent>();

            List<AbstractExternalData> featureVectoreDatasets =
                    plateToFeatureVectoreDatasetMap.get(plateId);
            List<AbstractExternalData> childlessImageDatasets =
                    plateToChildlessImageDatasetMap.get(plateId);
            DatasetImagesReference singleImageDatasetOrNull =
                    plateToSingleImageDatasetReference.get(plateId);
            boolean singleImageAlreadyUsed = false;

            if (featureVectoreDatasets != null)
            {
                for (AbstractExternalData featureVectorDataset : featureVectoreDatasets)
                {
                    DatasetReference featureVectorDatasetReference =
                            featureVectorDatasetsToReference.get(featureVectorDataset);
                    DatasetImagesReference imagesDatasetReference =
                            featureVectorDatasetsToImageReference.get(featureVectorDataset);
                    if (imagesDatasetReference == null && singleImageDatasetOrNull != null)
                    {
                        // If the plate has only one childless image dataset, then we assume that it
                        // must have been the one which has been analysed. We need such a heuristic
                        // because some analysis dataset may have no parent dataset assigned.
                        imagesDatasetReference = singleImageDatasetOrNull;
                        singleImageAlreadyUsed = true;
                    }
                    clonedWellContents.add(wellContent.cloneWithImageDatasets(
                            imagesDatasetReference, featureVectorDatasetReference));
                }
            }

            // there can be more than one dataset with images for each well - in such a case we will
            // have one well content duplicated for each dataset
            if (childlessImageDatasets != null && singleImageAlreadyUsed == false)
            {
                for (AbstractExternalData childlessImageDataset : childlessImageDatasets)
                {
                    DatasetImagesReference imagesDatasetReference =
                            childlessImageDatasetsToImageReference.get(childlessImageDataset);
                    clonedWellContents.add(wellContent.cloneWithImageDatasets(
                            imagesDatasetReference, null));
                }
            }
            // if there are no datasets for the well content, we add it without images
            if (clonedWellContents.isEmpty())
            {
                wellsWithDatasets.add(wellContent);
            } else
            {
                wellsWithDatasets.addAll(clonedWellContents);
            }
        }
        return wellsWithDatasets;
    }

    private List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets)
    {
        List<WellContent> enrichedWellContents = new ArrayList<WellContent>();
        Map<String/* dss code */, List<WellContent>> datastoreToWellContentsMap =
                createAnalysisDatastoreToWellContentsMap(wellsWithDatasets);
        for (Entry<String, List<WellContent>> entry : datastoreToWellContentsMap.entrySet())
        {
            String datastoreCode = entry.getKey();
            List<WellContent> oneDatastoreWellContents = entry.getValue();

            if (datastoreCode != null)
            {
                IHCSFeatureVectorLoader loader =
                        businessObjectFactory.createHCSFeatureVectorLoader(datastoreCode);
                oneDatastoreWellContents =
                        enrichWithFeatureVectors(oneDatastoreWellContents, loader);
            }
            enrichedWellContents.addAll(oneDatastoreWellContents);
        }
        return enrichedWellContents;
    }

    // Groups elements according to the datastore server to which the connected analysis dataset
    // belongs. At null key we store all elements which do not have the dataset assigned.
    private Map<String/* dss code */, List<WellContent>> createAnalysisDatastoreToWellContentsMap(
            List<WellContent> wellContents)
    {
        return GroupByMap.create(wellContents, new IGroupKeyExtractor<String, WellContent>()
            {
                @Override
                public String getKey(WellContent wellContent)
                {
                    DatasetReference featureVectorDataset =
                            wellContent.tryGetFeatureVectorDataset();
                    String datastoreCode =
                            (featureVectorDataset == null ? null : featureVectorDataset
                                    .getDatastoreCode());
                    return datastoreCode;
                }
            }).getMap();
    }

    // should be called for wells which have feature vector datasets from the same DSS
    private List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets,
            IHCSFeatureVectorLoader loader)
    {
        List<WellFeatureVectorReference> wellReferences = extractWellReferences(wellsWithDatasets);
        WellFeatureCollection<FeatureVectorValues> featureVectors =
                loader.fetchWellFeatureValuesIfPossible(session, wellReferences);
        return enrichWithFeatureVectors(wellsWithDatasets, featureVectors);
    }

    private static class FeaturesMetadata
    {
        private final String[] featureCodes;

        private final String[] featureLabels;

        public FeaturesMetadata(String[] featureCodes, String[] featureLabels)
        {
            this.featureCodes = featureCodes;
            this.featureLabels = featureLabels;
        }
    }

    private static List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets,
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        FeaturesMetadata featureMetadata = extractFeaturesMetadata(featureVectors);
        Map<WellFeatureVectorReference, FeatureVectorValues> refsToFeaturesMap =
                createReferencesToFeaturesMap(featureVectors);
        List<WellContent> enrichedWellContents = new ArrayList<WellContent>();
        for (WellContent wellContent : wellsWithDatasets)
        {
            WellContent enrichedWellContent =
                    enrichWithFeatureVectors(wellContent, refsToFeaturesMap, featureMetadata);
            enrichedWellContents.add(enrichedWellContent);
        }
        return enrichedWellContents;
    }

    private static FeaturesMetadata extractFeaturesMetadata(
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        int size = featureVectors.getFeatureCodesAndLabels().size();
        String[] featureCodes = new String[size];
        featureVectors.getFeatureCodes().toArray(featureCodes);

        String[] featureLabels = new String[size];
        featureVectors.getFeatureLabels().toArray(featureLabels);

        return new FeaturesMetadata(featureCodes, featureLabels);
    }

    private static WellContent enrichWithFeatureVectors(WellContent wellsWithDatasets,
            Map<WellFeatureVectorReference, FeatureVectorValues> refsToFeaturesMap,
            FeaturesMetadata featureMetadata)
    {
        WellFeatureVectorReference ref = tryAsWellReference(wellsWithDatasets);
        if (ref != null)
        {
            FeatureVectorValues featureVectorValues = refsToFeaturesMap.get(ref);
            if (featureVectorValues != null)
            {
                NamedFeatureVector fv =
                        new NamedFeatureVector(featureVectorValues.getFeatureValues(),
                                featureMetadata.featureCodes, featureMetadata.featureLabels);
                return wellsWithDatasets.cloneWithFeatureVector(fv);
            }
        }
        return wellsWithDatasets;
    }

    private static Map<WellFeatureVectorReference, FeatureVectorValues> createReferencesToFeaturesMap(
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        Map<WellFeatureVectorReference, FeatureVectorValues> map =
                new HashMap<WellFeatureVectorReference, FeatureVectorValues>();
        for (FeatureVectorValues featureVector : featureVectors.getFeatures())
        {
            map.put(featureVector.getFeatureVectorReference(), featureVector);
        }
        return map;
    }

    private static List<WellFeatureVectorReference> extractWellReferences(
            List<WellContent> wellsWithDatasets)
    {
        List<WellFeatureVectorReference> wellRefs = new ArrayList<WellFeatureVectorReference>();
        for (WellContent wellContent : wellsWithDatasets)
        {
            WellFeatureVectorReference ref = tryAsWellReference(wellContent);
            if (ref != null)
            {
                wellRefs.add(ref);
            }
        }
        return wellRefs;
    }

    private static WellFeatureVectorReference tryAsWellReference(WellContent wellContent)
    {
        WellFeatureVectorReference ref = null;
        WellLocation location = wellContent.tryGetLocation();
        DatasetReference featureVectorDataset = wellContent.tryGetFeatureVectorDataset();
        if (location != null && featureVectorDataset != null)
        {
            WellLocation pos = new WellLocation(location.getRow(), location.getColumn());
            ref = new WellFeatureVectorReference(featureVectorDataset.getCode(), pos);

        }
        return ref;
    }

    private static DatasetImagesReference tryGetSingleImageDataset(
            List<AbstractExternalData> childlessImageDatasets,
            Map<String, ImageDatasetParameters> imageParams)
    {
        if (childlessImageDatasets != null && childlessImageDatasets.size() == 1)
        {
            AbstractExternalData singleImageDataset = childlessImageDatasets.get(0);
            return createDatasetImagesReference(singleImageDataset, imageParams);
        } else
        {
            return null;
        }
    }

    private static DatasetImagesReference tryGetImageDatasetReference(
            AbstractExternalData featureVectoreDataset, Map<String, ImageDatasetParameters> imageParams)
    {
        Collection<AbstractExternalData> parents = featureVectoreDataset.getParents();
        if (parents != null && parents.size() == 1)
        {
            AbstractExternalData imageDataset = parents.iterator().next();
            return createDatasetImagesReference(imageDataset, imageParams);
        } else
        {
            return null;
        }
    }

    private static DatasetImagesReference createDatasetImagesReference(AbstractExternalData imageDataset,
            Map<String, ImageDatasetParameters> imageParams)
    {
        ImageDatasetParameters imageParameters = imageParams.get(imageDataset.getCode());
        if (imageParameters != null)
        {
            return DatasetImagesReference.create(
                    ScreeningUtils.createDatasetReference(imageDataset), imageParameters);
        } else
        {
            operationLog.error("Cannot find image parameters for dataset: "
                    + imageDataset.getCode() + ". It will not be displayed");
            return null;
        }
    }

    private static Map<Long/* sample id */, List<AbstractExternalData>> createPlateToDatasetMap(
            Collection<AbstractExternalData> datasets)
    {
        Map<Long, List<AbstractExternalData>> map = new HashMap<Long, List<AbstractExternalData>>();
        for (AbstractExternalData dataset : datasets)
        {
            Sample sample = dataset.getSample();
            if (sample != null)
            {
                Long sampleId = sample.getId();

                List<AbstractExternalData> plateDatasets = map.get(sampleId);
                if (plateDatasets == null)
                {
                    plateDatasets = new ArrayList<AbstractExternalData>();
                    map.put(sampleId, plateDatasets);
                }
                plateDatasets.add(dataset);
            }
        }
        return map;
    }

    // TODO 2011-04-04, Tomasz Pylak: inefficient, rewrite to use single queryfor all datasets
    private Map<String/* dataset code */, ImageDatasetParameters> loadImagesReport(
            Iterable<AbstractExternalData> imageDatasets)
    {
        List<ImageDatasetParameters> imageParameters = new ArrayList<ImageDatasetParameters>();
        for (AbstractExternalData dataSet : imageDatasets)
        {
            ImageDatasetParameters imageParams =
                    ScreeningUtils.tryLoadImageParameters(dataSet, businessObjectFactory);
            if (imageParams != null)
            {
                imageParameters.add(imageParams);
            }
        }
        return asDatasetToParamsMap(imageParameters);
    }

    private static Map<String/* dataset code */, ImageDatasetParameters> asDatasetToParamsMap(
            List<ImageDatasetParameters> imageParameters)
    {
        Map<String, ImageDatasetParameters> map = new HashMap<String, ImageDatasetParameters>();
        for (ImageDatasetParameters params : imageParameters)
        {
            map.put(params.getDatasetCode(), params);
        }
        return map;
    }

    private List<WellContent> loadLocations(WellSearchCriteria materialCriteria)
    {
        Iterable<WellContentQueryResult> locations = loadRawLocations(materialCriteria);
        return convert(locations);
    }

    private Iterable<WellContentQueryResult> loadRawLocations(WellSearchCriteria materialCriteria)
    {
        Iterable<WellContentQueryResult> locations;
        MaterialSearchCriteria materialSearchCriteria =
                materialCriteria.getMaterialSearchCriteria();

        ExperimentSearchCriteria experimentCriteria = materialCriteria.getExperimentCriteria();
        SingleExperimentSearchCriteria experimentOrNull = experimentCriteria.tryGetExperiment();
        BasicProjectIdentifier projectOrNull = experimentCriteria.tryGetProjectIdentifier();

        IScreeningQuery dao = getScreeningDAO();
        if (materialSearchCriteria.tryGetMaterialCodesOrProperties() != null)
        {
            MaterialSearchCodesCriteria codesCriteria =
                    materialSearchCriteria.tryGetMaterialCodesOrProperties();

            long start = System.currentTimeMillis();
            long[] materialIds = findMaterialIds(codesCriteria);

            operationLog.info(String.format(
                    "[%d msec] Finding %d materials for criteria '%s'. Result: %s",
                    (System.currentTimeMillis() - start), materialIds.length, codesCriteria,
                    abbreviate(materialIds, 100)));
            if (materialIds.length > MAX_NUMBERS_OF_MATERIALS)
            {
                throw new UserFailureException("More than " + MAX_NUMBERS_OF_MATERIALS
                        + " materials for criteria '" + codesCriteria + "' are found. "
                        + "Please restrict your search criteria.");
            }
            start = System.currentTimeMillis();

            if (experimentOrNull != null)
            {
                locations =
                        dao.getPlateLocationsForMaterialCodes(materialIds, codesCriteria
                                .getMaterialTypeCodes(), experimentOrNull.getExperimentId().getId());
            } else if (projectOrNull != null)
            {
                locations =
                        dao.getPlateLocationsForMaterialCodesInProject(materialIds,
                                codesCriteria.getMaterialTypeCodes(), projectOrNull.getSpaceCode(),
                                projectOrNull.getProjectCode());
            } else
            {
                locations =
                        dao.getPlateLocationsForMaterialCodes(materialIds,
                                codesCriteria.getMaterialTypeCodes());
            }
        } else if (materialSearchCriteria.tryGetMaterialId() != null)
        {
            long materialId = materialSearchCriteria.tryGetMaterialId().getId();
            if (experimentOrNull != null)
            {
                locations =
                        dao.getPlateLocationsForMaterialId(materialId, experimentOrNull
                                .getExperimentId().getId());
            } else if (projectOrNull != null)
            {
                locations =
                        dao.getPlateLocationsForMaterialId(materialId,
                                projectOrNull.getSpaceCode(), projectOrNull.getProjectCode());
            } else
            {
                locations = dao.getPlateLocationsForMaterialId(materialId);
            }
        } else
        {
            throw new IllegalStateException("unhandled materia search criteria: "
                    + materialSearchCriteria);
        }
        return locations;
    }

    private static String abbreviate(long[] values, int limit)
    {
        int realLimit;
        if (limit == -1)
        {
            realLimit = values.length;
        } else
        {
            realLimit = Math.min(limit, values.length);
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < realLimit; i++)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    // NOET: this ignores material types, it has to be filtered later
    private long[] findMaterialIds(MaterialSearchCodesCriteria codesCriteria)
    {
        List<String> materialTypeCodes = Arrays.asList(codesCriteria.getMaterialTypeCodes());
        List<MaterialTypePE> types =
                daoFactory.getEntityTypeDAO(
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL)
                        .listEntityTypes();
        Set<String> propertyCodes = new HashSet<String>();
        for (MaterialTypePE mt : types)
        {
            if (materialTypeCodes.contains(mt.getCode()))
            {
                for (MaterialTypePropertyTypePE mtpt : mt.getMaterialTypePropertyTypes())
                {
                    propertyCodes.add(mtpt.getPropertyType().getCode());
                }
            }
        }
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        ArrayList<DetailedSearchCriterion> listOfCriteria =
                new ArrayList<DetailedSearchCriterion>();
        for (String value : codesCriteria.getMaterialCodesOrProperties())
        {
            listOfCriteria.add(createCodeCriterion(value));
            listOfCriteria.add(createPropertyCriterion(value, propertyCodes));
        }
        criteria.setCriteria(listOfCriteria);
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);
        criteria.setUseWildcardSearchMode(codesCriteria.isExactMatchOnly());
        return ArrayUtils.toPrimitive(daoFactory
                .getHibernateSearchDAO()
                .searchForEntityIds(session.getUserName(), criteria,
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL)
                .toArray(new Long[0]));
    }

    private DetailedSearchCriterion createPropertyCriterion(String value,
            Set<String> allEntityPropertyCodes)
    {
        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAnyPropertyField(new ArrayList<String>(
                allEntityPropertyCodes)));
        criterion.setValue(value);
        return criterion;
    }

    private DetailedSearchCriterion createCodeCriterion(String code)
    {
        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField
                .createAttributeField(MaterialAttributeSearchFieldKind.CODE));
        criterion.setValue(code);
        return criterion;
    }

    private List<WellContent> loadLocations(TechId geneMaterialId, TechId experimentId)
    {
        DataIterator<WellContentQueryResult> locations =
                getScreeningDAO().getPlateLocationsForMaterialId(geneMaterialId.getId(),
                        experimentId.getId());

        return convert(locations);
    }

    private List<WellContent> loadLocations(TechId geneMaterialId)
    {
        DataIterator<WellContentQueryResult> locations =
                getScreeningDAO().getPlateLocationsForMaterialId(geneMaterialId.getId());
        return convert(locations);
    }

    private List<WellContent> loadLocationsForProject(TechId geneMaterialId, TechId projectId)
    {
        DataIterator<WellContentQueryResult> locations =
                getScreeningDAO().getPlateLocationsForMaterialAndProjectIds(geneMaterialId.getId(),
                        projectId.getId());
        return convert(locations);
    }

    private List<WellContent> convert(Iterable<WellContentQueryResult> queryResults)
    {
        List<WellContent> wellContents = convertAndRemoveDuplicateWells(queryResults);

        List<WellContent> withProperties = enrichWithWellProperties(wellContents);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        List<Material> containedMaterials = getMaterialsWithDuplicates(withProperties);
        materialLister.enrichWithProperties(containedMaterials);

        return wellContents;
    }

    /**
     * Return *all* material objects contained in the wells as list. The list can contained different objects representing the same entity in the
     * database, but we need the duplication to be able to populate an object graph with wells correctly.
     */
    private static List<Material> getMaterialsWithDuplicates(List<WellContent> wellLocations)
    {
        List<Material> materials = new ArrayList<Material>();
        for (WellContent wc : wellLocations)
        {
            materials.addAll(wc.getMaterialContents());
        }
        return materials;
    }

    private List<WellContent> convertAndRemoveDuplicateWells(
            Iterable<WellContentQueryResult> queryResults)
    {
        List<WellContent> wellContents = new ArrayList<WellContent>();
        Set<String> seenWellPermIds = new HashSet<String>();

        for (WellContentQueryResult queryResult : queryResults)
        {
            String permId = queryResult.well_perm_id;
            if (false == seenWellPermIds.contains(permId))
            {
                seenWellPermIds.add(permId);
                wellContents.add(convert(queryResult));
            }
        }
        return wellContents;
    }

    private static WellContent convert(WellContentQueryResult well)
    {
        WellLocation wellLocation =
                ScreeningUtils.tryCreateLocationFromMatrixCoordinate(well.well_code);
        EntityReference wellReference =
                new EntityReference(well.well_id, well.well_code, well.well_type_code,
                        EntityKind.SAMPLE, well.well_perm_id);
        EntityReference plate =
                new EntityReference(well.plate_id, well.plate_code, well.plate_type_code,
                        EntityKind.SAMPLE, well.getPlatePermId());

        return new WellContent(wellLocation, wellReference, plate, convertExperiment(well));
    }

    private static ExperimentReference convertExperiment(WellContentQueryResult loc)
    {
        return new ExperimentReference(loc.exp_id, loc.exp_perm_id, loc.exp_code,
                loc.exp_type_code, loc.proj_code, loc.space_code);
    }

    private List<WellContent> filterByAnalysisProcedure(List<WellContent> wells,
            AnalysisProcedureCriteria criteria)
    {
        if (criteria.isAllProcedures())
        {
            return wells;
        }
        ArrayList<WellContent> filtered = new ArrayList<WellContent>();
        for (WellContent well : wells)
        {
            String analysisProcedureCode =
                    well.tryGetFeatureVectorDataset() == null ? null : well
                            .tryGetFeatureVectorDataset().getAnalysisProcedure();
            if (criteria.matches(analysisProcedureCode))
            {
                filtered.add(well);
            }
        }

        return filtered;
    }

}
