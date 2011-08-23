#! /bin/bash
# 
# Creates a snapshot of an openBIS instance based on a configuration file.
# 
# usage: create-snapshot.sh <configuration file>
# 
# The configuration file has to have the following key-value pairs (stored as <key> = <value>):
# 
# repository			Path to the directory which will store the snapshot.
# servers					Path to to a directory which contains the folders 'datastore_server'
# 								and 'openBIS-server'. 
# store						Path to the directory which contains the data set store. Everything there
# 								will be added to the snapshot.
# databases				Space separated list of database to be dumped.
# index						Relative path to the lucene index. 
# 								It is relative to <servers path>/openBIS-server/jetty/. 
# 
# Important Notes: 
# - This script should be run after all servers have been stopped.
# - Can not be used for segmented stores because the script doesn't follow symbolic links. 
# 
function getValue {
    file=$1
    key=$2
    awk -F ' *= *' -v key=$2 '{map[$1] = $2} END {print map[key]}' $file
}

if [ $# -ne 1 ]; then
    echo "Usage: create-snapshot.sh <configuration file>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
CONFIGURATION_FILE="$1"

SERVERS_PATH=`getValue $CONFIGURATION_FILE servers`
if [ -z "$SERVERS_PATH" ]; then
    echo "Path to servers not specified in $CONFIGURATION_FILE."
    exit 1
fi
OPENBIS_AS_ROOT="$SERVERS_PATH/openBIS-server/jetty/"
if [ ! -d "$OPENBIS_AS_ROOT" ]; then
    echo "Error: $OPENBIS_AS_ROOT isn't a directory."
    echo "Most probable reason: $SERVERS_PATH doesn't point to a valid openBIS instance."
    exit 1
fi
REPOSITORY=`getValue $CONFIGURATION_FILE repository`
if [ -z "$REPOSITORY" ]; then
    echo "Repository not specified in $CONFIGURATION_FILE."
    exit 1
fi
mkdir -p "$REPOSITORY"
STORE=`getValue $CONFIGURATION_FILE store`
if [ -z "$STORE" ]; then
    echo "Store not specified in $CONFIGURATION_FILE."
    exit 1
fi
if [ ! -d "$STORE" ]; then
    echo "Store path $STORE doesn't point to an existing directory."
    exit 1
fi
DATABASES=`getValue $CONFIGURATION_FILE databases`
if [ -z "$DATABASES" ]; then
    echo "At least one database has to be specified in $CONFIGURATION_FILE."
    exit 1
fi
INDEX="$OPENBIS_AS_ROOT"`getValue $CONFIGURATION_FILE index`
if [ -z "$INDEX" ]; then
    echo "Index not specified in $CONFIGURATION_FILE."
fi
mkdir -p "$INDEX"
TIMESTAMP=`date +%Y-%m-%d_%H:%M:%S`
SNAPSHOT_FOLDER_NAME="openbis-snapshot-$TIMESTAMP"
SNAPSHOT="$REPOSITORY/$SNAPSHOT_FOLDER_NAME"

##################################################
#
# Creating snapshot
#
echo "==== Creating snapshot $SNAPSHOT.tgz"

mkdir "$SNAPSHOT"
cp -p "$CONFIGURATION_FILE" "$SNAPSHOT/snapshot.config"
############## dump the store ##############
for path in "$STORE"/*; do
    index_of_last_slash=`expr $path : '.*/'`
    file_name=${path:$index_of_last_slash}
    if [ `expr $file_name : '[0-9]*'` -ne 0 ]; then 
        if [ -h "$path" ]; then
            echo "Share $file_name is not dumped because it is a symbolic link."
        else
            echo "Start dumping share $file_name."
            parent_folder=${path:0:$index_of_last_slash}
            tar -rf "$SNAPSHOT/store.tar" -C "$parent_folder" $file_name 
            if [ $? -ne 0 ]; then
                echo "Error while dumping share $file_name. Snapshot creation aborted."
                exit 1
            fi
            echo "Share $file_name sucessfully dumped."
        fi
    fi
done
echo "Dump of store $STORE has been successfully created."
############## dump databases ##############
for db in $DATABASES; do
    pg_dump -U postgres -O $db > "$SNAPSHOT/$db.sql"
    if [ $? -ne 0 ]; then
        echo "Error dumping database '$db'. Snapshot creation aborted."
        exit 1
    fi
    echo "Database '$db' has been successfully dumped."
done
############## dump index ##############
tar -cf "$SNAPSHOT/index.tar" -C "$INDEX" .
if [ $? -ne 0 ]; then
    echo "Error creating index dump. Snapshot creation aborted."
    exit 1
fi
echo "Dump of index $INDEX has been successfully created."
############## packaging ##############
tar -zcf "$SNAPSHOT.tgz" -C "$REPOSITORY" "$SNAPSHOT_FOLDER_NAME" 
if [ $? -ne 0 ]; then
    echo "Error packaging snapshot $SNAPSHOT."
    exit 1
fi
rm -rf "$SNAPSHOT"

echo "==== $SNAPSHOT.tgz successfully created."
    



