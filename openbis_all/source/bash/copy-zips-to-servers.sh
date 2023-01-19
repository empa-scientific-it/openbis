#!/bin/bash
# Copies all zip files from the current directory to the home directory of the specified servers

export SPRINT=bs-openbis07.ethz.ch
export DEMO=cisd-openbis.ethz.ch
export AGRONOMICS=bs-agronomics01.ethz.ch
export DSU=openbis-dsu.bsse.ethz.ch
export SCU=openbis-scu.ethz.ch
export LIMB=bs-openbis03.ethz.ch
export CHIPDB=bs-openbis09.ethz.ch


# Different types of server specific zips we distinguish
export ZIPS="openBIS-server-S*.zip  datastore_server-S*.zip"
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-S*.zip *dsu*.zip *tracking*"

# Special plugin
export DATASTORE_PLUGIN="datastore_server_plugin-yeastx-*.zip"

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

echo -e "\nCopying to default dss...\n"
for l in $DATASTORE_PLUGIN; do
        echo $AGRONOMICS; scp -p $l $AGRONOMICS:~openbis/config
        echo $YEASTX; scp -p $l $YEASTX:~openbis/config
        echo $BASYSBIO; scp -p $l $BASYSBIO:~openbis/config
        echo $BASYSBIO_TEST; scp -p $l $BASYSBIO_TEST:~openbis/config
done
