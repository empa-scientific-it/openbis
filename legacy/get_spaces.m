function spaces = get_spaces
% GET_SPACES
% Get a list of all available spaces (DataFrame object). To create a sample or a 
% dataset, you need to specify in which space it should live.

global obi

% test connection
test_connection(obi)

spaces = obi.get_spaces();

spaces = df_to_table(spaces.df);

end
