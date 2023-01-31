def process(transaction):
    data = transaction.createNewDataSet("UNKNOWN", transaction.getIncoming().getName())
    data.setExperiment(transaction.getExperiment("/CISD/NEMO/EXP1"))
    transaction.moveFile(transaction.getIncoming().getPath(), data)