#!/bin/env bash

# Install prerequisites for openbis -- java and postgresql
# NB openbis must be downloaded an installed separately because it is not available anonymously

# Install Java
sudo yum -y install java-1.8.0-openjdk.x86_64

# Install postgresql
sudo yum -y install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo cp /vagrant/config/postgres/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf
sudo systemctl start postgresql
sudo systemctl enable postgresql


# Create the openbis user
sudo useradd openbis
sudo -u postgres createuser openbis

# Add an entry for this hostname to /etc/hosts -- otherwise java complains
echo "Add to /etc/hosts: 127.0.0.1 localhost `hostname`"
echo "Run the openbis installer to get openbis installed."
