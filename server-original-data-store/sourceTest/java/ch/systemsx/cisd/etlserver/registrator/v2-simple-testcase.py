execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")
def should_retry_processing(map, error):
    return False


def process(transaction):
  dataSet = transaction.createNewDataSet()
  incoming = transaction.getIncoming()
  transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
