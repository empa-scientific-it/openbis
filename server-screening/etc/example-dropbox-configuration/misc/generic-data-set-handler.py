transaction = service.transaction()
dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
dataSet.setExperiment(transaction.getExperiment("/TEST/DATA_IMPORT/E11"))