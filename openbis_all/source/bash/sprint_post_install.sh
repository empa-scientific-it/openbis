#!/bin/bash
# Central post install script for all openBIS servers

export BIN=~/bin
export SERVER=`uname -n`

export SPRINT=bs-openbis07.ethz.ch
export DEMO=bs-openbis08.ethz.ch
export LIVERX=openbis-liverx.ethz.ch
export DSU=bs-openbis04.ethz.ch
export SCU=bs-openbis05.ethz.ch
export LIMB=bs-openbis03.ethz.ch

function create_individual_greeting_message {
# Creates an individual greeting message
	if [ -f ~openbis/config/openbis_instance.txt ]; then
	   export OPENBIS_DICT=~openbis/sprint/openBIS-server/jetty/webapps/openbis/common-dictionary.js
	   export SERVER_INSTANCE_NAME=`cat ~openbis/config/openbis_instance.txt`
	   perl -pe 's/openbis_instance: "",/openbis_instance: "$ENV{SERVER_INSTANCE_NAME}",/' -i $OPENBIS_DICT
	fi
}

function restore_config_files {
  echo restoring config files...
  if [ -d ~openbis/config/images ]; then
	cp -r ~openbis/config/images ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi
  if [ -f ~openbis/config/loginHeader.html ]; then
	cp ~openbis/config/loginHeader.html ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi
  if [ -f ~openbis/config/help.html ]; then
	cp ~openbis/config/help.html ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi
  if [ -f ~openbis/config/index.html ]; then
  	cp ~openbis/config/index.html  ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi  
  if  [ -f ~openbis/config/openbis-as-jetty.xml ]; then
  	cp ~openbis/config/openbis-as-jetty.xml ~openbis/sprint/openBIS-server/jetty/etc/jetty.xml
  fi
  if  [ -f ~openbis/config/openbis_log.xml ]; then
  	cp ~openbis/config/openbis_log.xml ~openbis/sprint/openBIS-server/jetty/etc/log.xml
  fi
  if  [ -f ~openbis/config/datastore_log.xml ]; then
  	cp ~openbis/config/datastore_log.xml ~openbis/sprint/datastore_server/etc/log.xml
  fi  
  if  [ -f ~openbis/config/welcomePage.html ]; then
  	cp ~openbis/config/welcomePage.html ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi  
  if  [ -f ~openbis/config/welcomePageSimple.html ]; then
  	cp ~openbis/config/welcomePageSimple.html ~openbis/sprint/openBIS-server/jetty/webapps/openbis/
  fi  
  if  [ -f ~openbis/config/web-client.properties ]; then
  	cp ~openbis/config/web-client.properties ~openbis/sprint/openBIS-server/jetty/etc/
  fi  
  if  [ -f ~openbis/config/datastore_server.conf ]; then
  	cp ~openbis/config/datastore_server.conf ~openbis/sprint/datastore_server/etc/
  fi  
}

case "$SERVER" in

	$SPRINT)
	echo SPRINT:$SPRINT;
	restore_config_files
	create_individual_greeting_message
	
	$BIN/sprint_post_install_sprint.sh
	;;
	$DEMO)
	restore_config_files
	create_individual_greeting_message
	echo DEMO:$DEMO;
	$BIN/sprint_post_install_demo.sh
	;;
	$LIVERX)
	restore_config_files
	create_individual_greeting_message
	echo LIVERX:$LIVERX;
	;;
	$DSU)
	echo DSU:$DSU;
	restore_config_files
	create_individual_greeting_message
	DSU_SERVER_HOME=~openbis/sprint/openBIS-server/jetty/webapps/openbis
    	cp ~openbis/config/openBIS_for_DSU.pdf $DSU_SERVER_HOME
	;;
	$SCU)
	echo SCU:$SCU;
	restore_config_files
	create_individual_greeting_message
	;;
	$LIMB)
	echo LIMB:$LIMB;
	restore_config_files
	create_individual_greeting_message
	;;
	*)
	restore_config_files
  create_individual_greeting_message
	;;
esac
