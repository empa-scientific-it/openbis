# Multi data set archiving

## Introduction

Multi data set archiver is a tool to archive several datasets together
in chunks of relatively large size. When a group of datasets is selected
for archive it is verified if they are all together of proper size and
then they are being stored as one big container file (tar) on the
destination storage.

When unarchiving data sets from a multi data set archive the following
rules are obeyed:

-   Unarchiving of data sets from different containers is possible as
    long as the maximum unarchiving cap specified in the
    plugin.properties file is not exceeded.
-   All data sets from a container are unarchived even though
    unarchiving has been requested only for a sub set.
-   The data sets are unarchived into a share which is marked as an
    unarchiving scratch share.
-   In case of not enough free space in the scratch share the oldest
    (defined by modification time stamp) data sets are removed from the
    scratch share to free space. For those data sets the archiving
    status is set back to ARCHIVED.

To test the archiver find the datasets you want to archive in openbis
GUI and "add to archive".

## Important technical details

The archiver requires configuration of three important entities.

-   An archive destination (e.g. on Strongbox).
-   A PostgreSQL database for mapping information (i.e. which data set
    is in which container file).
-   An unarchiving scratch share.

Multi dataset archiver is not compatible with other archivers. You
should have all data available before configuring this archiver.

## Workflows

The multi data set archiver can be configured for four different
workflows. The workflow is selected by the presence/absence of the
properties `staging-destination` and `replicated-destination`.

### Simple workflow

None of the properties  `staging-destination`
and `replicated-destination` are present.

1.  Wait for enough free space on the archive destination.
2.  Store the data set in a container file directly on the archive
    destination.
3.  Perform sanity check. That is, getting the container file to the
    local disk and compare the content with the content of all data sets
    in the store.
4.  Add mapping data to the PostgreSQL database.
5.  Remove data sets from the store if requested.
6.  Update archiving status for all data sets.

### Staging workflow

Property `staging-destination` is specified but
`replicated-destination` is not.

1.  Store the data sets in a container file in the staging folder.
2.  Wait for enough free space on the archive destination.
3.  Copy the container file from the staging folder to the archive
    destination.
4.  Perform sanity check.
5.  Remove container file from the staging folder.
6.  Add mapping data to the PostgreSQL database.
7.  Remove data sets from the store if requested.
8.  Update archiving status for all data sets.

### Replication workflow

Property `     replicated`-destination is specified but
`     staging`-destination is not.

1.  Wait for enough free space on the archive destination.
2.  Store the data set in a container file directly on the archive
    destination.
3.  Perform sanity check.
4.  Add mapping data to the PostgreSQL database.
5.  Wait until the container file has also been copied (by some external
    process) to a replication folder.
6.  Remove data sets from the store if requested.
7.  Update archiving status for all data sets.

Some remarks:

-   Steps 5 to 7 will be performed asynchronously from the first four
    steps because step 5 can take quite long. In the meantime the next
    archiving task can already be performed.
-   If the container file isn't replicated after some time archiving
    will be rolled back and scheduled again.

### Staging and replication workflow

When both properties `staging-destination` and `replicated-destination`
are present staging and replication workflow will be combined.

## Clean up

In case archiving fails all half-baked container files have to be
removed. By default this is done immediately.

But in context of tape archiving systems (e.g. Strongbox) immediate
deletion might not always be possible all the time. In this case a
deletion request is schedule. The request will be stored in file. It
will be handled in a separate thread in regular time intervals (polling
time). If deletion isn't possible after some timeout an e-mail will be
sent. Such deletion request will still be handled but the e-mail allows
manual intervention/deletion. Note, that deletion requests for
non-existing files will always be handled successfully.

## Configuration steps

-   Disable existing archivers
    -   Find all properties of a form `archiver.*` in
        `servers/datastore_server/etc/service.properties` and remove
        them.
    -   Find all DSS core plugins of type `miscellaneous` which define
        an archiver. Disable them by adding an empty marker file
        named `disabled`.

-   Enable archiver
    -   Configure a new DSS core plugin of type `miscellaneous`:

        **multi-dataset-archiver/1/dss/miscellaneous/archiver/plugin.properties**

            archiver.class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiver

            # Temporary folder (needed for sanity check). Default: Value provided by Java system property java.io.tmpdir. Usually /tmp
            # archiver.temp-folder = <java temp folder>

            # Archive destination
            archiver.final-destination = path/to/strongbox/as/mounted/resource

            # Staging folder (needed for 'staging workflow' and 'staging and replication workflow')
            archiver.staging-destination = path/to/local/stage/area

            # Replication folder (needed for 'replication workflow' and 'staging and replication workflow')
            archiver.replicated-destination = path/to/mounted/replication/folder

            # The archiver will refuse to archive group of data sets, which together are smaller than this value
            archiver.minimum-container-size-in-bytes = 15000000

            # The archiver will refuse to archive group of data sets, which together are bigger than this value.
            # The archiver will ignore this value, when archiving single data set.
            archiver.maximum-container-size-in-bytes = 35000000

            # This variable is meant for another use case, than this archiver, but is shared among all archivers.
            # For this archiver it should be specified for something safely larger than maximum-container-size-in-bytes
            archiver.batch-size-in-bytes = 80000000

            # (since version 20.10.4) Check consistency between file meta data of the files in the store and from the pathinfo database.
            # Default value: true 
            # check-consistency-between-store-and-pathinfo-db = true

            # Archiving can be speed up if setting this flag to false (default value: true). But this works only if the data sets
            # to be archived do not contain hdf5 files which are handled as folders (like the thumbnail h5ar files in screening/microscopy).
            # archiver.hdf5-files-in-data-set = true

            # Whether all data sets should be archived in a top level directory of archive or with sharding (the way data sets are stored in openbis internal store)
            # archiver.with-sharding = false

            # Polling time for evaluating free space on archive destination
            # archiver.waiting-for-free-space-polling-time = 1 min

            # Maximum waiting time for free space on archive destination
            # archiver.waiting-for-free-space-time-out = 4 h

            # If set to true, then an initial waiting time will be added before starting a sanity check.
            # If the sanity check fails, it will be retried. The time between each sanity check attempt is doubled,
            # starting from the initial waiting time up to the maximum waiting time (see properties below).
            # Default: false
            archiver.wait-for-sanity-check = true

            # Initial waiting time before starting a sanity check. Works only if 'wait-for-sanity-check = true'
            # Default: 10sec
            archiver.wait-for-sanity-check-initial-waiting-time = 120 sec

            # Maximum total waiting time for failed sanity check attempts. Works only if 'wait-for-sanity-check = true'
            # Default: 30min
            archiver.wait-for-sanity-check-max-waiting-time = 5 min

            # A template of a shell command to be executed before unarchiving. The template may use ${container-path} and ${container-id} variables which will be replaced with an absolute container path (full path of the tar file to be unarchived)
            # and a container id (id of the container to be unarchived used in the archiving database). The command created from the template is executed only once for a given container (just before the first unarchiving attempt) and is not retried.
            # The unarchiver waits for the command to finish before proceeding. If the command exits with status zero, then the unarchiving is started. If the command exits with a non-zero value, then the archiving is marked as failed.
            #
            # Example: tar -tf ${container-path}
            # Default: null
            archiver.unarchiving-prepare-command-template

            # If set to true, then the unarchiver waits for T flag to be removed from the file in the final destination before it tries to read the file.
            # Default: false
            archiver.unarchiving-wait-for-t-flag = true

            # Maximum total waiting time for failed unarchiving attempts.
            # Default: null
            archiver.unarchiving-max-waiting-time = 1d

            # Polling time for waiting on unarchiving.
            # Default: null
            archiver.unarchiving-polling-time = 5 min

            # If set to true, then the archiver waits for T flag to be set on the file in the replicated destination. The check is done before a potential sanity check of the replicated file (see 'finalizer-sanity-check').
            # Default: false
            archiver.finalizer-wait-for-t-flag = true

            # If set to true, then a sanity check for the replicated destination is also performed (in addition to a sanity check for the final destination which is always executed).
            # Default: false
            archiver.finalizer-sanity-check = true

            # Minimum required free space at final destination before triggering archiving if > 0. This threshold can be
            # specified as a percentage of total space or number of bytes. If both are specified the threshold is given by
            # the maximum of both values.
            # archiver.minimum-free-space-at-final-destination-in-percentage
            # archiver.minimum-free-space-at-final-destination-in-bytes

            # Minimum free space on archive destination after container file has been added.
            # archiver.minimum-free-space-in-MB = 1024

            # Polling time for waiting on replication. Only needed if archiver.replicated-destination is specified.
            # archiver.finalizer-polling-time = 1 min

            # Maximum waiting time for replication finished.  Only needed if archiver.replicated-destination is specified.
            # archiver.finalizer-max-waiting-time = 1 d

            # Maximum total size (in MB) of data sets that can be scheduled for unarchiving at any given time. When not specified, defaults to 1 TB.
            # Note also that the value specified must be consistent with the scratch share size. 
            # maximum-unarchiving-capacity-in-megabytes = 200000

            # Delay unarchiving. Needs MultiDataSetUnarchivingMaintenanceTask.
            # archiver.delay-unarchiving = false

            # Size of the buffer used for copying data. Default value: 1048576 (i.e. 1 MB). This value is only important in case of accurate
            # measurements of data transfer rates. In case of expected fast transfer rates a larger value (e.g. 10 MB) should be used.
            # archiver.buffer-size = 1048576

            # Maximum size of the writing queue for copying data. Reading from the data store and writing to the TAR file is 
            # done in parallel. The default value 5 * archiver.buffer-size. 
            # archiver.maximum-queue-size-in-bytes = 5242880

            # Path (absolute or relative to store root) of an empty file. If this file is present starting 
            # archiving will be paused until this file has been removed. 
            # This property is useful for archiving media/facilities with maintenance downtimes.
            # archiver.pause-file = pause-archiving

            # Time interval between two checks whether pause file still exists or not.
            # archiver.pause-file-polling-time = 10 min

            #-------------------------------------------------------------------------------------------------------
            # Clean up properties
            # 
            # A comma-separated list of path to folders which should be cleaned in a separate thread
            #archiver.cleaner.file-path-prefixes-for-async-deletion = <absolute path 1>, <absolute path 2>, ...

            # A folder which will contain deletion request files. This is a mandatory property if 
            # archiver.cleaner.file-path-prefixes-for-async-deletion is specified.
            #archiver.cleaner.deletion-requests-dir = <some local folder>

            # Polling time interval for looking and performing deletion requests. Default value is 10 minutes.
            #archiver.cleaner.deletion-polling-time = 10 min

            # Time out of deletion requests. Default value is one day.
            #archiver.cleaner.deletion-time-out = 24 h

            # Optional e-mail address. If specified every integer multiple of the timeout period an e-mail is send to 
            # this address listing all deletion requests older than specified timeout.
            #archiver.cleaner.email-address = <some valid e-mail address>

            # Optional e-mail address for the 'from' field.
            #archiver.cleaner.email-from-address = <some well-formed e-mail address>

            # Subject for the 'subject' field. Mandatory if an e-mail address is specified.
            #archiver.cleaner.email-subject = Deletion failure

            # Template with variable ${file-list} for the body text of the e-mail. The variable will be replaced by a list of
            # lines. Two lines for each deletion request. One for the absolute file path and one of the request time stamp.
            # Mandatory if an e-mail address is specified.
            #archiver.cleaner.email-template = The following files couldn't be deleted:\n${file-list}

            #-------------------------------------------------------------------------------------------------------
            # The following properties are necessary in combination with data source configuration
            multi-dataset-archive-database.kind = prod
            multi-dataset-archive-sql-root-folder = datastore_server/sql/multi-dataset-archive


        You should make sure that all destination directories exist and
        DSS has read/write privileges before attempting archiving
        (otherwise the operation will fail).  
        Add the core plugin module name (here `multi-dataset-archiver`)
        to the property `enabled-modules` of `core-plugin.properties`.

-   Enable PostgreSQL datasource
    -   Configure a new DSS core plugin of type `data-sources`:

        **multi-dataset-archiver/1/dss/data-sources/multi-dataset-archiver-db/plugin.properties**

            version-holder-class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBVersionHolder
            databaseEngineCode = postgresql
            basicDatabaseName = multi_dataset_archive
            urlHostPart = ${multi-dataset-archive-database.url-host-part:localhost}
            databaseKind = ${multi-dataset-archive-database.kind:prod}
            scriptFolder = ${multi-dataset-archive-sql-root-folder:}
            owner = ${multi-dataset-archive-database.owner:}
            password = ${multi-dataset-archive-database.password:}

-   Create a share which will be used exclusively as a scratch share for
    unarchiving. To mark it for this purpose add a `share.properties`
    file to the share (e.g. `<mounted share>/store/1/share.properties`)
    with property `unarchiving-scratch-share = true`.  
    In addition the maximum size of the share can be specified. Example:

    **share.properties**

        unarchiving-scratch-share = true
        unarchiving-scratch-share-maximum-size-in-GB = 100

-   It is recommended to do archiving in a separate queue in order to
    avoid situation when fast processing plugin tasks are not processes
    because multi data set archiving tasks can take quite long. If one
    of the two workflows with replication is selected
    (i.e. `archiver.replicated-destination`) a second processing plugin
    (ID `Archiving Finalizer`) is used. It should run in a queue
    different from the queue used for archiving. The following setting
    in DSS `service.properties` covers all workflows:

    **service.properties**

        data-set-command-queue-mapping = archiving:Archiving|Copying data sets to archive, unarchiving:Unarchiving, archiving-finalizer:Archiving Finalizer

## Clean up Unarchiving Scratch Share

(Since version 20.10.4) Data sets in the unarchiving scratch share can
be removed any times because they are already present in archive.
Normally this happens during unarchving if there is not enough free
space available in the scratch share. But this may fail for some reason.
This can lead to the effect that unarchiving doesn't work because they
are data sets in the scratch share which can be removed because they are
archived.

Therefore, it is recommended to setup a
[CleanUpUnarchivingScratchShareTask](../system-admin-documentation/advanced-features/maintenance-tasks.md#cleanupunarchivingscratchsharetask)
which removes data sets from the scratch share which fulfill the
following conditions:

-   The data set is in state ARCHIVED and the flag `presentInArchive` 
    is set.
-   The data set is found in the Multi Data Set Archive database and the
    corresponding TAR archive file exists.  

## Deletion of archived Data Sets

(Since version 20.10.3) Archived data sets can be deleted permanently.
But they are still in the container files. In order to remove them also
from the container files a
[MultiDataSetDeletionMaintenanceTask](../system-admin-documentation/advanced-features/maintenance-tasks.md#multidatasetdeletionmaintenancetask)
has to be configured.

## Recovery from corrupted archiving queues

In case the queues with the archiving commands get corrupted, they
cannot be used any more, they need to be deleted before the DSS starts
and a new one will be created. The typical scenario where this happens
is when you get out of space on the disk where the queues are stored.

The following steps describe how to recover from such a situation.

1.  Finding out the data sets that are in 'ARCHIVE\_PENDING' status.

```sql
SELECT id, size, present_in_archive, share_id, location FROM external_data WHERE status = 'ARCHIVE_PENDING';
 
openbis_prod=> SELECT id, size, present_in_archive, share_id, location FROM external_data WHERE status = 'ARCHIVE_PENDING'; 
    data_id |    size     | present_in_archive | share_id |                               location                                
---------+-------------+--------------------+----------+-----------------------------------------------------------------------
    3001 | 34712671864 | f                  | 1        | 585D8354-92A3-4C24-9621-F6B7063A94AC/17/65/a4/20170712111421297-37998
    3683 | 29574172672 | f                  | 1        | 585D8354-92A3-4C24-9621-F6B7063A94AC/39/6c/b0/20171106181516927-39987
    3688 | 53416316928 | f                  | 1        | 585D8354-92A3-4C24-9621-F6B7063A94AC/ca/3b/93/20171106183212074-39995
    3692 | 47547908096 | f                  | 1        | 585D8354-92A3-4C24-9621-F6B7063A94AC/b7/26/85/20171106185354378-40002
```
        
2.  The data sets found, can be or not in the archiving process. This is
    not easy to find out instantly. It's easier just to execute the
    above statement in subsequent days.

3.  If the data sets are still in 'ARCHIVE\_PENDING' after a sensible
    amount of time (1 week for example) and there is no other issues,
    like the archiving destination is not available there is a good
    change, they are really stuck on the process.

4.  Reaching this point, the data sets are most likely still on the data
    store as indicated by the combination of share ID and location
    indicated. Verify this! If they are not there hope they are archived
    or you are on trouble.

5.  If they are on the store, you need to set the status to available
    again using a SQL statement.

```sql
openbis_prod=> UPDATE external_data SET status = 'AVAILABLE', present_in_archive = 'f'  WHERE id IN (SELECT id FROM data where code in ('20170712111421297-37998', '20171106181516927-39987')); 
```
      

If there is half copied files on the archive destination, these need
to be delete too, to find them run the next queries.

    

To find out the containers:
      
```sql   
SELECT * FROM data_sets WHERE CODE IN('20170712111421297-37998', '20171106181516927-39987', '20171106183212074-39995', '20171106185354378-40002');

multi_dataset_archive_prod=> SELECT * FROM data_sets WHERE CODE IN('20170712111421297-37998', '20171106181516927-39987', '20171106183212074-39995', '20171106185354378-40002');
    id  |          code           | ctnr_id | size_in_bytes 
-----+-------------------------+---------+---------------
    294 | 20170712111421297-37998 |      60 |   34712671864
    295 | 20171106185354378-40002 |      61 |   47547908096
    296 | 20171106183212074-39995 |      61 |   53416316928
    297 | 20171106181516927-39987 |      61 |   29574172672
(4 rows)

multi_dataset_archive_prod=> SELECT * FROM containers WHERE id IN(60, 61);
    id |                    path                     | unarchiving_requested 
----+---------------------------------------------+-----------------------
    60 | 20170712111421297-37998-20171108-105339.tar | f
    61 | 20171106185354378-40002-20171108-130342.tar | f
```

```{note}
We have never seen it but if there is a container with data
sets in different archiving status then, you need to recover the
ARCHIVED data sets from the container and copy them manually to the
data store before being able to continue.
```

```sql
multi_dataset_archive_prod=> SELECT * FROM data_sets WHERE ctnr_id IN(SELECT ctnr_id FROM data_sets WHERE CODE IN('20170712111421297-37998', '20171106181516927-39987', '20171106183212074-39995', '20171106185354378-40002'));
```

6.  After deleting the files clean up the multi dataset archiver
    database.

```sql
openbis_prod=> DELETE FROM containers WHERE id IN (SELECT ctnr_id FROM data_sets WHERE CODE IN('20170712111421297-37998', '20171106181516927-39987', '20171106183212074-39995', '20171106185354378-40002'));
```
