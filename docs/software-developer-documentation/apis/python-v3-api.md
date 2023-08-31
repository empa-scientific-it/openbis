# Python (V3 API) - pyBIS!

pyBIS is a Python module for interacting with openBIS. pyBIS is designed to be most useful in a [Jupyter Notebook](https://jupyter.org) or IPython environment, especially if you are developing Python scripts for automatisation. Jupyter Notebooks offer some sort of IDE for openBIS, supporting TAB completition and immediate data checks, making the life of a researcher hopefully easier.

## Dependencies and Requirements

- pyBIS relies the openBIS API v3
- openBIS version 16.05.2 or newer is required
- 19.06.5 or later is recommended
- pyBIS uses Python 3.6 or newer and the Pandas module

## Installation

```python
pip install --upgrade pybis
```

That command will download install pyBIS and all its dependencies. If pyBIS is already installed, it will be upgraded to the latest version.

If you haven't done yet, install Jupyter and/or Jupyter Lab (the next Generation of Jupyter):

```python
pip install jupyter
pip install jupyterlab
```

## General Usage

### TAB completition and other hints in Jupyter / IPython

- in a Jupyter Notebook or IPython environment, pybis helps you to enter the commands
- After every dot `.` you might hit the `TAB` key in order to look at the available commands.
- if you are unsure what parameters to add to a , add a question mark right after the method and hit `SHIFT+ENTER`
- Jupyter will then look up the signature of the method and show some helpful docstring

### Checking input

- When working with properties of entities, they might use a **controlled vocabulary** or are of a specific **property type**.
- Add an underscore `_` character right after the property and hit `SHIFT+ENTER` to show the valid values
- When a property only acceps a controlled vocabulary, you will be shown the valid terms in a nicely formatted table
- if you try to assign an **invalid value** to a property, you'll receive an error immediately

### Glossary

- **spaces:** used for authorisation eg. to separate two working groups. If you have permissions in a space, you can see everything which in that space, but not necessarily in another space (unless you have the permission).
- **projects:** a space consists of many projects.
- **experiments / collections:** a projects contain many experiments. Experiments can have _properties_
- **samples / objects:** an experiment contains many samples. Samples can have _properties_
- **dataSet:** a dataSet which contains the actual _data files_, either pyhiscal (stored in openBIS dataStore) or linked
- **attributes:** every entity above contains a number of attributes. They are the same accross all instances of openBIS and independent of their type.
- **properties:** Additional specific key-value pairs, available for these entities:

  - experiments
  - samples
  - dataSets

  every single instance of an entity must be of a specific **entity type** (see below). The type defines the set of properties.

- **experiment type / collection type:** a type for experiments which specifies its properties
- **sample type / object type:** a type for samples / objects which specifies its properties
- **dataSet type:** a type for dataSets which specifies its properties
- **property type:** a single property, as defined in the entity types above. It can be of a classic data type (e.g. INTEGER, VARCHAR, BOOLEAN) or its values can be controlled (CONTROLLEDVOCABULARY).
- **plugin:** a script written in [Jython](https://www.jython.org) which allows to check property values in a even more detailed fashion

## connect to OpenBIS

### login

In an **interactive session** e.g. inside a Jupyter notebook, you can use `getpass` to enter your password safely:

```python
from pybis import Openbis
o = Openbis('https://example.com')
o = Openbis('example.com')          # https:// is assumed

import getpass
password = getpass.getpass()

o.login('username', password, save_token=True)   # save the session token in ~/.pybis/example.com.token
```

In a **script** you would rather use two **environment variables** to provide username and password:

```python
from pybis import Openbis
o = Openbis(os.environ['OPENBIS_HOST'])

o.login(os.environ['OPENBIS_USERNAME'], os.environ['OPENBIS_PASSWORD'])
```

As an even better alternative, you should use personal access tokens (PAT) to avoid username/password altogether. See below.

### Verify certificate

By default, your SSL-Certification is being verified. If you have a test-instance with a self-signed certificate, you'll need to turn off this verification explicitly:

```python
from pybis import Openbis
o = Openbis('https://test-openbis-instance.com', verify_certificates=False)
```

### Check session token, logout()

Check whether your session, i.e. the **session token** is still valid and log out:

```python
print(f"Session is active: {o.is_session_active()} and token is {o.token}")
o.logout()
print(f"Session is active: {o.is_session_active()"}
```

### Personal access token (PAT)

As an (new) alternative to login every time you run a script, you can create tokens which

- once issued, do **not need username or password**
- are **much longer valid** than session tokens (default is one year)
- **survive restarts** of an openBIS instance

To create a token, you first need a valid session – either through classic login or by assigning an existing valid session token:

```python
from pybis import Openbis
o = Openbis('https://test-openbis-instance.com')

o.login("username", "password")
# or
o.set_token("your_username-220808165456793xA3D0357C5DE66A5BAD647E502355FE2C")
```

Then you can create a new personal access token (PAT) and use it for all further pyBIS queries:

```python
pat = o.get_or_create_personal_access_token(sessionName="Project A")
o.set_token(pat, save_token=True)
```

You may also use permId directly:

```python
pat = o.get_or_create_personal_access_token(sessionName="Project A")
o.set_token(pat.permId, save_token=True) 
```

```{note}
If there is an existing PAT with the same _sessionName_ which is still valid and the validity is within the warning period (defined by the server), then this existing PAT is returned instead. However, you can enforce creating a new PAT by passing the argument `force=True`.
```

```{note}
Most operations are permitted using the PAT, _except_:
```

- all operations on personal access tokens itself
- i.e. create, list, delete operations on tokens

For these operations, you need to use a session token instead.

To get a list of all currently available tokens:

```python
o.get_personal_access_tokens()
o.get_personal_access_tokens(sessionName="APPLICATION_1")
```

To delete the first token shown in the list:

```python
o.get_personal_access_tokens()[0].delete('some reason')
```

### Caching

With `pyBIS 1.17.0`, a lot of caching has been introduced to improve the speed of object lookups that do not change often. If you encounter any problems, you can turn it off like this:

```python
o = Openbis('https://example.com', use_cache=False)

# or later in the script
o.use_cache = False
o.clear_cache()
o.clear_cache('sampleType')
```

## Mount openBIS dataStore server

### Prerequisites: FUSE / SSHFS

Mounting an openBIS dataStore server requires FUSE / SSHFS to be installed (requires root privileges). The mounting itself requires no root privileges.

**Mac OS X**

Follow the installation instructions on
https://osxfuse.github.io

**Unix Cent OS 7**

```bash
$ sudo yum install epel-release
$ sudo yum --enablerepo=epel -y install fuse-sshfs
$ user="$(whoami)"
$ usermod -a -G fuse "$user"
```

After the installation, an `sshfs` command should be available.

### Mount dataStore server with pyBIS

Because the mount/unmount procedure differs from platform to platform, pyBIS offers two simple methods:

```python
o.mount()
o.mount(username, password, hostname, mountpoint, volname)
o.is_mounted()
o.unmount()
o.get_mountpoint()
```

Currently, mounting is supported for Linux and Mac OS X only.

All attributes, if not provided, are re-used by a previous login() command. If no mountpoint is provided, the default mounpoint will be `~/hostname`. If this directory does not exist, it will be created. The directory must be empty before mounting.

## Masterdata

OpenBIS stores quite a lot of meta-data along with your dataSets. The collection of data that describes this meta-data (i.e. meta-meta-data) is called masterdata. It consists of:

- sample types
- dataSet types
- material types
- experiment types
- property types
- vocabularies
- vocabulary terms
- plugins (jython scripts that allow complex data checks)
- tags
- semantic annotations

### browse masterdata

```python
sample_types = o.get_sample_types()  # get a list of sample types
sample_types.df                      # DataFrame object
st = o.get_sample_types()[3]         # get 4th element of that list
st = o.get_sample_type('YEAST')
st.code
st.generatedCodePrefix
st.attrs.all()                       # get all attributes as a dict
st.get_validationPlugin()            # returns a plugin object

st.get_property_assignments()        # show the list of properties
                                     # for that sample type
o.get_material_types()
o.get_dataset_types()
o.get_experiment_types()
o.get_collection_types()

o.get_property_types()
pt = o.get_property_type('BARCODE_COMPLEXITY_CHECKER')
pt.attrs.all()

o.get_plugins()
pl = o.get_plugin('Diff_time')
pl.script  # the Jython script that processes this property

o.get_vocabularies()
o.get_vocabulary('BACTERIAL_ANTIBIOTIC_RESISTANCE')
o.get_terms(vocabulary='STORAGE')
o.get_tags()
```

### create property types

**Samples** (objects), **experiments** (collections) and **dataSets** contain type-specific **properties**. When you create a new sample, experiment or datasSet of a given type, the set of properties is well defined. Also, the values of these properties are being type-checked.

The first step in creating a new entity type is to create a so called **property type**:

```python
pt_text = o.new_property_type(
    code        = 'MY_NEW_PROPERTY_TYPE',
    label       = 'yet another property type',
    description = 'my first property',
    dataType    = 'VARCHAR',
)
pt_text.save()

pt_int = o.new_property_type(
    code        = 'MY_NUMBER',
    label       = 'property contains a number',
    dataType    = 'INTEGER',
)
pt_int.save()

pt_voc = o.new_property_type(
    code        = 'MY_CONTROLLED_VOCABULARY',
    label       = 'label me',
    description = 'give me a description',
    dataType    = 'CONTROLLEDVOCABULARY',
    vocabulary  = 'STORAGE',
)
pt_voc.save()

pt_richtext = o.new_property_type(
    code        = 'MY_RICHTEXT_PROPERTY',
    label       = 'richtext data',
    description = 'property contains rich text',
    dataType    = 'MULTILINE_VARCHAR',
    metaData    = {'custom_widget' : 'Word Processor'}
)
pt_richtext.save()

pt_spread = o.new_property_type(
    code        = 'MY_TABULAR_DATA',
    label       = 'data in a table',
    description = 'property contains a spreadsheet',
    dataType    = 'XML',
    metaData    = {'custom_widget': 'Spreadsheet'}
)
pt_spread.save()
```

The `dataType` attribute can contain any of these values:

- `INTEGER`
- `VARCHAR`
- `MULTILINE_VARCHAR`
- `REAL`
- `TIMESTAMP`
- `BOOLEAN`
- `HYPERLINK`
- `XML`
- `CONTROLLEDVOCABULARY`
- `MATERIAL`

When choosing `CONTROLLEDVOCABULARY`, you must specify a `vocabulary` attribute (see example). Likewise, when choosing `MATERIAL`, a `materialType` attribute must be provided.

To create a **richtext property**, use `MULTILINE_VARCHAR` as `dataType` and set `metaData` to `{'custom_widget' : 'Word Processor'}` as shown in the example above.

To create a **tabular, spreadsheet-like property**, use `XML` as `dataType` and set `metaData` to `{'custom_widget' : 'Spreadhseet'}`as shown in the example above.

**Note**: PropertyTypes that start with a \$ are by definition `managedInternally` and therefore this attribute must be set to True.

### create sample types / object types

The second step (after creating a property type, see above) is to create the **sample type**. The new name for **sample** is **object**. You can use both methods interchangeably:

- `new_sample_type()` == `new_object_type()`

```python
sample_type = o.new_sample_type(
    code                = 'my_own_sample_type',  # mandatory
    generatedCodePrefix = 'S',                   # mandatory
    description         = '',
    autoGeneratedCode   = True,
    subcodeUnique       = False,
    listable            = True,
    showContainer       = False,
    showParents         = True,
    showParentMetadata  = False,
    validationPlugin    = 'Has_Parents'          # see plugins below
)
sample_type.save()
```

When `autoGeneratedCode` attribute is set to `True`, then you don't need to provide a value for `code` when you create a new sample. You can get the next autoGeneratedCode like this:

```python
sample_type.get_next_sequence()    # eg. 67
sample_type.get_next_code()        # e.g. FLY77
```

From pyBIS 1.31.0 onwards, you can provide a `code` even for samples where its sample type has `autoGeneratedCode=True` to offer the same functionality as ELN-LIMS. In earlier versions of pyBIS, providing a code in this situation caused an error.

### assign and revoke properties to sample type / object type

The third step, after saving the sample type, is to **assign or revoke properties** to the newly created sample type. This assignment procedure applies to all entity types (dataset type, experiment type).

```python
sample_type.assign_property(
	prop                 = 'diff_time',           # mandatory
	section              = '',
	ordinal              = 5,
	mandatory            = True,
	initialValueForExistingEntities = 'initial value'
	showInEditView       = True,
	showRawValueInForms  = True
)
sample_type.revoke_property('diff_time')
sample_type.get_property_assignments()
```

### create a dataset type

The second step (after creating a **property type**, see above) is to create the **dataset type**. The third step is to **assign or revoke the properties** to the newly created dataset type.

```python
dataset_type = o.new_dataset_type(
    code                = 'my_dataset_type',       # mandatory
    description         = None,
    mainDataSetPattern  = None,
    mainDataSetPath     = None,
    disallowDeletion    = False,
    validationPlugin    = None,
)
dataset_type.save()
dataset_type.assign_property('property_name')
dataset_type.revoke_property('property_name')
dataset_type.get_property_assignments()
```

### create an experiment type / collection type

The second step (after creating a **property type**, see above) is to create the **experiment type**.

The new name for **experiment** is **collection**. You can use both methods interchangeably:

- `new_experiment_type()` == `new_collection_type()`

```python
experiment_type = o.new_experiment_type(
    code,
    description      = None,
    validationPlugin = None,
)
experiment_type.save()
experiment_type.assign_property('property_name')
experiment_type.revoke_property('property_name')
experiment_type.get_property_assignments()
```

### create material types

Materials and material types are deprecated in newer versions of openBIS.

```python
material_type = o.new_material_type(
    code,
    description=None,
    validationPlugin=None,
)
material_type.save()
material_type.assign_property('property_name')
material_type.revoke_property('property_name')
material_type.get_property_assignments()

```

### create plugins

Plugins are Jython scripts that can accomplish more complex data-checks than ordinary types and vocabularies can achieve. They are assigned to entity types (dataset type, sample type etc). [Documentation and examples can be found here](https://openbis.readthedocs.io/en/latest/user-documentation/general-admin-users/properties-handled-by-scripts.html)

```python
pl = o.new_plugin(
    name       ='my_new_entry_validation_plugin',
    pluginType ='ENTITY_VALIDATION',       # or 'DYNAMIC_PROPERTY' or 'MANAGED_PROPERTY',
    entityKind = None,                     # or 'SAMPLE', 'MATERIAL', 'EXPERIMENT', 'DATA_SET'
    script     = 'def calculate(): pass'   # a JYTHON script
)
pl.save()
```

### Users, Groups and RoleAssignments

Users can only login into the openBIS system when:

- they are present in the authentication system (e.g. LDAP)
- the username/password is correct
- the user's mail address needs is present
- the user is already added to the openBIS user list (see below)
- the user is assigned a role which allows a login, either directly assigned or indirectly assigned via a group membership

```python
o.get_groups()
group = o.new_group(code='group_name', description='...')
group = o.get_group('group_name')
group.save()
group.assign_role(role='ADMIN', space='DEFAULT')
group.get_roles()
group.revoke_role(role='ADMIN', space='DEFAULT')

group.add_members(['admin'])
group.get_members()
group.del_members(['admin'])
group.delete()

o.get_persons()
person = o.new_person(userId='username')
person.space = 'USER_SPACE'
person.save()
# person.delete() is currently not possible.

person.assign_role(role='ADMIN', space='MY_SPACE')
person.assign_role(role='OBSERVER')
person.get_roles()
person.revoke_role(role='ADMIN', space='MY_SPACE')
person.revoke_role(role='OBSERVER')

o.get_role_assignments()
o.get_role_assignments(space='MY_SPACE')
o.get_role_assignments(group='MY_GROUP')
ra = o.get_role_assignment(techId)
ra.delete()
```

### Spaces

Spaces are fundamental way in openBIS to divide access between groups. Within a space, data can be easily shared. Between spaces, people need to be given specific access rights (see section above). The structure in openBIS is as follows:

- space
  - project
    - experiment / collection
      - sample / object
        - dataset

```python
space = o.new_space(code='space_name', description='')
space.save()
o.get_spaces(
    start_with = 0,                   # start_with and count
    count      = 10,                  # enable paging
)
space = o.get_space('MY_SPACE')

# get individual attributes
space.code
space.description
space.registrator
space.registrationDate
space.modifier
space.modificationDate

# set individual attribute
# most of the attributes above are set automatically and cannot be modified.
space.description = '...'

# get all attributes as a dictionary
space.attrs.all()

space.delete('reason for deletion')
```

### Projects

Projects live within spaces and usually contain experiments (aka collections):

- space
  - project
    - experiment / collection
      - sample / object
        - dataset

```python
project = o.new_project(
    space       = space,
    code        = 'project_name',
    description = 'some project description'
)
project = space.new_project(
	code         = 'project_code',
	description  = 'project description'
)
project.save()

o.get_projects(
    space       = 'MY_SPACE',         # show only projects in MY_SPACE
    start_with  = 0,                  # start_with and count
    count       = 10,                 # enable paging
)
o.get_projects(space='MY_SPACE')
space.get_projects()

project.get_experiments() # see details and limitations in Section 'search for experiments'

project.get_attachments()             # deprecated, as attachments are not compatible with ELN-LIMS.
                                      # Attachments are an old concept and should not be used anymore.
p.add_attachment(                     # deprecated, see above
    fileName='testfile',
     description= 'another file',
     title= 'one more attachment'
)
project.download_attachments(<path or cwd>)  # deprecated, see above

# get individual attributes
project.code
project.description

# set individual attribute
project.description = '...'

# get all attributes as a dictionary
project.attrs.all()

project.freeze = True
project.freezeForExperiments = True
project.freezeForSamples = True
```

### Experiments / Collections

Experiments live within projects:

- space
  - project
    - experiment / collection
      - sample / object
        - dataset

The new name for **experiment** is **collection**. You can use boths names interchangeably:

- `get_experiment()` = `get_collection()`
- `new_experiment()` = `new_collection()`
- `get_experiments()` = `get_collections()`

#### create a new experiment

```python
exp = o.new_experiment
    code='MY_NEW_EXPERIMENT',
    type='DEFAULT_EXPERIMENT',
    space='MY_SPACE',
    project='YEASTS'
)
exp.save()
```

#### search for experiments

```python
experiments = o.get_experiments(
    project       = 'YEASTS',
    space         = 'MY_SPACE',
    type          = 'DEFAULT_EXPERIMENT',
    tags          = '*',
    finished_flag = False,
    props         = ['name', 'finished_flag']
)
experiments = project.get_experiments()
experiment = experiments[0]        # get first experiment of result list
experiment = experiment
for experiment in experiments:     # iterate over search results
    print(experiment.props.all())
dataframe = experiments.df         # get Pandas DataFrame of result list

exp = o.get_experiment('/MY_SPACE/MY_PROJECT/MY_EXPERIMENT')
```

***Note: Attributes download***

The `get_experiments()` method, by default, returns fewer details to make the download process faster.
However, if you want to include specific attributes in the results, you can do so by using the `attrs` parameter.

The `get_experiments()` method results include only `identifier`, `permId`, `type`, `registrator`, `registrationDate`, `modifier`, `modificationDate`

```get attributes
experiments = o.get_experiments(
    project       = 'YEASTS',
    space         = 'MY_SPACE',
    type          = 'DEFAULT_EXPERIMENT',
    attrs          = ["parents", "children"]
)

    identifier             permId                type               registrator    registrationDate     modifier    modificationDate     parents                    children
--  ---------------------  --------------------  -----------------  -------------  -------------------  ----------  -------------------  -------------------------  ----------
 0  /MY_SPACE/YEASTS/EXP1  20230407070122991-46  DEFAULT_EXPERIMENT  admin          2023-04-07 09:01:23  admin       2023-04-07 09:02:22  ['/MY_SPACE/YEASTS/EXP2']  []

```

#### Experiment attributes

```python
exp.attrs.all()                    # returns all attributes as a dict

exp.attrs.tags = ['some', 'tags']
exp.tags = ['some', 'tags']        # same thing
exp.save()

exp.code
exp.description
exp.registrator
...

exp.project = 'my_project'
exp.space   = 'my_space'
exp.freeze = True
exp.freezeForDataSets = True
exp.freezeForSamples = True

exp.save()                         # needed to save/update the changed attributes and properties
```

#### Experiment properties

**Getting properties**

```python
experiment.props == ds.p                  # you can use either .props or .p to access the properties
experiment.p                              # in Jupyter: show all properties in a nice table
experiment.p()                            # get all properties as a dict
experiment.props.all()                    # get all properties as a dict
experiment.p('prop1','prop2')             # get some properties as a dict
experiment.p.get('$name')                 # get the value of a property
experiment.p['property']                  # get the value of a property
```

**Setting properties**

```python
experiment.experiment = 'first_exp'       # assign sample to an experiment
experiment.project = 'my_project'         # assign sample to a project

experiment.p. + TAB                       # in Jupyter/IPython: show list of available properties
experiment.p.my_property_ + TAB           # in Jupyter/IPython: show datatype or controlled vocabulary
experiment.p['my_property']= "value"      # set the value of a property
experiment.p.set('my_property, 'value')   # set the value of a property
experiment.p.my_property = "some value"   # set the value of a property
experiment.p.set({'my_property':'value'}) # set the values of some properties
experiment.set_props({ key: value })      # set the values of some properties

experiment.save()                         # needed to save/update the changed attributes and properties
```

### Samples / Objects

Samples usually live within experiments/collections:

- space
  - project
    - experiment / collection
      - sample / object
        - dataset

The new name for **sample** is **object**. You can use boths names interchangeably:

- `get_sample()` = `get_object()`
- `new_sample()` = `new_object()`
- `get_samples()` = `get_objects()`

etc.

```python
sample = o.new_sample(
    type       = 'YEAST',
    space      = 'MY_SPACE',
    experiment = '/MY_SPACE/MY_PROJECT/EXPERIMENT_1',
    parents    = [parent_sample, '/MY_SPACE/YEA66'],   # you can use either permId, identifier
    children   = [child_sample],                       # or sample object
    props      = {"name": "some name", "description": "something interesting"}
)
sample = space.new_sample( type='YEAST' )
sample.save()

sample = o.get_sample('/MY_SPACE/MY_SAMPLE_CODE')
sample = o.get_sample('20170518112808649-52')
samples= o.get_samples(type='UNKNOWN')    # see details and limitations in Section 'search for samples / objects'

# get individual attributes
sample.space
sample.code
sample.permId
sample.identifier
sample.type  # once the sample type is defined, you cannot modify it

# set attribute
sample.space = 'MY_OTHER_SPACE'

sample.experiment    # a sample can belong to one experiment only
sample.experiment = '/MY_SPACE/MY_PROJECT/MY_EXPERIMENT'

sample.project
sample.project = '/MY_SPACE/MY_PROJECT'  # only works if project samples are
enabled

sample.tags
sample.tags = ['guten_tag', 'zahl_tag' ]

sample.attrs.all()                    # returns all attributes as a dict
sample.props.all()                    # returns all properties as a dict

sample.get_attachments()              # deprecated, as attachments are not compatible with ELN-LIMS.
                                      # Attachments are an old concept and should not be used anymore.
sample.download_attachments(<path or cwd>)  # deprecated, see above
sample.add_attachment('testfile.xls') # deprecated, see above

sample.delete('deleted for some reason')
```

### create/update/delete many samples in a transaction

Creating a single sample takes some time. If you need to create many samples, you might want to create them in one transaction. This will transfer all your sample data at once. The Upside of this is the **gain in speed**. The downside: this is a **all-or-nothing** operation, which means, either all samples will be registered or none (if any error occurs).

**create many samples in one transaction**

```python
trans = o.new_transaction()
for i in range (0, 100):
    sample = o.new_sample(...)
    trans.add(sample)

trans.commit()
```

**update many samples in one transaction**

```python
trans = o.new_transaction()
for sample in o.get_samples(count=100):
    sample.prop.some_property = 'different value'
    trans.add(sample)

trans.commit()
```

**delete many samples in one transaction**

```python
trans = o.new_transaction()
for sample in o.get_samples(count=100):
    sample.mark_to_be_deleted()
    trans.add(sample)

trans.reason('go what has to go')
trans.commit()
```

**Note:** You can use the `mark_to_be_deleted()`, `unmark_to_be_deleted()` and `is_marked_to_be_deleted()` methods to set and read the internal flag.

#### parents, children, components and container

```python
sample.get_parents()
sample.set_parents(['/MY_SPACE/PARENT_SAMPLE_NAME')
sample.add_parents('/MY_SPACE/PARENT_SAMPLE_NAME')
sample.del_parents('/MY_SPACE/PARENT_SAMPLE_NAME')

sample.get_children()
sample.set_children('/MY_SPACE/CHILD_SAMPLE_NAME')
sample.add_children('/MY_SPACE/CHILD_SAMPLE_NAME')
sample.del_children('/MY_SPACE/CHILD_SAMPLE_NAME')

# A Sample may belong to another Sample, which acts as a container.
# As opposed to DataSets, a Sample may only belong to one container.
sample.container    # returns a sample object
sample.container = '/MY_SPACE/CONTAINER_SAMPLE_NAME'   # watch out, this will change the identifier of the sample to:
                                                       # /MY_SPACE/CONTAINER_SAMPLE_NAME:SAMPLE_NAME
sample.container = ''                                  # this will remove the container.

# A Sample may contain other Samples, in order to act like a container (see above)
# caveat: containers are NOT compatible with ELN-LIMS
# The Sample-objects inside that Sample are called «components» or «contained Samples»
# You may also use the xxx_contained() functions, which are just aliases.
sample.get_components()
sample.set_components('/MY_SPACE/COMPONENT_NAME')
sample.add_components('/MY_SPACE/COMPONENT_NAME')
sample.del_components('/MY_SPACE/COMPONENT_NAME')
```

#### sample tags

```python
sample.get_tags()
sample.set_tags('tag1')
sample.add_tags(['tag2','tag3'])
sample.del_tags('tag1')
```

#### Sample attributes and properties

**Getting properties**

```python
sample.attrs.all()                    # returns all attributes as a dict
sample.attribute_name                 # return the attribute value

sample.props == ds.p                  # you can use either .props or .p to access the properties
sample.p                              # in Jupyter: show all properties in a nice table
sample.p()                            # get all properties as a dict
sample.props.all()                    # get all properties as a dict
sample.p('prop1','prop2')             # get some properties as a dict
sample.p.get('$name')                 # get the value of a property
sample.p['property']                  # get the value of a property
```

**Setting properties**

```python
sample.experiment = 'first_exp'       # assign sample to an experiment
sample.project = 'my_project'         # assign sample to a project

sample.p. + TAB                       # in Jupyter/IPython: show list of available properties
sample.p.my_property_ + TAB           # in Jupyter/IPython: show datatype or controlled vocabulary
sample.p['my_property']= "value"      # set the value of a property
sample.p.set('my_property, 'value')   # set the value of a property
sample.p.my_property = "some value"   # set the value of a property
sample.p.set({'my_property':'value'}) # set the values of some properties
sample.set_props({ key: value })      # set the values of some properties

sample.save()                         # needed to save/update the attributes and properties
```

#### search for samples / objects

The result of a search is always list, even when no items are found. The `.df` attribute returns
the Pandas dataFrame of the results.

```python
samples = o.get_samples(
    space      ='MY_SPACE',
    type       ='YEAST',
    tags       =['*'],                # only sample with existing tags
    start_with = 0,                   # start_with and count
    count      = 10,                  # enable paging
    where = {
        "$SOME.WEIRD-PROP": "hello"   # only receive samples where properties match
    }

    registrationDate = "2020-01-01",  # date format: YYYY-MM-DD
    modificationDate = "<2020-12-31", # use > or < to search for specified date and later / earlier
    attrs=[                           # show these attributes in the dataFrame
        'sample.code',
        'registrator.email',
        'type.generatedCodePrefix'
    ],
    parent_property = 'value',        # search in a parent's property
    child_property  = 'value',        # search in a child's property
    container_property = 'value'      # search in a container's property
    parent = '/MY_SPACE/PARENT_SAMPLE', # sample has this as its parent
    parent = '*',                     # sample has at least one parent
    child  = '/MY_SPACE/CHILD_SAMPLE',
    child  = '*',                     # sample has at least one child
    container = 'MY_SPACE/CONTAINER',
    container = '*'                   # sample lives in a container
    props=['$NAME', 'MATING_TYPE']    # show these properties in the result
)

sample = samples[9]                   # get the 10th sample
                                      # of the search results
sample = samples['/SPACE/AABC']       # same, fetched by identifier
for sample in samples:                # iterate over the
   print(sample.code)                 # search results


samples.df                            # returns a Pandas DataFrame object

samples = o.get_samples(props="*")    # retrieve all properties of all samples
```

***Note: Attributes download***

The `get_samples()` method, by default, returns fewer details to make the download process faster.
However, if you want to include specific attributes in the results, you can do so by using the `attrs` parameter.

The `get_samples()` method results include only `identifier`, `permId`, `type`, `registrator`, `registrationDate`, `modifier`, `modificationDate`

```get attributes
experiments = o.get_samples(
    space         = 'MY_SPACE',
    type          = 'YEAST',
    attrs          = ["parents", "children"]
)

    identifier                permId                type               registrator    registrationDate     modifier    modificationDate     parents                    children
--  ---------------------     --------------------  -----------------  -------------  -------------------  ----------  -------------------  -------------------------  ----------
 0  /MY_SPACE/YEASTS/SAMPLE1  20230407070121337-47  YEAST              admin          2023-04-07 09:06:23  admin       2023-04-07 09:06:22  ['/MY_SPACE/YEASTS/EXP2']  []

```

#### freezing samples

```python
sample.freeze = True
sample.freezeForComponents = True
sample.freezeForChildren = True
sample.freezeForParents = True
sample.freezeForDataSets = True
```

### Datasets

Datasets are by all means the most important openBIS entity. The actual files are stored as datasets; all other openBIS entities mainly are necessary to annotate and to structure the data:

- space
  - project
    - experiment / collection
      - sample / object
        - dataset

#### working with existing dataSets

**search for datasets**

This example does the following

- search for all datasets of type `SCANS`, retrieve the first 10 entries
- print out all properties
- print the list of all files in this dataset
- download the dataset

```python
datasets = sample.get_datasets(type='SCANS', start_with=0, count=10)
for dataset in datasets:
    print(dataset.props())
    print(dataset.file_list)
    dataset.download()
dataset = datasets[0]
```

***Note: Attributes download***

The `get_datasets()` method, by default, returns fewer details to make the download process faster.
However, if you want to include specific attributes in the results, you can do so by using the `attrs` parameter.

The `get_datasets()` method results include only `permId`, `type`, `experiment`, `sample`, `registrationDate`, `modificationDate`,
`location`, `status`, `presentInArchive`, `size`

```get attributes
experiments = o.get_datasets(
    space         = 'MY_SPACE',
    attrs          = ["parents", "children"]
)

    permId                type      experiment                sample                   registrationDate     modificationDate     location                                 status     presentInArchive      size  parents                   children
--  --------------------  --------  ------------------------  ---------------------    -------------------  -------------------  ---------------------------------------  ---------  ------------------  ------  ------------------------  ------------------------
 0  20230526101657295-48  RAW_DATA  /MY_SPACE/DEFAULT/DEFAULT  /MY_SPACE/DEFAULT/EXP1  2023-05-26 12:16:58  2023-05-26 12:17:37  1F60C7DC-63D8-4C07/20230526101657295-48  AVAILABLE  False                  469  []                        ['20230526101737019-49']
 1  20230526101737019-49  RAW_DATA  /MY_SPACE/DEFAULT/DEFAULT  /MY_SPACE/DEFAULT/EXP1  2023-05-26 12:17:37  2023-05-26 12:17:37  1F60C7DC-63D8-4C07/20230526101737019-49  AVAILABLE  False                  127  ['20230526101657295-48']  []
```

**More dataset functions:**

```python
ds = o.get_dataset('20160719143426517-259')
ds.get_parents()
ds.get_children()
ds.sample
ds.experiment
ds.physicalData
ds.status                         # AVAILABLE   LOCKED   ARCHIVED
                                  # ARCHIVE_PENDING   UNARCHIVE_PENDING
                                  # BACKUP_PENDING
ds.archive()                      # archives a dataset, i.e. moves it to a slower but cheaper diskspace (tape).
                                  # archived datasets cannot be downloaded, they need to be unarchived first.
                                  # This is an asynchronous process,
                                  # check ds.status regularly until the dataset becomes 'ARCHIVED'
ds.unarchive()                    # this starts an asynchronous process which gets the dataset from the tape.
                                  # Check ds.status regularly until it becomes 'AVAILABLE'

ds.attrs.all()                    # returns all attributes as a dict
ds.props.all()                    # returns all properties as a dict

ds.add_attachment()               # Deprecated. Attachments usually contain meta-data
ds.get_attachments()              # about the dataSet, not the data itself.
ds.download_attachments(<path or cwd>)  # Deprecated, as attachments are not compatible with ELN-LIMS.
                                  # Attachments are an old concept and should not be used anymore.
```

#### download dataSets

```python
o.download_prefix                  # used for download() and symlink() method.
                                   # Is set to data/hostname by default, but can be changed.
ds.get_files(start_folder="/")     # get file list as Pandas dataFrame
ds.file_list                       # get file list as array
ds.file_links                      # file list as a dict containing direct https links

ds.download()                      # simply download all files to data/hostname/permId/
ds.download(
	destination = 'my_data',        # download files to folder my_data/
	create_default_folders = False, # ignore the /original/DEFAULT folders made by openBIS
	wait_until_finished = False,    # download in background, continue immediately
	workers = 10                    # 10 downloads parallel (default)
)
ds.download_path                   # returns the relative path (destination) of the files after a ds.download()
ds.is_physical()                   # TRUE if dataset is physically
```

#### link dataSets

Instead of downloading a dataSet, you can create a symbolic link to a dataSet in the openBIS dataStore. To do that, the openBIS dataStore needs to be mounted first (see mount method above). **Note:** Symbolic links and the mount() feature currently do not work with Windows.

```python
o.download_prefix                  # used for download() and symlink() method.
                                   # Is set to data/hostname by default, but can be changed.
ds.symlink()                       # creates a symlink for this dataset: data/hostname/permId
                                   # tries to mount openBIS instance
                                   # in case it is not mounted yet
ds.symlink(
   target_dir = 'data/dataset_1/', # default target_dir is: data/hostname/permId
   replace_if_symlink_exists=True
)
ds.is_symlink()
```

#### dataSet attributes and properties

**Getting properties**

```python
ds.attrs.all()                    # returns all attributes as a dict
ds.attribute_name                 # return the attribute value

ds.props == ds.p                  # you can use either .props or .p to access the properties
ds.p                              # in Jupyter: show all properties in a nice table
ds.p()                            # get all properties as a dict
ds.props.all()                    # get all properties as a dict
ds.p('prop1','prop2')             # get some properties as a dict
ds.p.get('$name')                 # get the value of a property
ds.p['property']                  # get the value of a property
```

**Setting properties**

```python
ds.experiment = 'first_exp'       # assign dataset to an experiment
ds.sample = 'my_sample'           # assign dataset to a sample

ds.p. + TAB                       # in Jupyter/IPython: show list of available properties
ds.p.my_property_ + TAB           # in Jupyter/IPython: show datatype or controlled vocabulary
ds.p['my_property']= "value"      # set the value of a property
ds.p.set('my_property, 'value')   # set the value of a property
ds.p.my_property = "some value"   # set the value of a property
ds.p.set({'my_property':'value'}) # set the values of some properties
ds.set_props({ key: value })      # set the values of some properties
```

#### search for dataSets

- The result of a search is always list, even when no items are found
- The `.df` attribute returns the Pandas dataFrame of the results

```python
datasets = o.get_datasets(
    type  ='MY_DATASET_TYPE',
    **{ "SOME.WEIRD:PROP": "value"},  # property name contains a dot or a
                                      # colon: cannot be passed as an argument
    start_with = 0,                   # start_with and count
    count      = 10,                  # enable paging
    registrationDate = "2020-01-01",  # date format: YYYY-MM-DD
    modificationDate = "<2020-12-31", # use > or < to search for specified date and later / earlier
    parent_property = 'value',        # search in a parent's property
    child_property  = 'value',        # search in a child's property
    container_property = 'value'      # search in a container's property
    parent = '/MY_SPACE/PARENT_DS',   # has this dataset as its parent
    parent = '*',                     # has at least one parent dataset
    child  = '/MY_SPACE/CHILD_DS',
    child  = '*',                     # has at least one child dataset
    container = 'MY_SPACE/CONTAINER_DS',
    container = '*',                  # belongs to a container dataset
    attrs=[                           # show these attributes in the dataFrame
        'sample.code',
        'registrator.email',
        'type.generatedCodePrefix'
    ],
    props=['$NAME', 'MATING_TYPE']    # show these properties in the result
)
datasets = o.get_datasets(props="*")  # retrieve all properties of all dataSets
dataset = datasets[0]                 # get the first dataset in the search result
for dataset in datasets:              # iterate over the datasets
    ...
df = datasets.df                      # returns a Pandas dataFrame object of the search results
```

In some cases, you might want to retrieve precisely certain datasets. This can be achieved by
methods chaining (but be aware, it might not be very performant):

```python
datasets = o.get_experiments(project='YEASTS')\
			 .get_samples(type='FLY')\
			 .get_datasets(
					type='ANALYZED_DATA',
					props=['MY_PROPERTY'],
					MY_PROPERTY='some analyzed data'
		 	 )
```

- another example:

```python
datasets = o.get_experiment('/MY_NEW_SPACE/MY_PROJECT/MY_EXPERIMENT4')\
           .get_samples(type='UNKNOWN')\
           .get_parents()\
           .get_datasets(type='RAW_DATA')
```

#### freeze dataSets

- once a dataSet has been frozen, it cannot be changed by anyone anymore
- so be careful!

```python
ds.freeze = True
ds.freezeForChildren = True
ds.freezeForParents = True
ds.freezeForComponents = True
ds.freezeForContainers = True
ds.save()
```

#### create a new dataSet

```python
ds_new = o.new_dataset(
    type       = 'ANALYZED_DATA',
    experiment = '/SPACE/PROJECT/EXP1',
    sample     = '/SPACE/SAMP1',
    files      = ['my_analyzed_data.dat'],
    props      = {'name': 'some good name', 'description': '...' }
)
ds_new.save()
```

#### create dataSet with zipfile

DataSet containing one zipfile which will be unzipped in openBIS:

```python
ds_new = o.new_dataset(
    type       = 'RAW_DATA',
    sample     = '/SPACE/SAMP1',
    zipfile    = 'my_zipped_folder.zip',
)
ds_new.save()
```

#### create dataSet with mixed content

- mixed content means: folders and files are provided
- a relative specified folder (and all its content) will end up in the root, while keeping its structure
  - `../measurements/` --> `/measurements/`
  - `some/folder/somewhere/` --> `/somewhere/`
- relative files will also end up in the root
  - `my_file.txt` --> `/my_file.txt`
  - `../somwhere/else/my_other_file.txt` --> `/my_other_file.txt`
  - `some/folder/file.txt` --> `/file.txt`
- useful if DataSet contains files and folders
- the content of the folder will be zipped (on-the-fly) and uploaded to openBIS
- openBIS will keep the folder structure intact
- relative path will be shortened to its basename. For example:

| local                      | openBIS    |
| -------------------------- | ---------- |
| `../../myData/`            | `myData/`  |
| `some/experiment/results/` | `results/` |

```python
ds_new = o.new_dataset(
    type       = 'RAW_DATA',
    sample     = '/SPACE/SAMP1',
    files     = ['../measurements/', 'my_analyis.ipynb', 'results/']
)
ds_new.save()
```

#### create dataSet container

A DataSet of kind=CONTAINER contains other DataSets, but no files:

```python
ds_new = o.new_dataset(
    type       = 'ANALYZED_DATA',
    experiment = '/SPACE/PROJECT/EXP1',
    sample     = '/SPACE/SAMP1',
    kind       = 'CONTAINER',
    props      = {'name': 'some good name', 'description': '...' }
)
ds_new.save()
```

#### get, set, add and remove parent datasets

```python
dataset.get_parents()
dataset.set_parents(['20170115220259155-412'])
dataset.add_parents(['20170115220259155-412'])
dataset.del_parents(['20170115220259155-412'])
```

#### get, set, add and remove child datasets

```python
dataset.get_children()
dataset.set_children(['20170115220259155-412'])
dataset.add_children(['20170115220259155-412'])
dataset.del_children(['20170115220259155-412'])
```

#### dataSet containers

- A DataSet may belong to other DataSets, which must be of kind=CONTAINER
- As opposed to Samples, DataSets may belong (contained) to more than one DataSet-container
- caveat: containers are NOT compatible with ELN-LIMS

```python
dataset.get_containers()
dataset.set_containers(['20170115220259155-412'])
dataset.add_containers(['20170115220259155-412'])
dataset.del_containers(['20170115220259155-412'])
```

- a DataSet of kind=CONTAINER may contain other DataSets, to act like a folder (see above)
- the DataSet-objects inside that DataSet are called components or contained DataSets
- you may also use the xxx_contained() functions, which are just aliases.
- caveat: components are NOT compatible with ELN-LIMS

```python
dataset.get_components()
dataset.set_components(['20170115220259155-412'])
dataset.add_components(['20170115220259155-412'])
dataset.del_components(['20170115220259155-412'])
```

### Semantic Annotations

create semantic annotation for sample type 'UNKNOWN':

```python

sa = o.new_semantic_annotation(
	entityType = 'UNKNOWN',
	predicateOntologyId = 'po_id',
	predicateOntologyVersion = 'po_version',
	predicateAccessionId = 'pa_id',
	descriptorOntologyId = 'do_id',
	descriptorOntologyVersion = 'do_version',
	descriptorAccessionId = 'da_id'
)
sa.save()
```

Create semantic annotation for property type (predicate and descriptor values omitted for brevity)

```python
sa = o.new_semantic_annotation(propertyType = 'DESCRIPTION', ...)
sa.save()
```

**Create** semantic annotation for sample property assignment (predicate and descriptor values omitted for brevity)

```python
sa = o.new_semantic_annotation(
	entityType = 'UNKNOWN',
	propertyType = 'DESCRIPTION',
	...
)
sa.save()
```

**Create** a semantic annotation directly from a sample type. Will also create sample property assignment annotations when propertyType is given:

```python
st = o.get_sample_type("ORDER")
st.new_semantic_annotation(...)
```

**Get all** semantic annotations

```python
o.get_semantic_annotations()
```

**Get** semantic annotation by perm id

```python
sa = o.get_semantic_annotation("20171015135637955-30")
```

**Update** semantic annotation

```python
sa.predicateOntologyId = 'new_po_id'
sa.descriptorOntologyId = 'new_do_id'
sa.save()
```

**Delete** semantic annotation

```python
sa.delete('reason')
```

### Tags

```python
new_tag = o.new_tag(
	code        = 'my_tag',
	description = 'some descriptive text'
)
new_tag.description = 'some new description'
new_tag.save()
o.get_tags()
o.get_tag('/username/TAG_Name')
o.get_tag('TAG_Name')

tag.get_experiments()
tag.get_samples()
tag.get_owner()   # returns a person object
tag.delete('why?')
```

### Vocabulary and VocabularyTerms

An entity such as Sample (Object), Experiment (Collection), Material or DataSet can be of a specific _entity type_:

- Sample Type (Object Type)
- Experiment Type (Collection Type)
- DataSet Type
- Material Type

Every type defines which **Properties** may be defined. Properties act like **Attributes**, but they are type-specific. Properties can contain all sorts of information, such as free text, XML, Hyperlink, Boolean and also **Controlled Vocabulary**. Such a Controlled Vocabulary consists of many **VocabularyTerms**. These terms are used to only allow certain values entered in a Property field.

So for example, you want to add a property called **Animal** to a Sample and you want to control which terms are entered in this Property field. For this you need to do a couple of steps:

1. create a new vocabulary _AnimalVocabulary_
2. add terms to that vocabulary: _Cat, Dog, Mouse_
3. create a new PropertyType (e.g. _Animal_) of DataType _CONTROLLEDVOCABULARY_ and assign the _AnimalVocabulary_ to it
4. create a new SampleType (e.g. _Pet_) and _assign_ the created PropertyType to that Sample type.
5. If you now create a new Sample of type _Pet_ you will be able to add a property _Animal_ to it which only accepts the terms _Cat, Dog_ or _Mouse_.

**create new Vocabulary with three VocabularyTerms**

```python
voc = o.new_vocabulary(
    code = 'BBB',
    description = 'description of vocabulary aaa',
    urlTemplate = 'https://ethz.ch',
    terms = [
        { "code": 'term_code1', "label": "term_label1", "description": "term_description1"},
        { "code": 'term_code2', "label": "term_label2", "description": "term_description2"},
        { "code": 'term_code3', "label": "term_label3", "description": "term_description3"}
    ]
)
voc.save()

voc.vocabulary = 'description of vocabulary BBB'
voc.chosenFromList = True
voc.save() # update
```

**create additional VocabularyTerms**

```python
term = o.new_term(
	code='TERM_CODE_XXX',
	vocabularyCode='BBB',
	label='here comes a label',
	description='here might appear a meaningful description'
)
term.save()
```

**update VocabularyTerms**

To change the ordinal of a term, it has to be moved either to the top with the `.move_to_top()` method or after another term using the `.move_after_term('TERM_BEFORE')` method.

```python
voc = o.get_vocabulary('STORAGE')
term = voc.get_terms()['RT']
term.label = "Room Temperature"
term.official = True
term.move_to_top()
term.move_after_term('-40')
term.save()
term.delete()
```

### Change ELN Settings via pyBIS

#### Main Menu

The ELN settings are stored as a **JSON string** in the `$eln_settings` property of the `GENERAL_ELN_SETTINGS` sample. You can show the **Main Menu settings** like this:

```python
import json
settings_sample = o.get_sample("/ELN_SETTINGS/GENERAL_ELN_SETTINGS")
settings = json.loads(settings_sample.props["$eln_settings"])
print(settings["mainMenu"])
{'showLabNotebook': True,
 'showInventory': True,
 'showStock': True,
 'showObjectBrowser': True,
 'showExports': True,
 'showStorageManager': True,
 'showAdvancedSearch': True,
 'showUnarchivingHelper': True,
 'showTrashcan': False,
 'showVocabularyViewer': True,
 'showUserManager': True,
 'showUserProfile': True,
 'showZenodoExportBuilder': False,
 'showBarcodes': False,
 'showDatasets': True}
```

To modify the **Main Menu settings**, you have to change the settings dictionary, convert it back to json and save the sample:

```python
settings['mainMenu']['showTrashcan'] = False
settings_sample.props['$eln_settings'] = json.dumps(settings)
settings_sample.save()
```

#### Storages

The **ELN storages settings** can be found in the samples of project `/ELN_SETTINGS/STORAGES`

```python
o.get_samples(project='/ELN_SETTINGS/STORAGES')
```

To change the settings, just change the sample's properties and save the sample:

```python
sto = o.get_sample('/ELN_SETTINGS/STORAGES/BENCH')
sto.props()
{'$name': 'Bench',
 '$storage.row_num': '1',
 '$storage.column_num': '1',
 '$storage.box_num': '9999',
 '$storage.storage_space_warning': '80',
 '$storage.box_space_warning': '80',
 '$storage.storage_validation_level': 'BOX_POSITION',
 '$xmlcomments': None,
 '$annotations_state': None}
 sto.props['$storage.box_space_warning']= '80'
 sto.save()
```

#### Templates

The **ELN templates settings** can be found in the samples of project `/ELN_SETTINGS/TEMPLATES`

```python
o.get_samples(project='/ELN_SETTINGS/TEMPLATES')
```

To change the settings, use the same technique as shown above with the storages settings.

#### Custom Widgets

To change the **Custom Widgets settings**, get the `property_type` and set the `metaData` attribute:

```python
pt = o.get_property_type('YEAST.SOURCE')
pt.metaData = {'custom_widget': 'Spreadsheet'}
pt.save()
```

Currently, the value of the `custom_widget` key can be set to either

- `Spreadsheet` (for tabular, Excel-like data)
- `Word Processor` (for rich text data)
