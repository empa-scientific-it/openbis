function experiment_types = get_experiment_types
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

global obi

% test connection
test_connection(obi)

experiment_types = obi.get_experiment_types();
experiment_types = df_to_cell(experiment_types.df);


end

