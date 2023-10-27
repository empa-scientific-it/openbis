# Sharing Databases

## Introduction

Application server and data store server(s) can share the same database.
For example, openBIS screening uses a database for image meta data
(called imaging-db) which is used by DSS to register and delivering
images. It is also used by AS to provide information about available
images and transformations.

For configuration of the data bases [core plugin](../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins) on the AS and for each DSS have to be defined. For a DSS it is a core plugin of type `data-sources` and for AS it is a core plugin of type `dss-data-sources`. Optionally the AS can get configuration parameters from its registered DSS instances by defining a mapping file `etc/dss-datasource-mapping` for the AS.

When a DSS is registering itself at the AS all its data source
definitions are provided and stored on the AS. This allows the AS (if a
mapping file is defined)

-   to reduce configuration of core plugins of type `dss-data-sources`
    to a minimum.
-   to have only one core plugin of type `dss-data-sources` independent
    of the number of technologies/modules and DSS instances.

The AS can have only one data source per pair defined by data store code
and module code.

## Share Databases without Mapping File

Without a mapping file specified data sources are independently defined for DSS and AS. For details see [DSS Data Sources](https://unlimited.ethz.ch/display/openBISDoc2010/Installation+and+Administrators+Guide+of+the+openBIS+Data+Store+Server#InstallationandAdministratorsGuideoftheopenBISDataStoreServer-DataSources) and [AS Data Sources](../system-admin-documentation/installation/installation-and-configuration-guide.md#configuring-dss-data-sources), respectively. Note, that the roperties `database-driver` and `database-url` are mandatory for AS.

## Share Databases with Mapping File

When a mapping file is used the configuration doesn't change for data
sources defined for DSS. But the configuration parameters for an
actually used data source in AS can come from three sources:

-   AS core plugins of type `dss-data-sources`
-   Data source definitions as provided by the data stores
-   Mapping file `etc/dss-datasource-mapping`

AS core plugins no longer need to define the properties
 `database-driver` and `database-url` because they are provided by DSS
or the mapping file. The same is true for properties `username`
and `password.` In fact the` plugin.properties` can be empty. Usually
only parameters for logging and connection pooling are used.

The mapping file is used to pick the right AS core plugin and the right
data source provided by the DSS. In addition database credentials can be
overwritten by the mapping file.

```{warning}
Only those properties in a core plugin of type `dss-data-source` are overwritten which are **undefined**.
```

The mapping file is a text file with lines of the following syntax:

\<data store code pattern\>.\<module code pattern\>.\<type\> = \<value\>

where \<data store code pattern\> and \<module code pattern\>
are wildcard patterns for the data store code and module/technology
code, repectively. The \<type\> can have one of the following
values:

|Type|Meaning of <value>|
|--- |--- |
|config|Mapping from the actual data store and module code to an existing AS core plugin of type dss-data-sources. The value should have one of the two following forms:<data store code><data store code>[<module code>]If a code is a star symbol the corresponding actual value of the data store code or module code is used. Note, that the first form (without module code) is usefully only if the data source is define in the service.properties of AS.|
|data-source-code|The data source code as provided by the DSS.|
|host-part|Host part of the data source URL. It contains the host name (or IP address) and optional the port. Overwrites the value provided by the DSS.|
|sid|Unique identifier of the database. In most cases this is the database name. Overwrites the value provided by the DSS.|
|username|User name. Overwrites the value provided by the DSS.|
|password|User password. Overwrites the value provided by the DSS.|

Empty lines and lines starting with '\#' will be ignored.

When AS needs a data source for a specific data store and module it will
consult the mapping file line by line. For each type it considers only
the last line matching the actual data store and module code. From this
information it is able to pick the right AS core plugin of
type `dss-data-sources`, the data source definitions provided by DSS at
registration, and the values for the host part of the URL, database
name, user and password.

If there is no matching line of type `config` found the AS core plugin
with key \<actual data store code\>\[\<actual module code\>\] is
used.

If there is no matching line of type `data-source-code` found it is
assumed that the data store has one and only one data source. Thus data
store code has to be defined in the mapping file if the data store has
more than one data source. Remember, per data store and module there can
be only one data source for AS.

Here are some examples for various use cases:

### Mapping all DSSs on one

**etc/dss-datasource-mapping**

`*.*.config = dss`

This means that any request for data source for data store x and
module/technology y will be mapped to the same configuration. If one of
the properties driver class, URL, user name, and password is missing it
will be replaced by the data source definition provided by data store
server x at registration. This works only, if all DSS instances have
only **one** data source specified.

The following mapping file is similar:

**etc/dss-datasource-mapping**

`*.*.config = dss[*]`

This means that any request for data source for data store x and
module/technology y will be mapped to AS core plugin DSS of module y.

### Mapping all DSSs on one per module

**etc/dss-datasource-mapping**

```
*.proteomics.config = dss1[proteomics]
*.proteomics.data-source-code = proteomics-db
*.screening.config = dss1[screening]
*.screening.data-source-code = imaging-db
```


All DSS instances for the same module are mapped onto an AS core plugin
named DSS1 for the corresponding module. This time the data source code
is also specified. This is needed if the corrsponding DSS has more than
one data source defined. For example in screening `path-info-db` is
often used in addition to `imaging-db` to speed up file browsing in the
data store.

### Overwriting Parameters

Reusing the same AS dss-data-sources core plugin is most flexible with
the mapping file if no driver, URL, username and password have been
defined in such a core plugin. In this case all these parameters come
form the data source information provided at DSS registration. If DSS
and AS are running on the same machine AS can usually use these
parameters. In this case mapping files like  in the previous examples
are enough.

The situation is different if the DSS instances, AS and the database
server running on different machines. The following example assumes that
the AS and the database server running on the same machine but at least
one of the DSS instances are running on a different machine. In this
case the database URL for the such a DSS instances could be different
than the URL for the AS.

**etc/dss-datasource-mapping**

```
*.screening.config = dss1[screening]
*.screening.data-source-code = imaging-db
*.screening.host-part = localhost 
```


Also database name (aka sid), user, and password can be overwritten in
the same way.

### Overwriting Generic Settings

**etc/dss-datasource-mapping**

```
*.screening.config = dss1[screening]
*.screening.data-source-code = imaging-db
*.screening.host-part = localhost 
*.screening.username = openbis
*.screening.password = !a7zh93jP.
DSS3.screening.host-part = my.domain.org:1234
DSS3.screening.username = ob
DSS3.screening.password = 8uij.hg6
```


This is an example where all DSS instances except DSS3 are accessing the
same database server which is on the same machine as the AS. Username
and password are also set in order to ignore corresponding data source
definitions of all DSS instances. DSS3 uses a different database server
which could be on the same machine as DSS3. Also username and password
are different.

Note, that the generic mapping definitions (i.e. definitions with wild
cards for data store codes or module codes) should appear before the
more specific definitions.
