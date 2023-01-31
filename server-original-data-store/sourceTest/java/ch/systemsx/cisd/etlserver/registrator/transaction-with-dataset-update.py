transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
dataSet.setPropertyValue('dataSetProp', 'dataSetPropValue')
dataSet.setDataSetType('O1')

experiment = transaction.getExperiment('/SPACE/PROJECT/EXP')
dataSet.setExperiment(experiment)
transaction.moveFile(incoming.getAbsolutePath(), dataSet)

container = transaction.getDataSetForUpdate("container-data-set-code")
newContents = list(container.containedDataSetCodes)
newContents.append(dataSet.dataSetCode)
container.setContainedDataSetCodes(newContents)
container.setPropertyValue("newProp", "newValue")
container.setExperiment(experiment)

