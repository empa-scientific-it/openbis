Optional Datastore Server Configuration
=======================================

## Configuring DSS Data Sources

It is quite common that openBIS AS is using a database filled by DSS.
Depending on the DSS (specified by the DSS code) and the technology
different databases have to be used.

Configuration is best done by AS [core
plugins](../../software-developer-documentation/server-side-extensions/core-plugins.md) of type
`dss-data-sources`. The name of the plugin is just the DSS code. The
following properties of `plugin.properties` are recognized:

| Property Key                                     | Description                                                                                                                                                                                                                             |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| technology                                       | Normally the technology/module folder of the core plugin specifies the technology/module for which this data source has to be configured. If this is not the case this property allows to specify the technology/module independently.  |
| database-driver                                  | Fully qualified class name of the data base driver, e.g. `org.postgresql.Driver`.                                                                                                                                                        |
| database-url                                     | URL of the database, e.g. `jdbc:postgresql://localhost/imaging_dev`                                                                                                                                                                       |
| username                                         | Optional user name needed to access database.                                                                                                                                                                                          |
| password                                         | Optional password needed to access database.                                                                                                                                                                                           |
| validation-query                                 | Optional SQL script to be executed to validate database connections.                                                                                                                                                                   |
| database-max-idle-connections                    | The maximum number of connections that can remain idle in the pool. A negative value indicates that there is no limit. Default: -1                                                                                                      |
| database-max-active-connections                  | The maximum number of active connections that can be allocated at the same time. A negative value indicates that there is no limit. Default: -1                                                                                         |
| database-max-wait-for-connection                 | The maximum number of seconds that the pool will wait for a connection to be returned before throwing an exception. A value less than or equal to zero means the pool is set to wait indefinitely. Default: -1                          |
| database-active-connections-log-interval         | The interval (in ms) between two regular log entries of currently active database connections if more than one connection is active. Default: Disabled                                                                                  |
| database-active-number-connections-log-threshold | The number of active connections that will trigger a NOTIFY log and will switch on detailed connection logging. Default: Disabled                                                                                                       |
| database-log-stacktrace-on-connection-logging    | If true and logging enabled also stack traces are logged. Default: `false`                                                                                                                                                                |

Properties `database-driver` and `database-url` can be omitted if a
`etc/dss-datasource-mapping` is defined. For more see [Sharing Databases](../../uncategorized/sharing-databases.md).