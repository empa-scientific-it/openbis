# Contributing to pyBIS

## Run the tests

In order to avoid endless mocking, the tests rely on a locally running instance of openBIS.

- create a folder to store the openBIS state
- mkdir -p ~/openbis_state
- Copy & paste the command below to download the openBIS docker image and run it locally on port 8443:

```sh
docker run -e SERVER_HOST_PORT=localhost:8443 -e GROUP_ID=12940 -e GROUP_NAME="docker-host-folder-group" -e CORE_PLUGINS='enabled-modules = monitoring-support, dropbox-monitor, dataset-uploader, dataset-file-search, xls-import, openbis-sync, eln-lims, eln-lims-life-sciences' -v ~/openbis_state:/home/openbis/openbis_state -p 8443:443 openbis/debian-openbis:20.10.2.3
```

Once the local openBIS instance is up and running, run the tests:

```bash
pytest tests
```

## Make a release

- `git flow release start pybis-1.32.0`
- add the most important changes to CHANGELOG.md
- bump the version number in `setup.py` and `/pybis/__init__.py`
  - increase the last digit +1 for bugfixes
  - increase the middle digit +1 for improvements and added features
  - increase the first digit +1 for breaking changes and backward-incompatibilities
- after commiting, finish the release with `git flow release finish pybis-1.32.0`
- create a distribution: `python setup.py sdist`
- upload the distribution: `twine upload dist/PyBIS-1.32.0.tar.gz`

## Make a pre-release

- set the version number in `setup.py` to `1.32.0rc1` (to specify a release candidate, for example)
- see https://peps.python.org/pep-0440/
- create a distribution: `python setup.py sdist`
- upload the distribution: `twine upload dist/PyBIS-1.32.0rc1.tar.gz`
- people will be able to install the test distribution using `pip install PyBIS==1.32.0rc1`
- a normal `pip install pybis` will not install a pre-release, only stable releases
