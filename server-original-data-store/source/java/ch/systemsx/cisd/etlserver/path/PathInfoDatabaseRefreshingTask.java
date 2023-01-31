/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.path;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;

import net.lemnik.eodsql.QueryTool;

/**
 * Maintenance task to recreate path-info database entries.
 * 
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseRefreshingTask extends AbstractPathInfoDatabaseFeedingTask
{
    static final String DATA_SET_TYPE_KEY = "data-set-type";

    static final String TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY = "time-stamp-of-youngest-data-set";

    static final String CHUNK_SIZE_KEY = "chunk-size";

    private static final int DEFAULT_CHUNK_SIZE = 100;

    private static final Comparator<AbstractExternalData> REVERSE_REGISTRATION_DATE_COMPARATOR = new Comparator<AbstractExternalData>()
        {
            @Override
            public int compare(AbstractExternalData ds1, AbstractExternalData ds2)
            {
                long t1 = ds1.getRegistrationDate().getTime();
                long t2 = ds2.getRegistrationDate().getTime();
                if (t1 == t2)
                {
                    return -ds1.getCode().compareTo(ds2.getCode());
                }
                return t1 < t2 ? 1 : (t1 > t2 ? -1 : 0);
            }
        };

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDatabaseRefreshingTask.class);

    private IEncapsulatedOpenBISService service;

    private String timeStampAndCodeOfYoungestDataSet;

    private int chunkSize;

    private String dataSetType;

    public PathInfoDatabaseRefreshingTask()
    {
    }

    PathInfoDatabaseRefreshingTask(IEncapsulatedOpenBISService service, IPathsInfoDAO dao,
            IHierarchicalContentFactory hierarchicalContentFactory, IDataSetDirectoryProvider directoryProvider)
    {
        this.service = service;
        this.dao = dao;
        this.directoryProvider = directoryProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
            dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        defineStateFile(properties, directoryProvider.getStoreRoot());
        timeStampAndCodeOfYoungestDataSet = tryGetTimeStampAndCodeOfYoungestDataSet(properties);
        if (stateFile.exists() == false && timeStampAndCodeOfYoungestDataSet == null)
        {
            throw new ConfigurationFailureException("Either property '" + TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY
                    + "' is defined or '" + stateFile.getAbsolutePath() + "' exists.");
        }
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_KEY, DEFAULT_CHUNK_SIZE);
        computeChecksum = PropertyUtils.getBoolean(properties, COMPUTE_CHECKSUM_KEY, true);
        checksumType = getAndCheckChecksumType(properties);
        dataSetType = properties.getProperty(DATA_SET_TYPE_KEY);
    }

    private String tryGetTimeStampAndCodeOfYoungestDataSet(Properties properties)
    {
        String ts = properties.getProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY);
        if (ts == null)
        {
            return null;
        }
        try
        {
            parseTimeStamp(ts);
            return ts;
        } catch (ParseException ex)
        {
            throw new ConfigurationFailureException("Invalid property '"
                    + TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY + "': " + ts, ex);
        }

    }

    @Override
    public void execute()
    {
        List<PhysicalDataSet> dataSets = getPhysicalDataSets();
        if (dataSets.isEmpty())
        {
            return;
        }
        operationLog.info("Refresh path info for " + dataSets.size() + " physical data sets.");
        long t0 = System.currentTimeMillis();
        for (PhysicalDataSet dataSet : dataSets)
        {
            String dataSetCode = dataSet.getCode();
            dao.deleteDataSet(dataSetCode);
            feedPathInfoDatabase(dataSet, dataSet.isH5Folders(), dataSet.isH5arFolders());
            updateTimeStampFile(renderTimeStampAndCode(dataSet));
        }
        operationLog.info("Path info for " + dataSets.size() + " physical data sets refreshed in "
                + (System.currentTimeMillis() - t0) / 1000 + " secs.");
    }

    private List<PhysicalDataSet> getPhysicalDataSets()
    {
        String timeStampAndCode = getLastTimeStampAndCode();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        searchCriteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE,
                CompareMode.LESS_THAN_OR_EQUAL, extractTimeStamp(timeStampAndCode), "0"));
        if (dataSetType != null)
        {
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, dataSetType));
        }
        List<AbstractExternalData> dataSets = service.searchForDataSets(searchCriteria);
        Collections.sort(dataSets, REVERSE_REGISTRATION_DATE_COMPARATOR);
        List<PhysicalDataSet> result = new ArrayList<>();
        for (AbstractExternalData dataSet : dataSets)
        {
            PhysicalDataSet physicalDataSet = dataSet.tryGetAsDataSet();
            if (checkDataSet(physicalDataSet, timeStampAndCode))
            {
                result.add(physicalDataSet);
                if (result.size() >= chunkSize)
                {
                    return result;
                }
            }
        }
        return result;
    }

    private boolean checkDataSet(PhysicalDataSet physicalDataSet, String timeStampAndCode)
    {
        if (physicalDataSet == null)
        {
            return false;
        }
        if (physicalDataSet.getStatus().equals(DataSetArchivingStatus.AVAILABLE) == false)
        {
            return false;
        }
        return renderTimeStampAndCode(physicalDataSet).compareTo(timeStampAndCode) < 0;
    }

    @Override
    protected Logger getOperationLog()
    {
        return operationLog;
    }

    private String getLastTimeStampAndCode()
    {
        if (stateFile.exists() == false)
        {
            return timeStampAndCodeOfYoungestDataSet;
        }
        return FileUtilities.loadToString(stateFile).trim();
    }

    private String renderTimeStampAndCode(PhysicalDataSet dataSet)
    {
        return renderTimeStampAndCode(dataSet.getRegistrationDate(), dataSet.getDataSetCode());
    }
}
