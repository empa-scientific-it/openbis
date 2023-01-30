def process(transaction):
    incoming = transaction.getIncoming()
    parts = incoming.name.split()
    dataSetType = parts[0]
    id = parts[1].replace(':', '/')
   
    dataSet = transaction.createNewDataSet()
    dataSet.setDataSetType(dataSetType)
    if id.count('/') > 2:
        dataSet.setExperiment(transaction.getExperiment(id))
    else:
        dataSet.setSample(transaction.getSample(id))
          
    transaction.moveFile(incoming.getAbsolutePath(), dataSet, 'data')
