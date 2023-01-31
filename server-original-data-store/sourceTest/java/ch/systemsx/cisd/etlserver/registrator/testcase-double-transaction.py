execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

transaction = service.transaction(incoming, factory)
transaction1 = service.transaction(incoming, factory)

dataSet = transaction.createNewDataSet()
dataSet1 = transaction1.createNewDataSet()

transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
transaction1.moveFile(incoming.getPath() + '/sub_data_set_2', dataSet1)

dataSet.setDataSetType('O1')
dataSet1.setDataSetType('O1')

dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
dataSet1.setExperiment(transaction1.getExperiment('/SPACE/PROJECT/EXP'))

