function [url, user, pw] = user_url_pw_inputdlg
%user_url_pw_inputdlg
%   Return the URL, user name and password for the openBIS server

prompt = {'openBIS URL:', 'openBIS user:'};
title = 'openBIS connection details';
definput = {'https://XYZ.ethz.ch/openbis:8443', ''};
answer = inputdlg(prompt, title, 1, definput);

url = answer{1};
user = answer{2};

% pw = passwordEntryDialog('CheckPasswordLength',0);
pw = passcode;

end