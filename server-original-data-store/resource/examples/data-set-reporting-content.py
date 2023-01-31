import java.util.Date as Date

CODE = "Code"
FILE_NAME = "File Name"
RELATIVE_PATH = "Relative Path"
LAST_MODIFIED = "Last Modified"
SIZE = "Size"

def describe(dataSets, tableBuilder):
    tableBuilder.addHeader(CODE)
    tableBuilder.addHeader(FILE_NAME)
    tableBuilder.addHeader(RELATIVE_PATH)
    tableBuilder.addHeader(LAST_MODIFIED)
    tableBuilder.addHeader(SIZE)
    for dataSet in dataSets:
        print "script reporting " + dataSet.getDataSetCode()
        describeNode(dataSet.getContent().getRootNode(), dataSet.getDataSetCode(), tableBuilder)
        

def describeNode(node, dataSetCode, tableBuilder):
    print "describe node: " + dataSetCode + "/" + node.getRelativePath()
    if node.isDirectory():
        for child in node.getChildNodes():
            describeNode(child, dataSetCode, tableBuilder)
    else:
        row = tableBuilder.addRow()
        row.setCell(CODE, dataSetCode)
        row.setCell(FILE_NAME, node.getName())
        row.setCell(RELATIVE_PATH, node.getRelativePath())
        row.setCell(LAST_MODIFIED, Date(node.getLastModified()))
        row.setCell(SIZE, node.getFileLength())