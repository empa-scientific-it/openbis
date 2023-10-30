Share IDs
=========

## Motivation

An openBIS instance for a facility often needs the possibility that each customer can have its one disk space in the data store. This means a mapping is needed to decided in eager shuffling (by using MappingBasedShareFinder) and archiving (see Archiver for Facilities) to which share and which archive the data set should go.


## Syntax

For this purpose a single mapping file is used. It is a tab-separated value file with three columns: Identifier, Share IDs, Archive Folder. The file contains a row with the headers (which can be arbitrary because they are not checked). Each following row are of the form <identifier>TAB<comma-separated share IDs>TAB<archive folder>.

- <identifier regex>: This is a regular expression for an experiment identifier (/<space code>/<project code>/<experiment code>), a project identifier (/<space code>/<project code>), or a space identifier (/<space code>).
- <comma-separated share IDs>: This is a comma-separated list of zero-to-many share IDs.
- <comma-separated archive folders>: This is a comma-separated list of absolute or relative paths to the archive folders. The list can contain zero, one or two paths. When this column is empty then the row should be ignored for archive folder mapping. When the column contains exactly one path, then it is treated as a common archive folder for all data sets no matter of their size. When the column contains two paths, then the first one is an archive folder for "big" data sets and the other is an archive folder for "small" data sets. Which data sets are considered "big" and which are "small" is controlled by "small-data-sets-size-limit" archiver property (see ZIP and TAR archivers). When this column contains two paths then "small-data-sets-size-limit" property becomes mandatory.


## Resolving Rules

The mapping algorithm selects for a specified data set a line from the mapping file in four steps:

- Pick the entry whose regular expression matches the identifier of the experiment to which the data set belongs. If such an entry exists and if it has a value (archive folder or share IDs, depending on what is needed) this entry will be selected.
- Otherwise pick the entry whose regular expression matches the project identifier and select it if it exists and has a value.
- Otherwise pick the entry whose regular expression matches the space identifier (i.e. /<space code>) and select it if it exists and has a value.
- Otherwise no entry is picked which means either no share IDs or default archive folder.

The mapping file is reloaded if it has been changed. That is, changes in the mapping do not require a restart of DSS.


## Example

    Identifier	Share IDs	Archive Folder
    /MAIER/DEFAULT/EXP1	7, 2	/net/miller/archive
    /SMITH	6	/net/smith/openbis/archive-big, /net/smith/openbis/archive-small
    /MAIER/DEFAULT	2	
    /MAIER	1	/net/maier/archive

The following table shows the archive folder and the list of share IDs for various experiment identifiers:

    /MAIER/DEFAULT/EXP7	2	/net/maier/archive
    /MAIER/DEFAULT/EXP1	7, 2	/net/miller/archive
    /MAIER/PROJECT-X/EXP1	1	/net/maier/archive
    /SMITH/P786/E775	6	/net/smith/openbis/archive-big when a data set is considered "big" and /net/smith/openbis/archive-small when a data set is considered "small"
    /MILLER/AKZU-3/E83	 	<default archive folder>