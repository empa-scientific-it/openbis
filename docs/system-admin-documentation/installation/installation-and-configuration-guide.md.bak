Installation and Administrator Guide of the openBIS Server
==========================================================

## System Requirements

The minimal requirements of a system running openBIS are:

- Operating System: Linux / MacOS X (The binaries: `bash`, `awk`, `sed`, `unzip` need to be installed and in the `PATH` of the openBIS user.)
- Java Runtime Environment: recent versions of Oracle JRE 11
- PostgreSQL 11

We find Linux to be the easiest choice to get an openBIS server running quickly.

For recommended memory settings, see [Recommended CPU and memory settings for openBIS 20.10](https://openbis.readthedocs.io/en/latest/system-admin-documentation/installation/system-requirements.html).

An SMTP server needs to be accessible and configured if you want to obtain notifications.

## Installation

The server distribution is a `gzipped` `tar` file named `openBIS-installation-standard-technologies-<version>` `.tar.gz`. It contains:

- `console.properties:` configuration file for a console/non-gui installation

- `extract.sh:` helper script for installation

- `jul.config:` Log configuration for the openBIS install process

- `openBIS-installer.jar` Java archive containing openBIS

- `run-console.sh` Installation script for console/non-gui installation

- `run-ui.sh` Installation script for gui installation

### Installation steps

1. Create a service user account, i.e. an unprivileged, regular user account. **Do not run openBIS as root!**

2. Gunzip the distribution on the server machine into some temporary folder.

3. Run either the console/non-gui installation script or the gui installation script:

    **GUI-based installation**

    ```bash
    tar xvfz openBIS-installation-standard-technologies-S139.0-r26480.tar.gz
    cd openBIS-installation-standard-technologies-S139.0-r26480
    ./run-ui.sh
    ```

    In the non-gui version you have to edit the `console.properties`
    files:

    **Non-GUI installation**

    ```bash
    tar xvfz openBIS-installation-standard-technologies-S139.0-r26480.tar.gz
    cd openBIS-installation-standard-technologies-S139.0-r26480
    vi console.properties
    ./run-console.sh
    ```
    
    ```{note}
    Please be aware that the directory where openbis is installed should not already exist. Users should specify the directory where they want to install openBIS (in the console.properties or in the graphical installer) and this directory will be created by the installation procedure. If the directory already exists, the installation will fail.
    ```

After the successful installation you should have a look at the configuration file called s`ervice.properties`. It is located in `<server_folder>openBIS-server/jetty/etc/`

This file is a an [Extended Properties File](https://openbis.readthedocs.io/en/latest/system-admin-documentation/installation/optional-application-server-configuration.html). Here is an example which can be used as a template:

**service.properties**

```bash
# ---------------------------------------------------------------------------
# Database configuration
# ---------------------------------------------------------------------------
# The database instance local unique identifier. Used when the new database is created.
database-instance = DEFAULT

# Supported: currently only 'postgresql' is supported
database.engine = postgresql
database.url-host-part =
database.kind = prod
# User who owns the database. Default: Operating system user running the server.
database.owner =
database.owner-password =
# Superuser of the database. Default: database-dependent.
database.admin-user = 
database.admin-password =
# Max. number of active database connections. Default: 20. 
database.max-active-connections =
# Max. number of idle database connections to keep open. Default: 20. 
database.max-idle-connections = 
# Log interval (in seconds) between two regular log entries of the number of active database 
# connections. Default: 3600s.
database.active-connections-log-interval = 

# ---------------------------------------------------------------------------
# Master data by Excel sheets
# ---------------------------------------------------------------------------
# Path to the file which stores version information of master data imported from Excel sheets. 
# Default value: ../../../xls-import-version-info.json. The file will be created. 
# It should be <openbis installation path>/servers/openBIS-server. 
# Note, that the folder containing this file has to exist. 
# xls-import.version-data-file = ../../../xls-import-version-info.json

# ---------------------------------------------------------------------------
# Authentication configuration
# ---------------------------------------------------------------------------
# Supported Authentication options are:
# 'file-authentication-service'
# 'ldap-authentication-service'
# 'crowd-authentication-service'
# 'file-crowd-authentication-service'
# 'file-ldap-authentication-service'
# 'ldap-crowd-authentication-service'
# 'file-ldap-crowd-caching-authentication-service'
# For a detailed description, have a look at the Installation and Administrator
# Guide of the openBIS Server: https://wiki-bsse.ethz.ch/x/oYIUBQ 
authentication-service = file-ldap-crowd-caching-authentication-service

# ---------------------------------------------------------------------------
# Caching configuration (only used with 'file-ldap-crowd-caching-authentication-service')
# ---------------------------------------------------------------------------
# The time that the authentication cache keeps entries. Default: 28h
authentication.cache.time = 28h
# The time that the authentication cache does not perform re-validation on a cache entry. 
# Default: 1h
authentication.cache.time-no-revalidation = 1h

# ---------------------------------------------------------------------------
# Crowd configuration
# ---------------------------------------------------------------------------
#
# The Crowd host.
# Mandatory.
crowd.service.host = 
# The Crowd service port. Default: 443
crowd.service.port =
# The timeout (in s) to wait for a Crowd query to return, -1 for "wait indefinitely". Default: 10s. 
crowd.service.timeout =
# The Crowd application name. The value 'openbis' is just a suggestion.
# Mandatory. 
crowd.application.name = openbis
# The Crowd application password. 
# Mandatory.
crowd.application.password =

# ---------------------------------------------------------------------------
# LDAP configuration
# ---------------------------------------------------------------------------
# The space-separated URLs of the LDAP servers, e.g. "ldap://d.ethz.ch/DC=d,DC=ethz,DC=ch". 
# Mandatory.
ldap.server.url = 
# The distinguished name of the security principal, e.g. "CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch".
# Mandatory.
ldap.security.principal.distinguished.name = 
# Password of the LDAP user account that will be used to login to the LDAP server to perform the queries. 
# Mandatory.
ldap.security.principal.password = 
# The security protocol to use, use "ssl" or "none", default is "ssl"
ldap.security.protocol =
# The authentication method to use: "none" (no authentication), "simple", "strong" (SASL), defaults to "simple"
ldap.security.authentication-method =
# The referral mode:
# "follow" - follow referrals automatically (the default)
# "ignore" - ignore referrals
# "throw" - throw ReferralException when a referral is encountered
ldap.referral =
# The attribute name for the user id, defaults to "uid"
ldap.attributenames.user.id =
# The attribute name for the email, defaults to "mail"
ldap.attributenames.email =
# The attribute name for the first name, defaults to "givenName"
ldap.attributenames.first.name =
# The attribute name for the last name, defaults to "sn"
ldap.attributenames.last.name =
# Set to true to also query for email aliases
ldap.queryEmailForAliases = true
# The query template, needs to contain %s which will be filled with the query term, e.g. uid=username
# The default is:
# ldap.queryTemplate = (&(objectClass=organizationalPerson)(objectCategory=person)(objectClass=user)(%s))
# which is known to work for many Active Directory installations.
# For OpenLDAP, replace by: 
# ldap.queryTemplate = (&(%s))
# For restriction to BSSE accounts in OpenLDAP, set to: 
# ldap.queryTemplate = (&(objectClass=bssePosixAccount)(%s))
ldap.queryTemplate =
# The number of times a failed LDAP query is retried at the max. Default: 1.
ldap.maxRetries = 
# The timeout (in s) to wait for an LDAP query to return, -1 for "wait indefinitely". Default: 10s. 
ldap.timeout = 
# The time (in s) to wait after a failure before retrying the query. Default: 10s. 
ldap.timeToWaitAfterFailure =

# ---------------------------------------------------------------------------
# Anonymous login configuration (optional)
# ---------------------------------------------------------------------------
# Login of the existing user whose settings will be used for anonymous login 
#user-for-anonymous-login = <user-login>

# ---------------------------------------------------------------------------
# Project authorization
# ---------------------------------------------------------------------------
# Enabled if set to 'true'. Default: disabled 
authorization.project-level.enabled = true
# Regular expression for user ids allowed to have a project role 
authorization.project-level.users = .*

# ---------------------------------------------------------------------------
# Project samples
# ---------------------------------------------------------------------------
# Enabled if set to 'true'. Default: disabled 
# Note: Changing to 'true' turns experiment samples to project samples 
# which can not be reverted after setting this flag back to 'false'. Also
# the sample identifier will change for such samples.
#project-samples-enabled = true

# ---------------------------------------------------------------------------
# Client configuration
# ---------------------------------------------------------------------------
# Name of the file that stores Web Client configuration
web-client-configuration-file = etc/web-client.properties

# A comma-separated list of trusted cross-origin domains, that are allowed to
# query openBIS content. Typically these are lightweight webapps that integrate with openBIS 
# via JSON-RPC services, but are not directly hosted within the openBIS application.
# 
# Example 1 (two different domains configured):
# 
# trusted-cross-origin-domains=https://myapp.domain.com:8443, http://other.domain.com
#
# Example 2 (match every domain):
#
# trusted-cross-origin-domains= *
#
# The '*' matches any arbitrary domain. It should be used with care as it opens openBIS 
# for potential cross-site scripting attacks.
#
#trusted-cross-origin-domains=

# ---------------------------------------------------------------------------
# Session configuration
# ---------------------------------------------------------------------------
# The time after which an inactive session is expired by the service (in minutes).
session-timeout = 720

# Session time (in minutes) in case of presents of file etc/nologin.html. Should be < 30. 
#session-timeout-no-login = 10

# Maximum number of sessions allowed per user. Zero means unlimited number of sessions. Default value is 1.
# max-number-of-sessions-per-user = 1

# Comma separated list of users allowed to have unlimited number of sessions. Default: Empty list.
# Note: The DSS (user 'etlserver' by default, see property 'username' of DSS service.properties)
# should be added to this list.
# users-with-unrestricted-number-of-sessions = 


# ---------------------------------------------------------------------------
# Business rules configuration
# ---------------------------------------------------------------------------
# When set to "true" enables the system to store material codes containing non-alphanumeric characters.
# Regardless of the value of this property no white spaces are allowed in the material codes.
#material-relax-code-constraints=false

# Comma-separated list of regular expression of data set types which do not require that the data set 
# is linked to an experiment. If not linked to an experiment a link to a sample with space is required.
data-set-types-with-no-experiment-needed = .*

# When set to 'true' the sequence of sample codes is gap less for each type if all samples are created by
# batch registrations. 
#create-continuous-sample-codes = false


# ---------------------------------------------------------------------------
# RPC Dropbox Default DSS configuration 
# ---------------------------------------------------------------------------
# Set this to the DSS code of the DSS handling RPC Dropboxes for this user.
# Note: This is only required if more than one DSS is connected to this openBIS server.
dss-rpc.put.dss-code =

# ---------------------------------------------------------------------------
# Hibernate Search
# ---------------------------------------------------------------------------
# The working directory.
hibernate.search.index-base = ./indices
# One of NO_INDEX, SKIP_IF_MARKER_FOUND, INDEX_FROM_SCRATCH.
# If not specified, default (SKIP_IF_MARKER_FOUND) is taken.
hibernate.search.index-mode = SKIP_IF_MARKER_FOUND
# Defines the maximum number of elements indexed before flushing the transaction-bound queue.
# Default is 1000.
hibernate.search.batch-size = 1000
# Maximum number of search results
hibernate.search.maxResults = 100000
# If 'async', the update of indices will be done in a separate thread.
hibernate.search.worker.execution=async
# How long fulltext searches can take (in seconds) before they are timed out. 
# When not defined, there is no timeout.
# fulltext-timeout = 30

# ---------------------------------------------------------------------------
# Online Help
# ---------------------------------------------------------------------------
# Online help is broken into two sections -- generic and specific. Generic help links back to
# the CISD. Specific help is provided by the host of the installation
#
# OpenBIS needs to know the root URL for the online help and a template for the individual pages.
# The template should have on parameter, called title, and should be constructed to automatically
# create the page if it does not already exist.
# The template can be created by going to the root page, adding a new link to the page, and
# replacing the title of the new page with the ${title}
onlinehelp.generic.root-url = https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp
onlinehelp.generic.page-template = https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title}&linkCreation=true&fromPageId=40633829
#onlinehelp.specific.root-url = https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp
#onlinehelp.specific.page-template = https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title}&linkCreation=true&fromPageId=40633829

# ---------------------------------------------------------------------------
# JMX memory monitor
# ---------------------------------------------------------------------------
# Interval between two runs of the memory monitor (in seconds). 
# Set to -1 to disable the memory monitor.
memorymonitor-monitoring-interval = 60
# Interval between two regular log call of the memory monitor (in seconds).
# Set to -1 to disable regular memory usage logging. 
memorymonitor-log-interval = 3600
# The percentage of memory that, if exceeded, triggers a notify log of the memory manager, 
# Set to 100 to disable.
memorymonitor-high-watermark-percent = 90

# ---------------------------------------------------------------------------
# Database Configurations for Query module (optional)
# ---------------------------------------------------------------------------
# Comma separated keys of databases configured for Query module.
# Each database should have configuration properties prefixed with its key.
# Mandatory properties for each <database> include: 
#   <database>.label                - name shown to the openBIS user when adding or editing a customized query
#       <database>.database-driver      - JDBC Driver of the database (e.g. org.postgresql.Driver)
#   <database>.database-url           - JDBC URL to the database (e.g. jdbc:postgresql://localhost/openbis)
# Optional properties for each <database> include:
#   <database>.database-user        - name of the database user (default: user.name from system properties)
#   <database>.database-password    - password of the database user
#   <database>.creator-minimal-role - minimal role required to create/edit queries on this database (default: POWER_USER)
#   <database>.data-space           - If NOT specified OBSERVER of any space will be allowed to perform 
#                                     queries and <creator-minimal-role> of any space will allowed 
#                                     to create/edit queries on this DB.
#                                   - If specified only OBSERVER of the space will be allowed to perform 
#                                     queries and <creator-minimal-role> of the space will allowed 
#                                     to create/edit queries on this DB.
#query-databases = openbisDB
#
#openbisDB.label = openBIS meta data
#openbisDB.data-space = CISD
#openbisDB.creator-minimal-role = SPACE_ADMIN
#openbisDB.database-driver = org.postgresql.Driver
#openbisDB.database-url = jdbc:postgresql://localhost/openbis_${database.kind}
#openbisDB.database-username =
#openbisDB.database-password =

# ---------------------------------------------------------------------------
# Maintenance plugins configuration (optional)
# ---------------------------------------------------------------------------
# Comma separated names of maintenance plugins. 
# Each plugin should have configuration properties prefixed with its name.
# Mandatory properties for each <plugin> include: 
#   <plugin>.class - Fully qualified plugin class name
#   <plugin>.interval - The time between plugin executions (in seconds)
# Optional properties for each <plugin> include:
#   <plugin>.start - Time of the first execution (HH:mm)
#   <plugin>.execute-only-once - If true the task will be executed exactly once, 
#                                interval will be ignored. By default set to false.
#maintenance-plugins = demo
#
#demo.class = ch.systemsx.cisd.openbis.generic.server.task.DemoMaintenanceTask
#demo.interval = 60
#demo.property_1 = some value
#demo.property_2 = some value 2

#
# Internal - do not change
#

# Authorization
# Supported: 'no-authorization' and 'active-authorization'
authorization-component-factory = active-authorization

script-folder = .

#
# Version of Jython to be used in plugins. 2.5 and 2.7 are supported
#
jython-version=2.7

##########
# V3 API #
##########

# -------------------------------------------------------------------------
# The configuration below reflects the default values used by the V3 API.
# Please uncomment and change the chosen values to overwrite the defaults. 
# -------------------------------------------------------------------------
#
# A path to a directory where operation execution details are stored.
#
#  api.v3.operation-execution.store.path = operation-execution-store
#
# A thread pool that is used for executing all asynchronous operations. 
#
# api.v3.operation-execution.thread-pool.name = operation-execution-pool
# api.v3.operation-execution.thread-pool.core-size = 10
# api.v3.operation-execution.thread-pool.max-size = 10
# api.v3.operation-execution.thread-pool.keep-alive-time = 0
#
# A name of a thread that updates operation execution progress information.
#
# api.v3.operation-execution.progress.thread-name = operation-execution-progress
#
# An interval that controls how often operation execution progress information gets updated. The interval is defined in seconds.
# 
# api.v3.operation-execution.progress.interval = 5
#
# Availability times control for how long information about an operation execution is stored in the system. 
# There are 3 levels of such information:
#
# * core information (code, state, owner, description, creation_date, start_date, finish_date)
# * summary information (summary of operations, progress, error, results)
# * detailed information (details of operations, progress, error, results)
#
# Each level of information can have a different availability time.
# The availability times can be defined at the moment of scheduling an operation execution.
# If a time is not provided explicitly then a corresponding 'default' value is used. 
# The maximum possible value that can be used for a given availability time is controlled with the 'max' property.
#
# All availability times are defined in seconds.
# Examples of values: 31536000 (1 year), 2592000 (30 days), 86400 (1 day), 3600 (1 hour).
#
# api.v3.operation-execution.availability-time.default = 31536000
# api.v3.operation-execution.availability-time.max = 31536000
# api.v3.operation-execution.availability-time.summary.default = 2592000
# api.v3.operation-execution.availability-time.summary.max = 2592000
# api.v3.operation-execution.availability-time.details.default = 86400 
# api.v3.operation-execution.availability-time.details.max = 86400
#
# Maintenance tasks responsible for marking and deleting timed out operation executions. Intervals are defined in seconds.
#
# api.v3.operation-execution.availability-update.mark-timeout-pending-task.name = operation-execution-mark-timeout-pending-task
# api.v3.operation-execution.availability-update.mark-timeout-pending-task.interval = 60
#
# api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.name = operation-execution-mark-timed-out-or-deleted-task
# api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.interval = 300
#
# Maintenance task responsible for marking new, scheduled and running operation executions as failed after server restart.
#
# api.v3.operation-execution.state-update.mark-failed-after-server-restart-task.name = operation-execution-mark-failed-after-server-restart-task
#
#
```

### Database Settings

All properties starting with `database.` specify the settings for the openBIS database. They are all mandatory.

| Property                         | Description                                                                                                                                                                                                           |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `database.engine`                  | Type of database. Currently only postgresql is supported.                                                                                                                                                            |
| `database.create-from-scratch`     | If true the database will be dropped and an empty database will be created. In productive use always set this value to  false  .                                                                                     |
| `database.script-single-step-mode` | If true all SQL scripts are executed in single step mode. Useful for localizing errors in SQL scripts. Should be always false in productive mode.                                                                    |
| `database.url-host-part`           | Part of JDBC URL denoting the host of the database server. If openBIS Application Server and database server are running on the same machine this property should be an empty string.                                |
| `database.kind`                    | Part of the name of the database. The full name reads openbis_<  kind  >.                                                                                                                                      |
| `database.admin-user`              | ID of the user on database server with admin rights, like creation of tables. Should be an empty string if default admin user should be used. In case of PostgreSQL the default admin user is assumed to be postgres. |
| database.admin-password          | Password for admin user. Usual an empty string.                                                                                                                                                                      |
| `database.owner`                   | ID of the user owning the data. This should generally be openbis. The openbis role and password need to be created. In case of an empty string it is the same user who started up openBIS Application Server.        |
| `database.owner-password`          | Password of the owner.                                                                                                                                                                                               |
```{warning}
The credentials for the database user with the privilege to create a new database depends on the installation and configuration of the PostgreSQL database.
```

## Start Server

The server is started as follows:

```console
prompt> <installation folder>/bin/bisup.sh
```

On startup the openBIS server creates the database on PostgreSQL and
checks the connection with the remote authentication services (if they
are configured). Log files can be found in
`<installation folder>/servers/openBIS-server/jetty/logs`. Also the
following command shows the log: `<installation folder>/bin/bislog.sh`

```{warning}
The first user logged in into the system will have full administrator rights (role `INSTANCE_ADMIN`).
```

## Stop Server

The server is stopped as follows:

```console
prompt> <installation folder>/bin/bisdown.sh
```

## Authentication systems

Generic openBIS currently supports four authentication systems: a
self-contained system based on a UNIX-like passwd file, LDAP, the Crowd
system (see <http://www.atlassian.com/software/crowd>) and Single Sign
On (eg SWITCH AAI). Beside this there are also so called stacked
authentication methods available. Stacked authentication methods use
multiple authentication systems in the order indicated by the name. The
first authentication system being able to provide an entry for a
particular user id will be used. If you need full control over what
authentication systems are used in what order, you can define your own
stacked authentication service in the Spring application context file:
`<server folder>/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/genericCommonContext.xml.`

### The default authentication configuration

In the template service properties, we set
`authentication-service = file-ldap-crowd-caching-authentication-service`,
which means that file-based authentication, LDAP and Crowd are used for
authentication, in this order. As LDAP and Crowd are not configured in
the template service properties, this effectively corresponds
to `file-authentication-service`, however when LDAP and / or Crowd are
configured, they are picked up on server start and are used to
authenticate users when they are not found in the local `passwd` file.
Furthermore, as it is a caching authentication service, it will cache
authentication results from LDAP and / or Crowd in
`file <server folder>/jetty/etc/passwd_cache`. See section
*Authentication Cache* below for details on this caching.

### The file based authentication system

This authentication schema uses the file
`<server folder>/jetty/etc/passwd` to determine whether a login to the
system is successful or not.

The script `<server folder>/jetty/bin/passwd.sh` can be used to maintain
this file. This script supports the options:

```bash
passwd list | [remove|show|test] <userid> | [add|change] <userid> [option [...]]
    --help                 : Prints out a description of the options.
    [-P,--change-password] : Read the new password from the console,
    [-e,--email] VAL       : Email address of the user.
    [-f,--first-name] VAL  : First name of the user.
    [-l,--last-name] VAL   : Last name of the user.
    [-p,--password] VAL    : The password.
```


A new user can be added with

```bash
prompt> passwd.sh add [-f <first name>] [-l <last name>] [-e <email>] [-p <password>] <username>
```

If no password is provided with the `-p` option, the system will ask for
a password of the new user on the console. Please note that providing a
password on the command line can be a security risk, because the
password can be found in the shell history, and, for a short time, in
the `ps` table. Thus `-p` is not recommended in normal operation.

The password of a user can be tested with

```bash
prompt> passwd.sh test <username>
```

The system will ask for the current password on the console and then
print whether the user was authenticated successfully or not.

An account can be changed with

```bash
prompt> passwd.sh change [-f <first name>] [-l <last name>] [-e <email>] [-P] <username>
```

An account can be removed with

```bash
prompt> passwd.sh remove <username>
```

The details of an account can be queried with

```bash
prompt> passwd.sh show <username>
```

All accounts can be listed with

```bash
prompt> passwd.sh list
```

The password file contains each user in a separate line. The fields of
each line are separated by colon and contain (in this order): *User Id*,
*Email Address*, *First Name*, *Last Name* and *Password Hash*.
The *Password Hash* field represents the
[salted](http://en.wikipedia.org/wiki/Salted_hash)
[SHA1](http://en.wikipedia.org/wiki/Sha1) hash of the user's password in
[BASE64 encoding](http://en.wikipedia.org/wiki/Base64).

### The interface to LDAP

To work with an LDAP server, you need to provide the server URL with
(example) and set the
`authentication-service = ldap-authentication-service`

```bash
ldap.server.url = ldap://d.ethz.ch/DC=d,DC=ethz,DC=ch
```

and the details of an LDAP account who is allowed to make queries on the
LDAP server with (example)

```bash
ldap.security.principal.distinguished.name = CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch
ldap.security.principal.password = Carls_LDAP_Password
```


Note: A space-separated list of URLs can be provided if distinguished
name and password  are valid for all specified LDAP servers.

### The interface to Crowd

When setting `authentication-service = crowd-authentication-service` in
`service.properties`, the `passwd` file has no effect. Instead, the
following properties need to be configured via the following properties.

The URL (without port information):

    crowd.service.url = https://crowd.your.org

The Port of the URL:

    crowd.service.port = 443

The name of the application account in Crowd:

    crowd.application.name = openbis

The password of the application account in Crowd:

    crowd.application.password = <application password>

If Crowd is used as an authentication service, the IP of the openBIS
server and the name (of the openBIS application) has to be registered
with the Crowd server.

### Authentication Cache

If configuring a caching authentication service like
`file-ldap-crowd-caching-authentication-service`, authentication results
from remote authentication services like LDAP and / or Crowd are cached
locally in the openBIS Application Server. The advantage is a faster
login time on repeated logins when one or more remote authentication
services are slow. The disadvantage is that changes to data in the
remote authentication system (like a changed password or email address)
are becoming known to openBIS only with a delay. In order to minimize
this effect, the authentication caching performs "re-validation" of
authentication requests asynchronously. That means it doesn't block the
user from logging in because it is performed in different thread than
the login.

There are two service properties which give you control over the working
of the authentication cache:

- `authentication.cache.time` lets you set for how long (after putting
    it into the cache) a cache entry (read: "user name and password")
    will be kept if the user does not have a successful login to openBIS
    in this period of time (as successful logins will trigger
    re-validation and thus renewal of the cache entry). The default is
    28h, which means that users logging into the system every day will
    never experience a delay from slow remote authentication systems. A
    non-positive value will disable caching.
- `authentication.cache.time-no-revalidation` lets you set for how
    long (after putting it into the cache) a cache entry will *not* be
    re-validated if the login was successful. This allows you to reduce
    the load that openBIS creates on the remote authentication servers
    for successful logins of the same user. The default is 1h. Setting
    it to 0 will always trigger re-validation, setting it to
    `authentication.cache.time` will not perform re-validation at all
    and thus expire every cache entry after that time.

An administrator with shell access to the openBIS Application Server can
see and change the current cache entries in the
file `<server folder>/jetty/`etc/passwd\_cache. The format is the same
as for the file-based authentication system (see section *The file based
authentication system* above), but has an additional field *Cached At*
added to the end of each line. *Cached At* is the time (in milli-seconds
since start of the Unix epoch, which is midnight *Universal Time
Coordinated*, 1 January 1970) when the entry was cached. Removing a line
from this file will remove the corresponding cache entry. The
authentication cash survives openBIS Application Server restarts because
of this persisted file. If you need to clear the cache altogether, it is
sufficient to remove the `passwd_cache` file at any time. No server
restart is needed to make changes to this file take effect.

You can switch off authentication caching by either
setting `authentication.cache.time = -1`, or by choosing an
authentication service which does not have `caching` in its name.

### Anonymous Login

In order to allow anonymous login a certain user known by openBIS (not
necessarily by the authentication system) has to be specified. This is
done by the property `user-for-anonymous-login`. The value is the user
ID. The display settings and the authorization settings of this user are
used for the anonymous login.

Anonymous login is possible with URL parameter `anonymous` set to `true`
or by property `default-anonymous-login` in web configuration properties
(see [Web Client Customization](https://openbis.readthedocs.io/en/latest/system-admin-documentation/installation/installation-and-configuration-guide.html#web-client-customizations)). Note, that for the ELN client the property `default-anonymous-login` isn't used. Anonymous login needs only the property `user-for-anonymous-login` for an existing user with some rights.

### Single Sign On Authentication

Currently only Shibboleth SSO is supported. For more details see [Single Sign On Authentication](https://openbis.readthedocs.io/en/latest/system-admin-documentation/installation/installation-and-configuration-guide.html#single-sign-on-authentication).

## Authorization

openBIS authorization is described here: [Authorization](https://openbis.readthedocs.io/en/latest/system-admin-documentation/installation/installation-and-configuration-guide.html#authorization).

## System Customization

### Login Page - Banners

To add banners to the main OpenBIS change `loginHeader.html` page. It is
stored in the same directory as `index.html`. Note that if the height of
`loginHeader.html` is too big, the content may overlap with the rest of
the OpenBIS login page.

Example of the `loginHeader.html`:

    <center><img src="images/banner.gif"></center>

For announcements you have to edit the `index.html` file. Here is an
example showing the tail:

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
    December 2010 from 10 am to 3 pm!
<br>
</span>
</form>
</div>
</body>
</html>
```


Note: the current work around with `br` tags between the lines ensures
that the login box is still centered.

### Client Customization

#### Configuration

To reconfigure some parts of the openBIS Web Client and Data Set Upload
Client, prepare the configuration file and add the path to the value of
`web-client-configuration-file` property in openBIS
`service.properties`.

    web-client-configuration-file = etc/web-client.properties

#### Web client customizations

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

#### Data Set Upload Client Customizations

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

##### Examples

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

#### Full web-client.properties Example

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


### Configuring File Servlet

This service is specially tailored for web applications requiring to
upload files to the system without using the DataSet concept, it was
meant to be used for small images and rich text editors like CKEditor.

| Property Key                        | Default Value              | Description                                                                                               |
|-------------------------------------|----------------------------|-----------------------------------------------------------------------------------------------------------|
| file-server.maximum-file-size-in-MB | 10                         |  Max size of files.                                                                                      |
| file-server.repository-path         |  ../../../data/file-server | Path where files will be stored, ideally should be a folder on the same NAS you are storing the DataSets. |
| file-server.download-check          | true                       | Checks that the user is log in into the system to be able to download files.                             |

### Configuring DSS Data Sources

It is quite common that openBIS AS is using a database filled by DSS.
Depending on the DSS (specified by the DSS code) and the technology
different databases have to be used.

Configuration is best done by AS [core
plugins](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html) of type
`dss-data-sources`. The name of the plugin is just the DSS code. The
following properties of `plugin.properties` are recognized:

| Property Key                                     | Description                                                                                                                                                                                                                             |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| technology                                       | Normally the technology/module folder of the core plugin specifies the technology/module for which this data source has to be configured. If this is not the case this property allows to specify the technology/module independently.  |
| database-driver                                  | Fully qualified class name of the data base driver, e.g. `org.postgresql.Driver`.                                                                                                                                                        |
| database-url                                     | URL of the database, e.g. `jdbc:postgresql://localhost/imaging_dev`                                                                                                                                                                       |
| username                                         | Optional user name needed to access database.                                                                                                                                                                                          |
| password                                         | Optional password needed to access database.                                                                                                                                                                                           |
| validation-query                                 | Optional SQL script to be executed to validate database connections.                                                                                                                                                                   |
| database-max-idle-connections                    | The maximum number of connections that can remain idle in the pool. A negative value indicates that there is no limit. Default: -1                                                                                                      |
| database-max-active-connections                  | The maximum number of active connections that can be allocated at the same time. A negative value indicates that there is no limit. Default: -1                                                                                         |
| database-max-wait-for-connection                 | The maximum number of seconds that the pool will wait for a connection to be returned before throwing an exception. A value less than or equal to zero means the pool is set to wait indefinitely. Default: -1                          |
| database-active-connections-log-interval         | The interval (in ms) between two regular log entries of currently active database connections if more than one connection is active. Default: Disabled                                                                                  |
| database-active-number-connections-log-threshold | The number of active connections that will trigger a NOTIFY log and will switch on detailed connection logging. Default: Disabled                                                                                                       |
| database-log-stacktrace-on-connection-logging    | If true and logging enabled also stack traces are logged. Default: `false`                                                                                                                                                                |

Properties `database-driver` and `database-url` can be omitted if a
`etc/dss-datasource-mapping` is defined. For more see [Sharing
Databases](https://unlimited.ethz.ch/display/openBISDoc2010/Sharing+Databases).

### Changing the Capability-Role map

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


#### Capability Role Map for V3 API

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

### Querying Project Database

In some customized versions of openBIS an additional project-specific
database is storing data from registered data sets. This database can be
queried via SQL Select statements in openBIS Web application. In order
to protect modification of this database by malicious SQL code openBIS
application server should access this database as a user which is member
of a read-only group. The name of this read-only group is project
specific.

```{note}
It is possible to configure openBIS to query multiple project-specific databases.
```

#### Create Read-Only User in PostgreSQL

A new user (aka role) is created by

```sql
CREATE ROLE <read-only user> LOGIN NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
```

This new user is added to the read-only group by the following command:

```sql
GRANT <read-only group> TO <read-only user>;
```

The name of the read-only group can be obtained by having a look into
the list of all groups:

```sql
SELECT * from PG_GROUP;
```

*Note that by default openBIS creates a user* ` openbis_readonly `
*which has read-only permissions to all database objects. You can use
this user to access the openBIS meta database through the openBIS query
interface.*

#### Enable Querying

To enable querying functionality for additional databases in openBIS Web  application a [core plugin](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html#core-plugins) of type query-databases has to be created. The following `plugin.properties` have to be specified:

| Property          | Description                                                                                                               |
|-------------------|---------------------------------------------------------------------------------------------------------------------------|
| label             | Label of the database. It will be used in the Web application in drop down lists for adding / editing customized queries. |
| database-driver   | JDBC Driver of the database, e.g. org.postgresql.Driver for postgresql.                                                  |
| database-url      | JDBC URL to the database containing full database name, e.g. jdbc:postgresql://localhost/database_name for postgresql     |
| database-username | Above-mentioned defined read-only user.                                                                                  |
| database-password | Password of the read-only user.                                                                                          |

#### Configure Authorization

In order to configure authorization two additional properties can be
configured:

| Property                              | Description                                                                                                                                                                 |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <database>.data-space           | To which data-space this database belongs to (optional, i.e. a query database can be configured not to belong to one data space by leaving this configuration value empty). |
| <database>.creator-minimal-role | What role is required to be allowed to create / edit queries on this database (optional, default: INSTANCE_OBSERVER if data-space is not set, POWER_USER otherwise).       |

The given parameters data-space and creator-minimal-role are used by
openBIS to enforce proper authorization.

For example, if

    data-space = CISD
    creator-minimal-role = SPACE_ADMIN

is configured, then for the query database configured with key `db1`:

- only a `SPACE_ADMIN` on data space `CISD` and an `INSTANCE_ADMIN`
    are allowed to create / edit queries,
- only a user who has the `OBSERVER` role in data space `CISD` is
    allowed to execute a query.

For query databases that do not belong to a space but that have a column with any of the [magic column names](https://openbis.readthedocs.io/en/latest/user-documentation/general-admin-users/custom-database-queries.html#hyperlinks), the query result is filtered on a per-row basis according to what the user executing the query is allowed to see. In detail this means: if the user executing the query is not an instance admin, filter out all rows which belong to a data space where the user doesn't have a least the observer role. The relationship between a row and a data space is established by means of the experiment / sample / data set whose `permId` is given by one of the magical column names.

For sensitive data where authorization needs to be enforced, there are
two setups possible:

1. Configure a query database that **does not** belong to a data space
    and set the creator-minimal-role to `INSTANCE_ADMIN`. Any instance
    admin can be trusted to understand authorization issues and ensure
    that only queries are added for this query database that contain a
    proper reference to an experiment / sample / data set. This way, it
    can be ensured that only properly filtered query results are
    returned to the user running the query.
2. Configure a query database that **does** belong to a specific data
    space and set the creator-minimal-role to `POWER_USER`. The
    datastore server (or whatever server maintains the query database)
    ensures that only information related to the configured data space
    is added to the query database. Thus whatever query the power user
    writes for this database, it will only reveal information from this
    data space. As only users with `OBSERVER` role on this data space
    are allowed to execute the query, authorization is enforced properly
    without the need of filtering query results.

### Master data import/export

The master data of openBIS comprises all entity/property types, property
assignments, vocabularies etc. needed for your customized installation
to work. The system offers a way to export/import master data via Jython
scripts. More information on how to do create such scripts and run them
manually see the advanced guide [Jython Master Data Scripts](https://unlimited.ethz.ch/display/openBISDoc2010/Jython+Master+Data+Scripts#JythonMasterDataScripts-Commandlinetools).

A master data script can be run automatically by start up of the AS if
it is defined in an AS core plugin. The script path should be
`<installation directory>/servers/core-plugins/<module name>/<version number>/as/initialize-master-data.py`.
For more details about the folder structure of core plugins see [Core
Plugins](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html#core-plugins). If there are several
core plugins with master data scripts the scripts will be executed in
alphabetical order of the module names. For example, the master data
script of module `screening-optional` will be executed after the master
data script of module `screening` has been executed.

Execution of master data script can be suppressed by
disabling `initialize-master-data` core plugin. For more details see
[Core Plugins](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html).

### Limit of open files

When putting a lot of files in a drop box you might run into the problem
of  '`too many open files error`'. Please consider changing the ulimit
value (for RHEL6 edit `/etc/security/limits.conf` ) to a higher value.

### Runtime changes to logging

The
script  `<installation directory>/servers/openBIS-server/jetty/bin/configure.sh `can
be used to change the logging behavior of openBIS application server
while the server is running.

The script is used like this: configure.sh \[command\] \[argument\]

The table below describes the possible commands and their arguments.

| Command                              | Argument(s)                                            | Default Value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|--------------------------------------|--------------------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| log-service-calls                    | 'on', 'off'                                            | 'off'         | Turns on / off detailed service call logging.
When this feature is enabled, openBIS will log about start and end of every service call it executes to file <installation directory>/servers/openBIS-server/jetty/log/openbis_service_calls.txt                                                                                                                                                                                                                                 |
| log-long-running-invocations         | 'on', 'off'                                            | 'on'          | Turns on / off logging of long running invocations.
When this feature is enabled, openBIS will periodically create a report of all service calls that have been in execution more than 15 seconds to file <installation directory>/servers/openBIS-server/jetty/log/openbis_long_running_threads.txt.                                                                                                                                                                         |
| debug-db-connections                 | 'on', 'off'                                            | 'off'         | Turns on / off logging about database connection pool activity.
When this feature is enabled, information about every borrow and return to database connection pool is logged to openBIS main log in file <installation directory>/servers/openBIS-server/jetty/log/openbis_log.txt                                                                                                                                                                                            |
| log-db-connections                   | no argument / minimum connection age (in milliseconds) | 5000          | When this command is executed without an argument, information about every database connection that has been borrowed from the connection pool is written into openBIS main log in file <installation directory>/servers/openBIS-server/jetty/log/openbis_log.txt
If the "minimum connection age" argument is specified, only connections that have been out of the pool longer than the specified time are logged. The minimum connection age value is given in milliseconds. |
| record-stacktrace-db-connections     | 'on', 'off'                                            | 'off'         | Turns on / off logging of stacktraces.
When this feature is enabled AND debug-db-connections is enabled, the full stack trace of the borrowing thread will be recorded with the connection pool activity logs.                                                                                                                                                                                                                                                                      |
| log-db-connections-separate-log-file | 'on', 'off'                                            | 'off'         | Turns on / off database connection pool logging to separate file.
When this feature is disabled, the database connection pool activity logging is done only to openBIS main log. When this feature is enabled, the activity logging is done ALSO to file <installation directory>/servers/openBIS-server/jetty/log/openbis_db_connections.txt.                                                                                                                                |

 

### Deleted Entity History

Logging the history of deleted entities can be enabled / disabled in
service.properties using setting

entity-history.enabled = \[true | false\]

Since 20.10.1 the default value is true (meaning, entity history is
enabled). Before 20.10.1 the default value was false.

Deleted entity history can be queried with script show-history.sh, which
is located in $OPENBIS\_INSTALL\_DIR/bin

## Troubleshooting Problems

### Samples with datasets and no experiments

In the openBIS UI users could detach samples with container data sets
from the experiment. This bug was fix on version S176 released on 14 of
march of 2014.

The following SQL script lists all samples with data sets but no
experiments:

```sql
##
## SELECT SAMPLES WITH DATASETS AND NO EXPERIMENTS
##
SELECT s.id, d.expe_id from samples_all s join data_all d on (d.samp_id=s.id) where s.expe_id is null ORDER by s.id

If the last query shows no output the system is fine, if not, it can be
repaired with the following update query.

##
## FIX SAMPLES WITH DATASETS AND NO EXPERIMENTS ASSIGNING EXPERIMENT FROM DATASET
##
UPDATE samples_all
SET expe_id = subquery.expe_id
FROM (
    SELECT s.id as samp_id, d.expe_id as expe_id from samples_all s join data_all d on (d.samp_id=s.id) where s.expe_id is null
) as subquery
where id = subquery.samp_id
```

