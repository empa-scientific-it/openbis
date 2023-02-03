#!/bin/bash
# Moves the current installation to a backup folder. 

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

# check for inconsistence upgrade
INSTALLER_JAR=`ps aux|grep openBIS-installer.jar|grep -v grep|awk '{for(i=1;i<=NF;i++){if($i~/openBIS-installer\.jar/){print $i}}}'`
UPGRADE_VERSION=`java -cp $BASE InstallerVariableAccess $INSTALLER_JAR version.number`
UPGRADE_REVISION=`java -cp $BASE InstallerVariableAccess $INSTALLER_JAR revision.number`
BUILD_INFO=`cat $BASE/BUILD-installation.INFO`
VERSION_REVISION=${BUILD_INFO%*:*}
VERSION=${VERSION_REVISION%:*}
REVISION=${VERSION_REVISION#*:}
echo "existing version: $VERSION-r$REVISION"
echo "upgrade  version: $UPGRADE_VERSION-r$UPGRADE_REVISION (installer: $INSTALLER_JAR)"
ERROR_MSG="ERROR: Can not upgrade from version $VERSION-r$REVISION to version $UPGRADE_VERSION-r$UPGRADE_REVISION"
if [[ $VERSION != 'SNAPSHOT' && $UPGRADE_VERSION != 'SNAPSHOT' ]]; then # upgrade from non-snapshot -> non-snapshot
    if [[ ${VERSION:0:1} == 'S' && ${UPGRADE_VERSION:0:1} != 'S' ]]; then # upgrade from sprint -> release
        if [[ ${UPGRADE_VERSION:0:2} == '13' ]]; then
            FIRST_RELEASE_REVISION='28762'
        elif [[ ${UPGRADE_VERSION:0:2} == '16' ]]; then
            FIRST_RELEASE_REVISION='36515'
        elif [[ ${UPGRADE_VERSION:0:2} == '18' ]]; then
            FIRST_RELEASE_REVISION='1530174746'
        else
            FIRST_RELEASE_REVISION=$REVISION
        fi
        if [[ $REVISION -gt $FIRST_RELEASE_REVISION ]]; then
            echo $ERROR_MSG
            exit 1
        fi
    elif [[ ${VERSION:0:1} != 'S' && ${UPGRADE_VERSION:0:1} == 'S' ]]; then # upgrade from release -> sprint
        if [[ $REVISION -gt $UPGRADE_REVISION ]]; then
            echo $ERROR_MSG
            exit 1
        fi
    elif [[ $UPGRADE_VERSION < $VERSION ]]; then # upgrade from sprint -> sprint or release -> release
        echo $ERROR_MSG
        exit 1
    fi
fi

BACKUP_DIR=$1
if [ "$BACKUP_DIR" == "" ]; then
	echo ERROR: directory in which configuration should be stored has not been specified! 
	exit 1
fi
DATABASES_TO_BACKUP="$2"
CONSOLE=$3

$BASE/alldown.sh

ROOT_DIR=$BASE/../servers
CONFIG=$BACKUP_DIR/config-backup

echo "Creating backup folder $BACKUP_DIR ..."
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

$BASE/backup-databases.sh $BACKUP_DIR "$DATABASES_TO_BACKUP" $CONSOLE
if [ $? -ne "0" ]; then
  echo "Creating database backups had failed. Aborting ..."
  exit 1
fi

OLD_BIS=$BACKUP_DIR/openBIS-server

echo "Moving old installation to backup dir"

if [ -d $ROOT_DIR/openBIS-server ]; then
  echo "mv $ROOT_DIR/openBIS-server $OLD_BIS"
  mv $ROOT_DIR/openBIS-server $OLD_BIS
fi
 
echo "mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server"
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server

if [ -d $ROOT_DIR/big_data_link_server ]; then
  echo "mv $ROOT_DIR/big_data_link_server $BACKUP_DIR/big_data_link_server"
  mv $ROOT_DIR/big_data_link_server $BACKUP_DIR/big_data_link_server
fi

echo "cp -R $ROOT_DIR/core-plugins $BACKUP_DIR/core-plugins"
cp -R $ROOT_DIR/core-plugins $BACKUP_DIR/core-plugins
rm -rf $BACKUP_DIR/core-plugins/eln-lims/bin

if [ -d $BACKUP_DIR/datastore_server/data/sessionWorkspace ]; then
  echo "rm -r $BACKUP_DIR/datastore_server/data/sessionWorkspace"
  rm -r $BACKUP_DIR/datastore_server/data/sessionWorkspace
fi

echo "DONE"
