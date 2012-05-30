execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")
def process():
    dataSet = transaction.createNewDataSet()
    transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
    dataSet.setDataSetType('O1')
    dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
    container = transaction.createNewDataSet("CONTAINER_TYPE")
    container.setExperiment(transaction.getExperiment("/SPACE/PROJECT/EXP"))
    container.setContainedDataSetCodes([dataSet.getDataSetCode()])