# Excel Import Service

-   Created by [Fuentes Serna Juan Mariano
    (ID)](%20%20%20%20/display/~juanf%0A), last modified on [Dec 05,
    2022](/pages/diffpagesbyversion.action?pageId=53745981&selectedPageVersions=7&selectedPageVersions=8 "Show changes")

  

## Introduction

The Excel import service reads xls definitions for both types and
entities and send them to openBIS. It is the replacement of the old
master data scripts adding support for the creation of openBIS entities.

**The goals are:**

For common users an import format with the following features to avoid
the shortcomings of the old format:  

-   Recognisable labels as column names.
-   Multi-type imports.
-   Parents/Children creation and linking on a single import.

For advanced users like consultants and plugin developers a tool that
allows to specify on an Excel sheet:

-   Metadata model.
-   Basic entity structures used for navigation.

## Modes

To support different use cases the import service supports the next
modes, specifying one of them is mandatory.

-   UPDATE IF EXISTS: This one should be the default mode to use to make
    incremental updates.
-   IGNORE EXISTING: This mode should be used when the intention is to
    ignore updates. Existing entities will be ignored. That way is
    possible to avoid unintentionally updating entities and at the same
    time adding new ones.
-   FAIL IF EXISTS: This mode should be used when the intention is to
    fail if anything is found. That way is possible to avoid making any
    unintentional changes.

## Organising Definition Files

All data can be arranged according to the needs of the user, in any
number of files and any number of worksheets. All files have to be in
one directory.

The names of the files and worksheets are ignored by the service, the
user is advised to use descriptive names that they can quickly
remember/refer to later.

If there are dependencies between files they should be submitted
together or an error will be shown.

**Example:**

We want to define vocabularies and sample types with properties using
these vocabularies. We can arrange our files in several ways:

1.  put vocabulary and sample types in separate files named i.e
    vocabulary.xls and sample\_types.xlsx respectively
2.  put vocabulary and sample types in different worksheets in the same
    xls file
3.  put everything in one worksheet in the same file

## Organising Definitions

**Type definitions:**

The order of type definitions is not important for the Excel import
service, with exception of Vocabularies, those need to be placed before
the property types that use them.

**Entity definitions:**

Type definitions for the entities should already exist in the database
at the time when entities are registered. Generally Entity definitions
are placed at the end.

### Text cell formatting (colours, fonts, font style, text decorations)

All types of formatting are permitted, and users are encouraged to use
them to make their excel files more readable. Adding any non text
element (table, clipart) will cause the import to fail.

![image info](img/94.png)

(A valid, but not easily readable, example)

### Definition, rows and sheet formatting

-   A valid sheet has to start with definition on the first row.
-   Each definition has to be separated by one empty row.
-   Two or more consecutive empty rows mark the end of the definitions.
-   Empty spaces at the beginning or end of headers are silently
    eliminated.

  

If any content is placed after two consecutive empty rows it will result
in an error. This is to alert the user and avoid silently ignoring
content.

Header rows **NEED TO BE** a valid attribute of the entity or entity
type, property label or property code.

Any unintended header will result in an error. This is to avoid possible
misspellings and avoid silently ignoring content.

## Entity Types Definitions

All entity types can be created*.* There are differences due to the
nature of the defined elements themselves.

### Vocabulary and Vocabulary Term

Vocabulary

<table style="width:99%;">
<colgroup>
<col style="width: 49%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Description</td>
<td>Yes</td>
</tr>
</tbody>
</table>

Vocabulary Term

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Label</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Description</td>
<td>Yes</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>VOCABULARY_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$STORAGE.STORAGE_VALIDATION_LEVEL</td>
<td>Validation Level</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Label</strong></td>
<td><strong>Description</strong></td>
</tr>
<tr class="odd">
<td>1</td>
<td>RACK</td>
<td>Rack Validation</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>BOX</td>
<td>Box Validation</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>BOX_POSITION</td>
<td>Box Position Validation</td>
<td><br />
</td>
</tr>
</tbody>
</table>

Experiment Type
---------------

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Description</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Validation script</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Ontology Id</td>
<td>No</td>
</tr>
<tr class="even">
<td>Ontology Version</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Ontology Annotation Id</td>
<td>No</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>EXPERIMENT_TYPE</strong></td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td style="text-align: left;"><strong>Code</strong></td>
<td style="text-align: left;"><strong>Description</strong></td>
<td><strong>Validation script</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>2</td>
<td style="text-align: left;">DEFAULT_EXPERIMENT</td>
<td style="text-align: left;"><br />
</td>
<td>date_range_validation.py</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
</tbody>
</table>

### Sample Type

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Description</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Auto generate codes</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Validation script</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Generate code prefix</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Ontology Id</td>
<td>No</td>
</tr>
<tr class="even">
<td>Ontology Version</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Ontology Annotation Id</td>
<td>No</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>SAMPLE_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Auto generate codes</strong></td>
<td><strong>Validation script</strong></td>
<td><strong>Generated code prefix</strong></td>
</tr>
<tr class="odd">
<td>2</td>
<td>STORAGE_POSITION</td>
<td><br />
</td>
<td>TRUE</td>
<td>storage_position_validation.py</td>
<td>STO</td>
</tr>
</tbody>
</table>

### Dataset Type

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Description</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Validation script</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Ontology Id</td>
<td>No</td>
</tr>
<tr class="even">
<td>Ontology Version</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Ontology Annotation Id</td>
<td>No</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>DATASET_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Validation script</strong></td>
</tr>
<tr class="odd">
<td>1</td>
<td>RAW_DATA</td>
<td><br />
</td>
<td><br />
</td>
</tr>
</tbody>
</table>

### Property Type

A property type can exist unassigned to an entity type or assigned to an
entity type.

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory Assigned</th>
<th>Mandatory Unassigned</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Version</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Mandatory</td>
<td>No</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Show in edit views</td>
<td>No</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Section</td>
<td>No</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Property label</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Data type</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Vocabulary code</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Description</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Metadata</td>
<td>No</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Dynamic script</td>
<td>No</td>
<td>No</td>
</tr>
<tr class="even">
<td>Ontology Id</td>
<td>No</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Ontology Version</td>
<td>No</td>
<td>No</td>
</tr>
<tr class="even">
<td>Ontology Annotation Id</td>
<td>No</td>
<td>No</td>
</tr>
</tbody>
</table>

A property type requires a data type to be defined, valid data types
are.

<table>
<thead>
<tr class="header">
<th>Data type</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>INTEGER</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>REAL</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>VARCHAR</td>
<td>Text of any length but displayed as a single line field.</td>
</tr>
<tr class="even">
<td>MULTILINE_VARCHAR</td>
<td>Text of any length but displayed as a multi line field.</td>
</tr>
<tr class="odd">
<td>HYPERLINK</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>BOOLEAN</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>CONTROLLEDVOCABULARY</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>XML</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>TIMESTAMP</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>DATE</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>SAMPLE</td>
<td>Sample of any type.</td>
</tr>
<tr class="even">
<td>SAMPLE:&lt;SAMPLE_TYPE&gt;</td>
<td>Sample of the indicated type.</td>
</tr>
</tbody>
</table>

  

**Example Unassigned Property**

In this case, the property is registered without being assigned to a
type, and  the block of property types uses the PROPERTY\_TYPE block.

<table>
<tbody>
<tr class="odd">
<td><strong>PROPERTY_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Mandatory</strong></td>
<td><strong>Show in edit views</strong></td>
<td><strong>Section</strong></td>
<td><strong>Property label</strong></td>
<td><strong>Data type</strong></td>
<td><strong>Vocabulary code</strong></td>
<td><strong>Description</strong></td>
</tr>
<tr class="odd">
<td>1</td>
<td>$WELL.COLOR_ENCODED_ANNOTATION</td>
<td>FALSE</td>
<td>TRUE</td>
<td><br />
</td>
<td>Color Annotation</td>
<td>CONTROLLEDVOCABULARY</td>
<td>$WELL.COLOR_ENCODED_ANNOTATIONS</td>
<td>Color Annotation for plate wells</td>
</tr>
<tr class="even">
<td>1</td>
<td>ANNOTATION.SYSTEM.COMMENTS</td>
<td>FALSE</td>
<td>TRUE</td>
<td><br />
</td>
<td>Comments</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Comments</td>
</tr>
<tr class="odd">
<td>1</td>
<td>ANNOTATION.REQUEST.QUANTITY_OF_ITEMS</td>
<td>FALSE</td>
<td>TRUE</td>
<td><br />
</td>
<td>Quantity of Items</td>
<td>INTEGER</td>
<td><br />
</td>
<td>Quantity of Items</td>
</tr>
<tr class="even">
<td>2</td>
<td>$BARCODE</td>
<td>FALSE</td>
<td>FALSE</td>
<td><br />
</td>
<td>Custom Barcode</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Custom Barcode</td>
</tr>
</tbody>
</table>

**Example Assigned**

In this case the property types are assigned to a sample type and the
block of property types belong to the entity type block (SAMPLE\_TYPE in
this case).

<table>
<tbody>
<tr class="odd">
<td><strong>SAMPLE_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Auto generate codes</strong></td>
<td><strong>Validation script</strong></td>
<td><strong>Generated code prefix</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>ENTRY</td>
<td><br />
</td>
<td>TRUE</td>
<td><br />
</td>
<td>ENTRY</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Mandatory</strong></td>
<td><strong>Show in edit views</strong></td>
<td><strong>Section</strong></td>
<td><strong>Property label</strong></td>
<td><strong>Data type</strong></td>
<td><strong>Vocabulary code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Metadata</strong></td>
<td><strong>Dynamic script</strong></td>
</tr>
<tr class="odd">
<td>1</td>
<td>$NAME</td>
<td>FALSE</td>
<td>TRUE</td>
<td>General info</td>
<td>Name</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Name</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$SHOW_IN_PROJECT_OVERVIEW</td>
<td>FALSE</td>
<td>TRUE</td>
<td>General info</td>
<td>Show in project overview</td>
<td>BOOLEAN</td>
<td><br />
</td>
<td>Show in project overview page</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$DOCUMENT</td>
<td>FALSE</td>
<td>TRUE</td>
<td>General info</td>
<td>Document</td>
<td>MULTILINE_VARCHAR</td>
<td><br />
</td>
<td>Document</td>
<td>{ "custom_widget" : "Word Processor" }</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$ANNOTATIONS_STATE</td>
<td>FALSE</td>
<td>FALSE</td>
<td><br />
</td>
<td>Annotations State</td>
<td>XML</td>
<td><br />
</td>
<td>Annotations State</td>
<td><br />
</td>
<td><br />
</td>
</tr>
</tbody>
</table>

### Entity Type Validation Script and Property Type Dynamic Script

Scripts have to reside in *.py* files in the *scripts* directory within
the folder that contains the Excel files.

Within *scripts,* files can be organised in any suitable setup:

In order to refer to a validation or dynamic script
(e.g. *storage\_position\_validation.py* below), the relative path (from
the *scripts* directory) to the file has to be provided in the relevant
column. See the example columns below.

**Example**

**<span
![image info](img/932.png)

<table>
<tbody>
<tr class="odd">
<td><strong>SAMPLE_TYPE</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Auto generate codes</strong></td>
<td><strong>Validation scriptƒgre</strong></td>
<td><strong>Generated code prefix</strong></td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>2</td>
<td>STORAGE_POSITION</td>
<td><br />
</td>
<td>TRUE</td>
<td>storage_position_validation.py</td>
<td>STO</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Version</strong></td>
<td><strong>Code</strong></td>
<td><strong>Mandatory</strong></td>
<td><strong>Show in edit views</strong></td>
<td><strong>Section</strong></td>
<td><strong>Property label</strong></td>
<td><strong>Data type</strong></td>
<td><strong>Vocabulary code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Metadata</strong></td>
<td><strong>Dynamic script</strong></td>
</tr>
<tr class="odd">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_CODE</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Code</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Storage Code</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_RACK_ROW</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Rack Row</td>
<td>INTEGER</td>
<td><br />
</td>
<td>Number of Rows</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_RACK_COLUMN</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Rack Column</td>
<td>INTEGER</td>
<td><br />
</td>
<td>Number of Columns</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_BOX_NAME</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Box Name</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Box Name</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_BOX_SIZE</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Box Size</td>
<td>CONTROLLEDVOCABULARY</td>
<td>$STORAGE_POSITION.STORAGE_BOX_SIZE</td>
<td>Box Size</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_BOX_POSITION</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage Box Position</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Box Position</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$STORAGE_POSITION.STORAGE_USER</td>
<td>FALSE</td>
<td>TRUE</td>
<td>Physical Storage</td>
<td>Storage User Id</td>
<td>VARCHAR</td>
<td><br />
</td>
<td>Storage User Id</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td>1</td>
<td>$XMLCOMMENTS</td>
<td>FALSE</td>
<td>FALSE</td>
<td><br />
</td>
<td>Comments</td>
<td>XML</td>
<td><br />
</td>
<td>Comments log</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>1</td>
<td>$ANNOTATIONS_STATE</td>
<td>FALSE</td>
<td>FALSE</td>
<td><br />
</td>
<td>Annotations State</td>
<td>XML</td>
<td><br />
</td>
<td>Annotations State</td>
<td><br />
</td>
<td><p><br />
</p>
<p><br />
</p></td>
</tr>
</tbody>
</table>

### Entity Types Versioning

#### General Usage 

Version is a mandatory field for entity types, it just starts at 1; and
during updating a type definition is expected to increment it;
otherwise, the system will ignore the changes.

#### Explanation

Additionally, use the keyword FORCE to reinstall the type even if is
present and has been deleted.

The system keeps track of what versions of entities have been installed
storing this information, so in the future when one updates their types,
the version specified in the spreadsheet is checked against the stored
version.

For every TYPE found in the Excel sheet the next algorithm is performed:

    IF ENTITY OR (TYPE.Version > STORED_VERSION) OR (TYPE.Version == FORCE): // If is a new version
        IF ITEM NOT EXISTS in openBIS:
            CREATE ITEM                             
        ELSE: // Doesn't exist branch
            IF FAIL_IF_EXISTS:
                THROW EXCEPTION
            IF UPDATE_IF_EXISTS:
                UPDATE ITEM
            ELSE IF IGNORE_EXISTING:
                PASS // Ignore as requested
    ELSE:
        PASS // Ignore object that have not been updated

  

## Entity Definitions

Most entities can be created, excluding DataSets*.* There are
differences due to the nature of the defined elements themselves.

General Rules:

-   Header order is arbitrary.
-   When referring to another entity only Identifiers are allowed.
    Sample Variables are the only exception.
-   Vocabulary values in property value rows can be referred to by
    either the vocabulary term code or the vocabulary term label.

  

If a mandatory header is missing it results in an error.

Repeated headers will result in an error, in case a Property shares
Label with an Attribute is encouraged to use the property code instead.

### Space

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Description</td>
<td>Yes</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>SPACE</strong></td>
<td style="text-align: left;"><br />
</td>
</tr>
<tr class="even">
<td><strong>Code</strong></td>
<td style="text-align: left;"><strong>Description</strong></td>
</tr>
<tr class="odd">
<td>ELN_SETTINGS</td>
<td style="text-align: left;">ELN Settings</td>
</tr>
<tr class="even">
<td>DEFAULT_LAB_NOTEBOOK</td>
<td style="text-align: left;">Default Lab Notebook</td>
</tr>
<tr class="odd">
<td>METHODS</td>
<td style="text-align: left;">Folder for methods</td>
</tr>
<tr class="even">
<td>MATERIALS</td>
<td style="text-align: left;">Folder for th materials</td>
</tr>
<tr class="odd">
<td>STOCK_CATALOG</td>
<td style="text-align: left;">Folder for the catalog</td>
</tr>
<tr class="even">
<td>STOCK_ORDERS</td>
<td style="text-align: left;">Folder for orders</td>
</tr>
<tr class="odd">
<td>PUBLICATIONS</td>
<td style="text-align: left;">Folder for publications</td>
</tr>
</tbody>
</table>

### Project

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Identifier</td>
<td>Yes on UPDATES, ignored on INSERT</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Space</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Description</td>
<td>Yes</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>PROJECT</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Identifier</strong></td>
<td style="text-align: left;"><strong>Code</strong></td>
<td><strong>Description</strong></td>
<td><strong>Space</strong></td>
</tr>
<tr class="odd">
<td>/DEFAULT_LAB_NOTEBOOK/DEFAULT_PROJECT</td>
<td style="text-align: left;">DEFAULT_PROJECT</td>
<td>Default Project</td>
<td>DEFAULT_LAB_NOTEBOOK</td>
</tr>
<tr class="even">
<td>/METHODS/PROTOCOLS</td>
<td style="text-align: left;">PROTOCOLS</td>
<td>Protocols</td>
<td>METHODS</td>
</tr>
<tr class="odd">
<td>/STOCK_CATALOG/PRODUCTS</td>
<td style="text-align: left;">PRODUCTS</td>
<td>Products</td>
<td>STOCK_CATALOG</td>
</tr>
<tr class="even">
<td>/STOCK_CATALOG/SUPPLIERS</td>
<td style="text-align: left;">SUPPLIERS</td>
<td>Suppliers</td>
<td>STOCK_CATALOG</td>
</tr>
<tr class="odd">
<td>/STOCK_CATALOG/REQUESTS</td>
<td style="text-align: left;">REQUESTS</td>
<td>Requests</td>
<td>STOCK_CATALOG</td>
</tr>
<tr class="even">
<td>/STOCK_ORDERS/ORDERS</td>
<td style="text-align: left;">ORDERS</td>
<td>Orders</td>
<td>STOCK_ORDERS</td>
</tr>
<tr class="odd">
<td>/ELN_SETTINGS/TEMPLATES</td>
<td style="text-align: left;">TEMPLATES</td>
<td>Templates</td>
<td>ELN_SETTINGS</td>
</tr>
<tr class="even">
<td>/PUBLICATIONS/PUBLIC_REPOSITORIES</td>
<td style="text-align: left;">PUBLIC_REPOSITORIES</td>
<td>Public Repositories</td>
<td>PUBLICATIONS</td>
</tr>
</tbody>
</table>

### Experiment

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Identifier</td>
<td>Yes on UPDATES, ignored on INSERT</td>
</tr>
<tr class="even">
<td>Code</td>
<td>Yes</td>
</tr>
<tr class="odd">
<td>Project</td>
<td>Yes</td>
</tr>
<tr class="even">
<td>Property Code</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Property Label</td>
<td>No</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>EXPERIMENT</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Experiment type</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td><strong>COLLECTION</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Identifier</strong></td>
<td style="text-align: left;"><strong>Code</strong></td>
<td><strong>Project</strong></td>
<td><strong>Name</strong></td>
<td><strong>Default object type</strong></td>
</tr>
<tr class="odd">
<td>/METHODS/PROTOCOLS/GENERAL_PROTOCOLS</td>
<td style="text-align: left;">GENERAL_PROTOCOLS</td>
<td>/METHODS/PROTOCOLS</td>
<td>General Protocols</td>
<td>GENERAL_PROTOCOL</td>
</tr>
<tr class="even">
<td>/STOCK_CATALOG/PRODUCTS/PRODUCT_COLLECTION</td>
<td style="text-align: left;">PRODUCT_COLLECTION</td>
<td>/STOCK_CATALOG/PRODUCTS</td>
<td>Product Collection</td>
<td>PRODUCT</td>
</tr>
<tr class="odd">
<td>/STOCK_CATALOG/SUPPLIERS/SUPPLIER_COLLECTION</td>
<td style="text-align: left;">SUPPLIER_COLLECTION</td>
<td>/STOCK_CATALOG/SUPPLIERS</td>
<td>Supplier Collection</td>
<td>SUPPLIER</td>
</tr>
<tr class="even">
<td>/STOCK_CATALOG/REQUESTS/REQUEST_COLLECTION</td>
<td style="text-align: left;">REQUEST_COLLECTION</td>
<td>/STOCK_CATALOG/REQUESTS</td>
<td>Request Collection</td>
<td>REQUEST</td>
</tr>
<tr class="odd">
<td>/STOCK_ORDERS/ORDERS/ORDER_COLLECTION</td>
<td style="text-align: left;">ORDER_COLLECTION</td>
<td>/STOCK_ORDERS/ORDERS</td>
<td>Order Collection</td>
<td>ORDER</td>
</tr>
<tr class="even">
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td style="text-align: left;">TEMPLATES_COLLECTION</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>Template Collection</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td>/PUBLICATIONS/PUBLIC_REPOSITORIES/PUBLICATIONS_COLLECTION</td>
<td style="text-align: left;">PUBLICATIONS_COLLECTION</td>
<td>/PUBLICATIONS/PUBLIC_REPOSITORIES</td>
<td>Publications Collection</td>
<td>PUBLICATION</td>
</tr>
</tbody>
</table>

### Sample

<table>
<thead>
<tr class="header">
<th>Headers</th>
<th>Mandatory</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>$</td>
<td>No</td>
</tr>
<tr class="even">
<td>Identifier</td>
<td>Yes on UPDATES, ignored on INSERT</td>
</tr>
<tr class="odd">
<td>Code</td>
<td>No</td>
</tr>
<tr class="even">
<td>Project</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Experiment</td>
<td>No</td>
</tr>
<tr class="even">
<td>Auto generate code</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Parents</td>
<td>No</td>
</tr>
<tr class="even">
<td>Children</td>
<td>No</td>
</tr>
<tr class="odd">
<td>Property Code</td>
<td>No</td>
</tr>
<tr class="even">
<td>Property Label</td>
<td>No</td>
</tr>
</tbody>
</table>

**Example**

<table>
<tbody>
<tr class="odd">
<td><strong>SAMPLE</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Sample type</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td><strong>ORDER</strong></td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>$</strong></td>
<td style="text-align: left;"><strong>Identifier</strong></td>
<td><strong>Code</strong></td>
<td><strong>Space</strong></td>
<td><strong>Project</strong></td>
<td><strong>Experiment</strong></td>
<td><strong>Order Status</strong></td>
</tr>
<tr class="odd">
<td><br />
</td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE</td>
<td>ORDER_TEMPLATE</td>
<td>ELN_SETTINGS</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td>Not yet ordered</td>
</tr>
</tbody>
</table>

#### Defining Parent and Children in Samples

Parent and child columns can be used to define relations between
samples. Samples can be addressed by:

1.  $ : Variables, only really useful during batch inserts for samples
    with autogenerated codes since Identifiers can't be known. Variables
    SHOULD start with $.
2.  Identifiers

  

Parents and children SHOULD be separated by an end of line, each sample
should be in its own line.

<table>
<tbody>
<tr class="odd">
<td><strong>SAMPLE</strong></td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>Sample type</strong></td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="odd">
<td><strong>ORDER</strong></td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
<td><br />
</td>
</tr>
<tr class="even">
<td><strong>$</strong></td>
<td style="text-align: left;"><strong>Parents</strong></td>
<td style="text-align: left;"><strong>Children</strong></td>
<td style="text-align: left;"><strong>Identifier</strong></td>
<td><strong>Code</strong></td>
<td><strong>Space</strong></td>
<td><strong>Project</strong></td>
<td><strong>Experiment</strong></td>
<td><strong>Order Status</strong></td>
</tr>
<tr class="odd">
<td><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_A</td>
<td>ORDER_TEMPLATE</td>
<td>ELN_SETTINGS</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td>Not yet ordered</td>
</tr>
<tr class="even">
<td>$B</td>
<td style="text-align: left;"><p><br />
</p></td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_B</td>
<td>ORDER_TEMPLATE</td>
<td>ELN_SETTINGS</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td>Not yet ordered</td>
</tr>
<tr class="odd">
<td><br />
</td>
<td style="text-align: left;"><p>/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_A</p>
<p>$B</p></td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_D</td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_C</td>
<td>ORDER_TEMPLATE</td>
<td>ELN_SETTINGS</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td>Not yet ordered</td>
</tr>
<tr class="even">
<td><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;"><br />
</td>
<td style="text-align: left;">/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE_D</td>
<td>ORDER_TEMPLATE</td>
<td>ELN_SETTINGS</td>
<td>/ELN_SETTINGS/TEMPLATES</td>
<td>/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION</td>
<td>Not yet ordered</td>
</tr>
</tbody>
</table>

### Properties and Sample Variables

As a general rule, properties would only accept data of the specified
type.

Sample properties would typically require an Identifier to be given but
a variable '$' could be used instead for a sample declared at any point
of the document, including cyclical dependencies. This is useful for
scenarios where Sample codes are autogenerated and can't be known in
advance.

### Entity Versioning

They don't have versioning, only entity types have versioning.

## Master Data as a Core Plugin

The master data plugin is an AS core plugin.

Directory structure **(important)** :

![image info](img/1806.png)

Use standard initialize-master-data.py handle as it is ingested by
openbis on startup. **Excel files** should be organised **in
*master-data*** **directory** in the same plugin and **scripts** should
be contained in ***scripts* directory** under master-data.

Contents of initialize-master-data.py:

    from ch.ethz.sis.openbis.generic.server.asapi.v3 import ApplicationServerApi
    from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions
    from ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl import MasterDataRegistrationHelper
    import sys

    helper = MasterDataRegistrationHelper(sys.path)
    api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)
    sessionToken = api.loginAsSystem()
    props = CustomASServiceExecutionOptions().withParameter('xls', helper.listXlsByteArrays()) \
        .withParameter('xls_name', 'ELN-LIMS-LIFE-SCIENCES').withParameter('update_mode', 'UPDATE_IF_EXISTS') \
        .withParameter('scripts', helper.getAllScripts())
    result = api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import-api"), props)

  

There are following parameters to fill (Easiest is to use
MasterDataRegistrationHelper to evaluate parameter values):

-   'xls': Array of excel files. It can be easily acquired by calling
    helper.listXlsByteArrays or listCsvByteArrays.
-   'xls\_name' - Name for the batch, it is used by versioning system.
-   'update\_mode' - See "Modes" section.
-   'scripts' - if you have any scripts in your data, provide them here.
    It is easiest to get it with MasterDataRegistrationHelper
    getAllScripts function.

'results' object is a summary of what has been created.

**Example**

For an complete up to date example, please check the
eln-lims-life-sciences plugin that ships with the installer or on the
official Git repository:

<https://sissource.ethz.ch/sispub/openbis/-/tree/master/openbis_standard_technologies/dist/core-plugins/eln-lims-life-sciences/1/as>

Or download the complete plugin using the next link:

<https://sissource.ethz.ch/sispub/openbis/-/archive/master/openbis-master.zip?path=openbis_standard_technologies/dist/core-plugins/eln-lims-life-sciences>

## Known Limitations

-   Property type assignments to entity types cannot be updated since
    the current V3 API does not support this functionality. This means
    that a change in the order of assignments or group names during an
    update will be ignored.