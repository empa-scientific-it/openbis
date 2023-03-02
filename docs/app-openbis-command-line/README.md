# oBIS

oBIS is a command-line tool that makes it possible to handle data sets tracked by OpenBIS,
where users have complete freedom to structure and manipulate the data as they wish, while retaining
the benefits of openBIS.

With oBIS, it is possible not only to handle datasets stored in OpenBIS but also available to keep
only metadata send to openBIS, while the data itself is managed externally, by the user. In this
case, OpenBIS is aware of its existence and the data can be used for provenance tracking.

# Table of contents

1. [Prerequisites and installation](#1-prerequisites)
2. [Installation](#2-installation)
3. [Usage](#3-usage)
4. [Work modes](#4-work-modes)
    1. [Standard Data Store](#41-standard-data-store)
        1. [Commands](#411-commands)
        2. [Examples](#412-examples)
    2. [External Data Store](#42-external-data-store)
        1. [Settings](#421-settings)
        2. [Commands](#422-commands)
        3. [Examples](#423-examples)
5. [Big Data Link Services](#5-big-data-link-services)
6. [Rationale for obis](#6-rationale-for-obis)
7. [Literature](#7-literature)

## 1. Prerequisites

- python 3.6 or higher
- git 2.11 or higher
- git-annex 6 or higher [Installation guide](https://git-annex.branchable.com/install/)

## 2. Installation

```
pip3 install obis
```

Since `obis` is based on `pybis`, the pip command will also install pybis and all its dependencies.

## 3. Usage

### 3.1 Help is your friend!

$ obis --help

```
Usage: obis [OPTIONS] COMMAND [ARGS]...

Options:
  --version                Show the version and exit.
  -q, --quiet              Suppress status reporting.
  -s, --skip_verification  Do not verify cerficiates
  -d, --debug              Show stack trace on error.
  --help                   Show this message and exit.

Commands:
  addref         Add the given repository as a reference to openBIS.
  clone          Clone the repository found in the given data set id.
  collection     Get/set settings related to the collection.
  commit         Commit the repository to git and inform openBIS.
  config         Get/set configurations.
  data_set       Get/set settings related to the data set.
  download       Download files of a data set.
  init           Initialize the folder as a data repository.
  init_analysis  Initialize the folder as an analysis folder.
  move           Move the repository found in the given data set id.
  object         Get/set settings related to the object.
  removeref      Remove the reference to the given repository from openBIS.
  repository     Get/set settings related to the repository.
  settings       Get all settings.
  status         Show the state of the obis repository.
  sync           Sync the repository with openBIS.
  token          create/show a openBIS token
  upload         Upload files to form a data set.
```

To show detailed help for a specific command, type `obis <command> --help` :

```
$ obis commit --help
Usage: obis commit [OPTIONS] [REPOSITORY]

Options:
  -m, --msg TEXT               A message explaining what was done.
  -a, --auto_add               Automatically add all untracked files.
  -i, --ignore_missing_parent  If parent data set is missing, ignore it.
  --help                       Show this message and exit.
```

## 4. Work modes

oBIS command line tool can work in two modes depending on how data is stored:

1. Standard Data Store mode
2. External Data Store mode

**Warning:** Each repository can work in a single mode only! Mixing modes is not supported.

Depending on the mode, some commands may be unavailable or behave differently. Please read details
in the adequate section.

Here is a short summary of which commands are available in given modes:

| Command          | Standard Data Store | External Data Store |
|------------------|:-------------------:|:-------------------:|
| addref           |          ❌          |          ✅          |
| clone            |          ❌          |          ✅          |
| collection get   |          ✅          |          ✅          |
| collection set   |          ✅          |          ✅          |
| collection clear |          ❌          |          ✅          |
| commit           |          ❌          |          ✅          |
| config get       |          ✅          |          ✅          |
| config set       |          ✅          |          ✅          |
| config clear     |          ❌          |          ✅          |
| data_set get     |          ❌          |          ✅          |
| data_set set     |          ❌          |          ✅          |
| data_set clear   |          ❌          |          ✅          |
| data_set search  |          ✅          |          ❌          |
| download         |          ✅          |          ❌          |
| init             |          ❌          |          ✅          |
| init -p          |          ✅          |          ❌          |
| init_analysis    |          ❌          |          ✅          |
| move             |          ❌          |          ✅          |
| object get       |          ✅          |          ✅          |
| object set       |          ✅          |          ✅          |
| object clear     |          ❌          |          ✅          |
| object search    |          ✅          |          ❌          |
| removeref        |          ❌          |          ✅          |
| repository get   |          ❌          |          ✅          |
| repository set   |          ❌          |          ✅          |
| repository clear |          ❌          |          ✅          |
| settings get     |          ❌          |          ✅          |
| settings set     |          ❌          |          ✅          |
| settings clear   |          ❌          |          ✅          |
| status           |          ❌          |          ✅          |
| sync             |          ❌          |          ✅          |
| token            |          ❌          |          ✅          |
| upload           |          ✅          |          ❌          |

**Login**

Some commands like `download` or `upload` will connect to OpenBIS instance. At that time, oBIS will
use username configured in `.obis/config.json` and will ask for password whenever session expires or
username changes.

## 4.1 Standard Data Store

Standard Data Store mode depicts a workflow where datasets are stored directly in the OpenBIS
instance. In this mode user can download/upload files to OpenBIS, search for objects/datasets
fulfilling filtering criteria
and get/set properties of objects/collections represented by datasets in current repository.

## 4.1.1 Commands

**collection**

```
obis collection get [key1] [key2] ...
obis collection set [key1]=[value1], [key2]=[value2] ...
```

With `collection` command, obis crawls through current repository and gathers all data set ids and
then - if
data set is connected directly to a collection - gets or sets given properties to it in OpenBIS

*Note some property names may require to be encapsulated in '', e.g. '$name'*

**config**

```
obis config get [key]
obis config set [key]=[value]
```

With `config` command, obis can get/set config of a local repository, e.g. when setting access link
to OpenBIS instance

The settings are saved within the obis repository, in the `.obis` folder, as JSON files, or
in `~/.obis` for the global settings. They can be added/edited manually, which might be useful when
it comes to integration with other tools.

**Example `.obis/config.json`**

```
{
    "fileservice_url": null,
    "git_annex_hash_as_checksum": true,
    "hostname": "bsse-bs-dock-5-160.ethz.ch",
    "is_physical": true,
    "openbis_url": "http://localhost:8888"
}
```

**data_set**

```
obis data_set search [OPTIONS]

Options:
  -object_type, --object_type TEXT              
                                  Object type code to filter by
  -space, --space TEXT            Space code
  -project, --project TEXT        Full project identification code
  -experiment, --experiment TEXT  Full experiment code
  -type, --type TEXT              Type code
  -property TEXT                  Property code
  -property-value TEXT            Property value
  -save, --save TEXT              Directory name to save results
```

With `data_set search` command, obis connects to a configured OpenBIS instance and searches for all
data sets that fulfill given filtering criteria.
At least one filtering criteria must be specified. Search results can be downloaded by
using `save` option.

*Note: Filtering by `-project` may not work when `Project Samples` are disabled in OpenBIS
configuration.*

**download**

```
obis download [options] [data_set_id]

Options:
  -from-file, --from-file TEXT  An output .CSV file from `obis data_set search`
                                command with the list of objects to download
                                data sets from
  -f, --file TEXT               File in the data set to download - downloading
                                all if not given.
  -s, --skip_integrity_check    Flag to skip file integrity check with
                                checksums
```

The `download` command downloads, the files of a given data set from the OpenBIS instance specified
in `config`. This command requires the DownloadHandler / FileInfoHandler microservices to be running
and the `fileservice_url` needs to be configured.

**init**

```
obis init -p [folder]
```

If a folder is given, obis will initialize that folder as an obis repository that works in the
Standard Data Store mode.
If not, it will use the current folder.

**object get / set**

```
obis collection get [key1] [key2] ...
obis collection set [key1]=[value1], [key2]=[value2] ...
```

With `get` and `set` commands, obis crawls through current repository and gathers all data set ids
and then - if
data set is connected directly to an object - gets or sets given properties to it in OpenBIS

*Note some property names may require to be encapsulated in '', e.g. '$name'*

**object search**

```
obis object search [OPTIONS]

Options:
  -type, --type TEXT              Type code to filter by
  -space, --space TEXT            Space code
  -project, --project TEXT        Full project identification code
  -experiment, --experiment TEXT  Full experiment code
  -property TEXT                  Property code
  -property-value TEXT            Property value
  -save, --save TEXT              File name to save results in csv format
```

With `object search` command, obis connects to a configured OpenBIS instance and searches for all
objects/samples that fulfill given filtering criteria.
At least one filtering criteria must be specified. Search results can be downloaded int a file by
using `-save` option.

*Note: Filtering by `-project` may not work when `Project Samples` are disabled in OpenBIS
configuration.*

**upload**

```
obis upload [sample_id] [data_set_type] [OPTIONS]
```

With `upload` command, a new data set of type `data_set_type` will be created under
object `sample_id`. Files and folders specified with `-f` flag will be uploaded to a newly created
data set.

### 4.1.2 Examples

**Create an obis repository to work in Standard Data Store mode**

```
# global settings to be use for all obis repositories
obis config -g set openbis_url=https://localhost:8888
obis config -g set user=admin
# create an obis repository with a folder name
obis init -p data1
cd data1
# check configuration
obis config get is_physical
# search for objects of type BACTERIA in sapce TESTID  in OpenBIS
obis object search -space TESTID -type BACTERIA
# save search results in a files
obis object search -space TESTID -type BACTERIA -save results.csv
obis object search -space TESTID -save results_space.csv
# upload files to an existing object as type RAW_DATA
obis upload 20230228133001314-59 RAW_DATA -f results.csv -f results_space.csv
```

**download datasets of an object and check properties**

```
# assuming we are in a configured obis repository
obis download 20230228091119011-58
# set object name to XYZ
obis object set '$name'=XYZ
# set children of an object to /TESTID/PROJECT_101/PROJECT_101_EXP_3
obis object set children=/TESTID/PROJECT_101/PROJECT_101_EXP_3
```

## 4.2 External Data Store

External Data Store mode allows for orderly management of data in
conditions that require great flexibility. oBIS makes it possible to track data on a file system,
where users have complete freedom to structure and manipulate the data as they wish, while retaining
the benefits of openBIS. With oBIS, only metadata is actually stored and managed by openBIS. The
data itself is managed externally, by the user, but openBIS is aware of its existence and the data
can be used for provenance tracking.

Under the covers, obis takes advantage of publicly available and tested tools to manage data on the
file system. In particular, it uses git and git-annex to track the content of a dataset. Using
git-annex, even large binary artifacts can be tracked efficiently. For communication with openBIS,
obis uses the openBIS API, which offers the power to register and track all metadata supported by
openBIS.

### 4.2.1 Settings

With `get` you retrieve one or more settings. If the `key` is omitted, you retrieve all settings of
the `type`:

```
obis [type] [options] get [key]
```

With `set` you set one or more settings:

```
obis [type] [options] set [key1]=[value1], [key2]=[value2], ...
```

With `clear` you unset one or more settings:

```
obis [type] [options] clear [key1]
```

With the type `settings` you can get all settings at once:

```
obis settings [options] get
```

The option `-g` can be used to interact with the global settings. The global settings are stored
in `~/.obis` and are copied to an obis repository when that is created.

Following settings exist:

| type       | setting                      | description                                                                                                                                                                                                                                                                             |
|------------|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| collection |  `id`                        | Identifier of the collection the created data set is attached to. Use either this or the object id.                                                                                                                                                                                     |
| config     | `allow_only_https`           | Default is true. If false, http can be used to connect to openBIS.                                                                                                                                                                                                                      |
| config     | `fileservice_url`            | URL for downloading files. See DownloadHandler / FileInfoHandler services.                                                                                                                                                                                                              |
| config     | `git_annex_backend`          | Git annex backend to be used to calculate file hashes. Supported backends are SHA256E (default), MD5 and WORM.                                                                                                                                                                          |
| config     | `git_annex_hash_as_checksum` | Default is true. If false, a CRC32 checksum will be calculated for openBIS. Otherwise, the hash calculated by git-annex will be used.                                                                                                                                                   |
| config     | `hostname`                   | Hostname to be used when cloning / moving a data set to connect to the machine where the original copy is located.                                                                                                                                                                      |
| config     | `openbis_url`                | URL for connecting to openBIS (only protocol://host:port, without a path).                                                                                                                                                                                                              |
| config     | `openbis_token`              | Token to use when connecting to openBIS. Can be either a session token or a personal access token. Alternatively, it can be a path to a file containing the token.                                                                                                                      |
| config     | `session_name`               | The name every personal access token is associated with.                                                                                                                                                                                                                                |
| config     | `obis_metadata_folder`       | Absolute path to the folder which obis will use to store its metadata. If not set, the metadata will be stored in the same location as the data. This setting can be useful when dealing with read-only access to the data. The clone and move commands will not work when this is set. |
| config     | `user`                       | User for connecting to openBIS.                                                                                                                                                                                                                                                         |
| data_set   | `type`                       | Data set type of data sets created by obis.                                                                                                                                                                                                                                             |
| data_set   | `properties`                 | Data set properties of data sets created by obis.                                                                                                                                                                                                                                       |
| object     | `id`                         | Identifier of the object the created data set is attached to. Use either this or the collection id.                                                                                                                                                                                     |
| repository | `data_set_id`                | This is set by obis. Is is the id of the most recent data set created by obis and will be used as the parent of the next one.                                                                                                                                                           |
| repository | `external_dms_id`            | This is set by obis. Id of the external dms in openBIS.                                                                                                                                                                                                                                 |
| repository | `id`                         | This is set by obis. Id of the obis repository.                                                                                                                                                                                                                                         |

The settings are saved within the obis repository, in the `.obis` folder, as JSON files, or
in `~/.obis` for the global settings. They can be added/edited manually, which might be useful when
it comes to integration with other tools.

**Example `.obis/config.json`**

```
{
    "fileservice_url": null,
    "git_annex_hash_as_checksum": true,
    "hostname": "bsse-bs-dock-5-160.ethz.ch",
    "openbis_url": "http://localhost:8888"
}
```

**Example `.obis/data_set.json`**

```
{
    "properties": {
        "K1": "v1",
        "K2": "v2"
    },
    "type": "UNKNOWN"
}
```

## 4.2.2 Commands

**init**

```
obis init [folder]
```

If a folder is given, obis will initialize that folder as an obis in the External Data Store mode.
If not, it will use the current folder.

**init_analysis**

```
obis init_analysis [options] [folder]
```

With init_analysis, a repository can be created which is derived from a parent repository. If it is
called from within a repository, that will be used as a parent. If not, the parent has to be given
with the `-p` option.

**commit**

```
obis commit [options]
```

The `commit` command adds files to a new data set in openBIS. If the `-m` option is not used to
define a commit message, the user will be asked to provide one.

**sync**

```
obis sync
```

When git commits have been done manually, the `sync` command creates the corresponding data set in
openBIS. Note that, when interacting with git directly, use the git annex commands whenever
applicable, e.g. use "git annex add" instead of "git add".

**status**

```
obis status [folder]
```

This shows the status of the repository folder from which it is invoked, or the one given as a
parameter. It shows file changes and whether the repository needs to be synchronized with openBIS.

**clone**

```
obis clone [options] [data_set_id]
```

The `clone` command copies a repository associated with a data set and registers the new copy in
openBIS. In case there are already multiple copied of the repository, obis will ask from which copy
to clone.

- To avoid user interaction, the copy index can be chosen with the option `-c`
- With the option `-u` a user can be defined for copying the files from a remote system
- By default, the file integrity is checked by calculating the checksum. This can be skipped
  with `-s`.

_Note_: This command does not work when `obis_metadata_folder` is set.

**move**

```
obis move [options] [data_set_id]
```

The `move` command works the same as `clone`, except that the old repository will be removed.

Note: This command does not work when `obis_metadata_folder` is set.

**addref / removeref**

```
obis addref
obis removeref
```

Obis repository folders can be added or removed from openBIS. This can be useful when a repository
was moved or copied without using the `move` or `copy` commands.

**token**

```
obis token get <session_name> [--validity-days] [--validity-weeks] [--validity-months]
```

Gets or creates a new personal access token (PAT) and stores it in the obis configuration. If
no `session_name` is provided or is not stored in the configuration, you'll be asked interactively.
If no validity period is provided, the maximum (configured by the server) is used. If a PAT with
this `session_name` already exists and it is going to expire soon (according to server
setting `personal_access_tokens_validity_warning_period`), a new PAT will be created, stored in the
obis configuration and used for every subsequent request.

### 4.2.3 Examples

**Create an obis repository and commit to openBIS**

```
# global settings to be use for all obis repositories
obis config -g set openbis_url=https://localhost:8888
obis config -g set user=admin
# create an obis repository with a file
obis init data1
cd data1
echo content >> example_file
# configure the repository
obis data_set set type=UNKNOWN
obis object set id=/DEFAULT/DEFAULT
# commit to openBIS
obis commit -m 'message'
```

**Commit to git and sync manually**

```
# assuming we are in a configured obis repository
echo content >> example_file
git annex add example_file
git commit -m 'message'
obis sync
```

**Create an analysis repository**

```
# assuming we have a repository 'data1'
obis init_analysis -p data1 analysis1
cd analysis1
obis data_set set type=UNKNOWN
obis object set id=/DEFAULT/DEFAULT
echo content >> example_file
obis commit -m 'message'
```

## 5. Big Data Link Services

The Big Data Link Services can be used to download files which are contained in an obis repository.
The services are included in the installation folder of openBIS,
under `servers/big_data_link_services`. For how to configure and run them, consult
the [README.md](https://sissource.ethz.ch/sispub/openbis/blob/master/big_data_link_server/README.md)
file.

## 6. Rationale for obis

Data-provenance tracking tools like openBIS make it possible to understand and follow the research
process. What was studied, what data was acquired and how, how was data analyzed to arrive at final
results for publication -- this is information that is captured in openBIS. In the standard usage
scenario, openBIS stores and manages data directly. This has the advantage that openBIS acts as a
gatekeeper to the data, making it easy to keep backups or enforce access restrictions, etc. However,
this way of working is not a good solution for all situations.

Some research groups work with large amounts of data (e.g., multiple TB), which makes it inefficient
and impractical to give openBIS control of the data. Other research groups require that data be
stored on a shared file system under a well-defined directory structure, be it for historical
reasons or because of the tools they use. In this case as well, it is difficult to give openBIS full
control of the data.

For situations like these, we have developed `obis`, a tool for orderly management of data in
conditions that require great flexibility. `obis` makes it possible to track data on a file system,
where users have complete freedom to structure and manipulate the data as they wish, while retaining
the benefits of openBIS. With `obis`, only metadata is actually stored and managed by openBIS. The
data itself is managed externally, by the user, but openBIS is aware of its existence and the data
can be used for provenance tracking. `obis` is packaged as a stand-alone utility, which, to be
available, only needs to be added to the `PATH` variable in a UNIX or UNIX-like environment.

Under the covers, `obis` takes advantage of publicly available and tested tools to manage data on
the file system. In particular, it uses `git` and `git-annex` to track the content of a dataset.
Using `git-annex`, even large binary artifacts can be tracked efficiently. For communication with
openBIS, `obis` uses the openBIS API, which offers the power to register and track all metadata
supported by openBIS.

## 7. Literature

V. Korolev, A. Joshi, V. Korolev, M.A. Grasso, A. Joshi, M.A. Grasso, et al., "PROB: A tool for
tracking provenance and reproducibility of big data experiments", Reproduce '14. HPCA 2014, vol. 11,
pp. 264-286, 2014.
http://ebiquity.umbc.edu/_file_directory_/papers/693.pdf
