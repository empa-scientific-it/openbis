openBIS Server Installation
===========================

## Contents of openBIS Installer Tarball

The server distribution is a `gzipped` `tar` file named `openBIS-installation-standard-technologies-<version>.tar.gz`. It contains the following files:

- `console.properties:` Installation configuration file

- `extract.sh:` helper script for installation

- `jul.config:` Log configuration for the openBIS install process

- `openBIS-installer.jar` Java archive containing openBIS

- `run-console.sh` Installation script


## Installation Steps

1. Create a service user account, i.e. an unprivileged, regular user account. **Do not run openBIS as root!**

2. Gunzip the distribution on the server machine into some temporary folder:
    ```bash
    mkdir tmp
    mv xvfz openBIS-installation-standard-technologies-<release-number>.tar.gz tmp/
    cd tmp/
    tar xvfz openBIS-installation-standard-technologies-<release-number>.tar.gz
    ```

3. Customize the `console.properties` file by specifying values for at least the following parameters: `INSTALL_PATH`, `DSS_ROOT_DIR`, `INSTALLATION_TYPE`, `ELN-LIMS`, and `ELN-LIMS-LIFE-SCIENCES`. Each parameter is documented inline.

4. Run installation script:
    ```bash
    ./run-console.sh
    ```

When done, openBIS is installed in the directory specified as `INSTALL_PATH` in the `console.properties`. Within this system admin documentation pages, we're referring this path as `$INSTALL_PATH`.

```{note}
Please be aware that the directory where openBIS is installed should not already exist. Users should specify the directory where they want to install openBIS (in the console.properties) and this directory will be created by the installation procedure. If the directory already exists, the installation will fail, except from when the installer detects that it already contains an existing openBIS installation. In the latter case, the installer will try to upgrade the existing release to the one to be installed by invoking $INSTALL_PATH/bin/upgrade.sh.
```