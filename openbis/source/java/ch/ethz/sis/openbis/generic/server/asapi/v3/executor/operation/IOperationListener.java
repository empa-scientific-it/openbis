package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

import java.util.Properties;


public interface IOperationListener<OPERATION extends IOperation, RESULT extends IOperationResult>
{
    public static final String LISTENER_PROPERTY_KEY = "operation-listener";
    public static final String LISTENER_CLASS_KEY = LISTENER_PROPERTY_KEY + ".class";
    public abstract void setup(Properties properties);
    public abstract void beforeOperation(IApplicationServerInternalApi api, Session session, OPERATION operation);
    public abstract void afterOperation(IApplicationServerInternalApi api, Session session, OPERATION operation,
                                        RESULT result, RuntimeException runtimeException);
}
