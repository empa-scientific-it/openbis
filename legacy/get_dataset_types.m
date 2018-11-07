function dataset_types = get_dataset_types
%get_dataset_types
%   Returns a table of all available dataset types

global obi

% test connection
test_connection(obi)

dataset_types = obi.get_dataset_types();

dataset_types = df_to_table(dataset_types.df);

end
