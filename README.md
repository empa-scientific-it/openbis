# Building openBIS

## Requirements
- JDK8 or JDK11

## Step By Step:
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd installation/
./gradlew clean
./gradlew build -x test
```
## Where the build is found?
```
./installation/targets/gradle/distributions/openBIS-installation-standard-technologies-SNAPSHOT-rXXXXXXXXXX.tar.gz
```

## Why we disable tests to make the build?
They increase the time to obtain a build plus some tests could have additional environment requirements.

## Why the core UI made using GWT is not build anymore?
It increases the time to obtain a build plus it requires JDK8, it will be removed on next release. For now it can be build following the next commands:
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd openbis_standard_technologies/
./gradlew clean
./gradlew buildCoreUIPackageUsingJDK8 -x test
```
## How to compile the V3 JS bundle used by the new Admin UI in production?
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd openbis_standard_technologies/
./gradlew clean
./gradlew bundleOpenbisStaticResources -x test
```
The output can be found at: openbis/source/java/ch/systemsx/cisd/openbis/public/resources/api/v3

config.bundle.js
config.bundle.min.js
openbis.bundle.js
openbis.bundle.min.js

# Developing openBIS

## Requirements
- Postgres 11
- IntelliJ IDEA CE

## Step By Step:
```
File -> New -> Project From Existing Sources
Select the gradle folder to load the gradle model
After the model is loaded execute the tasks:

openBISDevelopementEnvironmentASPrepare
openBISDevelopementEnvironmentASStart
openBISDevelopementEnvironmentDSSStart
```

## IntelliJ can't find package com.sun.*, but I can compile the project using the command line!
Turn off "File | Settings | Build, Execution, Deployment | Compiler | Java Compiler | Use --release option for cross-compilation".
