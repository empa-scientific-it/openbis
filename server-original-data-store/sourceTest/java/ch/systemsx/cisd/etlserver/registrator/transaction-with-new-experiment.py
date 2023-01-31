transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
dataSet.setPropertyValue('dataSetProp', 'dataSetPropValue')
transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
dataSet.setDataSetType('O1')

experiment = transaction.createNewExperiment('/SPACE/PROJECT/EXP', "EXP_TYPE")
experiment.setPropertyValue('propCode', 'propValue')

dataSet.setExperiment(experiment)

