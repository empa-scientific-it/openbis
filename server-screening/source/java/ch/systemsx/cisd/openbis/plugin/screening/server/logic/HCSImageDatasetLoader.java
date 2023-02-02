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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility class for loading HCS image datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class HCSImageDatasetLoader extends PlateDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    protected static Collection<Long> extractIds(List<AbstractExternalData> datasets)
    {
        List<Long> ids = new ArrayList<Long>();
        for (AbstractExternalData dataset : datasets)
        {
            ids.add(dataset.getId());
        }
        return ids;
    }

    private static Map<Long/* child data set id */, AbstractExternalData/* parent data sets */> createChildToParentDataSetsMap(
            Map<Long, Set<Long>> childIdToParentIdsMap, List<AbstractExternalData> parentDatasets)
    {
        Map<Long, AbstractExternalData> childDataSetToParentDataSetsMap = new HashMap<Long, AbstractExternalData>();
        for (Entry<Long, Set<Long>> entry : childIdToParentIdsMap.entrySet())
        {
            List<AbstractExternalData> parents = findDatasetsWithIds(entry.getValue(), parentDatasets);
            // NOTE: if a child data set has more than one parent data set, all the
            // parents will be ignored.
            if (parents.size() == 1)
            {
                Long childId = entry.getKey();
                childDataSetToParentDataSetsMap.put(childId, parents.get(0));
            }
        }
        return childDataSetToParentDataSetsMap;
    }

    private static List<AbstractExternalData> findDatasetsWithIds(Set<Long> datasetIds,
            List<AbstractExternalData> datasets)
    {
        List<AbstractExternalData> found = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataset : datasets)
        {
            if (datasetIds.contains(dataset.getId()))
            {
                found.add(dataset);
            }
        }
        return found;
    }

    HCSImageDatasetLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            String homeSpaceOrNull, Set<? extends PlateIdentifier> plates,
            String... datasetTypeCodes)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                (datasetTypeCodes.length == 0) ? new String[]
                { ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN } : datasetTypeCodes);
    }

    /**
     * Return the image datasets for the specified plates.
     */
    public List<AbstractExternalData> getImageDatasets()
    {
        load();
        return filterImageDatasets();
    }

    /**
     * Return the raw image datasets for the specified plates.
     */
    private List<AbstractExternalData> getRawImageDatasets()
    {
        load();
        return filterRawImageDatasets();
    }

    /**
     * Return the segmentation image datasets (overlays) for the specified plates.
     */
    private List<AbstractExternalData> getSegmentationImageDatasets()
    {
        load();
        List<AbstractExternalData> imageDatasets = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataset : getDatasets())
        {
            if (ScreeningUtils.isBasicHcsImageDataset(dataset))
            {
                imageDatasets.add(dataset);
            }
        }

        return fetchChildrenDataSets(imageDatasets,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN,
                createDatasetLister());

    }

    /**
     * Return the image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getImageDatasetReferences()
    {
        return asImageDatasetReferences(getImageDatasets());
    }

    /**
     * Return the raw image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getRawImageDatasetReferences()
    {
        return asImageDatasetReferences(getRawImageDatasets());
    }

    /**
     * Return the segmentation image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getSegmentationImageDatasetReferences()
    {
        return asImageDatasetReferences(getSegmentationImageDatasets());
    }

    private List<AbstractExternalData> filterImageDatasets()
    {
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData externalData : getDatasets())
        {
            if (ScreeningUtils.isBasicHcsImageDataset(externalData))
            {
                if (externalData.tryGetContainer() == null)
                {
                    result.add(externalData);
                }
            }
        }
        return result;
    }

    private List<AbstractExternalData> filterRawImageDatasets()
    {
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData externalData : getDatasets())
        {
            if (ScreeningUtils.isRawHcsImageDataset(externalData))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    private List<ImageDatasetReference> asImageDatasetReferences(List<AbstractExternalData> imageDatasets)
    {
        List<ImageDatasetReference> references = new ArrayList<ImageDatasetReference>();
        for (AbstractExternalData imageDataset : imageDatasets)
        {
            ImageDatasetReference reference = tryAsImageDataset(imageDataset);
            if (reference != null)
            {
                references.add(reference);
            }
        }
        return references;
    }

    /** Sets parents of all datasets in 'childrenDataSets'. */
    protected void enrichWithParentDatasets(Collection<AbstractExternalData> childrenDataSets,
            List<AbstractExternalData> parentDataSets, String childTypePattern, IDatasetLister datasetLister)
    {
        Map<Long/* child id */, AbstractExternalData/* parent */> childIdToParentDataSetMap =
                createChildToParentDataSetsMap(parentDataSets, childTypePattern, datasetLister);
        enrichWithParentDatasets(childrenDataSets, childIdToParentDataSetMap);
    }

    private void enrichWithParentDatasets(Collection<AbstractExternalData> childrenDataSets,
            Map<Long, AbstractExternalData> childIdToParentDataSetMap)
    {
        for (AbstractExternalData child : childrenDataSets)
        {
            AbstractExternalData parentImageDataset = childIdToParentDataSetMap.get(child.getId());
            if (parentImageDataset != null)
            {
                child.setParents(Arrays.asList(parentImageDataset));
            }
        }
    }

    private Map<Long, AbstractExternalData> createChildToParentDataSetsMap(
            List<AbstractExternalData> parentDataSets, String childTypePattern, IDatasetLister datasetLister)
    {
        List<AbstractExternalData> filteredChildrenForParents =
                listFilteredChildrenDataSets(parentDataSets, childTypePattern, datasetLister);
        return createChildToParentDataSetsMap(filteredChildrenForParents, parentDataSets,
                datasetLister);
    }

    /**
     * Fetched children datasets (with a matching type) for the specified parent datasets, sets their parents.
     */
    protected List<AbstractExternalData> fetchChildrenDataSets(List<AbstractExternalData> parentDataSets,
            String childTypePattern, IDatasetLister datasetLister)
    {
        List<AbstractExternalData> filteredChildrenForParents =
                listFilteredChildrenDataSets(parentDataSets, childTypePattern, datasetLister);
        Map<Long, AbstractExternalData> childIdToParentDataSetsMap =
                createChildToParentDataSetsMap(filteredChildrenForParents, parentDataSets,
                        datasetLister);

        setParentDatasets(filteredChildrenForParents, childIdToParentDataSetsMap);
        return filteredChildrenForParents;
    }

    private static void setParentDatasets(List<AbstractExternalData> childrenDatasets,
            Map<Long, AbstractExternalData> childIdToParentDataSetsMap)
    {
        for (AbstractExternalData child : childrenDatasets)
        {
            AbstractExternalData parentImageDataset = childIdToParentDataSetsMap.get(child.getId());
            if (parentImageDataset != null)
            {
                child.setParents(Arrays.asList(parentImageDataset));
            }
        }
    }

    private Map<Long, AbstractExternalData> createChildToParentDataSetsMap(
            List<AbstractExternalData> childrenDataSets, List<AbstractExternalData> parentDataSets,
            IDatasetLister datasetLister)
    {
        Collection<Long> childrenIds = extractIds(childrenDataSets);
        Map<Long, Set<Long>> childIdToParentIdsMap = datasetLister.listParentIds(childrenIds);
        return createChildToParentDataSetsMap(childIdToParentIdsMap, parentDataSets);
    }

    protected IDatasetLister createDatasetLister()
    {
        return businessObjectFactory.createDatasetLister(session);
    }

    private List<AbstractExternalData> listFilteredChildrenDataSets(List<AbstractExternalData> parentDataSets,
            String childTypePattern, IDatasetLister datasetLister)
    {
        return ScreeningUtils.filterExternalDataByTypePattern(
                datasetLister.listByParentTechIds(extractIds(parentDataSets)), childTypePattern);
    }

    private AbstractExternalData tryGetParent(AbstractExternalData externalData)
    {
        if (externalData.getParents() != null && externalData.getParents().size() == 1)
        {
            return externalData.getParents().iterator().next();
        } else
        {
            return null;
        }
    }

    protected ImageDatasetReference tryAsImageDataset(AbstractExternalData externalData)
    {
        if (externalData == null
                || ScreeningUtils.isTypeMatching(externalData, ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN) == false
                || externalData.getSample() == null)
        {
            return null;
        }
        DataStore dataStore = externalData.getDataStore();
        DataSetType dataSetType = externalData.getDataSetType();
        String dataSetTypeCodeOrNull = dataSetType == null ? null : dataSetType.getCode();

        return new ImageDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull,
                getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(externalData),
                createExperimentIdentifier(externalData), extractPlateGeometry(externalData),
                externalData.getRegistrationDate(), extractProperties(externalData),
                tryAsImageDataset(tryGetParent(externalData)));
    }
}
