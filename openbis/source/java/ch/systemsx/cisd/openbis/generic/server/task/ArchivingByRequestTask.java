/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PhysicalDataSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * @author Franz-Josef Elmer
 */
public class ArchivingByRequestTask extends AbstractGroupMaintenanceTask
{
    static final String KEEP_IN_STORE = "keep-in-store";

    static final String MINIMUM_CONTAINER_SIZE_IN_BYTES = "minimum-container-size-in-bytes";

    static final long DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES = 10 * FileUtils.ONE_GB;

    static final String MAXIMUM_CONTAINER_SIZE_IN_BYTES = "maximum-container-size-in-bytes";

    static final long DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES = 80 * FileUtils.ONE_GB;

    private long minimumContainerSize;

    private long maximumContainerSize;

    private boolean keepInStore;

    public ArchivingByRequestTask()
    {
        super(false);
    }

    @Override
    protected void setUpSpecific(Properties properties)
    {
        keepInStore = PropertyUtils.getBoolean(properties, KEEP_IN_STORE, false);
        minimumContainerSize = PropertyUtils.getLong(properties, MINIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES);
        maximumContainerSize = PropertyUtils.getLong(properties, MAXIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES);
        if (maximumContainerSize <= minimumContainerSize)
        {
            throw new ConfigurationFailureException(MINIMUM_CONTAINER_SIZE_IN_BYTES + "=" + minimumContainerSize
                    + " has to be less than " + MAXIMUM_CONTAINER_SIZE_IN_BYTES + "=" + maximumContainerSize);
        }
        if (minimumContainerSize < 0)
        {
            throw new ConfigurationFailureException(MINIMUM_CONTAINER_SIZE_IN_BYTES + "=" + minimumContainerSize
                    + " has to be greater or equal zero.");
        }
    }

    @Override
    public void execute()
    {
        IApplicationServerInternalApi service = getService();
        String sessionToken = null;
        try {
            sessionToken = service.loginAsSystem();
            execute(service, sessionToken);
        } catch (Exception exception) {
            if (sessionToken != null) {
                service.logout(sessionToken);
            }
            throw exception;
        }
    }

    public void execute(IApplicationServerInternalApi service, String sessionToken)
    {
        if (MaintenanceTaskUtils.areAllDataStoreServersRunning(service, sessionToken) == false)
        {
            operationLog.info("Not executed because DSS isn't running (yet).");
            return;
        }
        List<DataSet> dataSets = getDataSetsToBeArchived(service, sessionToken);
        operationLog.info(dataSets.size() + " data sets to be archived.");
        Map<String, List<DataSet>> dataSetsByGroups = getDataSetsByGroups(getGroups(), dataSets);
        for (Entry<String, List<DataSet>> entry : dataSetsByGroups.entrySet())
        {
            String groupKey = entry.getKey();
            List<DataSetHolder> items = entry.getValue().stream().map(DataSetHolder::new).collect(Collectors.toList());
            operationLog.info("Archive " + items.size() + " data sets"
                    + (StringUtils.isNotBlank(groupKey) ? (" for group " + groupKey) : "") + ".");
            List<List<DataSetHolder>> chunks = getChunks(items, minimumContainerSize, maximumContainerSize);
            for (List<DataSetHolder> chunk : chunks)
            {
                if (chunk.size() == 1 && chunk.get(0).getDataSet().getPhysicalData().getSize() > maximumContainerSize)
                {
                    operationLog.warn("Data set " + chunk.get(0).getDataSet().getPermId() + " is larger than the "
                            + MAXIMUM_CONTAINER_SIZE_IN_BYTES + ": "
                            + chunk.get(0).getDataSet().getPhysicalData().getSize() + " > " + maximumContainerSize);
                }
                List<DataSetPermId> ids = chunk.stream().map(i -> i.getDataSet().getPermId()).collect(Collectors.toList());
                if (ids.isEmpty() == false)
                {
                    DataSetArchiveOptions archiveOptions = new DataSetArchiveOptions();
                    archiveOptions.setRemoveFromDataStore(keepInStore == false);
                    if (StringUtils.isNotBlank(groupKey))
                    {
                        archiveOptions.withOption(Constants.SUB_DIR_KEY, groupKey.toLowerCase());
                    }
                    service.archiveDataSets(sessionToken, ids, archiveOptions);
                }
            }
        }
    }

    static <T extends SizeHolder> List<List<T>> getChunks(List<T> items, long minChunkSize, long maxChunkSize)
    {
        Collections.sort(items, new SimpleComparator<T, Long>()
            {
                @Override
                public Long evaluate(T item)
                {
                    return -item.getSize();
                }
            });
        List<List<T>> chunks = new ArrayList<>();
        List<T> currentChunk = new ArrayList<>();
        long currentChunkSize = 0;
        for (T item : items)
        {
            long size = item.getSize();
            if (size >= maxChunkSize)
            {
                chunks.add(Arrays.asList(item));
                continue;
            }
            if (currentChunkSize >= minChunkSize)
            {
                if (currentChunkSize + size > maxChunkSize)
                {
                    chunks.add(currentChunk);
                    currentChunk = new ArrayList<>();
                    currentChunkSize = 0;
                }
                currentChunk.add(item);
                currentChunkSize += size;
            } else
            {
                if (currentChunkSize + size < maxChunkSize)
                {
                    currentChunk.add(item);
                    currentChunkSize += size;
                }
            }
        }
        if (currentChunkSize >= minChunkSize)
        {
            chunks.add(currentChunk);
        }
        return chunks;
    }

    private List<DataSet> getDataSetsToBeArchived(IApplicationServerInternalApi service, String sessionToken)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        PhysicalDataSearchCriteria physicalSearchCriteria = searchCriteria.withPhysicalData();
        physicalSearchCriteria.withPresentInArchive().thatEquals(false);
        physicalSearchCriteria.withArchivingRequested().thatEquals(true);
        physicalSearchCriteria.withStatus().thatEquals(ArchivingStatus.AVAILABLE);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withSample().withProject().withSpace();
        fetchOptions.withSample().withSpace();
        List<DataSet> dataSets = service.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        List<DataSet> result = new ArrayList<DataSet>();
        List<String> dataSetsWithUnknownSize = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            Long size = dataSet.getPhysicalData().getSize();
            if (size == null)
            {
                dataSetsWithUnknownSize.add(dataSet.getCode());
            } else
            {
                result.add(dataSet);
            }
        }
        if (dataSetsWithUnknownSize.isEmpty() == false)
        {
            operationLog.warn("The size of the following data sets is unknown: " + CollectionUtils.abbreviate(dataSetsWithUnknownSize, 100));
        }
        return result;
    }

    private Set<String> getGroups()
    {
        Set<String> groupKeys = new TreeSet<>();
        UserManagerConfig groupDefinitions = readGroupDefinitions(null);
        if (groupDefinitions != null)
        {
            List<UserGroup> groups = groupDefinitions.getGroups();
            for (UserGroup group : groups)
            {
                groupKeys.add(group.getKey());
            }
        }
        return groupKeys;
    }

    private Map<String, List<DataSet>> getDataSetsByGroups(Set<String> groupKeys, List<DataSet> dataSets)
    {
        Map<String, List<DataSet>> dataSetsByGroups = new TreeMap<>();
        for (DataSet dataSet : dataSets)
        {
            String spaceCode = dataSet.getExperiment().getProject().getSpace().getCode();
            String[] prefixAndCode = StringUtils.split(spaceCode, "_", 2);
            String groupKey = "";
            if (prefixAndCode.length == 2)
            {
                String prefix = prefixAndCode[0];
                if (groupKeys.contains(prefix))
                {
                    groupKey = prefix;
                }
            }
            List<DataSet> list = dataSetsByGroups.get(groupKey);
            if (list == null)
            {
                list = new ArrayList<>();
                dataSetsByGroups.put(groupKey, list);
            }
            list.add(dataSet);
        }
        return dataSetsByGroups;
    }

    protected IApplicationServerInternalApi getService()
    {
        return CommonServiceProvider.getApplicationServerApi();
    }

    static interface SizeHolder
    {
        long getSize();
    }

    private static class DataSetHolder implements SizeHolder
    {
        private final DataSet dataSet;

        public DataSetHolder(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        @Override
        public long getSize()
        {
            return dataSet.getPhysicalData().getSize();
        }

        public DataSet getDataSet()
        {
            return dataSet;
        }

    }

}
