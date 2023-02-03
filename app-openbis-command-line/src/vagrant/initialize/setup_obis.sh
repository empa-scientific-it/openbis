# Create the obis user
sudo useradd obis

# obis dependencies
sudo yum -y install gcc-c++.x86_64

sudo yum -y install https://centos7.iuscommunity.org/ius-release.rpm
sudo yum -y install python36u.x86_64
sudo yum -y install python36u-devel.x86_64
sudo yum -y install python36u-setuptools.noarch
sudo easy_install-3.6 pip
