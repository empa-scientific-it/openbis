openBIS Logging
===============

## Runtime changes to logging

The script  `$INSTALL_PATH/servers/openBIS-server/jetty/bin/configure.sh `can be used to change the logging behavior of openBIS application server while the server is running.

The script is used like this: configure.sh \[command\] \[argument\]

The table below describes the possible commands and their arguments.

| Command                              | Argument(s)                                            | Default Value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|--------------------------------------|--------------------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| log-service-calls                    | 'on', 'off'                                            | 'off'         | Turns on / off detailed service call logging.
When this feature is enabled, openBIS will log about start and end of every service call it executes to file $INSTALL_PATH/servers/openBIS-server/jetty/log/openbis_service_calls.txt                                                                                                                                                                                                                                 |
| log-long-running-invocations         | 'on', 'off'                                            | 'on'          | Turns on / off logging of long running invocations.
When this feature is enabled, openBIS will periodically create a report of all service calls that have been in execution more than 15 seconds to file $INSTALL_PATH/servers/openBIS-server/jetty/log/openbis_long_running_threads.txt.                                                                                                                                                                         |
| debug-db-connections                 | 'on', 'off'                                            | 'off'         | Turns on / off logging about database connection pool activity.
When this feature is enabled, information about every borrow and return to database connection pool is logged to openBIS main log in file $INSTALL_PATH/servers/openBIS-server/jetty/log/openbis_log.txt                                                                                                                                                                                            |
| log-db-connections                   | no argument / minimum connection age (in milliseconds) | 5000          | When this command is executed without an argument, information about every database connection that has been borrowed from the connection pool is written into openBIS main log in file $INSTALL_PATH/servers/openBIS-server/jetty/log/openbis_log.txt
If the "minimum connection age" argument is specified, only connections that have been out of the pool longer than the specified time are logged. The minimum connection age value is given in milliseconds. |
| record-stacktrace-db-connections     | 'on', 'off'                                            | 'off'         | Turns on / off logging of stacktraces.
When this feature is enabled AND debug-db-connections is enabled, the full stack trace of the borrowing thread will be recorded with the connection pool activity logs.                                                                                                                                                                                                                                                                      |
| log-db-connections-separate-log-file | 'on', 'off'                                            | 'off'         | Turns on / off database connection pool logging to separate file.
When this feature is disabled, the database connection pool activity logging is done only to openBIS main log. When this feature is enabled, the activity logging is done ALSO to file $INSTALL_PATH/servers/openBIS-server/jetty/log/openbis_db_connections.txt.                                                                                                                                |
 