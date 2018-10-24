function material_types = get_material_types
%get_material_types
%   Returns a table of all available material types

global obi

% test connection
test_connection(obi)

material_types = obi.get_material_types();

material_types = df_to_table(material_types.df);

end
