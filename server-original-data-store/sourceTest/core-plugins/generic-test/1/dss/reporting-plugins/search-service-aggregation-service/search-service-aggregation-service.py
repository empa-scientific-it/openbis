from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.dto.identifier import ProjectIdentifier

def result(tableBuilder, value):
  row = tableBuilder.addRow()
  row.setCell("result", value)

def aggregate(parameters, tableBuilder):
  tableBuilder.addHeader("result")
  
  mode = parameters.get('mode')
  
  if (mode == 'datasetsAll'):
    searchCriteria = SearchCriteria()
    dataSets = searchServiceUnfiltered.searchForDataSets(searchCriteria)
    result(tableBuilder, len(dataSets))

  if (mode == 'datasetsFiltered'):
    searchCriteria = SearchCriteria()
    dataSets = searchService.searchForDataSets(searchCriteria)
    result(tableBuilder, len(dataSets))

  if (mode == 'samplesAll'):
    searchCriteria = SearchCriteria()
    samples = searchServiceUnfiltered.searchForSamples(searchCriteria)
    result(tableBuilder, len(samples))

  if (mode == 'samplesFiltered'):
    searchCriteria = SearchCriteria()
    samples = searchService.searchForSamples(searchCriteria)
    result(tableBuilder, len(samples))
    
  if (mode == 'experimentsAll'):
    projectId = parameters.get('projectId')
    experiments = searchServiceUnfiltered.listExperiments(projectId)
    result(tableBuilder, len(experiments))

  if (mode == 'experimentsFiltered'):
    projectId = parameters.get('projectId')
    experiments = searchService.listExperiments(projectId)
    result(tableBuilder, len(experiments))    