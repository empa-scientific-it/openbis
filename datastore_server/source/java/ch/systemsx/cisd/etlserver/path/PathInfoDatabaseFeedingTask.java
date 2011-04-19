/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.File;
import java.util.List;
import java.util.Properties;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.HierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.etlserver.postregistration.ICleanupTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import ch.systemsx.cisd.etlserver.postregistration.NoCleanupTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Maintenance and post registration task which feeds pathinfo database with all data set paths.
 *
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseFeedingTask implements IMaintenanceTask, IPostRegistrationTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDatabaseFeedingTask.class);
    
    private static IPathsInfoDAO createDAO()
    {
        return QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }
    
    private static IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }
    
    private static HierarchicalContentFactory createContentFactory()
    {
        return new HierarchicalContentFactory();
    }
    
    private IEncapsulatedOpenBISService service;
    
    private IDataSetDirectoryProvider directoryProvider;

    private IPathsInfoDAO dao;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    public PathInfoDatabaseFeedingTask()
    {
    }

    public PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(service, getDirectoryProvider(), createDAO(), createContentFactory());
    }

    @Private
    PathInfoDatabaseFeedingTask(IEncapsulatedOpenBISService service,
            IDataSetDirectoryProvider directoryProvider, IPathsInfoDAO dao,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        this.service = service;
        this.directoryProvider = directoryProvider;
        this.dao = dao;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
    }

    public boolean requiresDataStoreLock()
    {
        return false;
    }
    

    public void setUp(String pluginName, Properties properties)
    {
        service = ServiceProvider.getOpenBISService();
        directoryProvider = getDirectoryProvider();
        dao = createDAO();
        hierarchicalContentFactory = createContentFactory();
    }

    public void execute()
    {
        List<SimpleDataSetInformationDTO> dataSets = service.listDataSets();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            feedPathInfoDatabase(dataSet);
        }
    }

    public IPostRegistrationTaskExecutor createExecutor(final String dataSetCode)
    {
        return new IPostRegistrationTaskExecutor()
            {
                public ICleanupTask createCleanupTask()
                {
                    return new NoCleanupTask();
                }

                public void execute()
                {
                    ExternalData dataSet = service.tryGetDataSet(dataSetCode);
                    if (dataSet == null)
                    {
                        operationLog.error("Data set " + dataSetCode + " unknown by openBIS.");
                        return;
                    }
                    feedPathInfoDatabase(dataSet);
                }
            };
    }
        
    private void feedPathInfoDatabase(IDatasetLocation dataSet)
    {
        IShareIdManager shareIdManager = directoryProvider.getShareIdManager();
        String dataSetCode = dataSet.getDataSetCode();
        shareIdManager.lock(dataSetCode);
        File dataSetRoot = directoryProvider.getDataSetDirectory(dataSet);
        if (dataSetRoot.exists() == false)
        {
            operationLog.error("Root directory of data set " + dataSetCode
                    + " does not exists: " + dataSetRoot);
            shareIdManager.releaseLocks();
            return;
        }

        try
        {
            DatabaseBasedDataSetPathsInfoFeeder feeder =
                    new DatabaseBasedDataSetPathsInfoFeeder(dao, hierarchicalContentFactory);
            Long id = dao.tryGetDataSetId(dataSetCode);
            if (id != null)
            {
                feeder.addPaths(dataSetCode, dataSet.getDataSetLocation(), dataSetRoot);
            }
            dao.commit();
            operationLog.info("Paths inside data set " + dataSetCode
                    + " successfully added to database.");
        } catch (Exception ex)
        {
            handleException(ex, dataSetCode);
        } finally
        {
            shareIdManager.releaseLocks();
        }
    }

    private void handleException(Exception ex, String dataSet)
    {
        operationLog.error("Couldn't feed database with path infos of data set " + dataSet,
                ex);
        dao.rollback();
    }
    
}
