function pass = obi_test(varargin)
%obi_test Testing function for the Matlab openBIS Toolbox
%   This function runs a few tests for the Matlab openBIS Toolbox. It
%   returns true if all the tests pass successfully and fasle otherwise.
%   Input argumtents (all optional):
%   teardown ... delete everything at the end (true)
pass = true;

if nargin == 1
    teardown = varargin{1};
else
    teardown = true;
end

%% 0. Check if pyversion is setup correctly

%% 1. Login to openBIS
url = 'https://127.0.0.1'; % URL of test server
user = 'hluetcke'; % user name for test server
% enter password on command line (only works with bash shells!)
disp('Enter openBIS password:')
[~, pw] = system('read -s password; echo $password');
pw = strtrim(pw);

obi = OpenBis(url, user, pw);
assert(obi.is_session_active(), 'Session not active');
clear pw

%% 2. Create space for test
space_name = 'TESTING_SPACE';
try
    space = obi.new_space(space_name, 'a space for tests of the Matlab openBIS Toolbox');
    fprintf('\n%s - Successfully created Space %s\n', datestr(clock,31), space_name)
catch
    disp('Could not create requested space')
    rethrow(lasterror)
end
spaces = obi.get_spaces();
assert(any(ismember(spaces.code, space_name)), 'Space has not been created');

%% 3. Create project for test
project_name = 'TESTING_PROJECT';
try
    project = obi.new_project(space, project_name, 'a project for tests of the Matlab openBIS Toolbox');
    fprintf('\n%s - Successfully created Project %s\n', datestr(clock,31), project_name)
catch
    disp('Could not create requested project')
    rethrow(lasterror)
end
projects = obi.get_projects(space_name, project_name);
assert(any(ismember(projects.identifier, sprintf('/%s/%s', space_name, project_name))), 'Project has not been created');

%% 4. Create experiment for test
exp_name = 'TESTING_EXPERIMENT';
try
    exp = obi.new_experiment('DEFAULT_EXPERIMENT', exp_name, sprintf('/%s/%s', space_name, project_name));
    fprintf('\n%s - Successfully created Experiment %s\n', datestr(clock,31), exp_name)
catch
    disp('Could not create requested experiment')
    rethrow(lasterror)
end

%% 5. Create dataset with dummy files
% first create the dummy files
file1 = sprintf('test_%d%d%d%d%d.txt',randi(9,1,5));
file2 = sprintf('test_%d%d%d%d%d.txt',randi(9,1,5));
    function create_file(fname)
        fid = fopen(fname, 'w+');
        fprintf(fid, 'Hello world!');
        fclose(fid);
    end
create_file(file1);
create_file(file2);

% then create the dataset in the TESTING_EXPERIMENT
dataset_type = 'RAW_DATA';
dataset_object = sprintf('/%s/%s/%s', space_name, project_name, exp_name);
file_list = {file1, file2};
try
    dataset = obi.new_dataset(dataset_type, dataset_object, file_list);
    fprintf('\n%s - Successfully created new Dataset with permId %s\n', datestr(clock,31), char(dataset.permId))
catch
    disp('Could not create new dataset')
    rethrow(lasterror)
end


%% 6. Download the created dataset
try
    files = obi.get_dataset_files(dataset);
    file_list = files.pathInDataSet(files.fileSize>0);
    destination = sprintf('temp_%d%d%d%d%d', randi(9,1,5));
    path_to_files = obi.dataset_download(dataset, file_list, 'destination', destination, ...
        'wait_until_finished', true);
    fprintf('\n%s - Successfully downloaded Dataset with permId %s to folder %s\n', ...
        datestr(clock,31), char(dataset.permId), destination)
catch
    disp('Could not download dataset')
    rethrow(lasterror)
end


%% 7. Tear-down (delete everything, optional)
if teardown
    fprintf('\n\n%s - Starting tear-down', datestr(clock,31))
    % delete the created local files
    delete(file1);  delete(file2); rmdir(destination, 's');
    fprintf('\n%s - Successfully deleted local files\n', datestr(clock,31))
    
    % delete openBIS experiment
    obi.delete_experiment(exp, 'created by Matlab-openBIS toolbox test function');
    fprintf('\n%s - Successfully deleted Experiment %s\n', datestr(clock,31), exp_name)
    
    % delete openBIS project
    obi.delete_project(project_name, 'created by Matlab-openBIS toolbox test function');
    fprintf('\n%s - Successfully deleted Project %s\n', datestr(clock,31), project_name)
    
    % delete openBIS space
    obi.delete_space(space, 'created by Matlab-openBIS toolbox test function');
    fprintf('\n%s - Successfully deleted Space %s\n', datestr(clock,31), space_name)
end

%% 8. Logout
obi.logout()

end

