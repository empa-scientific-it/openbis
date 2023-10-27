# Jython DataSetValidator

## Overview

Jython dataset validators are an option for implementing validation of
data sets using the python scripting language when using a jython
dropbox. See [Dropboxes](../software-developer-documentation/server-side-extensions/dss-dropboxes.md) for the
basic configuration. The validators can also be run on clients, either
the command-line dss client or the web start Data Set Batch Uploader,
though there are some additional restrictions on which scripts can be
run within the batch uploader.

## Configuration

To configure a validator, add the configuration parameter
"validation-script-path" to the thread definition. For example:

**plugin.properties**

```
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
```


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

<https://sissource.ethz.ch/sispub/openbis/-/blob/master/server-original-data-store/source/java/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1/validation/ValidationError.java>

## Example scripts

One can use both python standard libraries and Java libraries.

### Simple script using python libraries:

```py
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
```


### Simple script using only java libraries:

```py
def validate_data_set_file(file):
    found_match = False
# Note we use the python startswith method here.
    if file.getName().startswith('foo'):
        found_match = True

    errors = []
    if found_match:
        errors.append(createFileValidationError(file.getName() + " is not a valid data set."))

    return errors
```


## Extracting Displaying Metadata

The module that validates a data set may, in addition to performing
validation, implement a function that extracts metadata. This makes it
possible to give the user immediate feedback about how the system
interprets the data, giving her an opportunity to correct any
inconsistencies she detects.

To do this, implement a function call `extract_metadata` in the module
that implements `valadate_data_set_file`. The function
`extract_metadata` should return a dictionary where the keys are the
property codes and values are property values.

### Example

```py
def extract_metadata(file):
    return { 'FILE-NAME' : file.getName() }
```


## Testing

### Validation Scripts

Scripts can be tested using the command-line client's "testvalid"
command. This command takes the same arguments as put, plus an optional
script parameter. If the script is not specified, the data set is
validated against the server's validation script.

Examples:

```bash
# Use the server script
./dss_client.sh testvalid -u username -p password -s openbis-url experiment E-TEST-2 /path/to/data/set

# Use a local script
./dss_client.sh testvalid -u username -p password -s openbis-url experiment E-TEST-2 /path/to/data/set /path/to/script
```


### Extract Metadata Scripts

The extract metadata script can be tested with the `testextract` command
in the command-line client. The arguments are the same as for
`testvalid`.
