#!/bin/bash
# This is a very simple version of the script which installs the cifex

PREV=cifex-S72
NEW=cifex-$1
OLD_INSTALL=~/old/$PREV/jetty

test -d $PREV || echo Directory $PREV does not exist!
test -d $PREV || exit 1

alias cp='cp'
alias rm='rm'

./$PREV/jetty/bin/shutdown.sh
mv $PREV old/
rm -f cifex

mkdir $NEW
ln -s $NEW cifex
cd cifex

mv ../cifex*.zip .
unzip cifex*.zip
cd cifex
cp $OLD_INSTALL/work/webapp/WEB-INF/classes/service.properties ~/cifex/jetty/work/webapp/WEB-INF/classes/service.properties
cp $OLD_INSTALL/etc/keystore .
cp $OLD_INSTALL/etc/jetty.xml .
./install.sh ..
cd ../jetty
cp $OLD_INSTALL/bin/jetty.properties ~/cifex/jetty/bin/jetty.properties
cp $OLD_INSTALL/etc/triggers.txt ~/cifex/jetty/etc/triggers.txt
cp $OLD_INSTALL/dssTrigger.properties ~/cifex/jetty/

cd ..
rm -fr cifex
jetty/bin/startup.sh
cd ~/

echo "Now restart Datastore if needed"