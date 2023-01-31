CODE = "Data Set Code"
EXPERIMENT_IDENTIFIER = "Experiment Identifier"
EXPERIMENT_TYPE = "Experiment Type"
EXPERIMENT_DESCRIPTION = "Description"

def describe(dataSets, tableBuilder):
    
    tableBuilder.addHeader(CODE)
    tableBuilder.addHeader(EXPERIMENT_IDENTIFIER)
    tableBuilder.addHeader(EXPERIMENT_TYPE)
    tableBuilder.addHeader(EXPERIMENT_DESCRIPTION)
    
    for dataSet in dataSets:
        projectIdentifier = "/" + dataSet.getSpaceCode() + "/" + dataSet.getProjectCode()
        print "script reporting " + dataSet.getDataSetCode() + " from " + projectIdentifier
        experiments = searchService.listExperiments(projectIdentifier)
        
        for experiment in experiments:
            row = tableBuilder.addRow()
            row.setCell(CODE, dataSet.getDataSetCode())
            row.setCell(EXPERIMENT_IDENTIFIER, experiment.getExperimentIdentifier())
            row.setCell(EXPERIMENT_TYPE, experiment.getExperimentType())
            row.setCell(EXPERIMENT_DESCRIPTION, experiment.getPropertyValue("DESCRIPTION"))