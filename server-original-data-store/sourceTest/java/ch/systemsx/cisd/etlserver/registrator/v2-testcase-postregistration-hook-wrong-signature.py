execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(context, unnecessary_argument):
    jythonHookTestTool.log("post_metadata_registration")

def process(transaction):
  dataSet = transaction.createNewDataSet()
  transaction.moveFile(transaction.getIncoming().getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))

