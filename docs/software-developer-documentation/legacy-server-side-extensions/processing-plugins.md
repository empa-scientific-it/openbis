Processing Plugins
==================

## Introduction

A processing plugin runs on the DSS. It processes a specified set of data sets. The user can trigger a processing plugin in the openBIS Web application. After processing an e-mail is sent to the user.

A processing plugin is configured on the DSS best by introducing a [core plugin](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html) of type `processing-plugins`. All processing plugins have the following properties in common:

|Property Key|Description|
|--- |--- |
|class|The fully-qualified Java class name of the reporting plugin. The class has to implement IProcessingPluginTask.|
|label|The label. It will be shown in the GUI.|
|dataset-types|Comma-separated list of regular expressions. The plugin can process only data sets of types matching one of the regular expressions.  If new data set types are registered with openBIS, the DSS will need to be restarted before the new data set types are known to the processing plugins.|
|properties-file|Path to an optional file with additional properties.|
|allowed-api-parameter-classes|A comma-separated list of regular expression for fully-qualified class names. Any classes matching on of the regular expressions is allowed as a class of a Java parameter object of a remote API call. For more details see API Security.|
|disallowed-api-parameter-classes|A comma-separated list of regular expression for fully-qualified class names. Any classes matching on of the regular expressions is not allowed as a class of a Java parameter object of a remote API call. For more details see API Security.|

## Multiple Processing Queues

By default only one processing plugin task is processed. All other
scheduled tasks have to wait in a queue. This can be inconvenient if
there is a mixture of long tasks (taking hours or even days) and short
tasks (taking only seconds or minutes).

DSS can be configured two run more than one processing queue. Each queue
(except the default one) has a name (which also appears in the log
file). Also a regular expression is associated with the queue. When a
processing plugin task is scheduled the appropriate queue is selected by
the ID of the processing plugin (this is either a name in the
property `processing-plugins` of `service.properties` of DSS or the name
of the core-plugin folder). If the ID matches the regular expression the
task is added to the corresponding queue. If non of the regular
expression matches the default queue is used.

The queues have to be specified by the
property `data-set-command-queue-mapping`. It contains a comma-separated
list of queue definitions. Each definition has the form 

`<queue name>:<regular expression>`

### Archiving

If archiving is enable (i.e. `archiver.class` in `service.properties` of
DSS is defined or a core-plugin of type `miscellaneous` with
ID `archiver` is defined) there will be three processing plugins with
the following IDs: `Archiving`, `Copying data sets to archive`, and
`Unarchiving`

## Generic Processing Plugins

### RevokeLDAPUserAccessMaintenanceTask

```{note}
This Maintenance Task should only be used if the server uses
LDAP only, it will take users from other authentication services as
missing.
```

**Description**: Renames, deactivates and delete all roles from users
that are no longer available on LDAP following the next algorithm.

-   Grabs all active users.
-   The users that follow all the points of the next criteria are
    renamed to userId-YYYY.MM.DD and deactivated:  
    -   Are not a system user.
    -   Don't have the ETL\_SERVER role.
    -   Don't have a LDAP principal.

**Configuration**:

|Property Key|Description|
|--- |--- |
|server-url|LDAP server URL.|
|security-principal-distinguished-name|LDAP principal distinguished name.|
|security-principal-password|LDAP principal password.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.generic.server.task.RevokeLDAPUserAccessMaintenanceTask
interval = 60 s
server-url = ldap://d.ethz.ch/DC=d,DC=ethz,DC=ch
security-principal-distinguished-name = CN=cisd-helpdesk,OU=EthUsers,DC=d,DC=ethz,DC=ch
security-principal-password = ******
```


### DataSetCopierForUsers

### DataSetCopier

**Description**: Copies all files of the specified data sets to another (remote) folder. The actual copying is done by the rsync command.

**Configuration**:

|Property Key|Description|
|--- |--- |
|destination|Path to the destination folder. This can be a path to a local/mounted folder or to a remote folder accessible via SSH. In this case the name of the host has to appear as a prefix. General syntax: [<host>:][<rsync module>:]<path>|
|hard-link-copy|If true hard links are created for each file of the data sets. This works only if the share which stores the data set is in the same local file system as the destination folder. Default: false.|
|rename-to-dataset-code|If true the copied data set will be renamed to the data set code. Default: false.|
|rsync-executable|Optional path to the executable command rsync.|
|rsync-password-file|Path to the rsync password file. It is only needed if an rsync module is used.|
|ssh-executable|Optional path to the executable command ssh. SSH is only needed for not-mounted folders which are accessible via SSH.|
|ln-executable|Optional path to the executable command ln. The ln command is only needed when hard-link-copy = true.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier
label = Copy to analysis incoming folder
dataset-types = MS_DATA, UNKNOWN
destination = analysis-server:analysis-incoming-data
rename-to-dataset-code = true 
```


### DataSetCopierForUsers

**Description**: Copies all files of the specified data sets to a
(remote) user folder. The actual copying is done by the rsync command.

**Configuration**:

|Property Key|Description|
|--- |--- |
|destination|Path template to the destination folder. It should contain ${user} as a placeholder for the user ID.
The path can point to a local/mounted folder or to a remote folder accessible via SSH. In this case the name of the host has to appear as a prefix. General syntax: [<host>:][<rsync module>:]<path>|
|hard-link-copy|If true hard links are created for each file of the data sets. This works only if the share which stores the data set is in the same local file system as the destination folder. Default: false.|
|rename-to-dataset-code|If true the copied data set will be renamed to the data set code. Default: false.|
|rsync-executable|Optional path to the executable command rsync.|
|rsync-password-file|Path to the rsync password file. It is only needed if an rsync module is used.|
|ssh-executable|Optional path to the executable command ssh. SSH is only needed for not-mounted folders which are accessible via SSH.|
|ln-executable|Optional path to the executable command ln. The ln command is only needed when hard-link-copy = true.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopierForUsers
label = Copy to user playground
dataset-types = MS_DATA, UNKNOWN
destination = tmp/playground/${user}/data-sets
hard-link-copy = true
rename-to-dataset-code = true 
```


### JythonBasedProcessingPlugin

**Description**: Invokes a Jython script to do the processing. For more
details see [Jython-based Reporting and Processing
Plugins](https://unlimited.ethz.ch/display/openBISDoc2010/Jython-based+Reporting+and+Processing+Plugins).

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|


**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedProcessingPlugin
label = Calculate some numbers
dataset-types = MS_DATA, UNKNOWN
script-path = script.py
```


### ReportingBasedProcessingPlugin

**Description**: Runs a Jython-based reporting plugin of type
TABLE\_MODEL and sends the result table as a TSV file to the user.

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|
|single-report|If true only one report will be sent. Otherwise a report for each data set will be sent. Default: false|
|email-subject|Subject of the e-mail to be sent. Default: None|
|email-body|Body of the e-mail to be sent. Default: None|
|attachment-name|Name of the attached TSV file. Default: report.txt|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.ReportingBasedProcessingPlugin
label = Create monthly report
dataset-types = MS_DATA, UNKNOWN
script-path = script.py
email-subject = DSS Monthly Report 
```


### DataSetAndPathInfoDBConsistencyCheckProcessingPlugin

**Description**: The processing task checks the consistency between the
data store and the meta information stored in the `PathInfoDB`. It will
check for:

-   existence (i.e. exists in PathInfoDB but not on file system or
    exists on file system but not in PathInfoDB)
-   file size
-   CRC32 checksum

If it finds any deviations, it will send out an email which contains all differences found.

**Configuration**: Properties common for all processing plugins (see Introduction)

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetAndPathInfoDBConsistencyCheckProcessingPlugin
label = Check consistency between data store and path info database
dataset-types = .*
creening Processing Plugins
```


### ScreeningReportingBasedProcessingPlugin

**Description**: Runs a Jython-based reporting plugin of type
TABLE\_MODEL and sends the result table as a TSV file to the user. There
is some extra support for screening.

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|
|single-report|If true only one report will be sent. Otherwise a report for each data set will be sent. Default: false|
|email-subject|Subject of the e-mail to be sent. Default: None|
|email-body|Body of the e-mail to be sent. Default: None|
|attachment-name|Name of the attached TSV file. Default: report.txt|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningReportingBasedProcessingPlugin
label = Create monthly report
dataset-types = HCS_IMAGE
script-path = script.py
email-subject = DSS Monthly Report 
```
