#!/bin/env bash

sudo pip3 install -e /vagrant_repo/pybis/src/python/
sudo pip3 install -e /vagrant_repo/obis/src/python/

sudo pip3 install pyOpenSSL

if [ ! -L /usr/bin/git ]; then

    pushd / $@ > /dev/null

    sudo wget https://downloads.kitenet.net/git-annex/linux/current/git-annex-standalone-amd64.tar.gz
    sudo tar -xvvf git-annex-standalone-amd64.tar.gz

    sudo ln -s /git-annex.linux/git /usr/bin/git
    sudo ln -s /git-annex.linux/git-annex /usr/bin/git-annex

    sudo chmod -R 777 /git-annex.linux
    sudo chmod -R 777 /usr/bin/git
    sudo chmod -R 777 /usr/bin/git-annex

    popd $@ > /dev/null

fi
