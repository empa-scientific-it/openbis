function logout
%LOGOUT
%   Log out of openBIS. After logout, the session token is no longer valid.

global obi

% test connection
test_connection(obi)

obi.logout;

end

