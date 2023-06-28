New Entity Type Registration
====
 
## Enable Rich Text Editor or Spreadsheet Widgets

[](# "Print this article")

  
For certain fields, it is possible to enable the use of a Rich Text
Editor (RTE) or a spreadsheet component. *Instance admin* rights are
necessary for this.

  
The **RTE** can be enabled for properties of type
**MULTILINE\_VARCHAR**. The **spreadsheet component** can be enabled for
properties of type **XML**.

  
Procedure:  
  

1.  Properties are defined when creating new entity types (*Datasets*,
    *Objects*, *Experiments/Collections*)
2.  To set a property as RTE or spreadsheet go to the **Settings**,
    under **Utilities**
3.  Select /ELN\_SETTINGS/GENERAL\_ELN\_SETTINGS
4.  Enable editing and scroll down to the **Custom Widgets** section
5.  Click the + button on the same line as **Property Type** and
    **Widget**, as shown below
6.  A new field will appear where you can select the property type and
    the widget. Choices are: **Word Processor** (=RTE) or
    **Spreadsheet.**

![image info](img/custom-widget-gen-settings-1024x293.png)

Updated on October 19, 2022
 
## Enable Objects in dropdowns

[](# "Print this article")

 

 

By default, no Object shows in dropdown menus. Which object types should
show in dropdown menus can be customised from the Settings.

1.  Navigate to the Object Type definitions Extension
2.  Open one Object Type (e.g. Antibody)
3.  Select show in drop downs
4.  Save the Settings

 

![image info](img/Screenshot-2020-02-26-at-13.19.33-1-1024x537.png)

Updated on October 19, 2022
 
## Register masterdata via Excel

[](# "Print this article")

 

It is possible to register openBIS masterdata using an Excel template
from the admin UI.

 

This can be done from the Import menu under the Tools sections, as shown
below. Three options can be chosen for the import:

 

1.  **fail if exists**: if a type or a property already exists in the
    database, the upload will fail.
2.  **ignore if exists**: if a type or a property already exists in the
    database, the upload will ignore this.
3.  **update is exists**: if a type or a property already exists in the
    database, the upload will update existing values.

 

![image info](img/Excel-import-admin-UI-1024x634.png)

 

 

An example template of an Excel masterdata file can be found here:
[masterdata-template](https://openbis.ch/wp-content/uploads/2022/02/masterdata-template.xls)

Please note that in the template we used separate spreadsheets for each
type (Sample, Experiment, Dataset), but it is also possible to have
everything in one single spreadsheet.

 

### Modifying existing types

If you wish to add a new property to an existing
*Collection/Object/Dataset* type, you need to:

1\. add the property in the file

2\. increase the version number of the *Collection/Object/Dataset* type

 

![image info](img/masterdata-type-version.png)

3\. use **Ignore if exists** as upload method. In this case, only the
new property is added to the type.

 

The import on the admin UI allows to register entities in addition to
masterdata. An example template file for this can be found here:
[masterdata-metadata](https://openbis.ch/wp-content/uploads/2022/02/masterdata-metadata.xls)

 

More extensive documentation on the XLS format for masterdata and
metadata registration can be found
[here](https://unlimited.ethz.ch/display/openBISDoc2010/Excel+Import+Service).

Updated on January 13, 2023
 
## Properties overview

[](# "Print this article")

 

The overview of all properties registered in openBIS and their
assignments to types is available under the **Types** section in the
admin UI, as shown below.

 

![image info](img/properties-overview-admin-UI-1024x640.png)

Updated on March 1, 2022
 
## Internal properties and vocabularies

[](# "Print this article")

 

Some properties and vocabularies are internally defined in openBIS.

These can be identified by the **$** sign.

 

Internal properties (e.g. $NAME, $BARCODE, etc) cannot be deleted nor
modified, not even by an instance admin.

 

Internal vocabularies (e.g. $DEFAULT\_COLLECTION\_VIEWS, etc), cannot be
deleted and their existing terms cannot be deleted nor modified, however
an instance admin can add new terms to an internal vocabulary.

 

Updated on January 5, 2023
