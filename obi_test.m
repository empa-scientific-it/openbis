function pass = obi_test
%obi_test Testing function for the Matlab openBIS Toolbox
%   This function runs a few tests for the Matlab openBIS Toolbox. It
%   returns true if all the tests pass successfully and fasle otherwise.
pass = true;

%% 0. Check if pyversion is setup correctly

%% 1. Login to openBIS
obi = OpenBis();
assert(obi.is_session_active(), 'Session not active')

%% 2. Create space for test


%% 3. Create project for test


%% 4. Create experiment for test


%% 5. Create dataset with dummy files


%% 6. Download the created dataset


%% 7. Tear-down (delete everything, optional)


%% 8. Logout


end

