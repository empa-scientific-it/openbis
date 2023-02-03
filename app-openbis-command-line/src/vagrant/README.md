# Getting started with Vagrant, openBIS and obis

## Quickstart -- Setup

To help users interested in trying out **obis**, we provide a vagrant setup that can be used to quickly create a test environment. The environment automates most of the installation process, but there are a few steps that must be done manually.

0. Download openbis from the https://wiki-bsse.ethz.ch/display/bis/Production+Releases
1. Put the extract the openbis installer to the obis/src/vagrant/initialize folder
2. cd to `obis/src/vagrant`
3. `vagrant plugin install vagrant-vbguest` -- install the vagrant-vbguest plugin
3. `vagrant up --provision --provider virtualbox` -- initialize the virtual machine

This sets up two machines:
1. obisserver - contains an obis client and an openBIS installation which is started when the machine comes up
2. obisclient - contains a second obis client for scenarios where two are needed

## Running openBIS

0. log into the obisserver machne: `vagrant ssh obisserver`
1. switch to the openbis user: `sudo su openbis`
3. run the start script: `~/bin/allup.sh`

When openBIS is running it can be accessed on the host machine from `https://localhost:8443/openbis`.

## Using obis

obis can be used on both vagrant machines.

1. `vagrant ssh obisserver` (or `obisclient`)
2. `sudo su obis`
3. `cd ~obis`
4. Configure the openbis_url: `obis config -g set openbis_url=https://obisserver:8443`
5. Use obis.
6. `exit` -- log off the virtual machine

If you clone a dataset on the other server, the password of the obis user is obis.

## obis/EasyBD Demo

### Preflight

First, make sure the environment is set up as expected.

We will use the screening server, https://sprint-openbis.ethz.ch:8446. Make sure that it has the following data set type configured:

    name: GIT_REPO
    kind: LINK
    properties: DESCRIPTION

Also make sure that there is a sample with the identifier /DEMO/BIGDATA. The type does not matter.

Check the obis configuration.

    obis config -g get

The following should be configured:

    openbis_url: https://sprint-openbis.ethz.ch:8446
    user: adamsr
    data_set_type: GIT_REPO
    verify_certificates: False

This is probably the case, but if it is not, you need to execute the following commands. We will set this environment globally, so it is not necessary for every single data set.

    obis config -g set openbis_url=https://sprint-openbis.ethz.ch:8446
    obis config -g set user=adamsr
    obis config -g set data_set_type=GIT_REPO
    obis config -g set verify_certificates=false

### Demo

Here, we will create a data set and then go look at it.

    mkdir example_data
    cd example_data
    obis init data .

Now create a file and put some content into it. E.g.,

    vi info.txt

When finished, we need to set the sample\_id or experiment\_id this data set should be associated with. Then we can commit it.

    obis object set id=/DEMO/BIGDATA
    obis commit -m"Initial data commit."

Log into the sprint server https://sprint-openbis.ethz.ch:8446 and go to the sample /DEMO/BIGDATA. You should see the newly registered data set there. You can take a look at it and see file metadata for its contents.

Now edit the file again:

    vi info.txt

If you run

    obis status

You should see that the file is dirty. Now, before we commit, we will set some data set metadata.

    obis data_set set properties='{"DESCRIPTION": "Version 1"}'

And now we commit:

    obis commit

You will be prompted for a commit message. Provide one.

In openBIS, you will find a new data set associated with the sample /DEMO/BIGDATA. The previous data set its parent. It will have a property _Description_ set to "Version 1".


# How this folder is organized

- `Vagrantfile` -- includes all information how the virtual machine needs to be set up: including synched folders, port forwarding, shell scripts to run, memory consumption etc.
- `initialize/` -- folder contains shell scripts that are run during the provisioning process (`setup_*`) and some `start_*` and `stop_*` scripts to start services on the virtual machine (eg. JupyterHub, openBIS)
- `config/` -- folder contains configuration files for openBIS and Postgres database.

The `initialize/` folder is synched, which means it is visible inside the virtual machine.


## About Vagrant virtual environment

Vagrant automates the creation of virtual environments. Here, we use vagrant to define an environment for running openBIS with the obis command-line tool. You can download Vagrant from this website:

https://www.vagrantup.com

Vagrant needs a virtualizing software in order to run the virtual machine. Vagrant works well with many backend providers; by default it works with VirtualBox (https://www.virtualbox.org/), but you can use other providers as well. Vagrant acts like a remote control to start virtual machines.

When setting up a machine the first time, Vagrant reads a file called «Vagrantfile». This file contains information about which OS template to start with (we use CentOS 7). It then continues with all the shell commandos in order to set up our virtual machine.


## Setting up the virtual machine (vagrant)

0. cd to `src/vagrant`
1. `vagrant plugin install vagrant-vbguest`
2. `vagrant up --provision --provider virtualbox` -- this will read `Vagrantfile` and provision a CentOS 7 VM and install most software prerequisites (python, JupyterHub, etc.). This can take a while and needs a fast internet connection too.
3. Commands to control the machine:
    - `vagrant halt` -- shut down machine
    - `vagrant up`   -- restart machine
    - `vagrant ssh`  -- log in
4. all vagrant commands need to be executed inside the `/vagrant` directory, because the command always reads the `Vagrantfile`


## start openBIS

1. `vagrant ssh` -- to log into the virtual machine
2. `/vagrant_initialize/start_services.sh` -- start openBIS and any other services
3. point your browser to `https://localhost:8443/openbis/` and check whether your server is up and running. Try logging in as an admin user, for example.
4. check the AS and DSS logs if you encounter any problems
   * `/home/openbis/bin/bislog.sh` -- openbis AS logfile
   * `/home/openbis/bin/dsslog.sh` -- datastore server DSS logfile
5. You can access the openBIS instance at https://localhost:8443/openbis/.

## create a new user in openBIS

1. point your browser to the ELN-LIMS Lab notebook running at `https://localhost:8443/openbis/webapp/eln-lims/` and log in as admin
2. go to Utilities -> User Manager, click on the Operations dropdown and choose "ceate User"
3. enter a username (User ID) and a password to create a new user in openBIS (this may a while)
