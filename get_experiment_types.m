function experiment_types = get_experiment_types
%get_experiment_types
%   Returns a table of all available experiment types

global obi

% test connection
test_connection(obi)

experiment_types = obi.get_experiment_types();

experiment_types = df_to_table(experiment_types.df);

end
