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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureList;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlateContentLoader.class);

    /**
     * Loads data about the plate for a specified sample id. Attaches information about images and image analysis datasets.
     * 
     * @param hibernateSession
     */
    public static PlateContent loadImagesAndMetadata(Session session,
            org.hibernate.Session hibernateSession, IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, TechId plateId)
    {
        return new PlateContentLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).getPlateContent(plateId);
    }

    /**
     * Loads feature vector of specified dataset with one feature specified by name.
     */
    public static FeatureVectorDataset loadFeatureVectorDataset(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DatasetReference dataset, CodeAndLabel featureName)
    {
        return new PlateContentLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).fetchFeatureVector(dataset, featureName);
    }

    /**
     * Loads data about the plate for a specified dataset, which is supposed to contain images.
     */
    public static PlateImages loadImagesAndMetadataForDataset(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, TechId datasetId)
    {
        return new PlateContentLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).getPlateContentForDataset(datasetId);
    }

    /**
     * Loads information about datasets connected to specified sample (microscopy) or a container sample (HCS). In particular loads the logical images
     * in datasets belonging to the specified sample (restricted to one well in HCS case).
     */
    public static ImageSampleContent getImageDatasetInfosForSample(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        return new PlateContentLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).getImageDatasetInfosForSample(sampleId,
                wellLocationOrNull);
    }

    public static List<PlateMetadata> loadPlateMetadata(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, List<TechId> plateIds)
    {
        return new PlateContentLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).getPlateMetadatas(plateIds);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final LogicalImageLoader imageLoader;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private PlateContentLoader(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.imageLoader =
                new LogicalImageLoader(session, hibernateSession, businessObjectFactory,
                        managedPropertyEvaluatorFactory);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    private PlateImages getPlateContentForDataset(TechId datasetId)
    {
        DataPE dataSet = loadDataset(datasetId);
        SamplePE plate = dataSet.tryGetSample();
        if (plate == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' has no sample connected.",
                    dataSet.getCode());
        }
        List<WellMetadata> wells = loadWells(new TechId(HibernateUtils.getId(plate)));
        DatasetImagesReference datasetImagesReference =
                imageLoader.tryLoadImageDatasetReference(dataSet);
        if (datasetImagesReference == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' is not an image dataset.",
                    dataSet.getCode());
        }
        Geometry plateGeometry = getPlateGeometry(plate);
        PlateMetadata plateMetadata =
                new PlateMetadata(translate(plate), wells, plateGeometry.getNumberOfRows(),
                        plateGeometry.getNumberOfColumns());
        return new PlateImages(plateMetadata, datasetImagesReference);
    }

    private Geometry getPlateGeometry(SamplePE plate)
    {
        List<IEntityProperty> properties =
                EntityPropertyTranslator.translate(plate.getProperties(), null, null,
                        managedPropertyEvaluatorFactory,
                        new SamplePropertyAccessValidator(session, businessObjectFactory.getDAOFactory()));
        return PlateDimensionParser.getPlateGeometry(properties);
    }

    private DataPE loadDataset(TechId datasetId)
    {
        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadDataByTechId(datasetId);
        DataPE dataSet = dataBO.getData();
        return dataSet;
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IDataSetTable dataSetTable = createDataSetTable();

        Sample plate = loadPlate(plateId);
        List<DataPE> datasets = loadDatasets(plateId, dataSetTable);
        List<WellMetadata> wells = loadWells(plateId);

        List<ImageDatasetEnrichedReference> imageDatasetReferences =
                imageLoader.loadImageDatasets(datasets);

        List<DatasetReference> featureVectorDatasets = filterAndFetchFeatureVectors(datasets);

        List<DatasetReference> unknownDatasetReferences = extractUnknownDatasets(datasets);

        Geometry plateGeometry = PlateDimensionParser.getPlateGeometry(plate.getProperties());
        int rows = plateGeometry.getNumberOfRows();
        int cols = plateGeometry.getNumberOfColumns();
        PlateMetadata plateMetadata = new PlateMetadata(plate, wells, rows, cols);
        return new PlateContent(plateMetadata, imageDatasetReferences, featureVectorDatasets,
                unknownDatasetReferences);
    }

    private List<DatasetReference> extractUnknownDatasets(List<DataPE> datasets)
    {
        List<DataPE> unknownDatasets = ScreeningUtils.filterUnknownDatasets(datasets);
        List<DatasetReference> unknownDatasetReferences = createDatasetReferences(unknownDatasets);
        return unknownDatasetReferences;
    }

    private List<DatasetReference> filterAndFetchFeatureVectors(List<DataPE> datasets)
    {
        List<DataPE> analysisDatasets = ScreeningUtils.filterImageAnalysisDatasetsPE(datasets);
        List<DatasetReference> featureVectorDatasetReferences =
                createDatasetReferences(analysisDatasets);
        return featureVectorDatasetReferences;
    }

    private FeatureVectorDataset fetchFeatureVector(DatasetReference datasetReference,
            CodeAndLabel featureName)
    {
        IHCSFeatureVectorLoader loader =
                businessObjectFactory.createHCSFeatureVectorLoader(datasetReference
                        .getDatastoreCode());
        if (featureName == null)
        {
            return loadFeatureVector(datasetReference, loader);
        } else
        {
            return loadFeatureVector(datasetReference, featureName, loader);
        }
    }

    private FeatureVectorDataset loadFeatureVector(DatasetReference ref,
            IHCSFeatureVectorLoader loader)
    {
        Map<Long, GenericEntityPropertyRecord> analysisProcedures =
                businessObjectFactory.createDatasetLister(session).fetchProperties(
                        Collections.singletonList(ref.getId()),
                        ScreeningConstants.ANALYSIS_PROCEDURE_PROPERTY);

        GenericEntityPropertyRecord analysisProcedure = analysisProcedures.get(ref.getId());
        FeatureVectorDataset featureVectorDataset =
                loadFeatureVector(ref, loader, analysisProcedure == null ? null
                        : analysisProcedure.value);
        return featureVectorDataset;
    }

    private FeatureVectorDataset loadFeatureVector(DatasetReference datasetReference,
            IHCSFeatureVectorLoader loader, String analysisProcedure)
    {
        WellFeatureCollection<FeatureVectorValues> featureValues =
                loader.fetchDatasetFeatureValues(session,
                        Arrays.asList(datasetReference.getCode()), Collections.<String> emptyList());
        List<FeatureVectorValues> featureVectors = featureValues.getFeatures();

        List<FeatureList> featureLists = tryLoadFeatureLists(datasetReference);

        FeatureVectorDataset featureVectorDataset =
                new FeatureVectorDataset(datasetReference, featureVectors, featureLists,
                        analysisProcedure);
        return featureVectorDataset;
    }

    private List<FeatureList> tryLoadFeatureLists(DatasetReference datasetReference)
    {

        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(Collections.singletonList(datasetReference.getCode()),
                false, false);
        DataPE dataPE = dataSetTable.getDataSets().iterator().next();
        DatasetDescription datasetDescription = DataSetTranslator.translateToDescription(dataPE);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("data-set", datasetDescription);

        TableModel tableModel =
                dataSetTable.createReportFromAggregationService(
                        ScreeningConstants.FEATURE_LISTS_AGGREGATION_SERVICE_KEY, dataPE
                                .getDataStore().getCode(), parameters);

        if (tableModel.getRows().size() > 0)
        {
            List<FeatureList> result = new ArrayList<FeatureList>();
            for (TableModelRow row : tableModel.getRows())
            {
                String name = row.getValues().get(0).toString();
                String[] valuesArray = row.getValues().get(1).toString().split("\\r?\\n");
                List<String> values = new ArrayList<String>(Arrays.asList(valuesArray));

                result.add(new FeatureList(name, values));
            }

            return result;
        }

        return null;
    }

    // loads feature vector with only one feature specified by name
    private FeatureVectorDataset loadFeatureVector(DatasetReference datasetReference,
            CodeAndLabel featureName, IHCSFeatureVectorLoader loader)
    {
        WellFeatureCollection<FeatureVectorValues> featureValues =
                loader.fetchDatasetFeatureValues(session,
                        Arrays.asList(datasetReference.getCode()),
                        Collections.singletonList(featureName.getCode()));

        List<FeatureVectorValues> featureVectors = featureValues.getFeatures();

        Map<Long, GenericEntityPropertyRecord> analysisProcedures =
                businessObjectFactory.createDatasetLister(session).fetchProperties(
                        Arrays.asList(datasetReference.getId()),
                        ScreeningConstants.ANALYSIS_PROCEDURE_PROPERTY);
        GenericEntityPropertyRecord property = analysisProcedures.get(datasetReference.getId());

        List<FeatureList> featureLists = tryLoadFeatureLists(datasetReference);

        FeatureVectorDataset featureVectorDataset =
                new FeatureVectorDataset(datasetReference, featureVectors, featureLists,
                        property == null ? null : property.value);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("loadFeatureVector(%s,%s):",
                    datasetReference.getPermId(), featureName.toString()));
            for (int i = 0; i < featureVectorDataset.getDatasetFeatures().size(); ++i)
            {
                operationLog.debug(String.format("%d: %s -> %s", i, featureVectorDataset
                        .getDatasetFeatures().get(i).getWellLocation(), featureVectorDataset
                        .getDatasetFeatures().get(i).getFeatureMap()));
            }
        }
        return featureVectorDataset;
    }

    private <T extends DataPE> List<DatasetReference> createDatasetReferences(List<T> datasets)
    {
        List<DatasetReference> datasetReferences = new ArrayList<DatasetReference>();
        for (T dataset : datasets)
        {
            datasetReferences.add(ScreeningUtils.createDatasetReference(dataset,
                    session.getBaseIndexURL(), managedPropertyEvaluatorFactory,
                    new SamplePropertyAccessValidator(session, businessObjectFactory.getDAOFactory())));
        }
        return datasetReferences;
    }

    private IDataSetTable createDataSetTable()
    {
        return businessObjectFactory.createDataSetTable(session);
    }

    private Sample loadPlate(TechId plateId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(plateId);
        sampleBO.enrichWithProperties();
        SamplePE sample = sampleBO.getSample();
        return translate(sample);
    }

    private Sample translate(SamplePE sample)
    {
        return SampleTranslator.translate(sample, session.getBaseIndexURL(), null,
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, businessObjectFactory.getDAOFactory()));
    }

    private List<WellMetadata> loadWells(TechId plateId)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        List<Sample> wells = sampleLister.list(createSamplesForContainerCriteria(plateId));
        List<Material> containedMaterials = getReferencedMaterials(wells);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        materialLister.enrichWithProperties(containedMaterials);
        return createWells(wells);
    }

    private static List<Material> getReferencedMaterials(
            List<? extends IEntityPropertiesHolder> entities)
    {
        List<Material> materials = new ArrayList<Material>();
        for (IEntityPropertiesHolder entity : entities)
        {
            List<IEntityProperty> properties = entity.getProperties();
            for (IEntityProperty prop : properties)
            {
                Material material = prop.getMaterial();
                if (material != null)
                {
                    materials.add(material);
                }
            }
        }
        return materials;
    }

    protected static List<DataPE> loadDatasets(TechId plateId, IDataSetTable dataSetTable)
    {
        dataSetTable.loadBySampleTechIdWithoutRelationships(plateId);
        return dataSetTable.getDataSets();
    }

    private static List<WellMetadata> createWells(List<Sample> wellSamples)
    {
        List<WellMetadata> wells = new ArrayList<WellMetadata>();
        for (Sample wellSample : wellSamples)
        {
            wells.add(createWell(wellSample));
        }
        return wells;
    }

    private static WellMetadata createWell(Sample wellSample)
    {
        WellMetadata well = new WellMetadata();
        WellLocation locationOrNull = tryGetLocation(wellSample);
        well.setWellSample(wellSample, locationOrNull);
        return well;
    }

    private static WellLocation tryGetLocation(Sample wellSample)
    {
        return ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellSample.getSubCode());
    }

    private static ListOrSearchSampleCriteria createSamplesForContainerCriteria(TechId plateId)
    {
        return new ListOrSearchSampleCriteria(ListSampleCriteria.createForContainer(plateId));
    }

    private TechId fetchContainerId(TechId wellId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(wellId);
        SamplePE sample = sampleBO.getSample();
        return new TechId(sample.getContainer().getId());
    }

    private ImageSampleContent getImageDatasetInfosForSample(TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        TechId datasetOwnerSampleId;
        if (wellLocationOrNull != null)
        {
            datasetOwnerSampleId = fetchContainerId(sampleId);
        } else
        {
            datasetOwnerSampleId = sampleId;
        }
        List<DataPE> datasets = loadDatasets(datasetOwnerSampleId, createDataSetTable());
        List<DataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);

        List<LogicalImageInfo> logicalImages = new ArrayList<LogicalImageInfo>();
        for (DataPE imageDataset : imageDatasets)
        {
            LogicalImageInfo logicalImage =
                    imageLoader.tryLoadLogicalImageInfo(imageDataset.getCode(), imageDataset
                            .getDataStore().getCode(), wellLocationOrNull);
            if (logicalImage != null)
            {
                logicalImages.add(logicalImage);
            }
        }

        List<DatasetReference> unknownDatasetReferences = extractUnknownDatasets(datasets);
        return new ImageSampleContent(logicalImages, unknownDatasetReferences);
    }

    private List<PlateMetadata> getPlateMetadatas(List<TechId> plateIds)
    {
        ArrayList<PlateMetadata> result = new ArrayList<PlateMetadata>();
        for (TechId plateId : plateIds)
        {
            Sample plate = loadPlate(plateId);
            List<WellMetadata> wells = loadWells(plateId);
            Geometry plateGeometry = PlateDimensionParser.getPlateGeometry(plate.getProperties());
            int rows = plateGeometry.getNumberOfRows();
            int cols = plateGeometry.getNumberOfColumns();
            PlateMetadata plateMetadata = new PlateMetadata(plate, wells, rows, cols);
            result.add(plateMetadata);
        }

        return result;
    }

}
