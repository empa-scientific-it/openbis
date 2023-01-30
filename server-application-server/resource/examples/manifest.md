Examples
========

This folder contains several example core-plugins technologies that illustrate different aspects of openBIS functionality. This file explains what these examples do.

service-demo
============

The service-demo example core-plugin technology demonstrates the integration between openBIS webapps and aggregation and ingestion services. 

Services
--------

The example includes an aggregation service, example-aggregation-service, and an ingestion service, example-ingestion-service. The example-aggregation-service returns a table that contains the parameters pass in the by the caller. To install the example, add the service-demo folder to the openBIS core-plugins folder. Include "service-demo" in the `enabled-technologies` property in the AS and DSS service.properties files. 

The example-ingestion-service registers a sample of type `LIBRARY`. This script assumes that there is an experiment with the identifier `/TEST/TEST-PROJECT/DEMO-EXP-HCS` and a sample type `LIBRARY`. This experiment and sample type are automatically installed by the openBIS installer in a fresh installation. If they do not exist in your installation, you will need to change the script to register a sample type that exists and associate it with an experiment that exists.

Note, by default, the `LIBRARY` sample type is not shown in the standard sample lists. To see the registered samples, you will need to explicitly search for it. This can be done by entering "LIBRARY" in the openBIS search box.

Webapps
-------

This example includes two webapps, `aggregation` and `ingestion`. The aggregation webapp calls the example-aggregation-service and displays the result. The ingestion webapp calls the example-ingestion-service, specifying a sample code determined by the the current time, and displays the result.

The aggregation webapp is available at the url: {server url}/aggregation (e.g., https://localhost:8443/aggregation/ for a local installation).
The ingestion webapp is available at the url: {server url}/ingestion (e.g., https://localhost:8443/ingestion/ for a local installation).

crud-demo
=========

The crud-demo example core-plugin technology demonstrates how to implement a simple CRUD (create, read, update, delete) app using an aggregation and ingestion service.

To install the demo, add the crud-demo folder to the openBIS core-plugins folder. Include "crud-demo" in the `enabled-technologies` property in the AS and DSS service.properties files. You will additionally need to create a symbolic link from the `datastore_server/lib` folder to `crud.jar` in the folder crud-demo/1/dss/data-sources/crud-db/crud.jar. E.g., in the DSS lib folder:

	ln -s ../../core-plugins/crud-demo/1/dss/data-sources/crud-db/crud.jar ./

After staring the servers, the UI will be available at the following URL:

	https://localhost:8443/crud-demo/

Data Sources
------------

The example includes one data source, `crud-db`. The data source specifies the parameters necessary to access the database and defines the structure of the database tables in the cruddb/sql folder. With the tables defined this way, the database can be evolved and automatically migrated. It is, however, necessary to provide a class that specifies the version of the database. This class is located in the crud.jar file. The source for the class is:

	package ch.ethz.cisd.cruddemo.db;

	import ch.systemsx.cisd.openbis.dss.generic.shared.IDatabaseVersionHolder;

	public class CrudDemoDbVersionHolder implements IDatabaseVersionHolder
	{
	    @Override
	    public String getDatabaseVersion()
	    {
	        return "001"; // changed in S139
	    }
	}


Services
--------

The crud-demo uses two services, the `crud-aggregation-service` and the `crud-ingestion-service`. The aggregation service is used to display the content of the table. The ingestion service is used to modify the content of the table. In both cases, the table is accessed directly via SQL.


Webapps
-------

There is one web app, `crud-demo` that implements the UI for displaying and modifying the content of the database. 

The webapp is available at the url: {server url}/crud-demo/ (e.g., https://localhost:8443/crud-demo/ for a local installation).
