/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ArchivingAggregationService extends AggregationService
{
    static final String METHOD_KEY = "method";

    static final String ARGS_KEY = "args";

    static final String GET_ARCHIVING_INFO_METHOD = "getArchivingInfo";

    private static final long serialVersionUID = 1L;

    private IApplicationServerApi v3api;

    private IArchiverPlugin archiver;

    public ArchivingAggregationService(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null, null);
    }

    ArchivingAggregationService(Properties properties, File storeRoot, IApplicationServerApi v3api, IArchiverPlugin archiver)
    {
        super(properties, storeRoot);
        this.v3api = v3api;
        this.archiver = archiver;
    }

    @Override
    public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        try
        {
            Object method = parameters.get(METHOD_KEY);
            List<String> arguments = getArguments(parameters);
            if (GET_ARCHIVING_INFO_METHOD.equals(method))
            {
                return getArchivingInfo(context.trySessionToken(), arguments);
            }
            throw new UserFailureException("Unknown method '" + method + "'.");
        } catch (Throwable e)
        {
            logInvocationError(parameters, e);
            return errorTableModel(parameters, e);
        }
    }

    private TableModel getArchivingInfo(String sessionToken, List<String> dataSetCodes)
    {
        Map<String, Set<String>> bundlesByDataSetCode = getBundles(dataSetCodes);
        Set<String> allDataSets = mergeAllBundles(bundlesByDataSetCode);
        Map<String, Long> dataSetSizes = getDataSetSizes(sessionToken, allDataSets);
        Map<String, Long> bundleSizes = getBundleSizes(bundlesByDataSetCode, dataSetSizes);
        long totalSize = getTotalSize(allDataSets, dataSetSizes);

        Map<String, Object> infos = new TreeMap<>();
        infos.put("total size", totalSize);
        for (Entry<String, Set<String>> entry : bundlesByDataSetCode.entrySet())
        {
            Map<String, Object> info = new TreeMap<>();
            String dataSetCode = entry.getKey();
            info.put("size", getDataSetSizeOr0IfUnknown(dataSetSizes, dataSetCode));
            Set<String> bundleDataSets = entry.getValue();
            info.put("bundleId", getBundleId(bundleDataSets));
            Set<String> notDeletedDataSets = new HashSet<>(bundleDataSets);
            notDeletedDataSets.retainAll(dataSetSizes.keySet());
            info.put("numberOfDataSets", notDeletedDataSets.size());
            info.put("bundleSize", getDataSetSizeOr0IfUnknown(bundleSizes, dataSetCode));
            infos.put(dataSetCode, info);
        }

        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("STATUS");
        builder.addHeader("MESSAGE");
        builder.addHeader("RESULT");
        IRowBuilder row = builder.addRow();
        row.setCell("STATUS", "OK");
        row.setCell("MESSAGE", "Operation Successful");
        GenericObjectMapper objectMapper = new GenericObjectMapper();
        try
        {
            String jsonValue = objectMapper.writeValueAsString(infos);
            row.setCell("RESULT", jsonValue);
        } catch (JsonProcessingException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
        return builder.getTableModel();
    }

    private Map<String, Set<String>> getBundles(List<String> dataSetCodes)
    {
        Map<String, Set<String>> results = new TreeMap<String, Set<String>>();
        for (String dataSetCode : new HashSet<>(dataSetCodes))
        {
            results.put(dataSetCode, new TreeSet<>(getArchiver().getDataSetCodesForUnarchiving(Arrays.asList(dataSetCode))));
        }
        return results;
    }

    private Set<String> mergeAllBundles(Map<String, Set<String>> bundles)
    {
        Set<String> result = new TreeSet<>();
        for (Set<String> set : bundles.values())
        {
            result.addAll(set);
        }
        return result;
    }

    private Map<String, Long> getDataSetSizes(String sessionToken, Collection<String> dataSetCodes)
    {
        List<DataSetPermId> ids = dataSetCodes.stream().map(DataSetPermId::new).collect(Collectors.toList());
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        Map<String, Long> result = new TreeMap<>();
        for (DataSet dataSet : getv3api().getDataSets(sessionToken, ids, fetchOptions).values())
        {
            PhysicalData physicalData = dataSet.getPhysicalData();
            if (physicalData == null)
            {
                throw new UserFailureException("Data set " + dataSet.getCode() + " isn't a physical data set.");
            }
            Long size = physicalData.getSize();
            if (size == null)
            {
                throw new UserFailureException("Physical data set " + dataSet.getCode() + " has unknown size.");
            }
            result.put(dataSet.getCode(), size);
        }
        return result;
    }

    private Map<String, Long> getBundleSizes(Map<String, Set<String>> bundlesByDataSetCode, Map<String, Long> dataSetSizes)
    {
        Map<String, Long> result = new TreeMap<>();
        for (Entry<String, Set<String>> entry : bundlesByDataSetCode.entrySet())
        {
            long sum = 0;
            for (String dataSetCode : entry.getValue())
            {
                sum += getDataSetSizeOr0IfUnknown(dataSetSizes, dataSetCode);
            }
            result.put(entry.getKey(), sum);
        }
        return result;
    }

    private long getTotalSize(Set<String> allDataSets, Map<String, Long> dataSetSizes)
    {
        long sum = 0;
        for (String dataSetCode : allDataSets)
        {
            sum += getDataSetSizeOr0IfUnknown(dataSetSizes, dataSetCode);
        }
        return sum;
    }

    private long getDataSetSizeOr0IfUnknown(Map<String, Long> dataSetSizes, String dataSetCode)
    {
        Long size = dataSetSizes.get(dataSetCode);
        return size != null ? size : 0;
    }

    private String getBundleId(Set<String> bundleDataSets)
    {
        List<String> sortedDataSets = new ArrayList<>(bundleDataSets);
        Collections.sort(sortedDataSets);
        // Because each archived data set can be only in one bundle
        // a bundle is uniquely identified by the lexicographically smallest data set.
        return sortedDataSets.get(0);
    }

    private List<String> getArguments(Map<String, Object> parameters)
    {
        List<String> result = new ArrayList<String>();
        Object args = parameters.get(ARGS_KEY);
        if (args instanceof String)
        {
            String[] splitted = ((String) args).split(",");
            for (String string : splitted)
            {
                String arg = string.trim();
                if (StringUtils.isNotBlank(arg))
                {
                    result.add(arg);
                }
            }
        }
        return result;
    }

    private IArchiverPlugin getArchiver()
    {
        if (archiver == null)
        {
            IDataStoreServiceInternal dataStoreService = ServiceProvider.getDataStoreService();
            archiver = dataStoreService.getArchiverPlugin();
        }
        return archiver;
    }

    private IApplicationServerApi getv3api()
    {
        if (v3api == null)
        {
            v3api = ServiceProvider.getV3ApplicationService();
        }
        return v3api;
    }
}
