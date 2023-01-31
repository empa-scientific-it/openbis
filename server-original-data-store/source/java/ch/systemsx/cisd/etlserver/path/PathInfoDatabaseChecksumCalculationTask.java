/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

import net.lemnik.eodsql.QueryTool;

/**
 * Maintenance task to calculate checksums for file entries in the pathinfo database with unknown checksum.
 * 
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseChecksumCalculationTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDatabaseChecksumCalculationTask.class);

    private IPathsInfoDAO dao;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private ITimeProvider timeProvider;

    private String checksumType;

    public PathInfoDatabaseChecksumCalculationTask()
    {
    }

    PathInfoDatabaseChecksumCalculationTask(IPathsInfoDAO dao,
            IHierarchicalContentProvider hierarchicalContentProvider, ITimeProvider timeProvider, String checksumType)
    {
        this.dao = dao;
        this.hierarchicalContentProvider = hierarchicalContentProvider;
        this.timeProvider = timeProvider;
        this.checksumType = checksumType;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
        hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        checksumType = AbstractPathInfoDatabaseFeedingTask.getAndCheckChecksumType(properties);
    }

    @Override
    public void execute()
    {
        Map<String, List<PathEntryDTO>> entriesOrderedByDataSets = getPathEntries();

        Set<Entry<String, List<PathEntryDTO>>> entrySet = entriesOrderedByDataSets.entrySet();
        int dataSetCounter = 0;
        int fileCounter = 0;
        for (Entry<String, List<PathEntryDTO>> entry : entrySet)
        {
            long t0 = timeProvider.getTimeInMilliseconds();
            String dataSetCode = entry.getKey();
            try
            {
                IHierarchicalContent content = hierarchicalContentProvider.asContentWithoutModifyingAccessTimestamp(dataSetCode);
                try
                {
                    List<PathEntryDTO> pathEntries = entry.getValue();
                    for (PathEntryDTO pathEntry : pathEntries)
                    {
                        IHierarchicalContentNode node = content.getNode(pathEntry.getRelativePath());
                        InputStream inputStream = node.getInputStream();
                        PathInfo pathInfo = new PathInfo();
                        PathInfo.setChecksum(pathInfo, inputStream, true, checksumType);
                        dao.updateChecksum(pathEntry.getId(), pathInfo.getChecksumCRC32(), pathInfo.getChecksum());
                        fileCounter++;
                    }
                    dao.commit();
                    operationLog.info((timeProvider.getTimeInMilliseconds() - t0 + 500) / 1000
                            + " seconds needed to update checksums of " + pathEntries.size()
                            + " files of data set " + dataSetCode + ".");
                    dataSetCounter++;
                } catch (Exception ex)
                {
                    operationLog.error("Couldn't update checksum for some file in data set "
                            + dataSetCode, ex);
                    dao.rollback();
                }
            } catch (IllegalArgumentException ex)
            {
                // Data sets unknown by the hierarchical content provider are ignored
                operationLog.debug(ex.getMessage());
            }
        }
        operationLog.info("Checksums of " + fileCounter + " files in " + dataSetCounter
                + " data sets have been calculated.");
    }

    private Map<String, List<PathEntryDTO>> getPathEntries()
    {
        List<PathEntryDTO> entries = dao.listDataSetFilesWithUnkownChecksum();
        Map<String, List<PathEntryDTO>> entriesOrderedByDataSets =
                new TreeMap<String, List<PathEntryDTO>>();
        for (PathEntryDTO pathEntryDTO : entries)
        {
            String dataSetCode = pathEntryDTO.getDataSetCode();
            List<PathEntryDTO> list = entriesOrderedByDataSets.get(dataSetCode);
            if (list == null)
            {
                list = new ArrayList<PathEntryDTO>();
                entriesOrderedByDataSets.put(dataSetCode, list);
            }
            list.add(pathEntryDTO);
        }
        dao.commit(); // Needed because DAO is a TransactionQuery. Otherwise there will be an idle connection
        operationLog.info("Start calculating checksums of " + entries.size() + " files in "
                + entriesOrderedByDataSets.size() + " data sets.");
        return entriesOrderedByDataSets;
    }

}
