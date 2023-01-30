#
# Default openBIS operations like creating a backup or upgrading to a newer version often
# need to be customized by a specific project (e.g. screening). This function provides 
# infrastructure for such functionality. 
#
# The original script (e.g. upgrade.sh) includes a call specifying a filemask e.g.
#
# executeScriptHooks "Executing upgrade post-hook " "$ROOT/servers/*/bin/post-install-*.sh"
#
# Parameters
# $1 - an appropriate log message to identify the nature of the scripts being executed
# $2 - a file mask specifying the scripts to be executed (see the example above)
executeScriptHooks()
{
  if [ "$1" == "" ]; then 
    echo "ERROR: You must specify a log message when calling 'executeScriptHooks'"
    exit 1
  fi
  if [ "$2" == "" ]; then 
    echo "ERROR: You must specify a file mask for scripts to be executed"
    exit 1
  fi
  
  logmessage=$1
  filemask=$2
  
  for hook in $filemask; do
      echo "$logmessage $hook"
      bash $hook
  done
}

#
# Copies a file (first parameter) to a destination (second parameter). 
# Does nothing if file does not exist. Will follow symbolic links.
#
copyFileIfExists() 
{
  if [ -e "$1" ]; then
      cp -p "$1" "$2"
  fi
}

#
# Copies a file/folder (first parameter) to a destination (second parameter). 
# Does nothing if file/folder does not exist.
#
copyIfExists() 
{
  if [ -e "$1" ]; then
      cp -R "$1" "$2"
  fi
}

#
# Installs openBIS server to a given destination
# The function assumes that the openBIS-server*.zip file is already present in the destination.  
#
installOpenBisServer() 
{
  if [ "$1" == "" ]; then 
    echo "ERROR: You must specify a folder to install openBIS-server"
    exit 1
  fi
  
  INSTALL_DIR=$1
	TMP_EXTRACT=$INSTALL_DIR/tmp-extract
	echo Installing openBIS Application Server to $INSTALL_DIR
	
	mkdir -p "$TMP_EXTRACT"
	mkdir $INSTALL_DIR/openBIS-server
	unzip $INSTALL_DIR/openBIS-*.zip -d "$TMP_EXTRACT"
	$TMP_EXTRACT/openBIS-server/install.sh $INSTALL_DIR/openBIS-server
	
	rm -rf "$TMP_EXTRACT"
}

#
# Installs Data Store Server to a given destination
# The function assumes that the datastore-server*.zip file is already present in the destination.  
#
installDataStoreServer() 
{
  if [ "$1" == "" ]; then 
    echo "ERROR: You must specify a folder to install Data Store Server"
    exit 1
  fi
  
  INSTALL_DIR=$1
	echo Installing openBIS Datastore Server to $INSTALL_DIR
	unzip $INSTALL_DIR/datastore*.zip -d $INSTALL_DIR
}
