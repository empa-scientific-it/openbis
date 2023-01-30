#!/bin/bash
# Upgrades openbis AS and DSS. 
# Assumes that openbis is installed in the 'servers' directory on the same level as this script parent directory


#
# Upgrades the installation from two separate ZIP files 
# (AS, DSS) dropped into the "servers" directory. 
#
upgrade_from_zips() 
{

NOW=`date +%y%m%d-%H%M`
BACKUP_DIR=$BASE/../backup/$NOW
CONFIG=$BACKUP_DIR/config-backup
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

OLD_BIS=$BACKUP_DIR/openBIS-server

echo "Copying old installation to backup dir"
echo "mv $ROOT_DIR/openBIS-server $OLD_BIS"
echo "mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server"

mv $ROOT_DIR/openBIS-server $OLD_BIS 
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server

# Note: to restore the database afterwards one can use:
#    pg_restore -d db-name db-file.dmp 
$PG_DUMP -U $DB_USER_NAME -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB-${NOW}.dmp

installOpenBisServer $ROOT_DIR
installDataStoreServer $ROOT_DIR

$BASE/restore-config-from-backup.sh $CONFIG
cp -r $OLD_BIS/jetty/indices* $ROOT_DIR/openBIS-server/jetty/

mv $ROOT_DIR/*.zip $BACKUP_DIR/

}

#
# Upgrades the installation from an installer tarball
#
upgrade_from_installer_tarball() 
{

OPENBIS_INSTALL_DIR=$BASE/..
TARBALL=$1
INSTALLER_DIR=${TARBALL%.tar.gz}

mkdir -p $OPENBIS_INSTALL_DIR/backup

pushd $ROOT_DIR > /dev/null

echo "Extracting installation tarball $TARBALL.."
tar xzf $TARBALL

popd > /dev/null

# set INSTALL_PATH in the console.properties
sed_params='s#^INSTALL_PATH=.*$#INSTALL_PATH='$OPENBIS_INSTALL_DIR'#'
sed -i '' "$sed_params" $INSTALLER_DIR/console.properties

# run the installation
$INSTALLER_DIR/run-console.sh
rm -rf $INSTALLER_DIR

BACKUP_DIR_NAME=`ls -rt $OPENBIS_INSTALL_DIR/backup | tail -1`
BACKUP_DIR=$OPENBIS_INSTALL_DIR/backup/$BACKUP_DIR_NAME

echo "Moving $TARBALL to $BACKUP_DIR..."
mv $TARBALL $BACKUP_DIR

}


BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env
source $BASE/common-functions.sh

ROOT_DIR=$BASE/../servers

$BASE/alldown.sh

installer_tarball=""
for installer_tarball in $ROOT_DIR/*.tar.gz; do
  : # only use the loop to assign the correct value of the installer_tarball
done

if [ -f "$installer_tarball" ]; then
  upgrade_from_installer_tarball $installer_tarball
else 
  upgrade_from_zips
fi


$BASE/allup.sh