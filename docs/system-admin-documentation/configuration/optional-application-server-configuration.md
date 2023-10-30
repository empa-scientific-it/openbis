Optional Application Server Configuration
=========================================

### Deleted Entity History

Logging the history of deleted entities can be enabled / disabled in the AS service.properties using setting

    entity-history.enabled = \[true | false\]

Since 20.10.1 the default value is true (meaning, entity history is enabled). Before 20.10.1 the default value was false.

Deleted entity history can be queried with script `$INSTALL_PATH/bin/show-history.sh`.


## Login Page - Banners

To add banners to the main openBIS change `loginHeader.html` page. It is
stored in the same directory as `index.html`. Note that if the height of
`loginHeader.html` is too big, the content may overlap with the rest of
the openBIS login page.

Example of the `loginHeader.html`:

    <center><img src="images/banner.gif"></center>

For announcements you have to edit the `index.html` file. Here is an example showing the tail:

```html
<input style="margin-left: 200px" type="submit" id="openbis_login_submit" value="Login">
<br>
<br>
<br>
<br>
<span style="color:red">
Due the server maintenance openBIS 
<br>
will not be available on 24th of 
<br>
December 2023 from 10 am to 3 pm!
<br>
</span>
</form>
</div>
</body>
</html>
```

Note: the current work-around with `br` tags between the lines ensures that the login box is still centered.


## Client Customization

### Configuration

To reconfigure some parts of the openBIS Web Client and Data Set Upload
Client, prepare the configuration file and add the path to the value of
`web-client-configuration-file` property in openBIS
`service.properties`.

    web-client-configuration-file = etc/web-client.properties


### Web client customizations

- Enable the trashcan. When the trashcan is enabled, deleting entities
    only marks them as deleted but not deletes them physically (it is
    also called "logical deletion"). When clicking on the trashcan icon
    in the Web GUI, the user can see all of his deletion operations and
    can revert them individually. Only an admin can empty the trashcan
    and thus delete the entities physically. Only with enabled trashcan
    is it possible to delete complete hierarchies (e.g. an experiment
    with samples and datasets attached).
- Default view mode (`SIMPLE/NORMAL`) that should be used if user
    doesn't have it specified in a URL.
- Replacement texts for 'Experiment' and 'Sample' by `experiment-text`
    and `sample-text`, respectively.
- Anonymous login by default.
- Sample, material, experiment and data set `detail views `can be
    customized by:
    - hiding the sections (e.g. attachments)
- Additionally `data set detail view` can be customized by:
    - removing `Smart View` and `File View` from the list of available
        reports in `Data View` section
- Technology specific properties with property `technologies` which is
    a comma-separated list of technologies. For each technology
    properties are defined where the property names start with
    technology name followed by a dot character.


### Data Set Upload Client Customizations

It is possible to restrict the set of data set types available to the
user in the data set uploader. This is useful when there are some data
set types that a user would never upload; for example, if there are data
set types that are used only internally exist only to support
third-party software.

The restriction is specified in the web-client.properties file using
either a whitelist or a blacklist. If both are specified, the whitelist
is used. To specify a whitelist, use the key
`creatable-data-set-types-whitelist`; for a blacklist, use the key
`creatable-data-set-types-blacklist`. The value for the property should
be a comma-separated list of regular-expression patterns to match. In
the case of the whitelist, data set types that match the specified
patterns are shown to the user, whereas for the blacklist, the data set
types that match the specified patterns are those that are not shown to
the user.


#### Examples

- Specifying a whitelist

**web-client.properties.**

    creatable-data-set-types-whitelist = .*IMAGE.*, ANALYSIS, THUMBNAIL[0-9]?

Assume we have the following data set types in our system:

*PROCESSED-DATA*, *MICROSCOPE-IMAGE*, *IMAGE-SCREENING*, *ANALYSIS*,
*ANALYSIS-FEATURES*, *THUMBNAIL1*, *THUMBNAIL-BIG*

In this case, the follwing data set types will be available to the user:

*MICROSCOPE-IMAGE*, *IMAGE-SCREENING*, *ANALYSIS*, *THUMBNAIL1*

- Specifying a blacklist

**web-client.properties.**

    creatable-data-set-types-blacklist = .*IMAGE.*, ANALYSIS, THUMBNAIL[0-9]?

Assume we have the following data set types in our system:

*PROCESSED-DATA*, *MICROSCOPE-IMAGE*, *IMAGE-SCREENING*, *ANALYSIS*,
*ANALYSIS-FEATURES*, *THUMBNAIL1*, *THUMBNAIL-BIG*

In this case, the follwing data set types will be available to the user:

*PROCESSED-DATA*, *ANALYSIS-FEATURES*, *THUMBNAIL-BIG*


### Full web-client.properties Example

**web-client.properties**

```
# Enable the trash can and logical deletion.
# Default value: false
enable-trash = true

# Replacement texts for 'Experiment' and 'Sample' in the UI 
# sample-text = Object
# experiment-text = Collection

# Default view mode that should be used if user doesn't have it specified in URL.
# Options: 'NORMAL' (standard or application mode - default), 'SIMPLE' (read-only mode with simplified GUI)
#
default-view-mode = SIMPLE

# Flag specifying whether default login mode is anonymous or not. 
# If true a user-for-anonymous-login has to be defined in service.properties
# Default value: false
default-anonymous-login = true

# Configuration of entity (experiment, sample, data set, material) detail views.
#
# Mandatory properties:
#   - view (entity detail view id)
#   - types (list of entity type codes)
# Optional properties:
#   - hide-sections (list of section ids)
#   - hide-smart-view (removes "Smart View" from Data Set Detail View -> Data View) (generic_dataset_viewer)
#   - hide-file-view (removes "File View" from Data Set Detail View -> Data View) (generic_dataset_viewer)
# Available sections in entity-detail-views:
#   generic_dataset_viewer
#       data-set-data-section
#       data-set-parents-section
#       data-set-children-section
#       query-section
#   generic_experiment_viewer
#       data-set-section
#       attachment-section
#       query-section
#       container-sample-section
#   generic_sample_viewer
#       container-sample-section
#       derived-samples-section
#       parent-samples-section
#       data-set-section
#       attachment-section
#       query-section
#   generic_material_viewer
#       query-section
#
# Example:
#
#detail-views = sample-view, experiment-view, data-view
#
#sample-view.view = generic_sample_viewer
#sample-view.types = STYPE1, STYPE2
#sample-view.hide-sections = derived-samples-section, container-sample-section
#
#experiment-view.view = generic_sample_viewer
#experiment-view.types = ETYPE1, ETYPE2
#experiment-view.hide-sections = data-set-section
#
#data-view.view = generic_dataset_viewer
#data-view.types = DSTYPE
#data-view.hide-smart-view = false
#data-view.hide-file-view = true

#technologies = screening
#screening.image-viewer-enabled = true

#
# Only render these types when creating new data sets via the 
# Data Set Upload Client
#
#creatable-data-set-types-whitelist=WHITELISTED_TYPE1, WHITELISTED_TYPE2

#
# Do not render these types in the Data Set Upload Client. 
# The value of the property is only taken into account if  
# creatable-data-set-types-whitelist is not configured
#
#creatable-data-set-types-blacklist=BLACKLISTED_TYPE1, BLACKLISTED_TYPE2
```


## Configuring File Servlet

This service is specially tailored for web applications requiring to
upload files to the system without using the DataSet concept, it was
meant to be used for small images and rich text editors like CKEditor.

| Property Key                        | Default Value              | Description                                                                                               |
|-------------------------------------|----------------------------|-----------------------------------------------------------------------------------------------------------|
| file-server.maximum-file-size-in-MB | 10                         |  Max size of files.                                                                                      |
| file-server.repository-path         |  ../../../data/file-server | Path where files will be stored, ideally should be a folder on the same NAS you are storing the DataSets. |
| file-server.download-check          | true                       | Checks that the user is log in into the system to be able to download files.                             |


## Changing the Capability-Role map

openBIS uses a map of capabilities to roles to decide what role is
needed to perform a given action. The defaults can be overridden by
creating a file `etc/capabilities`. One line in this file has one of the
following formats:

```
<Capability>: <Role>[,<ROLE>...]
<Capability>: <Role>[,<ROLE>...][; <Parameter> = <Role>[, <Role>...]][; <Parameter> = <Role>[, <Role>]] ...
<Capability>: <Parameter> = <Role>[, <Role>...][; <Parameter> = <Role>[, <Role>]] ...
```


which sets a new (minimal) role for the given capability. There is a
special role `INSTANCE_DISABLED` which allows to completely disable a
capability for an instance. Note: to set multiple roles for single
capability use multiple lines in the file.

This is the default map:

|Capability                      |Parameter|Default Role                       |Comment                                                                                                                                                                                        |
|--------------------------------|---------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|`WRITE_CUSTOM_COLUMN`             |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`DELETE_CUSTOM_COLUMN`            |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_FILTER`                    |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`DELETE_FILTER`                   |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_DATASET`                   |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_DATASET_PROPERTIES`        |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`DELETE_DATASET`                  |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_EXPERIMENT_SAMPLE`         |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`WRITE_EXPERIMENT_ATTACHMENT`     |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`WRITE_EXPERIMENT_PROPERTIES`     |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`DELETE_EXPERIMENT`               |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_SAMPLE`                    |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`WRITE_SAMPLE_ATTACHMENT`         |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`WRITE_SAMPLE_PROPERTIES`         |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`DELETE_SAMPLE`                   |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`DELETE_SAMPLE_ATTACHMENT`        |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_DATASET`                   |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_DATASET_PROPERTIES`        |         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`DELETE_DATASET`                  |         |`SPACE_POWER_USER`                  |Delete datasets (this capability IS NOT enough to delete datasets with deletion_disallow flag set to true in their type - see `FORCE_DELETE_DATASET`)                                            |
|`FORCE_DELETE_DATASET`            |         |`INSTANCE_DISABLED`                  |Delete datasets (this capability IS enough to delete datasets with deletion_disallow flag set to true in their type - see `DELETE_DATASET`)                                                      |
|`ARCHIVE_DATASET`                 |         |`SPACE_POWER_USER`                  |Move dataset from data store into archive                                                                                                                                                      |
|`UNARCHIVE_DATASET`               |         |`SPACE_USER`                        |Copy back dataset from archive to data store                                                                                                                                                   |
|`LOCK_DATA_SETS`                  |         |`SPACE_ADMIN`                       |Prevent data sets from being archived                                                                                                                                                          |
|`UNLOCK_DATA_SETS`                |         |`SPACE_ADMIN`                       |Release locked data sets                                                                                                                                                                       |
|`WRITE_EXPERIMENT_SAMPLE_MATERIAL`|         |`INSTANCE_ADMIN`                    |Registration / update of experiments, samples and materials in one go                                                                                                                          |
|`REGISTER_SPACE`                  |         |`SPACE_ADMIN`                       |The user will become space admin of the freshly created space                                                                                                                                  |
|`DELETE_SPACE`                    |         |`SPACE_ADMIN`                       |                                                                                                                                                                                               |
|`UPDATE_SPACE`                    |         |`SPACE_ADMIN`                       |                                                                                                                                                                                               |
|`REGISTER_PROJECT`                |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_PROJECT`                   |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_SAMPLE_ATTACHMENT`         |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`DELETE_PROJECT`                  |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_PROJECT_ATTACHMENT`        |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`REGISTER_VOCABULARY`             |         |`INSTANCE_ADMIN`                    |                                                                                                                                                                                               |
|`WRITE_VOCABULARY`                |         |`INSTANCE_ADMIN`                    |                                                                                                                                                                                               |
|`DELETE_VOCABULARY`               |         |`INSTANCE_ADMIN`                    |                                                                                                                                                                                               |
|`WRITE_VOCABULARY_TERM`           |         |`SPACE_POWER_USER`                  |                                                                                                                                                                                               |
|`WRITE_UNOFFICIAL_VOCABULARY_TERM`|         |`SPACE_USER`                        |                                                                                                                                                                                               |
|`PURGE`                           |         |`SPACE_ADMIN`                       |Permanently delete experiments, samples and datasets in the trashcan (this capability IS NOT enough to delete datasets with deletion_disallow flag set to true in their type - see `FORCE_PURGE`)|
|`FORCE_PURGE`                     |         |`INSTANCE_DISABLED`                  |Permanently delete experiments, samples and datasets in the trashcan (this capability IS enough to delete datasets with deletion_disallow flag set to true in their type - see `PURGE`)          |
|`RESTORE`                         |         |`SPACE_USER`                        |Get back experiments, samples and datasets from the trashcan                                                                                                                                   |
|`ASSIGN_EXPERIMENT_TO_PROJECT`    |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ASSIGN_PROJECT_TO_SPACE`         |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ASSIGN_SAMPLE_TO_EXPERIMENT`     |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER`|Re-assign a sample to a new experiment (called in 'register experiment', 'update experiment', 'update sample'')                                                                                |
|`UNASSIGN_SAMPLE_FROM_EXPERIMENT` |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER`|                                                                                                                                                                                               |
|`ASSIGN_SAMPLE_TO_SPACE`          |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |Re-assign a sample to a new space (called in 'update sample')                                                                                                                                  |
|`ASSIGN_DATASET_TO_EXPERIMENT`    |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ASSIGN_DATASET_TO_SAMPLE`        |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`SHARE_SAMPLE`                    |         |`INSTANCE_ADMIN`, `INSTANCE_ETL_SERVER`|                                                                                                                                                                                               |
|`UNSHARE_SAMPLE`                  |         |`INSTANCE_ADMIN`, `INSTANCE_ETL_SERVER`|                                                                                                                                                                                               |
|`ADD_PARENT_TO_SAMPLE`            |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`ADD_PARENT_TO_SAMPLE`            |SAMPLE   |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`ADD_PARENT_TO_SAMPLE`            |PARENT   |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`REMOVE_PARENT_FROM_SAMPLE`       |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`REMOVE_PARENT_FROM_SAMPLE`       |SAMPLE   |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`REMOVE_PARENT_FROM_SAMPLE`       |PARENT   |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`ADD_CONTAINER_TO_SAMPLE`         |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`REMOVE_CONTAINER_FROM_SAMPLE`    |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ADD_PARENT_TO_DATASET`           |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`REMOVE_PARENT_FROM_DATASET`      |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ADD_CONTAINER_TO_DATASET`        |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`REMOVE_CONTAINER_FROM_DATASET`   |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`ASSIGN_ROLE_TO_SPACE_VIA_DSS`    |         |`SPACE_ADMIN`, `INSTANCE_ETL_SERVER`   |                                                                                                                                                                                               |
|`CREATE_SPACES_VIA_DSS`           |         |`SPACE_ADMIN`, `INSTANCE_ETL_SERVER`   |                                                                                                                                                                                               |
|`CREATE_PROJECTS_VIA_DSS`         |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`UPDATE_PROJECTS_VIA_DSS`         |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`CREATE_EXPERIMENTS_VIA_DSS`      |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`UPDATE_EXPERIMENTS_VIA_DSS`      |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`CREATE_SPACE_SAMPLES_VIA_DSS`    |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`UPDATE_SPACE_SAMPLES_VIA_DSS`    |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`CREATE_INSTANCE_SAMPLES_VIA_DSS` |         |`INSTANCE_ETL_SERVER`                |                                                                                                                                                                                               |
|`UPDATE_INSTANCE_SAMPLES_VIA_DSS` |         |`INSTANCE_ETL_SERVER`                |                                                                                                                                                                                               |
|`CREATE_MATERIALS_VIA_DSS`        |         |`INSTANCE_ETL_SERVER`                |                                                                                                                                                                                               |
|`UPDATE_MATERIALS_VIA_DSS`        |         |`INSTANCE_ETL_SERVER`                |                                                                                                                                                                                               |
|`CREATE_DATA_SETS_VIA_DSS`        |         |`SPACE_USER`, `SPACE_ETL_SERVER`       |                                                                                                                                                                                               |
|`UPDATE_DATA_SETS_VIA_DSS`        |         |`SPACE_POWER_USER`, `SPACE_ETL_SERVER` |                                                                                                                                                                                               |
|`SEARCH_ON_BEHALF_OF_USER`        |         |`INSTANCE_OBSERVER`                  |All search or list operations being performed on behalf of another user. Supposed to be used by a service user for server-to-server communication tasks.                                      |

Older versions of openBIS used to allow changing entity relationships to
regular `SPACE_USER`. If you want to get this behavior back, put these
lines into `etc/capabilities`:

```
ASSIGN_EXPERIMENT_TO_PROJECT: SPACE_USER
ASSIGN_EXPERIMENT_TO_PROJECT: SPACE_ETL_SERVER
ASSIGN_SAMPLE_TO_EXPERIMENT: SPACE_USER
ASSIGN_SAMPLE_TO_EXPERIMENT: SPACE_ETL_SERVER
UNASSIGN_SAMPLE_FROM_EXPERIMENT: SPACE_USER
UNASSIGN_SAMPLE_FROM_EXPERIMENT: SPACE_ETL_SERVER
ASSIGN_SAMPLE_TO_SPACE: SPACE_USER
ASSIGN_SAMPLE_TO_SPACE: SPACE_ETL_SERVER
ASSIGN_DATASET_TO_EXPERIMENT: SPACE_USER
ASSIGN_DATASET_TO_EXPERIMENT: SPACE_ETL_SERVER
ASSIGN_DATASET_TO_SAMPLE: SPACE_USER
ASSIGN_DATASET_TO_SAMPLE: SPACE_ETL_SERVER
ADD_PARENT_TO_SAMPLE: SPACE_USER
ADD_PARENT_TO_SAMPLE: SPACE_ETL_SERVER
REMOVE_PARENT_FROM_SAMPLE: SPACE_USER
REMOVE_PARENT_FROM_SAMPLE: SPACE_ETL_SERVER
ADD_CONTAINER_TO_SAMPLE: SPACE_USER
ADD_CONTAINER_TO_SAMPLE: SPACE_ETL_SERVER
REMOVE_CONTAINER_FROM_SAMPLE: SPACE_USER
REMOVE_CONTAINER_FROM_SAMPLE: SPACE_ETL_SERVER
ADD_PARENT_TO_DATASET: SPACE_USER
ADD_PARENT_TO_DATASET: SPACE_ETL_SERVER
REMOVE_PARENT_FROM_DATASET: SPACE_USER
REMOVE_PARENT_FROM_DATASET: SPACE_ETL_SERVER
ADD_CONTAINER_TO_DATASET: SPACE_USER
ADD_CONTAINER_TO_DATASET: SPACE_ETL_SERVER
REMOVE_CONTAINER_FROM_DATASET: SPACE_USER
REMOVE_CONTAINER_FROM_DATASET: SPACE_ETL_SERVER
```


### Capability Role Map for V3 API

| Method of IApplicationServerApi          | Default Roles                                     | Capability                        |
|------------------------------------------|---------------------------------------------------|-----------------------------------|
| archiveDataSets                          | PROJECT_POWER_USER, SPACE_ETL_SERVER              | ARCHIVE_DATASET                   |
| confirmDeletions, forceDeletion == false | PROJECT_ADMIN, SPACE_ETL_SERVER                   | CONFIRM_DELETION                  |
| confirmDeletions, forceDeletion == true  | disabled                                          | CONFIRM_DELETION_FORCED           |
| createAuthorizationGroups                | INSTANCE_ADMIN                                    | CREATE_AUTHORIZATION_GROUP        |
| createCodes                              | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_CODES                      |
| createDataSetTypes                       | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_DATASET_TYPE               |
| createDataSets                           | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_DATASET                    |
| createExperimentTypes                    | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_EXPERIMENT_TYPE            |
| createExperiments                        | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_EXPERIMENT                 |
| createExternalDataManagementSystems      | INSTANCE_ADMIN                                    | CREATE_EXTERNAL_DMS               |
| createMaterialTypes                      | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_MATERIAL_TYPE              |
| createMaterials                          | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_MATERIAL                   |
| createPermIdStrings                      | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_PERM_IDS                   |
| createPersons                            | INSTANCE_ADMIN                                    | CREATE_PERSON                     |
| createPlugins                            | INSTANCE_ADMIN                                    | CREATE_PLUGIN                     |
| createProjects                           | SPACE_POWER_USER, SPACE_ETL_SERVER                | CREATE_PROJECT                    |
| createPropertyTypes                      | INSTANCE_ADMIN                                    | CREATE_PROPERTY_TYPE              |
| createQueries                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | CREATE_QUERY                      |
| createRoleAssignments, instance role     | INSTANCE_ADMIN                                    | CREATE_INSTANCE_ROLE              |
| createRoleAssignments, space role        | SPACE_ADMIN                                       | CREATE_SPACE_ROLE                 |
| createRoleAssignments, project role      | PROJECT_ADMIN                                     | CREATE_PROJECT_ROLE               |
| createSampleTypes                        | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_SAMPLE_TYPE                |
| createSamples                            | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_SAMPLE                     |
| createSemanticAnnotations                | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | CREATE_SEMANTIC_ANNOTATION        |
| createSpaces                             | SPACE_ADMIN, SPACE_ETL_SERVER                     | CREATE_SPACE                      |
| createTags                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | CREATE_TAG                        |
| createVocabularies                       | INSTANCE_ADMIN                                    | CREATE_VOCABULARY                 |
| createVocabularyTerms, official terms    | PROJECT_POWER_USER, SPACE_ETL_SERVER              | CREATE_OFFICIAL_VOCABULARY_TERM   |
| createVocabularyTerms, unofficial terms  | PROJECT_USER, SPACE_ETL_SERVER                    | CREATE_UNOFFICIAL_VOCABULARY_TERM |
| deleteAuthorizationGroups                | INSTANCE_ADMIN                                    | DELETE_AUTHORIZATION_GROUP        |
| deleteDataSetTypes                       | INSTANCE_ADMIN                                    | DELETE_DATASET_TYPE               |
| deleteDataSets                           | PROJECT_POWER_USER, SPACE_ETL_SERVER              | DELETE_DATASET                    |
| deleteExperimentTypes                    | INSTANCE_ADMIN                                    | DELETE_EXPERIMENT_TYPE            |
| deleteExperiments                        | PROJECT_POWER_USER, SPACE_ETL_SERVER              | DELETE_EXPERIMENT                 |
| deleteExternalDataManagementSystems      | INSTANCE_ADMIN                                    | DELETE_EXTERNAL_DMS               |
| deleteMaterialTypes                      | INSTANCE_ADMIN                                    | DELETE_MATERIAL_TYPE              |
| deleteMaterials                          | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | DELETE_MATERIAL                   |
| deleteOperationExecutions                | PROJECT_USER, SPACE_ETL_SERVER                    | DELETE_OPERATION_EXECUTION        |
| deletePlugins                            | INSTANCE_ADMIN                                    | DELETE_PLUGIN                     |
| deleteProjects                           | SPACE_POWER_USER, PROJECT_ADMIN, SPACE_ETL_SERVER | DELETE_PROJECT                    |
| deletePropertyTypes                      | INSTANCE_ADMIN                                    | DELETE_PROPERTY_TYPE              |
| deleteQueries                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | DELETE_QUERY                      |
| deleteRoleAssignments, instance role     | INSTANCE_ADMIN                                    | DELETE_INSTANCE_ROLE              |
| deleteRoleAssignments, space role        | SPACE_ADMIN                                       | DELETE_SPACE_ROLE                 |
| deleteRoleAssignments, project role      | PROJECT_ADMIN                                     | DELETE_PROJECT_ROLE               |
| deleteSampleTypes                        | INSTANCE_ADMIN                                    | DELETE_SAMPLE_TYPE                |
| deleteSamples                            | PROJECT_POWER_USER, SPACE_ETL_SERVER              | DELETE_SAMPLE                     |
| deleteSemanticAnnotations                | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | DELETE_SEMANTIC_ANNOTATION        |
| deleteSpaces                             | SPACE_ADMIN, SPACE_ETL_SERVER                     | DELETE_SPACE                      |
| deleteTags                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | DELETE_TAG                        |
| deleteVocabularies                       | INSTANCE_ADMIN                                    | DELETE_VOCABULARY                 |
| deleteVocabularyTerms                    | PROJECT_POWER_USER, SPACE_ETL_SERVER              | DELETE_VOCABULARY_TERM            |
| executeAggregationService                | PROJECT_OBSERVER                                  | EXECUTE_AGGREGATION_SERVICES      |
| executeCustomASService                   | PROJECT_OBSERVER, SPACE_ETL_SERVER                | EXECUTE_CUSTOM_AS_SERVICE         |
| executeProcessingService                 | PROJECT_USER                                      | EXECUTE_PROCESSING_SERVICES       |
| executeQuery                             | PROJECT_OBSERVER, SPACE_ETL_SERVER                | EXECUTE_QUERY                     |
| executeReportingService                  | PROJECT_OBSERVER                                  | EXECUTE_REPORTING_SERVICES        |
| executeSearchDomainService               | PROJECT_OBSERVER                                  | EXECUTE_SEARCH_DOMAIN_SERVICES    |
| executeSql                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | EXECUTE_QUERY                     |
| getAuthorizationGroups                   | PROJECT_ADMIN                                     | GET_AUTHORIZATION_GROUP           |
| getDataSetTypes                          | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_DATASET_TYPE                  |
| getDataSets                              | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_DATASET                       |
| getExperimentTypes                       | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_EXPERIMENT_TYPE               |
| getExperiments                           | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_EXPERIMENT                    |
| getExternalDataManagementSystems         | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_EXTERNAL_DMS                  |
| getMaterialTypes                         | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_MATERIAL_TYPE                 |
| getMaterials                             | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_MATERIAL                      |
| getOperationExecutions                   | PROJECT_USER, SPACE_ETL_SERVER                    | GET_OPERATION_EXECUTION           |
| getPersons                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_PERSON                        |
| getPlugins                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_PLUGIN                        |
| getProjects                              | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_PROJECT                       |
| getPropertyTypes                         | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_PROPERTY_TYPE                 |
| getQueries                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_QUERY                         |
| getRoleAssignments                       | PROJECT_ADMIN                                     | GET_ROLE_ASSIGNMENT               |
| getSampleTypes                           | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_SAMPLE_TYPE                   |
| getSamples                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_SAMPLE                        |
| getSemanticAnnotations                   | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_SEMANTIC_ANNOTATION           |
| getSessionInformation                    | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_SESSION                       |
| getSpaces                                | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_SPACE                         |
| getTags                                  | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_TAG                           |
| getVocabularies                          | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_VOCABULARY                    |
| getVocabularyTerms                       | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_VOCABULARY_TERM               |
| lockDataSets                             | PROJECT_ADMIN                                     | LOCK_DATASET                      |
| revertDeletions                          | PROJECT_USER, SPACE_ETL_SERVER                    | REVERT_DELETION                   |
| searchAggregationServices                | PROJECT_OBSERVER                                  | SEARCH_AGGREGATION_SERVICES       |
| searchAuthorizationGroups                | PROJECT_ADMIN                                     | SEARCH_AUTHORIZATION_GROUP        |
| searchCustomASServices                   | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_CUSTOM_AS_SERVICES         |
| searchDataSetTypes                       | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_DATASET_TYPE               |
| searchDataSets                           | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_DATASET                    |
| searchDataStores                         | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_DATASTORE                  |
| searchDeletions                          | PROJECT_USER, SPACE_ETL_SERVER                    | SEARCH_DELETION                   |
| searchExperimentTypes                    | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_EXPERIMENT_TYPE            |
| searchExperiments                        | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_EXPERIMENT                 |
| searchExternalDataManagementSystems      | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_EXTERNAL_DMS               |
| searchGlobally                           | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_GLOBALLY                   |
| searchMaterialTypes                      | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_MATERIAL_TYPE              |
| searchMaterials                          | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_MATERIAL                   |
| searchObjectKindModifications            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_OBJECT_KIND_MODIFICATION   |
| searchOperationExecutions                | PROJECT_USER, SPACE_ETL_SERVER                    | GET_OPERATION_EXECUTION           |
| searchPersons                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | GET_PERSON                        |
| searchPlugins                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_PLUGIN                     |
| searchProcessingServices                 | PROJECT_OBSERVER                                  | SEARCH_PROCESSING_SERVICES        |
| searchProjects                           | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_PROJECT                    |
| searchPropertyTypes                      | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_PROPERTY_TYPE              |
| searchQueries                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_QUERY                      |
| searchReportingServices                  | PROJECT_OBSERVER                                  | SEARCH_REPORTING_SERVICES         |
| searchRoleAssignments                    | PROJECT_ADMIN                                     | SEARCH_ROLE_ASSIGNMENT            |
| searchSampleTypes                        | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_SAMPLE_TYPE                |
| searchSamples                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_SAMPLE                     |
| searchSearchDomainServices               | PROJECT_OBSERVER                                  | SEARCH_SEARCH_DOMAIN_SERVICES     |
| searchSemanticAnnotations                | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_SEMANTIC_ANNOTATION        |
| searchSpaces                             | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_SPACE                      |
| searchTags                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_TAG                        |
| searchVocabularies                       | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_VOCABULARY                 |
| searchVocabularyTerms                    | PROJECT_OBSERVER, SPACE_ETL_SERVER                | SEARCH_VOCABULARY_TERM            |
| unarchiveDataSets                        | PROJECT_USER, SPACE_ETL_SERVER                    | UNARCHIVE_DATASET                 |
| unlockDataSets                           | PROJECT_ADMIN                                     | UNLOCK_DATASET                    |
| updateAuthorizationGroups                | INSTANCE_ADMIN                                    | UPDATE_AUTHORIZATION_GROUP        |
| updateDataSetTypes                       | INSTANCE_ADMIN                                    | UPDATE_DATASET_TYPE               |
| updateDataSets                           | PROJECT_POWER_USER, SPACE_ETL_SERVER              | UPDATE_DATASET                    |
| updateDataSets, properties               | PROJECT_POWER_USER, SPACE_ETL_SERVER              | UPDATE_DATASET_PROPERTY           |
| updateExperimentTypes                    | INSTANCE_ADMIN                                    | UPDATE_EXPERIMENT_TYPE            |
| updateExperiments                        | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_EXPERIMENT                 |
| updateExperiments, attachments           | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_EXPERIMENT_ATTACHMENT      |
| updateExperiments, properties            | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_EXPERIMENT_PROPERTY        |
| updateExternalDataManagementSystems      | INSTANCE_ADMIN                                    | UPDATE_EXTERNAL_DMS               |
| updateMaterialTypes                      | INSTANCE_ADMIN                                    | UPDATE_MATERIAL_TYPE              |
| updateMaterials                          | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | UPDATE_MATERIAL                   |
| updateMaterials, properties              | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | UPDATE_MATERIAL_PROPERTY          |
| updateOperationExecutions                | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_OPERATION_EXECUTION        |
| updatePersons, activate                  | INSTANCE_ADMIN                                    | ACTIVATE_PERSON                   |
| updatePersons, deactivate                | INSTANCE_ADMIN                                    | DEACTIVATE_PERSON                 |
| updatePersons, set home space            | SPACE_ADMIN                                       | UPDATE_HOME_SPACE                 |
| updatePlugins                            | INSTANCE_ADMIN                                    | UPDATE_PLUGIN                     |
| updateProjects                           | SPACE_POWER_USER, PROJECT_ADMIN, SPACE_ETL_SERVER | UPDATE_PROJECT                    |
| updateProjects, attachments              | SPACE_POWER_USER, PROJECT_ADMIN, SPACE_ETL_SERVER | UPDATE_PROJECT_ATTACHMENT         |
| updatePropertyTypes                      | INSTANCE_ADMIN                                    | UPDATE_PROPERTY_TYPE              |
| updateQueries                            | PROJECT_OBSERVER, SPACE_ETL_SERVER                | UPDATE_QUERY                      |
| updateSampleTypes                        | INSTANCE_ADMIN                                    | UPDATE_SAMPLE_TYPE                |
| updateSamples                            | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_SAMPLE                     |
| updateSamples, attachments               | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_SAMPLE_ATTACHMENT          |
| updateSamples, properties                | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_SAMPLE_PROPERTY            |
| updateSemanticAnnotations                | INSTANCE_ADMIN, INSTANCE_ETL_SERVER               | UPDATE_SEMANTIC_ANNOTATION        |
| updateSpaces                             | SPACE_ADMIN, SPACE_ETL_SERVER                     | UPDATE_SPACE                      |
| updateTags                               | PROJECT_OBSERVER, SPACE_ETL_SERVER                | UPDATE_TAG                        |
| updateVocabularies                       | INSTANCE_ADMIN                                    | UPDATE_VOCABULARY                 |
| updateVocabularyTerms, official terms    | PROJECT_POWER_USER, SPACE_ETL_SERVER              | UPDATE_OFFICIAL_VOCABULARY_TERM   |
| updateVocabularyTerms, unofficial terms  | PROJECT_USER, SPACE_ETL_SERVER                    | UPDATE_UNOFFICIAL_VOCABULARY_TERM |