Dropboxes
=========

Jython Dropboxes
----------------

### Introduction

The jython dropbox feature makes it possible for a script written in the
Python language to control the data set registration process of the
openBIS Data Store Server. A script can modify the files in the dropbox
and register data sets, samples, and experiments as part of its
processing. The framework provides tools to track file operations and,
if necessary, revert them, ensuring that the incoming file or directory
is returned to its original state in the event of an error.

By deafult python 2.5 is used, but it's possible to use python version
2.7.

Dropboxes are dss core plugins: [Core Plugins](./core-plugins.md#core-plugins)

### Simple Example

Here is an example that registers files that arrive in the drop box as
data sets. They are explicitly attached to the experiment "JYTHON" in
the project "TESTPROJ" and space "TESTGROUP".

**data-set-handler-basic.py**

```py
def process(transaction):
    # Create a data set
    dataSet = transaction.createNewDataSet()
 
    # Reference the incoming file that was placed in the dropbox
    incoming = transaction.getIncoming()
    # Add the incoming file into the data set
    transaction.moveFile(incoming.getAbsolutePath(), dataSet)
 
    # Get an experiment for the data set
    exp = transaction.getExperiment("/TESTGROUP/TESTPROJ/JYTHON")

    # Set the owner of the data set -- the specified experiment
    dataSet.setExperiment(exp)
```


This example is is unrealistically simple, but contains all the elements
necessary to implement a jython drop box. The main idea is to perform
several operations within the bounds of a transaction on the data and
metadata. The transaction is used to track the changes made so they can
be executed together or all reverted if a problem occurs.

### More Realistic Example

The above example demonstrates the concept, but it is unrealistically
simple. In general, we want to be able to determine and specify the
experiment/sample for a data set and explicitly set the data set type as
well.

In this example, we handle a usage scenario where there is one
experiment done every day. All data produced on a single day is
associated with the experiment for that date. If the experiment for a
given day does not exist, it is created.

**data-set-handler-experiment-reg.py**

```py
from datetime import datetime
 
def process(transaction):

    # Try to get the experiment for today
    now_str = datetime.today().strftime('%Y%m%d')
    expid = "/TESTGROUP/TESTPROJ/" + now_str
    exp = transaction.getExperiment(expid)


    # Create an experiment if necessary
    if None == exp:
    exp = transaction.createNewExperiment(expid, "COMPOUND_HCS")
    exp.setPropertyValue("DESCRIPTION", "An experiment created on " + datetime.today().strftime('%Y-%m-%d'))
    exp.setPropertyValue("COMMENT", now_str)
     
    dataSet = transaction.createNewDataSet()
    
    incoming = transaction.getIncoming()
    transaction.moveFile(incoming.getAbsolutePath(), dataSet)
    dataSet.setDataSetType("HCS_IMAGE")
    dataSet.setExperiment(exp)
```


More complex processing is also possible. In the following sections, we
explain how to configure a jython dropbox and describe the API in
greater detail.

### Model

The model underlying dropbox registration is the following: when a new
file or folder is found in the dropbox folder, the process function of
the script file is invoked with a [data set registration transaction](./dss-dropboxes.md#idatasetregistrationtransaction) as an argument.
The process function has the responsibility of looking at the incoming
file or folder and determining what needs to be registered or modified
in the metadata database and what data needs to be stored on the file
system. The
[IDataSetRegistrationTransaction](./dss-dropboxes.md#idatasetregistrationtransaction) interface
defines the API for specifying entities to register and update.

Committing a transaction is actually a two-part process. The metadata is stored in the openBIS application server's database; the data is kept on the file system in a sharded directory structure beneath the data store server's *store* directory. All modifications requested as part of a transaction are committed atomically — they either all succeed or all fail.

Several [Events](./dss-dropboxes.md#events-registration-process-hooks) occur in the process of committing a transaction. By defining jython functions, it is possible to be notified and intervene when an event occurs. Because the infrastructure reserves the right to delay or retry actions if resources become unavailable, the process function and event functions cannot use global variables to communicate with each other. Instead, they should use the registration context object to communicate. Anything stored in the registration context must, however, be serializable by Java serialization.

Details
-------

### Dropbox Configuration

A jython dropbox is typically distributed as a [core plugin](./core-plugins.md#core-plugins) and configured in its plugin.properties file. A dropbox configured to run a jython script, which is kept in the same directory as plugin.properties. The configuration requires a storage processor and the name of the script (a full path is not necessary if the script is in the same directory as the plugin.properties). Here is an example configuration for a dropbox that uses the jython handler.

**plugin.properties**

```
#
# REQUIRED PARAMETERS
#
# The directory to watch for new data sets
incoming-dir = ${root-dir}/incoming-jython

# The handler class. Must be either ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2 or a subclass thereof
top-level-data-set-handler = ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2

# The script to execute, reloaded and recompiled each time a file/folder is placed in the dropbox
script-path = ${root-dir}/data-set-handler.py

# The appropriate storage processor
storage-processor = ch.systemsx.cisd.etlserver.DefaultStorageProcessor

# Specify jython version. Default is whatever is specified in datastore server service.properties under property "jython-version"
plugin-jython-version=2.5
#
# OPTIONAL PARAMETERS
#
 
# False if incoming directory is assumed to exist.
# Default - true: Incoming directory will be created on start up if it doesn't exist.
incoming-dir-create = true

# Defines how the drop box decides if a folder is ready to process: either by a 'marker-file' or a time out which is called 'auto-detection'
# The time out is set globally in the service.properties and is called 'quiet-period'. This means when the number of seconds is over and no changes have
# been made to the incoming folder the drop will start to register. The marker file must have the following naming schema: '.MARKER_is_finished_<incoming_folder_name>'
incoming-data-completeness-condition = marker-file 
 
# Defines whether the dropbox should handle .h5 archives as folders (true) or as files (false). Default is true.
h5-folders = true
 
# Defines whether the dropbox should handle .h5ar archives as folders (true) or as files (false). Default is true.
h5ar-folders = true
```


#### Development mode

Set property `development-mode = true` in your dropbox to enable a quick
feedback loop when developing your dropbox. By default dropboxes have
complex auto-recovery mechanism working, which on errors waits and
retries the registration several times. It can be useful in case of
short network problems or other unexpected turbulences. In this case it
can take a long time between the dropbox tries to register something,
and actual error report. During development it is essential to have a
quick feedback if your dropbox does what it should or not. Thus - set
the development mode if you are modifying your script and remember to
set it back when you are done.

#### Jython version

Set property `plugin-jython-version=2.7` in your dropbox
plugin.properties to change default jython version for the single
dropbox. Available are versions 2.5 and 2.7

Jython API
----------

When a new file is placed in the dropbox, the framework compiles and
executes the script, checks that the signatures of the `process`
function and any defined event-handling functions are correct, and then
invokes its `process` function.

### IDataSetRegistrationTransaction

Have a look
at [IDataSetRegistrationTransactionV2](https://openbis.ch/javadoc/20.10.x/javadoc-dropbox-api/ch/systemsx/cisd/etlserver/registrator/api/v2/IDataSetRegistrationTransactionV2.html)
for the calls available in a transaction. Note that you need to use the
file methods in the transaction, like e.g. `moveFile()`,  rather than
manipulating the file system directly to get fully transactional
behavior.

#### TransDatabase queries

The query object returned
by `getDatabaseQuery(String dataSourceName)` allows to perform any query
and executing any statement on the given query database in the context
of a database transaction. Here are the methods available from the query
interface:

```java
public interface DynamicQuery {

    /**
        * Performs a SQL query. The returned List is connected to the database and
        * updateable.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template.
        * 
        * @return The result set as List; each row is represented as one Map<String,Object>.
        */
    List<Map<String, Object>> select(final String query,
            final Object... parameters);

    /**
        * Performs a SQL query. The returned List is connected and
        * updateable.
        * 
        * @param type  The Java type to return one rows in the returned
        *            result set.
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template.
        * 
        * @return The result set as List; each row is represented as one Map<String,Object>.
        */
    <T> List<T> select(final Class<T> type, final String query,
            final Object... parameters);

    /**
        * Executes a SQL statement.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template.
        * 
        * @return The number of rows updated by the SQL statement, or -1 if not
        *         applicable. <b>Note:</b> Not all JDBC drivers support this
        *         cleanly.
        */
    int update(final String query, final Object... parameters);

    /**
        * Executes a SQL statement as a batch for all parameter values provided.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template. At least
        *            one of the parameters needs to be an array or
        *            <code>Collection</code>. If multiple parameters are arrays or
        *            <code>Collection</code>, all of them need to have the same
        *            size.
        * 
        * @return The number of rows updated by the SQL statement, or -1 if not
        *         applicable. <b>Note:</b> Not all JDBC drivers support this
        *         cleanly.
        */
    int batchUpdate(final String query, final Object... parameters);

    /**
        * Executes a SQL statement. Supposed to be used for INSERT statements with
        * an automatically generated integer key.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template.
        * 
        * @return The automatically generated key. <b>Note:</b> Not all JDBC
        *         drivers support this cleanly.
        */
    long insert(final String query, final Object... parameters);

    /**
        * Executes a SQL statement. Supposed to be used for INSERT statements with
        * one or more automatically generated keys.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template.
        * 
        * @return The automatically generated keys. <b>Note:</b> Not all JDBC
        *         drivers support this cleanly and it is in general driver-dependent 
        *         what keys are present in the returned map.
        */
    Map<String, Object> insertMultiKeys(final String query,
            final Object... parameters);

    /**
        * Executes a SQL statement as a batch for all parameter values provided.
        * Supposed to be used for INSERT statements with an automatically generated
        * integer key.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template. At least
        *            one of the parameters needs to be an array or
        *            <code>Collection</code>. If multiple parameters are arrays or
        *            <code>Collection</code>, all of them need to have the same
        *            size.
        * 
        * @return The automatically generated key for each element of the batch.
        *         <b>Note:</b> Not all JDBC drivers support this cleanly.
        */
    long[] batchInsert(final String query, final Object... parameters);

    /**
        * Executes a SQL statement as a batch for all parameter values provided.
        * Supposed to be used for INSERT statements with one or more automatically
        * generated keys.
        * 
        * @param query  The SQL query template.
        * @param parameters  The parameters to fill into the SQL query template. At least
        *            one of the parameters needs to be an array or
        *            <code>Collection</code>. If multiple parameters are arrays or
        *            <code>Collection</code>, all of them need to have the same
        *            size.
        * 
        * @return The automatically generated keys for each element of the batch.
        *         <b>Note:</b> Not all JDBC drivers support this cleanly and it is
        *         in general driver-dependent what keys are present in the returned map.
        */
    Map<String, Object>[] batchInsertMultiKeys(final String query,
            final Object... parameters);
}
```


### Events / Registration Process Hooks

  
The script can be informed of events that occur during the registration
process. To be informed of an event, define a function in the script
file with the name specified in the table. The script can do anything it
wants within an event function. Typical things to do in event functions
include sending emails or registering data in secondary databases. Some
of the event functions can be used to control the behavior of the
registration.

This table summarizes the supported events.

#### Events Table

|Function Name|Return Value|Description|
|--- |--- |--- |
|pre_metadata_registration(DataSetRegistrationContext context)|void|Called before the openBIS AS is informed of the metadata modifications. Throwing an exception in this method aborts the transaction.|
|post_metadata_registration(DataSetRegistrationContext context)|void|The metadata has been successfully stored in the openBIS AS. This can also be a place to register data in a secondary transaction, with the semantics that any errors are ignored.|
|rollback_pre_registration(DataSetRegistrationContext context, Exception exception)|void|Called if the metadata was not successfully storedin the openBIS AS.|
|post_storage(DataSetRegistrationContext context)|void|Called once the data has been placed in the appropriate sharded directory of the store. This can only happen if the metadata was successfully registered with the AS.|
|should_retry_processing(DataSetRegistrationContext context, Exception problem)|boolean|A problem occurred with the process function, should the operation be retried? A retry happens only if this method returns true.|

Note: the `rollback_pre_registration` function is intended to handle
cases when the dropbox code finished properly, but the registration of
data in openbis failed. These kinds of problems are impossible to handle
from inside of the `process` function. The exceptions raised during the
call to the `process` function should be handled by the function itself
by catching exceptions.

#### Typical Usage Table

|Function Name|Usage|
|--- |--- |
|pre_metadata_registration(DataSetRegistrationContext context)|This event can be used as a place to register information in a secondary database. If the transaction in the secondary database does not commit, false can be returned to prevent the data from entering openBIS.|
|post_metadata_registration(DataSetRegistrationContext context)|This event can be used as a place to register information in a secondary database. Errors encountered are ignored.|
|rollback_pre_registration(DataSetRegistrationContext context, Exception exception)|Undoing a commit to a secondary transaction. Sending an email to the admin that the data set could not be stored.|
|post_storage(DataSetRegistrationContext context)|Sending an email to tell the user that the data has been successfully registered. Notifying an external system that a data set has been registered.|
|should_retry_processing(DataSetRegistrationContext context, Exception problem)|Informing openBIS if it should retry processing a data set.|

Example Scripts
---------------

A simple script that registers the incoming file as a data set
associated with a particular experiment.

**data-set-handler-basic.py**

```py
def process(transaction):
    dataSet = transaction.createNewDataSet()
    incoming = transaction.getIncoming()
    transaction.moveFile(incoming.getAbsolutePath(), dataSet)
    dataSet.setExperiment(transaction.getExperiment("/TESTGROUP/TESTPROJ/JYTHON"))
```


A script that registers the incoming file and associates it to a daily
experiment, which is created if necessary.

**data-set-handler-experiment-reg.py**

```py
from datetime import datetime
def process(transaction)
    # Try to get the experiment for today
    now_str = datetime.today().strftime('%Y%m%d')
    expid = "/TESTGROUP/TESTPROJ/" + now_str
    exp = transaction.getExperiment(expid)
    # Create an experiment
    if None == exp:
        exp = transaction.createNewExperiment(expid, "COMPOUND_HCS")
        exp.setPropertyValue("DESCRIPTION", "An experiment created on " + datetime.today().strftime('%Y-%m-%d'))
        exp.setPropertyValue("COMMENT", now_str)
    dataSet = transaction.createNewDataSet()
    incoming = transaction.getIncoming()    
    transaction.moveFile(incoming.getAbsolutePath(), dataSet)
    dataSet.setDataSetType("HCS_IMAGE")
    dataSet.setExperiment(exp)
```


Delete, Move, or Leave Alone on Error
-------------------------------------

When a problem occurs processing a file in the dropbox, the processing
is retried. This behavior can be controlled (see
[\#Errors](#Dropboxes-Errors)). If openBIS determines that it should not
retry after an error or that it cannot successfully register the
entities requested, the registration fails. It it possible to configure
what happens to a file in the dropbox if a registration fails. The
configuration can specify a behavior – delete the file, move it to an
error folder, or leave it untouched – for each of several possible
sources of errors.

By default, the file is left untouched in every case. To change this
behavior, specify an on-error-decision property on the drop box. This
has one required sub-key, "class"; other sub-keys are determined by the
class.

### Summary

-   Main Key:  
    -   on-error-decision

-   Required Sub Keys:
    -   class : The class the implements the decision

There is currently one class available :
ch.systemsx.cisd.etlserver.registrator.ConfiguredOnErrorActionDecision

This class has the following sub keys:

-   -   invalid-data-set (a data set that fails validation)
    -   validation-script-error (the validation script did not execute
        correctly)
    -   registration-error (openBIS failed to register the data set)
    -   registration-script-error (the registration script did not
        execute correctly)
    -   storage-processor-error (the storage processor reports an error)
    -   post-registration-error (an error happened after the data set
        had been registered and stored)

### Example

**plugin.properties**

```
#
# On Error Decision
#
# The class that implements the decision
on-error-decision.class = ch.systemsx.cisd.etlserver.registrator.ConfiguredOnErrorActionDecision
    
# What to do if the data set fails validation
on-error-decision.invalid-data-set = MOVE_TO_ERROR
    
# What to do if the validation script has problems
on-error-decision.validation-script-error = MOVE_TO_ERROR
    
# What to do if the openBIS does not accept the entities
on-error-decision.registration-error = MOVE_TO_ERROR
    
# What to do if the registration script has problems
on-error-decision.registration-script-error = MOVE_TO_ERROR
    
# What to do if the storage processor does not run correctly
on-error-decision.storage-processor-error = MOVE_TO_ERROR
    
# What to do if an error occurs after the entities have been registered in openBIS
on-error-decision.post-registration-error = MOVE_TO_ERROR
```


### Search

The transaction provides an interface for listing and searching for core
entities, experiment, sample, and data set.

#### API

To use the search capability, one must first retrieve the search service
from the transaction. By default the search service returns the entities
filtered to only those accessible by the user on behalf of wich, the
script is running. It is still possible to search all existing entities
by using unfiltered search service accessible from the transaction via
method getSearchServiceUnfiltered().

#### Experiment

For experiment, there is a facility for listing all experiments that
belong to a specified project.

#### Sample and Data Set

For sample and data set, a more powerful search capability is available.
This requires a bit more knowledge of the java classes, but is very
flexible. For each entity, there is a simplified method that performs a
search for samples or data sets, respectively, with a specified value
for a particular property, optionally restricted by entity type (sample
type or data set type). This provides an easy-to-use interface for a
common case. More complex searches, however, need to use the more
powerful API.

### Authorization Service

The transaction provides an interface for querying the access privileges
of a user and for filtering collections of entities down to those
visible to a user.

#### API

To use the authorization service, one must first retrieve the it from
the transaction.

### Example

#### Combined Example

In this example, we create a data set, list experiments belonging to a
project, search for samples, search for data sets, and assign the
experiment, sample, and parent data sets based on the results of the
searches.

**data-set-handler-with-search.py**

```py
def process(tr):
    data_set = tr.createNewDataSet()
    incoming = tr.getIncoming()
    tr.moveFile(incoming.getAbsolutePath(), data_set)
    # Get the search service
    search_service = tr.getSearchService()

    # List all experiments in a project
    experiments = search_service.listExperiments("/cisd/noe")

    # Search for all samples with a property value determined by the file name; we don't care about the type
    samplePropValue = incoming.getName()
    samples = search_service.searchForSamples("ORGANISM", samplePropValue, None)

    # If possible, set the owner to the first sample, otherwise the first experiment
    if samples.size() > 0:
        data_set.setSample(samples[0])
    else:
        data_set.setExperiment(experiments[0])

    # Search for any potential parent data sets and use them as parents
    parent_data_sets = search_service.searchForDataSets("COMMENT", "no comment", "HCS_IMAGE")
    parent_data_set_codes = map(lambda each : each.getDataSetCode(), parent_data_sets)
    data_set.setParentDatasets(parent_data_set_codes)
```


An example from the Deep Sequencing environment handling BAM files:

**data-set-handler-alignment.py**

```py
'''
This is handling bowtie-BAM files and extracts some properties from the BAM header and
the samtools flagstat command. The results are formatted and attached  as a property
to the openBIS DataSet.
Prerequisites are the DataSetType: ALIGNMENT and
the following properties assigned to the DataSetType mentioned above:
ALIGNMENT_SOFTWARE, ISSUED_COMMAND, SAMTOOLS_FLAGSTAT,
TOTAL_READS, MAPPED_READS
Obviously you need a working samtools binary
Note:
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''
import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
FOLDER='/net/bs-dsu-data/array0/dsu/dss/incoming-jython-alignment/'
SAMTOOLS='/usr/local/dsu/samtools/samtools'
def process(transaction):
    incoming = transaction.getIncoming()
    # Create a data set and set type
    dataSet = transaction.createNewDataSet("ALIGNMENT")
    dataSet.setMeasuredData(False)
    incomingPath = incoming.getAbsolutePath()
    # Get the incoming name
    name = incoming.getName()
    # expected incoming Name, e.g.:ETHZ_BSSE_110429_63558AAXX_1_sorted.bam
    split = name.split("_")
    sample=split[2]+ '_'+ split[3] + ':' + split[4]
    # Extract values from a samtools view and set the results as DataSet properties
    # Command: samtools view -H ETHZ_BSSE_110429_63558AAXX_1_sorted.bam
    arguments = SAMTOOLS + ' view -H ' + FOLDER + name
    #print('Arguments: '+ arguments)
    cmdResult = os.popen(arguments).read()
    properties = cmdResult.split("\n")[-2].split('\t')
    aligner = (properties[1].split(':')[1].upper() +  '_' + properties[2].split(':')[1])
    command = properties[3]
    arguments = SAMTOOLS + ' flagstat ' + FOLDER + name
    cmdResult = os.popen(arguments).read()
    totalReads = cmdResult.split('\n')[0].split(' ')[0]
    mappedReads = cmdResult.split('\n')[2].split(' ')[0]
    dataSet.setPropertyValue("ALIGNMENT_SOFTWARE", aligner)
    dataSet.setPropertyValue("ISSUED_COMMAND", command)
    dataSet.setPropertyValue("SAMTOOLS_FLAGSTAT", cmdResult)
    dataSet.setPropertyValue("TOTAL_READS", totalReads)
    dataSet.setPropertyValue("MAPPED_READS", mappedReads)
    # Add the incoming file into the data set
    transaction.moveFile(incomingPath, dataSet)
    # Get the search service
    search_service = transaction.getSearchService()
    # Search for the sample
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample));
    foundSamples = search_service.searchForSamples(sc)
    if foundSamples.size() > 0:
        dataSet.setSample(foundSamples[0])
```


Error Handling
--------------

### Automatic Retry (auto recovery)

OpenBIS has a complex mechanism to ensure that the data registration via
dropboxes is atomic. When error occurs during data registration, the
dropbox will try several times before it gives up on the process. The
retries can happen to the initial processing of the data, as well as to
the registration in application server. Even if these fail there is
still a chance to finish the registration. If the registration reaches
the certain level it stores the checkpoint on the disk. If at any point
the process fails, or the dss goes down it tries to recover from the
checkpoint.

There are two types of checkpoint files: State files and marker files.
There are stored in two different directories. The default location for
the state files is `datastore_sever/recovery-state`. This can be changed
by the property `dss-recovery-state-dir`  in DSS `service.properties`.
The default location for the marker files was
`<store location>/<share id>/recovery-marker`. This may lead to problems
if this local is remote. Since version 20.10.6 the default location is
 `datastore_sever/recovery-marker-dir`. This can be changed by the
property `dss-recovery-marker-dir`  in DSS `service.properties`. 

The `process` function will be retried if a
`should_retry_processing` function is defined in the dropbox script and
it returns true. There are two configuration settings that affect this
behavior. The setting `process-max-retry-count` limits the number of
times the process function can be retried. The number of times to retry
before giving up and the waiting periods are defined using properties
shown in the table below.

IMPORTANT NOTE: Please note, that the registration is considered as
failed only after, the whole retrying / recovery process will fail. It
means that it can take a long time before the .faulty\_paths file is
created, even when there is a simple dropbox error.

Therefor during development of a dropbox we recommend using **[development mode](./dss-dropboxes.md#development-mode)** , wich basically sets all retry values to 0, thus disabling the auto-recovery feature.

|Key|Default Value|Meaning|
|--- |--- |--- |
|process-max-retry-count|6|The maximum number of times the process function can be retried.|
|process-retry-pause-in-sec|300|The amount of time to wait between retries of the process function.|
|metadata-registration-max-retry-count|6|The number of times registering metadata with the server can be retried.|
|metadata-registration-retry-pause-in-sec|300|The number of times registering metadata with the server can be retried.|
|recovery-max-retry-count|50|The number of times the recovery from checkpoint can be retries.|
|recovery-min-retry-period|60|The amount of time to wait between recovery from checkpoint retries.|

![image info](img/771.png)

### Manual Recovery

The registration of data sets with Jython dropboxes has been designed to
be quite robust. Nonetheless, there are situations in which problems may
arise. This can especially be a problem during the development of
dropboxes. Here are the locations and semantics of several important
files and folders that can be useful for debugging a dropbox.

|File or Folder|Meaning|
|--- |--- |
|datastore_server/log-registrations|Keeps logs of registrations. See the registration log documentation for more information.|
|[store]/[share]/pre-staging|Contains hard-link copies of the original data. Dropbox process operate on these hardlink copies.|
|[store]/[share]/staging|The location used to prepare data sets for registration.|
|[store]/[share]/pre-commit|Where data from data sets are kept while register the metadata with the AS. Once metadata registration succeeds, files are moved from this folder into the final store directory.|
|[store]/[share]/recovery-marker (before version 20.10.6)
datastore_sever/recovery-marker-dir (since version 20.10.6)|Directories, one per dropbox, where marker files are kept that indicate that a recovery should happen on an incoming file if it is reprocessed. Deleting a marker file will force the incoming file to be processed as a new file, not a recovery.| 

Classpath / Configuration
-------------------------

If you want other jython modules to be available to the code that
implements the drop box, you will need to modify the
datastore\_server.conf file and add something like

`-Dpython.path=data/dropboxes/scripts:lib/jython-lib`

To the JAVA\_OPTS environment variable. The line should now look
something like this:

`JAVA_OPTS=${JAVA_OPTS:=-server -d64 -Dpython.path=data/dropboxes/scripts:lib/jython-lib}`

If the Jython dropbox need third-party JAR files they have to be added
to the core plugin in a sub-folder `lib/`.

Validation scripts
------------------

See [Jython DataSetValidator](../../uncategorized/jython-datasetvalidator.md).

Global Thread Parameters
------------------------

If you want to write a drop box which uses some parameters defined in
the service.properties you can access those properties via
the `getGlobalState`. Here we show an example how to use:

**Global tread properties**

```py
def getThreadProperties(transaction):
    threadPropertyDict = {}
    threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
    for key in threadProperties:
    try:
        threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
        pass
    return threadPropertyDict

# You can later access the thread properties like this:
threadPropertyDict = getThreadProperties(transaction)
incomingRootDir = threadPropertyDict[u'incoming-root-dir']
```


Sending Emails from a Drop box
------------------------------

```py
def post_storage(context):
    mailClient = context.getGlobalState().getMailClient()
    results = context.getPersistentMap().get(PERSISTANT_KEY_MAP)
    sendEmail(mailClient, results[0]) 

def process(transaction):
    transaction.getRegistrationContext().getPersistentMap().put(PERSISTANT_KEY_MAP, [fcId])
```


Java Dropboxes
--------------

The above examples show how to implement dropboxes in Python. Python,
however, is not the only language option: it is also possible to write
dropboxes in Java. Whereas Python has the advantage of short turnaround
and less verbose syntax, Java is a good choice in the dropbox employs
complex logic and/or does not need to be modified frequently. A natural
progression is to use Python at the beginning, when creating a new
dropbox, to take advantage of the short turnaround cycle and then move
to Java once the dropbox implementation becomes more stable. Since the
API is the same, this language transition process is quite painless.

### Configuration

As with other dropboxes, a Java dropbox should be deployed as a core-plugin.

**plugin.properties**

```
#
# REQUIRED PARAMETERS
#
# The directory to watch for new data sets
incoming-dir = ${root-dir}/incoming-java-dropbox

# The handler class. Must be either ch.systemsx.cisd.etlserver.registrator.api.v2.JavaTopLevelDataSetHandlerV2 or a subclass thereof
top-level-data-set-handler = ch.systemsx.cisd.etlserver.registrator.api.v2.JavaTopLevelDataSetHandlerV2

# The class that implements the dropbox (must implement ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2)
program-class = ch.systemsx.cisd.etlserver.registrator.api.v2.ExampleJavaDataSetRegistrationDropboxV2

# The appropriate storage processor
storage-processor = ch.systemsx.cisd.etlserver.DefaultStorageProcessor

#
# OPTIONAL PARAMETERS
#
    
# False if incoming directory is assumed to exist.
# Default - true: Incoming directory will be created on start up if it doesn't exist.
incoming-dir-create = true
```


The program-class parameter specifies the class that implements the
logic of the dropbox. This class must implement the
IJavaDataSetRegistrationDropboxV2 interface. This class, and any other
code it uses, should be packaged in a jar file that is provided with the
core-plugin. The name of the jar file can be freely chosen.

### Implementation

To implement a dropbox in Java, implement
the IJavaDataSetRegistrationDropboxV2 interface, which codifies the
interaction between the datastore server and the dropbox. We recommend
subclassing AbstractJavaDataSetRegistrationDropboxV2 to bootstrap the
implementation of this interface.

**IJavaDataSetRegistrationDropboxV2**

```java
/**
    * The interface that V2 dropboxes must implement. Defines the process method, which is called to
    * handle new data in the dropbox's incoming folder, and various event methods called as the
    * registration process progresses.
    * 
    * @author Pawel Glyzewski
    */
public interface IJavaDataSetRegistrationDropboxV2
{
    /**
        * Invoked when new data is found in the incoming folder. Implements the logic of registering
        * and modifying entities.
        * 
        * @param transaction The transaction that offers methods for registering and modifying entities
        *            and performing operations on the file system.
        */
    public void process(IDataSetRegistrationTransactionV2 transaction);
    /**
        * Invoked just before the metadata is registered with the openBIS AS. Gives dropbox
        * implementations an opportunity to perform additional operations. If an exception is thrown in
        * this method, the transaction is rolledback.
        * 
        * @param context Context of the registration. Offers access to the global state and persistent
        *            map.
        */
    public void preMetadataRegistration(DataSetRegistrationContext context);
    /**
        * Invoked if the transaction is rolledback before the metadata is registered with the openBIS
        * AS.
        * 
        * @param context Context of the registration. Offers access to the global state and persistent
        *            map.
        * @param throwable The throwable that triggered rollback.
        */
    public void rollbackPreRegistration(DataSetRegistrationContext context, Throwable throwable);
    /**
        * Invoked just after the metadata is registered with the openBIS AS. Gives dropbox
        * implementations an opportunity to perform additional operations. If an exception is thrown in
        * this method, it is logged but otherwise ignored.
        * 
        * @param context Context of the registration. Offers access to the global state and persistent
        *            map.
        */
    public void postMetadataRegistration(DataSetRegistrationContext context);
    /**
        * Invoked after the data has been stored in its final location on the file system and the
        * storage has been confirmed with the AS.
        * 
        * @param context Context of the registration. Offers access to the global state and persistent
        *            map.
        */
    public void postStorage(DataSetRegistrationContext context);
    /**
        * Is a function defined that can be used to check if a failed registration should be retried?
        * Primarily for use implementations of this interface that dispatch to dynamic languages.
        * 
        * @return true shouldRetryProcessing is defined, false otherwise.
        */
    public boolean isRetryFunctionDefined();
    /**
        * Given the problem with registration, should it be retried?
        * 
        * @param context Context of the registration. Offers access to the global state and persistent
        *            map.
        * @param problem The exception that caused the registration to fail.
        * @return true if the registration should be retried.
        */
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
            throws NotImplementedException;
}
```


Sending Emails in a drop box (simple)
-------------------------------------

```py
from ch.systemsx.cisd.common.mail import EMailAddress
```

```py
def process(transaction):
    replyTo = EMailAddress("manuel.kohler@id.ethz.ch")
    fromAddress = replyTo
    recipient1 = EMailAddress("recipient1@ethz.ch")
    recipient2 = EMailAddress("recipient2@ethz.ch")

    transaction.getGlobalState().getMailClient().sendEmailMessage("This is the subject", \
                "This is the body", replyTo, fromAddress, recipient1, recipient2);
```


### Java Dropbox Example

This is a simple example of a pure-java dropbox that creates a sample
and registers the incoming file as a data set of this sample.

**ExampleJavaDataSetRegistrationDropboxV2.java**

```java
package ch.systemsx.cisd.etlserver.registrator.api.v2;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
/**
    * An example dropbox implemented in Java.
    * 
    * @author Chandrasekhar Ramakrishnan
    */
public class ExampleJavaDataSetRegistrationDropboxV2 extends
        AbstractJavaDataSetRegistrationDropboxV2
{
    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        String sampleId = "/CISD/JAVA-TEST";
        ISample sample = transaction.createNewSample(sampleId, "DYNAMIC_PLATE");
        IExperimentImmutable exp = transaction.getExperiment("/CISD/NEMO/EXP-TEST-1");
        sample.setExperiment(exp);
        IDataSet dataSet = transaction.createNewDataSet();
        dataSet.setSample(sample);
        transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet);
    }
}
```


Java Code location

The Java file should go into a `lib` folder and should be wrapped as a
`jar`. The name does not matter.

While building a jar, the project should have the following
dependencies: `openBIS-API-dropbox-<version>.jar`,
`lib-commonbase-<version>.jar` and `cisd-hotdeploy-13.01.0.jar`. The
first two are available in the distribution in the archives
`openBIS-API-commonbase-<version>.zip` and
`openBIS-API-dropbox-<version>.zip`, the third one is available in [the Ivy repo](https://sissource.ethz.ch/openbis/openbis-public/openbis-ivy/-/blob/main/cisd/cisd-hotdeploy/13.01.0/cisd-hotdeploy-13.01.0.jar).

Example path where the created `jar` should reside:

`servers/core-plugins/illumina-ngs/2/dss/drop-boxes/register-cluster-alignment-java/lib`

Create a `jar` from your java dropbox file:

`jar cvf foo.jar foo.java`

Restart the DSS

Calling an Aggregation Service from a drop box
----------------------------------------------

**drop box code**

```py
'''
@author:
Manuel Kohler
'''
from ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService import createQueryApiServer
```

     
```py
def process(transaction):
    # use the etl server session token
    session_token = transaction.getOpenBisServiceSessionToken()

    # To find out do SQL on the openBIS DB: select code from data_stores;
    dss = "STANDARD"

    # folder name under the reporting_plugins
    service_key = "reporting_experimental"   

    # some parameters which are handed over
    d = {"param1": "hello", "param2": "from a drop box"}

    # connection to the openbis server returns IQueryApiServer
    s = createQueryApiServer("http://127.0.0.1:8888/openbis/openbis/", "600")

    # Actual call
    # Parameters: String sessionToken, String dataStoreCode,String serviceKey, Map<String, Object> parameters)
    s.createReportFromAggregationService(session_token, dss, service_key, d)
```


Known limitations
-----------------

#### Blocking

Registering/updating a large number of entities can cause other
concurrent operations that try to modify the same or related entities to
be blocked. This limitation applies to both dropboxes and batch
operations triggered from the web UI. Lists of operations that are
blocked are presented below. Each list contains operations that cannot
be performed when a specific kind of entity is being registered/updated.

Experiment:

-   creating/updating an experiment in the same project
-   updating the same space
-   updating the same project
-   updating the same experiment

Sample:

-   creating/updating an experiment in the same project
-   creating/updating a sample in the same experiment
-   updating the same space
-   updating the same project
-   updating the same experiment
-   updating the same sample

Data set:

-   creating/updating an experiment in the same project
-   creating/updating a sample in the same experiment
-   creating a dataset in the same experiment
-   updating the same space
-   updating the same project
-   updating the same experiment
-   updating the same sample

Material:

-   updating the same material
