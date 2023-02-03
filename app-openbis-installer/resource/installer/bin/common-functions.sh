POSTGRES_BIN=`cat $BASE/postgres_bin_path.txt`

#
# Removes white trailing and leading white spaces.
#
# This function should be used as follows:
#
# trimmedString=$(trim $someString)
#
trim()
{
  echo "$1" | sed 's/^ *//g' | sed 's/ *$//g'
}

#
# Checks whether the first argument, a comma-separated list of items contains the second argument
# as an item. Returns TRUE if this is the case. Trailing and leading
# whitespace of list items are ignored. 
#
# This function should be used as follows:
#
# result=$(contains " abc,  def , ghi " "abc")
#
contains()
{
  local list="$1"
  local item="$2"
  while true; do
    local part="${list%%,*}"
    local firstItem=$(trim $part)
    list="${list#*,}"
    if [ "$firstItem" == "$item" ]; then
      echo "TRUE"
      return
    fi
    if [ "$part" == "$list" ]; then
      echo "FALSE"
      return
    fi
  done
}

#
# Returns TRUE if the specified database exists. Password argument is optional
#
# This function should be used as follows:
#
# if [ $(databaseExist localhost 5432 "openbis_prod" $owner $password) == "TRUE" ]; then doBackup; fi
#
databaseExist()
{
  local host=$1
  local port=$2
  local database=$3
  local owner=$4
  pgpw=""
  if [ $# -eq 5 ]; then
    pgpw="PGPASSWORD=$5"
  fi
  if [ `exe_psql $pgpw -w -U $owner -h $host -p $port -l | eval "awk '/$database /'" | wc -l` -gt 0 ]; then
    echo "TRUE"
  else
    echo "FALSE"
  fi
}

#
# Run psql command using POSTGRES_BIN path
#
exe_psql()
{
  executable="$POSTGRES_BIN/psql"
  if [ ! -x "$executable" ]; then
    executable=psql
  fi
  execute "$executable" "$@"
}

#
# Run pg_dump command using POSTGRES_BIN path
#
exe_pg_dump()
{
  executable="$POSTGRES_BIN/pg_dump"
  if [ ! -x "$executable" ]; then
    executable=pg_dump
  fi
  execute "$executable" "$@"
}

execute()
{
  executable=$1
  shift
  if [ "${1%=*}" == "PGPASSWORD" ]; then
    export $1
    shift
  fi
  echo "$executable" "$@"
  "$executable" "$@"
  unset PGPASSWORD
}

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
# overwrites a folder (second parameter) with first parameter
#
copyFolderIfExists() 
{
  if [ -e "$1" ]; then
      rm -r "$2"
      cp -R "$1" "$2"
  fi
}

POSTGRES_BIN=`cat $BASE/postgres_bin_path.txt`

#
# Removes white trailing and leading white spaces.
#
# This function should be used as follows:
#
# trimmedString=$(trim $someString)
#
trim()
{
  echo "$1" | sed 's/^ *//g' | sed 's/ *$//g'
}

#
# Checks whether the first argument, a comma-separated list of items contains the second argument
# as an item. Returns TRUE if this is the case. Trailing and leading
# whitespace of list items are ignored. 
#
# This function should be used as follows:
#
# result=$(contains " abc,  def , ghi " "abc")
#
contains()
{
  local list="$1"
  local item="$2"
  while true; do
    local part="${list%%,*}"
    local firstItem=$(trim $part)
    list="${list#*,}"
    if [ "$firstItem" == "$item" ]; then
      echo "TRUE"
      return
    fi
    if [ "$part" == "$list" ]; then
      echo "FALSE"
      return
    fi
  done
}

#
# Returns TRUE if the specified database exists. Password argument is optional
#
# This function should be used as follows:
#
# if [ $(databaseExist localhost 5432 "openbis_prod" $owner $password) == "TRUE" ]; then doBackup; fi
#
databaseExist()
{
  local host=$1
  local port=$2
  local database=$3
  local owner=$4
  pgpw=""
  if [ $# -eq 5 ]; then
    pgpw="PGPASSWORD=$5"
  fi
  if [ `exe_psql $pgpw -w -U $owner -h $host -p $port -l | eval "awk '/$database /'" | wc -l` -gt 0 ]; then
    echo "TRUE"
  else
    echo "FALSE"
  fi
}

#
# Run psql command using POSTGRES_BIN path
#
exe_psql()
{
  executable="$POSTGRES_BIN/psql"
  if [ ! -x "$executable" ]; then
    executable=psql
  fi
  execute "$executable" "$@"
}

#
# Run pg_dump command using POSTGRES_BIN path
#
exe_pg_dump()
{
  executable="$POSTGRES_BIN/pg_dump"
  if [ ! -x "$executable" ]; then
    executable=pg_dump
  fi
  execute "$executable" "$@"
}

execute()
{
  executable=$1
  shift
  if [ "${1%=*}" == "PGPASSWORD" ]; then
    export $1
    shift
  fi
  echo "$executable" "$@"
  "$executable" "$@"
  unset PGPASSWORD
}

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
# Restore configuration file
#
restore()
{
    local source_file=$1
    local destination=$2
    local file=$3
    if [ -e "$source_file" ]; then
        if [ -e "$destination/$file" ]; then
            cp -p "$destination/$file" "$destination/$file.$version"
        fi
        echo "cp -p $source_file $destination/$file"
        cp -p "$source_file" "$destination/$file"
    fi
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
# overwrites a folder (second parameter) with first parameter
#
copyFolderIfExists() 
{
  if [ -e "$1" ]; then
      rm -r "$2"
      cp -R "$1" "$2"
  fi
}

#
# copy configurations
#
copyConfig()
{
  root="$1"
  pathPattern="$2"
  destination="$3"
  for f in `find $root | grep "$pathPattern"`; do
    d="$destination/${f#$root}"
    rm -rf "$d"
    d="${d%/*}"
    mkdir -p "$d"
    copyIfExists $f "$d"
  done
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
