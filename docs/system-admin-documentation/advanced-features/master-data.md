Master data import/export
=========================

The master data of openBIS comprises all entity/property types, property
assignments, vocabularies etc. needed for your customized installation
to work. The system offers a way to export/import master data via Jython
scripts. More information on how to do create such scripts and run them
manually see the advanced guide [Jython Master Data Scripts](../../uncategorized/jython-master-data-scripts##command-line-tools).

A master data script can be run automatically by start up of the AS if
it is defined in an AS core plugin. The script path should be
`$INSTALL_PATH/servers/core-plugins/<module name>/<version number>/as/initialize-master-data.py`.
For more details about the folder structure of core plugins see [Core
Plugins](../../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins). If there are several
core plugins with master data scripts the scripts will be executed in
alphabetical order of the module names. For example, the master data
script of module `screening-optional` will be executed after the master
data script of module `screening` has been executed.

Execution of master data script can be suppressed by
disabling `initialize-master-data` core plugin. For more details see
[Core Plugins](../../software-developer-documentation/server-side-extensions/core-plugins.md).