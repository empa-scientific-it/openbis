% demo script for accessing openBIS from Matlab using Pybis
% we use Matlab's capability to call Python libraries
% Pybis documentation is available here:
% https://sissource.ethz.ch/sispub/pybis/blob/master/src/python/PyBis/README.md


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
ds_id = '20101105142049776-6512';
ds = obi.get_dataset(ds_id);
% download
data_dir = '/Users/Henry/Data/Projects/MatlabOpenbis/data';
ds.download(pyargs('destination', data_dir, 'wait_until_finished', false));

%% list files / directories in dataset
% get list and convert to Matlab cell array
files = cell(ds.get_file_list);
for iii = 1:numel(files)
    iii_file = files{iii};
    fprintf('%d - %s - isDir: %d\n', iii, string(iii_file{'pathInDataSet'}), iii_file{'isDirectory'})
end

%% Spaces
% list spaces
spaces = obi.get_spaces;
spaces
% create a new space
space_id = 'MATLAB_TEST';
space = obi.new_space(pyargs('code',  space_id, 'description', 'test space for Matlab access to openBIS'));
space
space.save;
% delete created space afterwards
% space.delete('just a test');

%% Projects
% list projects in Space
space.get_projects
% create new project
project_code = 'test_project';
project_description = 'my awsome project';
project = space.new_project(pyargs('code', project_code, ...
    'description' , project_description));
project = project.save();
space.get_projects


%% clean up
% logout
obi.logout()



