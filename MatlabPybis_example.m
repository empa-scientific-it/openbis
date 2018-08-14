% demo script for accessing openBIS from Matlab using Pybis
% we use Matlab's capability to call Python libraries

%% first check the Python version
pyversion
% a different Python version can be selected by specifying the Python
% executable, e.g. pyversion /Users/Henry/miniconda3/bin/python

%% enter username and password for openBIS
username = 'hluetcke';
pw = passwordEntryDialog('CheckPasswordLength',0);

%% connect to openBIS
obi = py.pybis.Openbis('https://limb.ethz.ch/openbis:8443', pyargs('verify_certificates', 0));
obi.login(username, pw, pyargs('save_token', 1));
clear pw
obi

%% select datasets
datasets = obi.get_datasets(pyargs('type','HISTOLOGY'));
datasets

%% download dataset
% get specific dataset
ds = obi.get_dataset('20101105142049776-6512');
% download
data_dir = '/Users/Henry/Data/Projects/MatlabOpenbis/data';
ds.download(pyargs('destination', data_dir, 'wait_until_finished', false))


%% clean up
% logout
obi.logout()
