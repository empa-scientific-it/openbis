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
obi = OpenBis();
assert(obi.is_session_active(), 'Session not active');

%% 2. Create space for test
space_name = 'TESTING_SPACE';
try
    space = obi.new_space(space_name, 'a space for tests of the Matlab openBIS Toolbox');
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
catch
    disp('Could not create requested project')
    rethrow(lasterror)
end
projects = obi.get_projects(space_name, project_name);
assert(any(ismember(projects.identifier, sprintf('/%s/%s', space_name, project_name))), 'Project has not been created');

%% 4. Create experiment for test
experiment_name = 'TESTING_EXPERIMENT';


%% 5. Create dataset with dummy files


%% 6. Download the created dataset


%% 7. Tear-down (delete everything, optional)
if teardown
    % delete project
    obi.delete_project(project_name, 'created by Matlab-openBIS toolbox test function')
    % delete space
    obi.delete_space(space, 'created by Matlab-openBIS toolbox test function')
end

%% 8. Logout
obi.logout()

end

