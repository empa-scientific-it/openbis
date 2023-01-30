package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICacheManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;

public class CacheClearanceMaintenanceTask implements IMaintenanceTask {

    private int cacheTimeout = OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT;

    private ICacheManager cacheManager;

    protected final Logger notificationLog;

    public CacheClearanceMaintenanceTask()
    {
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());
    }

    @Override
    public void setUp(String pluginName, Properties properties) {
        cacheManager = CommonServiceProvider.getApplicationContext().getBean(ICacheManager.class);
        cacheTimeout = PropertyUtils.getInt(properties, OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT,
                OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT);
    }

    @Override
    public void execute()
    {
        notificationLog.info("Cache clearance execution started.");
        cacheManager.clearOld(new Date(new Date().getTime() - cacheTimeout * 1000L));
        notificationLog.info("Cache clearance execution finished.");
    }

}
