/*
 * Copyright 2022 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class CleanUpUnarchivingScratchShareTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CleanUpUnarchivingScratchShareTask.class);

    private IEncapsulatedOpenBISService service;

    private String archiveFolder;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        archiveFolder = PropertyParametersUtil.extractSingleSectionProperties(properties,
                PluginTaskInfoProvider.ARCHIVER_SECTION_NAME, false).getProperties()
                .getProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY);
        if (archiveFolder == null)
        {
            throw new ConfigurationFailureException("Missing property: "
                    + PluginTaskInfoProvider.ARCHIVER_SECTION_NAME
                    + "." + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY);
        }
    }

    @Override
    public void execute()
    {
        File storeRoot = getStoreRoot();
        IEncapsulatedOpenBISService service = getService();
        IFreeSpaceProvider spaceProvider = createFreeSpaceProvider();
        ISimpleLogger logger = new Log4jSimpleLogger(operationLog);
        Share scratchShare = MultiDataSetArchivingUtils.getScratchShare(storeRoot, service, spaceProvider, logger);
        List<SimpleDataSetInformationDTO> dataSets = scratchShare.getDataSetsOrderedBySize();
        operationLog.info("Starting clean up. Scanning " + dataSets.size() + " data sets.");
        List<SimpleDataSetInformationDTO> dataSetsToBeRemoved = findDataSetsToBeRemoved(storeRoot, dataSets);
        if (dataSetsToBeRemoved.isEmpty() == false)
        {
            operationLog.info(dataSetsToBeRemoved.size() + " archived data sets in unarchiving scratch share found.");
            IDataSetDirectoryProvider directoryProvider = getDirectoryProvider();
            IShareIdManager shareIdManager = getShareIdManager();
            SegmentedStoreUtils.deleteFromUnarchivingScratchShare(dataSetsToBeRemoved, scratchShare, service,
                    directoryProvider, shareIdManager, logger);
            ;
            operationLog.info(dataSetsToBeRemoved.size()
                    + " archived data sets successfully removed from unarchiving scratch share.");
        }
        operationLog.info("Clean up task finished");
    }

    private List<SimpleDataSetInformationDTO> findDataSetsToBeRemoved(File storeRoot,
            List<SimpleDataSetInformationDTO> dataSets)
    {
        IMultiDataSetArchiverReadonlyQueryDAO multiDataSetQuery = getReadonlyQuery();
        List<SimpleDataSetInformationDTO> dataSetsToBeRemoved = new ArrayList<>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            File dataSetFile = new File(storeRoot, dataSet.getDataSetShareId() + "/" + dataSet.getDataSetLocation());
            if (dataSet.getStatus().equals(DataSetArchivingStatus.ARCHIVED)
                    && dataSet.isPresentInArchive() && dataSetFile.exists())
            {
                String dataSetCode = dataSet.getDataSetCode();
                MultiDataSetArchiverDataSetDTO dataSetInArchive = multiDataSetQuery.getDataSetForCode(dataSetCode);
                if (dataSetInArchive == null)
                {
                    operationLog.warn("Data set " + dataSetCode + " unknown by Multi Data Set database.");
                    break;
                }
                long containerId = dataSetInArchive.getContainerId();
                String containerPath = multiDataSetQuery.getContainerForId(containerId).getPath();
                long size = multiDataSetQuery.listDataSetsForContainerId(containerId).stream()
                        .mapToLong(MultiDataSetArchiverDataSetDTO::getSizeInBytes).sum();
                File containerFile = new File(archiveFolder, containerPath);
                if (containerFile.exists() == false)
                {
                    operationLog.warn("Container file for data set " + dataSetCode + " does not exists: "
                            + containerFile.getAbsolutePath());
                    break;
                }
                if (Math.abs(containerFile.length() - size) > size / 10)
                {
                    operationLog.warn("Size of container file " + containerFile.getAbsolutePath()
                            + " (which contains data set " + dataSetCode + ") doesn't match the size calculated "
                            + "from the Multi Data Set database: " + containerFile.length() + " vs " + size);
                    break;
                }
                dataSetsToBeRemoved.add(dataSet);
            }
        }
        return dataSetsToBeRemoved;
    }

    protected IShareIdManager getShareIdManager()
    {
        return ServiceProvider.getShareIdManager();
    }

    protected IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }

    protected File getStoreRoot()
    {
        return ServiceProvider.getConfigProvider().getStoreRoot();
    }

    protected IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

    protected IFreeSpaceProvider createFreeSpaceProvider()
    {
        return new SimpleFreeSpaceProvider();
    }

    protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        return MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
    }
}
