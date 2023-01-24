#!/bin/bash
# Copies all zip files from the current directory to the home directory of the specified servers

export SPRINT=bs-openbis07.ethz.ch
export DEMO=cisd-openbis.ethz.ch
export DSU=openbis-dsu.bsse.ethz.ch
export SCU=openbis-scu.ethz.ch
export LIMB=bs-openbis03.ethz.ch
export CHIPDB=bs-openbis09.ethz.ch


# Different types of server specific zips we distinguish
export ZIPS="openBIS-server-S*.zip  datastore_server-S*.zip"
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-S*.zip *dsu*.zip *tracking*"

echo -e "\nCopying default openBIS/DSS to servers...\n"
for i in $ZIPS; do
         echo $DEMO; scp -p $i $DEMO:~openbis
         echo $LIVERX; scp -p $i $LIVERX:~openbis
         echo $SCU; scp -p $i $SCU:~openbis
         echo $LIMB; scp -p $i $LIMB:~openbis
         echo $CHIPDB; scp -p $i $CHIPDB:~openbis
done

echo -e "\nCopying to $DSU...\n"
for k in $ZIPS_DSU; do
        echo $DSU; scp -p $k sbsuser@$DSU:~openbis
done