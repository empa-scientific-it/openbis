import org.apache.commons.io.IOUtils as IOUtils

DATA_SET = "Data Set"
DATA_SET_TYPE = "Data Set Type"

def describe(dataSets, tableBuilder):
	print "AT THE SCRIPT"
	tableBuilder.addHeader(DATA_SET)
	tableBuilder.addHeader(DATA_SET_TYPE)
	for dataSet in dataSets:
	
		print "DataSet "+dataSet.dataSetCode+": "+dataSet.getContent().getRootNode().getName()
		print "DataSet "+dataSet.dataSetCode+": "+dataSet.content.rootNode.childNodes[0].name
		print "DataSet "+dataSet.dataSetCode+": "+dataSet.content.rootNode.childNodes[0].childNodes[0].name

		print "File: "+IOUtils.readLines(dataSet.content.rootNode.childNodes[0].childNodes[0].getInputStream()).toString()
	
		row = tableBuilder.addRow()
		row.setCell(DATA_SET, dataSet.dataSetCode)
		row.setCell(DATA_SET_TYPE, dataSet.dataSetTypeCode)

