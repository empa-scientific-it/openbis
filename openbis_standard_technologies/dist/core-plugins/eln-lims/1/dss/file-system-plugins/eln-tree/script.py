import json
import os

from java.nio.file import NoSuchFileException
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectIdentifier
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
    def __init__(self, settings):
        self.sections = ["Lab Notebook", "Inventory", "Stock"]
        self.inventorySpaces = settings.inventorySpaces
        self.mainMenues = settings.mainMenues
        self.sampleTypeViewAttributes = settings.sampleTypeViewAttributes
        self.endingsOfHiddenSpaces = []
        self.hideSpaceEndingWith("ELN_SETTINGS")
        self.hideSpaceEndingWith("NAGIOS")
        self.hideSpaceEndingWith("STORAGE")
        self.hiddenExperimentTypes = {}
        self.hiddenSampleTypes = {}
        self.hiddenDataSetTypes = {}

    def assertValidSection(self, section):
        if section not in self.sections:
            raise NoSuchFileException("Invalid section '%s'." % section)

    def assertValidSpace(self, space):
        if section not in self.sections:
            raise NoSuchFileException("Invalid section '%s'." % section)

    def hideSpaceEndingWith(self, spaceCodeEnding):
        self.endingsOfHiddenSpaces.append(spaceCodeEnding)

    def acceptSpace(self, section, space):
        code = space.getCode()
        for ending in self.endingsOfHiddenSpaces:
            if code.endswith(ending):
                return False
        if section == "Lab Notebook":
            return not code in self.inventorySpaces and not self._showStockSpace(code) \
                and self._showItem(code, "LabNotebook")
        elif section == "Inventory":
            return code in self.inventorySpaces and not self._showStockSpace(code) \
                and self._showItem(code, "Inventory")
        elif section == "Stock":
            return code in self.inventorySpaces and self._showStockSpace(code)
        return True

    def _showStockSpace(self, code):
        for ending in ["STOCK_CATALOG", "STOCK_ORDERS"]:
            if code.endswith(ending):
                return self._showItem(code, "Stock")
        return False

    def _showItem(self, code, item):
        group = self._getGroupPrefix(code)
        if group in self.mainMenues:
            mainMenue = self.mainMenues[group]
            showTerm = "show%s" % item
            return mainMenue[showTerm] if showTerm in mainMenue else False
        return False
    
    def _getGroupPrefix(self, code):
        groups = filter(lambda group: code.startswith(group), self.mainMenues.keys())
        return groups[0] if groups else "GENERAL"

    def hideExperimentType(self, typeCode):
        self.hiddenExperimentTypes[typeCode] = True

    def acceptExperiment(self, experiment):
        return experiment.getType().getCode() not in self.hiddenExperimentTypes

    def hideSampleType(self, typeCode):
        self.hiddenSampleTypes[typeCode] = True

    def acceptSample(self, sample):
        group = self._getGroupPrefix(sample.getSpace().getCode())
        attributes = self.sampleTypeViewAttributes[group]
        sampleTypeCode = sample.getType().getCode()
        if sampleTypeCode in self.hiddenSampleTypes:
            return False
        if sampleTypeCode in attributes:
            attr = attributes[sampleTypeCode]
            return "SHOW_ON_NAV" in attr and attr["SHOW_ON_NAV"]
        return False

    def hideDataSetType(self, typeCode):
        self.hiddenDataSetTypes[typeCode] = True

    def acceptDataSet(self, dataSet):
        return dataSet.getType().getCode() not in self.hiddenDataSetTypes

class Settings(object):
    def __init__(self, inventorySpaces, mainMenues, sampleTypeViewAttributes):
        self.inventorySpaces = inventorySpaces
        self.mainMenues = mainMenues
        self.sampleTypeViewAttributes = sampleTypeViewAttributes

def resolve(subPath, context):
    acceptor = createAcceptor(context)
    if len(subPath) == 0:
        return listSections(acceptor, context)

    section = subPath[0]
    acceptor.assertValidSection(section)
    if len(subPath) == 1:
        return listSpaces(section, acceptor, context)

    space = subPath[1]
    assertValidSpace(space, context)
    if len(subPath) == 2:
        return listProjects(space, acceptor, context)

    project = subPath[2]
    assertValidProject(space, project, context)
    if len(subPath) == 3:
        return listExperiments(section, space, project, acceptor, context)
    if len(subPath) == 4:
        return listExperimentContent(subPath, acceptor, context)
    if len(subPath) > 4:
        return listChildren(subPath, acceptor, context)

def createAcceptor(context):
    acceptor = Acceptor(getAllSettings(context))
    pluginsFolder = "%s/resolver-plugins" % os.path.dirname(__file__)
    for pluginFileName in os.listdir(pluginsFolder):
        file = "%s/%s" % (pluginsFolder, pluginFileName)
        execfile(file, {"acceptor":acceptor})
    return acceptor

def listSections(acceptor, context):
    response = context.createDirectoryResponse()
    for section in acceptor.sections:
        response.addDirectory(section)
    return response

def listSpaces(section, acceptor, context):
    fetchOptions = SpaceFetchOptions()
    spaces = context.getApi().searchSpaces(context.getSessionToken(), SpaceSearchCriteria(), fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for space in spaces:
        if acceptor.acceptSpace(section, space):
            response.addDirectory(space.getCode(), space.getModificationDate())
    return response

def assertValidSpace(space, context):
    if space != space.upper():
        raise NoSuchFileException("Space '%s' contains lower case characters." % space)
    fetchOptions = SpaceFetchOptions()
    id = SpacePermId(space)
    if context.getApi().getSpaces(context.getSessionToken(), [id], fetchOptions).isEmpty():
        raise NoSuchFileException("Unknown space '%s'." % space)

def assertValidProject(space, project, context):
    if project != project.upper():
        raise NoSuchFileException("Project '%s' contains lower case characters." % project)
    fetchOptions = ProjectFetchOptions()
    id = ProjectIdentifier(space, project)
    if context.getApi().getProjects(context.getSessionToken(), [id], fetchOptions).isEmpty():
        raise NoSuchFileException("Unknown project '%s'." % id)

def getAllSettings(context):
    criteria = SampleSearchCriteria()
    criteria.withType().withCode().thatEquals("GENERAL_ELN_SETTINGS")
    fetchOptions = SampleFetchOptions()
    fetchOptions.withProperties()
    settingsSamples = context.getApi().searchSamples(context.getSessionToken(), criteria, fetchOptions).getObjects()
    inventorySpaces = set()
    mainMenues = {}
    sampleTypeViewAttributes = {}
    for settingsSample in settingsSamples:
        settings = settingsSample.getProperty("$ELN_SETTINGS")
        if settings is not None:
            settings = json.loads(settings)
            inventorySpaces.update(settings["inventorySpaces"])
            inventorySpaces.update(settings["inventorySpacesReadOnly"])
            group = settingsSample.getCode().split("_ELN_SETTINGS")[0]
            if "mainMenu" in settings:
                mainMenues[group] = settings["mainMenu"]
            if "sampleTypeDefinitionsExtension" in settings:
                sampleTypeViewAttributes[group] = settings["sampleTypeDefinitionsExtension"]
    return Settings(inventorySpaces, mainMenues, sampleTypeViewAttributes)

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
                raise NoSuchFileException("Couldn't resolve '%s' because of invalid node type: %s" % (path, nodeType))
        node = context.getCache().getNode(path)
        if node is None:
            raise NoSuchFileException("Couldn't resolve '%s'" % path)
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
    fetchOptions.withSpace()
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
    fetchOptions.withChildren().withSpace()
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
        filePermId = "%s::%s" % (dataSetCode, childNode.getRelativePath())
        context.getCache().putNode(Node("DATASET", filePermId), filePath)
        if response is not None:
            if childNode.isDirectory():
                response.addDirectory(nodeName, childNode.getLastModified())
            else:
                response.addFile(nodeName, childNode)

def getContentNode(permId, context):
    splittedId = permId.split("::")
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
