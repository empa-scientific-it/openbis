import org.apache.commons.io.IOUtils as IOUtils

def process(dataSet):
    print "script processing " + dataSet.getDataSetCode()
    processNode(dataSet.getContent().getRootNode(), dataSet.getDataSetCode())
    
def processNode(node, dataSetCode):
    print "process node: " + dataSetCode + "/" + node.getRelativePath()
    if node.isDirectory():
        for child in node.getChildNodes():
            processNode(child, dataSetCode)
    else:
        print "content (" + str(node.getFileLength()) + "): " + \
              IOUtils.readLines(node.getInputStream()).toString()