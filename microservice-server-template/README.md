# MICROSERVICE SERVER #

## Introduction ##

This project is to be used to developed Java micro services.

## Configuration ##

A json file with the configuration should be given during the startup as an argument. If it os not given the default "config.json" will be loaded (mostly used for distribution) and if not found "./conf/config.json" is loaded (mostly used for development).

## Build ##

./gradlew distZip

The build will be found at ./build/distributions/big_data_link_server.zip

## Startup ##

Unzip the build and execute

./bin/microservice-server-template

## Main packages ##

### ch.ethz.sis.microservices.api ###
Supposed to contain only immutable DTOs annotated with lombok's @Data.

### ch.ethz.sis.microservices.server.json ###
JSON object mapper interface and implementations.

### ch.ethz.sis.microservices.server.logging ###
Logging interface and implementation.

### ch.ethz.sis.microservices.server.services ###
Service interface and implementations.

To implement a new service is sufficient to extend the class ch.ethz.sis.microservices.server.services.Service. This class extends the standard J2EE HttpServlet and is up to the implementer to decide to accept the different http methods.

This class just provides a convenience method getServiceConfig() to retrieve the configuration.

### ch.ethz.sis.microservices.server.startup ###
Main class and launcher.

### ch.ethz.sis.microservices.util ###
Utility classes.