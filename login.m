function login(url, user, pw)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

global obi

obi = py.pybis.Openbis(url, pyargs('verify_certificates', 0));
obi.login(user, pw, pyargs('save_token', 1));

end

