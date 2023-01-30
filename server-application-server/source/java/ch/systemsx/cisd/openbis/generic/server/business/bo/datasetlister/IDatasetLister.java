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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;

/**
 * A class for fast dataset listing.
 * 
 * @author Tomasz Pylak
 */
public interface IDatasetLister
{
    /**
     * @return datasets connected to the experiment with the specified id
     * @param showOnlyDirectlyConnected whether to return only directly connected datasets, or also all descendants in dataset parent-child
     *            relationship hierarchy
     */
    List<AbstractExternalData> listByExperimentTechId(TechId experimentId, boolean showOnlyDirectlyConnected);

    /**
     * @return datasets connected to the sample with the specified id
     * @param showOnlyDirectlyConnected whether to return only directly connected datasets, or also all descendants in dataset parent-child
     *            relationship hierarchy
     */
    List<AbstractExternalData> listBySampleTechId(TechId sampleId, boolean showOnlyDirectlyConnected);

    /** @return datasets that are parents of a dataset with the specified id */
    List<AbstractExternalData> listByChildTechId(TechId childDatasetId);

    /** @return datasets that are containers of a dataset with the specified id */
    List<AbstractExternalData> listByComponentTechId(TechId componentDatasetId);

    /** @return datasets that are components of a dataset with the specified id */
    List<AbstractExternalData> listByContainerTechId(TechId containerDatasetId);

    /** @return all datasets that are children of any specified dataset id */
    List<AbstractExternalData> listByParentTechIds(Collection<Long> parentDatasetIds);

    /** @return datasets connected to the metaproject with the specified id */
    List<AbstractExternalData> listByMetaprojectId(Long metaprojectId);

    /**
     * Returns a map with all parent data set IDs of specified data set IDs. The keys of the map are IDs from the argument. A value of the map
     * contains at least one element.
     */
    Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs);

    Map<Long, Set<Long>> listContainerIds(Collection<Long> dataSetIDs);

    /**
     * Returns a map with all child data set IDs of specified data set IDs. The keys of the map are IDs from the argument. A value of the map contains
     * at least one element.
     */
    Map<Long, Set<Long>> listChildrenIds(Collection<Long> dataSetIDs);

    Map<Long, Set<Long>> listComponetIds(Collection<Long> dataSetIDs);

    /**
     * Returns a map with all data sets of specified samples. The sample arguments are the key into the returned map. The returned data sets contains
     * all derived data sets (children, grand children, etc.).
     */
    Map<Sample, List<AbstractExternalData>> listAllDataSetsFor(List<Sample> samples);

    /**
     * Lists all data sets with specified codes.
     */
    List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes);

    /**
     * @param datasetCodes Codes of datasets.
     * @param datasetFetchOptions The options of what datasets to fetch. Lists all data sets with specified codes.
     */
    List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists all physical data sets of specified data store.
     */
    List<AbstractExternalData> listByDataStore(long dataStoreID);

    /**
     * Lists all physical data sets of specified data store.
     * 
     * @param datasetFetchOptions The options of what datasets to fetch.
     */
    List<AbstractExternalData> listByDataStore(long dataStoreID,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists the oldest <var>limit</var> physical datasets of the specified data store.
     * 
     * @param datasetFetchOptions The options of what datasets to fetch.
     */
    public List<AbstractExternalData> listByDataStore(long dataStoreID, int limit,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists the oldest <var>limit</var> physical datasets younger than <var>youngerThan</var> of the specified data store.
     * 
     * @param datasetFetchOptions The options of what datasets to fetch.
     */
    public List<AbstractExternalData> listByDataStore(long dataStoreID, Date youngerThan, int limit,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists physical datasets with unknown size of the specified data store.
     */
    List<AbstractExternalData> listByDataStoreWithUnknownSize(long dataStoreID, int limit, String dataSetCodeLowerLimit,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists physical datasets by archiving status of the specified data store.
     */
    List<AbstractExternalData> listByArchivingStatus(long dataStoreID, DataSetArchivingStatus archivingStatus, Boolean presentInArchive,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * Lists {@link DataSetShareId}s of all data sets (even those in trash) in specified data store.
     */
    List<DataSetShareId> listAllDataSetShareIdsByDataStore(long dataStoreID);

    /** @return datasets with given ids */
    List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds);

    /**
     * @param datasetIds Database ids of datasets.
     * @param datasetFetchOptions The options of what datasets to fetch.
     * @return datasets with given ids
     */
    List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /** @return datasets specified by given criteria */
    List<AbstractExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria);

    /** @return datasets specified by given criteria */
    List<AbstractExternalData> listByArchiverCriteria(String dataStoreCode, ArchiverDataSetCriteria criteria);

    /**
     * @return Datasets connected to the samples with the specified ids
     */
    List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds);

    /**
     * @return Datasets connected to the samples with the specified ids
     */
    List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions);

    /**
     * @return Location of the specified data set.
     */
    IDatasetLocationNode listLocationsByDatasetCode(String datasetCode);

    /**
     * @return properties of given type for given dataset ids
     */
    Map<Long, GenericEntityPropertyRecord> fetchProperties(List<Long> ids, String propertyTypeCode);

    /**
     * @return codes of the datasets, that are contained in the given dataset
     */
    List<String> listContainedCodes(String datasetCode);

    /**
     * @return list of not archived data sets marked with a tag
     */
    List<AbstractExternalData> listByMetaprojectIdAndArchivalState(Long metaprojectId, boolean isArchived);

}
