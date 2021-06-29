function pass = obi_test
%obi_test Testing function for the Matlab openBIS Toolbox
%   This function runs a few tests for the Matlab openBIS Toolbox. It
%   returns true if all the tests pass successfully and fasle otherwise.
pass = true;

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


%% 4. Create experiment for test


%% 5. Create dataset with dummy files


%% 6. Download the created dataset


%% 7. Tear-down (delete everything, optional)


%% 8. Logout
obi.logout()

end

