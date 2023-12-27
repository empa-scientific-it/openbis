Custom Datastore Server Services
==================================

## Introduction

On Data Store Server (DSS) it is possible to define services that - upon request - will compute a desired output from a custom code. These services have full access on data store and Application Server (AS).

These services are created by:
1. Creating DSS core plugin of type `services`.
2. Implementing special logic that would be triggered upon request.

## How to write a custom DSS service core plugin

Here is the recipe to create an DSS core plugin of type `services`:

1. The folder `<core plugin folder>/<module>/<version>/dss/services/<core plugin name>` has to be created.

2. In this folder two files have to be created: `plugin.properties` and `script.py`. The properties file should contain:

   **plugin.properties**

    ```py
    class = ch.ethz.sis.openbis.generic.server.dss.plugins.JythonBasedCustomDSSServiceExecutor
    script-path = script.py
    ```

3. The script file should have the function `process` with two arguments. The first argument is the context. It contains the methods `getSessionToken()` and `getApplicationService()` which returns an instance of `ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi`. The second argument is a map of key-value pairs. The key is a string and the values is an arbitrary object. Anything returned by the script will be returned to the caller of the service. Here is an example of a script which creates a space:

   **script.py**
    ```py
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation

    def process(context, parameters):
        space_creation = SpaceCreation()
        space_creation.code = parameters.get('space_code');
        result = context.applicationService.createSpaces(context.sessionToken, [space_creation]);
        return "Space created: %s" % result
    ```
   Note, that all changes on the AS database will be done in one transaction.

## How to use a custom DSS service


With the following method of the API version 3 a specified service can
be executed:

```java
public Object executeCustomDSSService(String sessionToken, ICustomDSSServiceId serviceId, CustomDSSServiceExecutionOptions options);
```

The `serviceId` can be created as an instance of `CustomDssServiceCode`. Note, that the service code is just the core plugin name.

Parameter bindings (i.e. key-value pairs) are specified in the `CustomDSSServiceExecutionOptions` object by invoking for each binding the method `withParameter()`.

Here is a code example:

```java
CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions().withParameter("space_code", "my-space");
Object result = service.executeCustomDSSService(sessionToken, new CustomDssServiceCode("space-creator"), options);
System.out.println(result);
```
 
