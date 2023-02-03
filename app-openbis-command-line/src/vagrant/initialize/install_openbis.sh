#!/bin/env bash

if [ ! -L /openbis_installed ]; then

    pushd . $@ > /dev/null

    ob_dir=$(find /vagrant/initialize/ -maxdepth 1 -type d -name openBIS-installation-standard-technologies-* | head -n1)
    echo "Installing openBIS from $ob_dir"
    cp /vagrant/config/openbis/console.properties $ob_dir
    chmod a+rw "$ob_dir/console.properties"

    sudo -u openbis cd ~openbis/
    sudo su openbis -c "export ADMIN_PASSWORD=admin && export ETLSERVER_PASSWORD=etlserver && $ob_dir/run-console.sh"

    sudo su openbis -c "sed -i '/host-address = /c\host-address = https://obisserver' /home/openbis/servers/datastore_server/etc/service.properties"

    sudo su openbis -c "~/servers/openBIS-server/jetty/bin/passwd.sh add obis -p obis"

    sudo touch /openbis_installed
    sudo chmod 777 /openbis_installed

    popd $@ > /dev/null

fi
