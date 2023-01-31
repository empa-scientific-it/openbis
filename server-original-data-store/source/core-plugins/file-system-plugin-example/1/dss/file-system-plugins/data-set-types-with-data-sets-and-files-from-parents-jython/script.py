from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id import DataSetPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria

def resolve(subPath, context):
    if len(subPath) == 0:
        return listDataSetTypes(context);

    dataSetType = subPath[0];
    if len(subPath) == 1:
        return listDataSetsOfGivenType(dataSetType, context);

    return resolveFileSearch(subPath, context);

def resolveFileSearch(subPath, context):
    dataSetCode = subPath[1];
    requestedFileName = None if len(subPath) == 2 else subPath[2];
    if len(subPath) > 3:
        raise ArgumentException("This resolver can't resolve path of that length");

    dataSetsToSearch = searchForDataSetAndParents(dataSetCode, context);

    if dataSetsToSearch == None:
        return context.createNonExistingFileResponse(None);

    if requestedFileName != None:
        result = findRequestedNode(dataSetsToSearch, requestedFileName, context);
        if result != None:
            return result;
        else:
            return context.createNonExistingFileResponse("Unable to locate requested file");

    response = context.createDirectoryResponse();
    for node in findAllNodes(dataSetsToSearch, context.getContentProvider()):
        response.addFile(node.getName(), node);
    return response;

def findAllNodes(dataSetsToSearch, contentProvider):
    result = []
    for dataSet in dataSetsToSearch:
        nodes = []
        nodes.append(contentProvider.asContent(dataSet.getCode()).getRootNode());

        while len(nodes) > 0:
            node = nodes.pop();
            print "Popping node %s from %s" % (node.getName(), dataSet.getCode())
            if node.isDirectory():
                for child in node.getChildNodes():
                    nodes.append(child);
            else:
                result.append(node);
    return result;

def findRequestedNode(dataSetsToSearch, requestedFileName, context):
    contentProvider = context.getContentProvider();
    for dataSet in dataSetsToSearch:
        content = contentProvider.asContent(dataSet.getCode());
        node = findRequestedSubNode(content.getRootNode(), requestedFileName);
        if node is not None:
            return context.createFileResponse(node, content);
    return None;

def findRequestedSubNode(node, requestedFileName):
    if node.isDirectory():
        for subNode in node.getChildNodes():
            result = findRequestedSubNode(subNode, requestedFileName);
            if result is not None:
                return result;
    else:
        print "%s ==? %s " % (node.getName(), requestedFileName)
        if node.getName() == requestedFileName:
            return node;
    return None;

# returns a list of data sets starting with requested data set and continued by it's parents
def searchForDataSetAndParents(dataSetCode, context):
    dataId = DataSetPermId(dataSetCode);
    fetchOptions = DataSetFetchOptions();
    fetchOptions.withParents();
    dataSet = context.getApi().getDataSets(context.getSessionToken(), [dataId], fetchOptions).get(dataId);

    if dataSet == None:
        return None

    dataSetsToSearch = []
    dataSetsToSearch.append(dataSet);
    dataSetsToSearch.extend(dataSet.getParents());
    return dataSetsToSearch;

def listDataSetsOfGivenType(dataSetType, context):
    fetchOptions = DataSetFetchOptions();
    fetchOptions.withParents();
    searchCriteria = DataSetSearchCriteria();
    searchCriteria.withType().withCode().thatEquals(dataSetType);
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), searchCriteria, fetchOptions).getObjects();

    result = context.createDirectoryResponse();
    for dataSet in dataSets:
        result.addDirectory(dataSet.getCode());
    return result;

def listDataSetTypes(context):
    dataSetTypes = context.getApi().searchDataSetTypes(context.getSessionToken(), DataSetTypeSearchCriteria(), DataSetTypeFetchOptions()).getObjects();

    response = context.createDirectoryResponse();
    for dataSetType in dataSetTypes:
        response.addDirectory(dataSetType.getCode());
    return response;
