#!/bin/bash
# Upgrades openbis ELN-LIMS. 
# Asumes that the eln-lims tarball is on the 'servers' directory 
# Assumes that core-plugins are installed in the 'servers' directory on the same level as this script parent directory

##
## Upgrade script algorithm explained
##

#-- If current installation don't exists return with message, no eln-lims installation found, do a normal first installation

#-- If installation exists
#-- Check if current installation is a minimum master data installation
#-- Backup etc folder (skip if etc folder don't exist)
#-- Remove eln-lims folder
#-- Uncompress eln-lims tarball into the core-plugins folder
#-- If last installation was minimum master data installation, remove extra eln-lims master data script (skip if wasn't)
#-- Restore etc folder from backup (skip if backup don't exist)

echo "1 - Starting ELN-LIMS Upgrade"

# Define a timestamp function
timestamp() {
  date +"%y%m%d-%H%M"
}

# Directories used
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE=$DIR"/.."
BACKUP_BASE=$BASE"/backup"
BACKUP=$BACKUP_BASE"/$(timestamp)"
CORE_PLUGINS=$BASE"/servers/core-plugins/"
ELN_INSTALLATION=$BASE"/servers/core-plugins/eln-lims"
ELN_INSTALLATION_MINIMUM_MD_SCRIPT=$ELN_INSTALLATION"/2/as/initializemasterdataminimum.py"
ELN_INSTALLATION_STANDARD_MD_SCRIPT=$ELN_INSTALLATION"/2/as/initialize-master-data.py"
ELN_ETC_CONFIG_FOLDER=$ELN_INSTALLATION"/2/as/webapps/eln-lims/html/etc/"
ELN_TARBALL_LOCATION=$BASE"/servers/eln-lims-*.tar.gz";
ELN_TARBALL="$( ls $ELN_TARBALL_LOCATION )" #The script fails and exits automatically if the tarball is not found
echo "2 - The ELN-LIMS tarball found to be used is: "$ELN_TARBALL

if [ -d $ELN_INSTALLATION ]; then
	#Check if is a minimum master data installation
	if [ -f $ELN_INSTALLATION_MINIMUM_MD_SCRIPT ]; then
	    IS_MINIMUM_MASTER_DATA=false
	else
		IS_MINIMUM_MASTER_DATA=true
	fi
	echo "3 - Is minimum master data installation: "$IS_MINIMUM_MASTER_DATA
	
	#Backup ELN-LIMS etc folder
	if [ -d $ELN_ETC_CONFIG_FOLDER ]; then
		echo "4 - Backing up ELN-LIMS etc folder at: "$BACKUP
		#Creating backup folder and its parents if doesn't exist
		mkdir -p $BACKUP"/eln-lims/2/as/webapps/eln-lims/html/etc/"
		cp $ELN_ETC_CONFIG_FOLDER* $BACKUP"/eln-lims/2/as/webapps/eln-lims/html/etc/"
	else
		echo "4 - ELN-LIMS etc folder was not found, is this installation quite old?"
	fi
	
	#Remove ELN-LIMS folder
	echo "5 - Remove current ELN-LIMS installation"
	rm -rf $ELN_INSTALLATION
	
	#Uncompress eln-lims tarball into the eln-lims folder
	echo "6 - Uncompressing tarball"
	tar xfz $ELN_TARBALL -C $CORE_PLUGINS
	
	#if is minimum master data, remove the extra types script
	if [ IS_MINIMUM_MASTER_DATA ]; then
		echo "7 - Replacing master data script since minimum master data installation was found"
		mv $ELN_INSTALLATION_MINIMUM_MD_SCRIPT $ELN_INSTALLATION_STANDARD_MD_SCRIPT
	else
		echo "7 - Not modifying master data scripts since a standard master data installation was found"
	fi
	#Restore etc folder contents if they where backup
	if [ -d $BACKUP"/eln-lims/2/as/webapps/eln-lims/html/etc/" ]; then
		echo "8 - Restoring ELN-LIMS etc folder"
		cp $BACKUP"/eln-lims/2/as/webapps/eln-lims/html/etc/"* $ELN_ETC_CONFIG_FOLDER
	fi
else
	echo "3 - No ELN-LIMS installation found, at folder \""$ELN_INSTALLATION"\". Do first a normal installation, this script is only for upgrades."
fi
echo "9 - ELN-LIMS Upgrade Finished"