# API Listener Core Plugin (V3 API)

Introduction
------------

The V3 API listener core plugin is an implementation of the interceptor pattern: `<https://en.wikipedia.org/wiki/Interceptor_pattern>`

It actually intercepts twice, right before an operation is executed, and right after.

![image info](img/122.png)

Its main focus is to help integrations. It gives an opportunity to integrators to execute additional functionality before or after an api call with the next purposes:

- Modify the API call inputs/outputs immediately before/after they reach its executor.
- Trigger additional internal logic.
- Notify third party systems.

Core Plugin
-----------

To archive these goals is necessary to provide a core plugin of the type 'api-listener' to the AS:

### Plugin.properties

It is required to provide an 'operation-listener.class' indicating the class name of the listener that will be loaded.

Additionally any number of properties following the pattern `operation-listener.<your-custom-name>` can be provided. Custom properties are provided to help maintainability, they give an opportunity to the integrator to only need to compile the listener once and configure it differently for different instances.

**plugin.properties**

```
operation-listener.class = ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.OperationListenerExample
operation-listener.your-config-property = Your Config Message
```


### lib

The core plugin should contain a lib folder with a jar containing a class that implements the interface IOperationListener, this interface is provided with the V3 API jar and provides 3 methods:

- setup: Runs on startup. Gives one opportunity to read the configuration provided to the core plugin
- beforeOperation: Runs before each operation occurs. In addition to the operation intercepted it also provides access to the api and the session token used for the operation.
- afterOperation: Intercepts after the operation occurs. In addition to the operation intercepted it also provides access to the api, the session token used for the operation, the operation result and any exception that happened during the operation.

```{warning}
Implicit Requirements

**Requirement 1:  The Listener should be Thread Safe Code**

A single instance of the Listener is created during the server startup. Since a single instance is used to serve all requests thread safe code is a requirement. We strongly suggest to not to keep any state.

**Requirement 2: The Listener should not throw Exceptions**

If the listener throw an exception it will make the API call fail.

**Requirement 3: The Listener should use IOperation and IOperationResult as indicated below**

All API Operations go through every listener so the method signatures should use IOperation and IOperationResult.<br /><br />Please use instanceof for safe casting.
```

**IOperationListener**

```java
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
```


### Example - Logging

The next implementation example captures the calls and logs on the standard openbis log the operation name:

**OperationListenerExample**

```java
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

    private String yourConfigProperty = null;

    @Override
    public void setup(Properties properties)
    {
        yourConfigProperty = properties.getProperty("operation-listener.your-config-property");
        operationLog.info("setup: " + yourConfigProperty);
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
```


### Example - Loggin Sources

You can download a complete example with sources [here](att/api-listener-example.zip) to use as a template to make your own.
