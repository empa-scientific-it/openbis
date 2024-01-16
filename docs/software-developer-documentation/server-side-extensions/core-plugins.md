Core Plugins
============

## Motivation

The `service.properties` file of openBIS Application Server (AS) and Data Store Server (DSS) can be quite big because of all the configuration data for maintenance tasks, drop-boxes, reporting and processing plugins, etc. Making this configuration more modular will improve the structure. It would also allow to have core plugins shipped with distribution and customized plugins separately. This makes maintenance of these plugins more independent. For example, a new maintenance task plugin can be added in an update without any need for an admin to put the configuration data manually into the `service.properties` file.

## Core Plugins Folder Structure

All plugins whether they are a part of the distribution or added and maintained are stored in the folder usually called `core-plugins`. Standard (i.e. core) plugins are part of the distribution. During installation the folder `core-plugins` is unpacked as a sibling folder of `openBIS-server` and` datastore_server`.

The folder structure is organized as follows:

- The file `core-plugins.properties` containing the following properties:  
    - `enabled-modules`: comma-separated list of regular expressions for all enabled modules.
    - `disabled-core-plugins`: comma-separated list of disabled plugins. All plugins are disabled for which the beginning of full plugin ID matches one of the terms of this list. To disable initialization of master data of a module - disable it's core plugin "initialize-master-data"
- The children of `core-plugins` are folders denoting modules like the standard technologies, `proteomics` and `screening`. For customization, any module can be added.
- Each module folder has children which are numbered folders. The number denotes the version of the plugins of that module. The version with the largest number will be used. Different modules can have different largest version numbers.
- Every version folder has the subfolder `as` and/or` dss `which have subfolders for the various types of plugins. The types are different for AS and DSS:  
    - AS:  
        - `maintenance-tasks`: Maintenance tasks triggered by some time schedule. Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.common.maintenance.IMaintenanceTask`. For more details see [Maintenance Tasks](../../system-admin-documentation/advanced-features/maintenance-tasks.md).
        - `dss-data-sources`: Definition of data sources with corresponding data source definitions for DSS. For more details see [Installation and Administrator Guide of the openBIS Server](../../system-admin-documentation/installation/installation-and-configuration-guide.md).
        - `query-databases`: Databases for SQL queries. For more details see [Custom Database Queries](../../user-documentation/general-admin-users/custom-database-queries.md).
        - `custom-imports`: Custom file imports to DSS via Web interface. For more details see [Custom Import](../legacy-server-side-extensions/custom-import.md).
        - `services`: Custom services. For more details see [Custom Application Server Services](./as-services.md).
        - `webapps`: HTML5 applications that use the openBIS API. For more details see [openBIS webapps](../client-side-extensions/openbis-webapps.md).
        - `miscellaneous`: Any additional properties.
    - `DSS:`
        - `drop-boxes`: ETL server threads for registration of data sets.                            `
        - `reporting-plugins`: Reports visible in openBIS. Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask`. For more details see [Reporting Plugins](../legacy-server-side-extensions/reporting-plugins.md).
        - `processing-plugins`: Processing tasks triggered by users. Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask`. For more details see [Processing Plugins](../legacy-server-side-extensions/processing-plugins.md).`                            `
        - `maintenance-tasks`: Maintenance tasks triggered by some time schedule. Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.common.maintenance.IMaintenanceTask`. For more details see [Maintenance Tasks](../../system-admin-documentation/advanced-features/maintenance-tasks.md).
        - `search-domain-services`: Services for variaous search domains (e.g. search on sequence databases using BLAST). Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService`.
        - `data-sources`: Internal or external database sources.
        - `services`: Custom services. For more details see [Custom Datastore Server Services](./dss-services.md).
        - `servlet-services`: Services based on servlets. Property `class` denotes fully-qualified class name of a class implementing `javax.servlet.Servlet`.
        - `imaging-overview-plugins`: Data set type specific provider of the overview image of a data set. Property `class` denotes fully-qualified class name of a class implementing `ch.systemsx.cisd.openbis.dss.generic.server.IDatasetImageOverviewPlugin`.
        - `file-system-plugins`: Provider of a custom DSS file system (FTP/SFTP) view hierarchy. Property `class` denotes fully-qualified class name of a class
            implementing `ch.systemsx.cisd.openbis.dss.generic.server.fs.IResolverPlugin`  
            Property code denotes the name of the top-level directory
            under which the custom hierarchy will be visible
        - `miscellaneous`: Any additional properties.`                            `
- Folders of each of these types can have an arbitrary number of subfolders. But if the type folder is present it should have at least one subfolder. Each defining one plugin. The name of these subfolders define the plugin ID. It has to be unique over all plugins independent of module and plugin type. It should not contain the characters space ' ', comma '`,`', and equal sign '`=`'.
- Each plugin folder should contain at least the file `plugin.properties`. There could be additional files (referred in `plugin.properties`) but no subfolders.

Here is an example of a typical structure of a core plugins folder:

```console
core-plugins
    core-plugins.properties
    proteomics
    1
        as
        initialize-master-data.py
         dss
        drop-boxes
            ms-injection
            plugin.properties
        maintenance-tasks
            data-set-clean-up
            plugin.properties
    screening
    1
        core-plugin.properties
        as
        initialize-master-data.py
        maintenance-tasks
            material-reporting
            mapping.txt
            plugin.properties
        custom-imports
            myCustomImport
            plugin.properties
        dss
        drop-boxes
            hcs-dropbox
            lib
                custom-lib.jar
            hcs-dropbox.py
            plugin.properties
```


You might noticed the file `initialize-master-data.py` in AS core plugins sections  in this example. It is a script to register master data in the openBIS core database. For more details see [Installation and Administrator Guide of the openBIS Server](../../system-admin-documentation/installation/installation-and-configuration-guide.md).

Each plugin can refer to any number of files. These files are part of
the plugin folder. In `plugin.properties` they are referred relative to
the plugin folder, that is by file name. Example:

**plugin.properties**

```
incoming-dir = ${incoming-root-dir}/incoming-hcs
incoming-data-completeness-condition = auto-detection
top-level-data-set-handler = ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler
script-path = hcs-dropbox.py
storage-processor = ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor
storage-processor.data-source = imaging-db
storage-processor.define-channels-per-experiment = false
```


## Merging Configuration Data

At start up of AS and DSS merges the content of `service.properties` with the content of all `plugin.properties` of the latest version per enabled module. Plugin properties can be deleted by adding `<plugin ID>.<plugin property key> = __DELETED__` to service.properties. Example:

`simple-dropbox.incoming-data-completeness-condition = __DELETED__`

This leads to a deletion of the property `incoming-data-completeness-condition` specified in `plugins.properties` of the plugin `simple-dropbox`.

Merging is done by injection the properties of `plugin.properties` into `service.properties `by adding the plugin ID as a prefix to the property key (not for `miscellaneous). `For example, the property `script-path` of plugin `hcs-dropbox` becomes `hcs-dropbox.script-path`. References to files inside the plugin are replaced by a path relative to the working directory. For the various plugin types (except `miscellaneous`) the plugin ID is appended to the related property in `service.properties` for this plugin type. For example, plugins of type `drop-boxes` are added to the property `inputs`.

## Enabling Modules and Disabling Plugins

There are three methods to control which plugins are available and witch not:

- enabling by property `enabled-modules` in` core-plugins.properties`: This enables all plugins of certain modules.
- disabling by property `disabled-core-plugins` in `core-plugins.properties` : This allows to disable on a fine grade level specific plugins.
- disabling by marker file: Plugin developers should use this method when developing new plugins.

### Enabling Modules

The property `enabled-modules` in `core-plugins.properties` is a comma-separated list of regular expressions denoting modules. All plugins in a module folder of `core-plugins` folder are enabled if the module name matches one of these regular expressions. If this list is empty or the property hasn't been specified no core-plugin will be used. Note, that this property is manipulated by openBIS Installer for Standard Technologies. Example:

**service.properties**

`enabled-modules = screening, proteomics, dev-module-.*`

### Disabling Core Plugins by Property

The property `disabled-core-plugins` in `core-plugins.properties` allows to disable plugins selectively either by module name, module combined with plugin type or full plugin ID. Example:

**service.properties**

`disabled-core-plugins = screening, proteomics:reporting-plugins, proteomics:maintenance-tasks:data-set-clean-up`

### Disabling Core Plugins by Marker File

The empty marker file `disabled` in a certain plugin folder disables the particular plugin.

## Core Plugin Dependency

A core plugin can depend on another core plugin. The dependency is specified in `<module>/<version>/core-plugin.properties`. It has a property named `required-plugins`. Its value is a comma-separated list of core-plugins on which it depends. The dependency can be pecified selectively either by module name, module combined with plugin type or full plugin ID. Example:

**core-plugin.properties**

`required-plugins = module-a, module-b:initialize-master-data, module-b:reporting-plugins, module-a:drop-boxes:generic`

## Rules for Plugin Writers

As a consequence of the way plugins are merged with `service.properties` writers of plugins have to obey the following rules:

- Plugin IDs have to be unique among all plugins whether they are defined in `service.properties` or as core plugins. The only exceptions are plugins of type `miscellaneous`.
- In `plugin.properties` other properties can be referred by the usual `${<property key>`} notation. The referred property can be in `service.properties` or in any `plugin.properties`.
- As convention use `${incoming-root-dir`} when defining the incoming folder for a drop box.
- Refer files in `plugin.properties` only by names and add them as siblings of `plugin.properties` to the plugin folder. Note, that different plugins can refer files with the same name. There will be no ambiguity which file is meant.
- In order to be completely independent from updates of the core plugins which are part of the distribution create your own module, like `my-plugins`, and put all your plugins there. Do not forget to add your module to the property `enabled-modules` in `core-plugins.properties`.

## Using Java libraries in Core Plugins

OpenBIS allows you to include Java libraries in core plugin folders. The \*.jar files have to be stored in `<code plugin folder>/lib` folder. For instance, in order to use "my-lib.jar" in "my-dropbox" a following file structure is needed:

**service.properties**

```
my-technology
    1
        dss
        drop-boxes
            my-dropbox
            lib
                my-lib.jar
            dropbox.py
            plugin.properties
```

Having this structure, Java classes from "my-lib.jar" can be imported and used in "dropbox.py" script.

```{note}
Currently this feature is only supported for DSS core plugins. Under the hood, a symbolic link to a jar file is created in "datastore\_server/lib" folder during DSS startup.
```
