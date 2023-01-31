execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")
def process(transaction):
    linkds = transaction.createNewDataSet("LINK_TYPE")
    linkds.setExperiment(transaction.getExperiment("/SPACE/PROJECT/EXP"))
    linkds.setExternalCode("EXC")
    linkds.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))
