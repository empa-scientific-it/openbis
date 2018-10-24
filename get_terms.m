function terms = get_terms
%get_sample_types
%Returns information about existing vocabulary terms. 
%If a vocabulary code is provided, it only returns the terms of that vocabulary

global obi

% test connection
test_connection(obi)

terms = obi.get_terms();

terms = df_to_table(terms.df);

end
