#!/bin/bash
# Scripts which determines if the template configuration of the DSS and openBIS servers 
# is the same as the current one.
# Returns 0 if configuration is the one stored in the template file, 1 otherwise.

is_update_needed="false"
CONFIG_DIR=~/config

function compare_configs {
        local config_template=$1
        local config_current=$2

        #echo Comparing template config file $config_template with current config file $config_current
        diff $config_template $config_current > .dss_changed
        if [ "`cat .dss_changed`" != "" ]; then
           echo "Current configuration is not the same as the one stored in the template file. There are following differences:"
           cat .dss_changed
                 echo 
           echo "DIFF: Please update the template using the command:"
           echo "  cp $config_current $config_template"
           is_update_needed="true"
        else
           #echo "OK: Configuration file $config_template is up to date."
           echo
        fi
}

compare_configs $CONFIG_DIR/datastore_server-service.properties ~/sprint/datastore_server/etc/service.properties
TOMCAT_DIR=~/sprint/openBIS-server/apache-tomcat
compare_configs $CONFIG_DIR/service.properties $TOMCAT_DIR/webapps/openbis/WEB-INF/classes/service.properties
compare_configs $CONFIG_DIR/openbis.conf $TOMCAT_DIR/etc/openbis.conf
compare_configs $CONFIG_DIR/images/ $TOMCAT_DIR/webapps/openbis/images/
compare_configs $CONFIG_DIR/loginHeader.html $TOMCAT_DIR/webapps/openbis/loginHeader.html
compare_configs $CONFIG_DIR/help.html $TOMCAT_DIR/webapps/openbis/help.html


rm  .dss_changed

if [ "$is_update_needed" = "true" ]; then

  exit 1
else
  exit 0
fi