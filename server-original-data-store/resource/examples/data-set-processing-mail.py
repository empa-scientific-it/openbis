import org.apache.commons.io.IOUtils as IOUtils

def process(dataSet):
    dataSetCode = dataSet.getDataSetCode()
    print "script processing " + dataSetCode
    processNode(dataSet.getContent().getRootNode(), dataSet.getDataSetCode())
    
def processNode(node, dataSetCode):
    print "process node: " + dataSetCode + "/" + node.getRelativePath()
    if node.isDirectory():
        for child in node.getChildNodes():
            processNode(child, dataSetCode)
    else:
        fileName = node.getName()
        if fileName.endswith(".txt"):
            fileAsString = IOUtils.readLines(node.getInputStream()).toString()
            mailService.createEmailSender().\
                withSubject("processed text file " + fileName).\
                withBody("see the attached file").\
                withAttachedText(fileAsString, fileName).\
                send()
        else:
            filePath = node.getFile().getPath()
            mailService.createEmailSender().\
                withSubject("processed file " + fileName).\
                withBody("see the attached file").\
                withAttachedFile(filePath, fileName).\
                send()