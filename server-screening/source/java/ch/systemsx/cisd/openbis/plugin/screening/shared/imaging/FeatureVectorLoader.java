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
package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractImgIdentifiable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Builder for a table of feature vectors. After building a list of feature codes and a list of {@link FeatureTableRow}s are available. Feature
 * vectors are retrieved from {@link IImagingReadonlyQueryDAO}.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
public class FeatureVectorLoader
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FeatureVectorLoader.class);

    // stores all feature vectors of one dataset
    private static final class DatasetFeaturesBundle
    {
        private ImgAnalysisDatasetDTO dataSet;

        private String datasetContainerCode;

        private Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap;

        private FeatureVocabularyTermsMap featureDefToVocabularyTerms;

        private ImgContainerDTO plate;
    }

    public static interface IMetadataProvider
    {
        /**
         * Prefetches in a single call all sample identifiers for given perm ids. Calling this method before doing calls to
         * <code>getSampleIdentifiers</code> will greatly improve performance.
         */
        void getSampleIdentifiers(List<String> samplePermIds);

        /** fetches sample identifier from openBIS */
        SampleIdentifier tryGetSampleIdentifier(String samplePermId);

        List<String> tryGetContainedDatasets(String datasetCode);

    }

    private final IImagingReadonlyQueryDAO dao;

    // if null plate identifiers in FeatureTableRow are not set
    private final IMetadataProvider metadataProviderOrNull;

    private final List<DatasetFeaturesBundle> bundles;

    private final Map<CodeAndLabel, Integer> featureCodeLabelToIndexMap;

    private final Set<String> featureCodes;

    private final boolean useAllFeatures;

    // This is lazily initialized and is the same information as featureCodeLabelToIndexMap, but in
    // an array form.
    private CodeAndLabel[] codesAndLabels;

    /**
     * fetches specified features of specified wells
     * 
     * @param featureCodes empty list means no filtering.
     * @param metadataProviderOrNull if null plate identifiers in FeatureTableRow are not set
     * @throws UserFailureException if one of the specified datasets contains no feature vectors or does not exist.
     */
    public static WellFeatureCollection<FeatureTableRow> fetchWellFeatures(
            List<FeatureVectorDatasetWellReference> references, List<String> featureCodes,
            IImagingReadonlyQueryDAO dao, IMetadataProvider metadataProviderOrNull)
    {
        FeatureVectorLoader builder =
                new FeatureVectorLoader(featureCodes, dao, metadataProviderOrNull);
        Set<String> datasetCodes = extractDatasetCodes(references);
        builder.addFeatureVectorsOfDataSetsOrDie(datasetCodes);
        List<FeatureTableRow> features = builder.createFeatureTableRows(references);
        return new WellFeatureCollection<FeatureTableRow>(features, builder.getCodesAndLabels());
    }

    /**
     * fetches specified features of all wells in the specified dataset
     * 
     * @throws UserFailureException if the specified dataset contains no feature vectors or does not exist.
     */
    public static WellFeatureCollection<FeatureVectorValues> fetchWellFeatureCollection(
            List<String> datasetCodes, List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IMetadataProvider provider)
    {
        WellFeatureCollection<FeatureTableRow> features =
                fetchDatasetFeatures(datasetCodes, featureCodes, dao, provider);
        return asFeatureVectorValues(features);
    }

    private static WellFeatureCollection<FeatureVectorValues> asFeatureVectorValues(
            WellFeatureCollection<FeatureTableRow> featureRowsCollection)
    {
        List<FeatureTableRow> featureRows = featureRowsCollection.getFeatures();
        List<FeatureVectorValues> fvs = new ArrayList<FeatureVectorValues>();
        for (FeatureTableRow row : featureRows)
        {
            fvs.add(new FeatureVectorValues(row));
        }
        return new WellFeatureCollection<FeatureVectorValues>(fvs,
                featureRowsCollection.getFeatureCodesAndLabels());
    }

    /**
     * Fetches specified features of all wells.
     * 
     * @param featureCodes empty list means no filtering.
     * @param metadataProviderOrNull if null plate identifiers in FeatureTableRow are not set
     * @throws UserFailureException if one of the specified datasets contains no feature vectors or does not exist.
     */
    public static WellFeatureCollection<FeatureTableRow> fetchDatasetFeatures(
            List<String> datasetCodes, List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IMetadataProvider metadataProviderOrNull)
    {
        FeatureVectorLoader builder =
                new FeatureVectorLoader(featureCodes, dao, metadataProviderOrNull);
        builder.addFeatureVectorsOfDataSetsOrDie(datasetCodes);
        List<FeatureTableRow> features = builder.createFeatureTableRows();
        return new WellFeatureCollection<FeatureTableRow>(features, builder.getCodesAndLabels());
    }

    /**
     * Fetches all features of specified wells. Uses basic data types. If a reference to a dataset without any feature vectors is specified, it is
     * silently ignored.
     */
    public static WellFeatureCollection<FeatureVectorValues> fetchWellFeatureValuesIfPossible(
            List<WellFeatureVectorReference> references, IImagingReadonlyQueryDAO dao,
            IMetadataProvider metadataProvider)
    {
        FeatureVectorLoader builder =
                new FeatureVectorLoader(new ArrayList<String>(), dao, metadataProvider);
        Set<String> datasetCodes = extractDatasetCodesFromSimpleReferences(references);
        builder.addFeatureVectorsOfDataSetsIfPossible(datasetCodes);
        List<FeatureVectorValues> features =
                builder.createFeatureVectorValuesIfPossible(references);
        return new WellFeatureCollection<FeatureVectorValues>(features, builder.getCodesAndLabels());
    }

    /** stores feature vectors for a set of wells */
    public static class WellFeatureCollection<T extends FeatureVectorValues>
    {
        private final List<T> features;

        private final List<CodeAndLabel> featureNames;

        public WellFeatureCollection(List<T> features, List<CodeAndLabel> featureNames)
        {
            this.features = features;
            this.featureNames = featureNames;
        }

        public List<T> getFeatures()
        {
            return features;
        }

        /** codes and labels of all features in this collection */
        public List<CodeAndLabel> getFeatureCodesAndLabels()
        {
            return featureNames;
        }

        public List<String> getFeatureCodes()
        {
            List<String> codes = new ArrayList<String>();
            for (CodeAndLabel codeAndTitle : featureNames)
            {
                codes.add(codeAndTitle.getCode());
            }
            return codes;
        }

        public List<String> getFeatureLabels()
        {
            List<String> labels = new ArrayList<String>();
            for (CodeAndLabel codeAndTitle : featureNames)
            {
                labels.add(codeAndTitle.getLabel());
            }
            return labels;
        }
    }

    private void addFeatureVectorsOfDataSetsIfPossible(Collection<String> datasetCodes)
    {
        final List<ImgAnalysisDatasetDTO> dataSets = listAnalysisDatasetsByPermId(datasetCodes);
        if (dataSets.size() != datasetCodes.size())
        {
            operationLog.warn(createUnknownDatasetMessage(datasetCodes, dataSets));
        }
        addFeatureVectorsOfDataSets(dataSets);
    }

    private String createUnknownDatasetMessage(Collection<String> requestedDatasetCodes,
            final List<ImgAnalysisDatasetDTO> existingDataSets)
    {
        return String.format(
                "Some of the datasets are unknown! Requested datasets: %s. Found datasets: %s.",
                requestedDatasetCodes, existingDataSets);
    }

    private static Set<String> extractDatasetCodesFromSimpleReferences(
            List<WellFeatureVectorReference> references)
    {
        Set<String> datasetCodes = new HashSet<String>();
        for (WellFeatureVectorReference ref : references)
        {
            datasetCodes.add(ref.getDatasetCode());
        }
        return datasetCodes;
    }

    private static Set<String> extractDatasetCodes(
            List<FeatureVectorDatasetWellReference> references)
    {
        Set<String> datasetCodes = new HashSet<String>();
        for (FeatureVectorDatasetWellReference ref : references)
        {
            datasetCodes.add(ref.getDatasetCode());
        }
        return datasetCodes;
    }

    /**
     * Creates an instance for specified DAO and openBIS service but filters on specified features.
     * 
     * @param featureCodes empty list means no filtering.
     * @param metadataProviderOrNull if null info about plates will not be fetched from openBIS
     */
    FeatureVectorLoader(List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IMetadataProvider metadataProviderOrNull)
    {
        this.dao = dao;
        this.metadataProviderOrNull = metadataProviderOrNull;
        bundles = new ArrayList<DatasetFeaturesBundle>();
        featureCodeLabelToIndexMap = new LinkedHashMap<CodeAndLabel, Integer>();
        this.featureCodes = new LinkedHashSet<String>(featureCodes);
        this.useAllFeatures = featureCodes.isEmpty();
    }

    /**
     * Adds feature vectors for specified feature vector data set code.
     * 
     * @throws UserFailureException if dataset with the specified code contains no feature vectors or does not exist.
     */
    void addFeatureVectorsOfDataSetsOrDie(Collection<String> datasetCodes)
    {
        final List<ImgAnalysisDatasetDTO> dataSets = listAnalysisDatasetsByPermId(datasetCodes);
        if (dataSets.size() != datasetCodes.size())
        {
            throw new UserFailureException(createUnknownDatasetMessage(datasetCodes, dataSets));
        }
        addFeatureVectorsOfDataSets(dataSets);
    }

    private List<ImgAnalysisDatasetDTO> listAnalysisDatasetsByPermId(Collection<String> datasetCodes)
    {
        List<String> allCodes = new LinkedList<String>(datasetCodes);

        HashMap<String, String> containedToContainer = new HashMap<String, String>();

        if (metadataProviderOrNull != null)
        {
            for (String code : datasetCodes)
            {
                List<String> containedDataSetIds = metadataProviderOrNull.tryGetContainedDatasets(code);
                for (String contained : containedDataSetIds)
                {
                    containedToContainer.put(contained, code);
                }
                allCodes.addAll(containedDataSetIds);
            }
        }

        List<ImgAnalysisDatasetDTO> list = dao.listAnalysisDatasetsByPermId(allCodes.toArray(new String[0]));
        for (ImgAnalysisDatasetDTO imgAnalysisDatasetDTO : list)
        {
            imgAnalysisDatasetDTO.setDataSetContainerId(containedToContainer.get(imgAnalysisDatasetDTO.getPermId()));
        }
        return list;
    }

    /**
     * Adds feature vectors for specified feature vector data sets codes.
     */
    void addFeatureVectorsOfDataSets(List<ImgAnalysisDatasetDTO> datasets)
    {
        DatasetFeatureDefinitionCachedLister lister =
                new DatasetFeatureDefinitionCachedLister(datasets, featureCodes, useAllFeatures,
                        dao, metadataProviderOrNull);
        for (ImgAnalysisDatasetDTO dataset : datasets)
        {
            Map<String, ImgFeatureDefDTO> featureCodeToDefMap =
                    lister.getFeatureCodeToDefMap(dataset);

            if (useAllFeatures)
            {
                featureCodes.addAll(featureCodeToDefMap.keySet());
            }
            assignIndicesToFeatures(featureCodeToDefMap);

            DatasetFeaturesBundle bundle = new DatasetFeaturesBundle();
            bundle.featureDefToValuesMap = lister.getFeatureValues(dataset);
            bundle.dataSet = dataset;
            bundle.plate = lister.getContainer(dataset);
            bundle.featureDefToVocabularyTerms =
                    createFeatureIdToVocabularyTermsMap(dataset,
                            bundle.featureDefToValuesMap.keySet(), lister);
            bundles.add(bundle);
        }
    }

    private void assignIndicesToFeatures(final Map<String, ImgFeatureDefDTO> featureCodeToDefMap)
    {
        for (String featureCode : featureCodes)
        {
            final ImgFeatureDefDTO featureDefinition = featureCodeToDefMap.get(featureCode);
            if (featureDefinition != null)
            {
                CodeAndLabel codeAndLabel = asCodeAndLabel(featureDefinition);
                if (featureCodeLabelToIndexMap.containsKey(codeAndLabel) == false)
                {
                    featureCodeLabelToIndexMap.put(codeAndLabel, Integer.valueOf(
                            featureCodeLabelToIndexMap.size()));
                }
            }
        }
    }

    /**
     * Helper class which fetches all the results from the database at the beginning with one query, groups them and serves them from the cache.
     */
    private static class DatasetFeatureDefinitionCachedLister
    {
        private final GroupByMap<Long/* dataset id */, ImgFeatureDefDTO> requestedFeatureDefinitionsMap;

        private final GroupByMap<Long/* dataset id */, ImgFeatureVocabularyTermDTO> featureVocabularyTermsMap;

        // values for all datasets and requested features
        private final GroupByMap</* feature def id */Long, ImgFeatureValuesDTO> featureValuesMap;

        private final TableMap<Long/* plate id */, ImgContainerDTO> containersByIdMap;

        /**
         * @datasets datasets in which we are interested
         * @param featureCodes codes of features for which we want to fetch the values
         * @param useAllFeatures if true the featureCodes param is ignored and values are fetched for all features
         */
        public DatasetFeatureDefinitionCachedLister(List<ImgAnalysisDatasetDTO> datasets,
                Set<String> featureCodes, boolean useAllFeatures, IImagingReadonlyQueryDAO dao,
                IMetadataProvider provider)
        {
            this.containersByIdMap = createContainerByIdMap(datasets, dao);

            long[] datasetIds = extractIds(datasets);
            List<ImgFeatureDefDTO> requestedFeatureDefinitions =
                    listRequestedFeatureDefinitions(datasetIds, featureCodes, useAllFeatures, dao);
            this.requestedFeatureDefinitionsMap =
                    GroupByMap.create(requestedFeatureDefinitions,
                            new IGroupKeyExtractor<Long, ImgFeatureDefDTO>()
                                {
                                    @Override
                                    public Long getKey(ImgFeatureDefDTO featureDef)
                                    {
                                        return featureDef.getDataSetId();
                                    }
                                });

            List<ImgFeatureVocabularyTermDTO> featureVocabularyTerms =
                    dao.listFeatureVocabularyTermsByDataSetId(datasetIds);
            this.featureVocabularyTermsMap =
                    GroupByMap.create(featureVocabularyTerms,
                            new IGroupKeyExtractor<Long, ImgFeatureVocabularyTermDTO>()
                                {
                                    @Override
                                    public Long getKey(
                                            ImgFeatureVocabularyTermDTO featureVocabularyTerm)
                                    {
                                        return featureVocabularyTerm.getDataSetId();
                                    }
                                });

            List<ImgFeatureValuesDTO> requestedFeatureValues =
                    dao.getFeatureValues(extractIds(requestedFeatureDefinitions));
            this.featureValuesMap =
                    GroupByMap.create(requestedFeatureValues,
                            new IGroupKeyExtractor<Long, ImgFeatureValuesDTO>()
                                {
                                    @Override
                                    public Long getKey(ImgFeatureValuesDTO featureVal)
                                    {
                                        return featureVal.getFeatureDefId();
                                    }
                                });
        }

        private static TableMap<Long, ImgContainerDTO> createContainerByIdMap(
                List<ImgAnalysisDatasetDTO> datasets, IImagingReadonlyQueryDAO dao)
        {
            List<ImgContainerDTO> containers =
                    dao.listContainersByIds(extractContainerIds(datasets));
            return new TableMap<Long, ImgContainerDTO>(containers,
                    new IKeyExtractor<Long, ImgContainerDTO>()
                        {
                            @Override
                            public Long getKey(ImgContainerDTO container)
                            {
                                return container.getId();
                            }
                        });
        }

        private List<ImgFeatureDefDTO> listRequestedFeatureDefinitions(long[] datasetIds,
                Set<String> featureCodes, boolean useAllFeatures, IImagingReadonlyQueryDAO dao)
        {
            List<ImgFeatureDefDTO> allFeatureDefinitions =
                    dao.listFeatureDefsByDataSetIds(datasetIds);
            List<ImgFeatureDefDTO> requestedFeatureDefinitions =
                    extractRequestedFeatureDefinitions(featureCodes, useAllFeatures,
                            allFeatureDefinitions);
            return requestedFeatureDefinitions;
        }

        public ImgContainerDTO getContainer(ImgAnalysisDatasetDTO dataset)
        {
            return containersByIdMap.getOrDie(dataset.getContainerId());
        }

        public Map<String, ImgFeatureDefDTO> getFeatureCodeToDefMap(ImgAnalysisDatasetDTO dataset)
        {
            List<ImgFeatureDefDTO> featureDefinitions = getRequestedFeatureDefinitions(dataset);
            return createCodeToDefMap(featureDefinitions);
        }

        private List<ImgFeatureDefDTO> getRequestedFeatureDefinitions(ImgAnalysisDatasetDTO dataset)
        {
            List<ImgFeatureDefDTO> def = requestedFeatureDefinitionsMap.tryGet(dataset.getId());
            return def == null ? Collections.<ImgFeatureDefDTO> emptyList() : def;
        }

        public List<ImgFeatureVocabularyTermDTO> getFeatureVocabularyTerms(
                ImgAnalysisDatasetDTO dataSet)
        {
            List<ImgFeatureVocabularyTermDTO> terms =
                    featureVocabularyTermsMap.tryGet(dataSet.getId());
            if (terms == null)
            {
                return Collections.emptyList();
            }
            return terms;
        }

        public Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> getFeatureValues(
                ImgAnalysisDatasetDTO dataset)
        {
            List<ImgFeatureDefDTO> datasetFeatureDefinitions =
                    getRequestedFeatureDefinitions(dataset);
            Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> defToValuesMap =
                    new HashMap<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>>();
            for (ImgFeatureDefDTO featureDef : datasetFeatureDefinitions)
            {
                List<ImgFeatureValuesDTO> values = featureValuesMap.getOrDie(featureDef.getId());
                defToValuesMap.put(featureDef, values);
            }
            return defToValuesMap;
        }

        private static List<ImgFeatureDefDTO> extractRequestedFeatureDefinitions(
                Set<String> featureCodes, boolean useAllFeatures,
                List<ImgFeatureDefDTO> allFeatureDefinitions)
        {
            if (useAllFeatures)
            {
                return allFeatureDefinitions;
            } else
            {
                return filterByCode(allFeatureDefinitions, featureCodes);
            }
        }

        private static List<ImgFeatureDefDTO> filterByCode(
                List<ImgFeatureDefDTO> allFeatureDefinitions, Set<String> featureCodes)
        {
            List<ImgFeatureDefDTO> result = new ArrayList<ImgFeatureDefDTO>();
            for (ImgFeatureDefDTO featureDef : allFeatureDefinitions)
            {
                if (featureCodes.contains(featureDef.getCode()))
                {
                    result.add(featureDef);
                }
            }
            return result;
        }

        private static long[] extractContainerIds(List<ImgAnalysisDatasetDTO> datasets)
        {
            long[] ids = new long[datasets.size()];
            int i = 0;
            for (ImgAnalysisDatasetDTO dataset : datasets)
            {
                ids[i++] = dataset.getContainerId();
            }
            return ids;
        }

        private static long[] extractIds(List<? extends AbstractImgIdentifiable> identifiables)
        {
            long[] ids = new long[identifiables.size()];
            int i = 0;
            for (AbstractImgIdentifiable identifiable : identifiables)
            {
                ids[i++] = identifiable.getId();
            }
            return ids;
        }

        private static Map<String, ImgFeatureDefDTO> createCodeToDefMap(
                final List<ImgFeatureDefDTO> featureDefinitions)
        {
            final Map<String, ImgFeatureDefDTO> featureCodeToDefMap =
                    new LinkedHashMap<String, ImgFeatureDefDTO>();
            for (ImgFeatureDefDTO def : featureDefinitions)
            {
                featureCodeToDefMap.put(def.getCode(), def);
            }
            return featureCodeToDefMap;
        }
    }

    private static FeatureVocabularyTermsMap createFeatureIdToVocabularyTermsMap(
            ImgAnalysisDatasetDTO dataSet, Set<ImgFeatureDefDTO> datasetFeatureDefs,
            DatasetFeatureDefinitionCachedLister lister)
    {
        List<ImgFeatureVocabularyTermDTO> allTerms = lister.getFeatureVocabularyTerms(dataSet);
        return FeatureVocabularyTermsMap.createVocabularyTermsMap(allTerms, datasetFeatureDefs);
    }

    /**
     * Returns all feature codes/labels found. If the feature code list in the constructor is not empty the result will a list where the codes are a
     * subset of this list.
     */
    List<CodeAndLabel> getCodesAndLabels()
    {
        return Arrays.asList(getCodeAndLabelArray());
    }

    /**
     * Returns all features for previously loaded datasets. Features for all plate wells are returned.
     */
    List<FeatureTableRow> createFeatureTableRows()
    {
        List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();
        for (DatasetFeaturesBundle bundle : bundles)
        {
            ImgContainerDTO plate = bundle.plate;
            SampleIdentifier identifier = tryGetSampleIdentifier(plate);
            for (int rowIndex = 1; rowIndex <= plate.getNumberOfRows(); rowIndex++)
            {
                for (int colIndex = 1; colIndex <= plate.getNumberOfColumns(); colIndex++)
                {
                    final FeatureTableRow row =
                            createFeatureTableRow(bundle, identifier, null, new WellPosition(
                                    rowIndex, colIndex));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private SampleIdentifier tryGetSampleIdentifier(ImgContainerDTO container)
    {
        if (metadataProviderOrNull != null)
        {
            return metadataProviderOrNull.tryGetSampleIdentifier(container.getPermId());
        } else
        {
            return null;
        }
    }

    /**
     * Returns all features for the specified wells in previously loaded datasets. Operates on very basic data types. Ignores the references for which
     * no feature vectors exist.
     */
    private List<FeatureVectorValues> createFeatureVectorValuesIfPossible(
            List<WellFeatureVectorReference> references)
    {
        Map<String/* dataset code */, DatasetFeaturesBundle> bundleMap = createBundleMap(bundles);
        List<FeatureVectorValues> featureVectors = new ArrayList<FeatureVectorValues>();
        for (WellFeatureVectorReference reference : references)
        {
            String referenceDataSetCode = reference.getDatasetCode();

            List<String> allCodes = new LinkedList<String>();
            allCodes.add(referenceDataSetCode);

            if (metadataProviderOrNull != null)
            {
                allCodes.addAll(metadataProviderOrNull
                        .tryGetContainedDatasets(referenceDataSetCode));
            }

            for (String dataSetCode : allCodes)
            {
                DatasetFeaturesBundle bundle = bundleMap.get(dataSetCode);
                if (bundle != null)
                {
                    FeatureVectorValues featureVector =
                            createFeatureVector(bundle, reference.getWellLocation());
                    featureVectors.add(featureVector);
                }
            }
        }
        return featureVectors;
    }

    /**
     * Returns all features for the specified wells in previously loaded datasets.
     */
    private List<FeatureTableRow> createFeatureTableRows(
            List<FeatureVectorDatasetWellReference> references)
    {
        Map<String/* dataset code */, DatasetFeaturesBundle> bundleMap = createBundleMap(bundles);
        List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();
        for (FeatureVectorDatasetWellReference reference : references)
        {
            String dataSetCode = reference.getDatasetCode();
            DatasetFeaturesBundle bundle = getDatasetFeaturesBundleOrDie(bundleMap, dataSetCode);
            ImgContainerDTO plate = bundle.plate;
            SampleIdentifier identifier = tryGetSampleIdentifier(plate);
            final FeatureTableRow row =
                    createFeatureTableRow(bundle, identifier, reference,
                            reference.getWellPosition());
            rows.add(row);
        }
        return rows;
    }

    private DatasetFeaturesBundle getDatasetFeaturesBundleOrDie(
            Map<String, DatasetFeaturesBundle> bundleMap, String dataSetCode)
    {
        DatasetFeaturesBundle bundle = bundleMap.get(dataSetCode);
        if (bundle == null)
        {
            throw new IllegalStateException("Dataset has not been loaded: " + dataSetCode);
        }
        return bundle;
    }

    private static HashMap<String, DatasetFeaturesBundle> createBundleMap(
            List<DatasetFeaturesBundle> bundles)
    {
        HashMap<String, DatasetFeaturesBundle> map = new HashMap<String, DatasetFeaturesBundle>();
        for (DatasetFeaturesBundle bundle : bundles)
        {
            map.put(bundle.dataSet.getPermId(), bundle);
            if (bundle.dataSet.getDataSetContainerId() != null)
            {
                map.put(bundle.dataSet.getDataSetContainerId(), bundle);
            }
        }
        return map;
    }

    private FeatureTableRow createFeatureTableRow(DatasetFeaturesBundle bundle,
            SampleIdentifier identifierOrNull, FeatureVectorDatasetWellReference reference,
            WellPosition wellPosition)
    {
        FeatureVectorValues featureVector = createFeatureVector(bundle, convert(wellPosition));
        FeatureTableRow row = new FeatureTableRow(featureVector);
        row.setPlateIdentifier(identifierOrNull);
        row.setReference(reference);
        return row;
    }

    private static WellLocation convert(WellPosition wellPosition)
    {
        return new WellLocation(wellPosition.getWellRow(), wellPosition.getWellColumn());
    }

    private FeatureVectorValues createFeatureVector(DatasetFeaturesBundle bundle,
            WellLocation wellLocation)
    {
        String permId = bundle.dataSet.getPermId();

        FeatureValue[] valueArray =
                createFeatureValueArray(bundle.featureDefToValuesMap,
                        bundle.featureDefToVocabularyTerms, wellLocation);
        return new FeatureVectorValues(permId, wellLocation, bundle.plate.getPermId(),
                getCodeAndLabelArray(), valueArray);
    }

    private CodeAndLabel[] getCodeAndLabelArray()
    {
        // Return the value if has already been initialized
        if (codesAndLabels != null)
        {
            return codesAndLabels;
        }

        // Lazily initialize it.
        codesAndLabels = new CodeAndLabel[featureCodeLabelToIndexMap.size()];
        for (CodeAndLabel codeAndLabel : featureCodeLabelToIndexMap.keySet())
        {
            codesAndLabels[featureCodeLabelToIndexMap.get(codeAndLabel)] = codeAndLabel;
        }

        return codesAndLabels;
    }

    private FeatureValue[] createFeatureValueArray(
            Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap,
            FeatureVocabularyTermsMap featureDefToVocabularyTerms, WellLocation wellLocation)
    {
        FeatureValue[] valueArray = new FeatureValue[featureCodeLabelToIndexMap.size()];
        for (Entry<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> entry : featureDefToValuesMap
                .entrySet())
        {
            ImgFeatureDefDTO featureDefinition = entry.getKey();
            List<ImgFeatureValuesDTO> featureValueSets = entry.getValue();
            // We take only the first set of feature value sets
            ImgFeatureValuesDTO featureValueDTO = featureValueSets.get(0);
            PlateFeatureValues featureValues = featureValueDTO.getValues();
            if (wellLocation.getRow() > featureValues.getGeometry().getNumberOfRows()
                    || wellLocation.getColumn() > featureValues.getGeometry().getNumberOfColumns())
            {
                break;
            }
            Integer index = featureCodeLabelToIndexMap.get(asCodeAndLabel(featureDefinition));
            assert index != null : "No index for feature " + featureDefinition.getCode();
            float floatValue =
                    featureValues.getForWellLocation(wellLocation.getRow(),
                            wellLocation.getColumn());
            valueArray[index] =
                    createFeatureValue(floatValue, featureDefinition, featureDefToVocabularyTerms);
        }
        fillEmptyValues(valueArray, featureDefToVocabularyTerms);
        return valueArray;
    }

    // NOTE: for a fixed dataset any feature vector element [i] will have the same type (vocabulary
    // or float).
    // This can be not true for vectors of 2 different datasets, although the length and feature
    // codes will match. It means that the type of the feature in all datasets cannot be determined
    // by looking at one value.
    private void fillEmptyValues(FeatureValue[] valueArray,
            FeatureVocabularyTermsMap featureDefToVocabularyTerms)
    {
        for (CodeAndLabel featureName : featureCodeLabelToIndexMap.keySet())
        {
            Integer index = featureCodeLabelToIndexMap.get(featureName);
            if (valueArray[index] == null)
            {
                FeatureValue emptyFeatureValue;
                if (featureDefToVocabularyTerms.hasVocabularyTerms(featureName.getCode()))
                {
                    emptyFeatureValue = FeatureValue.createEmptyVocabularyTerm();
                } else
                {
                    emptyFeatureValue = FeatureValue.createEmptyFloat();
                }
                valueArray[index] = emptyFeatureValue;
            }
        }
    }

    private static FeatureValue createFeatureValue(float floatValue,
            ImgFeatureDefDTO featureDefinition,
            FeatureVocabularyTermsMap featureDefToVocabularyTerms)
    {
        long featureDefId = featureDefinition.getId();
        if (featureDefToVocabularyTerms.hasVocabularyTerms(featureDefId))
        {
            if (Float.isNaN(floatValue))
            {
                return FeatureValue.createEmptyVocabularyTerm();
            } else
            {
                return featureDefToVocabularyTerms
                        .getVocabularyTerm(featureDefId, (int) floatValue);
            }
        } else
        {
            return FeatureValue.createFloat(floatValue);
        }
    }

    private static CodeAndLabel asCodeAndLabel(final ImgFeatureDefDTO featureDefinition)
    {
        return new CodeAndLabel(featureDefinition.getCode(), featureDefinition.getLabel());
    }

}
