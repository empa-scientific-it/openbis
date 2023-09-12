Data Export
====
 
## Export to File




### Export Lab Notebooks

  
All levels of the *Lab Notebook* and *Inventory* can be exported, using
the 2 export options in the **More..** drop down, as shown below. It is
possible to export metadata only, or metadata and data (up to 10 GBs). 

 

![image info](img/export-space.png)

 

 

In this way, users can export their complete lab notebook with all data
contained in it. Everything is exported to a zip file, which maintains
the same structure used in openBIS. Each folder contains several file
formats (*.txt, .html, .json, .docx*) with the metadata of the
corresponding *Project, Experiment, Object and Datasets* (see below).

 

![image info](img/exported-space-1024x302.png)

  
A link to download the zip file is sent via email to the user. Email
notification needs to be configured by a *system admin* during or after
installation, as explained in [Configure Data Store
Server](../../system-admin-documentation/installation/optional-datastore-server-configuration.md)
[.](https://wiki-bsse.ethz.ch/display/openBISDoc1906/Installation+and+Administrators+Guide+of+the+openBIS+Data+Store+Server)


In a similar way, it is possible to export *Projects*, *Experiments,
Objects, Dataset*s.

###  Export Builder

  
It is also possible to export the content of openBIS using the
**Export** **to ZIP** under **Exports** in the **Utilities** main menu.
 Here users can select items from different *Spaces* and export them by
clicking the **Export Selected** button.

 

Also in this case, a link to download the data is sent to the user via
email, as described above.

 

 

![image info](img/export-to-zip.png)

Updated on April 25, 2023
 
## Export to Zenodo



  
Currently openBIS offers an integration with the **Zenodo** data
repository ([https://zenodo.org/).](https://zenodo.org/)

  
This enables data direct data transfer from openBIS to Zenodo. First of
all the connection to Zenodo needs to be configured by a *system admin*
in the DSS service.properties (see [How to configure the openBIS
DSS)](../../system-admin-documentation/installation/optional-datastore-server-configuration.md)
If this is configured, a lab manager, who has admin rights for the
**Settings,** needs to enable it in the ELN, as explained in [Enable
Transfer to Data
Repositories](../general-admin-users/admins-documentation/enable-transfer-to-data-repositories.md)**.**

###  Create Zenodo Personal Access Token

  
In order to be able to export data to Zenodo, you need a valid Zenodo
account. You also need to create a **personal access token.** This can
be done from the **Applications** under **Settings** in Zenodo, as shown
below:

![image info](img/generate-zenodo-token-1024x498.png)

### Save Zenodo Personal Access Token in openBIS

  
After creating the personal access token in Zenodo, this needs to be
stored in openBIS, with the following procedure:

1.  Go to **User Profile** under **Utilities** in the main menu.
2.  Enable editing.
3.  Add the personal access token from Zenodo.
4.  **Save.**

 

![image info](img/user-profile-session-token.png)


### Export data to Zenodo

  
To export data to Zenodo:

1.  Go to **Exports** -> **Export to Zenodo** under **Utilities** in
    the main menu.
2.  Select the data you want to export from the menu.
3.  enter a **Submission** **Title.**
4.  Click **Export Selected** on top of the export form.
5.  The selected data are transferred as a zip file to Zenodo. You are
    now redirected to Zenodo, where you should fill in additional
    metadata information.
6.  Publish the entry in Zenodo.

 

![image info](img/export-to-zenodo-1024x862.png)

 

 

After you hit the **Publish** button in Zenodo, a new entry with the
details of this submission will be created in the **Publications**
folder in the **Inventory**. Please note that this may take a few
minutes.

 

![image info](img/publications-collection.png)

Updated on April 25, 2023
 
## Export to ETH Research Collection



 

The [ETH Research Collection](https://www.research-collection.ethz.ch/)
is a FAIR repository for publications and research data provided by ETH
Zurich to its scientists.

 

Data can be uploaded to the ETH Research Collection **only by members of
ETH Zurich**. This export feature is only available to ETHZ members.

 

To export data to the ETH Research Collection:

![image info](img/export-to-research-collection-1024x818.png)

1.  Go to **Utilities** -> **Exports** -> **Export to Research
    Collection**.
2.  Select what to export from the tree.
3.  Select the **Submission Type** from the available list: *Data
    collection, Dataset, Image, Model, Sound, Video, Other Research
    Data*.
4.  Select the **Retention Period** that will be used in the ETH
    Research Collection: *10 years, 15 years, indefinite.* This is time
    for which the data will be preserved in the Research Collection.
5.  Click the **Export Selected** button on top of the page.
6.  The selected data are transferred as zip file to the ETH Research
    Collection. You will be redirected to the ETH Research Collection
    and will need to complete the submission process there.

 

![image info](img/publications-collection.png)

 

A new entry with the details of this submission will be created in the
**Publications** folder in the **Inventory** after the submission
process in complete. This may take a few minutes.

 

The size limit for one single export to the ETH Research Collection is
10GB.

 

 

Updated on April 25, 2023
