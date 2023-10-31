Starting and Stopping the openBIS Application Server and Data Store Server
==========================================================================

## Start Server

The openBIS application server is started as follows:

```console
prompt> <installation folder>/bin/bisup.sh
```

On startup the openBIS server creates the openBIS database (named `openbis_prod` by default) and checks the connection with the remote authentication services (if they are configured). Log files can be found in `<installation folder>/servers/openBIS-server/jetty/logs`. Also the following command shows the log: `<installation folder>/bin/bislog.sh`

```{warning}
Unless otherwise configured through running the installation script or within the database itself, the first user logged in into the system will have full administrator rights (role `INSTANCE_ADMIN`).
```

Commonly, the application server is configured to access a local data store via the data store server. This has to be started after the AS:

```console
prompt> <installation folder>/bin/dssup.sh
```

The application server and the data store server can also be started one after the other using a single command:
```console
prompt> <installation folder>/bin/allup.sh
```


## Stop Server

The server is stopped as follows:

```console
prompt> <installation folder>/bin/bisdown.sh
```

To only stop the data store server:
```console
prompt> <installation folder>/bin/dssdown.sh
```

To stop both the data store server and then the applicaiton server:
```console
prompt> <installation folder>/bin/alldown.sh
```