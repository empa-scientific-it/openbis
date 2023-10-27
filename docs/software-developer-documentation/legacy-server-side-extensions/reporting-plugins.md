Reporting Plugins
=================

Introduction
------------

A reporting plugin runs on the DSS. It creates a report as a table or an
URL for a specified set of data sets or key-value pairs. The user can
invoke a reporting plugin in the openBIS Web application. The result
will be shown as a table or a link.

A reporting plugin is one of the three following types. The differences
are the type of input and output:

-   TABLE\_MODEL: *Input*: A set of data sets. *Output*: A table
-   DSS\_LINK: *Input*: One data set. *Output*: An URL
-   AGGREGATION\_TABLE\_MODEL: *Input*: A set of key-value pairs.
    *Output*: A table

A reporting plugin is configured on the DSS best by introducing a [core
plugin](../server-side-extensions/core-plugins.md) of type
`reporting-plugins`. All reporting plugins have the following properties
in common:

|Property Key|Description|
|--- |--- |
|class|The fully-qualified Java class name of the reporting plugin. The class has to implement IReportingPluginTask.|
|label|The label. It will be shown in the GUI.|
|dataset-types|Comma-separated list of regular expressions. The plugin can create a report only for the data sets of types matching one of the regular expressions. If new data set types are registered with openBIS, the DSS will need to be restarted before the new data set types are known to the processing plugins. This is a mandatory property for reporting plugins of type TABLE_MODEL and DSS_LINK. It will be ignored if the type is AGGREGATION_TABLE_MODEL.|
|properties-file|Path to an optional file with additional properties.|
|servlet.<property>|Properties for an optional servlet. It provides resources referred by URLs in the output of the reporting plugin.
This should be used if the servlet is only needed by this reporting plugin. If other plugins also need this servlet it should be configured as a core plugin of type services.|
|allowed-api-parameter-classes|A comma-separated list of regular expression for fully-qualified class names. Any classes matching on of the regular expressions is allowed as a class of a Java parameter object of a remote API call. For more details see API Security.|
|disallowed-api-parameter-classes|A comma-separated list of regular expression for fully-qualified class names. Any classes matching on of the regular expressions is not allowed as a class of a Java parameter object of a remote API call. For more details see API Security.|

Generic Reporting Plugins
-------------------------

### DecoratingTableModelReportingPlugin

**Type**: TABLE\_MODEL

**Description**: Modifies the output of a reporting plugin of type
TABLE\_MODEL

**Configuration**:

|Property Key|Description|
|--- |--- |
|reporting-plugin.class|The fully-qualified Java class name of the wrapped reporting plugin of type TABLE_MODEL|
|reporting-plugin.<property>|Property of the wrapped reporting plugin.|
|transformation.class|The fully-qualified Java class name of the transformation. It has to implement ITableModelTransformation.|
|transformation.<property>|Property of the transformation to be applied.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DecoratingTableModelReportingPlugin
label = Analysis Summary
dataset-types = HCS_IMAGE_ANALYSIS_DATA
reporting-plugin.class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.TSVViewReportingPlugin
reporting-plugin.separator = ,
transformation.class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.EntityLinksDecorator
transformation.link-columns = BARCODE, GENE
transformation.BARCODE.entity-kind = SAMPLE
transformation.BARCODE.default-space = DEMO
transformation.GENE.entity-kind = MATERIAL
transformation.GENE.material-type = GENE
```


##### Transformations

###### EntityLinksDecorator

**Description**: Changes plain columns into entity links.

**Configuration**:

|Property Key|Description|
|--- |--- |
|link-columns|Comma-separated list of column keys.|
|<column key>.entity-kind|Entity kind of column <column key>. Possible values are MATERIAL and SAMPLE.|
|<column key>.default-space|Optional space code for SAMPLE columns. It will be used if the column value contains only the sample code.|
|<column key>.material-type|Mandatory type code for MATERIAL columns.|

### GenericDssLinkReportingPlugin

**Type**: DSS\_LINK

**Description**: Creates an URL for a file inside the data set.

**Configuration**:

|Property Key|Description|
|--- |--- |
|download-url|Base URL. Contains protocol, domain, and port.|
|data-set-regex|Optional regular expression which specifies the file.|
|data-set-path|Optional relative path in the data set to narrow down the search.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.GenericDssLinkReportingPlugin
label = Summary
dataset-types = MS_DATA
download-url = https://my.domain.org:8443
data-set-regex = summary.*
data-set-path = report
```


### AggregationService

```{warning}
**Import Note on Authorization**
In AggregationServices and IngestionServices, the service programmer needs to ensure proper authorization by himself. He can do so by using the methods from [IAuthorizationService](http://svnsis.ethz.ch/doc/openbis/current/ch/systemsx/cisd/openbis/dss/generic/shared/api/internal/authorization/IAuthorizationService.html). The user id, which is needed when calling these methods, can be obtained from `DataSetProcessingContext` (when using Java), or the variable `userId` (when using Jython).
```

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: An abstract superclass for aggregation service
reporting plugins. An aggregation service reporting plugin takes a hash
map containing user parameters as an argument and returns tabular data
(in the form of a TableModel). The
JythonBasedAggregationServiceReportingPlugin below is a subclass that
allows for implementation of the logic in Jython.

**Configuration**: Dependent on the subclass.

To implement an aggregation service in Java, define a subclass
of `ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AggregationService`.
This subclass must implement the method

    TableModel createReport(Map<String, Object>, DataSetProcessingContext).

**Example**:

**ExampleAggregationServicePlugin**

```java
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
/**
    * @author Chandrasekhar Ramakrishnan
    */
public class ExampleAggregationServicePlugin extends AggregationService
{
    private static final long serialVersionUID = 1L;
    /**
        * Create a new plugin.
        * 
        * @param properties
        * @param storeRoot
        */
    public ExampleAggregationServicePlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }
    @Override
    public TableModel createReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("String");
        builder.addHeader("Integer");
        IRowBuilder row = builder.addRow();
        row.setCell("String", "Hello");
        row.setCell("Integer", 20);
        row = builder.addRow();
        row.setCell("String", parameters.get("name").toString());
        row.setCell("Integer", 30);
        return builder.getTableModel();
    }
}
```


**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ExampleAggregationServicePlugin
label = My Report
```


#### JythonAggregationService

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: Invokes a Jython script to create an aggregation
service report. For more details see [Jython-based Reporting and Processing Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md).

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonAggregationService
label = My Report
script-path = script.py
```


### IngestionService

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: An abstract superclass for aggregation service
reporting plugins that modify entities in the database. A db-modifying
aggregation service reporting plugin takes a hash map containing user
parameters and a transaction as arguments and returns tabular data (in
the form of a TableModel). The transaction is an
[IDataSetRegistrationTransactionV2](https://openbis.ch/javadoc/20.10.x/javadoc-dropbox-api/ch/systemsx/cisd/etlserver/registrator/api/v2/IDataSetRegistrationTransactionV2.html),
the same interface that is used by [dropboxes](../server-side-extensions/dss-dropboxes.md#dropboxes) to register and modify entities. The JythonBasedDbModifyingAggregationServiceReportingPlugin below is a subclass that allows for implementation of the logic in Jython. 

**Configuration**: Dependent on the subclass.

To implement an aggregation service in Java, define a subclass
of `ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IngestionService`.
This subclass must implement the method 

    TableModel process(IDataSetRegistrationTransactionV2 transaction, Map<String, Object> parameters, DataSetProcessingContext context)

**Example**:

**ExampleDbModifyingAggregationService.java**

```java
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
/**
    * An example aggregation service
    * 
    * @author Chandrasekhar Ramakrishnan
    */
public class ExampleDbModifyingAggregationService extends IngestionService<DataSetInformation>
{
    private static final long serialVersionUID = 1L;
    /**
        * @param properties
        * @param storeRoot
        */
    public ExampleDbModifyingAggregationService(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }
    @Override
    public TableModel process(IDataSetRegistrationTransactionV2 transaction,
            Map<String, Object> parameters, DataSetProcessingContext context)
    {
        transaction.createNewSpace("NewDummySpace", null);
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("String");
        builder.addHeader("Integer");
        IRowBuilder row = builder.addRow();
        row.setCell("String", "Hello");
        row.setCell("Integer", 20);
        row = builder.addRow();
        row.setCell("String", parameters.get("name").toString());
        row.setCell("Integer", 30);
        return builder.getTableModel();
    }
}
```


**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ExampleDbModifyingAggregationService
label = My Report
```


#### JythonIngestionService

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: Invokes a Jython script to register and modify entitiesand create an aggregation service report. The script receives a transaction as an argument. For more details see [Jython-based Reporting and Processing Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md).

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|
|share-id|Optional, defaults to 1 when not stated otherwise.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonIngestionService
label = My Report
script-path = script.py
```


### JythonBasedReportingPlugin

**Type:** TABLE\_MODEL

**Description**: Invokes a Jython script to create the report. For more
details see [Jython-based Reporting and Processing
Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md).

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedReportingPlugin
label = My Report
dataset-types = MS_DATA, UNKNOWN
script-path = script.py
```


### TSVViewReportingPlugin

**Type:** TABLE\_MODEL

**Description**: Presents the main data set file as a table. The main
file is specified by the Main Data Set Pattern and the Main Data Set
Path of the data set type. The file can be a CSV/TSV file or an Excel
file. This reporting plugin works only for one data set. 

**Configuration**:

|Property Key|Description|
|--- |--- |
|separator|Separator character. This property will be ignored if the file is an Excel file. Default: TAB character|
|ignore-comments|If true all rows starting with '#' will be ignored. Default: true|
|ignore-trailing-empty-cells|If true trailing empty cells will be ignored. Default: false|
|excel-sheet|Name or index of the Excel sheet used. This property will only be used if the file is an Excel file. Default: 0|
|transpose|If true transpose the original table, that is exchange rows with columns. Default: false|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.TSVViewReportingPlugin
label = My Report
dataset-types = MS_DATA, UNKNOWN
separator = ;
```


Screening Reporting Plugins
---------------------------

### ScreeningJythonBasedAggregationServiceReportingPlugin

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: Invokes a Jython script to create an aggregation
service report. For more details see [Jython-based Reporting and
Processing
Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md). There is some extra support for screening.

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonBasedReportingPlugin
label = My Report
dataset-types = HCS_IMAGE
script-path = script.py
```


### ScreeningJythonBasedDbModifyingAggregationServiceReportingPlugin

**Type:** AGGREGATION\_TABLE\_MODEL

**Description**: Invokes a Jython script to register and modify entities
and create an aggregation service report. The screening-specific version
has access to the screening facade for queries to the imaging database
and is given a screening transaction that supports registering plate
images and feature vectors. For more details see [Jython-based Reporting
and Processing
Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md).

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonBasedReportingPlugin
label = My Report
dataset-types = HCS_IMAGE
script-path = script.py
```


### ScreeningJythonBasedReportingPlugin

**Type:** TABLE\_MODEL

**Description**: Invokes a Jython script to create the report. For more details see [Jython-based Reporting and Processing Plugins](../../uncategorized/jython-based-reporting-and-processing-plugins.md).
There is some extra support for screening.

**Configuration**:

|Property Key|Description|
|--- |--- |
|script-path|Path to the jython script.|

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonBasedAggregationServiceReportingPlugin
label = My Report
script-path = script.py
```
