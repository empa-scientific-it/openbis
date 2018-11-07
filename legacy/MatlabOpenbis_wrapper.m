% wrapper / demo script for Matlab openBIS toolbox
% Todo: convert to ML live script

%% Enter connection info for openBIS
% connection details can be stored in the file user_data.json or entered manually
try
    user_data = jsondecode(fileread('user_data.json'));
    username = user_data.User;
    url = user_data.URL;
catch
    username = ''; % enter valid user name
    url = 'https://XYZ.ethz.ch/openbis:8443'; % replace with correct URL
end
% specify openBIS password
pw = passwordEntryDialog('CheckPasswordLength',0);

%% Connect to openBIS
login(url, username, pw)
clear pw % remove password from workspace

% login will create a global variable called obi
global obi

%% Check if connection is still active
% we can check if the connection has timed-out or is still valid
if obi.is_session_active
    fprintf('\nConnection to host %s is still valid.\n', char(obi.url))
else
    disp('Connection is not active. Please re-connect.')
end

%% Logout
obi.logout()