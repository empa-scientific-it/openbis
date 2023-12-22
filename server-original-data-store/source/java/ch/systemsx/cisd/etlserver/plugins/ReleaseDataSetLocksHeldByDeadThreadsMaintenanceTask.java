package ch.systemsx.cisd.etlserver.plugins;

import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

public class ReleaseDataSetLocksHeldByDeadThreadsMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_NAME = "injected-release-dataset-locks-held-by-dead-threads-task";

    public static final int DEFAULT_INTERVAL = 60;

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ReleaseDataSetLocksHeldByDeadThreadsMaintenanceTask.class);

    @Override public void setUp(final String pluginName, final Properties properties)
    {
    }

    @Override public void execute()
    {
        IDataStoreServiceInternal dataStoreService = ServiceProvider.getDataStoreService();
        if (dataStoreService != null)
        {
            IDataSetDirectoryProvider dataSetDirectoryProvider = dataStoreService.getDataSetDirectoryProvider();
            if (dataSetDirectoryProvider != null)
            {
                IShareIdManager shareIdManager = dataSetDirectoryProvider.getShareIdManager();
                if (shareIdManager != null)
                {
                    try
                    {
                        shareIdManager.cleanupLocks();
                        operationLog.info("Clean up of dataset locks held by dead threads has finished successfully.");
                    } catch (Exception e)
                    {
                        operationLog.error("Clean up of dataset locks held by dead threads has failed.", e);
                    }
                }
            }
        }
    }
}
