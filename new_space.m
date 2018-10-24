function new_space(code, description)
% NEW_SPACE
% Creates a new space in the openBIS instance.

global obi

% test connection
test_connection(obi)

space = obi.new_space(pyargs('code',  code, 'description', description));
space.save;

end