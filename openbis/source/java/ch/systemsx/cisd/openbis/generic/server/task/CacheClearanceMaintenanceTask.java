package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICacheManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

public class CacheClearanceMaintenanceTask extends AbstractMaintenanceTask
{

    private int cacheTimeout = OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT;

    private ICacheManager cacheManager;

    public CacheClearanceMaintenanceTask()
    {
        super(false);
    }

    @Override
    protected void setUpSpecific(final Properties properties)
    {
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
