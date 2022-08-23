package ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import java.util.Properties;

public interface IOperationListener<OPERATION extends IOperation, RESULT extends IOperationResult>
{
    public static final String LISTENER_PROPERTY_KEY = "operation-listener";
    public static final String LISTENER_CLASS_KEY = LISTENER_PROPERTY_KEY + ".class";
    public abstract void setup(Properties properties);
    public abstract void beforeOperation(IApplicationServerApi api, String sessionToken, OPERATION operation);
    public abstract void afterOperation(IApplicationServerApi api, String sessionToken, OPERATION operation,
                                        RESULT result, RuntimeException runtimeException);
}
