#! /bin/bash
set -o errexit

usage() {
    echo "Usage: $0 <server folder> [<service properties file> <startup properties file>]"
    exit 1
}

# Checks whether the number of arguments is smaller than one.
# We at least need the server folder.
check_arguments() {
    if [ $# -lt 1 ]; then
        usage
    fi
}

check_arguments $@

# Installation folder: where the distribution zip file has been unzipped (and where this script resides)
installation_folder="`dirname $0`"
if [ ${installation_folder#/} == ${installation_folder} ]; then
    installation_folder="`pwd`/${installation_folder}"
fi
# Where the server will be installed.
server_folder=$1
shift

if [ ${server_folder#/} == ${server_folder} ]; then
    server_folder="`pwd`/${server_folder}"
fi

properties_file="${installation_folder}/service.properties"
logconf_file="${installation_folder}/log.xml"
checksum_file="${installation_folder}/configuration.MD5"
startup_properties_file="${installation_folder}/openbis.conf"
# Check whether given properties files exist and are regular file.
if [ "$1" ]; then
    if [ ! -f "$1" ]; then
        echo "Given properties file '$1' does not exist!"
        exit 1
    fi
    properties_file="$1"
    if [ "${properties_file#/}" == "${properties_file}" ]; then
        properties_file="`pwd`/${properties_file}"
    fi
    shift
fi
if [ "$1" ]; then
    if [ ! -f "$1" ]; then
        echo "Given startup properties file '$1' does not exist!"
        exit 1
    fi
    startup_properties_file="$1"
    if [ "${startup_properties_file#/}" == "${startup_properties_file}" ]; then
        startup_properties_file="`pwd`/${startup_properties_file}"
    fi
    shift
fi

jetty_with_version="jetty-distribution-`cat $installation_folder/jetty-version.txt`"
jetty_folder="${server_folder}/jetty-dist"
base_folder="${server_folder}/jetty"

# Creates server folder.
mkdir -p "$server_folder"

# Checks whether a jetty folder already exists.
if [ -d $jetty_folder ]; then
    echo "There exists already a Jetty folder."
    echo "Please shutdown and remove this Jetty installation"
    echo "or choose another server folder."
    exit 1
fi

echo Unzipping Jetty...
# Files are unzipped in $jetty_with_version
unzip -q "$installation_folder/jetty.zip" -d "$server_folder"
mv "$server_folder/${jetty_with_version}" "$jetty_folder"
mv "$installation_folder/base" "$base_folder"


JETTY_BIN_DIR="$base_folder/bin"
mkdir -p "$JETTY_BIN_DIR"
JETTY_ETC_DIR="$base_folder/etc"
mkdir -p "$JETTY_ETC_DIR"

test -f "$installation_folder/web-client.properties" && cp -p "$installation_folder/web-client.properties" "$JETTY_ETC_DIR"
test -f "$JETTY_ETC_DIR/keystore" && rm "$JETTY_ETC_DIR/keystore"
cp -p "$installation_folder/openBIS.keystore" "$JETTY_ETC_DIR"
cp -p $startup_properties_file "$JETTY_ETC_DIR"


echo installing web archive...
openbis_webapp="$base_folder/webapps/openbis"
mkdir "$openbis_webapp"
unzip -q "$installation_folder/openBIS.war" -d "$openbis_webapp"
war_classes="$openbis_webapp/WEB-INF/classes"
mkdir -p "$war_classes/etc"
# Replace 'service.properties' and 'log.xml' files 
echo "Replace service.properties by following file (if it exists): $properties_file"
test -f "$properties_file" && cp -p "$properties_file" "$war_classes/"
echo "Replace log.xml by following file (if it exists): " $logconf_file
test -f "$logconf_file" && cp -p "$logconf_file" "$war_classes/etc/"
echo "Make the configuration checksum file available : " $checksum_file
test -f "$checksum_file" && cp -p "$checksum_file" "$base_folder"

# unzip native libraries
LIB_FOLDER="$openbis_webapp/WEB-INF/lib"
rm -rf $LIB_FOLDER/native
unzip -q $LIB_FOLDER/sis-base-*.jar -d $LIB_FOLDER native/*
unzip -q $LIB_FOLDER/sis-jhdf5-*.jar -d $LIB_FOLDER native/*

echo installing core-plugins
if [ -f "$server_folder/../core-plugins/core-plugins.properties" ]; then
    excludingStuff="-x core-plugins/core-plugins.properties" 
fi
# Before unzipping all file/folders which are symbolic links in the zip file they have to be removed in the target.
# Otherwise  a 'cannot delete old ...' error happens if the symbolic link was a file/folder. 
zipinfo "$installation_folder/core-plugins.zip" |egrep "^l"|awk '{$1=$2=$3=$4=$5=$6=$7=$8=""; print $0}'|while read file;do 
    rm -rf "$server_folder/../$file"
done
unzip -quo "$installation_folder/core-plugins.zip" $excludingStuff -d "$server_folder/.."

# Move config files to etc and create symlinks.
mv "$war_classes/service.properties" "$JETTY_ETC_DIR"
cd "$war_classes"
ln -s ../../../../etc/service.properties .
cd -

cp -p "$installation_folder/autosymlink.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/startup.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/shutdown.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/status.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/version.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/setup-env" "$JETTY_BIN_DIR"
cp -p "$installation_folder/passwd.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/check.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/watchdog.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/register-master-data.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/export-master-data.sh" "$JETTY_BIN_DIR"
cp -p "$installation_folder/export-master-data.py" "$JETTY_BIN_DIR"
cp -p "$installation_folder/configure.sh" "$JETTY_BIN_DIR"
ln -s passwd.sh "$JETTY_BIN_DIR/passwd_cache.sh"
chmod u+x $JETTY_BIN_DIR/*.sh

# Create a file called 'jetty.properties'.
JETTY_PROPERTIES="$JETTY_ETC_DIR/jetty.properties"
echo "JETTY_STOP_PORT=8079" > "$JETTY_PROPERTIES"
echo "JETTY_STOP_KEY=secret" >> "$JETTY_PROPERTIES"

mkdir -p "$base_folder/work"

cd "$base_folder"

echo openBIS Application Server installation finished
echo Starting server by jetty/bin/startup.sh
echo Stopping server by jetty/bin/shutdown.sh
