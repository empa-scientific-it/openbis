import sys

def execute_query(query_service, block, query, params=None):
	if params is None:
		result = query_service.select("path-info-db", query)
	else:
		result = query_service.select("path-info-db", query, params)
	block(result)
	result.close()

# Execute a query
query_service = state.getDataSourceQueryService()
execute_query(query_service, lambda result: sys.stdout.write("Roots: " + str(result.size()) + "\n") , "SELECT * from data_set_files WHERE parent_id is NULL")

execute_query(query_service, lambda result: sys.stdout.write("Level 1: " + str(result.size()) + "\n"), "SELECT * from data_set_files WHERE parent_id = ?1", [155555])
