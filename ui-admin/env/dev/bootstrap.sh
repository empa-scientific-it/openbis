#!/usr/bin/env bash

sudo locale-gen en_US.UTF-8

echo "
LC_ALL=en_US.UTF-8
LANG=en_US.UTF-8
" >> /etc/environment

apt update
apt install -y unzip elinks inotify-tools git openjdk-8-jdk

sudo -E -u vagrant -H -i /files/setup-vagrant.sh
