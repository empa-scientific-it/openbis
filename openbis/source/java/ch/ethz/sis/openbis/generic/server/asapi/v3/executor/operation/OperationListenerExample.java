package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
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
    public void beforeOperation(IApplicationServerInternalApi api, Session session, IOperation operation)
    {
        operationLog.info("beforeOperation: " + operation.getClass().getSimpleName());
    }

    @Override
    public void afterOperation(IApplicationServerInternalApi api, Session session, IOperation operation,
                               IOperationResult result, RuntimeException runtimeException)
    {
        operationLog.info("afterOperation: " + operation.getClass().getSimpleName());
    }
}
