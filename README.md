# Building openBIS

## Requirements

- JDK11 or JDK 17

## Step By Step:

```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd app-openbis-installer/
./gradlew clean
./gradlew build -x test "-Dorg.gradle.jvmargs=--add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
```

## Where the build is found?

```
./app-openbis-installer/targets/gradle/distributions/openBIS-installation-standard-technologies-SNAPSHOT-rXXXXXXXXXX.tar.gz
```

## Why we disable tests to make the build?

They increase the time to obtain a build plus some tests could have additional environment
requirements.

## Why the core UI made using GWT is not build anymore?

It increases the time to obtain a build plus it requires JDK8, it will be removed on next release.
For now it can be build following the next commands and only with JDK8:

```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd core-plugin-openbis/
./gradlew clean
./gradlew buildCoreUIPackageUsingJDK8 -x test
```

## How to compile the V3 JS bundle used by the new Admin UI in production?

```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd core-plugin-openbis/
./gradlew clean
./gradlew bundleOpenbisStaticResources -x test
```

The output can be found at:
server-application-server/source/java/ch/systemsx/cisd/openbis/public/resources/api/v3

config.bundle.js
config.bundle.min.js
openbis.bundle.js
openbis.bundle.min.js

# Development of openBIS

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

Turn off "File | Settings | Build, Execution, Deployment | Compiler | Java Compiler | Use --release
option for cross-compilation".

## Development of NG UI

1. Generate openBIS JS bundle by running in command line
    1. cd /<OPENBIS_PROJECT_ROOT>/core-plugin-openbis
    2. ./gradlew :bundleOpenbisStaticResources
2. Start openBIS in your chosen IDE (NG UI assumes it will run
   at: http://localhost:8888/openbis-test/):
    1. run openBISDevelopementEnvironmentASPrepare gradle task
    2. run openBISDevelopementEnvironmentASStart gradle task
3. In command line do:
    1. cd /<OPENBIS_PROJECT_ROOT>/ui-admin
    2. npm install
    3. npm run dev
4. Open in your chosen browser a url, by default: http://localhost:8124/ng-ui-path

## Setting up IntelliJ Idea

1. Under "IntelliJ IDEA" -> "Preferences" -> "Languages and Frameworks" -> Javascript, set the
   language version to ECMAScript 6.

## Setting up Visual Studio Code (alternative to IntelliJ Idea)

Install "ESLint" and "Prettier - Code formatter" extensions.
