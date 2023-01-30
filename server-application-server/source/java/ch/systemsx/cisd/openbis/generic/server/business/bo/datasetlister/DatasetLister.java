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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileSystemContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UrlContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = { DatasetRecord.class, DatasetRelationRecord.class, DataStoreRecord.class,
        DatasetCodeWithShareIdRecord.class, IDatasetListingQuery.class })
public class DatasetLister extends AbstractLister implements IDatasetLister
{
    public static final EnumSet<DataSetFetchOption> SUPPORTED_DATASET_FETCH_OPTIONS = EnumSet.of(
            DataSetFetchOption.BASIC, DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE,
            DataSetFetchOption.PROPERTIES, DataSetFetchOption.CHILDREN, DataSetFetchOption.PARENTS,
            DataSetFetchOption.PROPERTIES_OF_PARENTS, DataSetFetchOption.PROPERTIES_OF_CHILDREN,
            DataSetFetchOption.CONTAINER, DataSetFetchOption.CONTAINED,
            DataSetFetchOption.METAPROJECTS);

    public static final EnumSet<DataSetFetchOption> DEFAULT_DATASET_FETCH_OPTIONS = EnumSet.of(
            DataSetFetchOption.BASIC, DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE,
            DataSetFetchOption.PROPERTIES, DataSetFetchOption.PARENTS,
            DataSetFetchOption.CONTAINER, DataSetFetchOption.CONTAINED,
            DataSetFetchOption.METAPROJECTS);

    //
    // Input
    //

    private final String baseIndexURL;

    private final Long userId;

    //
    // Working interfaces
    //

    private final IDatasetListingQuery query;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private final SecondaryEntityDAO referencedEntityDAO;

    private final IDataDAO dataDAO;

    //
    // Working data structures
    //

    private final Long2ObjectMap<DataSetType> dataSetTypes =
            new Long2ObjectOpenHashMap<DataSetType>();

    private final Long2ObjectMap<DataStore> dataStores = new Long2ObjectOpenHashMap<DataStore>();

    private final Map<Long, FileFormatType> fileFormatTypes = new HashMap<Long, FileFormatType>();

    private final Map<Long, LocatorType> locatorTypes = new HashMap<Long, LocatorType>();

    private final Long2ObjectMap<ExternalDataManagementSystem> externalDataManagementSystems =
            new Long2ObjectOpenHashMap<ExternalDataManagementSystem>();

    private IRelationshipTypeDAO relationshipTypeDAO;

    public static IDatasetLister create(IDAOFactory daoFactory, String baseIndexURL, Long userId)
    {
        DatasetListerDAO dao = DatasetListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);
        IDataDAO dataDAO = daoFactory.getDataDAO();
        IRelationshipTypeDAO relationshipTypeDAO = daoFactory.getRelationshipTypeDAO();

        return create(dao, referencedEntityDAO, dataDAO, relationshipTypeDAO, baseIndexURL, userId);
    }

    static IDatasetLister create(DatasetListerDAO dao, SecondaryEntityDAO referencedEntityDAO, IDataDAO dataDAO,
            IRelationshipTypeDAO relationshipTypeDAO, String baseIndexURL, Long userId)
    {
        IDatasetListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO, dataDAO, relationshipTypeDAO, baseIndexURL, userId);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO, IDataDAO dataDAO, IRelationshipTypeDAO relationshipTypeDAO,
            String baseIndexURL, Long userId)
    {
        super(referencedEntityDAO);
        assert databaseInstance != null;
        assert query != null;

        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
        this.referencedEntityDAO = referencedEntityDAO;
        this.dataDAO = dataDAO;
        this.relationshipTypeDAO = relationshipTypeDAO;
        this.baseIndexURL = baseIndexURL;
        this.userId = userId;
    }

    private void checkFetchOptions(EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        EnumSet<DataSetFetchOption> work = EnumSet.copyOf(datasetFetchOptions);
        work.removeAll(SUPPORTED_DATASET_FETCH_OPTIONS);
        if (work.isEmpty() == false)
        {
            throw new IllegalArgumentException("Currently only " + SUPPORTED_DATASET_FETCH_OPTIONS
                    + " fetch options are supported by this method");
        }
    }

    @Override
    public List<AbstractExternalData> listBySampleTechId(TechId sampleId,
            boolean showOnlyDirectlyConnected)
    {
        if (showOnlyDirectlyConnected)
        {
            return enrichDatasets(query.getDatasetsForSample(sampleId.getId()));
        } else
        {
            // get all descendands of the sample
            LongSet sampleIds = referencedEntityDAO.getSampleDescendantIdsAndSelf(sampleId.getId());
            // get directly connected datasets, then go layer by layer into children datasets
            LongSet results = new LongOpenHashSet();
            LongSet currentLayer = new LongOpenHashSet(query.getDatasetIdsForSamples(sampleIds));
            LongSet nextLayer;
            while (currentLayer.isEmpty() == false)
            {
                Long relationshipTypeId = getParentChildRelationshipTypeId();
                nextLayer = new LongOpenHashSet(query.getDatasetChildrenIds(currentLayer, relationshipTypeId));
                results.addAll(currentLayer);
                nextLayer.removeAll(results); // don't go twice through the same dataset
                currentLayer = nextLayer;
            }
            return listByDatasetIds(results);
        }
    }

    @Override
    public List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds)
    {
        return listBySampleIds(sampleIds, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        LongSet ids = new LongOpenHashSet();
        for (Long id : sampleIds)
        {
            ids.add(id);
        }
        return enrichDatasets(query.getDatasetsForSamples(ids), datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByExperimentTechId(TechId experimentId,
            boolean showOnlyDirectlyConnected)
    {
        DataIterator<DatasetRecord> dataSets;
        if (showOnlyDirectlyConnected)
        {
            dataSets = query.getDatasetsForExperiment(experimentId.getId());
        } else
        {
            Long relationshipTypeId = getParentChildRelationshipTypeId();
            dataSets = query.getDataSetsForExperimentAndDescendents(experimentId.getId(), relationshipTypeId);
        }
        return enrichDatasets(dataSets);
    }

    @Override
    public List<AbstractExternalData> listByMetaprojectId(Long metaprojectId)
    {
        DataIterator<DatasetRecord> dataSets = query.getDatasetsForMetaproject(metaprojectId);
        return enrichDatasets(dataSets);
    }

    @Override
    public Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs)
    {
        return listParentIds(dataSetIDs, getParentChildRelationshipTypeId());
    }

    @Override
    public Map<Long, Set<Long>> listContainerIds(Collection<Long> dataSetIDs)
    {
        return listParentIds(dataSetIDs, getContainerComponentRelationshipTypeId());
    }

    private Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs, Long relationshipTypeId)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listParentDataSetIds(ids, relationshipTypeId);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> parents = map.get(relationship.data_id_child);
            if (parents == null)
            {
                parents = new LinkedHashSet<Long>();
                map.put(relationship.data_id_child, parents);
            }
            parents.add(relationship.data_id_parent);
        }
        return map;
    }

    @Override
    public Map<Long, Set<Long>> listChildrenIds(Collection<Long> dataSetIDs)
    {
        return listChilderenIds(dataSetIDs, getParentChildRelationshipTypeId());
    }

    @Override
    public Map<Long, Set<Long>> listComponetIds(Collection<Long> dataSetIDs)
    {
        return listChilderenIds(dataSetIDs, getContainerComponentRelationshipTypeId());
    }

    private Map<Long, Set<Long>> listChilderenIds(Collection<Long> dataSetIDs, Long relationshipTypeId)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listChildrenDataSetIds(ids, relationshipTypeId);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> children = map.get(relationship.data_id_parent);
            if (children == null)
            {
                children = new LinkedHashSet<Long>();
                map.put(relationship.data_id_parent, children);
            }
            children.add(relationship.data_id_child);
        }
        return map;
    }

    @Override
    public Map<Sample, List<AbstractExternalData>> listAllDataSetsFor(List<Sample> samples)
    {
        TableMap<Long, Sample> samplesByID =
                new TableMap<Long, Sample>(samples, new IKeyExtractor<Long, Sample>()
                    {
                        @Override
                        public Long getKey(Sample e)
                        {
                            return e.getId();
                        }
                    });
        Map<Sample, List<AbstractExternalData>> result =
                new HashMap<Sample, List<AbstractExternalData>>();
        Set<Long> sampleIDs = new HashSet<Long>();
        for (Sample sample : samples)
        {
            result.put(sample, new ArrayList<AbstractExternalData>());
            sampleIDs.add(sample.getId());
        }
        List<AbstractExternalData> rootDataSets = listBySampleIds(sampleIDs,
                EnumSet.of(DataSetFetchOption.BASIC, DataSetFetchOption.PROPERTIES));
        addChildren(rootDataSets);
        for (AbstractExternalData dataSet : rootDataSets)
        {
            Sample sample = samplesByID.tryGet(dataSet.getSample().getId());
            assert sample != null;
            result.get(sample).add(dataSet);
        }
        return result;
    }

    private void addChildren(List<AbstractExternalData> dataSets)
    {
        Map<Long, AbstractExternalData> dataSetsByID = new HashMap<Long, AbstractExternalData>();
        for (AbstractExternalData dataSet : dataSets)
        {
            dataSetsByID.put(dataSet.getId(), dataSet);
        }
        Map<Long, Set<Long>> childrenIDs = listChildrenIds(dataSetsByID.keySet());
        Set<Entry<Long, Set<Long>>> entrySet = childrenIDs.entrySet();
        Map<Long, Set<Long>> child2ParentsMap = new HashMap<Long, Set<Long>>();
        Set<Long> childIDs = new HashSet<Long>();
        for (Entry<Long, Set<Long>> entry : entrySet)
        {
            Set<Long> value = entry.getValue();
            Long parentID = entry.getKey();
            for (Long childId : value)
            {
                childIDs.add(childId);
                Set<Long> parents = child2ParentsMap.get(childId);
                if (parents == null)
                {
                    parents = new HashSet<Long>(1);
                    child2ParentsMap.put(childId, parents);
                }
                parents.add(parentID);
            }
        }
        if (childIDs.isEmpty() == false)
        {
            List<AbstractExternalData> children = listByDatasetIds(childIDs,
                    EnumSet.of(DataSetFetchOption.BASIC, DataSetFetchOption.PROPERTIES));
            for (AbstractExternalData child : children)
            {
                Set<Long> parentIDs = child2ParentsMap.get(child.getId());
                for (Long parentID : parentIDs)
                {
                    AbstractExternalData dataSet = dataSetsByID.get(parentID);
                    Collection<AbstractExternalData> childList = dataSet.getChildren();
                    if (childList == null)
                    {
                        childList = new ArrayList<AbstractExternalData>(1);
                        dataSet.setChildren(childList);
                    }
                    childList.add(child);
                }
            }
            addChildren(children);
        }
    }

    @Override
    public List<AbstractExternalData> listByChildTechId(TechId childDatasetId)
    {
        return enrichDatasets(query.getParentsOf(new LongOpenHashSet(Arrays.asList(childDatasetId.getId())),
                getParentChildRelationshipTypeId()));
    }

    @Override
    public List<AbstractExternalData> listByComponentTechId(TechId componentDatasetId)
    {
        return enrichDatasets(query.getParentsOf(new LongOpenHashSet(Arrays.asList(componentDatasetId.getId())),
                getContainerComponentRelationshipTypeId()));
    }

    @Override
    public List<AbstractExternalData> listByContainerTechId(TechId containerDatasetId)
    {
        return enrichDatasets(query.getChildrenOf(new LongOpenHashSet(Arrays.asList(containerDatasetId.getId())),
                getContainerComponentRelationshipTypeId()));
    }

    @Override
    public List<AbstractExternalData> listByParentTechIds(Collection<Long> parentDatasetIds)
    {
        return enrichDatasets(query.getChildrenOf(new LongOpenHashSet(parentDatasetIds),
                getParentChildRelationshipTypeId()));
    }

    @Override
    public List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds)
    {
        return listByDatasetIds(datasetIds, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return enrichDatasets(query.getDatasets(new LongOpenHashSet(datasetIds)),
                datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes)
    {
        return listByDatasetCode(datasetCodes, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        String[] codes = datasetCodes.toArray(new String[datasetCodes.size()]);
        DataIterator<DatasetRecord> datasets = query.getDatasets(codes);
        return enrichDatasets(datasets, datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID)
    {
        return listByDataStore(dataStoreID, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return enrichDatasets(query.getDatasetsByDataStoreId(dataStoreID), datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID, int limit,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        List<AbstractExternalData> data = null;
        int multiplier = 1;
        int lastSize = 0;

        // Given limit is not respected, if registration timestamp of all the returned data sets
        // would be the same. In this case we make sure we return (at least) all the data sets
        // with the same registration timestamp, no matter how many of them there are.
        while (data == null
                || (data.get(0).getRegistrationDate().equals(data.get(data.size() - 1)
                        .getRegistrationDate())) && lastSize != data.size())
        {
            lastSize = data != null ? data.size() : 0;
            data =
                    orderByDate(enrichDatasets(
                            handleDegenerateRegistrationTimestamp(
                                    query.getDatasetsByDataStoreId(dataStoreID, limit * multiplier),
                                    dataStoreID),
                            datasetFetchOptions));
            multiplier = multiplier << 1;
        }

        return data;
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID, Date youngerThan,
            int limit, EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return orderByDate(enrichDatasets(
                handleDegenerateRegistrationTimestamp(
                        query.getDatasetsByDataStoreId(dataStoreID, youngerThan, limit),
                        dataStoreID),
                datasetFetchOptions));
    }

    @Override
    public List<AbstractExternalData> listByArchivingStatus(long dataStoreID, DataSetArchivingStatus archivingStatus, Boolean presentInArchive,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);

        List<DatasetRecord> dataSets = null;

        if (archivingStatus != null)
        {
            String statusAsString = archivingStatus.name();
            if (presentInArchive != null)
            {
                dataSets = query.getDatasetsByDataStoreIdWithArchivingStatusAndPressentInArchive(dataStoreID,
                        statusAsString, presentInArchive);
            } else
            {
                dataSets = query.getDatasetsByDataStoreIdWithArchivingStatus(dataStoreID, statusAsString);
            }
        } else if (presentInArchive != null)
        {
            dataSets = query.getDatasetsByDataStoreIdWithPressentInArchive(dataStoreID, presentInArchive);
        } else
        {
            throw new UserFailureException("Neither archiving status nor present in archive flag are specified.");
        }

        return orderByCode(enrichDatasets(dataSets, datasetFetchOptions));
    }

    @Override
    public List<AbstractExternalData> listByDataStoreWithUnknownSize(long dataStoreID, int limit, String dataSetCodeLowerLimit,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);

        List<DatasetRecord> dataSets;

        if (dataSetCodeLowerLimit == null)
        {
            dataSets = query.getDatasetsByDataStoreIdWithUnknownSize(dataStoreID, limit);
        } else
        {
            dataSets = query.getDatasetsByDataStoreIdWithUnknownSize(dataStoreID, limit, dataSetCodeLowerLimit);
        }

        return orderByCode(enrichDatasets(dataSets, datasetFetchOptions));
    }

    private Iterable<DatasetRecord> handleDegenerateRegistrationTimestamp(List<DatasetRecord> list,
            long dataStoreID)
    {
        if (list.isEmpty())
        {
            return list;
        }
        final Date youngestDate = list.get(list.size() - 1).registration_timestamp;
        final List<DatasetRecord> youngestRecords =
                query.getDatasetsByDataStoreId(dataStoreID, youngestDate);
        // Check for degenerate case of multiple datasets having exactly regDate = youngestDate.
        if (youngestRecords.size() > 1)
        {
            final Long2ObjectMap<DatasetRecord> datasetMap =
                    new Long2ObjectOpenHashMap<DatasetRecord>(list.size() + youngestRecords.size());
            for (DatasetRecord dr : list)
            {
                datasetMap.put(dr.id, dr);
            }
            for (DatasetRecord dr : youngestRecords)
            {
                datasetMap.put(dr.id, dr);
            }
            return datasetMap.values();
        } else
        {
            return list;
        }
    }

    private List<AbstractExternalData> orderByDate(List<AbstractExternalData> list)
    {
        Collections.sort(list, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData o1, AbstractExternalData o2)
                {
                    return o1.getRegistrationDate().compareTo(o2.getRegistrationDate());
                }
            });
        return list;
    }

    private List<AbstractExternalData> orderByCode(List<AbstractExternalData> list)
    {
        Collections.sort(list, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData o1, AbstractExternalData o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        return list;
    }

    @Override
    public List<DataSetShareId> listAllDataSetShareIdsByDataStore(long dataStoreID)
    {
        List<DataSetShareId> results = new ArrayList<DataSetShareId>();

        DataIterator<DatasetCodeWithShareIdRecord> records =
                query.getAllDatasetsWithShareIdsByDataStoreId(dataStoreID);
        for (DatasetCodeWithShareIdRecord record : records)
        {
            DataSetShareId dataSetShareId = new DataSetShareId();
            dataSetShareId.setDataSetCode(record.code);
            dataSetShareId.setShareId(record.share_id);
            results.add(dataSetShareId);
        }
        return results;
    }

    @Override
    public List<AbstractExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria)
    {
        DataIterator<DatasetRecord> dataSets;
        String sampleType = criteria.getConnectedSampleTypeCode();
        long lastSeenDataSetId = criteria.getLastSeenDataSetId();
        if (sampleType == null)
        {
            dataSets = query.getNewDataSets(lastSeenDataSetId);
        } else
        {
            Long sampleTypeId = referencedEntityDAO.getSampleTypeIdForSampleTypeCode(sampleType);
            dataSets = query.getNewDataSetsForSampleType(sampleTypeId, lastSeenDataSetId);
        }
        if (criteria.shouldResultBeEnriched())
        {
            return enrichDatasets(dataSets);
        }
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(dataSets);
        Long2ObjectMap<AbstractExternalData> datasetMap = createPrimaryDatasets(datasetRecords);
        return asList(datasetMap);
    }

    @Override
    public List<AbstractExternalData> listByArchiverCriteria(String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        loadSmallConnectedTables();
        final Long dataStoreId = extractDataStoreId(dataStoreCode);
        final Date lastAccessDate = extractLastAccessDate(criteria);
        final String dataSetTypeCodeOrNull = criteria.tryGetDataSetTypeCode();
        final boolean presentInArchive = criteria.isPresentInArchive();

        if (dataDAO.isAccessTimestampEnabled())
        {
            if (dataSetTypeCodeOrNull == null)
            {
                return enrichDatasets(query.getAvailableExtDatasAccessedBefore(dataStoreId,
                        lastAccessDate, presentInArchive));
            } else
            {
                Long dataSetTypeId = extractDataSetTypeId(dataSetTypeCodeOrNull);
                return enrichDatasets(query.getAvailableExtDatasAccessedBeforeWithDataSetType(
                        dataStoreId, lastAccessDate, presentInArchive, dataSetTypeId));
            }
        } else
        {
            if (dataSetTypeCodeOrNull == null)
            {
                return enrichDatasets(query.getAvailableExtDatasRegisteredBefore(dataStoreId,
                        lastAccessDate, presentInArchive));
            } else
            {
                Long dataSetTypeId = extractDataSetTypeId(dataSetTypeCodeOrNull);
                return enrichDatasets(query.getAvailableExtDatasRegisteredBeforeWithDataSetType(
                        dataStoreId, lastAccessDate, presentInArchive, dataSetTypeId));
            }
        }
    }

    private Date extractLastAccessDate(ArchiverDataSetCriteria criteria)
    {
        return DateUtils.addDays(new Date(), -criteria.getOlderThan());
    }

    private Long extractDataStoreId(String dataStoreCode)
    {
        for (Entry<Long, DataStore> entry : dataStores.entrySet())
        {
            if (dataStoreCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data store '" + dataStoreCode + "' unknown.");
    }

    private Long extractDataSetTypeId(String dataSetTypeCode)
    {
        for (Entry<Long, DataSetType> entry : dataSetTypes.entrySet())
        {
            if (dataSetTypeCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data Set type '" + dataSetTypeCode + "' unknown.");
    }

    private List<AbstractExternalData> enrichDatasets(Iterable<DatasetRecord> datasets)
    {
        return enrichDatasets(datasets, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    private List<AbstractExternalData> enrichDatasets(Iterable<DatasetRecord> datasets,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(datasets);
        final Long2ObjectMap<AbstractExternalData> datasetMap =
                createPrimaryDatasets(datasetRecords);
        if (fetchOptions.contains(DataSetFetchOption.EXPERIMENT))
        {
            enrichWithExperiments(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.PROPERTIES)
                || fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_PROPERTIES))
        {
            enrichWithProperties(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.SAMPLE))
        {
            enrichWithSamples(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.CONTAINER))
        {
            enrichWithContainers(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.CONTAINED))
        {
            enrichWithContainedDataSets(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.PARENTS))
        {
            enrichWithParents(datasetMap,
                    fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_PARENTS));
        }
        if (fetchOptions.contains(DataSetFetchOption.CHILDREN))
        {
            enrichWithChildren(datasetMap,
                    fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_CHILDREN));
        }
        if (this.userId != null && fetchOptions.contains(DataSetFetchOption.METAPROJECTS))
        {
            enrichWithMetaProjects(datasetMap);
        }
        return asList(datasetMap);
    }

    private void enrichWithMetaProjects(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet set = new LongOpenHashSet();
        set.addAll(datasetMap.keySet());

        for (MetaProjectWithEntityId metaProject : query.getMetaprojects(set, userId))
        {
            Metaproject mp = new Metaproject();
            mp.setId(metaProject.id);
            mp.setCreationDate(metaProject.creation_date);
            mp.setDescription(metaProject.description);
            mp.setIdentifier("/" + metaProject.owner_name + "/" + metaProject.name);
            mp.setName(metaProject.name);
            mp.setOwnerId(metaProject.owner_name);
            mp.setPrivate(metaProject.is_private);

            AbstractExternalData data = datasetMap.get(metaProject.entity_id);

            if (data != null)
            {
                Collection<Metaproject> mps = data.getMetaprojects();
                if (mps == null)
                {
                    mps = new HashSet<Metaproject>();
                    data.setMetaprojects(mps);
                }
                mps.add(mp);
            }
        }
    }

    // assumes that the connection to the sample has been already established and sample has the
    // id set.
    private void enrichWithSamples(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = extractSampleIds(datasetMap);
        Long2ObjectMap<Sample> samples = referencedEntityDAO.getSamples(ids);
        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                Sample sample = samples.get(sampleId);
                dataset.setSample(sample);
            }
        }
    }

    private static LongSet extractSampleIds(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = new LongOpenHashSet();
        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                ids.add(sampleId);
            }
        }
        return ids;
    }

    private static LongSet extractExperimentIds(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = new LongOpenHashSet();
        for (AbstractExternalData dataset : datasetMap.values())
        {
            Experiment experiment = dataset.getExperiment();
            if (experiment != null)
            {
                ids.add(experiment.getId());
            }
        }
        return ids;
    }

    // assumes that the connection to experiment has been already established and experiment has the
    // id set.
    private void enrichWithExperiments(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = extractExperimentIds(datasetMap);
        Long2ObjectMap<Experiment> experiments = referencedEntityDAO.getExperiments(ids);

        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getExperiment() == null)
            {
                continue;
            }
            long experimentId = dataset.getExperiment().getId();
            dataset.setExperiment(experiments.get(experimentId));
        }
    }

    private static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    private void enrichWithProperties(final Long2ObjectMap<AbstractExternalData> resultMap)
    {
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                @Override
                public AbstractExternalData get(long id)
                {
                    return resultMap.get(id);
                }
            });
    }

    private void enrichWithParents(Long2ObjectMap<AbstractExternalData> datasetMap,
            boolean withProperties)
    {
        Map<Long, Set<Long>> parentIdsMap = listParentIds(datasetMap.keySet());
        Set<Long> allParentIds = new HashSet<Long>();

        for (Set<Long> parentIds : parentIdsMap.values())
        {
            allParentIds.addAll(parentIds);
        }

        DataIterator<DatasetRecord> parentIterator =
                query.getDatasets(new LongOpenHashSet(allParentIds));

        if (parentIterator != null)
        {
            Long2ObjectMap<AbstractExternalData> parentDatasetMap =
                    withProperties ? new Long2ObjectOpenHashMap<AbstractExternalData>() : null;
            Long2ObjectMap<AbstractExternalData> parentMap = createPrimaryDatasets(parentIterator);

            for (Entry<Long, Set<Long>> parentIdsEntry : parentIdsMap.entrySet())
            {
                Long datasetId = parentIdsEntry.getKey();
                Set<Long> parentIds = parentIdsEntry.getValue();

                AbstractExternalData dataset = datasetMap.get(datasetId);
                List<AbstractExternalData> parents = new ArrayList<AbstractExternalData>();

                for (Long parentId : parentIds)
                {
                    AbstractExternalData parent = parentMap.get(parentId);
                    if (parent != null)
                    {
                        parents.add(parent);
                        if (parentDatasetMap != null)
                        {
                            parentDatasetMap.put(parentId, parent);
                        }
                    }
                }

                dataset.setParents(parents);
            }
            if (parentDatasetMap != null)
            {
                enrichWithProperties(parentDatasetMap);
            }
        }
    }

    private void enrichWithChildren(Long2ObjectMap<AbstractExternalData> datasetMap,
            boolean withProperties)
    {
        Map<Long, Set<Long>> childrenIdsMap = listChildrenIds(datasetMap.keySet());
        Set<Long> allChildrenIds = new HashSet<Long>();

        for (Set<Long> childrenIds : childrenIdsMap.values())
        {
            allChildrenIds.addAll(childrenIds);
        }

        DataIterator<DatasetRecord> childrenIterator =
                query.getDatasets(new LongOpenHashSet(allChildrenIds));

        if (childrenIterator != null)
        {
            Long2ObjectMap<AbstractExternalData> childrenDatasetMap =
                    withProperties ? new Long2ObjectOpenHashMap<AbstractExternalData>() : null;
            Long2ObjectMap<AbstractExternalData> childrenMap =
                    createPrimaryDatasets(childrenIterator);

            for (Entry<Long, Set<Long>> childrenIdsEntry : childrenIdsMap.entrySet())
            {
                Long datasetId = childrenIdsEntry.getKey();
                Set<Long> childrenIds = childrenIdsEntry.getValue();

                AbstractExternalData dataset = datasetMap.get(datasetId);
                List<AbstractExternalData> children = new ArrayList<AbstractExternalData>();

                for (Long childId : childrenIds)
                {
                    AbstractExternalData child = childrenMap.get(childId);
                    if (child != null)
                    {
                        children.add(child);
                        if (childrenDatasetMap != null)
                        {
                            childrenDatasetMap.put(childId, child);
                        }
                    }
                }
                dataset.setChildren(children);
            }
            if (childrenDatasetMap != null)
            {
                enrichWithProperties(childrenDatasetMap);
            }
        }
    }

    private void enrichWithContainers(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        Set<Long> containersNotLoaded = new HashSet<Long>();
        Long relationshipTypeId = getContainerComponentRelationshipTypeId();
        List<DatasetRelationRecord> relationshipRecords = asList(query.listParentDataSetIds(datasetMap.keySet(), relationshipTypeId));
        for (DatasetRelationRecord datasetRelationRecord : relationshipRecords)
        {
            AbstractExternalData component = datasetMap.get(datasetRelationRecord.data_id_child);
            assert component != null;
            long containerId = datasetRelationRecord.data_id_parent;
            AbstractExternalData container = datasetMap.get(containerId);
            if (container != null)
            {
                addContainer(component, container, datasetRelationRecord);
            } else
            {
                containersNotLoaded.add(containerId);
            }
        }
        if (containersNotLoaded.isEmpty())
        {
            return;
        }
        // load the unavailable container data sets with an additional query
        TableMap<Long, AbstractExternalData> containersSecondPassMap = loadDataSetsByIds(containersNotLoaded);
        for (DatasetRelationRecord datasetRelationRecord : relationshipRecords)
        {
            AbstractExternalData component = datasetMap.get(datasetRelationRecord.data_id_child);
            AbstractExternalData container = containersSecondPassMap.tryGet(datasetRelationRecord.data_id_parent);
            if (container != null)
            {
                addContainer(component, container, datasetRelationRecord);
            }
        }
    }

    private void enrichWithContainedDataSets(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet componentsNotLoaded = new LongOpenHashSet();
        Long relationshipTypeId = getContainerComponentRelationshipTypeId();
        List<DatasetRelationRecord> relationshipRecords = asList(query.listChildrenDataSetIds(datasetMap.keySet(), relationshipTypeId));
        for (DatasetRelationRecord datasetRelationRecord : relationshipRecords)
        {
            AbstractExternalData container = datasetMap.get(datasetRelationRecord.data_id_parent);
            assert container != null;
            long componentId = datasetRelationRecord.data_id_child;
            AbstractExternalData component = datasetMap.get(componentId);
            if (component != null)
            {
                addContainer(component, container, datasetRelationRecord);
            } else
            {
                componentsNotLoaded.add(componentId);
            }
        }
        if (componentsNotLoaded.isEmpty())
        {
            return;
        }
        Long2ObjectMap<AbstractExternalData> datasets = createPrimaryDatasets(query.getDatasets(componentsNotLoaded));
        for (DatasetRelationRecord datasetRelationRecord : relationshipRecords)
        {
            AbstractExternalData container = datasetMap.get(datasetRelationRecord.data_id_parent);
            AbstractExternalData component = datasets.get(datasetRelationRecord.data_id_child);
            if (component != null)
            {
                addContainer(component, container, datasetRelationRecord);
            }
        }
    }

    private TableMap<Long, AbstractExternalData> loadDataSetsByIds(Set<Long> dataSetIds)
    {
        List<AbstractExternalData> dataSets = listByDatasetIds(dataSetIds);
        return new TableMap<Long, AbstractExternalData>(dataSets,
                KeyExtractorFactory.<AbstractExternalData> createIdKeyExtractor());
    }

    private void addContainer(AbstractExternalData component, AbstractExternalData container, DatasetRelationRecord record)
    {
        if (container.isContainer() == false)
        {
            throw new IllegalStateException("Data set '" + container.getCode() + "' is not a container data set.");
        }
        component.addContainer(container.tryGetAsContainerDataSet(), record.ordinal);
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        result.addAll(items.values());
        return result;
    }

    private Long2ObjectMap<AbstractExternalData> createPrimaryDatasets(
            Iterable<DatasetRecord> records)
    {
        Long2ObjectMap<AbstractExternalData> datasets =
                new Long2ObjectOpenHashMap<AbstractExternalData>();
        LongSet ids = new LongOpenHashSet();
        for (DatasetRecord record : records)
        {
            ids.add(record.id);
            AbstractExternalData dataSetOrNull = null;
            if (record.data_set_kind.equals(DataSetKind.CONTAINER.name()))
            {
                dataSetOrNull = convertToContainerDataSet(record);
            } else if (record.data_set_kind.equals(DataSetKind.LINK.name()))
            {
                dataSetOrNull = convertToLinkDataSet(record);
            } else if (record.location != null)
            {
                dataSetOrNull = convertToDataSet(record);
            }

            if (dataSetOrNull != null)
            {
                enrichWithDeletion(dataSetOrNull, record);
                datasets.put(record.id, dataSetOrNull);
            }
        }
        return datasets;
    }

    // NOTE: this just marks the data set as invalid without loading any details
    private void enrichWithDeletion(final AbstractExternalData dataSet, DatasetRecord row)
    {
        if (row.del_id != null)
        {
            final Deletion deletion = new Deletion();
            dataSet.setDeletion(deletion);
        }
    }

    private PhysicalDataSet convertToDataSet(DatasetRecord record)
    {
        PhysicalDataSet dataSet = new PhysicalDataSet();
        convertStandardAttributes(dataSet, record);

        dataSet.setComplete(resolve(record.is_complete));
        dataSet.setStatus(record.status == null ? null : DataSetArchivingStatus
                .valueOf(record.status));
        dataSet.setPresentInArchive(record.present_in_archive == null ? false
                : record.present_in_archive);
        dataSet.setSpeedHint(record.speed_hint == null ? Constants.DEFAULT_SPEED_HINT
                : record.speed_hint);
        dataSet.setShareId(record.share_id);
        dataSet.setFileFormatType(record.ffty_id == null ? null : fileFormatTypes
                .get(record.ffty_id));
        dataSet.setStorageConfirmation(record.storage_confirmation == null ? false
                : record.storage_confirmation);
        dataSet.setLocation(record.location);
        dataSet.setSize(record.size);
        dataSet.setLocatorType(record.loty_id == null ? null : locatorTypes.get(record.loty_id));
        dataSet.setPostRegistered(record.is_post_registered);
        dataSet.setH5Folders(record.h5_folders);
        dataSet.setH5arFolders(record.h5ar_folders);
        dataSet.setArchivingRequested(record.archiving_requested);
        return dataSet;
    }

    private ContainerDataSet convertToContainerDataSet(DatasetRecord record)
    {
        ContainerDataSet containerDataSet = new ContainerDataSet();
        convertStandardAttributes(containerDataSet, record);
        return containerDataSet;
    }

    private LinkDataSet convertToLinkDataSet(DatasetRecord record)
    {
        LinkDataSet linkDataSet = new LinkDataSet();

        convertStandardAttributes(linkDataSet, record);

        List<ContentCopyRecord> copyRecords = query.getContentCopiesOf(record.id);
        List<IContentCopy> copies = new ArrayList<>();
        for (ContentCopyRecord copyRecord : copyRecords)
        {
            if (copyRecord.external_code != null)
            {
                copies.add(new UrlContentCopy(copyRecord.edms_code, copyRecord.edms_label, copyRecord.edms_address, copyRecord.external_code));
                linkDataSet.setExternalCode(copyRecord.external_code);
            } else
            {
                copies.add(new FileSystemContentCopy(copyRecord.edms_code, copyRecord.edms_label, copyRecord.edms_address, copyRecord.edms_address,
                        copyRecord.path, copyRecord.hash, copyRecord.repository_id));
            }
            linkDataSet.setExternalDataManagementSystem(externalDataManagementSystems
                    .get(copyRecord.edms_id));
        }

        linkDataSet.setCopies(copies);

        return linkDataSet;
    }

    private void convertStandardAttributes(AbstractExternalData dataSet, DatasetRecord record)
    {
        dataSet.setCode(record.code);
        dataSet.setVersion(record.version);
        dataSet.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataSet.setId(record.id);
        dataSet.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.DATA_SET,
                record.code));
        dataSet.setId(record.id);
        dataSet.setDataProducerCode(record.data_producer_code);
        dataSet.setDataStore(dataStores.get(record.dast_id));
        dataSet.setDerived(record.is_derived);
        dataSet.setProductionDate(record.production_timestamp);
        dataSet.setRegistrationDate(record.registration_timestamp);
        dataSet.setRegistrator(getOrCreateActor(record.pers_id_registerer));
        dataSet.setModificationDate(record.modification_timestamp);
        dataSet.setModifier(getOrCreateActor(record.pers_id_modifier));
        if (dataDAO.isAccessTimestampEnabled())
        {
            dataSet.setAccessTimestamp(record.access_timestamp);
        } else
        {
            dataSet.setAccessTimestamp(record.modification_timestamp);
        }

        dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());

        if (record.samp_id != null)
        {
            Sample sample = new Sample();
            sample.setId(record.samp_id);
            dataSet.setSample(sample);
        }
        if (record.expe_id != null)
        {
            Experiment experiment = new Experiment();
            experiment.setId(record.expe_id);
            dataSet.setExperiment(experiment);
        }
    }

    private Boolean resolve(String booleanRepresentative)
    {
        if (booleanRepresentative == null)
        {
            return null;
        }
        return BooleanOrUnknown.tryToResolve(BooleanOrUnknown.valueOf(booleanRepresentative));
    }

    private void loadSmallConnectedTables()
    {
        dataSetTypes.clear();
        for (DataSetTypeRecord code : query.getDatasetTypes())
        {
            dataSetTypes.put(code.id, createDataSetType(code));
        }

        fileFormatTypes.clear();
        for (CodeRecord code : query.getFileFormatTypes())
        {
            fileFormatTypes.put(code.id, createFileFormatType(code));
        }

        locatorTypes.clear();
        for (CodeRecord code : query.getLocatorTypes())
        {
            locatorTypes.put(code.id, createLocatorType(code));
        }

        dataStores.clear();
        for (DataStoreRecord code : query.getDataStores())
        {
            dataStores.put(code.id, createDataStore(code));
        }

        externalDataManagementSystems.clear();
        for (ExternalDataManagementSystemRecord edms : query
                .getExternalDataManagementSystems())
        {
            externalDataManagementSystems.put(edms.id, createExternalDataManagementSystem(edms));
        }
    }

    private static void setCode(Code<?> codeHolder, CodeRecord codeRecord)
    {
        codeHolder.setCode(codeRecord.code);
    }

    private static DataStore createDataStore(DataStoreRecord codeRecord)
    {
        DataStore result = new DataStore();
        setCode(result, codeRecord);
        result.setHostUrl(codeRecord.download_url);
        String downloadUrl = DataStoreTranslator.translateDownloadUrl(codeRecord.download_url);
        result.setDownloadUrl(downloadUrl);
        return result;
    }

    private static ExternalDataManagementSystem createExternalDataManagementSystem(
            ExternalDataManagementSystemRecord edmsRecord)
    {
        ExternalDataManagementSystem result = new ExternalDataManagementSystem();

        result.setId(edmsRecord.id);
        result.setCode(edmsRecord.code);
        result.setLabel(edmsRecord.label);
        result.setUrlTemplate(edmsRecord.address);
        result.setAddress(edmsRecord.address);
        result.setAddressType(ExternalDataManagementSystemType.valueOf(edmsRecord.addressType));
        result.setOpenBIS(ExternalDataManagementSystemType.OPENBIS.equals(result.getAddressType()));

        return result;
    }

    private static LocatorType createLocatorType(CodeRecord codeRecord)
    {
        LocatorType result = new LocatorType();
        setCode(result, codeRecord);
        return result;
    }

    private static FileFormatType createFileFormatType(CodeRecord codeRecord)
    {
        FileFormatType result = new FileFormatType();
        setCode(result, codeRecord);
        return result;
    }

    private DataSetType createDataSetType(DataSetTypeRecord record)
    {
        DataSetType result = new DataSetType();
        setCode(result, record);
        result.setDeletionDisallow(record.deletion_disallow);
        return result;
    }

    @Override
    public Map<Long, GenericEntityPropertyRecord> fetchProperties(List<Long> ids,
            String propertyTypeCode)
    {
        DataIterator<GenericEntityPropertyRecord> queryResult =
                query.getEntityPropertyGenericValues(new LongOpenHashSet(ids), propertyTypeCode);

        Map<Long, GenericEntityPropertyRecord> result =
                new HashMap<Long, GenericEntityPropertyRecord>();
        for (GenericEntityPropertyRecord record : queryResult)
        {
            result.put(record.entity_id, record);
        }

        return result;
    }

    private static final class Key
    {
        private Long componentId;

        private Long containerId;

        Key(Long containerId, Long componentId)
        {
            this.containerId = containerId;
            this.componentId = componentId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Key == false)
            {
                return false;
            }
            Key key = (Key) obj;
            return isEqual(containerId, key.containerId) && isEqual(componentId, key.componentId);
        }

        private boolean isEqual(Long n1OrNull, Long n2OrNull)
        {
            return n1OrNull == null ? n1OrNull == n2OrNull : n1OrNull.equals(n2OrNull);
        }

        @Override
        public int hashCode()
        {
            return (containerId == null ? 0 : containerId.intValue()) * 37
                    + (componentId == null ? 0 : componentId.intValue());
        }

    }

    @Override
    public IDatasetLocationNode listLocationsByDatasetCode(String datasetCode)
    {
        List<DatasetLocationNodeRecord> records = loadRawLocationData(datasetCode);

        Map<Key, DatasetLocationNode> nodeMap = new HashMap<Key, DatasetLocationNode>();
        Map<Long, List<DatasetLocationNode>> containerMap = new HashMap<Long, List<DatasetLocationNode>>();
        DatasetLocationNode rootNode = null;
        for (DatasetLocationNodeRecord record : records)
        {
            DatasetLocation location;
            if (record.link_info == null)
            {
                location = new DatasetLocation();
                location.setDataSetLocation(record.location);
            } else
            {
                location = new LinkDataSetLocation();
            }
            location.setDatasetCode(record.code);
            location.setDataStoreCode(record.data_store_code);
            location.setDataStoreUrl(record.data_store_url);
            location.setOrderInContainer(record.ordinal);

            DatasetLocationNode node = new DatasetLocationNode(location);
            if (datasetCode.equals(record.code))
            {
                rootNode = node;
            }
            nodeMap.put(new Key(record.container_id, record.id), node);
            if (record.location == null)
            {
                List<DatasetLocationNode> list = containerMap.get(record.id);
                if (list == null)
                {
                    list = new ArrayList<DatasetLocationNode>();
                    containerMap.put(record.id, list);
                }
                list.add(node);
            }
        }

        linkContainedData(records, nodeMap, containerMap);

        return rootNode;
    }

    private void linkContainedData(List<DatasetLocationNodeRecord> records,
            Map<Key, DatasetLocationNode> nodeMap, Map<Long, List<DatasetLocationNode>> containerMap)
    {
        for (DatasetLocationNodeRecord record : records)
        {
            Long containerId = record.container_id;
            Long componentId = record.id;
            DatasetLocationNode componentNode = nodeMap.get(new Key(containerId, componentId));
            if (containerId != null)
            {
                List<DatasetLocationNode> containers = containerMap.get(containerId);
                for (DatasetLocationNode containerNode : containers)
                {
                    containerNode.addContained(componentNode);
                }
            }
        }
    }

    private List<DatasetLocationNodeRecord> loadRawLocationData(String datasetCode)
    {
        Long relationshipTypeId = getContainerComponentRelationshipTypeId();
        DataIterator<DatasetLocationNodeRecord> queryResult =
                query.listLocationsByDatasetCode(datasetCode, relationshipTypeId);
        List<DatasetLocationNodeRecord> result = new ArrayList<DatasetLocationNodeRecord>();
        for (DatasetLocationNodeRecord datasetLocationNodeRecord : queryResult)
        {
            result.add(datasetLocationNodeRecord);
        }
        return result;
    }

    private Long getParentChildRelationshipTypeId()
    {
        return RelationshipUtils.getParentChildRelationshipType(relationshipTypeDAO).getId();
    }

    private Long getContainerComponentRelationshipTypeId()
    {
        return RelationshipUtils.getContainerComponentRelationshipType(relationshipTypeDAO).getId();
    }

    @Override
    public List<String> listContainedCodes(String datasetCode)
    {
        List<String> result = new LinkedList<String>();
        Long relationshipTypeId = getContainerComponentRelationshipTypeId();
        DataIterator<String> queryResult = query.getContainedDataSetCodes(datasetCode, relationshipTypeId);
        for (String containedCode : queryResult)
        {
            result.add(containedCode);
        }
        queryResult.close();
        return result;
    }

    @Override
    public List<AbstractExternalData> listByMetaprojectIdAndArchivalState(Long metaprojectId, boolean isArchived)
    {
        DataIterator<DatasetRecord> dataSets =
                query.getDatasetsForMetaprojectAndArchivalState(metaprojectId, isArchived);
        return enrichDatasets(dataSets);
    }

}
