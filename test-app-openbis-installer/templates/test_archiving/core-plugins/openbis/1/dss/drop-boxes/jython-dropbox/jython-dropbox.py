tr = service.transaction()
dataSet = tr.createNewDataSet()
tr.moveFile(incoming.getAbsolutePath(), dataSet)
dataSet.setExperiment(tr.getExperiment("/TEST/TEST_PROJECT/EXP_TEST"))
