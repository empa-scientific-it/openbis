import os

from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions
from ch.systemsx.cisd.openbis.dss.generic.server.ftp import Node

class Acceptor(object):
    def __init__(self):
        self.endingsOfHiddenSpaces = []
        self.hideSpaceEndingWith("ELN_SETTINGS")
        self.hideSpaceEndingWith("NAGIOS")
        self.hideSpaceEndingWith("STORAGE")
        self.spaceEndingsBySection = {"Inventory": ["MATERIALS", "METHODS", "PUBLICATIONS"], 
                                      "Stock" : ["STOCK_CATALOG", "STOCK_ORDERS"]}
        self.hiddenExperimentTypes = {}
        self.hiddenSampleTypes = {}
        self.hiddenDataSetTypes = {}

    def hideSpaceEndingWith(self, spaceCodeEnding):
        self.endingsOfHiddenSpaces.append(spaceCodeEnding)

    def acceptSpace(self, section, space):
        code = space.getCode()
        for ending in self.endingsOfHiddenSpaces:
            if code.endswith(ending):
                return False
        if section in self.spaceEndingsBySection:
            for ending in self.spaceEndingsBySection[section]:
                if code.endswith(ending):
                    return True
            return False
        for endings in self.spaceEndingsBySection.values():
            for ending in endings:
                if code.endswith(ending):
                    return False
        return True

    def hideExperimentType(self, typeCode):
        self.hiddenExperimentTypes[typeCode] = True

    def acceptExperiment(self, experiment):
        return experiment.getType().getCode() not in self.hiddenExperimentTypes

    def hideSampleType(self, typeCode):
        self.hiddenSampleTypes[typeCode] = True

    def acceptSample(self, sample):
        return sample.getType().getCode() not in self.hiddenSampleTypes

    def hideDataSetType(self, typeCode):
        self.hiddenDataSetTypes[typeCode] = True

    def acceptDataSet(self, dataSet):
        return dataSet.getType().getCode() not in self.hiddenDataSetTypes

def resolve(subPath, context):
    acceptor = createAcceptor()
    if len(subPath) == 0:
        return listSections(context)
    section = subPath[0]
    if len(subPath) == 1:
        return listSpaces(section, acceptor, context)
    space = subPath[1]
    if len(subPath) == 2:
        return listProjects(space, acceptor, context)
    project = subPath[2]
    if len(subPath) == 3:
        return listExperiments(section, space, project, acceptor, context)
    if len(subPath) == 4:
        return listExperimentContent(subPath, acceptor, context)
    if len(subPath) > 4:
        return listChildren(subPath, acceptor, context)

def createAcceptor():
    acceptor = Acceptor()
    pluginsFolder = "%s/resolver-plugins" % os.path.dirname(__file__)
    for pluginFileName in os.listdir(pluginsFolder):
        file = "%s/%s" % (pluginsFolder, pluginFileName)
        execfile(file, {"acceptor":acceptor})
    return acceptor

def listSections(context):
    response = context.createDirectoryResponse()
    response.addDirectory("Lab Notebook")
    response.addDirectory("Inventory")
    response.addDirectory("Stock")
    return response

def listSpaces(section, acceptor, context):
    fetchOptions = SpaceFetchOptions()
    spaces = context.getApi().searchSpaces(context.getSessionToken(), SpaceSearchCriteria(), fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for space in spaces:
        if acceptor.acceptSpace(section, space):
            response.addDirectory(space.getCode(), space.getModificationDate())
    return response

def listProjects(space, acceptor, context):
    searchCriteria = ProjectSearchCriteria()
    searchCriteria.withSpace().withCode().thatEquals(space)
    fetchOptions = ProjectFetchOptions()
    projects = context.getApi().searchProjects(context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for project in projects:
        response.addDirectory(project.getCode(), project.getModificationDate())
    return response

def listExperiments(section, space, project, acceptor, context):
    response = context.createDirectoryResponse()
    addExperimentNodes(section, space, project, response, acceptor, context)
    return response

def listExperimentContent(subPath, acceptor, context):
    path = "/".join(subPath)
    node = getNode(subPath, acceptor, context)
    response = context.createDirectoryResponse()
    experimentPermId = node.getPermId()
    addExperimentChildNodes(path, experimentPermId, response, acceptor, context)
    return response

def listChildren(subPath, acceptor, context):
    path = "/".join(subPath)
    node = getNode(subPath, acceptor, context)
    nodeType = node.getType()
    permId = node.getPermId()
    if nodeType == "DATASET":
        dataSetCode, contentNode, content = getContentNode(permId, context)
        if contentNode.isDirectory():
            response = context.createDirectoryResponse()
            addDataSetFileNodes(path, dataSetCode, contentNode, response, acceptor, context)
            return response
        else:
            return context.createFileResponse(contentNode, content)
    elif nodeType == "SAMPLE":
        response = context.createDirectoryResponse()
        addSampleChildNodes(path, permId, response, acceptor, context)
        return response

def getNode(subPath, acceptor, context):
    path = "/".join(subPath)
    node = context.getCache().getNode(path)
    if node is None:
        if len(subPath) == 4:
            addExperimentNodes(subPath[0], subPath[1], subPath[2], None, acceptor, context)
        else:
            parentPath = subPath[:-1]
            parentNode = getNode(parentPath, acceptor, context)
            parentPathString = "/".join(parentPath)
            nodeType = parentNode.getType()
            if nodeType == "EXPERIMENT":
                addExperimentChildNodes(parentPathString, parentNode.getPermId(), None, acceptor, context)
            elif nodeType == "SAMPLE":
                addSampleChildNodes(parentPathString, parentNode.getPermId(), None, acceptor, context)
            elif nodeType == "DATASET":
                dataSetCode, contentNode, _ = getContentNode(parentNode.getPermId(), context)
                addDataSetFileNodes(parentPathString, dataSetCode, contentNode, None, acceptor, context)
            else:
                raise BaseException("Couldn't resolve '%s' because of invalid node type: %s" % (path, nodeType))
        node = context.getCache().getNode(path)
        if node is None:
            raise BaseException("Couldn't resolve '%s'" % path)
    return node

def addExperimentNodes(section, space, project, response, acceptor, context):
    searchCriteria = ExperimentSearchCriteria()
    searchCriteria.withProject().withCode().thatEquals(project)
    searchCriteria.withProject().withSpace().withCode().thatEquals(space)
    fetchOptions = ExperimentFetchOptions()
    fetchOptions.withType()
    fetchOptions.withProperties()
    experiments = context.getApi().searchExperiments(context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for experiment in experiments:
        if acceptor.acceptExperiment(experiment):
            gatherEntity(entitiesByName, experiment)
    addNodes("EXPERIMENT", entitiesByName, "%s/%s/%s" % (section, space, project), response, context)

def addExperimentChildNodes(path, experimentPermId, response, acceptor, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    listDataSets(path, dataSetSearchCriteria, False, response, acceptor, context)

    sampleSearchCriteria = SampleSearchCriteria()
    sampleSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    fetchOptions = SampleFetchOptions()
    fetchOptions.withType()
    fetchOptions.withProperties()
    fetchOptions.withParents().withExperiment()
    samples = context.getApi().searchSamples(context.getSessionToken(), sampleSearchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for sample in samples:
        if not hasParentInSameExperiment(sample, experimentPermId) and acceptor.acceptSample(sample):
            gatherEntity(entitiesByName, sample)
    addNodes("SAMPLE", entitiesByName, path, response, context)

def addSampleChildNodes(path, samplePermId, response, acceptor, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withSample().withPermId().thatEquals(samplePermId)
    listDataSets(path, dataSetSearchCriteria, True, response, acceptor, context)

    fetchOptions = SampleFetchOptions()
    fetchOptions.withChildren().withType()
    fetchOptions.withChildren().withProperties()
    sampleId = SamplePermId(samplePermId)
    sample = context.getApi().getSamples(context.getSessionToken(), [sampleId], fetchOptions)[sampleId]
    entitiesByName = {}
    for child in sample.getChildren():
        if acceptor.acceptSample(child):
            gatherEntity(entitiesByName, child)
    addNodes("SAMPLE", entitiesByName, path, response, context)

def addDataSetFileNodes(path, dataSetCode, contentNode, response, acceptor, context):
    for childNode in contentNode.getChildNodes():
        nodeName = childNode.getName()
        filePath = "%s/%s" % (path, nodeName)
        filePermId = "%s:%s" % (dataSetCode, childNode.getRelativePath())
        context.getCache().putNode(Node("DATASET", filePermId), filePath)
        if response is not None:
            if childNode.isDirectory():
                response.addDirectory(nodeName, childNode.getLastModified())
            else:
                response.addFile(nodeName, childNode)

def getContentNode(permId, context):
    splittedId = permId.split(":")
    dataSetCode = splittedId[0]
    content = context.getContentProvider().asContent(dataSetCode)
    contentNode = content.tryGetNode(splittedId[1]) if len(splittedId) > 1 else content.getRootNode()
    return dataSetCode, contentNode, content

def listDataSets(path, dataSetSearchCriteria, assignedToSample, response, acceptor, context):
    fetchOptions = DataSetFetchOptions()
    fetchOptions.withType()
    fetchOptions.withProperties()
    fetchOptions.withSample()
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for dataSet in dataSets:
        sample = dataSet.getSample()
        if ((assignedToSample and sample is not None) or (not assignedToSample and sample is None)) \
                and acceptor.acceptDataSet(dataSet):
            gatherEntity(entitiesByName, dataSet)
    addNodes("DATASET", entitiesByName, path, response, context)

def gatherEntity(entitiesByName, entity):
    nodeName = getNodeName(entity)
    if nodeName not in entitiesByName:
        entitiesByName[nodeName] = []
    entitiesByName[nodeName].append(entity)

def getNodeName(entity):
    nodeName = entity.getCode()
    name = entity.getProperties().get("$NAME")
    if name is not None:
        # replacing normal slash character by a 'division slash' in order to avoid interpretation as path delimiter
        nodeName = name.replace("/", u"\u2215")
    return nodeName

def addNodes(nodeType, entitiesByName, parentPath, response, context):
    for name in sorted(entitiesByName.keys()):
        entities = entitiesByName[name]
        for entity in entities:
            nodeName = name if len(entities) == 1 else "%s [%s]" % (name, entity.getCode())
            path = "%s/%s" % (parentPath, nodeName)
            context.getCache().putNode(Node(nodeType, entity.getPermId().getPermId()), path)
            if response is not None:
                response.addDirectory(nodeName, entity.getModificationDate())

def hasParentInSameExperiment(sample, experimentPermId):
    for parent in sample.getParents():
        if parent.getExperiment().getPermId().getPermId() == experimentPermId:
            return True
    return False
