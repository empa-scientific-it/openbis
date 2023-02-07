# openBIS search store

## Description

This plugin provides an API for storing searches in the form of V3 API
search criteria and fetch options. 

## Masterdata

The SEARCH_QUERY sample type is created to store the search criteria, 
fetch options, name and custom data. To avoid indexing, the search 
criteria, fetch options and custom data are stored as XML fields.

## Service

The service provices methods to save, update, load and delete the 
searches.
