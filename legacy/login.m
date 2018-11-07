function login(varargin)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

global obi

if nargin
    url = varargin{1};
    user = varargin{2};
    pw = varargin{3};
else
    [url, user, pw] = user_url_pw_inputdlg;
end

obi = py.pybis.Openbis(url, pyargs('verify_certificates', 0));
obi.login(user, pw, pyargs('save_token', 1));

end

function [url, user, pw] = user_url_pw_inputdlg

prompt = {'openBIS URL:', 'openBIS user:'};
title = 'openBIS connection details';
definput = {'https://XYZ.ethz.ch/openbis:8443', ''};
answer = inputdlg(prompt, title, 1, definput);

url = answer{1};
user = answer{2};

pw = passwordEntryDialog('CheckPasswordLength',0);

end