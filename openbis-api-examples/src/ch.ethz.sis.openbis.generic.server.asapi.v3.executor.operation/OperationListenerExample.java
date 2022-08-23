package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener.IOperationListener;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.Properties;

public class OperationListenerExample implements IOperationListener<IOperation, IOperationResult>
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            OperationListenerExample.class);

    @Override
    public void setup(Properties properties)
    {

    }

    @Override
    public void beforeOperation(IApplicationServerApi api, String sessionToken, IOperation operation)
    {
        operationLog.info("beforeOperation: " + operation.getClass().getSimpleName());
    }

    @Override
    public void afterOperation(IApplicationServerApi api, String sessionToken, IOperation operation,
                               IOperationResult result, RuntimeException runtimeException)
    {
        operationLog.info("afterOperation: " + operation.getClass().getSimpleName());
    }
}
