[Log
in](https://unlimited.ethz.ch/login.action?os_destination=%2Fdisplay%2FopenBISDoc2010%2FJython%2BDataSetValidator)

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
    in](/login.action?os_destination=%2Fdisplay%2FopenBISDoc2010%2FJython%2BDataSetValidator)

  

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
        ](/pages/viewpageattachments.action?pageId=53746030 "View Attachments")
    -   [ Page History
        ](/pages/viewpreviousversions.action?pageId=53746030)

    -   [ Page Information ](/pages/viewinfo.action?pageId=53746030)
    -   [ Resolved comments ](#)
    -   [ View in Hierarchy
        ](/pages/reorderpages.action?key=openBISDoc2010&openId=53746030#selectedPageInHierarchy)
    -   [ View Source
        ](/plugins/viewsource/viewpagesrc.action?pageId=53746030)
    -   [ Export to PDF
        ](/spaces/flyingpdf/pdfpageexport.action?pageId=53746030)
    -   [ Export to Word ](/exportword?pageId=53746030)
    -   [ View Visio File
        ](/plugins/lucidchart/selectVisio.action?contentId=53746030)

    -   [ Copy
        ](/pages/copypage.action?idOfPageToCopy=53746030&spaceKey=openBISDoc2010)

1.  [Pages](/collector/pages.action?key=openBISDoc2010)
2.  **…**
3.  [openBIS Documentation Rel. 20.10
    Home](/display/openBISDoc2010/openBIS+Documentation+Rel.+20.10+Home)
4.  [openBIS 20.10
    Documentation](/display/openBISDoc2010/openBIS+20.10+Documentation)
5.  [Guides](/display/openBISDoc2010/Guides)

-   []( "Unrestricted")
-   [Jira links]()

[Jython DataSetValidator](/display/openBISDoc2010/Jython+DataSetValidator)
--------------------------------------------------------------------------

-   Created by [Fuentes Serna Juan Mariano
    (ID)](%20%20%20%20/display/~juanf%0A) on [Oct 01,
    2020](/pages/viewpreviousversions.action?pageId=53746030 "Show changes")

### Overview

Jython dataset validators are an option for implementing validation of
data sets using the python scripting language when using a jython
dropbox. See [Dropboxes](/display/openBISDoc2010/Dropboxes) for the
basic configuration. The validators can also be run on clients, either
the command-line dss client or the web start Data Set Batch Uploader,
though there are some additional restrictions on which scripts can be
run within the batch uploader.

### Configuration

To configure a validator, add the configuration parameter
"validation-script-path" to the thread definition. For example:

**plugin.properties**

    # --------------------------------------------------------------------------------------------------
    # Jython thread
    # --------------------------------------------------------------------------------------------------
    # The directory to watch for incoming data.
    incoming-dir = /local0/openbis/data/incoming-jython
    top-level-data-set-handler = ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler
    incoming-data-completeness-condition = auto-detection
    strip-file-extension = true
    storage-processor = ch.systemsx.cisd.etlserver.DefaultStorageProcessor
    script-path = data-set-handler.py
    validation-script-path = data-set-validator.py

The script file (in this case "data-set-validator.py") needs to
implement one method, validate\_data\_set\_file(file), which takes a
file object as an argument and returns a collection of validation error
objects as a result. If the collection is empty, then it is assumed that
there were no validation errors.

There are convenience methods to create various kinds of validation
errors. These methods are:

-   `createFileValidationError(message: String)`,
-   `createDataSetTypeValidationError(message : String)`,
-   `createOwnerValidationError(message: String)` and
-   `createPropertyValidationError(property : String, message : String)`.  
    In the context of the validation scripts as they are currently
    implemented, the first one is probably the most relevant.

These methods are defined on the class
ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError.
The documentation for this class should be available here:

<http://svnsis.ethz.ch/doc/openbis/current/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1/validation/ValidationError.html>

### Example scripts

One can use both python standard libraries and Java libraries.

#### Simple script using python libraries:

    import os
    import re

    def validate_data_set_file(file):
        found_match = False
        if re.match('foo-.*bar', file.getName()):
            found_match = True

        errors = []
        if found_match:
            errors.append(createFileValidationError(file.getName() + " is not a valid data set."))

        return errors

#### Simple script using only java libraries:

    def validate_data_set_file(file):
        found_match = False
    # Note we use the python startswith method here.
        if file.getName().startswith('foo'):
            found_match = True

        errors = []
        if found_match:
            errors.append(createFileValidationError(file.getName() + " is not a valid data set."))

        return errors

### Extracting Displaying Metadata

The module that validates a data set may, in addition to performing
validation, implement a function that extracts metadata. This makes it
possible to give the user immediate feedback about how the system
interprets the data, giving her an opportunity to correct any
inconsistencies she detects.

To do this, implement a function call `extract_metadata` in the module
that implements `valadate_data_set_file`. The function
`extract_metadata` should return a dictionary where the keys are the
property codes and values are property values.

#### Example

    def extract_metadata(file):
        return { 'FILE-NAME' : file.getName() }

### Testing

#### Validation Scripts

Scripts can be tested using the command-line client's "testvalid"
command. This command takes the same arguments as put, plus an optional
script parameter. If the script is not specified, the data set is
validated against the server's validation script.

Examples:

    # Use the server script
    ./dss_client.sh testvalid -u username -p password -s openbis-url experiment E-TEST-2 /path/to/data/set

    # Use a local script
    ./dss_client.sh testvalid -u username -p password -s openbis-url experiment E-TEST-2 /path/to/data/set /path/to/script

#### Extract Metadata Scripts

The extract metadata script can be tested with the `testextract` command
in the command-line client. The arguments are the same as for
`testvalid`.

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

{"serverDuration": 119, "requestCorrelationId": "2a740d2e1d20a368"}
