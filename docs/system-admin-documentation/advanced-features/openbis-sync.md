openBIS Sync
============

## Introduction

This allows to synchronize two openBIS instances. One instance (called
Data Source) provides the data (meta-data and data sets). The other
instance (called Harvester) grabs these data and makes them available.
In regular time intervals the harvester instance will synchronize its
data with the data on the data source instance. That is, synchronization
will delete/add data from/to the harvester instance. The harvester
instance can synchronize only partially. It is also possible to gather
data from several data-source instances.

## Data Source

The Data Source instance provides a service based on the ResourceSync Framework Specification (see <http://www.openarchives.org/rs/1.1/resourcesync>). This service is provided as [core plugin](../../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins) module `openbis-sync` which has a DSS service based on [Service Plugins](../../uncategorized/service-plugins.md).

This DSS service access the main openBIS database directly. If the name of this database isn't {{openbis\_prod}} the property `database.kind` in DSS service.properties should be defined with the same value as the same property in AS service.properties. Example:

**servers/openBIS-server/jetty/etc/plugin.properties**

```html
...
database.kind = production
...

**servers/datastore\_server/etc/plugin.properties**

...
database.kind = production
...
```


The URL of the service is `<DSS base URL>/datastore_server/re-sync`. The
returned XML document looks like the following:

```xml
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/">
    <rs:ln href="https://localhost:8444/datastore_server/re-sync/?verb=about.xml" rel="describedby"/>
    <rs:md capability="description"/>
    <url>
    <loc>https://localhost:8444/datastore_server/re-sync/?verb=capabilitylist.xml</loc>
    <rs:md capability="capabilitylist"/>
    </url>
</urlset>
```


The loc element contains the URL which delivers a list of all
capabilities:

<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/">
    <rs:ln href="https://localhost:8444/datastore_server/re-sync/?verb=about.xml" rel="up"/>
    <rs:md capability="capabilitylist" from="2013-02-07T22:39:00"/>
    <url>
    <loc>https://localhost:8444/datastore_server/re-sync/?verb=resourcelist.xml</loc>
    <rs:md capability="resourcelist"/>
    </url>
</urlset>
```


From capabilities described in the ResourceSync Framework Specification
only `resourcelist` is supported. The resourcelist returns an XML with
all metadata of the data source openBIS instance. This includes master
data, meta data including file meta data.

Two optional URL parameters filter the data by spaces:

-   `black_list`: comma-separated list of regular expressions. All
    entities which belong to a space which matches one of the regular
    expressions of this list will be suppressed.
-   `white_list`: comma-separated list of regular expressions. If
    defined only entities which belong to a space which matches one of
    the regular expressions of this list will be delivered (if not
    suppressed by the black list).

Remarks:

-   Basic HTTP authentication is used for authentication.
-   The resourcelist capability returns only data visible for the user
    which did the authentication.

## Harvester

In order to get the data and meta-data from a Data Source openBIS instance a DSS harvester [maintenance task](./maintenance-tasks.md#maintenance-tasks) has to be configured on the Harvester openBIS instance. This maintenance task reads another configuration file each time the task is executed.

**plugin.properties**

```
class = ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.HarvesterMaintenanceTask
interval = 1 d
harvester-config-file = ../../data/harvester-config.txt
```


The only specific property of `HarvesterMaintenanceTask` is
`harvester-config-file` which is absolute or relative path to the actual
configuration file. This separation in two configuration files has been
done because `plugin.properties` is only read once (at start up of DSS).
Thus changes in Harvester configuration would be possible without
restarting DSS.

This DSS service access the main openBIS database directly in order to
synchronize timestamps and users. If the name of this database isn't
{{openbis\_prod}} the property `database.kind` in DSS service.properties
should be defined with the same value as the same property in AS
service.properties. Example:

**servers/openBIS-server/jetty/etc/plugin.properties**

```
...
database.kind = production
...
```


**servers/datastore\_server/etc/plugin.properties**

```
...
database.kind = production
...
```


### Harvester Config File

Here is an example of a typical configuration:

**harvester-config.txt**

```
[DS1]

resource-list-url = https://<data source host>:<DSS port>/datastore_server/re-sync

data-source-openbis-url = https://<data source host>:<AS port>/openbis/openbis
data-source-dss-url = https://<data source host>:<DSS port>/datastore_server
data-source-auth-realm = OAI-PMH
data-source-auth-user = <data source user id>
data-source-auth-pass = <data source password>
space-black-list = SYSTEM
space-white-list = ABC_.*

harvester-user = <harvester user id>
harvester-pass = <harvester user password>

keep-original-timestamps-and-users = false
harvester-tmp-dir = temp
last-sync-timestamp-file = ../../data/last-sync-timestamp-file_HRVSTR.txt
log-file = log/synchronization.log

email-addresses = <e-mail 1>, <e-mail 2>, ...

translate-using-data-source-alias = true
verbose = true
#dry-run = true
```


-   The configuration file can have one or many section for each openBIS
    instance. Each section start with an arbitrary name in square
    brackets.
-   `<data source host>`, `<DSS port>` and `<AS port>` have to be host
    name and ports of the Data Source openBIS instance as seen by the
    Harvester instance.
-   `<data source user id>` and `<data source password>` are the
    credential to access the Data Source openBIS instance. Only data
    seen by this user is harvested.
-   `space-black-list` and `space-white-list` have the same meaning
    as `black_list` and `white_list` as specified above in the Data
    Source section.
-   `<harvester user id>` and `<harvester user password>` are the
    credential to access the Harvester openBIS instance. It has to be a
    user with instance admin rights.
-   `Temporary `files created during harvesting are stored
    in` harvester-tmp-dir` which is a path relative to the root of the
    data store. The root store is specified by `storeroot-dir` in
    DSS `service.properties`. The default value is `temp`.
-   By default the original timestamps (registration timestamps and
    modification timestamps) and users (registrator and modifier) are
    synchronized. If necessary users will be created. With the
    configuration property  `keep-original-timestamps-and-users = false`
    no timestamps and users will be synchronized. 
-   The `last-sync-timestamp-file` is a relative or absolute path to the
    file which store the last timestamp of synchronization.
-   The `log-file` is a relative or absolute path to the file where
    synchronization information is logged. This information does not
    appear in the standard DSS log file.
-   In case of an error an e-mail is sent to the specified e-mail
    addresses.
-   `translate-using-data-source-alias` is a flag which controls whether
    the code of spaces, types and materials should have a prefix or not.
    If true the prefix will be the name in the square bracket followed
    by an underscore. The default value of this flag is false.
-   `verbose` flag adds to the synchronization log added, updated and
    deleted items. Default: `false` or `true` if `dry-run` flag is set.
-   `dry-run` flag allows to run without changing Harvester openBIS
    instance. This allows to check config errors or errors with the Data
    Source openBIS instance. A dry run will be performed even if this
    flag is set. Default: `false`
-   `master-data-update-allowed` flag allows to update master data as
    plugins, property types, entity types and entity assignments. Note,
    that master data can still be added if this flag is `false`.
    Default: `false`
-   `property-unassignment-allowed` flag allows to unassign property
    assignments, that is, removing property types from entity types.
    Default: `false`
-   `deletion-allowed` flag allows deletion of entities on the Harvester
    openBIS instance. Default: `false`
-   `keep-original-timestamps-and-users` flag yields that time stamps
    and users are copied from the Data Source to the Harvester.
    Otherwise the entities will have harvester user and the actual
    registration time stamp. Default: `true`
-   `keep-original-frozen-flags` flag yields that the frozen flags are
    copied from the Data Source to the Harvester. Otherwise entities
    which are frozen on the Data Source are not frozen on the Harvester.
    Default: `true`

### What HarvesterMaintenanceTask does

In the first step it reads the configuration file from the file path
specified by `harvester-config-file` in `plugins.properties`. Next, the
following steps will be performed in DRY RUN mode. That is, all data are
read, parsed and checked but nothing is changed on the Harvester. If no
error occured and the `dry-run` flag isn't set the same steps are
performed but this time the data is changed (i.e. synced) on the
Harvester.

1.  Read meta data from the Data Source.
2.  Delete entities from the Harvester which are no longer on the Data
    Source (if `deletion-allowed` flag is set).
3.  Register/update master data.
4.  Register/update spaces, projects, experiments, samples and
    materials.
5.  Register/update attachments.
6.  Synchronize files from the file service.
7.  Register/update data sets.
8.  Update timestamps and users (if `keep-original-timestamps-and-users`
    flag is set).
9.  Update frozen flags (if `keep-original-frozen-flags` flag is set).

-   Data are registered if they do not exists on the Harvester.
    Otherwise they are updated if the Data Source version has a
    modification timestamp which is after the last time the
    HarvesterMaintenanceTask has been performed
-   If `translate-using-data-source-alias` flag is set a prefix is added
    to spaces, types and materials when created. 
-   To find out if an entity already exist on the Harvester the perm ID
    is used.

### Master Data Synchronization Rules

Normally all master data are registered/updated if they do not exists or
they are older. But for internal vocabularies and property types
different rules apply. Internal means that the entity (i.e. a vocabulary
or a property type) is managed internally (visible by the prefix '$' in
its code) and has been registered by the system user.

1.  Internal vocabularies and property types will not be created or
    updated on the Harvester.
2.  An internal vocabulary or property type of the Data Source which
    doesn't exist on the Harvester leads to an error.
3.  An internal property type which exists on the Data Source and the
    Harvester but have different data type leads to an error.
4.  Terms of an internal vocabulary are added if they do not exists on
    the Harvester.
