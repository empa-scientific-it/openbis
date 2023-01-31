DATA_SOURCE = "path-info-db"
QUERY = """	
					SELECT ds.code as "data_set_code", dsf.*
					FROM data_sets ds, data_set_files dsf
					WHERE ds.code = ?{1} AND dsf.dase_id = ds.id
				"""
				
"""reporting table column names"""
DATA_SET_CODE = "Data Set"
RELATIVE_PATH = "Relative Path"
FILE_NAME = "File Name"
SIZE_IN_BYTES = "Size"
IS_DIRECTORY = "Is Directory?"
LAST_MODIFIED = "Last Modified"

def describe(dataSets, tableBuilder):
    
    tableBuilder.addHeader(DATA_SET_CODE)
    tableBuilder.addHeader(RELATIVE_PATH)
    tableBuilder.addHeader(FILE_NAME)
    tableBuilder.addHeader(SIZE_IN_BYTES)
    tableBuilder.addHeader(IS_DIRECTORY)
    tableBuilder.addHeader(LAST_MODIFIED)
    
    for dataSet in dataSets:
        results = queryService.select(DATA_SOURCE, QUERY, [dataSet.getDataSetCode()])
        print "Found " + str(len(results)) + " results for data set '" + dataSet.getDataSetCode() + "':" 
        for r in results:
            print r		# debugging
            row = tableBuilder.addRow()
            row.setCell(DATA_SET_CODE, r.get("DATA_SET_CODE".lower()))
            row.setCell(RELATIVE_PATH, r.get("RELATIVE_PATH".lower()))
            row.setCell(FILE_NAME, r.get("FILE_NAME".lower()))
            row.setCell(SIZE_IN_BYTES, r.get("SIZE_IN_BYTES".lower()))
            row.setCell(IS_DIRECTORY, r.get("IS_DIRECTORY".lower()))
            row.setCell(LAST_MODIFIED, r.get("LAST_MODIFIED".lower()))
        results.close()