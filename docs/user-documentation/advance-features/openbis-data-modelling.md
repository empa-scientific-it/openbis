openBIS Data Modelling
======================

Overview

openBIS has a hierarchical data structure:

1.  **Space**: folder with *Code and *Description**
2.  **Project**: folder with *Code* and *Description*
3.  **Experiment/Collection:** folder with *user-defined properties*
4.  **Object**: entity with *user-defined properties* 
5.  **Data set**: folder where data files are stored. A data set has
    *user-defined properties*  
      
![image info](img/125.png)

  

Access to openBIS is controlled either at the *Space* level or *Project*
level.

  

Data model in openBIS ELN-LIMS 
-------------------------------

In the openBIS ELN-LIMS the following structure is used.

## Inventory

The inventory is usually conceived to be shared by all lab members. The
inventory is used to store all materials and protocols (i.e. standard
operating procedures) used in the lab. It is possible to create
additional inventories, for example of instruments and equipment.

The following structure is used in the Inventory:

  

**Materials** (=*Space*)

**Methods** (=*Space*)

  

In the generic ELN-LIMS, the *Materials* folder is empty and everything
has to be defined by an admin user.

The *Methods* folder has default folders for general protocols defined:

  

**Methods** (=*Space*)

**Protocols** (=*Project*)

**General Protocols** (=*Collection*)

  

In the ELN-LIMS for life sciences, some folders are already predefined
in the *Materials* folder. For example:

  

**Materials** (=*Space*)

**Reagents** (=*Project*)

**Chemicals Collection** (=*Collection*)

**Enzymes Collection** (=*Collection*)

**Antibodies Collection** (=*Collection*)

  

An openBIS Instance admin can customise the Inventory folders for the
lab and create the needed Object types ([Register Master Data via the Admin Interface](https://unlimited.ethz.ch/display/openBISDoc2010/Register+Master+Data+via+the+Admin+Interface)).

## Lab Notebook

By default, the lab notebook is organised per user. Each user has a
personal folder (=*Space*), where to create *Projects*, *Experiments*
and *Experimental Steps (*=Objects). Data files can be uploaded to *Data
Sets*. Example structure:

  

**Username** (=*Space*)

**Master thesis project** (=*Project*)

**Experiment 1** (=*Experiment*)

**Experimental step 1** (=*Object*)

**Experimental step 2** (=*Object*)

**Raw Data** (=*Data set*)

  

  

Some labs prefer to organise their lab notebook using a classification
per project rather than per user. In this case an openBIS space would
correspond to a lab Project and an openBIS project could be a
sub-project. Example structure:

  

**SNF projects** (=*Space*)

**Project 1** (=*Project*)

**Experiment** (=*Experiment*)

**Experimental Step** (=*Object*)

**Raw Data** (=*Data set*)

**Project 2** (=*Project*)

**Experiment** (=*Experiment*)

**Experimental step 1** (=*Object*)

**Experimental step 2** (=*Object*)

**Raw Data** (=*Data set*)

  

openBIS parents and children
----------------------------

Objects can be linked to other objects, datasets to other datasets with
N:N relationship. In openBIS these connections are known as *parents*
and *children*.

  

![image info](img/255.png)

  

## Examples of parent-child relationships

1.  One or more samples are derived from one main sample. This is the
    parent of the other samples:  
![image info](img/263.png)
      
2.  One Experimental step is written following a protocol stored in the
    Inventory and using a sample stored in the inventory. The protocol
    and the sample are the parents of the Experimental
![image info](img/268.png)
      
3.  One Experimental Step is done after another and we want to keep
    track of the links between the steps:  
![image info](img/272.png)
      

Protocols
---------

Protocols are standard procedures that can be followed to perform given
experiments in a lab. Usually protocols are stored in the common
inventory and are linked to Experimental procedures using the
parent-child relationships described above. 

The protocol contains the standard steps to follow. The parameters
measured during one experiment following a give protocol should be
recorded in the Experimental Step.

Not all labs have standard procedures in place. In this case, the
*Methods* section of the Inventory does not need to be used.
