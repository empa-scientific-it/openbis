transaction = service.transaction()
dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
dataSet.setExperiment(transaction.getExperiment("/CISD/NEMO/EXP1"))