function sample_types = get_sample_types
%get_sample_types
%   Returns a table of all available sample types

global obi

% test connection
test_connection(obi)

sample_types = obi.get_sample_types();

sample_types = df_to_table(sample_types.df);

end
