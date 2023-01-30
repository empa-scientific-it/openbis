
def process(transaction):
    expid = "/DEFAULT/DEFAULT/EXP1"
    exp = transaction.getExperiment(expid)
     
    if None == exp:
        exp = transaction.createNewExperiment(expid, "UNKNOWN")
   
    dataSet = transaction.createNewDataSet()
          
    dataSet.setDataSetType("UNKNOWN")
    dataSet.setExperiment(exp)
    transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet)
