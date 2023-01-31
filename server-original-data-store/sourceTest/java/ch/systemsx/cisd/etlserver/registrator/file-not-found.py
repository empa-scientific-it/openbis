execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile('/non/existent/path', dataSet)
dataSet.setDataSetType('O1')
dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
