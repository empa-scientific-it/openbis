# oBIS

oBIS is a command-line tool to handle dataSets that are too big to store in openBIS but still need to be registered and tracked in openBIS.

## Prerequisites

- python 3.6 or higher
- git 2.11 or higher
- git-annex 6 or higher [Installation guide](https://git-annex.branchable.com/install/)

## Installation

```
pip3 install obis
```

Since `obis` is based on `pybis`, the pip command will also install pybis and all its dependencies.

## Usage

### Help is your friend!

```
$ obis --help
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
  download       Download files of a linked data set.
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

## Settings

With `get` you retrieve one or more settings. If the `key` is omitted, you retrieve all settings of the `type`:

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

The option `-g` can be used to interact with the global settings. The global settings are stored in `~/.obis` and are copied to an obis repository when that is created.

Following settings exist:

| type       | setting                      | description                                                                                                                                                                                                                                                                             |
| ---------- | ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| collection |  `id`                        | Identifier of the collection the created data set is attached to. Use either this or the object id.                                                                                                                                                                                     |
| config     | `allow_only_https`           | Default is true. If false, http can be used to connect to openBIS.                                                                                                                                                                                                                      |
| config     | `fileservice_url`            | URL for downloading files. See DownloadHandler / FileInfoHandler services.                                                                                                                                                                                                              |
| config     | `git_annex_backend`          | Git annex backend to be used to calculate file hashes. Supported backends are SHA256E (default), MD5 and WORM.                                                                                                                                                                          |
| config     | `git_annex_hash_as_checksum` | Default is true. If false, a CRC32 checksum will be calculated for openBIS. Otherwise, the hash calculated by git-annex will be used.                                                                                                                                                   |
| config     | `hostname`                   | Hostname to be used when cloning / moving a data set to connect to the machine where the original copy is located.                                                                                                                                                                      |
| config     | `openbis_url`                | URL for connecting to openBIS (only protocol://host:port, without a path).                                                                                                                                                                                                              |
| config     | `openbis_token`              | Token to use when connecting to openBIS. Can be either a session token or a personal access token. Alternatively, it can be a path to a file containing the token.                                                                                                                      |
| config     | `session_name`              | The name every personal access token is associated with.                                                                                                                      |
| config     | `obis_metadata_folder`       | Absolute path to the folder which obis will use to store its metadata. If not set, the metadata will be stored in the same location as the data. This setting can be useful when dealing with read-only access to the data. The clone and move commands will not work when this is set. |
| config     | `user`                       | User for connecting to openBIS.                                                                                                                                                                                                                                                         |
| data_set   | `type`                       | Data set type of data sets created by obis.                                                                                                                                                                                                                                             |
| data_set   | `properties`                 | Data set properties of data sets created by obis.                                                                                                                                                                                                                                       |
| object     | `id`                         | Identifier of the object the created data set is attached to. Use either this or the collection id.                                                                                                                                                                                     |
| repository | `data_set_id`                | This is set by obis. Is is the id of the most recent data set created by obis and will be used as the parent of the next one.                                                                                                                                                           |
| repository | `external_dms_id`            | This is set by obis. Id of the external dms in openBIS.                                                                                                                                                                                                                                 |
| repository | `id`                         | This is set by obis. Id of the obis repository.                                                                                                                                                                                                                                         |

The settings are saved within the obis repository, in the `.obis` folder, as JSON files, or in `~/.obis` for the global settings. They can be added/edited manually, which might be useful when it comes to integration with other tools.

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

## Commands

**init**

```
obis init [folder]
```

If a folder is given, obis will initialize that folder as an obis repository. If not, it will use the current folder.

**init_analysis**

```
obis init_analysis [options] [folder]
```

With init_analysis, a repository can be created which is derived from a parent repository. If it is called from within a repository, that will be used as a parent. If not, the parent has to be given with the `-p` option.

**commit**

```
obis commit [options]
```

The `commit` command adds files to a new data set in openBIS. If the `-m` option is not used to define a commit message, the user will be asked to provide one.

**sync**

```
obis sync
```

When git commits have been done manually, the `sync` command creates the corresponding data set in openBIS. Note that, when interacting with git directly, use the git annex commands whenever applicable, e.g. use "git annex add" instead of "git add".

**status**

```
obis status [folder]
```

This shows the status of the repository folder from which it is invoked, or the one given as a parameter. It shows file changes and whether the repository needs to be synchronized with openBIS.

**clone**

```
obis clone [options] [data_set_id]
```

The `clone` command copies a repository associated with a data set and registers the new copy in openBIS. In case there are already multiple copied of the repository, obis will ask from which copy to clone.

- To avoid user interaction, the copy index can be chosen with the option `-c`
- With the option `-u` a user can be defined for copying the files from a remote system
- By default, the file integrity is checked by calculating the checksum. This can be skipped with `-s`.

_Note_: This command does not work when `obis_metadata_folder` is set.

**move**

```
obis move [options] [data_set_id]
```

The `move` command works the same as `clone`, except that the old repository will be removed.

Note: This command does not work when `obis_metadata_folder` is set.

**download**

```
obis download [options] [data_set_id]
```

The `download` command downloads the files of a data set. Contrary to `clone`, this will not register another copy in openBIS. It is only for accessing files. This command requires the DownloadHandler / FileInfoHandler microservices to be running and the `fileservice_url` needs to be configured.

**addref / removeref**

```
obis addref
obis removeref
```

Obis repository folders can be added or removed from openBIS. This can be useful when a repository was moved or copied without using the `move` or `copy` commands.

**token**

```
obis token get <session_name> [--validity-days] [--validity-weeks] [--validity-months]
```

Gets or creates a new personal access token (PAT) and stores it in the obis configuration. If no `session_name` is provided or is not stored in the configuration, you'll be asked interactively. If no validity period is provided, the maximum (configured by the server) is used. If a PAT with this `session_name` already exists and it is going to expire soon (according to server setting `personal_access_tokens_validity_warning_period`), a new PAT will be created, stored in the obis configuration and used for every subsequent request.



## Examples

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

## Big Data Link Services

The Big Data Link Services can be used to download files which are contained in an obis repository. The services are included in the installation folder of openBIS, under `servers/big_data_link_services`. For how to configure and run them, consult the [README.md](https://sissource.ethz.ch/sispub/openbis/blob/master/big_data_link_server/README.md) file.

## Rationale for obis

Data-provenance tracking tools like openBIS make it possible to understand and follow the research process. What was studied, what data was acquired and how, how was data analyzed to arrive at final results for publication -- this is information that is captured in openBIS. In the standard usage scenario, openBIS stores and manages data directly. This has the advantage that openBIS acts as a gatekeeper to the data, making it easy to keep backups or enforce access restrictions, etc. However, this way of working is not a good solution for all situations.

Some research groups work with large amounts of data (e.g., multiple TB), which makes it inefficient and impractical to give openBIS control of the data. Other research groups require that data be stored on a shared file system under a well-defined directory structure, be it for historical reasons or because of the tools they use. In this case as well, it is difficult to give openBIS full control of the data.

For situations like these, we have developed `obis`, a tool for orderly management of data in conditions that require great flexibility. `obis` makes it possible to track data on a file system, where users have complete freedom to structure and manipulate the data as they wish, while retaining the benefits of openBIS. With `obis`, only metadata is actually stored and managed by openBIS. The data itself is managed externally, by the user, but openBIS is aware of its existence and the data can be used for provenance tracking. `obis` is packaged as a stand-alone utility, which, to be available, only needs to be added to the `PATH` variable in a UNIX or UNIX-like environment.

Under the covers, `obis` takes advantage of publicly available and tested tools to manage data on the file system. In particular, it uses `git` and `git-annex` to track the content of a dataset. Using `git-annex`, even large binary artifacts can be tracked efficiently. For communication with openBIS, `obis` uses the openBIS API, which offers the power to register and track all metadata supported by openBIS.

## Literature

V. Korolev, A. Joshi, V. Korolev, M.A. Grasso, A. Joshi, M.A. Grasso, et al., "PROB: A tool for tracking provenance and reproducibility of big data experiments", Reproduce '14. HPCA 2014, vol. 11, pp. 264-286, 2014.
http://ebiquity.umbc.edu/_file_directory_/papers/693.pdf
