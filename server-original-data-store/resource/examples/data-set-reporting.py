CODE = "Code"
TYPE = "Type"
SIZE = "Size"
LOCATION = "Location"
SPEED_HINT = "Speed Hint"
MAIN_PATTERN = "Main Data Set Pattern"
MAIN_PATH = "Main Data Set Path"
INSTANCE = "Instance"
SPACE = "Space"
PROJECT = "Project"
EXPERIMENT_CODE = "Experiment Code"
EXPERIMENT_IDENTIFIER = "Experiment Identifier"
EXPERIMENT_TYPE = "Experiment Type"
SAMPLE_CODE = "Sample Code"
SAMPLE_IDENTIFIER = "Sample Identifier"
SAMPLE_TYPE = "Sample Type"

def describe(dataSets, tableBuilder):
    
    tableBuilder.addHeader(CODE)
    tableBuilder.addHeader(TYPE)
    tableBuilder.addHeader(SIZE)
    tableBuilder.addHeader(LOCATION)
    tableBuilder.addHeader(SPEED_HINT)
    tableBuilder.addHeader(MAIN_PATTERN)
    tableBuilder.addHeader(MAIN_PATH)
    tableBuilder.addHeader(INSTANCE)
    tableBuilder.addHeader(SPACE)
    tableBuilder.addHeader(PROJECT)
    tableBuilder.addHeader(EXPERIMENT_CODE)
    tableBuilder.addHeader(EXPERIMENT_IDENTIFIER)
    tableBuilder.addHeader(EXPERIMENT_TYPE)
    tableBuilder.addHeader(SAMPLE_CODE)
    tableBuilder.addHeader(SAMPLE_IDENTIFIER)
    tableBuilder.addHeader(SAMPLE_TYPE)
    
    for dataSet in dataSets:
        print "script reporting " + dataSet.getDataSetCode()
        
        row = tableBuilder.addRow()
        row.setCell(CODE, dataSet.getDataSetCode())
        row.setCell(TYPE, dataSet.getDataSetTypeCode())
        row.setCell(SIZE, dataSet.getDataSetSize())
        row.setCell(LOCATION, dataSet.getDataSetLocation())
        row.setCell(SPEED_HINT, dataSet.getSpeedHint())
        row.setCell(MAIN_PATTERN, dataSet.getMainDataSetPattern())
        row.setCell(MAIN_PATH, dataSet.getMainDataSetPath())
        row.setCell(INSTANCE, dataSet.getInstanceCode())
        row.setCell(SPACE, dataSet.getSpaceCode())
        row.setCell(PROJECT, dataSet.getProjectCode())
        row.setCell(EXPERIMENT_CODE, dataSet.getExperimentCode())
        row.setCell(EXPERIMENT_IDENTIFIER, dataSet.getExperimentIdentifier())
        row.setCell(EXPERIMENT_TYPE, dataSet.getExperimentTypeCode())
        row.setCell(SAMPLE_CODE, dataSet.getSampleCode())
        row.setCell(SAMPLE_IDENTIFIER, dataSet.getSampleIdentifier())
        row.setCell(SAMPLE_TYPE, dataSet.getSampleTypeCode())