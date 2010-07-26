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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * Contains implementations of the screening public API calls.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningApiImpl
{
    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private final String dataStoreBaseURL;

    public ScreeningApiImpl(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, String dataStoreBaseURL)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
        this.dataStoreBaseURL = dataStoreBaseURL;
    }

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        FeatureVectorDatasetLoader datasetRetriever =
                new FeatureVectorDatasetLoader(session, businessObjectFactory, dataStoreBaseURL,
                        plates);
        List<FeatureVectorDatasetReference> result = datasetRetriever.getFeatureVectorDatasets();

        return result;
    }

    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        return new ImageDatasetLoader(session, businessObjectFactory, dataStoreBaseURL, plates)
                .getImageDatasets();
    }

    public List<Plate> listPlates()
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(loadPlateType());
        criteria.setIncludeSpace(true);
        criteria.setSpaceCode(null);
        criteria.setExcludeWithoutExperiment(true);

        List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        return asPlates(samples);
    }

    private static List<Plate> asPlates(List<Sample> samples)
    {
        final List<Plate> plates = new ArrayList<Plate>();
        for (Sample sample : samples)
        {
            plates.add(asPlate(sample));
        }
        return plates;
    }

    private static Plate asPlate(Sample sample)
    {
        final Experiment experiment = sample.getExperiment();
        final Project project = experiment.getProject();
        final Space sampleSpace = sample.getSpace();
        final String sampleSpaceCode = (sampleSpace != null) ? sampleSpace.getCode() : null;
        final Space experimentSpace = project.getSpace();
        final ExperimentIdentifier experimentId =
                new ExperimentIdentifier(experiment.getCode(), project.getCode(), experimentSpace
                        .getCode(), experiment.getPermId());
        return new Plate(sample.getCode(), sampleSpaceCode, sample.getPermId(), experimentId);
    }

    private SampleType loadPlateType()
    {
        ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        SampleTypePE plateTypePE =
                sampleTypeDAO.tryFindSampleTypeByCode(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
        assert plateTypePE != null : "plate type not found";
        return SampleTypeTranslator.translate(plateTypePE, null);
    }

    public List<ExperimentIdentifier> listExperiments()
    {
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        final List<ExperimentIdentifier> experimentIds = asExperimentIdentifiers(experiments);
        Collections.sort(experimentIds, new Comparator<ExperimentIdentifier>()
            {
                public int compare(ExperimentIdentifier o1, ExperimentIdentifier o2)
                {
                    return o1.getAugmentedCode().compareTo(o2.getAugmentedCode());
                }
            });
        return experimentIds;
    }

    private static List<ExperimentIdentifier> asExperimentIdentifiers(List<ExperimentPE> experiments)
    {
        final List<ExperimentIdentifier> experimentIds = new ArrayList<ExperimentIdentifier>();
        for (ExperimentPE experiment : experiments)
        {
            experimentIds.add(asExperimentIdentifier(experiment));
        }
        return experimentIds;
    }

    private static ExperimentIdentifier asExperimentIdentifier(ExperimentPE experiment)
    {
        final ExperimentIdentifier experimentId =
                new ExperimentIdentifier(experiment.getCode(), experiment.getProject().getCode(),
                        experiment.getProject().getGroup().getCode(), experiment.getPermId());
        return experimentId;
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        List<IDatasetIdentifier> identifiers = new ArrayList<IDatasetIdentifier>();
        for (String datasetCode : datasetCodes)
        {
            identifiers.add(getDatasetIdentifier(externalDataBO, datasetCode));
        }
        return identifiers;
    }

    private IDatasetIdentifier getDatasetIdentifier(IExternalDataBO externalDataBO,
            String datasetCode)
    {
        externalDataBO.loadByCode(datasetCode);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        if (externalData == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' does not exist", datasetCode);
        }
        return new DatasetIdentifier(datasetCode, externalData.getDataStore().getDownloadUrl());
    }

    static class DatasetReferenceHolder
    {
        final List<ImageDatasetReference> imageDatasets = new ArrayList<ImageDatasetReference>();

        final List<FeatureVectorDatasetReference> featureVectorDatasets =
                new ArrayList<FeatureVectorDatasetReference>();
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        final MaterialPE materialOrNull =
                daoFactory.getMaterialDAO().tryFindMaterial(
                        new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier(
                                materialIdentifier.getMaterialCode(), materialIdentifier
                                        .getMaterialTypeIdentifier().getMaterialTypeCode()));
        if (materialOrNull == null)
        {
            throw UserFailureException.fromTemplate("Material '%s' does not exist",
                    materialIdentifier.getAugmentedCode());
        }
        final List<WellContent> wellContent;
        if (experimentIdentifer.getPermId() != null)
        {
            wellContent =
                    GenePlateLocationsLoader.load(session, businessObjectFactory, daoFactory,
                            new TechId(materialOrNull.getId()), experimentIdentifer.getPermId(),
                            false);
        } else
        {
            final String spaceCode =
                    StringUtils.isBlank(experimentIdentifer.getSpaceCode()) ? session
                            .tryGetHomeGroupCode() : experimentIdentifer.getSpaceCode();
            if (spaceCode == null)
            {
                throw new UserFailureException("No space given and user has no home space.");
            }
            final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentId =
                    new ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier(
                            DatabaseInstanceIdentifier.HOME, spaceCode, experimentIdentifer
                                    .getProjectCode(), experimentIdentifer.getExperimentCode());
            wellContent =
                    GenePlateLocationsLoader.load(session, businessObjectFactory, daoFactory,
                            new TechId(materialOrNull.getId()), experimentId, false);
        }
        if (findDatasets)
        {
            final Set<Plate> plates = new HashSet<Plate>(wellContent.size());
            for (WellContent w : wellContent)
            {
                plates.add(asPlate(experimentIdentifer, w));
            }
            final FeatureVectorDatasetLoader datasetRetriever =
                    new FeatureVectorDatasetLoader(session, businessObjectFactory,
                            dataStoreBaseURL, plates);
            final List<ImageDatasetReference> imageDatasets = datasetRetriever.getImageDatasets();
            final List<FeatureVectorDatasetReference> featureVectorDatasets =
                    datasetRetriever.getFeatureVectorDatasets();

            return asPlateWellReferences(experimentIdentifer, wellContent,
                    createPlateToDatasetsMap(imageDatasets, featureVectorDatasets));
        } else
        {
            return asPlateWellReferences(experimentIdentifer, wellContent, Collections
                    .<String, DatasetReferenceHolder> emptyMap());
        }
    }

    private static Map<String, DatasetReferenceHolder> createPlateToDatasetsMap(
            List<ImageDatasetReference> imageDatasets,
            List<FeatureVectorDatasetReference> featureVectorDatasets)
    {
        final Map<String, DatasetReferenceHolder> map =
                new HashMap<String, DatasetReferenceHolder>();
        for (ImageDatasetReference dataset : imageDatasets)
        {
            DatasetReferenceHolder reference = map.get(dataset.getPlate());
            if (reference == null)
            {
                reference = new DatasetReferenceHolder();
                map.put(dataset.getPlate().getPermId(), reference);
            }
            reference.imageDatasets.add(dataset);
        }
        for (FeatureVectorDatasetReference dataset : featureVectorDatasets)
        {
            DatasetReferenceHolder reference = map.get(dataset.getPlate());
            if (reference == null)
            {
                reference = new DatasetReferenceHolder();
                map.put(dataset.getPlate().getPermId(), reference);
            }
            reference.featureVectorDatasets.add(dataset);
        }
        return map;
    }

    private static Plate asPlate(ExperimentIdentifier experimentIdentifier, WellContent wellContent)
    {
        return new Plate(wellContent.getPlate().getCode(), experimentIdentifier.getSpaceCode(),
                wellContent.getPlate().getPermId(), experimentIdentifier);
    }

    private static PlateWellReferenceWithDatasets asPlateWellReference(
            ExperimentIdentifier experimentIdentifier, WellContent wellContent,
            Map<String, DatasetReferenceHolder> plateToDatasetsMap)
    {
        final Plate plate = asPlate(experimentIdentifier, wellContent);
        final WellPosition wellPosition =
                new WellPosition(wellContent.tryGetLocation().getRow(), wellContent
                        .tryGetLocation().getColumn());
        final DatasetReferenceHolder datasetReferences = plateToDatasetsMap.get(plate.getPermId());
        if (datasetReferences == null)
        {
            return new PlateWellReferenceWithDatasets(plate, wellPosition);
        } else
        {
            return new PlateWellReferenceWithDatasets(plate, wellPosition,
                    datasetReferences.imageDatasets, datasetReferences.featureVectorDatasets);
        }
    }

    private static List<PlateWellReferenceWithDatasets> asPlateWellReferences(
            ExperimentIdentifier experimentIdentifer, List<WellContent> wellContents,
            Map<String, DatasetReferenceHolder> plateToDatasetsMap)
    {
        final List<PlateWellReferenceWithDatasets> plateWellReferences =
                new ArrayList<PlateWellReferenceWithDatasets>();
        for (WellContent wellContent : wellContents)
        {
            plateWellReferences.add(asPlateWellReference(experimentIdentifer, wellContent,
                    plateToDatasetsMap));
        }
        return plateWellReferences;
    }

}
