Multi Group Set Up
====
 
## General ELN Settings

[](# "Print this article")

 

In a multi-group instance an *Instance admin* can customise the General
ELN Settings.

 

The Settings can be access from the main menu, under **Utilities**.

 

![image info](img/menu-settings-multigroup.png)

 

The General ELN Settings are Settings that are not specific to any of
the defined groups group , as shown below.

 

![image info](img/settings-selection-multigroup.png)

 

The General ELN Settings consist of two parts:

 

1.  **Instance Settings**. These settings affect the whole instance, it
    is not possible to customise them on a group level.
2.  **Group Settings**. These settings affect all general *Spaces* that
    do not belong to any group defined in the configuration file
    (see [openBIS set up for multi group
    instances](https://unlimited.ethz.ch/display/openBISDoc2010/User+Group+Management+for+Multi-groups+openBIS+Instances)).
    This is the case, for example, if *Spaces* are manually created and
    they do not belong to any group (see [Create new ELN
    Spaces](https://openbis.ch/index.php/docs/admin-documentation/space-management/create-new-eln-spaces/)).

 

![image info](img/general-settings-1024x545.png)

 

Spaces that do not belong to any group do not have a group prefix. In
the example below **Publications** do not belong to any group in the
Inventory.

![image info](img/labnotebook-non-group-spaces-multigroup.png)

 

 

and **Horizon**, **Snf** do not belong to any group in the Lab notebook.

 

![image info](img/inventory-non-group-spaces-multigroup.png)

 

 

## Instance Settings

 

1.  **Custom widget**s. This section allows to enable the Rich Text
    Editor or Spreadsheet component for a given field, as described
    in [Enable Rich Text Editor or Spreadsheet
    Widgets;](https://openbis.ch/index.php/docs/admin-documentation/new-entity-type-registration/enable-rich-text-editor-or-spreadsheet-widgets/)
2.  **Forced Monospace Font**. This section allows to force the use of
    monospace font (i.e. fixed width) for selected MULTILINE\_VARCHAR
    properties. This is useful for example for plasmid sequences.
3.  **Dataset types for filenames**. This section allows to associate
    files with a given extension to a specific dataset type, as
    described in [Associate File Types to Dataset
    Types](https://openbis.ch/index.php/docs/admin-documentation/associate-file-types-to-dataset-types/).

 

 

## Group Settings

 

1.  **Storages**. In this section the storages for samples to be used in
    *Spaces* not belonging to any predefined group (see above), can be
    created, as described in [Configure Lab
    Storage;](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-materials-and-samples/configure-lab-storage/)
2.  **Templates**. In this section, the templates for a given *Object
    type* to be used in *Spaces* not belonging to any predefined group
    (see above) can be created, as described in [Create Templates for
    Objects](https://openbis.ch/index.php/docs/admin-documentation/create-templates-for-objects/);
3.  **Object types definition extension**. In this section, it is
    possible to:
    1.  Define if one *Object type* is a protocol. If an *Object type*
        is defined as a protocol, it is possible to create a local copy
        of it under an Experiment, when linking to it as a parent, as
        described in [Enable Protocols in
        Settings;](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-protocols/enable-protocols-in-settings/)
    2.  Enable the storage widget for an *Object type,* as described
        in [Enable Storage Widget on Sample
        Forms](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-materials-and-samples/enable-storage-widget-on-sample-forms/)
    3.  Define if the *Object type* should be shown in drop downs, as
        described in [Enable Objects in
        dropdowns](https://openbis.ch/index.php/docs/admin-documentation/new-entity-type-registration/enable-objects-in-dropdowns/);
    4.  Define if the *Object type* should be shown in the main menu
        under the Lab notebook section. By default objects are not shown
        in the main menu in the Inventory section.
    5.  Customise the *Parents* and *Children* sections for an *Object
        type* as described in [Customise Parents and Children Sections
        in Object
        Forms](https://openbis.ch/index.php/docs/admin-documentation/customise-parents-and-children-sections-in-object-forms/);
4.  **Inventory Spaces**. It is possible to move *Spaces* from the
    Inventory section to the Lab notebook section and vice-versa as
    described in [Move Spaces between Lab Notebook and
    Inventory](https://openbis.ch/index.php/docs/admin-documentation/space-management/move-space-between-lab-notebook-and-inventory/)
5.  **Main menu**. The main menu for the *Spaces* that do not belong to
    any predefined group (see above) can be customised here, as
    described in [Customise the Main
    Menu;](https://openbis.ch/index.php/docs/admin-documentation/customise-the-main-menu/)
6.  **Miscellaneous**. In this section it is possible to:
    1.  Show the dataset archiving buttons in *Spaces* that do not
        belong to any predefined group. Please note that this is not
        available by default, but the infrastructure for [archiving to
        tapes](https://openbis.ch/index.php/docs/user-documentation/data-archiving/)
        (StrongBox/StrongLink) needs to be put in place by a *system
        admin ([Multi data set
        archiving](https://unlimited.ethz.ch/display/openBISDoc2010/Multi+data+set+archiving))*.
    2.  Hide sections by default in *Spaces* that not belong to any
        predefined group. By default some sections in some forms are
        hidden:
        1.  Description in *Spaces* and *Projects*.
        2.  Identification info in *Spaces*, *Projects*, *Experiments*,
            *Objects*, *Datasets*.

By unchecking this option, these sections will be shown by default.

 

Updated on April 26, 2023
 
## Group ELN Settings

[](# "Print this article")

 

In a multi group instance a *group admin* or *Instance admin* can
customise the ELN Settings for the group.

 

The group Settings can be selected from the **Settings** in the main
menu.

 

![image info](img/menu-settings-multigroup.png)

 

The Settings for the relevant group can be selected from the available
dropdown, as shown below.

 

 

![image info](img/settings-selection-multigroup.png)

 

In the group settings the following is configurable:

 

1.  **Storages**. In this section the group storages for samples can be
    created, as described in [Configure Lab
    Storage;](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-materials-and-samples/configure-lab-storage/)
2.  **Templates**. In this section, the templates for a given *Object
    type* can be created, as described in [Create Templates for
    Objects](https://openbis.ch/index.php/docs/admin-documentation/create-templates-for-objects/);
3.  **Object types definition extension**. In this section, it is
    possible to:
    1.  Define if one *Object type* is a protocol. If an *Object type*
        is defined as a protocol, it is possible to create a local copy
        of it under an Experiment, when linking to it as a parent, as
        described in [Enable Protocols in
        Settings;](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-protocols/enable-protocols-in-settings/)
    2.  Enable the storage widget for an *Object type,* as described
        in [Enable Storage Widget on Sample
        Forms](https://openbis.ch/index.php/docs/admin-documentation/customise-inventory-of-materials-and-samples/enable-storage-widget-on-sample-forms/)
    3.  Define if the *Object type* should be shown in drop downs, as
        described in [Enable Objects in
        dropdowns](https://openbis.ch/index.php/docs/admin-documentation/new-entity-type-registration/enable-objects-in-dropdowns/);
    4.  Define if the *Object type* should be shown in the main menu
        under the Lab notebook section. By default objects are not shown
        in the main menu in the Inventory section.
    5.  Customise the Parents and Children sections for an *Object type*
        as described in [Customise Parents and Children Sections in
        Object
        Forms](https://openbis.ch/index.php/docs/admin-documentation/customise-parents-and-children-sections-in-object-forms/);
4.  **Inventory Spaces**. It is possible to move Spaces from the
    Inventory section to the Lab notebook section and vice-versa as
    described in [Move Spaces between Lab Notebook and
    Inventory](https://openbis.ch/index.php/docs/admin-documentation/space-management/move-space-between-lab-notebook-and-inventory/)
5.  **Main menu**. The main menu for the group can be customised here,
    as described in [Customise the Main
    Menu;](https://openbis.ch/index.php/docs/admin-documentation/customise-the-main-menu/)
6.  **Miscellaneous**. In this section it is possible to:
    1.  Show the dataset archiving buttons for the group. Please note
        that this is not available by default, but the infrastructure
        for [archiving to
        tapes](https://openbis.ch/index.php/docs/user-documentation/data-archiving/)
        (StrongBox/StrongLink) needs to be put in place by a *system
        admin ([Multi data set
        archiving](https://unlimited.ethz.ch/display/openBISDoc2010/Multi+data+set+archiving))*.
    2.  Hide sections by default. By default some sections in some forms
        are hidden:
        1.  Description in *Spaces* and *Projects*.
        2.  Identification info in *Spaces*, *Projects*, *Experiments*,
            *Objects*, *Datasets*.

By unchecking this option, these sections will be shown by default.

 

Updated on April 26, 2023
