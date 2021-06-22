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

def resolve(subPath, context):
    if len(subPath) == 0:
        return listSpaces(context)
    space = subPath[0]
    if len(subPath) == 1:
        return listProjects(space, context)
    project = subPath[1]
    if len(subPath) == 2:
        return listExperiments(space, project, context)
    experiment = subPath[2]
    if len(subPath) == 3:
        return listExperimentContent(subPath, context)
    if len(subPath) > 3:
        return listChildren(subPath, context)

def listSpaces(context):
    fetchOptions = SpaceFetchOptions()
    spaces = context.getApi().searchSpaces(context.getSessionToken(), SpaceSearchCriteria(), fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for space in spaces:
        response.addDirectory(space.getCode(), space.getModificationDate())
    return response

def listProjects(space, context):
    searchCriteria = ProjectSearchCriteria()
    searchCriteria.withSpace().withCode().thatEquals(space)
    fetchOptions = ProjectFetchOptions()
    projects = context.getApi().searchProjects(context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for project in projects:
        response.addDirectory(project.getCode(), project.getModificationDate())
    return response

def listExperiments(space, project, context):
    response = context.createDirectoryResponse()
    addExperimentNodes(space, project, response, context)
    return response

def listExperimentContent(subPath, context):
    node = getNode(subPath, context)
    path = "/".join(subPath)
    response = context.createDirectoryResponse()
    experimentPermId = node.getPermId()
    addExperimentChildNodes(path, experimentPermId, response, context)
    return response

def listChildren(subPath, context):
    path = "/".join(subPath)
    node = getNode(subPath, context)
    nodeType = node.getType()
    permId = node.getPermId()
    if nodeType == "DATASET":
        dataSetCode, contentNode, content = getContentNode(permId, context)
        if contentNode.isDirectory():
            response = context.createDirectoryResponse()
            addDataSetFileNodes(path, dataSetCode, contentNode, response, context)
            return response
        else:
            return context.createFileResponse(contentNode, content)
    elif nodeType == "SAMPLE":
        response = context.createDirectoryResponse()
        addSampleChildNodes(path, permId, response, context)
        return response

def getNode(subPath, context):
    path = "/".join(subPath)
    node = context.getCache().getNode(path)
    if node is None:
        if len(subPath) == 3:
            addExperimentNodes(subPath[0], subPath[1], None, context)
        else:
            parentPath = subPath[:-1]
            parentNode = getNode(parentPath, context)
            parentPathString = "/".join(parentPath)
            nodeType = parentNode.getType()
            if nodeType == "EXPERIMENT":
                addExperimentChildNodes(parentPathString, parentNode.getPermId(), None, context)
            elif nodeType == "SAMPLE":
                addSampleChildNodes(parentPathString, parentNode.getPermId(), None, context)
            elif nodeType == "DATASET":
                dataSetCode, contentNode, _ = getContentNode(parentNode.getPermId(), context)
                addDataSetFileNodes(parentPathString, dataSetCode, contentNode, None, context)
            else:
                raise BaseException("Couldn't resolve '%s'" % path)
        node = getNode(subPath, context)
    return node

def addExperimentNodes(space, project, response, context):
    searchCriteria = ExperimentSearchCriteria()
    searchCriteria.withProject().withCode().thatEquals(project)
    searchCriteria.withProject().withSpace().withCode().thatEquals(space)
    fetchOptions = ExperimentFetchOptions()
    fetchOptions.withProperties()
    experiments = context.getApi().searchExperiments(context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for experiment in experiments:
        gatherEntity(entitiesByName, experiment)
    addNodes("EXPERIMENT", entitiesByName, "%s/%s" % (space, project), response, context)

def addExperimentChildNodes(path, experimentPermId, response, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    listDataSets(path, dataSetSearchCriteria, False, response, context)

    sampleSearchCriteria = SampleSearchCriteria()
    sampleSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    fetchOptions = SampleFetchOptions()
    fetchOptions.withProperties()
    fetchOptions.withParents().withExperiment()
    samples = context.getApi().searchSamples(context.getSessionToken(), sampleSearchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for sample in samples:
        if not hasParentInSameExperiment(sample, experimentPermId):
            gatherEntity(entitiesByName, sample)
    addNodes("SAMPLE", entitiesByName, path, response, context)

def addSampleChildNodes(path, samplePermId, response, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withSample().withPermId().thatEquals(samplePermId)
    listDataSets(path, dataSetSearchCriteria, True, response, context)

    fetchOptions = SampleFetchOptions()
    fetchOptions.withChildren().withProperties()
    sampleId = SamplePermId(samplePermId)
    sample = context.getApi().getSamples(context.getSessionToken(), [sampleId], fetchOptions)[sampleId]
    entitiesByName = {}
    for child in sample.getChildren():
        gatherEntity(entitiesByName, child)
    addNodes("SAMPLE", entitiesByName, path, response, context)

def addDataSetFileNodes(path, dataSetCode, contentNode, response, context):
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

def listDataSets(path, dataSetSearchCriteria, assignedToSample, response, context):
    fetchOptions = DataSetFetchOptions()
    fetchOptions.withProperties()
    fetchOptions.withSample()
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, fetchOptions).getObjects()
    entitiesByName = {}
    for dataSet in dataSets:
        sample = dataSet.getSample()
        if (assignedToSample and sample is not None) or (not assignedToSample and sample is None):
            gatherEntity(entitiesByName, dataSet)
    addNodes("DATASET", entitiesByName, path, response, context)

def gatherEntity(entitiesByName, entity):
    name = entity.getProperties().get("$NAME")
    nodeName = name if name is not None else entity.getCode()
    if nodeName not in entitiesByName:
        entitiesByName[nodeName] = []
    entitiesByName[nodeName].append(entity)

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
