#
# Jetty Logging Module
#   Output Managed by Log4j 1.2.x
#

[depend]
resources

[lib]
lib/logging/*.jar

[files]
logs/
http://central.maven.org/maven2/org/slf4j/slf4j-api/1.6.6/slf4j-api-1.6.6.jar|lib/logging/slf4j-api-1.6.6.jar
http://central.maven.org/maven2/org/slf4j/slf4j-log4j12/1.6.6/slf4j-log4j12-1.6.6.jar|lib/logging/slf4j-log4j12-1.6.6.jar
http://central.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar|lib/logging/log4j-1.2.17.jar
https://raw.githubusercontent.com/jetty-project/logging-modules/master/log4j-1.2/log4j.properties|resources/log4j.properties
https://raw.githubusercontent.com/jetty-project/logging-modules/master/log4j-1.2/jetty-logging.properties|resources/jetty-logging.properties



