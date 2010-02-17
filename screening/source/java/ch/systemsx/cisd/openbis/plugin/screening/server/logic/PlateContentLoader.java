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
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateSingleImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Loads content of the plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateContentLoader
{
    /**
     * loads all images from all existing image datasets for the specified plate.
     */
    public static List<PlateSingleImageReference> listPlateImages(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).loadAllImages(plateId);
    }

    public static TableModel loadImageAnalysisForPlate(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .loadImageAnalysisForPlate(plateId);
    }

    public static TableModel loadImageAnalysisForExperiment(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId experimentId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .loadImageAnalysisForExperiment(experimentId);
    }

    /**
     * loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis only if one dataset with such a data exist.
     */
    public static PlateContent loadImagesAndMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId plateId)
    {
        return new PlateContentLoader(session, businessObjectFactory).getPlateContent(plateId);
    }

    /**
     * loads data about the plate for a specified dataset, which is supposed to contain images in
     * BDS-HCS format.
     */
    public static PlateImages loadImagesAndMetadataForDataset(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, TechId datasetId)
    {
        return new PlateContentLoader(session, businessObjectFactory)
                .getPlateContentForDataset(datasetId);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private PlateContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
    }

    private PlateImages getPlateContentForDataset(TechId datasetId)
    {
        ExternalDataPE externalData = loadDataset(datasetId);
        SamplePE plate = externalData.tryGetSample();
        if (plate == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' has no sample connected.",
                    externalData.getCode());
        }
        List<WellMetadata> wells = loadWells(new TechId(plate.getId()));
        DatasetImagesReference datasetImagesReference =
                loadImages(createExternalDataTable(), externalData);
        return new PlateImages(translate(plate), wells, datasetImagesReference);
    }

    private ExternalDataPE loadDataset(TechId datasetId)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        externalDataBO.loadDataByTechId(datasetId);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        return externalData;
    }

    private TableModel loadImageAnalysisForPlate(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        return loadImageAnalysis(externalDataTable, datasets);
    }

    private TableModel loadImageAnalysisForExperiment(TechId experimentId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();
        List<ExternalDataPE> datasets = loadDatasetsForExperiment(experimentId, externalDataTable);
        return loadImageAnalysis(externalDataTable, datasets);
    }

    private TableModel loadImageAnalysis(IExternalDataTable externalDataTable,
            List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> analysisDatasets =
                filterDatasetsByType(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        List<String> datasetCodes = extractCodes(analysisDatasets);
        String dataStoreCode = extractDataStoreCode(analysisDatasets);
        return DatasetLoader.loadAnalysisResults(datasetCodes, dataStoreCode, externalDataTable);
    }

    private List<PlateSingleImageReference> loadAllImages(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        List<ExternalDataPE> imageDatasets =
                filterDatasetsByType(datasets, ScreeningConstants.IMAGE_DATASET_TYPE);
        List<PlateSingleImageReference> imagePaths = new ArrayList<PlateSingleImageReference>();
        if (imageDatasets.size() > 0)
        {
            List<String> datasetCodes = extractCodes(imageDatasets);
            // NOTE: we assume that all datasets for one plate come from the same datastore
            String dataStoreCode = extractDataStoreCode(imageDatasets);
            imagePaths =
                    DatasetLoader.loadPlateImages(datasetCodes, dataStoreCode, externalDataTable);
        }
        return imagePaths;
    }

    private String extractDataStoreCode(List<ExternalDataPE> imageDatasets)
    {
        assert imageDatasets.size() > 0;
        String dataStoreCode = extractDataStoreCode(imageDatasets.get(0));
        ensureSameDataStore(imageDatasets, dataStoreCode);
        return dataStoreCode;
    }

    private List<String> extractCodes(List<ExternalDataPE> datasets)
    {
        List<String> datasetCodes = new ArrayList<String>();
        for (ExternalDataPE dataset : datasets)
        {
            datasetCodes.add(dataset.getCode());
        }
        return datasetCodes;
    }

    private String extractDataStoreCode(ExternalDataPE imageDataset)
    {
        return imageDataset.getDataStore().getCode();
    }

    private void ensureSameDataStore(List<ExternalDataPE> datasets, String dataStoreCode)
    {
        for (ExternalDataPE dataset : datasets)
        {
            String anotherDataStoreCode = extractDataStoreCode(dataset);
            if (anotherDataStoreCode.equals(dataStoreCode) == false)
            {
                throw UserFailureException
                        .fromTemplate(
                                "Datasets come from the different stores: '%s' and '%s'. Cannot perform the operation.",
                                dataStoreCode, anotherDataStoreCode);
            }
        }
    }

    private PlateContent getPlateContent(TechId plateId)
    {
        IExternalDataTable externalDataTable = createExternalDataTable();

        Sample plate = loadPlate(plateId);
        List<ExternalDataPE> datasets = loadDatasets(plateId, externalDataTable);
        List<WellMetadata> wells = loadWells(plateId);

        List<ExternalDataPE> imageDatasets =
                filterDatasetsByType(datasets, ScreeningConstants.IMAGE_DATASET_TYPE);
        DatasetImagesReference imageDataset = null;
        if (imageDatasets.size() == 1)
        {
            imageDataset = loadImages(externalDataTable, imageDatasets.get(0));
        }

        List<ExternalDataPE> analysisDatasets =
                filterDatasetsByType(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        DatasetReference analysisDataset = null;
        if (analysisDatasets.size() == 1)
        {
            analysisDataset = ScreeningUtils.createDatasetReference(analysisDatasets.get(0));
        }

        return new PlateContent(plate, wells, imageDataset, imageDatasets.size(), analysisDataset,
                analysisDatasets.size());
    }

    private IExternalDataTable createExternalDataTable()
    {
        return businessObjectFactory.createExternalDataTable(session);
    }

    private Sample loadPlate(TechId plateId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(plateId);
        SamplePE sample = sampleBO.getSample();
        return translate(sample);
    }

    private Sample translate(SamplePE sample)
    {
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private List<WellMetadata> loadWells(TechId plateId)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);

        List<Sample> wells = sampleLister.list(createSamplesForContainerCriteria(plateId));
        List<Material> containedMaterials = getReferencedMaterials(wells);
        materialLister.enrichWithProperties(containedMaterials);
        List<Material> genes =
                getInhibitedMaterials(containedMaterials,
                        ScreeningConstants.INHIBITOR_PROPERTY_CODE);
        materialLister.enrichWithProperties(genes);
        return createWells(wells);
    }

    protected static List<ExternalDataPE> loadDatasets(TechId plateId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadBySampleTechId(plateId);
        return externalDataTable.getExternalData();
    }

    private List<ExternalDataPE> loadDatasetsForExperiment(TechId experimentId,
            IExternalDataTable externalDataTable)
    {
        externalDataTable.loadByExperimentTechId(experimentId);
        return externalDataTable.getExternalData();
    }

    private DatasetImagesReference loadImages(IExternalDataTable externalDataTable,
            ExternalDataPE dataset)
    {
        PlateImageParameters imageParameters = loadImageParams(dataset, externalDataTable);
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
    }

    private PlateImageParameters loadImageParams(ExternalDataPE dataset,
            IExternalDataTable externalDataTable)
    {
        DataStorePE dataStore = dataset.getDataStore();
        String datasetCode = dataset.getCode();
        List<String> datasets = Arrays.asList(datasetCode);
        List<PlateImageParameters> imageParamsReports =
                DatasetLoader.loadPlateImageParameters(datasets, dataStore.getCode(),
                        externalDataTable);
        assert imageParamsReports.size() == 1;
        return imageParamsReports.get(0);
    }

    private static List<ExternalDataPE> filterDatasetsByType(List<ExternalDataPE> datasets,
            String datasetTypeCode)
    {
        List<ExternalDataPE> chosenDatasets = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            if (isTypeEqual(dataset, datasetTypeCode))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    private static boolean isTypeEqual(ExternalDataPE dataset, String datasetType)
    {
        return dataset.getDataSetType().getCode().equals(datasetType);
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
        Material content = tryFindMaterialProperty(wellSample.getProperties());
        well.setContent(content);
        if (content != null)
        {
            Material inhibited = tryFindInhibitedMaterial(content);
            well.setGene(inhibited);
        }
        return well;
    }

    private static WellLocation tryGetLocation(Sample wellSample)
    {
        return ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellSample.getSubCode());
    }

    private static Material tryFindInhibitedMaterial(Material content)
    {
        IEntityProperty property =
                tryFindProperty(content.getProperties(), ScreeningConstants.INHIBITOR_PROPERTY_CODE);
        if (property != null)
        {
            Material material = property.getMaterial();
            assert material != null : "Material property expected, but got: " + property;
            return material;
        } else
        {
            return null;
        }
    }

    private static List<Material> getInhibitedMaterials(List<Material> materials,
            String propertyCode)
    {
        List<Material> inhibitedMaterials = new ArrayList<Material>();
        for (Material material : materials)
        {
            Material inhibitedMaterial = tryFindInhibitedMaterial(material);
            if (inhibitedMaterial != null)
            {
                inhibitedMaterials.add(inhibitedMaterial);
            }
        }
        return inhibitedMaterials;
    }

    private static IEntityProperty tryFindProperty(List<IEntityProperty> properties,
            String propertyCode)
    {
        for (IEntityProperty prop : properties)
        {
            if (prop.getPropertyType().getCode().equals(propertyCode))
            {
                return prop;
            }
        }
        return null;
    }

    private static Material tryFindMaterialProperty(List<IEntityProperty> properties)
    {
        for (IEntityProperty prop : properties)
        {
            if (prop.getMaterial() != null)
            {
                return prop.getMaterial();
            }
        }
        return null;
    }

    private static List<Material> getReferencedMaterials(
            List<? extends IEntityPropertiesHolder> entities)
    {
        List<Material> materials = new ArrayList<Material>();
        for (IEntityPropertiesHolder entity : entities)
        {
            Material material = tryFindMaterialProperty(entity.getProperties());
            if (material != null)
            {
                materials.add(material);
            }
        }
        return materials;
    }

    private static ListOrSearchSampleCriteria createSamplesForContainerCriteria(TechId plateId)
    {
        return new ListOrSearchSampleCriteria(ListOrSearchSampleCriteria
                .createForContainer(plateId));
    }
}
