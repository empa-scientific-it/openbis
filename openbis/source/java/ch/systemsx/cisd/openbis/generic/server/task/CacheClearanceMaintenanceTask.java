package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.properties.PropertyUtils;

public class CacheClearanceMaintenanceTask extends AbstractMaintenanceTask
{

    private int cacheTimeout = OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT;

    public CacheClearanceMaintenanceTask()
    {
        super(false);
    }

    @Override
    protected void setUpSpecific(final Properties properties)
    {
        cacheTimeout = PropertyUtils.getInt(properties, OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT,
                OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT);
    }

    @Override
    public void execute()
    {
        notificationLog.info("Cache clearance execution started.");
        AbstractSearchObjectsOperationExecutor.clearOld(new Date(new Date().getTime() - cacheTimeout * 1000L));
        notificationLog.info("Cache clearance execution finished.");
    }

}
