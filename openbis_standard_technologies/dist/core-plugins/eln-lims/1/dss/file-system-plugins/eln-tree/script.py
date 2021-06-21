from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions
from ch.systemsx.cisd.openbis.dss.generic.server.ftp import Node

def resolve(subPath, context):
    path = "/".join(subPath)
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
        return listExperimentContent(path, context)

def listSpaces(context):
    spaces = context.getApi().searchSpaces(context.getSessionToken(), SpaceSearchCriteria(), SpaceFetchOptions()).getObjects()
    response = context.createDirectoryResponse()
    for space in spaces:
        response.addDirectory(space.getCode(), space.getModificationDate())
    return response

def listProjects(space, context):
    projectSearchCriteria = ProjectSearchCriteria()
    projectSearchCriteria.withSpace().withCode().thatEquals(space)
    projects = context.getApi().searchProjects(context.getSessionToken(), projectSearchCriteria, ProjectFetchOptions()).getObjects()
    response = context.createDirectoryResponse()
    for project in projects:
        response.addDirectory(project.getCode(), project.getModificationDate())
    return response

def listExperiments(space, project, context):
    searchCriteria = ExperimentSearchCriteria()
    searchCriteria.withProject().withCode().thatEquals(project)
    searchCriteria.withProject().withSpace().withCode().thatEquals(space)
    fetchOptions = ExperimentFetchOptions()
    fetchOptions.withProperties()
    experiments = context.getApi().searchExperiments(context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    response = context.createDirectoryResponse()
    for experiment in experiments:
        nodeName = experiment.getProperties().get("$NAME")
        if nodeName is None:
            nodeName = experiment.getCode()
        path = "%s/%s/%s" % (space, project, nodeName)
        context.getCache().putNode(Node("EXP", experiment.getPermId().getPermId()), path)
        response.addDirectory(nodeName, experiment.getModificationDate())
    return response

def listExperimentContent(path, context):
    node = context.getCache().getNode(path)
    experimentPermId = node.getPermId()
    print("EXP:%s" % experimentPermId)
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, DataSetFetchOptions()).getObjects()
    response = context.createDirectoryResponse()
    for dataSet in dataSets:
        response.addDirectory(dataSet.getCode(), dataSet.getModificationDate())

    sampleSearchCriteria = SampleSearchCriteria()
    sampleSearchCriteria.withExperiment().withPermId().thatEquals(experimentPermId)
    samples = context.getApi().searchSamples(context.getSessionToken(), sampleSearchCriteria, SampleFetchOptions()).getObjects()
    for sample in samples:
        response.addDirectory(sample.getCode(), sample.getModificationDate())
    return response
    