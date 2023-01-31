DATA_SOURCE = "openbis-metadata"
QUERY = """	
					SELECT d.code as "DATA_SET_CODE", prty.code as "PROPERTY_CODE", dsp.value as "PROPERTY_VALUE"
					FROM data d, data_set_properties dsp, data_set_type_property_types dstpt, property_types prty
					WHERE d.code = ?{1} AND dsp.ds_id = d.id AND dsp.dstpt_id = dstpt.id AND dstpt.prty_id = prty.id
				"""

DATA_SET_CODE = "Data Set"
PROPERTY_CODE = "Property"
PROPERTY_VALUE = "Value"

def describe(dataSets, tableBuilder):
    
    tableBuilder.addHeader(DATA_SET_CODE)
    tableBuilder.addHeader(PROPERTY_CODE)
    tableBuilder.addHeader(PROPERTY_VALUE)
    
    for dataSet in dataSets:
        results = queryService.select(DATA_SOURCE, QUERY, [dataSet.getDataSetCode()])
        print "Found " + str(len(results)) + " results for data set '" + dataSet.getDataSetCode() + "':"
        for r in results:
				    print r 	# debugging  
						row = tableBuilder.addRow()
            row.setCell(DATA_SET_CODE, r.get("DATA_SET_CODE"))
            row.setCell(PROPERTY_CODE, r.get("PROPERTY_CODE"))
            row.setCell(PROPERTY_VALUE, r.get("PROPERTY_VALUE"))
				results.close()