Custom Import
=============

### Introduction

`Custom Import` is a feature designed to give web users a chance to
import a file via `Jython Dropboxes`.

### Usage

To upload a file via `Custom Import`, the user should
choose `Import -> Custom Import` in openBIS top menu. The
`Custom Import` tab will be opened, and the user will get the combo box
filled with the list of configured imports. After selecting the desired
`Custom Import, the` user will be asked to select a file. After
selecting a file and clicking `the Save` button, the import will start.
The user should be aware, that the import is done in a synchronous way,
sometimes it might take a while to import data (it depends on the
dropbox code).

If a template file has been configured a download link will appear. The
downloaded template file can be used to create the file to be imported.

### Configuration

To have the possibility to use a `Custom Import` functionality, this
needs an AS [core plugin](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html) of type
custom-imports. The `plugin.properties` of each plugin has several
parameters:

|parameter name          |description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|name                    |The value of this parameter will be used as a name of Custom Import in web UI.                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|dss-code                |This parameter needs to specify the code of the datastore server running the dropbox which should be used by the Custom Import.                                                                                                                                                                                                                                                                                                                                                                                                                          |
|dropbox-name            |The value is the name of the dropbox that is used by the Custom Import.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|description             |Specifies a description of the Custom Import. The description is shown as a tooltip in the web UI.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|template-entity-kind    |Custom import templates are represented in OpenBIS as entity attachments. To make a given file available as a custom import template create an attachment with this file and refer to this attachment with template-entity-kind, template-entity-permid, template-attachment-name parameters, where: template-entity-kind is the kind of the entity the attachment has been added to (allowed values: PROJECT, EXPERIMENT, SAMPLE), template-entity-permid is the perm id of that entity and template-attachment-name is the file name of the attachment.|
|template-entity-permid  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|template-attachment-name|                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |


#### Example configuration

**plugin.properties**

    name = Example custom import
    dss-code = DSS1
    dropbox-name = jython-dropbox-1
    description = This is an example custom import
    template-entity-kind = PROJECT
    template-entity-permid = 20120814111307034-82319
    template-attachment-name = project_custom_import_template.txt

The dropbox needs to be defined on `the DSS` side as a `RPC dropbox`:

**service.properties**

    dss-rpc.put.<DATA_SET_TYPE> = jython-dropbox-1

 
