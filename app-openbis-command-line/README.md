# obis

Data-provenance tracking tools like openBIS make it possible to understand and follow the research process. What was studied, what data was acquired and how, how was data analyzed to arrive at final results for publication -- this is information that is captured in openBIS. In the standard usage scenario, openBIS stores and manages data directly. This has the advantage that openBIS acts as a gatekeeper to the data, making it easy to keep backups or enforce access restrictions, etc. However, this way of working is not a good solution for all situations.

Some research groups work with large amounts of data (e.g., multiple TB), which makes it inefficient and impractical to give openBIS control of the data. Other research groups require that data be stored on a shared file system under a well-defined directory structure, be it for historical reasons or because of the tools they use. In this case as well, it is difficult to give openBIS full control of the data.

For situations like these, we have developed `obis`, a tool for orderly management of data in conditions that require great flexibility. `obis` makes it possible to track data on a file system, where users have complete freedom to structure and manipulate the data as they wish, while retaining the benefits of openBIS. With `obis`, only metadata is actually stored and managed by openBIS. The data itself is managed externally, by the user, but openBIS is aware of its existence and the data can be used for provenance tracking. `obis` is packaged as a stand-alone utility, which, to be available, only needs to be added to the `PATH` variable in a UNIX or UNIX-like environment.

Under the covers, `obis` takes advantage of publicly available and tested tools to manage data on the file system. In particular, it uses `git` and `git-annex` to track the content of a dataset. Using `git-annex`, even large binary artifacts can be tracked efficiently. For communication with openBIS, `obis` uses the openBIS API, which offers the power to register and track all metadata supported by openBIS.

# Installation

Since `obis` is based on `pybis`, it requires python 3.6 and the corresponding `pip3`.

First, install `pybis` if it is not already installed. Then install `obis` (paths relative to repository root):

```
pip3 install src/python
```

# See also

  V. Korolev, A. Joshi, V. Korolev, M.A. Grasso, A. Joshi, M.A. Grasso, et al., "PROB: A tool for tracking provenance and reproducibility of big data experiments", Reproduce'14. HPCA 2014, vol. 11, pp. 264-286, 2014.
  http://ebiquity.umbc.edu/\_file\_directory\_/papers/693.pdf
