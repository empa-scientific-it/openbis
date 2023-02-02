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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.AnalysisProcedureResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

/**
 * Helper methods to operate on screening specific objects.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningUtils
{
    public static String labelTextPropertyName;

    private static String getLabelTextPropertyName()
    {
        if (labelTextPropertyName == null)
        {
            WebClientConfigurationProvider provider =
                    (WebClientConfigurationProvider) CommonServiceProvider.tryToGetBean(ResourceNames.WEB_CLIENT_CONFIGURATION_PROVIDER);

            labelTextPropertyName =
                    provider.getWebClientConfiguration().getPropertyOrNull(ScreeningConstants.TECHNOLOGY_NAME, "data-set-label-property-code");
            if (labelTextPropertyName == null)
            {
                labelTextPropertyName = "";
            }
        }
        return labelTextPropertyName;
    }

    public static WellLocation tryCreateLocationFromMatrixCoordinate(String wellCode)
    {
        Location loc = Location.tryCreateLocationFromTransposedMatrixCoordinate(wellCode);
        if (loc == null)
        {
            return null;
        } else
        {
            return new WellLocation(loc.getY(), loc.getX());
        }
    }

    public static DatasetReference createDatasetReference(DataPE dataset, String baseIndexURL,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        DataStore dataStore = DataStoreTranslator.translate(dataset.getDataStore());
        String dataTypeCode = dataset.getDataSetType().getCode();
        String fileTypeCode = null;
        Experiment experiment =
                ExperimentTranslator.translate(dataset.getExperiment(), baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        String analysisProcedureOrNull = EntityHelper.tryFindPropertyValue(dataset, ScreeningConstants.ANALYSIS_PROCEDURE);

        String labelText = EntityHelper.tryFindPropertyValue(dataset, getLabelTextPropertyName());

        return createDatasetReference(dataset.getId(), dataset.getCode(), analysisProcedureOrNull,
                dataStore, dataTypeCode, dataset.getRegistrationDate(), fileTypeCode, experiment, labelText);
    }

    public static DatasetReference createDatasetReference(AbstractExternalData dataset)
    {
        DataStore dataStore = dataset.getDataStore();
        String dataTypeCode = dataset.getDataSetType().getCode();
        String fileTypeCode = tryGetFileTypeCode(dataset);
        Experiment experiment = dataset.getExperiment();
        String analysisProcedureOrNull = EntityHelper.tryFindPropertyValue(dataset, ScreeningConstants.ANALYSIS_PROCEDURE);
        String labelText = EntityHelper.tryFindPropertyValue(dataset, getLabelTextPropertyName());
        return createDatasetReference(dataset.getId(), dataset.getCode(), analysisProcedureOrNull,
                dataStore, dataTypeCode, dataset.getRegistrationDate(), fileTypeCode, experiment, labelText);
    }

    private static String tryGetFileTypeCode(AbstractExternalData abstractDataSet)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet dataSet =
                abstractDataSet.tryGetAsDataSet();
        if (dataSet != null && dataSet.getFileFormatType() != null)
        {
            String fileTypeCode = dataSet.getFileFormatType().getCode();
            if (false == ScreeningConstants.UNKNOWN_FILE_FORMAT.equalsIgnoreCase(fileTypeCode))
            {
                return fileTypeCode;
            }
        }
        return null;
    }

    private static DatasetReference createDatasetReference(long datasetId, String datasetCode,
            String analysisProcedureOrNull, DataStore dataStore, String dataTypeCode,
            Date registrationDate, String fileTypeCode, Experiment experiment, String labelText)
    {
        return new DatasetReference(datasetId, datasetCode, dataTypeCode, registrationDate,
                fileTypeCode, dataStore.getCode(), dataStore.getHostUrl(), experiment.getPermId(),
                experiment.getIdentifier(), analysisProcedureOrNull, labelText);
    }

    public static <T extends DataPE> List<T> filterImageAnalysisDatasetsPE(List<T> datasets)
    {
        return filterDatasetsByTypePattern(datasets,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN);
    }

    public static List<AbstractExternalData> filterImageAnalysisDatasets(List<AbstractExternalData> datasets)
    {
        return filterExternalDataByTypePattern(datasets,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN);
    }

    /** @return unique set of analysis procedures which produced numerical analysis datasets. */
    public static AnalysisProcedures filterNumericalDatasetsAnalysisProcedures(
            List<AnalysisProcedureResult> analysisProcedureResults)
    {
        Set<String> uniqueAnalysisProcedures = new HashSet<String>();
        for (AnalysisProcedureResult analysisProcedureResult : analysisProcedureResults)
        {
            if (isMatching(analysisProcedureResult,
                    ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN))
            {
                uniqueAnalysisProcedures.add(analysisProcedureResult.analysisProcedure);
            }
        }
        return new AnalysisProcedures(uniqueAnalysisProcedures);
    }

    public static <T extends DataPE> List<T> filterImageOverlayDatasets(Collection<T> datasets)
    {
        return filterNonContainedDatasets(datasets,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.MICROSCOPY_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
    }

    /** excludes overlay image data sets even when they match to the image dataset pattern */
    public static <T extends DataPE> List<T> filterImageDatasets(List<T> datasets)
    {
        List<T> allDatasets =
                filterNonContainedDatasets(datasets,
                        ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                        ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);

        allDatasets =
                excludeDatasetsByTypePattern(allDatasets,
                        ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN,
                        ScreeningConstants.MICROSCOPY_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
        return allDatasets;
    }

    // returns datasets matching one of the specified types which at the same time do not have a
    // matching container dataset
    private static <T extends DataPE> List<T> filterNonContainedDatasets(Collection<T> datasets,
            String... datasetTypeCodePatterns)
    {
        List<T> typeMatchingDatasets =
                filterDatasetsByTypePattern(datasets, datasetTypeCodePatterns);
        final List<T> chosenDatasets = new ArrayList<T>();
        for (T dataset : typeMatchingDatasets)
        {
            if (isContainerMatching(dataset, datasetTypeCodePatterns) == false
                    && isNotEmpty(dataset))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    private static boolean isContainerMatching(AbstractExternalData dataset,
            String... datasetTypeCodePatterns)
    {
        ContainerDataSet container = dataset.tryGetContainer();
        return container != null && isOneOfTypesMatching(container, datasetTypeCodePatterns);
    }

    private static <T extends DataPE> boolean isContainerMatching(T dataset,
            String... datasetTypeCodePatterns)
    {
        List<DataPE> containers = dataset.getContainers();
        return containers.isEmpty() == false && isOneOfTypesMatching(containers.get(0), datasetTypeCodePatterns);
    }

    private static boolean isNotEmpty(AbstractExternalData dataset)
    {
        return dataset.isContainer() == false
                || dataset.tryGetAsContainerDataSet().getContainedDataSets().size() > 0;
    }

    private static <T extends DataPE> boolean isNotEmpty(T dataset)
    {
        return dataset.isContainer() == false || dataset.getContainedDataSets().size() > 0;
    }

    /** chooses datasets of unknown types */
    public static <T extends DataPE> List<T> filterUnknownDatasets(List<T> datasets)
    {
        return excludeDatasetsByTypePattern(datasets,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);
    }

    private static <T extends DataPE> List<T> excludeDatasetsByTypePattern(List<T> datasets,
            String... datasetTypeCodePatterns)
    {
        return filterDatasetsByTypePattern(datasets, false, datasetTypeCodePatterns);
    }

    public static <T extends DataPE> List<T> filterDatasetsByTypePattern(Collection<T> datasets,
            String... datasetTypeCodePatterns)
    {
        return filterDatasetsByTypePattern(datasets, true, datasetTypeCodePatterns);
    }

    private static <T extends DataPE> List<T> filterDatasetsByTypePattern(Collection<T> datasets,
            boolean doesMatch, String... datasetTypeCodePatterns)
    {
        final List<T> chosenDatasets = new ArrayList<T>();
        for (T dataset : datasets)
        {
            if (isOneOfTypesMatching(dataset, datasetTypeCodePatterns) == doesMatch)
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    private static boolean isOneOfTypesMatching(DataPE dataset, String... datasetTypeCodePatterns)
    {
        for (String datasetTypeCodePattern : datasetTypeCodePatterns)
        {
            if (isTypeMatching(dataset, datasetTypeCodePattern))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isMatching(AnalysisProcedureResult analysisProcedureResult,
            String datasetTypeCodePattern)
    {
        String analysisProcedure = analysisProcedureResult.analysisProcedure;
        if (analysisProcedure == null)
        {
            return true;
        }
        return analysisProcedureResult.datasetTypeCode.matches(datasetTypeCodePattern);
    }

    private static boolean isTypeMatching(DataPE dataset, String datasetTypeCodePattern)
    {
        return dataset.getDataSetType().getCode().matches(datasetTypeCodePattern);
    }

    /**
     * true if a dataset contains HCS images. Such a dataset can be a parent of a feature vector dataset or overlay dataset.
     */
    public static boolean isHcsImageDataset(AbstractExternalData externalData)
    {
        return isTypeMatchingExcludingContainer(externalData,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN);
    }

    private static boolean isTypeMatchingExcludingContainer(AbstractExternalData externalData,
            String typePattern)
    {
        return isOneOfTypesMatching(externalData, typePattern)
                && isContainerMatching(externalData, typePattern) == false
                && isNotEmpty(externalData);
    }

    public static boolean isRawHcsImageDataset(AbstractExternalData externalData)
    {
        return isTypeMatchingExcludingContainer(externalData,
                ScreeningConstants.HCS_RAW_IMAGE_DATASET_TYPE_PATTERN)
                || ScreeningConstants.HCS_RAW_IMAGE_LEGACY_DATASET_TYPE.equals(externalData
                        .getDataSetType().getCode());
    }

    public static boolean isSegmentationHcsImageDataset(AbstractExternalData externalData)
    {
        return isTypeMatchingExcludingContainer(externalData,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
    }

    public static boolean isBasicHcsImageDataset(AbstractExternalData externalData)
    {
        return isHcsImageDataset(externalData)
                && isSegmentationHcsImageDataset(externalData) == false;
    }

    public static List<AbstractExternalData> filterExternalDataByTypePattern(
            Collection<AbstractExternalData> datasets, String... datasetTypeCodePatterns)
    {
        List<AbstractExternalData> chosenDatasets = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataset : datasets)
        {
            if (isOneOfTypesMatching(dataset, datasetTypeCodePatterns))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    public static boolean isTypeMatching(AbstractExternalData dataset, String datasetTypeCodePattern)
    {
        return dataset.getDataSetType().getCode().matches(datasetTypeCodePattern);
    }

    private static boolean isOneOfTypesMatching(AbstractExternalData dataset,
            String... datasetTypeCodePatterns)
    {
        for (String datasetTypeCodePattern : datasetTypeCodePatterns)
        {
            if (isTypeMatching(dataset, datasetTypeCodePattern))
            {
                return true;
            }
        }
        return false;
    }

    /** Loads dataset metadata from the imaging database */
    public static ImageDatasetParameters tryLoadImageParameters(AbstractExternalData dataset,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        IImageDatasetLoader loader = tryCreateHCSDatasetLoader(dataset, businessObjectFactory);
        if (loader == null)
        {
            return null;
        }
        ImageDatasetParameters params = loader.getImageParameters();
        return params;
    }

    private static IImageDatasetLoader tryCreateHCSDatasetLoader(AbstractExternalData dataSet,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        String datastoreCode = dataSet.getDataStore().getCode();
        String datasetCode = dataSet.getCode();
        return businessObjectFactory.tryCreateImageDatasetLoader(datasetCode, datastoreCode);
    }

    public static String asJavaRegExpr(String[] substrings)
    {
        return asSubstringExpression(substrings, ".*");
    }

    public static String asSubstringExpression(String[] substrings, String starExpr)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < substrings.length; i++)
        {
            if (sb.length() > 0)
            {
                sb.append("|");
            }
            sb.append(starExpr);
            sb.append(substrings[i]);
            sb.append(starExpr);
        }
        return sb.toString();
    }

    public static boolean isMatchingAnalysisProcedure(AbstractExternalData dataset,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        String dataSetAnalysisProcedure =
                EntityHelper.tryFindPropertyValue(dataset, ScreeningConstants.ANALYSIS_PROCEDURE);
        return analysisProcedureCriteria.matches(dataSetAnalysisProcedure);
    }

    public static PlateIdentifier createPlateIdentifier(final Sample sample)
    {
        final String plateCode = sample.getCode();
        Space space = sample.getSpace();
        Project project = sample.getProject();
        String spaceCodeOrNull = null;
        String projectCodeOrNull = null;
        if (project != null)
        {
            projectCodeOrNull = project.getCode();
            space = project.getSpace();
        }
        if (space != null)
        {
            spaceCodeOrNull = space.getCode();
        }
        return new PlateIdentifier(plateCode, spaceCodeOrNull, projectCodeOrNull, sample.getPermId());
    }
}
