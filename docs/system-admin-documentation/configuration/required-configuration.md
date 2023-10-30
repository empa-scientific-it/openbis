openBIS Server Configuration
============================

After successful installation, the openBIS configuration files (which are extended Java property files) of the Application Server (AS) and data store server (DSS) should be checked. All necessary adjustments to those files should be made prior to running the system in production.


## Application Server Configuration

The openBIS Application Server is configured using the file `$INSTALL_PATH/servers/openBIS-server/jetty/etc/service.properties`. 

Each configuration item of the default service.properties file is self-documented by means of inline comments.


### Database Settings

All properties starting with `database.` specify the settings for the openBIS database. They are all mandatory.

| Property                         | Description                                                                                                                                                                                                           |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `database.engine`                  | Type of database. Currently only postgresql is supported.                                                                                                                                                            |
| `database.create-from-scratch`     | If true the database will be dropped and an empty database will be created. In productive use always set this value to  false  .                                                                                     |
| `database.script-single-step-mode` | If true all SQL scripts are executed in single step mode. Useful for localizing errors in SQL scripts. Should be always false in productive mode.                                                                    |
| `database.url-host-part`           | Part of JDBC URL denoting the host of the database server. If openBIS Application Server and database server are running on the same machine this property should be an empty string.                                |
| `database.kind`                    | Part of the name of the database. The full name reads openbis_<  kind  >.                                                                                                                                      |
| `database.admin-user`              | ID of the user on database server with admin rights, like creation of tables. Should be an empty string if default admin user should be used. In case of PostgreSQL the default admin user is assumed to be postgres. |
| database.admin-password          | Password for admin user. Usual an empty string.                                                                                                                                                                      |
| `database.owner`                   | ID of the user owning the data. This should generally be openbis. The correspoding role (and password matching the property `database.owner-password`) needs to be created on the PostgreSQL database prior to starting openBIS. In case of an empty string, it is the same user who started up the openBIS Application Server.        |
| `database.owner-password`          | Password of the owner. |

Additional configuration options are outlined [here](optional-application-server-configuration).


## Data Store Server Configuration

The openBIS Data Store Server is configured using the file `$INSTALL_PATH/servers/datastore_server/etc/service.properties`. 

Each configuration item of the default service.properties file is self-documented by means of inline comments.

Additional configuration options are outlined [here](optional-datastore-server-configuration).