/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * Maintenance task for unarchiving multi data set archives.
 *
 * @author Franz-Josef Elmer
 */
public class MultiDataSetUnarchivingMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiDataSetUnarchivingMaintenanceTask.class);

    @Override
    public void setUp(String pluginName, Properties properties)
    {
    }

    @Override
    public void execute()
    {
        IMultiDataSetArchiverReadonlyQueryDAO dao = getReadonlyQuery();
        List<MultiDataSetArchiverContainerDTO> containersForUnarchiving = dao.listContainersForUnarchiving();
        IDataStoreServiceInternal dataStoreService = getDataStoreService();
        IArchiverPlugin archiverPlugin = dataStoreService.getArchiverPlugin();
        IDataSetDirectoryProvider directoryProvider = dataStoreService.getDataSetDirectoryProvider();
        IHierarchicalContentProvider hierarchicalContentProvider = getHierarchicalContentProvider();
        ArchiverTaskContext context = new ArchiverTaskContext(directoryProvider, hierarchicalContentProvider);
        context.setForceUnarchiving(true);
        for (MultiDataSetArchiverContainerDTO container : containersForUnarchiving)
        {
            List<MultiDataSetArchiverDataSetDTO> dataSets = dao.listDataSetsForContainerId(container.getId());
            List<String> dataSetCodes = extractCodes(dataSets);
            operationLog.info("Start unarchiving " + CollectionUtils.abbreviate(dataSetCodes, 20));
            List<DatasetDescription> loadedDataSets = loadDataSets(dataSetCodes);
            archiverPlugin.unarchive(loadedDataSets, context);
            resetRequestUnarchiving(container);
            operationLog.info("Unarchiving finished for " + CollectionUtils.abbreviate(dataSetCodes, 20));
        }
    }

    private void resetRequestUnarchiving(MultiDataSetArchiverContainerDTO container)
    {
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            transaction.resetRequestUnarchiving(container.getId());
            transaction.commit();
            transaction.close();
        } catch (Exception e)
        {
            operationLog.warn("Reset request unarchiving of container " + container + " failed", e);
            try
            {
                transaction.rollback();
                transaction.close();
            } catch (Exception ex)
            {
                operationLog.warn("Rollback of multi dataset db transaction failed", ex);
            }
        }
    }

    private List<DatasetDescription> loadDataSets(List<String> dataSetCodes)
    {
        IEncapsulatedOpenBISService service = getASService();
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        for (AbstractExternalData dataSet : service.listDataSetsByCode(dataSetCodes))
        {
            result.add(DataSetTranslator.translateToDescription(dataSet));
        }
        return result;
    }

    private List<String> extractCodes(List<MultiDataSetArchiverDataSetDTO> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        return codes;
    }

    IEncapsulatedOpenBISService getASService()
    {
        return ServiceProvider.getOpenBISService();
    }

    IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }

    IDataStoreServiceInternal getDataStoreService()
    {
        return ServiceProvider.getDataStoreService();
    }

    IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        return MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
    }

    IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }
}
