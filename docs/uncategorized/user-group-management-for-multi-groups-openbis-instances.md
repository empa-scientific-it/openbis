[Log
in](https://unlimited.ethz.ch/login.action?os_destination=%2Fdisplay%2FopenBISDoc2010%2FUser%2BGroup%2BManagement%2Bfor%2BMulti-groups%2BopenBIS%2BInstances)

Linked Applications

Loading…

[![Confluence](/download/attachments/327682/atl.site.logo?version=1&modificationDate=1563454119905&api=v2)](/)

-   [Spaces](/spacedirectory/view.action "Spaces")
-   [Create ](# "Create from template")

-   Hit enter to search

-   [Help](# "Help")
    -   [Online
        Help](https://docs.atlassian.com/confluence/docs-82/ "Visit the Confluence documentation home")
    -   [Keyboard Shortcuts](# "View available keyboard shortcuts")
    -   [Feed
        Builder](/dashboard/configurerssfeed.action "Create your custom RSS feed.")
    -   [What’s
        new](https://confluence.atlassian.com/display/DOC/Confluence+8.2+Release+Notes)
    -   [Available Gadgets](# "Browse gadgets provided by Confluence")
    -   [About
        Confluence](/aboutconfluencepage.action "Get more information about Confluence")

-   

-   

-   

-   [Log
    in](/login.action?os_destination=%2Fdisplay%2FopenBISDoc2010%2FUser%2BGroup%2BManagement%2Bfor%2BMulti-groups%2BopenBIS%2BInstances)

  

[![openBIS Documentation Rel.
20.10](/images/logo/default-space-logo.svg)](/display/openBISDoc2010/openBIS+Documentation+Rel.+20.10+Home "openBIS Documentation Rel. 20.10")

[openBIS Documentation Rel.
20.10](/display/openBISDoc2010/openBIS+Documentation+Rel.+20.10+Home "openBIS Documentation Rel. 20.10")

-   [Pages](/collector/pages.action?key=openBISDoc2010)
-   [Blog](/pages/viewrecentblogposts.action?key=openBISDoc2010)

### Page tree

[](/collector/pages.action?key=openBISDoc2010)

Browse pages

ConfigureSpace tools

[](#)

-   [ ](#)
    -   [ Attachments (0)
        ](/pages/viewpageattachments.action?pageId=53746056 "View Attachments")
    -   [ Page History
        ](/pages/viewpreviousversions.action?pageId=53746056)

    -   [ Page Information ](/pages/viewinfo.action?pageId=53746056)
    -   [ Resolved comments ](#)
    -   [ View in Hierarchy
        ](/pages/reorderpages.action?key=openBISDoc2010&openId=53746056#selectedPageInHierarchy)
    -   [ View Source
        ](/plugins/viewsource/viewpagesrc.action?pageId=53746056)
    -   [ Export to PDF
        ](/spaces/flyingpdf/pdfpageexport.action?pageId=53746056)
    -   [ Export to Word ](/exportword?pageId=53746056)
    -   [ View Visio File
        ](/plugins/lucidchart/selectVisio.action?contentId=53746056)

    -   [ Copy
        ](/pages/copypage.action?idOfPageToCopy=53746056&spaceKey=openBISDoc2010)

1.  [Pages](/collector/pages.action?key=openBISDoc2010)
2.  **…**
3.  [openBIS Documentation Rel. 20.10
    Home](/display/openBISDoc2010/openBIS+Documentation+Rel.+20.10+Home)
4.  [openBIS 20.10
    Documentation](/display/openBISDoc2010/openBIS+20.10+Documentation)
5.  [Guides](/display/openBISDoc2010/Guides)

-   []( "Unrestricted")
-   [Jira links]()

[User Group Management for Multi-groups openBIS Instances](/display/openBISDoc2010/User+Group+Management+for+Multi-groups+openBIS+Instances)
--------------------------------------------------------------------------------------------------------------------------------------------

-   Created by [Fuentes Serna Juan Mariano
    (ID)](%20%20%20%20/display/~juanf%0A), last modified by [Elmer
    Franz-Josef (ID)](%20%20%20%20/display/~felmer%0A) on [Dec 04,
    2022](/pages/diffpagesbyversion.action?pageId=53746056&selectedPageVersions=10&selectedPageVersions=11 "Show changes")

### 

### Introduction

Running openBIS as a facility means that different groups share the same
openBIS instance. Therefore the following demands have to be addressed
by correct configuration of such an instance:

-   A user should have only access to data of groups to which he or she
    belongs.
-   Each group should have its own disk space on DSS by assigning each
    group to a specific
    [share](/display/openBISDoc2010/Installation+and+Administrators+Guide+of+the+openBIS+Data+Store+Server#InstallationandAdministratorsGuideoftheopenBISDataStoreServer-SegmentedStore).
-   Make openBIS available for a new group.
-   Optional usage reports should be sent regularly.

In order to fulfill these demands

-   a `UserManagementMaintenanceTask` has to be configured on AS
-   an `EagerShufflingTask` for the `PostRegistrationTask` has to be
    configured on DSS.
-   optionally a `UsageReportingTask `has to be configured on AS.

If a new group is added

-   a new share has to be added to the DSS store folder (a symbolic link
    to an NFS directory)
-   a group definition has to be added to a configuration file by added
    LDAP group keys or an explicit list of user ids.

### Configuration

Two types of configurations are needed:

-   Static configurations: Changes in these configuration need a restart
    of openBIS (AS and/or DSS)
-   Dynamic configurations: Changes apply without the need of a restart
    of openBIS

#### Static Configurations

The necessary static configurations have to be specified in two places:
AS and DSS service.properties.

##### AS service.properties

Here an LDAPAuthenticationService (only if needed) and a
UserManagementMaintenanceTask are configured:

**AS service.properties**

    # Authentication service. 
    # Usually a stacked service were first file-based service is asked (for users like etl-server, i.e. DSS)
    # and second the LDAP service if the file-based service fails. 
    authentication-service = file-ldap-authentication-service

    # When a new person is created in the database the authentication service is asked by default whether this
    # person is known by the authentication service. 
    # In the case of single-sign-on this doesn't work. In this case the authentication service shouldn't be asked.
    # and the flag 'allow-missing-user-creation' should be set 'true' (default: 'false')
    #
    # allow-missing-user-creation = false

    # The URL of the LDAP server, e.g. "ldaps://ldaps-hit-1.ethz.ch"
    ldap.server.url = <LDAP URL>
    # The distinguished name of the security principal, e.g. "CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch"
    ldap.security.principal.distinguished.name = <distinguished name to login to the LDAP server>
    # Password of the LDAP user account that will be used to login to the LDAP server to perform the queries
    ldap.security.principal.password = <password of the user to connect to the LDAP server>
    # The search base, e.g. "ou=users,ou=nethz,ou=id,ou=auth,o=ethz,c=ch"
    ldap.searchBase = <search base>
    ldap.queryTemplate = (%s)
    ldap.queryEmailForAliases = true

    # Maintenance tasks for user management
    maintenance-plugins = user-management, usage-reporting

    user-management.class = ch.systemsx.cisd.openbis.generic.server.task.UserManagementMaintenanceTask
    # Start time in 24h notation
    user-management.start = 01:15
    # Time interval of execution
    user-management.interval = 1 days
    # Path to the file with dynamic configuration
    user-management.configuration-file-path = ../../../data/user-management-maintenance-config.json
    # Path to the file with information which maps groups to data store shares. 
    # Will be created by the maintenance task and is needed by DSS (EagerShufflingTask during post registration)
    user-management.shares-mapping-file-path = ../../../data/shares-mapping.txt
    # Path to the audit log file. Default: logs/user-management-audit_log.txt
    # user-management.audit-log-file-path =

    usage-reporting.class = ch.systemsx.cisd.openbis.generic.server.task.UsageReportingTask
    # Time interval of execution and also length report period
    usage-reporting.interval = 7 days
    # Path to the file with group definition
    usage-reporting.configuration-file-path = ${user-management.configuration-file-path}
    # User reporting type. Possible values are NONE, ALL, OUTSIDE_GROUP_ONLY. Default: ALL
    usage-reporting.user-reporting-type = OUTSIDE_GROUP_ONLY
    # Comma-separated list of e-mail addresses for report sending
    usage-reporting.email-addresses = <address 1>, <address 2>, ... 

    # Mail server configuration is needed by UsageReportingTask
    mail.from = openbis@<host>
    mail.smtp.host = <SMTP host>
    mail.smtp.user = <can be empty>
    mail.smtp.password = <can be empty>

With this template configuration the UserManagementMaintenanceTask runs
every night at 1:15 am. It reads the configuration
file `<installation path>/data/user-management-maintenance-config.json`
and creates `<installation path>/data/shares-mapping.txt`. Every week a
usage report file of the previous week is sent to the specified
addresses.

For the LDAP configuration `ldap.server.url`,
`ldap.security.principal.distingished.name`, `ldap.security.principal.password`
and `ldap.searchBase` have to be specified.

The LDAP service is not only used for authenticating users but also to
obtain all users of a group. In the later case an independent query
template can be specified by the property `ldap-group-query-template` of
the `plugin.properties` of `UserManagementMaintenanceTask` (since
20.10.1.1). The % character in this template will be replaced by the
LDAP group key.

###### Active Directory

If the LDAP service is actually an Active Directory service the
configuration is a bit different. These are the changes:

-   Remove `ldap.queryTemplate`. This means that the default
    value `(&(objectClass=organizationalPerson)(objectCategory=person)(objectClass=user)(%s))`
    will be used.

-   It might be necessary to increase the timeout. The default value is
    10 second. Example: `ldap.timeout = 1 min`

-   Add the following line to the AS service.properties:

    **AS service.properties**

        user-management.filter-key = memberOf:1.2.840.113556.1.4.1941:

The ldap group keys described below in section *Dynamic Configurations*
have to be full distinguished names (DN) like
e.g. `CN=id-sis-source,OU=Custom,OU=EthLists,DC=d,DC=ethz,DC=ch`. To
find the correct DN an LDAP browsing tool (like Apache Directory Studio
<https://directory.apache.org/studio/>) might be useful.

##### DSS service.properties

Here the PostRegistrationMaintenanceTask has be extended for eager
shuffling.

**DSS service.properties**

    # Lists of post registrations tasks for each data set executed in the specified order. 
    # Note, that pathinfo-feeding is already defined.
    post-registration.post-registration-tasks = pathinfo-feeding, eager-shuffling
    post-registration.eager-shuffling.class = ch.systemsx.cisd.etlserver.postregistration.EagerShufflingTask
    post-registration.eager-shuffling.share-finder.class = ch.systemsx.cisd.openbis.dss.generic.shared.MappingBasedShareFinder
    # Path to the file with information which maps groups to data store shares. 
    post-registration.eager-shuffling.share-finder.mapping-file = ../../data/shares-mapping.txt

Eager shuffling moves the just registered data set from share 1 to the
share of the group as specified
in `<installation path>/data/shares-mapping.txt`. For more details about
share mapping see [Mapping File for Share Ids and Archiving
Folders](/display/openBISDoc2010/Mapping+File+for+Share+Ids+and+Archiving+Folders).

#### Dynamic Configurations

Each time the UserManagementMaintenanceTask is executed it reads the
configuration file specified
in `user-management.configuration-file-path` of AS `service.properties`.
It is a text file in JSON format which has the following structure, that
needs to be created manually:

    {
        "globalSpaces": ["<space 1>", "<space 2>", ...],
        "commonSpaces":
        {
            "<role 1>": ["<space post-fix 11>", "<space post-fix 12>", ...],
            "<role 2>": ["<space post-fix 21>", "<space post-fix 22>", ...],
            ...
        },
        "commonSamples":
        {
            "<sample identifier template 1>": "<sample type 1>", 
            "<sample identifier template 2>": "<sample type 2>",
            ...
        },
        "commonExperiments": 
        [
            {
                "identifierTemplate" : "<experiment identifier template 1>",
                "experimentType"   :  "<experiment type 1>", 
                "<property code 1>"  :  "<property value 1>",
                "<property code 2>"  :  "<property value 2>",
                ... 
            }, 
            {
                "identifierTemplate" : "<experiment identifier template 2>",
                "experimentType"   :  "<experiment type 2>", 
                "<property code 1>"  :  "<property value 1>",
                "<property code 2>"  :  "<property value 2>",
                ... 
            },  
            ...
        ],
        "instanceAdmins": ["<instance admin user id 1>", "<instance admin user id 1>"],
        "groups":
        [
            {
                "name": "<human readable group name 1>",
                "key": "<unique group key 1>",
                "ldapGroupKeys": ["<ldap group key 11>", "<ldap group key 12>", ...],
                "users": ["<user id 11>", "<user id 12>", ...],
                "admins": ["<user id 11>", "<user id 12>", ...],
                "shareIds": ["<share id 11>", "<share id 12>", ...],
                "useEmailAsUserId": true/false (default: false),
                "createUserSpace": true/false (default: true),
                "userSpaceRole" : <role> (default: non)
       },
            {
                "name": "<human readable group name 2>",
                "key": "<unique group key 2>",
                "ldapGroupKeys": ["<ldap group key 21>", "<ldap group key 22>", ...],
                "admins": ["<user id 21>", "<user id 22>", ...],
                "shareIds": ["<share id 21>", "<share id 22>", ...],
                "useEmailAsUserId": true/false (default: false),
                "createUserSpace": true/false (default: true),
                "userSpaceRole" : <role> (default: non)
        },
            ...
        ]
    }

Example:

    {
        "globalSpaces": ["ELN_SETTINGS"],
        "commonSpaces":
        {
            "USER": ["INVENTORY", "MATERIALS", "METHODS", "STORAGE", "STOCK_CATALOG"],
            "OBSERVER": ["ELN_SETTINGS", "STOCK_ORDERS"]
        },
        "commonSamples":
        {
            "ELN_SETTINGS/ELN_SETTINGS": "GENERAL_ELN_SETTINGS"
        }, 
        "commonExperiments":
        [
            {
                "identifierTemplate" : "ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Templates Collection",
                "$DEFAULT_OBJECT_TYPE" : null,
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "ELN_SETTINGS/STORAGES/STORAGES_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Storages Collection",
                "$DEFAULT_OBJECT_TYPE" : "STORAGE",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "PUBLICATIONS/PUBLIC_REPOSITORIES/PUBLICATION_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Publication Collection",
                "$DEFAULT_OBJECT_TYPE" : "PUBLICATION",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "STOCK_ORDERS/ORDERS/ORDER_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Order Collection",
                "$DEFAULT_OBJECT_TYPE" : "ORDER",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "STOCK_CATALOG/PRODUCTS/PRODUCT_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Product Collection",
                "$DEFAULT_OBJECT_TYPE" : "PRODUCT",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "STOCK_CATALOG/REQUESTS/REQUEST_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Request Collection",
                "$DEFAULT_OBJECT_TYPE" : "REQUEST",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            },
            {
                "identifierTemplate" : "STOCK_CATALOG/SUPPLIERS/SUPPLIER_COLLECTION",
                "experimentType" : "COLLECTION",
                "$NAME" : "Supplier Collection",
                "$DEFAULT_OBJECT_TYPE" : "SUPPLIER",
                "$DEFAULT_COLLECTION_VIEW" : "LIST_VIEW"
            }
        ],
        "groups": 
        [
            {
                "name":"ID SIS",
                "key":"SIS",
                "ldapGroupKeys": ["id-sis-source"],
                "admins": ["abc", "def"],
                "shareIds": ["2", "3"],
                "createUserSpace": false
            }
        ]
    }

##### Section `globalSpaces`

Optional. A list of space codes. If the corresponding spaces do not
exist they will be created. All users of all groups will have
SPACE\_OBSERVER rights on these spaces. For this reason the
authorization group `ALL_GROUPS` will be created.

##### Section `commonSpaces`

Optional. The following roles are allowed:

ADMIN, USER, POWER\_USER, OBSERVER.

For each role a list of space post-fix codes are specified. For each
group of the group section a space with code
`<group key>_<space post-fix>` will be created. Normal users of the
group will have access right SPACE\_&lt;ROLE&gt; and admin users will
have access right SPACE\_ADMIN.

##### Section `commonSamples`

Optional. A list of key-value pairs where the key is a sample identifier
template and the value is an existing sample type. The template has the
form

`<space post-fix code>/<sample post-fix code>`

The space post-fix code has to be in one of the lists of common spaces.
For each group of the group section a sample with identifier

`<group key>_<space post-fix code>/<group key>_<sample post-fix code>`

of specified type will be created.

##### Section `commonExperiments`

Optional. A list of maps where every key represents the different
experiment attributes, allowing the not only set the type but also set
property values. The template has the form

`<space post-fix code>/<project post-fix code>/<experiment post-fix code> `

The space post-fix code has to be in one of the lists of common spaces.
For each group of the group section an experiment with identifier

`<group key>_<space post-fix code>/<group key>_<project post-fix code>/<group key>_<experiment post-fix code>`

of specified type will be created.

##### Section `instanceAdmins` (since version 20.10.6)

Optional. A list of users for which INSTANCE\_ADMIN rights will be
established. If such users are no longer known by the authetication
service they will not be revoked`.`

##### Section `groups`

A list of group definitions. A group definition has the following
sections:

-   `name`: The human readable name of the group.
-   `key`: A unique alphanumerical key of the group that follows the
    same rules as openBIS codes (letters, digits, '-', '.' but no '\_'),
    for this particular purpose using only capital letters is
    recommended. It is used to created the two authorization groups
    `<group key>` and `<group key>_ADMIN.`
-   `ldapGroupKeys`: A list of group keys known by the LDAP
    authentication service.
-   `users`: An explicit list of user ids.
-   `admins`: A list of user ids. All admin users have SPACE\_ADMIN
    rights to all spaces (common and user ones) which belong to the
    group.
-   `shareIds`: This is a list of ids of data store shares. This list is
    only needed if `shares-mapping-file-path` has been specified.
-   `useEmailAsUserId`: (since 20.10.1) If `true` the email address will
    be used instead of the user ID to determine the code of the user's
    space. Note, that the '@' symbol in the email address will be
    replaced by '\_AT\_'. This flag should be used if [Single Sign
    On](/display/openBISDoc2010/Single+Sign+On+Authentication) is used
    for authentication but LDAP for managing the users of a group.
    Default: `false.`
-   `createUserSpace`: (since 20.10.1) This is a flag that controls a
    creation of personal user spaces for the users of this group. By
    default it is set to true, i.e. the personal user spaces will be
    created. If set to false, then the personal user spaces won't be
    created for this group.
-   `userSpaceRole`: Optional access role (either ADMIN, USER,
    POWER\_USER, or OBSERVER) for all users of the group on all personal
    user spaces. (since version 20.10.3)

### What UserManagementMaintenanceTask does

Each time this maintenance task is executed (according to the scheduling
interval of `plugin.properties`) the JSON configuration file will be
read first. The task does the following:

1.  Updates mapping file of data store shares if
    `shares-mapping-file-path` has been specified.
2.  Creates global spaces if they do not exist and allows
    SPACE\_OBSERVER access by all users of all groups.
3.  Revokes all users unknown by the authentication service. These users
    will not be deleted but deactivated. This includes removing home
    space and all authorization rights.
4.  Does for each specified group the following:
    1.  Creates the following two authorization groups if they do not
        exist:
        1.  `<group key>`: All users of the group will a member of this
            authorization group. This group has access rights to common
            spaces as specified.
        2.  `<group key>_ADMIN`: All admin users of the group will be
            member of this authorization group. This group has
            SPACE\_ADMIN rights to all common spaces and all personal
            user spaces.
    2.  Creates common spaces if they do not exist and assign roles for
        these space to the authorization groups.
    3.  Creates for each user of the LDAP groups or the explicit list of
        user ids a personal user space with SPACE\_ADMIN access right
        (NOTE: since 20.10.1 creation of personal user spaces can be
        disabled by setting "createUserSpace" flag in the group
        configuration to false). The space code read  
        `<group key>_<user id>[_<sequence number>]         `A sequence
        number will be used if there is already a space with code
        `<group key>_<user_id`&gt;. There are two reason why this can
        happen:
        1.  A user leaving the group and joining it again later but was
            always known by the authentication service.
        2.  A user leaving the group and the institution. That it, the
            user is no longer known by the authentication service. But
            later another user with the same user id is joining the
            group.
    4.  Creates common samples if they do not exist.
    5.  Creates common experiments (and necessary projects) if they do
        not exist.
5.  Assigns home spaces in accordance to the following rules:
    1.  If the user has no home space the personal user space of the
        first group of the JSON configuration file will become the home
        space.
    2.  The home space will not be changed if its code doesn't start
        with `<group key>_<user id>` for all groups.
    3.  If the user leaves a group the home space will be removed.

    Note, if a user is moved from one group to another group the home
    space of the user will be come the personal user space of the new
    group.

### Content of the Report File sent by UsageReportingTask

The report file is a TSV text file with following columns:

[TABLE]

-   The first line in the report (after the column headers) shows always
    the summary (with unspecified 'group name').
-   If `configuration-file-path` is specified usage for each specified
    group (in alphabetic order) is listed.
-   Finally usage by individual users follows if `user-reporting-type`
    isn't NONE

### Common use cases

Here are some common uses cases. No openBIS restart is needed for these
use cases.

#### Adding a new group

In order to make openBIS available for a new group three things have to
be done by an administrator:

1.  Add one or more shares to the DSS store. These are symbolic links to
    (remote) disk space which belongs to the new group. Note, that
    symbolic link has to be a number which is the share ID.
2.  Define a new group in the LDAP service and add all persons which
    should belong to the group. Note, a person can be in more than one
    group.
3.  Add to the above mentioned JSON configuration file a new section
    under `groups`.

#### Making a user an group admin

Add the user ID to the `admins` list of the group in the JSON
configuration file.

#### Remove a user from a group

The user has to be removed from the LDAP group on the LDAP service.

#### Adding more disk space

1.  Add a new share for the new disk to DSS store.
2.  Add the share id to the `shareIds` list.

  

### Manual configuration of multi-group instances

See [Manual configuration of Multi-groups openBIS
instances](/display/openBISDoc2010/Manual+configuration+of+Multi-groups+openBIS+instances)

  

-   No labels

Overview

Content Tools

Apps

-   Powered by [Atlassian
    Confluence](https://www.atlassian.com/software/confluence) 8.2.0
-   Printed by Atlassian Confluence 8.2.0
-   [Report a bug](https://support.atlassian.com/confluence-server/)
-   [Atlassian News](https://www.atlassian.com/company)

[Atlassian](https://www.atlassian.com/)

{"serverDuration": 135, "requestCorrelationId": "2e637ddf8ef2ecd0"}
