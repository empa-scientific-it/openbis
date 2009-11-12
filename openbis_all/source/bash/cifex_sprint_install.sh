# This is a very simple version of the script which installs the cifex
# You have to change the INSTALL_URL, PREV and NEW variables by yourself!

PREV=cifex-SNAPSHOT-S68
NEW=cifex-S68
INSTALL_URL=http://cisd-ci.ethz.ch:8090/cruisecontrol/artifacts/cifex/20091111140651/cifex-SNAPSHOT-r13316.zip

test -d $PREV || echo Directory $PREV does not exist!
test -d $PREV || exit 1

unalias cp
unalias rm

./cifex/jetty/bin/shutdown.sh
mv cifex-* old/
rm -f cifex

mkdir $NEW
ln -s $NEW cifex
cd cifex
wget $INSTALL_URL


unzip cifex-*.zip
OLD_INSTALL=~/old/$PREV/jetty
cd cifex
cp $OLD_INSTALL/etc/service.properties .
cp $OLD_INSTALL/etc/keystore .
cp $OLD_INSTALL/etc/jetty.xml .
./install.sh ..
cd ../jetty
cp $OLD_INSTALL/etc/jetty.properties etc/
cp $OLD_INSTALL/etc/triggers.txt etc/
cp $OLD_INSTALL/dssTrigger.properties .

cd ..
rm -fr cifex
jetty/bin/startup.sh
cd ~/

Echo "Now restart Datastore if needed"