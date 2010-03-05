#!/bin/sh
# Central post install script for all openBIS servers

export BIN=~/bin
export SERVER=`uname -n`

export SPRINT=cisd-bamus
export DEMO=cisd-tongariro
export YEASTX=imsb-us-openbis.ethz.ch
export PHOSPHONETX=openbis-phosphonetx.ethz.ch
export LIVERX=openbis-liverx.ethz.ch
export AGRONOMICS=bs-dsvr11.ethz.ch
export DSU=openbis-dsu
export SCU=openbis-scu
export BASYSBIO=bs-dsvr10.ethz.ch
export BASYSBIO_TEST=openbis-test

function create_individual_greeting_message {
# Creates an individual greeting message
	if [ -f ~/config/openbis_instance.txt ]; then
	   export OPENBIS_DICT=~openbis/sprint/openBIS-server/apache-tomcat/webapps/openbis/common-dictionary.js
	   export SERVER_INSTANCE_NAME=`cat ~/config/openbis_instance.txt`
	   perl -pe 's/openbis_instance: "",/openbis_instance: "$ENV{SERVER_INSTANCE_NAME}",/' -i $OPENBIS_DICT
	fi
}

function restore_loginHeader {
  if [ -f ~/config/loginHeader.html ]; then
    echo restoring loginHeader.html...
    cp -r ~/config/images ~/sprint/openBIS-server/apache-tomcat/webapps/openbis/
    cp ~/config/loginHeader.html ~/sprint/openBIS-server/apache-tomcat/webapps/openbis/
    cp ~/config/help.html ~/sprint/openBIS-server/apache-tomcat/webapps/openbis/
  fi
}

function add_yeastx_plugin {
	 echo copying yeastx...
	 unzip ~openbis/config/datastore_server_plugin*.zip
	 mv ~openbis/config/lib/datastore_server-plugins.jar ~openbis/sprint/datastore_server/lib
	 rmdir ~openbis/config/lib
	 rm ~/config/datastore_server_plugin*.zip
}

case "$SERVER" in

	$SPRINT)
	echo SPRINT:$SPRINT;
	restore_loginHeader
	create_individual_greeting_message
	
	$BIN/sprint_post_install_sprint.sh
	;;
	$DEMO)
	restore_loginHeader
	create_individual_greeting_message
	echo DEMO:$DEMO;
	$BIN/sprint_post_install_demo.sh
	;;
	$YEASTX)
	restore_loginHeader
	create_individual_greeting_message
	echo YEASTX:$YEASTX;
	$BIN/sprint_post_install_yeastx.sh
	;;
	$PHOSPHONETX)
	restore_loginHeader
	create_individual_greeting_message
	echo PHOSPHONETX:$PHOSPHONETX;
	;;
	$LIVERX)
	restore_loginHeader
	create_individual_greeting_message
	echo LIVERX:$LIVERX;
	;;
	$AGRONOMICS)
	echo AGRONOMICS:$AGRONOMICS;
	restore_loginHeader
	create_individual_greeting_message
	add_yeastx_plugin
	$BIN/sprint_post_install_agronomics.sh
	;;
	$DSU)
	echo DSU:$DSU;
	restore_loginHeader
	create_individual_greeting_message
	DSU_SERVER_HOME=~openbis/sprint/openBIS-server/apache-tomcat-5.5.26/webapps/openbis
    cp ~/config/openBIS_for_DSU.pdf $DSU_SERVER_HOME
	;;
	$SCU)
	echo SCU:$SCU;
	restore_loginHeader
	create_individual_greeting_message
	;;
	$BASYSBIO)
	echo BASYSBIO:$BASYSBIO;
	restore_loginHeader
	create_individual_greeting_message
	add_yeastx_plugin
	$BIN/sprint_post_install_basysbio.sh
	;;
	$BASYSBIO_TEST)
	echo BASYSBIO_TEST:$BASYSBIO_TEST;
	restore_loginHeader
	create_individual_greeting_message
	add_yeastx_plugin
	$BIN/sprint_post_install_basysbio.sh
	;;
	*)
	echo Wrong Server! $SERVER is not in the list of openBIS Servers.;
	exit 1;
	;;
esac
