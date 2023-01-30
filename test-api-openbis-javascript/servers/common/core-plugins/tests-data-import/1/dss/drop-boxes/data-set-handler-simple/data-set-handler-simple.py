#! /usr/bin/env python
def process(transaction):
    exp = transaction.getExperiment("/TEST/TEST-PROJECT/TEST-EXPERIMENT-2")
    dataSet = transaction.createNewDataSet()
    dataSet.setDataSetType("UNKNOWN")
    dataSet.setExperiment(exp)
    transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet)
