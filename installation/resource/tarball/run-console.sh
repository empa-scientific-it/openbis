#!/bin/bash

#Detect Java version

if type -p java; then
    _java=java
else
    echo "Java not available"
	exit -1
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}'| cut -c1-3)
    if [[ "$version" == "1.8" || "$version" == "11." ]]; then
        echo Java version $version found.
    else
        echo Java version $version found is neither 1.8 or 11.
        exit -1
    fi
fi

#Continue installation

#	
# reads the 'admin' user password from the console
#
function readAdminPassword()
{
	if [ -z "$ADMIN_PASSWORD" ]; then
	    read -s -p "Enter password for openBIS 'admin' user : " ADMIN_PASSWORD
	    echo ""
	    
	    read -s -p "Re-type password for openBIS 'admin' user : " ADMIN_PASSWORD2
	    echo ""
	    
	    if [ "$ADMIN_PASSWORD" != "$ADMIN_PASSWORD2" ]; then
	        echo "Administrator passwords do not match. Aborting installation."
	        exit 2
	    fi
	fi
}

#
# enforces the availability of a certain command on the system path
#
function ensureToolOnPath() 
{
  cmd=$1
  cmdLocation=$(which $cmd)
  
  if [ -z "$cmdLocation" ]; then
  
     echo "The installation process requires '$cmd' to be on the system path."
     echo "Please set the PATH variable accordingly and try again."
     exit 3
  
  fi
}


BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ensureToolOnPath "java" 

install_path=$( grep -e "^INSTALL_PATH=.*$" $BASE/console.properties | sed "s/INSTALL_PATH=//" )
if [ -z "$install_path" ]; then
    echo "The property INSTALL_PATH must be configured in $BASE/console.properties."
    echo "Please edit the file and run the installation script again."
    exit 1
fi
	
if [[ -d "$install_path/bin" && -d "$install_path/servers/core-plugins" && -d "$install_path/servers/datastore_server" && "$install_path/servers/openBIS-server" ]]; then
  echo "Previous openBIS installation detected. Upgrading..."
else 
  dss_root_dir=$( grep -e "^DSS_ROOT_DIR=.*$" $BASE/console.properties | sed "s/DSS_ROOT_DIR=//" )
  if [ -z "$dss_root_dir" ]; then
      echo "The property DSS_ROOT_DIR must be configured in $BASE/console.properties."
      echo "Please edit the file and run the installation script again."
      exit 1
  fi
  readAdminPassword
fi

java -Djava.util.logging.config.file=$BASE/jul.config -DADMIN_PASSWORD=$ADMIN_PASSWORD -DCONSOLE=true -Dmerge.props.to.installation.vars=true -cp $BASE/openBIS-installer.jar com.izforge.izpack.installer.bootstrap.Installer -options-auto $BASE/console.properties
