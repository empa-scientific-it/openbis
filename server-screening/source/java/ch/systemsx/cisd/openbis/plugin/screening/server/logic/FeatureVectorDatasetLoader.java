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

import static ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningUtils.isTypeMatching;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class FeatureVectorDatasetLoader extends HCSImageDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    // Parameter state
    private final String featureVectorDatasetTypeCode;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    // Running state
    private Collection<AbstractExternalData> featureVectorDatasets;

    FeatureVectorDatasetLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String homeSpaceOrNull,
            Set<? extends PlateIdentifier> plates)
    {
        this(session, businessObjectFactory, homeSpaceOrNull, plates, AnalysisProcedureCriteria
                .createAllProcedures());
    }

    FeatureVectorDatasetLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String homeSpaceOrNull,
            Set<? extends PlateIdentifier> plates,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN);
        featureVectorDatasetTypeCode = ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
    }

    public static class FeatureVectorExternalData
    {
        private final AbstractExternalData featureVectorDataset;

        private final AbstractExternalData imageDatasetOrNull;

        public FeatureVectorExternalData(AbstractExternalData featureVectorDataset,
                AbstractExternalData imageDatasetOrNull)
        {
            this.featureVectorDataset = featureVectorDataset;
            this.imageDatasetOrNull = imageDatasetOrNull;
        }

        public AbstractExternalData getFeatureVectorDataset()
        {
            return featureVectorDataset;
        }

        public AbstractExternalData tryGetImageDataset()
        {
            return imageDatasetOrNull;
        }
    }

    /**
     * Enriched with image dataset parents. Note that all feature vector datasets have to be connected directly to the plate, otherwise they will be
     * skipped.
     */
    public Collection<AbstractExternalData> getFeatureVectorDatasets()
    {
        loadAll();
        return featureVectorDatasets;
    }

    public List<FeatureVectorDatasetReference> getFeatureVectorDatasetReferences()
    {
        loadAll();
        return asFeatureVectorDatasetReferences();
    }

    private void loadAll()
    {
        // Load the image data sets
        load();
        loadFeatureVectorDatasets();
    }

    private void loadFeatureVectorDatasets()
    {
        final Map<Long, AbstractExternalData> featureVectorDatasetSet = new HashMap<Long, AbstractExternalData>();

        List<AbstractExternalData> imageDatasets = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataset : getDatasets())
        {
            if (isMatchingAnalysisDataSet(dataset))
            {
                featureVectorDatasetSet.put(dataset.getId(), dataset);
            } else if (isMatchingImageDataset(dataset))
            {
                imageDatasets.add(dataset);
            }
        }

        IDatasetLister datasetLister = createDatasetLister();
        enrichWithParentDatasets(featureVectorDatasetSet.values(), imageDatasets,
                featureVectorDatasetTypeCode, datasetLister);
        // Add feature vector datasets which are not connected directly to the plate, but are
        // connected to the image dataset.
        // These datasets are already enriched with parent datasets.
        List<AbstractExternalData> childrenDatasets =
                fetchChildrenDataSets(imageDatasets, featureVectorDatasetTypeCode, datasetLister);
        for (AbstractExternalData dataset : childrenDatasets)
        {
            if (isMatchingAnalysisDataSet(dataset))
            {
                featureVectorDatasetSet.put(dataset.getId(), dataset);
            }
        }

        featureVectorDatasets = featureVectorDatasetSet.values();
    }

    private boolean isMatchingImageDataset(AbstractExternalData dataset)
    {
        return isTypeMatching(dataset, ANY_HCS_IMAGE_DATASET_TYPE_PATTERN);
    }

    private boolean isMatchingAnalysisDataSet(AbstractExternalData dataset)
    {
        return isTypeMatching(dataset, HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN)
                && ScreeningUtils.isMatchingAnalysisProcedure(dataset, analysisProcedureCriteria);
    }

    private List<FeatureVectorDatasetReference> asFeatureVectorDatasetReferences()
    {
        List<FeatureVectorDatasetReference> result = new ArrayList<FeatureVectorDatasetReference>();
        for (AbstractExternalData externalData : featureVectorDatasets)
        {
            result.add(asFeatureVectorDataset(externalData));
        }
        return result;
    }

    private static AbstractExternalData tryGetOneParent(AbstractExternalData externalData)
    {
        Collection<AbstractExternalData> parents = externalData.getParents();
        if (parents != null && parents.size() == 1)
        {
            return parents.iterator().next();
        } else
        {
            return null;
        }
    }

    protected FeatureVectorDatasetReference asFeatureVectorDataset(AbstractExternalData externalData)
    {
        DataStore dataStore = externalData.getDataStore();
        // there should be no more parents than one, we ensure about that earlier
        AbstractExternalData parentDataset = tryGetOneParent(externalData);
        DataSetType dataSetType = externalData.getDataSetType();
        String dataSetTypeCodeOrNull = dataSetType == null ? null : dataSetType.getCode();

        ImageDatasetReference parentDatasetAsImageDataset = tryAsImageDataset(parentDataset);

        if (parentDataset == null || externalData.getSample() != null)
        {
            return new FeatureVectorDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull,
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(externalData),
                    createExperimentIdentifier(externalData), extractPlateGeometry(externalData),
                    externalData.getRegistrationDate(), parentDatasetAsImageDataset, extractProperties(externalData));
        } else
        {
            // Note: this only works reliably because this class sets the parents of the feature
            // vector data sets itself and sets it to a list with exactly one entry!
            // (see loadFeatureVectorDatasets() above)
            return new FeatureVectorDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull,
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(parentDataset),
                    createExperimentIdentifier(externalData), extractPlateGeometry(parentDataset),
                    externalData.getRegistrationDate(), parentDatasetAsImageDataset,
                    extractProperties(externalData));
        }
    }
}
