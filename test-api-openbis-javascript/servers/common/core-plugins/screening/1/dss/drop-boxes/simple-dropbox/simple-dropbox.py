#! /usr/bin/env python
def process(transaction):
    expid = "/TEST/TEST-PROJECT/DEMO-EXP-SIMPLE"
    exp = transaction.getExperiment(expid)
     
    if None == exp:
        exp = transaction.createNewExperiment(expid, "TEST-EXPERIMENT")
   
    dataSet = transaction.createNewDataSet()
          
    dataSet.setDataSetType("UNKNOWN")
    dataSet.setExperiment(exp)
    transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet)
