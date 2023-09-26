Maintenance Tasks
=================

## Maintenance Task Classification

| Category                                 |
|------------------------------------------|
| Feature                                  |
| Consistency and other Reports            |
| Consistency Repair and Manual Migrations |

| Relevancy  |
|------------|
| Default    |
| Relevant   |
| Rare       |
| Deprecated |

## Introduction

A maintenance task is a process which runs once or in regular time intervals. It is defined by a [core plugin](../../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins) of type `maintenance-tasks`. Usually a maintenance task can only run on AS or DSS but not in both environments.

The following properties are common for all maintenance tasks:

|Property Key|Description|
|--- |--- |
|class|The fully-qualified Java class name of the maintenance task. The class has to implement IMaintenanceTask.|
|execute-only-once|A flag which has to be set to true if the task should be executed only once. Default value: false|
|interval|A time interval (in seconds) which defines the pace of execution of the maintenance task. Can be specified with one of the following time units: ms, msec, s, sec, m, min, h, hours, d, days. Default time unit is sec. Default value: one day.|
|start|A time at which the task should be executed the first time. Format: HH:mm. where HH is a two-digit hour (in 24h notation) and mm is a two-digit minute. By default the task is execute at server startup.|
|run-schedule|Scheduling plan for task execution. Properties execute-only-once, interval, and start will be ignored if specified.<br /><br />**Crontab syntax:** <br /><br />`cron: <second> <minute> <hour> <day> <month> <weekday>`<br /><br />Examples:<br /><br />`cron: 0 0 * * * *`: the top of every hour of every day.<br /><br />`cron: */10 * * * * *`: every ten seconds.<br /><br />`cron: 0 0 8-10 * * *`: 8, 9 and 10 o'clock of every day.<br /><br />`cron: 0 0 6,19 * * *`: 6:00 AM and 7:00 PM every day.<br /><br />`cron: 0 0/30 8-10 * * *`: 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.<br /><br />`cron: 0 0 9-17 * * MON-FRI`: on the hour nine-to-five weekdays.<br /><br />`cron: 0 0 0 25 12 ?`: every Christmas Day at midnight.<br /><br />**Non-crontab syntax:** <br /><br />Comma-separated list of definitions with following syntax:<br /><br />`[[<counter>.]<week day>] [<month day>[.<month>]] <hour>[:<minute>]` <br /><br />where `<counter>` counts the specified week day of the month. `<week day>` is `MO`, `MON`, `TU`, `TUE`, `WE`, `WED`, `TH`, `THU`, `FR`, `FRI`, `SA`, `SAT`, `SU`, or `SUN` (ignoring case). `<month>` is either the month number (followed by an optionl '.') or `JAN`, `FEB`, `MAR`, `APR`, `MAY`, `JUN`, `JUL`, `AUG`, `SEP`, `OCT`, `NOV`, or `DEC` (ignoring case). <br /><br />Examples: <br /><br />`6, 18`: every day at 6 AM and 6 PM. <br /><br />`3.FR 22:15`: every third friday of a month at 22:15. <br /><br />`1. 15:50`: every first day of a month at 3:50 PM. <br /><br />`SAT 1:30`: every saturday at 1:30 AM. <br /><br />`1.Jan 5:15, 1.4. 5:15, 1.7 5:15, 1. OCT 5:15`: every first day of a quarter at 5:15 AM.|
|run-schedule-file|File where the timestamp for next execution is stored. It is used if run-schedule is specified. Default: `<installation folder>/<plugin name>_<class name>` |
|retry-intervals-after-failure|Optional comma-separated list of time intervals (format as for interval) after which a failed execution will be retried. Note, that a maintenance task will be execute always when the next scheduled timepoint occurs. This feature allows to execute a task much earlier in case of temporary errors (e.g. temporary unavailibity of another server).|

## Feature

### ArchivingByRequestTask

**Environment**: AS

**Relevancy:** Relevant

**Description**: Triggers archiving for data sets where the 'requested
archiving' flag is set. Waits with archiving until enough data sets for
a group come together. This is necessary for taped-base archiving where
the files to be stored have to be larger than a minimum size.

**Configuration**:

|Property Key|Description|
|--- |--- |
|keep-in-store|If true the archived data set will not be removed from the store. That is, only a backup will be created. Default: false|
|minimum-container-size-in-bytes|Minimum size of an archive container which has one or more data set. This is important for Multi Data Set Archiving. Default: 10 GB|
|maximum-container-size-in-bytes|Maximum size of an archive container which has one or more data set. This is important for Multi Data Set Archiving. Default: 80 GB|
|configuration-file-path|Path to the configuration file as used by User Group Management. Here only the group keys are needed. They define a set of groups. If there is no configuration file at the specified path this set is empty.A data set requested for archiving belongs the a specified group if its space starts with the group key followed by an underscore character '_'. Otherwise it belongs to no group. This maintenance task triggers archiving an archive container with one or more data set from the same group if the container fits the specified minimum and maximum size. Note, that data sets which do not belong to a group are handled as a group too. If a data set is larger than the maximum container size it will be archived even though the container is to large. The group key (in lower case) is provided to the archiver. The Multi Data Set Archiver will use this for storing the archive container in a sub folder of the same name.<br /><br /><br />Default: `etc/user-management-maintenance-config.json` |


**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.ArchivingByRequestTask
interval = 1 d
minimum-container-size-in-bytes =  20000000000
maximum-container-size-in-bytes = 200000000000
configuration-file-path = ../../../data/groups.json
```


**Notes:**  In practice every instance using multi dataset archiving
feature and also the ELN-LIMS should have this enabled.

### AutoArchiverTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Triggers archiving of data sets that have not been
archived yet.

**Configuration**:

|Property Key|Description|
|--- |--- |
|remove-datasets-from-store|If true the archived data set will be removed from the store. Default: false|
|data-set-type|Data set type of the data sets to be archived. If undefined all data set of all types might be archived.|
|older-than|Minimum number of days a data set to be archived hasn't been accessed. Default: 30|
|archive-candidate-discoverer.class|Discoverer of candidates to be archived:<ul><li>`ch.systemsx.cisd.etlserver.plugins.AgeArchiveCandidateDiscoverer`: All data sets with an access time stamp older than specified by property older-than are candidates. This is the default discoverer.</li><li>`ch.systemsx.cisd.etlserver.plugins.TagArchiveCandidateDiscoverer`: All data sets which are marked by one of the tags specified by the property `archive-candidate-discoverer.tags` are candidates.</li></ul> |
|policy.class|A policy specifies which data set candidates should be archived. If undefined all candidates will be archived. Has to be a fully-qualified name of a Java class implementing ch.systemsx.cisd.etlserver.IAutoArchiverPolicy.|
|policy.*|Properties specific for the policy specified by `policy.class`. More about policies can be found here.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.AutoArchiverTask
interval = 10 days
archive-candidate-discoverer.class = ch.systemsx.cisd.etlserver.plugins.TagArchiveCandidateDiscoverer
archive-candidate-discoverer.tags = /admin-user/archive
policy.class = ch.systemsx.cisd.etlserver.plugins.GroupingPolicy
policy.minimal-archive-size = 1500000
policy.maximal-archive-size = 3000000
policy.grouping-keys = Space#DataSetType, Space#Experiment:merge
```


### BlastDatabaseCreationMaintenanceTask 

**Environment**: DSS

**Relevancy:** Default (ELN-LIMS)

**Description**: Creates BLAST databases from FASTA and FASTQ files of
data sets and/or properties of experiments, samples, and data sets.

The title of all entries of the FASTA and FASTQ files will be extended
by the string `[Data set: <data set code>, File: <path>]`. Sequences
provide by an entity property will have identifiers of the form
`<entity kind>+<perm id>+<property type>+<time stamp>`. This allows to
determine where the matching sequences are stored in openBIS. A sequence
can be a nucleic acid sequence or an amino acid sequence.

For each data set a BLAST nucl and prot databases will be created (if
not empty) by the tool `makeblastdb`. For all entities of a specified
kind and type one BLAST database (one for nucleic sequences and one
for amino acid sequences) will be created from the plain sequences
stored in the specified property (white spaces will be removed). In
addition an index is created by the tool `makembindex` if the sequence
file of the database (file type `.nsq`) is larger than 1MB. The name of
the databases are `<data set code>-nucl/prot`
and `<entity kind>+<entity type code>+<property type code>+<time stamp>-nucl/prot`.
These databases are referred in the virtual database `all-nucl` (file:
`all-nucl.nal`) and `all-prot` (file: `all-prot.pal`).

If a data set is deleted the corresponding BLAST nucl and prot databases
will be automatically removed the next time this maintenance task runs.
If an entity of specified type has been modified the BLAST databases
will be recalculated the next time this maintenance task runs.

Works only if BLAST+ tool suite has been installed. BLAST+ can be
downloaded from
<ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/>

**Notes:**  It comes pre-configured with the ELN-LIMS but if additional
properties need to scanned they should be added to the plugin.properties

  

**Configuration**:

|Property Key|Description|
|--- |--- |
|dataset-types|Comma-separated list of regular expressions of data set types. All FASTA and FASTQ files from those data sets are handled. All data sets of types not matching at least one of the regular expression are not handled.|
|entity-sequence-properties|Comma-separated list of descriptions of entity properties with sequences. A description is of the form<br /><br />`<entity kind>+<entity type code>+<property type code>`<br /><br />where `<entity kind>` is either `EXPERIMENT`, `SAMPLE` or `DATA_SET` (Materials are not supported).|
|file-types|Space separated list of file types. Data set files of those file types have to be FASTA or FASTQ files. Default: `.fasta` `.fa` `.fsa` `.fastq`|
|blast-tools-directory|Path in the file system where all BLAST tools are located. If it is not specified or empty the tools directory has to be in the PATH environment variable.|
|blast-databases-folder|Path to the folder where all BLAST databases are stored. Default: `<data store root>/blast-databases`|
|blast-temp-folder|Path to the folder where temporary FASTA files are stored.  Default: `<blast-databases-folder>/tmp` |
|last-seen-data-set-file|Path to the file which stores the id of the last seen data set. Default: `<data store root>/last-seen-data-set-for-BLAST-database-creation` |


**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask
interval = 1 h
dataset-types = BLAST-.+
entity-sequence-properties = SAMPLE+OLIGO+SEQUENCE, EXPERIMENT+YEAST+PLASMID_SEQUENCE
blast-tools-directory = /usr/local/ncbi/blast/bin
```


### DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask 

**Environment**: DSS

**Relevancy:** Default

**Description**: Deletes data sets which have been deleted on AS.

```{note}
If this task isn't configured neither in service.properties nor as a core plugin it will be established automatically by using default configuration and running every 5 minutes.
```

**Configuration**:

|Property Key|Description|
|--- |--- |
|last-seen-data-set-file|Path to a file which will store the code of the last data set handled. Default: <br />`deleteDatasetsAlreadyDeletedFromApplicationServerTaskLastSeen` |
|timing-parameters.max-retries|Maximum number of retries in case of currently not available filesystem of the share containing the data set. Default:11|
|timing-parameters.failure-interval|Waiting time (in seconds) between retries. Default: 10|
|chunk-size|Number of data sets deleted together. The task is split into deletion tasks with maximum number of data sets. Default: No chunk size. That is, all data sets to be deleted are deleted in one go.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask
interval = 60
last-seen-data-set-file = lastSeenDataSetForDeletion.txt
```


### DeleteFromArchiveMaintenanceTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Deletes archived data sets which have been deleted on
AS. This tasks needs the archive plugin to be configured in
`service.properties. This task only works with non multi data set archivers.`

**Configuration**:

| Property Key    | Description                                                                                 |
|-----------------|---------------------------------------------------------------------------------------------|
| status-filename | Path to a file which will store the technical ID of the last data set deletion event on AS. |
| chunk-size      | Maximum number of entries deleted in one maintenance task run. Default: Unlimited           |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.DeleteFromArchiveMaintenanceTask
interval = 3600
status-filename = ../archive-cleanup-status.txt
```


### DeleteFromExternalDBMaintenanceTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Deletes database entries which are related to data sets
deleted in AS. The database is can be any relational database accessible
by DSS.

**Configuration**:

|Property Key|Description|
|--- |--- |
|data-source|Key of a data source configured in `service.properties` or in a core plugin of type 'data-sources'. A data source defines the credentials to access the database.|
|synchronization-table|Name of the table which stores the technical ID of the last data set deletion event on AS. This is ID is used to ask AS for all new data set deletion events. Default value: `EVENTS` |
|last-seen-event-id-column|Name of the column in the database table defined by property `synchronization-table` which stores the ID of the last data set deletion event. Default value: `LAST_SEEN_DELETION_EVENT_ID` |
|data-set-table-name|Comma-separated list of table names which contain stuff related to data sets to be deleted. In case of cascading deletion only the tables at the beginning of the cascade should be mentioned. Default value: `image_data_sets`, `analysis_data_sets`.|
|data-set-perm-id|Name of the column in all tables defined by `data-set-table-name` which stores the data set code. Default value: `PERM_ID`|
|chunk-size|Maximum number of entries deleted in one maintenance task run. Default: Unlimited|


**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.DeleteFromExternalDBMaintenanceTask
interval = 300
data-source = proteomics-db
data-set-table-name = data_sets
```
 

### EventsSearchMaintenanceTask

**Environment**: AS

**Relevancy:** Default

**Description**: Populates EVENTS\_SEARCH database table basing on
entries from EVENTS database table. EVENTS\_SEARCH table contains the
same information as EVENTS table but in a more search friendly format
(e.g. a single entry in EVENTS table may represent a deletion of
multiple objects deleted at the same time, in EVENT\_SEARCH table such
entry is split into separate entries - one for each deleted object.).
This is set up automatically.

**Configuration:**

There are no specific configuration parameters for this task.

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.events_search.EventsSearchMaintenanceTask
interval = 1 day
```


### ExperimentBasedArchivingTask 

**Environment**: DSS

**Relevancy:** rare, used when no MultiDataSetArchiver is used and
AutoArchiverTask is too complex.


**Description**: Archives all data sets of experiments which fulfill
some criteria. This tasks needs the archive plugin to be configured in
`service.properties`.

**Configuration**:

| Property Key                                        | Description                                                                                                                                                                                                                                                                                                                  |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| excluded-data-set-types                             | Comma-separated list of data set types. Data sets of such types are not archived. Default: No data set type is excluded.                                                                                                                                                                                                     |
| estimated-data-set-size-in-KB.<data set type> | Specifies for the data set type <data set type> the average size in KB. If <data set type> is DEFAULT it will be used for all data set types with unspecified estimated size.                                                                                                                                    |
| free-space-provider.class                           | Fully qualified class name of the free space provider (implementing `ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider`). Depending on the free space provider additional properties, all starting with prefix `free-space-provider`.,  might be needed. Default: `ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider` |
| monitored-dir                                       | Path to the directory to be monitored by the free space provider.                                                                                                                                                                                                                                                            |
| minimum-free-space-in-MB                            | Minimum free space in MB. If the free space is below this limit the task archives data sets. Default: 1 GB                                                                                                                                                                                                                   |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.ExperimentBasedArchivingTask
interval = 86400
minimum-free-space-in-MB = 2048
monitored-dir = /my-data/
estimated-data-set-size-in-KB.RAW_DATA = 12000
estimated-data-set-size-in-KB.DEFAULT = 35000
```

     

If there is not enough free space the task archives all data sets
experiment by experiment until free space is above the specified limit.
The oldest experiments are archived first. The age of an experiment is
determined by the youngest modification/registration time stamp of all
its data sets which are not excluded by data set type or archiving
status.

The free space is only calculated once when the task starts to figure
out whether archiving is necessary or not. This value is than used
together with estimated data set sizes to get an estimated free space
which is used for the stopping criteria. Why not calculating the free
space again with the free space provider after the data sets of an
experiment have been archived? The reason is that providing the free
space might be an expensive operation. This is the case when archiving
means removing data from a database which have been fed by data from
data sets of certain type. In this case archiving (i.e. deleting) those
data in the database do not automatically frees disk space because
freeing disk space is for databases often an expensive operation.

The DSS admin will be informed by an e-mail about which experiments have
been archived.

### HierarchicalStorageUpdater

**Environment**: DSS

**Description**: Creates/updates a mirrot of the data store. Data set
are organized hierachical in accordance to their experiment and samples

**Relevancy:** Deprecated

**Configuration**:

|Property Key|Description|
|--- |--- |
|storeroot-dir-link-path|Path to the root directory of the store as to be used for creating symbolic links. This should be used if the path to the store as seen by clients is different than seen by DSS.|
|storeroot-dir|Path to the root directory of the store. Used if storeroot-dir-link-path is not specified.|
|hierarchy-root-dir|Path to the root directory of mirrored store.|
|link-naming-strategy.class|Fully qualified class name of the strategy to generate the hierarchy (implementing `ch.systemsx.cisd.etlserver.plugins.IHierarchicalStorageLinkNamingStrategy`). Depending on the actual strategy additional properties, all starting with prefix `link-naming-strategy`.,  mighty be needed. Default: `ch.systemsx.cisd.etlserver.plugins.TemplateBasedLinkNamingStrategy` |
|link-source-subpath.<data set type>|Link source subpath for the specified data set type. Only files and folder in this relative path inside a data set will be mirrored. Default: The complete data set folder will be mirroed.|
|link-from-first-child.<data set type>|Flag which specifies whether only the first child of or the complete folder (either the data set or the one specified by link-source-subpath.<data set type>). Default: False|
|with-meta-data|Flag, which specifies whether directories with meta-data.tsv and a link should be created or only links. The default behavior is to create links-only. Default: false|
|link-naming-strategy.template|The exact form of link paths produced by TemplateBasedLinkNamingStrategy is defined by this template.<br /><br />The variables `dataSet`, `dataSetType`, `sample`, `experiment`, project and space will be recognized and replaced in the actual link path.<br /><br />Default: `${space}`/`${project}`/`${experiment}`/`${dataSetType}+${sample}+${dataSet}` |
|link-naming-strategy.component-template|If defined, specifies the form of link paths for component datasets. If undefined, component datasets links are formatted with `link-naming-strategy.template`.<br /><br />Works as `link-naming-strategy.template`, but has these additional variables: `containerDataSetType`, `containerDataSet`, `containerSample.<br /><br />Default: Undefined.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.HierarchicalStorageUpdater
storeroot-dir = ${root-dir}
hierarchy-root-dir = ../../mirror
link-naming-strategy.template = ${space}/${project}/${experiment}/${sample}/${dataSetType}-${dataSet}
link-naming-strategy.component-template = ${space}/${project}/${experiment}/${containerSample}/${containerDataSetType}-${containerDataSet}/${dataSetType}-${dataSet} 
```


### MultiDataSetDeletionMaintenanceTask 

**Environment**: DSS

**Relevancy:** Relevant

**Description**: Deletes data sets which are already deleted on AS also
from multi-data-set archives. This maintenance task works only if the
[Multi Data Set Archiver](../../uncategorized/multi-data-set-archiving.md)  is
configured. It does the following:

1.  Extracts the not-deleted data sets of a TAR container with deleted
    data sets into the store.
2.  Marks them as *not present in archive*.
3.  Deletes the TAR containers with deleted data sets.
4.  Requests archiving of the non-deleted data sets.

The last step requires that the maintenance task
[ArchivingByRequestTask](./maintenance-tasks.md#archivingbyrequesttask) is configured.

**Configuration**:

| Property Key            | Description                                                                                                                                                                                                                                                                                               |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| last-seen-event-id-file | File which contains the last seen event id.                                                                                                                                                                                                                                                               |
| mapping-file            | Optional file which maps data sets to share ids and archiving folders (for details see Mapping File for Share Ids and Archiving Folders). If not specified the first share which has enough free space and which isn't a unarchiving scratch share will be used for extracting the not-deleted data sets. |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetDeletionMaintenanceTask
interval = 1 d
last-seen-event-id-file = ${storeroot-dir}/MultiDataSetDeletionMaintenanceTask-last-seen-event-id.txt
mapping-file = etc/mapping.tsv 
```


**NOTE**: Should be configured on any instance using the multi dataset
archiver when the archive data should be deletable.

### MultiDataSetUnarchivingMaintenanceTask

**Environment**: DSS

**Relevancy:** Relevant

**Description**: Triggers unarchiving of multi data set archives. Is
only needed if the configuration property `delay-unarchiving` of the
[Multi Data Set Archiver](../../uncategorized/multi-data-set-archiving.md) is
set `true`.

This maintenance task allows to reduce the stress of the tape system by
otherwise random unarchiving events triggered by the users.

**Configuration**: No specific properties.

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetUnarchivingMaintenanceTask
interval = 1 d
start = 01:00  
```


### MultiDataSetArchiveSanityCheckMaintenanceTask

**Environment**: DSS

**Relevancy:** Default

**Description**: Task that verifies checksums of data sets archived
within a specific time window. It reads archives from the final
destination and checks if they are consistent with path info database
entries.

```{warning}
The task assumes MultiDataSetArchiver task is configured (the
task uses some of the multi data set archiver configuration properties
e.g. final destination location).
```

**Configuration**:

| Property Key    | Description                                                                   |
|-----------------|-------------------------------------------------------------------------------|
| status-file     | Path to a JSON file that keeps a list of already checked archive containers   |
| notify-emails   | List of emails to notify about problematic archive containers                 |
| interval        | Interval in seconds                                                           |
| check-to-date   | "To date" of the time window to be checked. Date in format yyyy-MM-dd HH:mm   |
| check-from-date | "From date" of the time window to be checked. Date in format yyyy-MM-dd HH:mm |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiveSanityCheckMaintenanceTask
interval = 3600
check-from-date = 2022-09-01 00:00
check-to-date = 2022-10-01 00:00
notify-emails = test1@email.com, test2@email.com
status-file = ../../multi-dataset-sanity-check-statuses.json
```


### PathInfoDatabaseFeedingTask 

**Environment**: DSS

**Relevancy:** Default, is part of the post registration task

**Description**: Feeds the pathinfo database with file paths of all data
sets in the store. It can be used as a maintenance task as well as a
post registration task. As a maintenance task it is needed to run only
once if a **PostRegistrationMaintenanceTask** is configured. This task
assumes a data source with for 'path-info-db'.

If used as a maintenance task the data sets are processed in the order
they are registered. The registration time stamp of the last processed
data set is the starting point when the task is executed next time.

**Configuration**:

|Property Key|Description|
|--- |--- |
|compute-checksum|If `true` the CRC32 checksum (and optionally a checksum of the type specified by `checksum-type`) of all files will be calculated and stored in pathinfo database. Default value: `false` |
|checksum-type|Optional checksum type. If specified and `compute-checksum = true` two checksums are calculated: CRC32 checksum and the checksum of specified type. The type and the checksum are stored in the pathinfo database. An allowed type has to be supported by `MessageDigest.getInstance(<checksum type>)`. For more details see [Oracle docs](http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html#getInstance-java.lang.String-).|
|data-set-chunk-size|Number of data sets requested from AS in one chunk if it is used as a maintenance task. Default: 1000|
|max-number-of-chunks|Maximum number of chunks of size data-set-chunk-size are processed if it is used as a maintenance task. If it is <= 0 and `time-limit` isn't defined all data sets are processed. Default: 0|
|time-limit|Limit of execution time of this task if it is used as a maintenance task. The task is stopped before reading next chunk if the time has been used up. If it is specified it is an alternative way to limit the number of data sets to be processed instead of specifying  `max-number-of-chunks`. This parameter can be specified with one of the following time units: `ms`, `msec`, `s`, `sec`, `m`, `min`, `h`, `hours`, `d`, `days`. Default time unit is `sec`.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.path.PathInfoDatabaseFeedingTask
execute-only-once = true
compute-checksum = true 
```
 

### PostRegistrationMaintenanceTask

**Environment**: DSS

**Relevancy:** Default

**Description**: A tasks which runs a sequence of so-called
post-registration tasks for each freshly registered data set.

**Configuration**:

| Property Key                 | Description                                                                                                                                                                                                                                    |
|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ignore-data-sets-before-date | Defines a registration date. All data sets registered before this date are ignored. Format: `yyyy-MM-dd`, where `yyyy` is a four-digit year, `MM` is a two-digit month, and `dd` is a two-digit day. Default value: no restriction.                    |
| last-seen-data-set-file      | Path to a file which stores the code of the last data set successfully post-registered. Default value: `last-seen-data-set.txt`                                                                                                                  |
| cleanup-tasks-folder         | Path to a folder which stores serialized clean-up tasks always created before a post-registration task is executed. These clean-up tasks are executed on start up of DSS after a server crash. Default value: `clean-up-tasks`                   |
| post-registration-tasks      | Comma-separated list of keys of post-registration task configuration. Each key defines (together with a '.') the prefix of all property keys defining the post-registration task. They are executed in the order their key appear in the list. |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.postregistration.PostRegistrationMaintenanceTask
interval = 60
cleanup-tasks-folder = ../cleanup-tasks
ignore-data-sets-before-date = 2011-01-27
last-seen-data-set-file = ../last-seen-data-set
post-registration-tasks = eager-shuffling, eager-archiving
eager-shuffling.class = ch.systemsx.cisd.etlserver.postregistration.EagerShufflingTask
eager-shuffling.share-finder.class = ch.systemsx.cisd.openbis.dss.generic.shared.ExperimentBasedShareFinder
eager-archiving.class = ch.systemsx.cisd.etlserver.postregistration.ArchivingPostRegistrationTask
```


### RevokeUserAccessMaintenanceTask

**Environment**: AS

**Relevancy:** Relevant

**Description**: Check if the users are available on the configured
authentication services, if they are not available, are automatically
disabled and their id renamed with the disable date.

For this to work the services should be able to list the available
users. If you use any service that doesn't allow it, the task
automatically disables itself because is impossible to know if the users
are active or not.

| Service                    | Compatible |
|----------------------------|------------|
| CrowdAuthenticationService | NO         |
| DummyAuthenticationService | NO         |
| NullAuthenticationService  | NO         |
| FileAuthenticationService  | YES        |
| LDAPAuthenticationService  | YES        |

**Configuration**:

This maintenance task automatically uses the services already configured
on the server.

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.RevokeUserAccessMaintenanceTask
interval = 60 s
```


### UserManagementMaintenanceTask

**Environment**: AS

**Relevancy:** Relevant

**Description**: Creates users, spaces, samples, projects and
experiments for all members of an LDAP authorization group or an
explicit list of user ids. A configuration file (in JSON format) will be
read each time this task is executed. All actions are logged in an audit
log file. For more details see [User Group Management for Multi-groups openBIS Instances](../../uncategorized/user-group-management-for-multi-groups-openbis-instances.md)

**Configuration:**

| Property Key              | Description                                                                                                                                                                          |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| configuration-file-path   | Relative or absolute path to the configuration file. Default: `etc/user-management-maintenance-config.json`                                                                            |
| audit-log-file-path       | Relative or absolute path to the audit log file. Default: `logs/user-management-audit_log.txt`                                                                                         |
| shares-mapping-file-path  | Relative or absolute path to the mapping file for data store shares. This is optional. If not specified the mapping file will not be managed by this maintenance task.               |
| filter-key                | Key which is used to filter LDAP results. Will be ignored if `ldap-group-query-template` is specified. Default value: `ou`                                                              |
| ldap-group-query-template | Direct LDAP query template. It should have '%' character which will be replaced by an LDAP key as specified in the configuration file.                                               |
| deactivate-unknown-users  | If `true` a user unknown by the authentication service will be deactivated. It should be set to `false` if no authenication service can be asked (like in Single-Sign-On). Default: `true` |

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.UserManagementMaintenanceTask
start = 02:42
interval = 1 day
```


## Consistency and other Reports

### DataSetArchiverOrphanFinderTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Finds archived data sets which are no longer in openBIS
(at least not marked as present-in-archive). A report will be created
and sent to the specified list of e-mail addresses (mandatory
property `email-addresses`). The task also looks for data sets which are
present-in-archive but actually not found in the archive.

This orphan finder task only works for Multi Data Set Archiver. It
doesn't work for RsyncArchiver, TarArchiver or ZipArchiver.

**Configuration**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.DataSetArchiverOrphanFinderTask
interval = 60 s
email-addresses = email1@bsse.ethz.ch, email2@bsse.ethz.ch
```


**Notes:** This is a consistency check task. It checks consistency for
datasets with the flag present-in-archive.

### DataSetAndPathInfoDBConsistencyCheckTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Checks that the file information in pathinfo database
is consistent with the information the file system provides. This is
done for all recently registered data sets. Note, archived data sets are
skipped. After all data sets (in the specified checking time interval)
have been checked the task checks them again.

**Configuration**:

|Property Key|Description|
|--- |--- |
|checking-time-interval|Time interval in the past which defines the range of data sets to be checked. That is, all data sets with registration date between now minus checking-time-interval and now will be checked. Can be specified with one of the following time units: `ms`, `msec`, `s`, `sec`, `m`, `min`, `h`, `hours`, `d`, `days`. Default time unit is `sec`. Default value: one day.|
|pausing-time-point|Optional time point. Format: `HH:mm`. where `HH` is a two-digit hour (in 24h notation) and `mm` is a two-digit minute.<br /><br />When specified this task stops checking after the specified pausing time point and continues when executed the next time or the next day if start or `continuing-time-point` is specified.<br /><br />After all data sets have been checked the task checks again all data sets started by the oldest one specified by `checking-time-interval`.|
|continuing-time-point|Time point where checking continous. Format: `HH:mm`. where `HH` is a two-digit hour (in 24h notation) and `mm` is a two-digit minute. Ignored when `pausing-time-point` isn't specified. Default value: Time when the task is executed.|
|chunk-size|Maximum number of data sets retrieved from AS. Ignored when `pausing-time-point` isn't specified. Default value: 1000|
|state-file|File to store registration time stamp and code of last considered data set. This is only used when pausing-time-point has been specified. Default: `<store root>/DataSetAndPathInfoDBConsistencyCheckTask-state.txt` |


**Example**: The following example checks all data sets of the last ten
years. It does the check only during the night and continues next night.

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.path.DataSetAndPathInfoDBConsistencyCheckTask
interval = 1 days
start = 23:15
pausing-time-point = 5:00
checking-time-interval = 3653 days
```


### MaterialExternalDBSyncTask

**Environment**: AS

**Relevancy:** Deprecated

**Description**: Feeds a report database with recently added or modified
materials.

**Configuration**:

|Property Key|Description|
|--- |--- |
|database-driver|Fully qualified name of the JDBC driver class.|
|database-url|URL to access the database server.|
|database-username|User name of the database. Default: User who started openBIS AS.|
|database-password|Optional password of the database user.|
|mapping-file|Path to the file containing configuration information of mapping material types and material properties to tables and columns in the report database.|
|read-timestamp-sql|The SQL select statement which returns one column of type time stamp for the time stamp of the last report. If the result set is empty the time stamp is assumed to be 1970-01-01. If the result set has more than one row the first row is used.|
|update-timestamp-sql|The SQL statement which updates or adds a time stamp. The statement has to contain a '?' symbol as the placeholder of the actual time stamp.|
|insert-timestamp-sql|The SQL statement to add a time stamp the first time. The statement has to contain a '?' symbol as the placeholder of the actual time stamp. Default: same as `update-timestamp-sql`.|

**Example**:

**service.properties of AS**

```
<task id>.class = ch.systemsx.cisd.openbis.generic.server.task.MaterialExternalDBSyncTask
<task id>.interval = 120
<task id>.read-timestamp-sql = select timestamp from timestamp
<task id>.update-timestamp-sql = update timestamp set timestamp = ?
<task id>.insert-timestamp-sql = insert into timestamp values(?)
<task id>.mapping-file = ../report-mapping.txt
<task id>.database-driver = org.postgresql.Driver
<task id>.database-url = jdbc:postgresql://localhost/material_reporting
```


#### Mapping File

The mapping file is a text file describing the mapping of the data (i.e.
material codes and material properties) onto the report database. It
makes several assumptions on the database schema:

-   One table per material type. There are only table of materials to be
    reported.
-   Each table has a column which contains the material code.  
    -   The entries are unique.
    -   The material code is a string not longer than 60 characters.
-   Each table has one column for each property type. Again, there are
    only column for properties to be reported.
-   The data type of the column should match the data type of the
    properties:
    -   MATERIAL:  only the material code (string) will be reported.
        Maximum length: 60
    -   CONTROLLEDVOCABULARY: the label (if defined) or the code will be
        reported. Maximum length: 128
    -   TIMESTAMP: timestamp
    -   INTEGER: integer of any number of bits (maximum 64).
    -   REAL: fixed or floating point number 
    -   all other data types are mapped to text.


The general format of the mapping file is as follows:
```
[<Material Type Code>: <table Name>, <code column
name>]

<Property Type Code>: <column name>

<Property Type Code>: <column name>

...

[<Material Type Code>: <table Name>, <code column
name>]

<Property Type Code>: <column name>

<Property Type Code>: <column name>

...
```

 Example:

**mapping.txt**

```
# Some comments
[GENE: GENE, GENE_ID]
GENE_SYMBOLS: symbol

[SIRNA: si_rna, code]
INHIBITOR_OF: suppressed_gene
SEQUENCE: Nucleotide_sequence
```


Some rules:

-   Empty lines and lines starting with '\#' will be ignored.
-   Table and column names can be upper or lower case or mixed.
-   Material type codes and property type codes have to be in upper
    case.

```{warning}
If you put a foreign key constraint on the material code of one of the material properties, you need to define the constraint checking as DEFERRED in order to not get a constraint violation. The reason is that this task will *not* order the `INSERT` statements by its dependencies, but in alphabetical order.
```

### UsageReportingTask

**Environment**: AS

**Relevancy:** Relevant

**Description**: Creates a daily/weekly/monthly report to a list of
e-mail recipients about the usage (i.e. creation of experiments, samples
and data sets) by users or groups. For more details see [User Group
Management for Multi-groups openBIS
Instances](../../uncategorized/user-group-management-for-multi-groups-openbis-instances.md).

In order to be able to send an e-mail the following properties in
`service.properties` have to be defined:

```
mail.from = openbis@<host>
mail.smtp.host = <SMTP host>
mail.smtp.user = <can be empty>
mail.smtp.password = <can be empty>
```


**Configuration**:


|Property Key|Description|
|--- |--- |
|interval|Determines the length of period: daily if less than or equal one day, weekly if less than or equal seven days, monthly if above seven days. The actual period is always the day/week/month before the execution day|
|email-addresses|Comma-separated e-mail addresses which will receive the report as an attached text file (format: TSV).|
|user-reporting-type|Type of reporting individual user activities. Possible values are<br /><ul><li>NONE: No reporting</li><li>ALL: Activities inside and outside groups and for all users</li><li>OUTSIDE_GROUP_ONLY: Activities outside groups and users of no groups</li></ul><br />Default: ALL|
|spaces-to-be-ignored|Optional list of comma-separated space codes of all the spaces which should be ignored for the report.|
|configuration-file-path|Optional configuration file defining groups.|
|count-all-entities|If `true` shows the number of all entities (collections, objects, data sets) in an additional column. Default: `false`|


**Example**:

```
class = ch.systemsx.cisd.openbis.generic.server.task.UsageReportingTask
interval = 7 days
email-addresses = ab@c.de, a@bc.de
```


  

## Consistency Repair and Manual Migrations

### BatchSampleRegistrationTempCodeUpdaterTask 

**Environment**: AS

**Relevancy:** Rare

**Description**: Replaces temporary sample codes (i.e. codes matching
the regular expression `TEMP\\.[a-zA-Z0-9\\-]+\\.[0-9]+`) by normal
codes (prefix specified by sample type plus number). This maintenance
task is only needed when `create-continuous-sample-codes` is set `true`
in `service.properties` of AS.

**Example**:

**plugin.properties**

`class = ch.systemsx.cisd.openbis.generic.server.task.BatchSampleRegistrationTempCodeUpdaterTask`

### CleanUpUnarchivingScratchShareTask 

**Environment**: DSS

**Relevancy:** Default

**Description**: Removes data sets from the unarchiving scratch share
which have status ARCHIVED and which are present in archive. For more
details see [Multi data set
archiving](../../uncategorized/multi-data-set-archiving.md).

**Configuration**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.CleanUpUnarchivingScratchShareTask
interval = 60 s
```


**Notes:** Recommended cleanup task to run on every instance.

### DataSetRegistrationSummaryTask 

**Environment**: AS

**Relevancy:** Rare

**Description**: Sends a data set summary report to a list of e-mail
recipients in regular time intervals. The report contains all new data
sets registered since the last report. Selected properties can be
included into the report. The data sets are grouped by the data set
type.

In order to be able to send an e-mail the following properties in
`service.properties` have to be defined:

```
mail.from = openbis@<host>
mail.smtp.host = <SMTP host>
mail.smtp.user = <can be empty>
mail.smtp.password = <can be empty>
```


**Configuration:**


|Property Key|Description|
|--- |--- |
|interval|Interval (in seconds) between regular checks whether to create a report or not. This value should be set to 86400 (1 day). Otherwise the same report might be sent twice or no report will be sent.|
|start|Time the report will be created. A good values for this parameter is some early time in the morning like in the example below.|
|days-of-week|Comma-separated list of numbers denoting days of week (Sunday=1, Monday=2, etc.). This parameter should be used if reports should be sent weekly or more often.|
|days-of-month|Comma-separated list of numbers denoting days of month. Default value of this parameter is 1.|
|email-addresses|Comma-separated list of e-mail addresses.|
|shown-data-set-properties|Optional comma-separated list of data set properties to be included into the report.|
|data-set-types|Restrict the report to the specified comma-separated data set types.|
|configured-content|Use the specified content as the body of the email.|


A report is sent at each day which is either a specified day of week or
day of month. If only weekly reports are needed the parameter
`days-of-month` should be set to an empty string.

**Example**:

**service.properties of AS**

```
<task id>.class = ch.systemsx.cisd.openbis.generic.server.task.DataSetRegistrationSummaryTask
<task id>.interval = 86400
<task id>.start = 1:00
<task id>.data-set-types = RAW_DATA, MZXML_DATA
<task id>.email-addresses = albert.einstein@princeton.edu, charles.darwin@evolution.org 
```


This means that on the 1st day of every month at 1:00 AM openBIS sends
to the specified e-mail recipients a report about the data sets of types
RAW\_DATA and MZXML\_DATA that have been uploaded in the previous month.

### DynamicPropertyEvaluationMaintenanceTask 

**Environment**: AS

**Relevancy:** Rare

**Description**: Re-evaluates dynamic properties of all entities

**Configuration**:

| Property Key | Description  |
|--------------|-------------------------------------------------------------------------------------|
| class        |ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationMaintenanceTask|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationMaintenanceTask
interval = 3600
```


### DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask

**Environment**: AS

**Relevancy:** Deprecated

**Description**: Re-evaluates dynamic properties of all samples which
refer via properties of type MATERIAL directly or indirectly to
materials changed since the last re-evaluation.

**Configuration**:

|Property Key|Description|
|--- |--- |
|class|`ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask`|
|timestamp-file|Path to a file which will store the timestamp of the last evaluation. Default value: `../../../data/DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask-timestamp.txt`.|
|initial-timestamp|Initial timestamp of the form `YYYY-MM-DD` (e.g. 2013-09-15) which will be used the first time when the timestamp file doesn't exist or has an invalid value. This is a mandatory property.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask
interval = 7 days
initial-timestamp = 2012-12-31
```


### FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask 

**Environment**: DSS

**Relevancy:** Rare

**Description**: Queries openBIS database to find data sets without a
size filled in, then queries the pathinfo DB to see if the size info is
available there; if it is available, it fills in the size from the
pathinfo information. If it is not available, it does nothing. Data sets
from openBIS database are fetched in chunks (see data-set-chunk-size
property). After each chunk the maintenance tasks checks whether a time
limit has been reached (see time-limit property). If so, it stops
processing. A code of the last processed data set is stored in a file
(see last-seen-data-set-file property). The next run of the maintenance
task will process data sets with a code greater than the one saved in
the "last-seen-data-set-file". This file is deleted periodically (see
delete-last-seen-data-set-file-interval) to handle a situation where
codes of new data sets are lexicographically smaller than the codes of
the old datasets. Deleting the file is also needed when pathinfo
database entries are added after a data set has been already processed
by the maintenance task. 

**Configuration**:


|Property Key|Description|
|--- |--- |
|last-seen-data-set-file|Path to a file that will store a code of the last handled data set. Default value: "fillUnknownDataSetSizeTaskLastSeen"|
|delete-last-seen-data-set-file-interval|A time interval (in seconds) which defines how often the "last-seen-data-set-file" file should be deleted. The parameter can be specified with one of the following time units:  `ms`, `msec`, `s`, `sec`, `m`, `min`, `h`, `hours`, `d`, `days`. Default time unit is `sec`. Default value: 7 days.|
|data-set-chunk-size|Number of data sets requested from AS in one chunk. Default: 100|
|time-limit|Limit of execution time of this task. The task is stopped before reading next chunk if the time has been used up. This parameter can be specified with one of the following time units: `ms`, `msec`, `s`, `sec`, `m`, `min`, `h`, `hours`, `d`, `days`. Default time unit is `sec`.|

**Example:**

**plugin.properties**

```
<task id>.class = ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask
<task id>.interval = 86400
<task id>.data-set-chunk-size = 1000
<task id>.time-limit = 1h
```


**NOTE**: Useful in scenarios where the path info feeding sub task of
post registration task fails.

### PathInfoDatabaseChecksumCalculationTask

**Environment**: DSS

**Relevancy:** Rare, often the CRC32 is calculated during the post
registration.

**Description**: Calculates the CRC32 checksum (and optionally a
checksum of specified type) of all files in the pathinfo database with
unknown checksum. This task is needed to run only once. It assumes a
data source for key 'path-info-db'. 

**Configuration**:

|Property Key|Description|
|--- |--- |
|checksum-type|Optional checksum type. If specified two checksums are calculated: CRC32 checksum and the checksum of specified type. The type and the checksum are stored in the pathinfo database. An allowed type has to be supported by `MessageDigest.getInstance(<checksum type>)`. For more details see http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html#getInstance-java.lang.String-.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.path.PathInfoDatabaseChecksumCalculationTask
execute-only-once = true
checksum-type = SHA-256
```


### PathInfoDatabaseRefreshingTask

**Environment**: DSS

**Relevancy:** Rare

**Description**: Refreshes the pathinfo database with file metadata of
physical and available data sets in the store. This task assumes a data
source with for 'path-info-db'.

The data sets are processed in the inverse order they are registered.
Only a maximum number of data sets are processed in one run. This is
specified by `chunk-size`.

```{warning}
Under normal circumstances this maintenance task is never needed, because the content of a physical data set is **never** changed by openBIS itself.<br /><br />Only in the rare cases that the content of physical data sets have to be changed this maintenance task allows to refresh the file meta data in the pathinfo database.
```

**Configuration**:

|Property Key|Description|
|--- |--- |
|time-stamp-of-youngest-data-set|Time stamp of the youngest data set to be considered. The format has to be `<4 digit year>-<month>-<day> <hour>:<minute>:<second>`.|
|compute-checksum|If `true` the CRC32 checksum (and optionally a checksum of the type specified by `checksum-type`) of all files will be calculated and stored in pathinfo database. Default value: true|
|checksum-type|Optional checksum type. If specified and `compute-checksum = true` two checksums are calculated: CRC32 checksum and the checksum of specified type. The type and the checksum are stored in the pathinfo database. An allowed type has to be supported by `MessageDigest.getInstance(<checksum type>)`. For more details see [Oracle doc](http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html#getInstance-java.lang.String-).|
|chunk-size|Number of data sets requested from AS in one chunk. Default: 1000|
|data-set-type|Optional data set type. If specified, only data sets of the specified type are considered. Default: All data set types.|
|state-file|File to store registration time stamp and code of last considered data set. Default: `<store root>/PathInfoDatabaseRefreshingTask-state.txt`|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.path.PathInfoDatabaseRefreshingTask
interval = 30 min
time-stamp-of-youngest-data-set = 2014-01-01 00:00:00
data-set-type = HCS_IMAGE
```


### RemoveUnusedUnofficialTermsMaintenanceTask

**Environment**: AS

**Relevancy:** Rare

**Description**: Removes unofficial unused vocabulary terms. For more details about unofficial vocabulary terms see [Ad Hoc Vocabulary Terms](../../uncategorized/ad-hoc-vocabulary-terms.md).

**Configuration:**

|Property Key|Description|
|--- |--- |
|older-than-days|Unofficial terms are only deleted if they have been registered more than the specified number of days ago. Default: 7 days.|


**Example**:

**service.properties of AS**

```
<task id>.class = ch.systemsx.cisd.openbis.generic.server.task.RemoveUnusedUnofficialTermsMaintenanceTask
<task id>.interval = 86400
<task id>.older-than-days = 30
```


### ResetArchivePendingTask

**Environment**: DSS

**Relevancy:** Rare

**Description**: For each data set not present in archive and status
ARCHIVE\_PENDING the status will be set to AVAILABLE if there is no
command in the DSS data set command queues referring to it.

**Configuration**:

**plugin.properties**

```
class = ch.systemsx.cisd.etlserver.plugins.ResetArchivePendingTask
interval = 60 s
```


### SessionWorkspaceCleanUpMaintenanceTask

**Environment**: AS

**Relevancy:** Default

**Description**: Cleans up session workspace folders of no longer active
sessions. This maintenance plugin is automatically added by default with
a default interval of 1 hour. If a manually configured version of the
plugin is detected then the automatic configuration is skipped.

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.SessionWorkspaceCleanUpMaintenanceTask
interval = 1 day
```


### MaterialsMigration

**Environment**: AS

**Relevancy:** Relevant

**Description**: Migrates the Materials entities and types to use a
Sample based model using Sample Properties. It automatically creates and
assigns sample types, properties and entities.

It allows to execute the migration and to delete of the old Materials
model in separate steps.

Deleting Materials and material types requires the migration to have
been a success,  before the deletion a validation check is run.

**Example**:

This maintenance task can be directly configured on the AS
service.properties

**service.properties**

```
maintenance-plugins = materials-migration

materials-migration.class = ch.systemsx.cisd.openbis.generic.server.task.MaterialsMigration
materials-migration.execute-only-once = true
materials-migration.doMaterialsMigrationInsertNew = true
materials-migration.doMaterialsMigrationDeleteOld = true
```


  

## Microscopy Maintenance Tasks

### MicroscopyThumbnailsCreationTask

**Environment**: DSS

**Relevancy:** Relevant

**Description**: Creates thumbnails for already registered microscopy
data sets.

**Configuration:**

|Property Key|Description|
|--- |--- |
|maximum-number-of-workers|If specified the creation will be parallelized among several workers. The actual number of workers depends on the number CPUs. There will be not more than 50% of CPUs used.|
|state-file|Name of the file which stores the registration time stamp of the last successfully handled data set. Default: `MicroscopyThumbnailsCreationTask-state.txt`|
|script-path|Path to the jython script which specifies the thumbnails to be generated. The script should have defined the method `process(transaction, parameters, tablebuilder)` as for `JythonIngestionService` (see Jython-based Reporting and Processing Plugins). Note, that tablebuilder will be ignored. In addition the global variables `image_config` and `image_data_set_structure` are defined:<br /><ul><li>image_data_set_structure: It is an object of the class `ImageDataSetStructure`. Information about channels, series numbers etc. can be requested.</li><li>image_config: It is an object of the class `SimpleImageContainerDataConfig`. It should be used to specify the thumbnails to be created. Currently only `setImageGenerationAlgorithm()` is supported.</li></ul>|
|main-data-set-type-regex|Regular expression for the type of data sets which have actual images. Default: `MICROSCOPY_IMG`|
|data-set-thumbnail-type-regex|Regular expression for the type of data sets which have thumbnails. This is used to test whether there are already thumbnails or not. Default: `MICROSCOPY_IMG_THUMBNAIL`|
|max-number-of-data-sets|The maximum number of data sets to be handle in a run of this task. If zero or less than zero all data sets will be handled. Default: 1000|
|data-set-container-type|Type of the data set container. Default: `MICROSCOPY_IMG_CONTAINER`|

**Example**:

**plugin.properties**

    class = ch.systemsx.cisd.openbis.dss.etl.MicroscopyThumbnailsCreationTask
    interval = 1 h
    script-path = specify_thumbnail_generation.py

with

**specify\_thumbnail\_generation.py**

```py
from ch.systemsx.cisd.openbis.dss.etl.dto.api.impl import MaximumIntensityProjectionGenerationAlgorithm
from sets import Set


def _get_series_num():
series_numbers = Set()
for image_info in image_data_set_structure.getImages():
    series_numbers.add(image_info.tryGetSeriesNumber())
return series_numbers.pop()

def process(transaction, parameters, tableBuilder):
seriesNum = _get_series_num()
if int(seriesNum) % 2 == 0:
    image_config.setImageGenerationAlgorithm(
            MaximumIntensityProjectionGenerationAlgorithm(
                "MICROSCOPY_IMG_THUMBNAIL", 256, 128, "thumbnail.png"))
```



### DeleteFromImagingDBMaintenanceTask

**Environment**: DSS

**Relevancy:** Relevant

**Description**: Deletes database entries from the imaging database.
This is special variant of [DeleteFromExternalDBMaintenanceTask](./maintenance-tasks.md#deletefromexternaldbmaintenancetask) with the same configuration parameters.

**Configuration**: See [DeleteFromExternalDBMaintenanceTask](./maintenance-tasks.md#deletefromexternaldbmaintenancetask)

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.etl.DeleteFromImagingDBMaintenanceTask
data-source = imaging-db
```

     

## Proteomics Maintenance Tasks
