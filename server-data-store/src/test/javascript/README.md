## Simple Javascript client for new DSS API

This client allows to perform simple operations on a server-data-store using the new api.

In order to use it:

1. Acquire datastore address - you can check .properties file for httpServerPort and httpServerUri
   Note: make sure address/port/uri match!
   (you may deploy dev instance by running AFSServerDevelopmentEnvironmentStart gradle task)

2. Open server-data-store-client.html in your browser
   Note: You mey need to disable security flags in your browser in case of self-signed certificates.

