from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute

EXPERIMENT = "Experiment"
CODE = "Data Set Code"
NUMBER_OF_FILES = "Number of Files"
NUMBER_OF_PROTEINS = "Number of Proteins"

def countFiles(node):
    sum = 1
    if node.isDirectory():
        for child in node.getChildNodes():
            sum = sum + countFiles(child)
    return sum

def getNumberOfProteins(dataSetCode):
    result = queryService.select("protein-db", "select count(*) as count from proteins where data_set = ?{1}", [dataSetCode])
    return result[0].get("count")

def aggregate(parameters, tableBuilder):
    experimentCode = parameters.get('experiment-code')
    searchCriteria = SearchCriteria()
    subCriteria = SearchCriteria()
    subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, experimentCode))
    searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(subCriteria))
    dataSets = searchService.searchForDataSets(searchCriteria)
    tableBuilder.addHeader(EXPERIMENT)
    tableBuilder.addHeader(CODE)
    tableBuilder.addHeader(NUMBER_OF_FILES)
    tableBuilder.addHeader(NUMBER_OF_PROTEINS)
    for dataSet in dataSets:
        dataSetCode = dataSet.getDataSetCode()
        content = contentProvider.getContent(dataSetCode)
        row = tableBuilder.addRow()
        row.setCell(EXPERIMENT, dataSet.experiment.experimentIdentifier)
        row.setCell(CODE, dataSetCode)
        row.setCell(NUMBER_OF_FILES, countFiles(content.rootNode))
        row.setCell(NUMBER_OF_PROTEINS, getNumberOfProteins(dataSetCode))
