from java.lang import IllegalArgumentException

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def pre_metadata_registration(context):
    jythonHookTestTool.log("pre_metadata_registration")
    raise IllegalArgumentException("Fail at pre_metadata_registration to cancel registration")

def process(transaction):
  dataSet = transaction.createNewDataSet()
  transaction.moveFile(transaction.getIncoming().getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))

