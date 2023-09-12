Search Domain Services
======================

A search domain service is a DSS plugin which allows to query some
domain specific search services. For example, a search service on a
database of nucleotide acid sequences. Currently only one search service
is supported: Searching of local BLAST databases for nucleotide and/or
protein sequences.

## Configuring a Service

To configure a service a
[core-plugin](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/core-plugins.html) of
type `search-domain-services` has to be created. The minimum
configuration for `plugin.properties` reads:

||Description|
|--- |--- |
|class|Fully qualified name of a Java class implementing ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService|
|label|The label. Can be used in user interfaces.|

## Querying a Service

Search domain services can be accessed via `IGeneralInformationService`.
The method `listAvailableSearchDomains` returns all available services.

A service can be queried by the method `searchOnSearchDomain`. Beside of
the `sessionToken` it has the following parameters:

-   `preferredSearchDomainOrNull`: This can be `null` If there is only
    one service configured. Otherwise the name of the core-plugin
    specifies the preferred services. If no such service hasn't been
    configured or it isn't be available the first available service will
    be used. If there is no available service the search will return an
    empty list.
-   `searchString`: This is the string to search for.
-   `optionalParametersOrNull`: This is a map of string-string key-value
    pairs of optional parameters. Can be `null`. The semantics of these
    parameters depends on the used service.

The method returns a list of `SearchDomainSearchResult` instances which
contain the following attributes: A description of the search domain
(class `SearchDomain`), the location
(interface `ISearchDomainResultLocation`), and a score. The result list
is sorted by score in descending order. The location has information
where the sequence is stored in openBIS and where it matches the search
string.

## Service Implementations

### BlastDatabase

**Description**: This implementations requires the
[BLAST+](http://blast.ncbi.nlm.nih.gov/Blast.cg) tools. The latest
versions can be downloaded from
[here](ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/).
Note, that this service is only available if the BLAST+ tools have been
installed. Only the tools `blastn` (for nucleotide search) and `blastp`
(for protein search) are used.

In order to build up a local BLAST database the maintenance task
[BlastDatabaseCreationMaintenanceTask](https://openbis.readthedocs.io/en/latest/system-admin-documentation/advanced-features/maintenance-tasks.html#blastdatabasecreationmaintenancetask) has to be configured.

Because the maintenance task to create the BLAST databases runs often
only once per day a change in entity properties or a registration of a 
data sets will not immediately be reflected by the search results. That
is, new sequences aren't found and changed/deleted sequences are still
found.

**Configuration**:

|Property Key|Description|
|--- |--- |
|blast-tools-directory|Path to the directory with BLAST+ command line tools. If defined it will be prepended to the commands blastn and blastp. If undefined it is assumed that the path is in the PATH environment variable.|
|blast-databases-folder|Path to the folder where all BLAST databases are stored. Default: <data store root>/blast-databases|

**Example**:

**plugin.properties**

    class = ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases.BlastDatabase
    label = BLAST database

#### **Optional Query Parameters**

The following optional query parameters (i.e. service method
parameter `optionalParametersOrNull` as described above) are understood
and used as command line parameters of the BLAST+ tools:

|Name            |Description                    |
|----------------|-------------------------------|
|`evalue`        |Defines the threshold of so-called "Expect Value" of found matches (for details see http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=FAQ#expect and http://homepages.ulb.ac.be/~dgonze/TEACHING/stat_scores.pdf). Higher values means more found matches. Default value is 10.|
|`word_size`     |Word size for initial match. Decreasing word size results in increasing number of matches. Default values (if `task` parameter hasn't been specified): 11 for `blastn` and 3 for `blastp`.|
|`task`          |Defines values for a set of parameters of the tools blastn and blastp. Possible values are<ul><li>blastn: Default value blastn</li></ul><table>  <thead>  <tr>  <th>Value</th>  <th>Description</th>  <th>Default value of word_size</th>  </tr>  </thead>  <tbody>  <tr>  <td>blastn-short</td>  <td>`blastn` program optimized for sequences shorter than 50 bases</td>  <td>7</td>  </tr>  <tr>  <td>blastn</td>  <td>Traditional `blastn` requiring an exact match of 11</td>  <td>11</td>  </tr>  <tr>  <td>dc-megablast</td>  <td>Discontiguous megablast used to find more distant (e.g., interspecies) sequences</td>  <td>11</td>  </tr> <tr>  <td>megablast</td>  <td>Traditional megablast used to find very similar (e.g., intraspecies or closely related species) sequences</td>  <td>28</td>  </tr> </tbody>  </table><ul><li>blastp: Default value blastp</li></ul><table>  <thead>  <tr>  <th>Value</th>  <th>Description</th>  <th>Default value of word_size</th>  </tr>  </thead>  <tbody>  <tr>  <td>blastp</td>  <td>Traditional `blastp` to compare a protein query to a protein database</td>  <td>3</td>  </tr>  <tr>  <td>blastp-short</td>  <td>`blastp` optimized for queries shorter than 30 residues</td>  <td>2</td>  </tr>  </tbody>  </table>     |
|ungapped        |If specified (with an empty string value) only ungapped matches are returned. Will be ignored for `blastp`.|

For more details about these parameters see
<http://www.ncbi.nlm.nih.gov/books/NBK1763/>.

#### Search Results

A search result has either a `DataSetFileBlastSearchResultLocation` or
an `EntityPropertyBlastSearchResultLocation` instance depending on
whether the result has been found in a sequence of a FASTA or FASTQ file
of a data set or in a sequence stored as a property of an experiment, a
sample or a data set. In any case the following informations can be
retrieved for each match:

|BLAST output column|Access in Java                                                                          |Description                                                                                                                                                                                          |
|-------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|`score`              |`SearchDomainSearchResult.getScore().getScore()`                                          |=HYPERLINK("http://homepages.ulb.ac.be/~dgonze/TEACHING/stat_scores.pdf","Score. See http://homepages.ulb.ac.be/~dgonze/TEACHING/stat_scores.pdf for an explanation of score, bit-score and evalue.")|
|`bitscore`           |`SearchDomainSearchResult.getScore().getBitScore()`                                       |                                                                                                                                                                                                     |
|`evalue`             |`SearchDomainSearchResult.getScore().getEvalue()`                                         |                                                                                                                                                                                                     |
|`sstart`             |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getSequenceStart()`     |Start of alignment in  found sequence                                                                                                                                                                |
|`send`               |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getSequenceEnd()`       |End of alignment in  found sequence                                                                                                                                                                  |
|`qstart`             |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getQueryStart()`        |Start of alignment in  search string.                                                                                                                                                                |
|`qend`               |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getQueryEnd()`          |End of alignment in  search string.                                                                                                                                                                  |
|`mismatch`           |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getNumberOfMismatches()`|Number of mismatches.                                                                                                                                                                                |
|`gaps`               |`SearchDomainSearchResult.getResultLocation().getAlignmentMatch().getTotalNumberOfGaps()` |Total number of gap.                                                                                                                                                                                 |
