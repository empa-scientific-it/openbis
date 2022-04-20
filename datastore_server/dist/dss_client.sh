LIB=lib/
java --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED -jar $LIB/dss_client.jar $*
