System Requirements
===================

## Architecture

As of today, openBIS can be deployed on the AMD64 (x86_64) architecture.
Support for ARM architecture is currently being developed.


## Hardware Configuration

Starting from openBIS version 20.10.0, openBIS memory and CPU usage requirements have remarkably dropped. The following guidelines cannot be applied to previous versions.

Below we provide recommended (virtual) hardware and database (PostgreSQL) server settings for three common use-cases:

| Parameter | Small | Medium | Big |
|---|---|---|---|
| Default ELN LIMS UI using Generic or Life-Sciences Technologies | x | x | x |
| Old core UI still actively used  | | x | x |
| up to 5 concurrent users | x | x | x |
| up to 20 concurrent users | | x | x |
| more than 20 concurrent users | | | x |

Please bear in mind that, the more customised an openBIS installation is, the more the recommended settings may vary from the optimal ones.


### CPU and Memory Configuration

| Scenario | Number of CPUs | Total memory | Memory allocated to OS | Memory allocated to PostgreSQL | Memory allocated to openBIS Application Server | Memory allocated to openBIS Data Store Server |
|---|---|---|---|---|---|---|
| Small | 2 modern x86 CPU cores | 4 GB | 1.5 GB | 1 GB | 1 GB | 0.5 GB |
| Medium | 2-4 modern x86 CPU cores | 8 GB | 2 GB | 2 GB | 3GB | 1 GB |
| Big | 4-8 modern x86 CPU cores | 16 GB | 3 GB | 3 GB | 8 GB | 2 GB |


### Postgres Memory Settings

Memory-related settings of your PostgreSQL server can be obtained from https://pgtune.leopard.in.ua/. For the "small" scenario, use the below template:

| Parameter | Value |
|---|---|
| DB Version | 15 |
| OS Type | (depends on your infrastructure) |
| DB Type | Web Applicaiton |
| Total Memory (RAM) | 3 GB |
| Number of CPUs | 2 |
| Number of Connections | 50 |
| Data Storage | (depends on your infrastructure) |

After clicking on "Generate", you get the matching settings of the postgresql.conf displayed, jointly with the commands to be used to apply these (ALTER SYSTEM).


### Tuning Of Hardware Settings In Case Of Issues

| Symptom | Recommended Action |
|---|---|
| Long query execution times | Increase CPU number and/or AS & Postgres memory settings, reconfigure Postgres and openBIS memory settings following the recommended settings provided by https://pgtune.leopard.in.ua/. |
| AS log shows out of memory errors | Increase AS Memory. This easily happens in old installations using the Legacy Core UI that requires additional memory for cache. |
| DSS log shows out of memory errors | Increase DSS Memory. |


## Operating System

We recommend to set up openBIS on a Linux operating system. We provide support for installing and operating openBIS on supported [Ubuntu Server LTS releases ](https://ubuntu.com/server).
- Operating System: Linux / MacOS X


## Third-Party Packages

The following software packages are required:

- The binaries `bash`, `awk`, `sed` and `unzip` need to be installed and in the `PATH` of the openBIS user.
- Java Runtime Environment: recent versions of Oracle JRE 11 or OpenJDK 11
- PostgreSQL 15


## Additional Requirements

An SMTP server needs to be accessible if you want openBIS to send out notifications via mail. We recommend to use the a local mail transfer agent such as Postfix configued for message sending.