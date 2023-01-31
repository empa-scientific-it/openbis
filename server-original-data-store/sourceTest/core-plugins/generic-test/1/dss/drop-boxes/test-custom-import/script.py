def process(transaction):
    data = transaction.createNewDataSet("HCS_IMAGE", transaction.getIncoming().getName())
    data.setExperiment(transaction.getExperiment("/CISD/NEMO/EXP1"))
    data.setPropertyValue("COMMENT", "test comment " + transaction.getIncoming().getName())
    transaction.moveFile(transaction.getIncoming().getPath(), data)