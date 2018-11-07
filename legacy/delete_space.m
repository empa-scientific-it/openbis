function delete_space(code, reason)
% DELETE_SPACE
% Delete space in the openBIS instance. A reason has to be specified.

global obi

% test connection
test_connection(obi)

space = obi.get_space(code);
space.delete(reason);

end